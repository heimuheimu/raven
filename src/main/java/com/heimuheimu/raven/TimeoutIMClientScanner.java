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

import com.heimuheimu.raven.clients.IMClient;
import com.heimuheimu.raven.constant.BeanStatusEnum;
import com.heimuheimu.raven.exception.RavenException;
import com.heimuheimu.raven.monitor.IMClientMonitor;
import com.heimuheimu.raven.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 超时 IM 客户端检测器，对超时的 IM 客户端执行关闭操作。
 *
 * <p><strong>说明：</strong>TimeoutIMClientScanner 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class TimeoutIMClientScanner implements Closeable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeoutIMClientScanner.class);

    private static final Logger RAVEN_IM_CLIENT_LOG = LoggerFactory.getLogger("RAVEN_IM_CLIENT_LOG");

    private static final Logger RAVEN_IM_CLIENT_TIMEOUT_LOG = LoggerFactory.getLogger("RAVEN_IM_CLIENT_TIMEOUT_LOG");

    /**
     * IM 服务提供者
     */
    private final IMServer server;

    /**
     * 超时 IM 客户端扫描任务
     */
    private ScanTask task;

    /**
     * 当前 TimeoutIMClientScanner 实例所处状态
     */
    private BeanStatusEnum state = BeanStatusEnum.UNINITIALIZED;

    /**
     * 构造一个 TimeoutIMClientScanner 实例。
     *
     * @param server IM 服务提供者
     */
    public TimeoutIMClientScanner(IMServer server) {
        this.server = server;
    }

    /**
     * 启动超时 IM 客户端检测器。
     */
    public synchronized void init() {
        if (state == BeanStatusEnum.UNINITIALIZED) {
            long startTime = System.currentTimeMillis();
            state = BeanStatusEnum.NORMAL;
            try {
                long clientTimeoutMills = TimeUnit.MILLISECONDS.convert(server.getConfiguration().getClientTimeout(),
                        TimeUnit.SECONDS);
                long delay = Math.min(clientTimeoutMills / 2, 60000);

                task = new ScanTask(clientTimeoutMills, delay);
                task.setName("raven-timeout-im-client-scanner");
                task.setDaemon(true);
                task.start();

                LinkedHashMap<String, Object> params = new LinkedHashMap<>();
                params.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                params.put("timeout", clientTimeoutMills + "ms");
                params.put("delay", delay + "ms");
                RAVEN_IM_CLIENT_TIMEOUT_LOG.info("Started TimeoutIMClientScanner.{}", LogBuildUtil.build(params));
            } catch (Exception e) {
                LinkedHashMap<String, Object> params = new LinkedHashMap<>();
                params.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                params.put("server", server);
                String errorMessage = "TimeoutIMClientScanner fails to start: `unexpected error`." + LogBuildUtil.build(params);
                LOGGER.error(errorMessage, e);
                close();
                throw new RavenException(errorMessage, e);
            }
        }
    }

    /**
     * 关闭超时 IM 客户端检测器。
     */
    @Override
    public synchronized void close() {
        if (state != BeanStatusEnum.CLOSED) {
            long startTime = System.currentTimeMillis();
            state = BeanStatusEnum.CLOSED;
            try {
                if (task != null) {
                    task.close();
                }
                LinkedHashMap<String, Object> params = new LinkedHashMap<>();
                params.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                params.put("server", server);
                RAVEN_IM_CLIENT_TIMEOUT_LOG.info("Stopped TimeoutIMClientScanner.{}", LogBuildUtil.build(params));
            } catch (Exception e) {
                LinkedHashMap<String, Object> params = new LinkedHashMap<>();
                params.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                params.put("server", server);
                LOGGER.error("TimeoutIMClientScanner fails to stop: `unexpected error`." + LogBuildUtil.build(params), e);
            }
        }
    }

    private class ScanTask extends Thread {

        private volatile boolean isRunning = true;

        private final long timeout;

        private final long delay;

        public ScanTask(long timeout, long delay) {
            this.timeout = timeout;
            this.delay = delay;
        }

        @Override
        public void run() {
            while (isRunning) {
                long startTime = System.currentTimeMillis();
                try {
                    Map<String, IMClient> clientMap = server.getEstablishedClientMap();
                    int count = 0;
                    int timeoutCount = 0;
                    for (String clientId : clientMap.keySet()) {
                        count++;
                        IMClient client = clientMap.get(clientId);
                        if (client != null && isTimeout(client)) {
                            timeoutCount++;
                            IMClientMonitor.getInstance().onError(IMClientMonitor.ERROR_CODE_TIMEOUT);
                            LinkedHashMap<String, Object> params = new LinkedHashMap<>();
                            try {
                                params.put("remote", client.getSocketChannel().getRemoteAddress());
                                params.put("local", client.getSocketChannel().getLocalAddress());
                            } catch (Exception ignored) {}
                            params.put("id", client.getId());
                            RAVEN_IM_CLIENT_LOG.error("IMClient timeout.{}", LogBuildUtil.build(params));
                            client.close();
                        }
                    }
                    LinkedHashMap<String, Object> params = new LinkedHashMap<>();
                    params.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                    params.put("count", count);
                    params.put("timeoutCount", timeoutCount);
                    RAVEN_IM_CLIENT_TIMEOUT_LOG.info("TimeoutIMClientScanner scan success.{}", LogBuildUtil.build(params));

                    try {
                        long sleepTime = delay - (System.currentTimeMillis() - startTime);
                        if (sleepTime > 0) {
                            Thread.sleep(sleepTime);
                        }
                    } catch (InterruptedException ignored) {}
                } catch (Exception e) {
                    LinkedHashMap<String, Object> params = new LinkedHashMap<>();
                    params.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                    params.put("server", server);
                    LOGGER.error("TimeoutIMClientScanner fails to scan: `unexpected error`." + LogBuildUtil.build(params), e);
                }
            }
        }

        private boolean isTimeout(IMClient client) {
            return System.currentTimeMillis() - client.getLastActiveTime() > timeout;
        }

        private void close() {
            this.isRunning = false;
            interrupt();
        }
    }
}
