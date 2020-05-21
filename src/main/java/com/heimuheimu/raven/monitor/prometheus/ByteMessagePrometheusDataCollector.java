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
import com.heimuheimu.raven.monitor.ByteMessageMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * 发送字节消息监控信息采集器，采集时会返回以下数据：
 * <ul>
 *     <li>raven_byte_message_count 相邻两次采集周期内需要发送的消息总数</li>
 *     <li>raven_byte_message_total_bytes 相邻两次采集周期内需要发送的字节总数</li>
 *     <li>raven_byte_message_max_bytes 相邻两次采集周期内需要发送的单条消息最大字节数</li>
 *     <li>raven_byte_message_error_count 相邻两次采集周期内发送失败的消息总数</li>
 *     <li>raven_byte_message_sent_count 相邻两次采集周期内发送成功的消息总数</li>
 *     <li>raven_byte_message_sent_avg_delay_milliseconds 相邻两次采集周期内发送成功的消息平均延迟时间，单位：毫秒</li>
 *     <li>raven_byte_message_sent_max_delay_milliseconds 相邻两次采集周期内发送成功的消息最大延迟时间，单位：毫秒</li>
 * </ul>
 *
 * @author heimuheimu
 */
public class ByteMessagePrometheusDataCollector implements PrometheusCollector {

    /**
     * 差值计算器
     */
    private final DeltaCalculator deltaCalculator = new DeltaCalculator();

    @Override
    public List<PrometheusData> getList() {
        ByteMessageMonitor monitor = ByteMessageMonitor.getInstance();
        List<PrometheusData> dataList = new ArrayList<>();
        // add raven_byte_message_count
        dataList.add(PrometheusData.buildGauge("raven_byte_message_count", "")
                .addSample(PrometheusSample.build(deltaCalculator.delta("MessageCount", monitor.getCount()))));
        // add raven_byte_message_total_bytes
        dataList.add(PrometheusData.buildGauge("raven_byte_message_total_bytes", "")
                .addSample(PrometheusSample.build(deltaCalculator.delta("TotalBytes", monitor.getTotalByteLength()))));
        // add raven_byte_message_max_bytes
        dataList.add(PrometheusData.buildGauge("raven_byte_message_max_bytes", "")
                .addSample(PrometheusSample.build(monitor.getMaxByteLength())));
        monitor.resetMaxByteLength();
        // add raven_byte_message_error_count
        dataList.add(PrometheusData.buildGauge("raven_byte_message_error_count", "")
                .addSample(PrometheusSample.build(deltaCalculator.delta("ErrorCount", monitor.getErrorCount()))));
        // add raven_byte_message_sent_count
        double sentCount = deltaCalculator.delta("SentCount", monitor.getSentCount());
        dataList.add(PrometheusData.buildGauge("raven_byte_message_sent_count", "")
                .addSample(PrometheusSample.build(sentCount)));
        // add raven_byte_message_sent_avg_delay_milliseconds
        double avgDelayedMilliseconds = sentCount > 0 ? deltaCalculator.delta("TotalDelayedMills", monitor.getTotalDelayedMills()) / sentCount : 0;
        dataList.add(PrometheusData.buildGauge("raven_byte_message_sent_avg_delay_milliseconds", "")
                .addSample(PrometheusSample.build(avgDelayedMilliseconds)));
        // add raven_byte_message_sent_max_delay_milliseconds
        dataList.add(PrometheusData.buildGauge("raven_byte_message_sent_max_delay_milliseconds", "")
                .addSample(PrometheusSample.build(monitor.getMaxDelayedMills())));
        monitor.resetMaxDelayedMills();
        return dataList;
    }
}
