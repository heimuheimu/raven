package com.heimuheimu.raven.monitor;

import com.heimuheimu.naivemonitor.util.MonitorUtil;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 发送字节消息信息监控器。
 *
 * <p><strong>说明：</strong>ByteMessageMonitor 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class ByteMessageMonitor {

    private static final ByteMessageMonitor INSTANCE = new ByteMessageMonitor();

    /**
     * IMClient 需要发送的消息总数
     */
    private final AtomicLong count = new AtomicLong();

    /**
     * IMClient 发送失败的消息总数
     */
    private final AtomicLong errorCount = new AtomicLong();

    /**
     * IMClient 需要发送的字节总长度
     */
    private final AtomicLong totalByteLength = new AtomicLong();

    /**
     * 单条消息最大字节长度
     */
    private volatile int maxByteLength = 0;

    /**
     * IMClient 已发送的消息总数
     */
    private final AtomicLong sentCount = new AtomicLong();

    /**
     * IMClient 已发送的消息延迟总时间，单位：毫秒
     */
    private final AtomicLong totalDelayedMills = new AtomicLong();

    /**
     * 已发送的消息最大延迟时间，单位：毫秒
     */
    private volatile long maxDelayedMills = 0;

    /**
     * 在 IMClient 发送一条字节消息时进行监控。
     *
     * @param byteLength 消息字节长度
     */
    public void onCreated(int byteLength) {
        MonitorUtil.safeAdd(count, 1);
        MonitorUtil.safeAdd(totalByteLength, byteLength);
        if (byteLength > maxByteLength) {
            maxByteLength = byteLength;
        }
    }

    /**
     * 在 IMClient 发送一条字节消息失败时进行监控。
     */
    public void onError() {
        MonitorUtil.safeAdd(errorCount, 1);
    }

    /**
     * 在 IMClient 发送完成一条字节消息时进行监控。
     *
     * @param delayMills 消息延迟时间
     */
    public void onSent(long delayMills) {
        MonitorUtil.safeAdd(sentCount, 1);
        MonitorUtil.safeAdd(totalDelayedMills, delayMills);
        if (delayMills > maxDelayedMills) {
            maxDelayedMills = delayMills;
        }
    }

    private ByteMessageMonitor() {
        // private constructor
    }

    /**
     * 获得 IMClient 需要发送的消息总数。
     *
     * @return IMClient 需要发送的消息总数
     */
    public long getCount() {
        return count.get();
    }

    /**
     * 获得 IMClient 发送失败的消息总数。
     *
     * @return IMClient 发送失败的消息总数
     */
    public long getErrorCount() {
        return errorCount.get();
    }

    /**
     * 获得 IMClient 需要发送的字节总长度。
     *
     * @return IMClient 需要发送的字节总长度
     */
    public long getTotalByteLength() {
        return totalByteLength.get();
    }

    /**
     * 获得单条消息最大字节长度。
     *
     * @return 单条消息最大字节长度
     */
    public int getMaxByteLength() {
        return maxByteLength;
    }

    /**
     * 重置单条消息最大字节长度。
     */
    public void resetMaxByteLength() {
        maxByteLength = 0;
    }

    /**
     * 获得 IMClient 已发送的消息总数。
     *
     * @return IMClient 已发送的消息总数
     */
    public long getSentCount() {
        return sentCount.get();
    }

    /**
     * 获得 IMClient 已发送的消息延迟总时间，单位：毫秒。
     *
     * @return IMClient 已发送的消息延迟总时间
     */
    public long getTotalDelayedMills() {
        return totalDelayedMills.get();
    }

    /**
     * 获得已发送的消息最大延迟时间，单位：毫秒。
     *
     * @return 已发送的消息最大延迟时间，单位：毫秒。
     */
    public long getMaxDelayedMills() {
        return maxDelayedMills;
    }

    /**
     * 重置已发送的消息最大延迟时间，单位：毫秒。
     */
    public void resetMaxDelayedMills() {
        maxDelayedMills = 0;
    }

    /**
     * 获得字节消息信息监控器，该方法不会返回 {@code null}。
     *
     * @return 字节消息信息监控器
     */
    public static ByteMessageMonitor getInstance() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "ByteMessageMonitor{" +
                "count=" + count +
                ", errorCount=" + errorCount +
                ", totalByteLength=" + totalByteLength +
                ", maxByteLength=" + maxByteLength +
                ", sentCount=" + sentCount +
                ", totalDelayedMills=" + totalDelayedMills +
                ", maxDelayedMills=" + maxDelayedMills +
                '}';
    }
}
