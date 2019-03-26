package com.heimuheimu.raven.clients;

import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;
import com.heimuheimu.naivemonitor.monitor.SocketMonitor;
import com.heimuheimu.raven.constant.BeanStatusEnum;
import com.heimuheimu.raven.exception.RavenException;
import com.heimuheimu.raven.exception.RejectedRegisterException;
import com.heimuheimu.raven.facility.UnusableServiceNotifier;
import com.heimuheimu.raven.monitor.IMClientManagerMonitor;
import com.heimuheimu.raven.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * IM 客户端管理器。
 *
 * <p><strong>说明：</strong>IMClientManager 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class IMClientManager implements Closeable {

    private static final Logger RAVEN_IM_CLIENT_LOG = LoggerFactory.getLogger("RAVEN_IM_CLIENT_LOG");

    private static final Logger RAVEN_IM_CLIENT_MANAGER_LOG = LoggerFactory.getLogger("RAVEN_IM_CLIENT_MANAGER_LOG");
    
    private static final Logger LOGGER = LoggerFactory.getLogger(IMClientManager.class);

    /**
     * IM 客户端管理器信息监控器
     */
    private static final IMClientManagerMonitor MANAGER_MONITOR = IMClientManagerMonitor.getInstance();

    /**
     * IM 客户端管理器名称
     */
    private final String name;

    /**
     * IM 客户端管理器使用的配置信息
     */
    private final IMClientManagerConfiguration configuration;

    /**
     * IMClientManager 后台线程
     */
    private IMClientManagerTask imClientManagerTask;

    /**
     * 当前 IMClientManager 实例所处状态
     */
    private volatile BeanStatusEnum state = BeanStatusEnum.UNINITIALIZED;

    /**
     * 构造一个 IMClientManager 实例。
     *
     * @param name IM 客户端管理器名称，不允许为 {@code null}
     * @param configuration IM 客户端管理器使用的配置信息，不允许为 {@code null}
     */
    public IMClientManager(String name, IMClientManagerConfiguration configuration) {
        this.name = name;
        this.configuration = configuration;
    }

    /**
     * 启动 IMClientManager，如果 IMClientManager 已经启动过一次，调用该方法不会有任何效果。
     *
     * @throws RavenException 如果 IMClientManager 启动失败，将会抛出此异常
     */
    public synchronized void init() throws RavenException {
        if (state == BeanStatusEnum.UNINITIALIZED) {
            long startTime = System.currentTimeMillis();
            state = BeanStatusEnum.NORMAL;
            LinkedHashMap<String, Object> params = buildParamsMap();
            params.put("configuration", configuration);
            try {
                imClientManagerTask = new IMClientManagerTask();
                imClientManagerTask.setName(name);
                imClientManagerTask.start();

                params.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                RAVEN_IM_CLIENT_MANAGER_LOG.info("Started IMClientManager.{}", LogBuildUtil.build(params));
            } catch (Exception e) {
                params.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                String errorMessage = "IMClientManager fails to start: `unexpected error`." + LogBuildUtil.build(params);
                LOGGER.error(errorMessage, e);
                close();
                throw new RavenException(errorMessage, e);
            }
        }
    }

    /**
     * 关闭 IMClientManager，如果 IMClientManager 已经关闭，调用该方法不会有任何效果。
     *
     * <p><strong>注意：</strong>关闭 IMClientManager 并不会关闭其管理的 IM 客户端，在程序运行期间通常不应调用此方法。</p>
     */
    @Override
    public synchronized void close() {
        if (state != BeanStatusEnum.CLOSED) {
            long startTime = System.currentTimeMillis();
            state = BeanStatusEnum.CLOSED;
            LinkedHashMap<String, Object> params = buildParamsMap();
            params.put("configuration", configuration);
            try {
                if (imClientManagerTask != null) {
                    imClientManagerTask.close();
                }

                params.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                RAVEN_IM_CLIENT_MANAGER_LOG.info("Stopped IMClientManager.{}", LogBuildUtil.build(params));
            } catch (Exception e) {
                params.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                LOGGER.error("IMClientManager fails to stop: `unexpected error`." + LogBuildUtil.build(params), e);
            } finally {
                UnusableServiceNotifier<IMClientManager> unusableServiceNotifier = configuration.getUnusableServiceNotifier();
                if (unusableServiceNotifier != null) {
                    unusableServiceNotifier.onClosed(this);
                }
            }
        }
    }

    /**
     * 判断当前 IM 客户端管理器是否可用。
     *
     * @return 当前 IMClientManager 是否可用
     */
    public boolean isActive() {
        return state == BeanStatusEnum.NORMAL;
    }

    /**
     * 获得当前 IM 客户端管理器名称。
     *
     * @return IM 客户端管理器名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获得当前 IM 客户端管理器使用的 Selector，如果客户端管理器已关闭或未初始化，将会返回 {@code null}。
     *
     * @return IM 客户端管理器使用的 Selector
     */
    public Selector getSelector() {
        if (state != BeanStatusEnum.NORMAL) {
            return null;
        }
        return imClientManagerTask.selector;
    }

    /**
     * 获得当前 IM 客户端管理器管理的 IMClient 数量，如果客户端管理器已关闭，将会返回 -1。
     *
     * @return 当前 IM 客户端管理器管理的 IMClient 数量
     */
    public int getClientCount() {
        if (state != BeanStatusEnum.NORMAL) {
            return -1;
        }
        return imClientManagerTask.getCount();
    }

    /**
     * 将 IMClient 注册到当前 IM 客户端管理器中。
     *
     * @param client IM 客户端，不允许为 {@code null}
     * @throws IllegalStateException 如果当前 IM 客户端管理器未初始化或已关闭，将会抛出此异常
     * @throws RejectedRegisterException 如果注册的 IMClient 数量已达到上限，将会抛出此异常
     * @throws RavenException 如果在注册过程中发生其它未知错误，将会抛出此异常
     */
    public void register(IMClient client) throws IllegalStateException, RavenException {
        if (state != BeanStatusEnum.NORMAL) {
            LinkedHashMap<String, Object> params = buildParamsMap();
            params.put("client", client);
            String errorMessage = "IMClient fails to register: `illegal state`." + LogBuildUtil.build(params);
            LOGGER.error(errorMessage);
            MANAGER_MONITOR.getExecutionMonitor().onError(IMClientManagerMonitor.ERROR_CODE_FAILS_TO_REGISTER);
            throw new IllegalStateException(errorMessage);
        }

        int capacity = configuration.getCapacity();
        if (capacity > 0) {
            if (getClientCount() >= capacity) {
                LinkedHashMap<String, Object> params = buildParamsMap();
                params.put("client", client);
                String errorMessage = "IMClient fails to register: `too many IMClient`." + LogBuildUtil.build(params);
                LOGGER.error(errorMessage);
                MANAGER_MONITOR.getExecutionMonitor().onError(IMClientManagerMonitor.ERROR_CODE_FAILS_TO_REGISTER);
                throw new RejectedRegisterException(errorMessage);
            }
        }

        try {
            imClientManagerTask.register(client);
        } catch (Exception e) {
            LinkedHashMap<String, Object> params = buildParamsMap();
            params.put("client", client);
            String errorMessage = "IMClient fails to register: `unexpected error`." + LogBuildUtil.build(params);
            LOGGER.error(errorMessage, e);
            MANAGER_MONITOR.getExecutionMonitor().onError(IMClientManagerMonitor.ERROR_CODE_FAILS_TO_REGISTER);
            throw new RavenException(errorMessage, e);
        }
    }

    private LinkedHashMap<String, Object> buildParamsMap() {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("name", name);
        params.put("state", state);
        return params;
    }

    private class IMClientManagerTask extends Thread {

        /**
         * SocketChannel 选择器
         */
        private final Selector selector;

        /**
         * 任务运行标志位
         */
        private volatile boolean isRunning = true;

        /**
         * 任务暂停标志位
         */
        private volatile boolean isPausing = false;

        /**
         * 接收 IM 客户端发送的数据使用的字节缓存
         */
        private final ByteBuffer buffer;

        /**
         * 构造一个 IMClientManagerTask 实例。
         *
         * @throws IOException 如果 SocketChannel 选择器创建失败，将会抛出此异常
         */
        public IMClientManagerTask() throws IOException {
            this.selector = Selector.open();
            int receiveBufferSize = configuration.getReceiveBufferSize();
            if (receiveBufferSize <= 0) {
                receiveBufferSize = 32 * 1024;
            }
            this.buffer = ByteBuffer.allocate(receiveBufferSize);
        }

        /**
         * 获得 Selector 中已注册的 SocketChannel 数量，即当前 IMClientManager 管理的 IMClient 数量，如果 Selector 已关闭，
         * 将会返回 -1。
         *
         * @return Selector 中已注册的 SocketChannel 数量
         */
        public int getCount() {
            if (selector.isOpen()) {
                return selector.keys().size();
            } else {
                return -1;
            }
        }

        public synchronized void register(IMClient client) throws IOException {
            try {
                isPausing = true;
                selector.wakeup();
                SocketChannel socketChannel = client.getSocketChannel();
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, SelectionKey.OP_READ, client);
            } finally {
                isPausing = false;
            }
        }

        @Override
        public void run() {
            SocketMonitor socketMonitor = MANAGER_MONITOR.getSocketMonitor();
            ExecutionMonitor executionMonitor = MANAGER_MONITOR.getExecutionMonitor();

            while (isRunning) {
                if (!isPausing) {
                    try {
                        int readyChannels = selector.select();
                        if (readyChannels == 0) { // 如果无可用 channel，进入下一次循环
                            continue;
                        }
                        long startNanoTime = System.nanoTime();
                        try {
                            Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                            while (selectionKeyIterator.hasNext()) {
                                SelectionKey selectionKey = selectionKeyIterator.next();
                                SocketChannel channel = (SocketChannel) selectionKey.channel();
                                IMClient client = (IMClient) selectionKey.attachment();
                                try {
                                    if (selectionKey.isValid() && selectionKey.isWritable()) {
                                        MANAGER_MONITOR.incrementWritableClient();
                                        ByteBuffer writeBuffer = client.getBufferForWrite();
                                        if (writeBuffer != null && writeBuffer.remaining() > 0) {
                                            int writeBytes = channel.write(writeBuffer);
                                            if (writeBytes > 0) {
                                                socketMonitor.onWritten(writeBytes);
                                            }
                                        }
                                        client.afterWrite();
                                    }
                                    
                                    if (selectionKey.isValid() && selectionKey.isReadable()) {
                                        MANAGER_MONITOR.incrementReadableClient();
                                        buffer.clear();
                                        int readBytes;
                                        do {
                                            readBytes = channel.read(buffer);
                                            if (readBytes > 0) {
                                                socketMonitor.onRead(readBytes);
                                            }
                                        } while (buffer.remaining() > 0 && readBytes > 0);

                                        if (buffer.position() > 0) {
                                            buffer.flip();
                                            client.receive(buffer);
                                        }

                                        if (readBytes == -1) {
                                            LinkedHashMap<String, Object> params = new LinkedHashMap<>();
                                            try {
                                                params.put("remote", channel.getRemoteAddress());
                                                params.put("local", channel.getLocalAddress());
                                            } catch (Exception ignored) {}
                                            params.put("id", client.getId());
                                            params.put("manager", getName());
                                            RAVEN_IM_CLIENT_LOG.info("IMClient has reached end-of-stream.{}", LogBuildUtil.build(params));
                                            client.close();
                                        }
                                    }
                                } catch (Exception e) {
                                    LinkedHashMap<String, Object> params = new LinkedHashMap<>();
                                    try {
                                        params.put("remote", client.getSocketChannel().getRemoteAddress());
                                        params.put("local", client.getSocketChannel().getLocalAddress());
                                    } catch (Exception ignored) {}
                                    params.put("id", client.getId());
                                    params.put("manager", getName());
                                    RAVEN_IM_CLIENT_LOG.error("IMClient fails to communicate: `unexpected error`." + LogBuildUtil.build(params), e);
                                    executionMonitor.onError(IMClientManagerMonitor.ERROR_CODE_FAILS_TO_COMMUNICATE);
                                    client.close();
                                }
                                selectionKeyIterator.remove();
                            }
                        } finally {
                            executionMonitor.onExecuted(startNanoTime);
                        }
                    } catch (ClosedSelectorException e) {
                        break; // break while loop anyway
                    } catch (Exception e) {
                        LinkedHashMap<String, Object> params = buildParamsMap();
                        LOGGER.error("IMClientManager fails to select: `unexpected error`." + LogBuildUtil.build(params), e);
                        executionMonitor.onError(IMClientManagerMonitor.ERROR_CODE_FAILS_TO_SELECT);
                        try {
                            Thread.sleep(500);
                        } catch (Exception ignored) {}
                    }
                }
            }

            IMClientManager.this.close(); // make sure close IMClientManager
        }

        public void close() throws IOException {
            this.isRunning = false;
            selector.close();
        }
    }
}
