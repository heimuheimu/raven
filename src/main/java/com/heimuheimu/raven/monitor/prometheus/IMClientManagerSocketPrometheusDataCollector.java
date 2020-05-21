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

import com.heimuheimu.naivemonitor.monitor.SocketMonitor;
import com.heimuheimu.naivemonitor.prometheus.PrometheusData;
import com.heimuheimu.naivemonitor.prometheus.PrometheusSample;
import com.heimuheimu.naivemonitor.prometheus.support.AbstractSocketPrometheusCollector;
import com.heimuheimu.raven.monitor.IMClientManagerMonitor;

import java.util.Collections;
import java.util.List;

/**
 * IM 客户端管理器 Socket 读、写信息采集器，采集时会返回以下数据：
 * <ul>
 *     <li>raven_manager_socket_read_count 相邻两次采集周期内 Socket 读取的次数</li>
 *     <li>raven_manager_socket_read_bytes 相邻两次采集周期内 Socket 读取的字节总数</li>
 *     <li>raven_manager_socket_max_read_bytes 相邻两次采集周期内单次 Socket 读取的最大字节数</li>
 *     <li>raven_manager_socket_write_count 相邻两次采集周期内 Socket 写入的次数</li>
 *     <li>raven_manager_socket_write_bytes 相邻两次采集周期内 Socket 写入的字节总数</li>
 *     <li>raven_manager_socket_max_write_bytes 相邻两次采集周期内单次 Socket 写入的最大字节数</li>
 * </ul>
 *
 * @author heimuheimu
 */
public class IMClientManagerSocketPrometheusDataCollector extends AbstractSocketPrometheusCollector {

    @Override
    protected String getMetricPrefix() {
        return "raven_manager";
    }

    @Override
    protected List<SocketMonitor> getMonitorList() {
        return Collections.singletonList(IMClientManagerMonitor.getInstance().getSocketMonitor());
    }

    @Override
    protected String getMonitorId(SocketMonitor monitor, int index) {
        return String.valueOf(index);
    }

    @Override
    protected void afterAddSample(int monitorIndex, PrometheusData data, PrometheusSample sample) {
        // do nothing
    }
}