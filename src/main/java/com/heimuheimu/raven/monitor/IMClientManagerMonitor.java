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

package com.heimuheimu.raven.monitor;

import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;
import com.heimuheimu.naivemonitor.monitor.SocketMonitor;
import com.heimuheimu.naivemonitor.monitor.factory.NaiveExecutionMonitorFactory;
import com.heimuheimu.naivemonitor.monitor.factory.NaiveSocketMonitorFactory;
import com.heimuheimu.naivemonitor.util.MonitorUtil;

import java.util.concurrent.atomic.AtomicLong;

/**
 * IM 客户端管理器信息监控器。
 *
 * <p><strong>说明：</strong>IMClientManagerMonitor 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class IMClientManagerMonitor {

    /**
     * 错误代码：IM 客户端注册时发生错误
     */
    public static final int ERROR_CODE_FAILS_TO_REGISTER = -10;

    /**
     * 错误代码：与 IM 客户端进行数据交互时发生错误
     */
    public static final int ERROR_CODE_FAILS_TO_COMMUNICATE = -20;

    /**
     * 错误代码：IM 客户端管理器在选择准备就绪的 IM 客户端时发生错误
     */
    public static final int ERROR_CODE_FAILS_TO_SELECT = -30;

    private static final IMClientManagerMonitor INSTANCE = new IMClientManagerMonitor();

    private IMClientManagerMonitor() {
        // private constructor
    }

    /**
     * IM 客户端管理器执行信息监控器
     */
    private final ExecutionMonitor executionMonitor = NaiveExecutionMonitorFactory.get("RAVEN_IM_CLIENT_MANAGER");

    /**
     *  IM 客户端 Socket 信息监控器
     */
    private final SocketMonitor socketMonitor = NaiveSocketMonitorFactory.get("RAVEN_IM_CLIENT_MANAGER");

    /**
     * 可读的 IM 客户端数量
     */
    private final AtomicLong readableClientCount = new AtomicLong();

    /**
     * 可写的 IM 客户端数量
     */
    private final AtomicLong writableClientCount = new AtomicLong();

    /**
     * 可读的 IM 客户端数量 +1。
     */
    public void incrementReadableClient() {
        MonitorUtil.safeAdd(readableClientCount, 1);
    }

    /**
     * 可写的 IM 客户端数量 +1。
     */
    public void incrementWritableClient() {
        MonitorUtil.safeAdd(writableClientCount, 1);
    }

    /**
     * 获得 IM 客户端管理器执行信息监控器。
     *
     * @return IM 客户端管理器执行信息监控器
     */
    public ExecutionMonitor getExecutionMonitor() {
        return executionMonitor;
    }

    /**
     * 获得 IM 客户端 Socket 信息监控器。
     *
     * @return IM 客户端 Socket 信息监控器
     */
    public SocketMonitor getSocketMonitor() {
        return socketMonitor;
    }

    /**
     * 获得可读的 IM 客户端数量。
     *
     * @return 可读的 IM 客户端数量
     */
    public long getReadableClientCount() {
        return readableClientCount.get();
    }

    /**
     * 获得可写的 IM 客户端数量。
     *
     * @return 可写的 IM 客户端数量
     */
    public long getWritableClientCount() {
        return writableClientCount.get();
    }

    /**
     * 获得 IM 客户端管理器信息监控器，该方法不会返回 {@code null}。
     *
     * @return IM 客户端管理器信息监控器
     */
    public static IMClientManagerMonitor getInstance() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "IMClientManagerMonitor{" +
                "executionMonitor=" + executionMonitor +
                ", socketMonitor=" + socketMonitor +
                ", readableClientCount=" + readableClientCount +
                ", writableClientCount=" + writableClientCount +
                '}';
    }
}
