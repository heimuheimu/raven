/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 heimuheimu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.heimuheimu.raven;

import com.heimuheimu.raven.clients.*;
import com.heimuheimu.raven.clients.support.IMClientUUIDGenerator;
import com.heimuheimu.raven.constant.BeanStatusEnum;
import com.heimuheimu.raven.exception.RavenException;
import com.heimuheimu.raven.exception.RejectedRegisterException;
import com.heimuheimu.raven.monitor.IMClientMonitor;
import com.heimuheimu.raven.net.SocketConfiguration;
import com.heimuheimu.raven.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IM 服务提供者，允许 IM 客户端与其建立连接进行数据通信。
 *
 * <p><strong>说明：</strong>IMServer 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class IMServer implements Closeable {

    private static final Logger RAVEN_IM_CLIENT_LOG = LoggerFactory.getLogger("RAVEN_IM_CLIENT_LOG");
    
    private static final Logger LOGGER = LoggerFactory.getLogger(IMServer.class);

    /**
     * 当前 IMServer 使用的配置信息
     */
    private final IMServerConfiguration configuration;

    /**
     * IM 客户端拦截器。
     */
    private final IMClientInterceptor clientInterceptor;

    /**
     * IM 客户端信息监控器
     */
    private final IMClientMonitor clientMonitor = IMClientMonitor.getInstance();

    /**
     * 连接成功的 IM 客户端 Map，Key 为 IM 客户端唯一 ID，Value 为对应的 IM 客户端
     */
    private final ConcurrentHashMap<String, IMClient> ESTABLISHED_CLIENT_MAP = new ConcurrentHashMap<>();

    /**
     * 当前 IMServer 实例所处状态
     */
    private volatile BeanStatusEnum state = BeanStatusEnum.UNINITIALIZED;

    /**
     * IM 客户端管理器列表
     */
    private volatile IMClientManagerList managerList = null;

    /**
     * 超时 IM 客户端检测器，如果没有配置超时时间，则为 {@code null}
     */
    private TimeoutIMClientScanner timeoutIMClientScanner;

    /**
     * IMServer 后台线程，通过监听端口与 IM 客户端建立连接
     */
    private IMServerTask imServerTask;

    /**
     * 构造一个 IMServer 实例。
     */
    public IMServer() {
        this(null);
    }

    /**
     * 构造一个 IMServer 实例。
     *
     * @param configuration IMServer 使用的配置信息，允许为 {@code null}
     */
    public IMServer(IMServerConfiguration configuration) {
        if (configuration == null) {
            configuration = new IMServerConfiguration();
        }
        this.configuration = configuration;
        this.clientInterceptor = configuration.getClientInterceptor() != null ?
                configuration.getClientInterceptor() : new IMClientInterceptorSkeleton() {};
    }

    /**
     * 启动 IMServer，如果 IMServer 已经启动过一次，调用该方法不会有任何效果。
     *
     * @throws RavenException 如果 IMServer 启动失败，将会抛出此异常
     */
    public synchronized void init() throws RavenException {
        if (state == BeanStatusEnum.UNINITIALIZED) {
            long startTime = System.currentTimeMillis();
            state = BeanStatusEnum.NORMAL;
            IMServerHolder.add(this);
            try {
                int poolSize = configuration.getPoolSize();
                if (poolSize <= 0) {
                    poolSize = 20;
                }
                IMClientManagerConfiguration managerConfiguration = configuration.getClientManagerConfiguration();
                if (managerConfiguration == null) {
                    managerConfiguration = new IMClientManagerConfiguration();
                }

                managerList = new IMClientManagerList(poolSize, managerConfiguration, configuration.getClientManagerListListener());

                imServerTask = new IMServerTask();
                imServerTask.setName("raven-im-server");
                imServerTask.start();

                if (configuration.getClientTimeout() > 0) {
                    timeoutIMClientScanner = new TimeoutIMClientScanner(this);
                    timeoutIMClientScanner.init();
                } else {
                    timeoutIMClientScanner = null;
                }

                LinkedHashMap<String, Object> params = buildParamsMap();
                params.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                RAVEN_IM_CLIENT_LOG.info("Started IMServerTask.{}", LogBuildUtil.build(params));
            } catch (Exception e) {
                String errorMessage = "IMServerTask fails to start: `unexpected error`." + LogBuildUtil.build(buildParamsMap());
                LOGGER.error(errorMessage, e);
                close();
                throw new RavenException(errorMessage, e);
            }
        }
    }

    /**
     * 关闭 IMServer，如果 IMServer 已经关闭，调用该方法不会有任何效果。
     */
    @Override
    public synchronized void close() {
        if (state != BeanStatusEnum.CLOSED) {
            long startTime = System.currentTimeMillis();
            state = BeanStatusEnum.CLOSED;
            try {
                if (timeoutIMClientScanner != null) {
                    timeoutIMClientScanner.close();
                }

                if (imServerTask != null) {
                    imServerTask.close();
                }

                if (managerList != null) {
                    managerList.close();
                }

                for (String clientId : ESTABLISHED_CLIENT_MAP.keySet()) {
                    IMClient client = ESTABLISHED_CLIENT_MAP.get(clientId);
                    if (client != null && client.isActive()) {
                        client.close();
                    }
                }

                LinkedHashMap<String, Object> params = buildParamsMap();
                params.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                RAVEN_IM_CLIENT_LOG.info("Stopped IMServerTask.{}", LogBuildUtil.build(params));
            } catch (Exception e) {
                LinkedHashMap<String, Object> params = buildParamsMap();
                params.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                LOGGER.error("IMServerTask fails to stop: `unexpected error`." + LogBuildUtil.build(params), e);
            }
        }
    }

    @Override
    public String toString() {
        return "IMServer{" +
                "configuration=" + configuration +
                ", clientInterceptor=" + clientInterceptor +
                ", clientMonitor=" + clientMonitor +
                ", state=" + state +
                '}';
    }

    /**
     * 获得当前 IMServer 使用的配置信息。
     *
     * @return 当前 IMServer 使用的配置信息
     */
    public IMServerConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * 获得 IM 客户端管理器列表，如果 IMServer 已关闭或未初始化，将会返回 {@code null}。
     *
     * @return IM 客户端管理器列表
     */
    public IMClientManagerList getManagerList() {
        if (state != BeanStatusEnum.NORMAL) {
            return null;
        }
        return managerList;
    }

    /**
     * 获得当前连接成功的 IM 客户端总数，如果 IMServer 已关闭或未初始化，将会返回 -1。
     *
     * @return 当前连接成功的 IM 客户端总数
     */
    public int getEstablishedClientCount() {
        if (state != BeanStatusEnum.NORMAL) {
            return -1;
        }
        return ESTABLISHED_CLIENT_MAP.size();
    }

    /**
     * 根据 ID 获得对应的 IM 客户端，如果该客户端不存在或已关闭，将会返回 {@code null}。
     *
     * @param clientId IM 客户端 ID
     * @return IM 客户端，可能为 {@code null}
     */
    public IMClient getEstablishedClient(String clientId) {
        return ESTABLISHED_CLIENT_MAP.get(clientId);
    }

    /**
     * 获得当前连接成功的 IM 客户端 Map，Key 为 IM 客户端唯一 ID，Value 为对应的 IM 客户端，如果 IMServer 已关闭或未初始化，
     * 将会返回空 Map，该方法不会返回 {@code null}。
     *
     * <p><strong>注意：</strong>返回的 Map 是只读的，不可修改。</p>
     *
     * @return 当前连接成功的 IM 客户端 Map，Key 为 IM 客户端唯一 ID，Value 为对应的 IM 客户端
     */
    public Map<String, IMClient> getEstablishedClientMap() {
        if (state != BeanStatusEnum.NORMAL) {
            return Collections.unmodifiableMap(new HashMap<>());
        }
        return Collections.unmodifiableMap(ESTABLISHED_CLIENT_MAP);
    }

    private LinkedHashMap<String, Object> buildParamsMap() {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("configuration", configuration);
        params.put("state", state);
        return params;
    }

    private void onClientClosed(IMClient unavailableClient) {
        String id = unavailableClient.getId();
        if (ESTABLISHED_CLIENT_MAP.remove(id, unavailableClient)) {
            clientMonitor.onClosed();
            try {
                clientInterceptor.onClosed(unavailableClient);
            } catch (Exception e) {
                LinkedHashMap<String, Object> params = new LinkedHashMap<>();
                params.put("unavailableClient", unavailableClient);
                LOGGER.error(LogBuildUtil.buildMethodExecuteFailedLog("IMClientInterceptor#onClosed(IMClient imClient)",
                        "unexpected error", params), e);
            }
        }
    }

    private class IMServerTask extends Thread {

        private volatile boolean isRunning = true;

        private final ServerSocketChannel serverSocketChannel;

        private IMServerTask() throws IOException {
            this.serverSocketChannel = ServerSocketChannel.open();
            this.serverSocketChannel.configureBlocking(true);
            this.serverSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 16 * 1024); // 16 KB
            this.serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, false);
            this.serverSocketChannel.bind(new InetSocketAddress(configuration.getPort()), configuration.getBacklog()); // 监听所有地址
        }

        @Override
        public void run() {
            SocketConfiguration socketConfiguration = configuration.getSocketConfiguration();
            if (socketConfiguration == null) {
                socketConfiguration = SocketConfiguration.DEFAULT;
            }

            IMClientIDGenerator clientIDGenerator = configuration.getClientIDGenerator();
            if (clientIDGenerator == null) {
                clientIDGenerator = new IMClientUUIDGenerator();
            }

            SocketChannel socketChannel;
            while (isRunning) {
                try {
                    socketChannel = serverSocketChannel.accept();
                } catch (ClosedChannelException ignored) { // serverSocketChannel is closed
                    break; // break while loop anyway
                } catch (Exception e) { // should not happen
                    LOGGER.error("Accepts a connection made to this channel's socket failed: `unexpected error`.", e);
                    clientMonitor.onError(IMClientMonitor.ERROR_CODE_FAILS_TO_ESTABLISH);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {}
                    continue;
                }

                if (socketChannel != null) {
                    long startTime = System.currentTimeMillis();
                    LinkedHashMap<String, Object> params = new LinkedHashMap<>();
                    try {
                        params.put("remote", socketChannel.getRemoteAddress());
                        params.put("local", socketChannel.getLocalAddress());
                    } catch (Exception ignored) {}

                    try {
                        socketConfiguration.apply(socketChannel);
                        if (clientInterceptor.canConnect(socketChannel)) {
                            IMClientManager manager = managerList.getIdleManager();
                            if (manager != null) {
                                String id = clientIDGenerator.generate();
                                params.put("id", id);
                                IMClient client = new IMClient(id, socketChannel, manager.getSelector(),
                                        configuration.getMaxWriteByteLength(), configuration.getClientListener(),
                                        IMServer.this::onClientClosed);
                                try {
                                    manager.register(client);
                                    clientInterceptor.onCreated(client);
                                    ESTABLISHED_CLIENT_MAP.put(id, client);
                                    params.put("manager", manager.getName());
                                    params.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                                    RAVEN_IM_CLIENT_LOG.info("Accepts a IMClient success.{}", LogBuildUtil.build(params));
                                    clientMonitor.onCreated();
                                } catch (Exception e) {
                                    String reason;
                                    if (e instanceof IllegalStateException) {
                                        reason = "illegal state";
                                    } else if (e instanceof RejectedRegisterException) {
                                        reason = "too many IMClient";
                                    } else {
                                        reason = "unexpected error";
                                    }
                                    params.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                                    RAVEN_IM_CLIENT_LOG.error("IMClient fails to establish: `" + reason + "`." + LogBuildUtil.build(params), e);
                                    clientMonitor.onError(IMClientMonitor.ERROR_CODE_FAILS_TO_ESTABLISH);
                                    client.close();
                                }
                            } else {
                                params.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                                RAVEN_IM_CLIENT_LOG.error("IMClient fails to establish: `no available IMClientManager`.{}", LogBuildUtil.build(params));
                                clientMonitor.onError(IMClientMonitor.ERROR_CODE_FAILS_TO_ESTABLISH);
                                closeSocketChannel(socketChannel);
                            }
                        } else {
                            params.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                            RAVEN_IM_CLIENT_LOG.error("IMClient fails to establish: `blocked by interceptor`.{}", LogBuildUtil.build(params));
                            clientMonitor.onError(IMClientMonitor.ERROR_CODE_FAILS_TO_ESTABLISH);
                            closeSocketChannel(socketChannel);
                        }
                    } catch (Exception e) {
                        params.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                        RAVEN_IM_CLIENT_LOG.error("IMClient fails to establish: `unexpected error`." + LogBuildUtil.build(params), e);
                        clientMonitor.onError(IMClientMonitor.ERROR_CODE_FAILS_TO_ESTABLISH);
                        closeSocketChannel(socketChannel);
                    }
                } else { // should not happen, just for bug detection
                    RAVEN_IM_CLIENT_LOG.error("IMClient fails to establish: `null SocketChannel`.");
                    clientMonitor.onError(IMClientMonitor.ERROR_CODE_FAILS_TO_ESTABLISH);
                }
            }

            IMServer.this.close(); // Make sure close IMServer
        }

        /**
         * 关闭指定的 SocketChannel 实例。
         *
         * @param socketChannel SocketChannel 实例，允许为 {@code null}
         */
        private void closeSocketChannel(SocketChannel socketChannel) {
            long startTime = System.currentTimeMillis();
            try {
                if (socketChannel != null && socketChannel.isOpen()) {
                    socketChannel.configureBlocking(true);
                    socketChannel.setOption(StandardSocketOptions.SO_LINGER, 0); // avoid TIME_WAIT being a problem
                    socketChannel.close();
                }
            } catch (Exception e) {
                LinkedHashMap<String, Object> params = buildParamsMap();
                params.put("socketChannel", socketChannel);
                params.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                LOGGER.error("SocketChannel fails to close: `unexpected error`." + LogBuildUtil.build(params), e);
            }
        }

        private void close() throws IOException {
            this.isRunning = false;
            serverSocketChannel.close();
        }
    }
}
