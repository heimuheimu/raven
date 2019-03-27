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

package com.heimuheimu.raven.monitor.falcon;

import com.heimuheimu.naivemonitor.falcon.FalconData;
import com.heimuheimu.naivemonitor.falcon.FalconDataCollector;
import com.heimuheimu.naivemonitor.falcon.support.AbstractExecutionDataCollector;
import com.heimuheimu.naivemonitor.falcon.support.AbstractFalconDataCollector;
import com.heimuheimu.naivemonitor.falcon.support.AbstractSocketDataCollector;
import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;
import com.heimuheimu.naivemonitor.monitor.SocketMonitor;
import com.heimuheimu.raven.IMServer;
import com.heimuheimu.raven.IMServerHolder;
import com.heimuheimu.raven.clients.IMClientManager;
import com.heimuheimu.raven.clients.IMClientManagerList;
import com.heimuheimu.raven.constant.FalconDataCollectorConstant;
import com.heimuheimu.raven.monitor.IMClientManagerMonitor;

import java.util.*;

/**
 * IM 客户端管理器信息 Falcon 监控数据采集器。该采集器采集周期为 30 秒，每次采集将会返回以下数据项：
 * <ul>
 *     <li>raven_manager_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 当前可用的 IM 客户端管理器数量</li>
 *     <li>raven_manager_avg_client_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 当前每个 IM 客户端管理器已管理的 IM 客户端平均数量</li>
 *     <li>raven_manager_max_client_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 当前单个 IM 客户端管理器已管理的 IM 客户端最大数量</li>
 *     <li>raven_manager_readable_client_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内可读的 IM 客户端数量</li>
 *     <li>raven_manager_writable_client_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内可写的 IM 客户端数量</li>
 *     <li>raven_manager_tps/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 迭代所有可用的 IM 客户端方法在 30 秒内每秒平均执行次数</li>
 *     <li>raven_manager_peak_tps/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 迭代所有可用的 IM 客户端方法在 30 秒内每秒最大执行次数</li>
 *     <li>raven_manager_avg_exec_time/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 迭代所有可用的 IM 客户端方法在 30 秒内单次操作平均执行时间</li>
 *     <li>raven_manager_max_exec_time/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 迭代所有可用的 IM 客户端方法在 30 秒内单次操作最大执行时间</li>
 *     <li>raven_manager_register_error_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内遇到的 IM 客户端注册失败错误次数</li>
 *     <li>raven_manager_communicate_error_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内遇到的 IM 客户端通信失败错误次数</li>
 *     <li>raven_manager_select_error_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内遇到的 IM 客户端选择失败错误次数</li>
 *     <li>raven_manager_socket_read_bytes/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 读取的总字节数</li>
 *     <li>raven_manager_socket_avg_read_bytes/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 每次读取的平均字节数</li>
 *     <li>raven_manager_socket_max_read_bytes/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 单次读取的最大字节数</li>
 *     <li>raven_manager_socket_written_bytes/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 写入的总字节数</li>
 *     <li>raven_manager_socket_avg_written_bytes/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 每次写入的平均字节数</li>
 *     <li>raven_manager_socket_max_written_bytes/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内 Socket 单次写入的最大字节数</li>
 * </ul>
 *
 * @author heimuheimu
 */
public class IMClientManagerDataCollector extends AbstractFalconDataCollector {

    private static final String COLLECTOR_NAME = "manager";

    private final IMClientManagerMonitor monitor = IMClientManagerMonitor.getInstance();

    private final FalconDataCollector socketDataCollector = new AbstractSocketDataCollector() {

        @Override
        protected String getCollectorName() {
            return COLLECTOR_NAME;
        }

        @Override
        public int getPeriod() {
            return FalconDataCollectorConstant.REPORT_PERIOD;
        }

        @Override
        protected String getModuleName() {
            return FalconDataCollectorConstant.MODULE_NAME;
        }

        @Override
        protected List<SocketMonitor> getSocketMonitorList() {
            return Collections.singletonList(monitor.getSocketMonitor());
        }
    };

    private final FalconDataCollector executionDataCollector = new AbstractExecutionDataCollector() {

        @Override
        protected List<ExecutionMonitor> getExecutionMonitorList() {
            return Collections.singletonList(monitor.getExecutionMonitor());
        }

        @Override
        protected Map<Integer, String> getErrorMetricSuffixMap() {
            Map<Integer, String> errorMetricSuffixMap = new HashMap<>();
            errorMetricSuffixMap.put(IMClientManagerMonitor.ERROR_CODE_FAILS_TO_REGISTER, "_register_error_count");
            errorMetricSuffixMap.put(IMClientManagerMonitor.ERROR_CODE_FAILS_TO_COMMUNICATE, "_communicate_error_count");
            errorMetricSuffixMap.put(IMClientManagerMonitor.ERROR_CODE_FAILS_TO_SELECT, "_select_error_count");
            return errorMetricSuffixMap;
        }

        @Override
        protected String getCollectorName() {
            return COLLECTOR_NAME;
        }

        @Override
        protected String getModuleName() {
            return FalconDataCollectorConstant.MODULE_NAME;
        }

        @Override
        public int getPeriod() {
            return FalconDataCollectorConstant.REPORT_PERIOD;
        }
    };

    /**
     * 上一次可读的 IM 客户端数量
     */
    private volatile long lastReadableClientCount = 0;

    /**
     * 上一次可写的 IM 客户端数量
     */
    private volatile long lastWritableClientCount = 0;

    @Override
    public List<FalconData> getList() {
        List<FalconData> falconDataList = new ArrayList<>();

        long managerCount = 0;
        long totalClientCount = 0;
        long maxClientCount = 0;
        IMServer server = IMServerHolder.get();
        if (server != null) {
            IMClientManagerList managerList = server.getManagerList();
            if (managerList != null) {
                int poolSize = managerList.getPoolSize();
                for (int i = 0; i < poolSize; i++) {
                    IMClientManager manager = managerList.get(i);
                    if (manager != null) {
                        int clientCount = manager.getClientCount();
                        if (clientCount >= 0) {
                            managerCount ++;
                            totalClientCount += clientCount;
                            maxClientCount = maxClientCount < clientCount ? clientCount : maxClientCount;
                        }
                    }
                }
            }
        }
        falconDataList.add(create("_count", managerCount));
        long averageClientCount = 0;
        if (managerCount > 0) {
            averageClientCount = totalClientCount / managerCount;
        }
        falconDataList.add(create("_avg_client_count", averageClientCount));
        falconDataList.add(create("_max_client_count", maxClientCount));

        long readableClientCount = monitor.getReadableClientCount();
        falconDataList.add(create("_readable_client_count", readableClientCount - lastReadableClientCount));
        lastReadableClientCount = readableClientCount;

        long writableClientCount = monitor.getWritableClientCount();
        falconDataList.add(create("_writable_client_count", writableClientCount - lastWritableClientCount));
        lastWritableClientCount = writableClientCount;

        falconDataList.addAll(executionDataCollector.getList());
        falconDataList.addAll(socketDataCollector.getList());
        return falconDataList;
    }

    @Override
    protected String getCollectorName() {
        return COLLECTOR_NAME;
    }

    @Override
    protected String getModuleName() {
        return FalconDataCollectorConstant.MODULE_NAME;
    }

    @Override
    public int getPeriod() {
        return FalconDataCollectorConstant.REPORT_PERIOD;
    }
}
