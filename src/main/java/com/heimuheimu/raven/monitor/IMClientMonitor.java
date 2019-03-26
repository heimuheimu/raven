package com.heimuheimu.raven.monitor;

import com.heimuheimu.naivemonitor.util.MonitorUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * IM 客户端信息监控器。
 *
 * <p><strong>说明：</strong>IMClientMonitor 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class IMClientMonitor {

    /**
     * 错误代码：IM 客户端在注册过程中发生错误。
     */
    public static final int ERROR_CODE_FAILS_TO_ESTABLISH = -10;

    /**
     * 错误代码：IM 客户端在关闭过程中发生错误。
     */
    public static final int ERROR_CODE_FAILS_TO_CLOSE = -20;

    /**
     * 错误代码：IM 客户端超时。
     */
    public static final int ERROR_CODE_TIMEOUT = -30;

    private static final IMClientMonitor INSTANCE = new IMClientMonitor();

    private IMClientMonitor() {
        // private constructor
    }

    /**
     * 已创建的 IM 客户端数量
     */
    private final AtomicLong createdCount = new AtomicLong();

    /**
     * 已关闭的 IM 客户端数量
     */
    private final AtomicLong closedCount = new AtomicLong();

    /**
     * IM 客户端失败次数 Map，Key 为错误代码，Value 为该错误代码对应的失败次数
     */
    private final ConcurrentHashMap<Integer, AtomicLong> errorCountMap = new ConcurrentHashMap<>();

    /**
     * 在 IM 客户端创建后进行监控。
     */
    public void onCreated() {
        MonitorUtil.safeAdd(createdCount, 1);
    }

    /**
     * 获得已创建的 IM 客户端数量。
     *
     * @return 已创建的 IM 客户端数量
     */
    public long getCreatedCount() {
        return createdCount.get();
    }

    /**
     * 在 IM 客户端关闭后进行监控。
     */
    public void onClosed() {
        MonitorUtil.safeAdd(closedCount, 1);
    }

    /**
     * 获得已关闭的 IM 客户端数量。
     *
     * @return 已关闭的 IM 客户端数量
     */
    public long getClosedCount() {
        return closedCount.get();
    }

    /**
     * 对 IM 客户端创建或关闭过程中发生的错误进行监控，错误码对应的失败次数 +1，可通过 {@link #getErrorCount(int)} 方法进行失败次数获取。
     *
     * @param errorCode 错误代码
     */
    public void onError(int errorCode) {
        //操作执行失败总次数 +1
        AtomicLong errorCount = errorCountMap.get(errorCode);
        if (errorCount == null) {
            errorCount = new AtomicLong();
            AtomicLong prevErrorCount = errorCountMap.putIfAbsent(errorCode, errorCount);
            if (prevErrorCount != null) {
                errorCount = prevErrorCount;
            }
        }
        MonitorUtil.safeAdd(errorCount, 1);
    }

    /**
     * 获得错误码对应的 IM 客户端失败总次数。
     *
     * @param errorCode 错误代码
     * @return 错误码对应的 IM 客户端失败总次数
     */
    public long getErrorCount(int errorCode) {
        AtomicLong errorCount = errorCountMap.get(errorCode);
        if (errorCount != null) {
            return errorCount.get();
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "IMClientMonitor{" +
                "createdCount=" + createdCount +
                ", closedCount=" + closedCount +
                ", errorCountMap=" + errorCountMap +
                '}';
    }

    /**
     * 获得 IM 客户端信息监控器，该方法不会返回 {@code null}。
     *
     * @return IM 客户端信息监控器
     */
    public static IMClientMonitor getInstance() {
        return INSTANCE;
    }
}
