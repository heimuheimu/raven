/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 heimuheimu
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

package com.heimuheimu.raven.monitor.prometheus;

import com.heimuheimu.naivemonitor.prometheus.PrometheusCollector;
import com.heimuheimu.naivemonitor.prometheus.PrometheusData;
import com.heimuheimu.naivemonitor.prometheus.PrometheusSample;
import com.heimuheimu.naivemonitor.util.DeltaCalculator;
import com.heimuheimu.raven.IMServer;
import com.heimuheimu.raven.IMServerHolder;
import com.heimuheimu.raven.clients.IMClientManager;
import com.heimuheimu.raven.clients.IMClientManagerList;
import com.heimuheimu.raven.monitor.IMClientManagerMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * IM 客户端管理器监控信息采集器，采集时会返回以下数据：
 * <ul>
 *     <li>raven_manager_count 采集时刻可用的 IM 客户端管理器数量</li>
 *     <li>raven_manager_avg_client_count 采集时刻每个 IM 客户端管理器已管理的 IM 客户端平均数量</li>
 *     <li>raven_manager_max_client_count 采集时刻单个 IM 客户端管理器已管理的 IM 客户端最大数量</li>
 *     <li>raven_manager_readable_client_count 相邻两次采集周期内可读的 IM 客户端数量，如果相同的客户端在多次迭代中都处于可读状态，会进行累加</li>
 *     <li>raven_manager_writable_client_count 相邻两次采集周期内可写的 IM 客户端数量，如果相同的客户端在多次迭代中都处于可写状态，会进行累加</li>
 * </ul>
 *
 * @author heimuheimu
 */
public class IMClientManagerPrometheusDataCollector implements PrometheusCollector {

    /**
     * 差值计算器
     */
    private final DeltaCalculator deltaCalculator = new DeltaCalculator();

    @Override
    public List<PrometheusData> getList() {
        List<PrometheusData> dataList = new ArrayList<>();
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
        // add raven_manager_count
        dataList.add(PrometheusData.buildGauge("raven_manager_count", "")
                .addSample(PrometheusSample.build(managerCount)));
        // add raven_manager_avg_client_count
        long averageClientCount = 0;
        if (managerCount > 0) {
            averageClientCount = totalClientCount / managerCount;
        }
        dataList.add(PrometheusData.buildGauge("raven_manager_avg_client_count", "")
                .addSample(PrometheusSample.build(averageClientCount)));
        // add raven_manager_max_client_count
        dataList.add(PrometheusData.buildGauge("raven_manager_max_client_count", "")
                .addSample(PrometheusSample.build(maxClientCount)));
        // add raven_manager_readable_client_count
        IMClientManagerMonitor monitor = IMClientManagerMonitor.getInstance();
        dataList.add(PrometheusData.buildGauge("raven_manager_readable_client_count", "")
                .addSample(PrometheusSample.build(deltaCalculator.delta("ReadableClientCount", monitor.getReadableClientCount()))));
        // add raven_manager_writable_client_count
        dataList.add(PrometheusData.buildGauge("raven_manager_writable_client_count", "")
                .addSample(PrometheusSample.build(deltaCalculator.delta("WritableClientCount", monitor.getWritableClientCount()))));
        return dataList;
    }
}
