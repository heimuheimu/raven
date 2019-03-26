package com.heimuheimu.raven.monitor.falcon;

import com.heimuheimu.naivemonitor.falcon.FalconData;
import com.heimuheimu.naivemonitor.falcon.support.AbstractFalconDataCollector;
import com.heimuheimu.raven.constant.FalconDataCollectorConstant;
import com.heimuheimu.raven.monitor.ByteMessageMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * 发送字节消息信息 Falcon 监控数据采集器。该采集器采集周期为 30 秒，每次采集将会返回以下数据项：
 * <ul>
 *     <li>raven_byte_message_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内需要发送的消息总数</li>
 *     <li>raven_byte_message_total_byte_length/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内需要发送的字节总长度</li>
 *     <li>raven_byte_message_avg_byte_length/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内需要发送的单条消息平均字节长度</li>
 *     <li>raven_byte_message_max_byte_length/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内需要发送的单条消息最大字节长度</li>
 *     <li>raven_byte_message_error_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内发送失败的消息总数</li>
 *     <li>raven_byte_message_sent_count/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内发送成功的消息总数</li>
 *     <li>raven_byte_message_sent_avg_delay/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内发送成功的消息平均延迟时间，单位：毫秒</li>
 *     <li>raven_byte_message_sent_max_delay/module=raven &nbsp;&nbsp;&nbsp;&nbsp; 30 秒内发送成功的消息最大延迟时间，单位：毫秒</li>
 * </ul>
 *
 * @author heimuheimu
 */
public class ByteMessageDataCollector extends AbstractFalconDataCollector {

    /**
     * 上一次需要发送的消息总数
     */
    private volatile long lastCount = 0;

    /**
     * 上一次需要发送的字节总长度
     */
    private volatile long lastTotalByteLength = 0;

    /**
     * 上一次发送失败的消息总数
     */
    private volatile long lastErrorCount = 0;

    /**
     * 上一次已发送的消息总数
     */
    private volatile long lastSentCount = 0;

    /**
     * 上一次已发送的消息延迟总时间，单位：毫秒
     */
    private volatile long lastTotalDelayedMills = 0;

    @Override
    public List<FalconData> getList() {
        ByteMessageMonitor monitor = ByteMessageMonitor.getInstance();
        List<FalconData> falconDataList = new ArrayList<>();

        long count = monitor.getCount();
        long periodCount = count - lastCount;
        falconDataList.add(create("_byte_message_count", periodCount));
        lastCount = count;

        long totalByteLength = monitor.getTotalByteLength();
        long periodTotalByteLength = totalByteLength - lastTotalByteLength;
        falconDataList.add(create("_byte_message_total_byte_length", periodTotalByteLength));
        lastTotalByteLength = totalByteLength;

        long averageByteLength = 0;
        if (periodCount > 0) {
            averageByteLength = periodTotalByteLength / periodCount;
        }
        falconDataList.add(create("_byte_message_avg_byte_length", averageByteLength));

        falconDataList.add(create("_byte_message_max_byte_length", monitor.getMaxByteLength()));
        monitor.resetMaxByteLength();

        long errorCount = monitor.getErrorCount();
        falconDataList.add(create("_byte_message_error_count", errorCount - lastErrorCount));
        lastErrorCount = errorCount;

        long sentCount = monitor.getSentCount();
        long periodSentCount = sentCount - lastSentCount;
        falconDataList.add(create("_byte_message_sent_count", periodSentCount));
        lastSentCount = sentCount;

        long totalDelayedMills = monitor.getTotalDelayedMills();
        long averageDelayedMills = 0;
        if (periodSentCount > 0) {
            averageDelayedMills = (totalDelayedMills - lastTotalDelayedMills) / periodSentCount;
        }
        falconDataList.add(create("_byte_message_sent_avg_delay", averageDelayedMills));
        lastTotalDelayedMills = totalDelayedMills;

        falconDataList.add(create("_byte_message_sent_max_delay", monitor.getMaxDelayedMills()));
        monitor.resetMaxDelayedMills();
        return falconDataList;
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
