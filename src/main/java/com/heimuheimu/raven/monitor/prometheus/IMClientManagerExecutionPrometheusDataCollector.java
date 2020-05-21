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

import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;
import com.heimuheimu.naivemonitor.prometheus.PrometheusData;
import com.heimuheimu.naivemonitor.prometheus.PrometheusSample;
import com.heimuheimu.naivemonitor.prometheus.support.AbstractExecutionPrometheusCollector;
import com.heimuheimu.raven.monitor.IMClientManagerMonitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IM 客户端管理器执行信息采集器，采集时会返回以下数据：
 * <ul>
 *     <li>raven_manager_exec_count 相邻两次采集周期内迭代所有可用的 IM 客户端执行次数</li>
 *     <li>raven_manager_exec_peak_tps_count 相邻两次采集周期内每秒最大迭代所有可用的 IM 客户端执行次数</li>
 *     <li>raven_manager_avg_exec_time_millisecond 相邻两次采集周期内单次迭代所有可用的 IM 客户端操作平均执行时间，单位：毫秒</li>
 *     <li>raven_manager_max_exec_time_millisecond 相邻两次采集周期内单次迭代所有可用的 IM 客户端操作最大执行时间，单位：毫秒</li>
 *     <li>raven_manager_exec_error_count{errorCode="-10",errorType="RegisterError"} 相邻两次采集周期内出现 IM 客户端注册失败的错误次数</li>
 *     <li>raven_manager_exec_error_count{errorCode="-20",errorType="CommunicateError"} 相邻两次采集周期内出现 IM 客户端通信失败的错误次数</li>
 *     <li>raven_manager_exec_error_count{errorCode="-30",errorType="SelectError"} 相邻两次采集周期内出现 IM 客户端选择失败的错误次数</li>
 * </ul>
 *
 * @author heimuheimu
 */
public class IMClientManagerExecutionPrometheusDataCollector extends AbstractExecutionPrometheusCollector {

    @Override
    protected String getMetricPrefix() {
        return "raven_manager";
    }

    @Override
    protected Map<Integer, String> getErrorTypeMap() {
        Map<Integer, String> errorTypeMap = new HashMap<>();
        errorTypeMap.put(IMClientManagerMonitor.ERROR_CODE_FAILS_TO_REGISTER, "RegisterError");
        errorTypeMap.put(IMClientManagerMonitor.ERROR_CODE_FAILS_TO_COMMUNICATE, "CommunicateError");
        errorTypeMap.put(IMClientManagerMonitor.ERROR_CODE_FAILS_TO_SELECT, "SelectError");
        return errorTypeMap;
    }

    @Override
    protected List<ExecutionMonitor> getMonitorList() {
        return Collections.singletonList(IMClientManagerMonitor.getInstance().getExecutionMonitor());
    }

    @Override
    protected String getMonitorId(ExecutionMonitor monitor, int index) {
        return String.valueOf(index);
    }

    @Override
    protected void afterAddSample(int monitorIndex, PrometheusData data, PrometheusSample sample) {
        // do nothing
    }
}
