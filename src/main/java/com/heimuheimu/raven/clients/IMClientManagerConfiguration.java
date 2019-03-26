package com.heimuheimu.raven.clients;

import com.heimuheimu.raven.facility.UnusableServiceNotifier;

/**
 * {@link IMClientManager} 使用的配置信息。
 *
 * <p><strong>说明：</strong>IMClientManagerConfiguration 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class IMClientManagerConfiguration {

    /**
     * 可管理的最大 IM 客户端数量，如果小于等于 0 ，则没有数量限制，默认为 -1
     */
    private volatile int capacity = -1;

    /**
     * 接收 IM 客户端发送数据使用的字节缓存大小，默认为 32 KB，如果小于等于 0，则使用具体实现指定的默认值
     */
    private volatile int receiveBufferSize = 32 * 1024;

    /**
     * IMClientManager 不可用通知器，默认为 {@code null}
     */
    private volatile UnusableServiceNotifier<IMClientManager> unusableServiceNotifier = null;

    /**
     * 获得可管理的最大 IM 客户端数量，如果小于等于 0 ，则没有数量限制，默认为 -1。
     *
     * @return 可管理的最大 IM 客户端数量，如果小于等于 0 ，则没有数量限制
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * 设置可管理的最大 IM 客户端数量，如果小于等于 0 ，则没有数量限制。
     *
     * @param capacity 可管理的最大 IM 客户端数量，如果小于等于 0 ，则没有数量限制
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * 获得接收 IM 客户端发送数据使用的字节缓存大小，默认为 32 KB，如果小于等于 0，则使用具体实现指定的默认值。
     *
     * @return 接收 IM 客户端发送数据使用的字节缓存大小
     */
    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    /**
     * 设置接收 IM 客户端发送数据使用的字节缓存大小，如果小于等于 0，则使用具体实现指定的默认值。
     *
     * @param receiveBufferSize 接收 IM 客户端发送数据使用的字节缓存大小
     */
    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    /**
     * 获得 IMClientManager 不可用通知器，默认为 {@code null}。
     *
     * @return IMClientManager 不可用通知器，可能为 {@code null}
     */
    public UnusableServiceNotifier<IMClientManager> getUnusableServiceNotifier() {
        return unusableServiceNotifier;
    }

    /**
     * 设置 IMClientManager 不可用通知器，允许为 {@code null}。
     *
     * @param unusableServiceNotifier IMClientManager 不可用通知器，允许为 {@code null}
     */
    public void setUnusableServiceNotifier(UnusableServiceNotifier<IMClientManager> unusableServiceNotifier) {
        this.unusableServiceNotifier = unusableServiceNotifier;
    }

    @Override
    public String toString() {
        return "IMClientManagerConfiguration{" +
                "capacity=" + capacity +
                ", receiveBufferSize=" + receiveBufferSize +
                ", unusableServiceNotifier=" + unusableServiceNotifier +
                '}';
    }
}
