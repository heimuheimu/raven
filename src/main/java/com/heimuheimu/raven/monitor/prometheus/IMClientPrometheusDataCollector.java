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
import com.heimuheimu.raven.monitor.IMClientMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * IM 客户端监控信息采集器，采集时会返回以下数据：
 * <ul>
 *     <li>raven_client_established_count 采集时刻保持连接的 IM 客户端数量</li>
 *     <li>raven_client_created_count 相邻两次采集周期内创建的 IM 客户端数量</li>
 *     <li>raven_client_closed_count 相邻两次采集周期内关闭的 IM 客户端数量</li>
 *     <li>raven_client_established_error_count 相邻两次采集周期内 IM 客户端创建失败的次数</li>
 *     <li>raven_client_closed_error_count 相邻两次采集周期内 IM 客户端关闭失败的次数</li>
 *     <li>raven_client_timeout_count 相邻两次采集周期内发生超时错误的 IM 客户端数量</li>
 * </ul>
 *
 * @author heimuheimu
 */
public class IMClientPrometheusDataCollector implements PrometheusCollector {

    /**
     * 差值计算器
     */
    private final DeltaCalculator deltaCalculator = new DeltaCalculator();

    @Override
    public List<PrometheusData> getList() {
        IMClientMonitor monitor = IMClientMonitor.getInstance();
        List<PrometheusData> dataList = new ArrayList<>();
        // add raven_client_established_count
        long establishedClientCount = 0;
        IMServer server = IMServerHolder.get();
        if (server != null) {
            establishedClientCount = Math.max(server.getEstablishedClientCount(), 0);
        }
        dataList.add(PrometheusData.buildGauge("raven_client_established_count", "")
                .addSample(PrometheusSample.build(establishedClientCount)));
        // add raven_client_created_count
        dataList.add(PrometheusData.buildGauge("raven_client_created_count", "")
                .addSample(PrometheusSample.build(deltaCalculator.delta("CreatedCount", monitor.getCreatedCount()))));
        // add raven_client_closed_count
        dataList.add(PrometheusData.buildGauge("raven_client_closed_count", "")
                .addSample(PrometheusSample.build(deltaCalculator.delta("ClosedCount", monitor.getClosedCount()))));
        // add raven_client_established_error_count
        dataList.add(PrometheusData.buildGauge("raven_client_established_error_count", "")
                .addSample(PrometheusSample.build(deltaCalculator.delta("EstablishedErrorCount", monitor.getErrorCount(IMClientMonitor.ERROR_CODE_FAILS_TO_ESTABLISH)))));
        // add raven_client_closed_error_count
        dataList.add(PrometheusData.buildGauge("raven_client_closed_error_count", "")
                .addSample(PrometheusSample.build(deltaCalculator.delta("ClosedErrorCount", monitor.getErrorCount(IMClientMonitor.ERROR_CODE_FAILS_TO_CLOSE)))));
        // add raven_client_timeout_count
        dataList.add(PrometheusData.buildGauge("raven_client_timeout_count", "")
                .addSample(PrometheusSample.build(deltaCalculator.delta("TimeoutCount", monitor.getErrorCount(IMClientMonitor.ERROR_CODE_TIMEOUT)))));
        return dataList;
    }
}
