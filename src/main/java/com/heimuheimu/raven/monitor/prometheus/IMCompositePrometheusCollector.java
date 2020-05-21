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

import java.util.ArrayList;
import java.util.List;

/**
 * IM 服务相关信息复合采集器，该采集器将会收集以下采集器的信息：
 * <ul>
 *     <li>{@link ByteMessagePrometheusDataCollector} 发送字节消息监控信息采集器</li>
 *     <li>{@link IMClientPrometheusDataCollector} IM 客户端监控信息采集器</li>
 *     <li>{@link IMClientManagerPrometheusDataCollector} IM 客户端管理器监控信息采集器</li>
 *     <li>{@link IMClientManagerExecutionPrometheusDataCollector} IM 客户端管理器执行信息采集器</li>
 *     <li>{@link IMClientManagerSocketPrometheusDataCollector} IM 客户端管理器 Socket 读、写信息采集器</li>
 * </ul>
 *
 * @author heimuheimu
 */
public class IMCompositePrometheusCollector implements PrometheusCollector {

    /**
     * 发送字节消息监控信息采集器
     */
    private final ByteMessagePrometheusDataCollector byteMessageCollector;

    /**
     * IM 客户端监控信息采集器
     */
    private final IMClientPrometheusDataCollector imClientCollector;

    /**
     * IM 客户端管理器监控信息采集器
     */
    private final IMClientManagerPrometheusDataCollector imClientManagerCollector;

    /**
     * IM 客户端管理器执行信息采集器
     */
    private final IMClientManagerExecutionPrometheusDataCollector imClientManagerExecutionCollector;

    /**
     * IM 客户端管理器 Socket 读、写信息采集器
     */
    private final IMClientManagerSocketPrometheusDataCollector imClientManagerSocketCollector;

    /**
     * 构造一个 IMCompositePrometheusCollector 实例。
     */
    public IMCompositePrometheusCollector() {
        this.byteMessageCollector = new ByteMessagePrometheusDataCollector();
        this.imClientCollector = new IMClientPrometheusDataCollector();
        this.imClientManagerCollector = new IMClientManagerPrometheusDataCollector();
        this.imClientManagerExecutionCollector = new IMClientManagerExecutionPrometheusDataCollector();
        this.imClientManagerSocketCollector = new IMClientManagerSocketPrometheusDataCollector();
    }

    @Override
    public List<PrometheusData> getList() {
        List<PrometheusData> dataList = new ArrayList<>();
        dataList.addAll(byteMessageCollector.getList());
        dataList.addAll(imClientCollector.getList());
        dataList.addAll(imClientManagerCollector.getList());
        dataList.addAll(imClientManagerExecutionCollector.getList());
        dataList.addAll(imClientManagerSocketCollector.getList());
        return dataList;
    }
}
