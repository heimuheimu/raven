package com.heimuheimu.raven;

import com.heimuheimu.raven.clients.*;
import com.heimuheimu.raven.net.SocketConfiguration;

import java.nio.channels.SocketChannel;

/**
 * {@link IMServer} 使用的配置信息。
 *
 * <p><strong>说明：</strong>IMServerConfiguration 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class IMServerConfiguration {

    /**
     * IMServer 使用的监听端口，默认为 4182
     */
    private volatile int port = 4182;

    /**
     * 最大等待建立连接数（The maximum number of pending connections），默认为 50，如果小于等 0，则使用具体实现指定的默认值
     */
    private volatile int backlog = 50;

    /**
     * IMServer 与 IM 客户端连接的 {@link SocketChannel} 使用的 Socket 配置信息，默认为 {@code null}，如果为 {@code null}，
     * 将使用 {@link SocketConfiguration#DEFAULT}
     */
    private volatile SocketConfiguration socketConfiguration = null;

    /**
     * IM 客户端唯一 ID 生成器，默认为 {@code null}，如果为 {@code null}，将使用默认的 ID 生成器
     */
    private volatile IMClientIDGenerator clientIDGenerator = null;

    /**
     * IM 客户端单次写入操作允许写入的最大字节数，默认为 64 KB，如果小于等于 0，则没有限制
     */
    private volatile int maxWriteByteLength = 64 * 1024;

    /**
     * IM 客户端事件监听器，默认为 {@code null}
     */
    private volatile IMClientListener clientListener = null;

    /**
     * IM 客户端超时时间，单位：秒，如果小于等于 0，则不会超时，默认为 60 秒
     */
    private volatile int clientTimeout = 60;

    /**
     * IMServer 使用 IM 客户端管理器数量，默认为 20，如果小于等 0，则使用具体实现指定的默认值
     */
    private volatile int poolSize = 20;

    /**
     * IMServer 创建 IM 客户端管理器使用的配置信息，默认为 {@code null}，如果为 {@code null}，将使用默认的配置信息
     */
    private volatile IMClientManagerConfiguration clientManagerConfiguration = null;

    /**
     * IM 客户端管理器列表事件监听器，默认为 {@code null}
     */
    private volatile IMClientManagerListListener clientManagerListListener = null;

    /**
     * IM 客户端拦截器，默认为 {@code null}
     */
    private volatile IMClientInterceptor clientInterceptor = null;

    /**
     * 获得 IMServer 使用的监听端口，默认为 4182。
     *
     * @return IMServer 使用的监听端口
     */
    public int getPort() {
        return port;
    }

    /**
     * 设置 IMServer 使用的监听端口。
     *
     * @param port IMServer 使用的监听端口
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * 获得最大等待建立连接数（The maximum number of pending connections），默认为 50，如果小于等 0，则使用具体实现指定的默认值。
     *
     * @return 最大等待建立连接数
     */
    public int getBacklog() {
        return backlog;
    }

    /**
     * 设置最大等待建立连接数（The maximum number of pending connections），如果小于等 0，则使用具体实现指定的默认值。
     *
     * @param backlog 最大等待建立连接数
     */
    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    /**
     * 获得 IMServer 与 IM 客户端连接的 {@link SocketChannel} 使用的 Socket 配置信息，默认为 {@code null}，如果为 {@code null}，
     * 将使用 {@link SocketConfiguration#DEFAULT}
     *
     * @return IMServer 与 IM 客户端连接的 {@link SocketChannel} 使用的 Socket 配置信息
     */
    public SocketConfiguration getSocketConfiguration() {
        return socketConfiguration;
    }

    /**
     * 设置 IMServer 与 IM 客户端连接的 {@link SocketChannel} 使用的 Socket 配置信息，允许为 {@code null}。
     *
     * @param socketConfiguration IMServer 与 IM 客户端连接的 {@link SocketChannel} 使用的 Socket 配置信息
     */
    public void setSocketConfiguration(SocketConfiguration socketConfiguration) {
        this.socketConfiguration = socketConfiguration;
    }

    /**
     * 获得 IM 客户端唯一 ID 生成器，默认为 {@code null}，如果为 {@code null}，将使用默认的 ID 生成器。
     *
     * @return IM 客户端唯一 ID 生成器
     */
    public IMClientIDGenerator getClientIDGenerator() {
        return clientIDGenerator;
    }

    /**
     * 设置 IM 客户端唯一 ID 生成器，允许为 {@code null}。
     *
     * @param clientIDGenerator IM 客户端唯一 ID 生成器
     */
    public void setClientIDGenerator(IMClientIDGenerator clientIDGenerator) {
        this.clientIDGenerator = clientIDGenerator;
    }

    /**
     * 获得 IM 客户端单次写入操作允许写入的最大字节数，默认为 64 KB。
     *
     * @return IM 客户端单次写入操作允许写入的最大字节数
     */
    public int getMaxWriteByteLength() {
        return maxWriteByteLength;
    }

    /**
     * 设置 IM 客户端单次写入操作允许写入的最大字节数，如果小于等于 0，则没有限制。
     *
     * @param maxWriteByteLength IM 客户端单次写入操作允许写入的最大字节数
     */
    public void setMaxWriteByteLength(int maxWriteByteLength) {
        this.maxWriteByteLength = maxWriteByteLength;
    }

    /**
     * 获得 IM 客户端事件监听器，默认为 {@code null}。
     *
     * @return IM 客户端事件监听器
     */
    public IMClientListener getClientListener() {
        return clientListener;
    }

    /**
     * 设置 IM 客户端事件监听器，允许为 {@code null}。
     *
     * @param clientListener IM 客户端事件监听器
     */
    public void setClientListener(IMClientListener clientListener) {
        this.clientListener = clientListener;
    }

    /**
     * 获得 IM 客户端超时时间，单位：秒，如果小于等于 0，则不会超时，默认为 60 秒。
     *
     * @return IM 客户端超时时间
     */
    public int getClientTimeout() {
        return clientTimeout;
    }

    /**
     * 设置 IM 客户端超时时间，单位：秒，如果小于等于 0，则不会超时。
     *
     * @param clientTimeout IM 客户端超时时间
     */
    public void setClientTimeout(int clientTimeout) {
        this.clientTimeout = clientTimeout;
    }

    /**
     * 获得 IMServer 使用的 IM 客户端连接管理器数量，默认为 20，如果小于等于 0，则使用具体实现指定的默认值。
     *
     * @return IMServer 使用的 IM 客户端连接管理器数量
     */
    public int getPoolSize() {
        return poolSize;
    }

    /**
     * 设置 IMServer 使用的 IM 客户端连接管理器数量，如果小于等 0，则使用具体实现指定的默认值。
     *
     * @param poolSize IMServer 使用的 IM 客户端连接管理器数量
     */
    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    /**
     * 获得 IMServer 创建 IM 客户端管理器使用的配置信息，默认为 {@code null}，如果为 {@code null}，将使用默认的配置信息。
     *
     * @return IMServer 创建 IM 客户端管理器使用的配置信息
     */
    public IMClientManagerConfiguration getClientManagerConfiguration() {
        return clientManagerConfiguration;
    }

    /**
     * 设置 IMServer 创建 IM 客户端管理器使用的配置信息，允许为 {@code null}。
     *
     * @param clientManagerConfiguration IMServer 创建 IM 客户端管理器使用的配置信息
     */
    public void setClientManagerConfiguration(IMClientManagerConfiguration clientManagerConfiguration) {
        this.clientManagerConfiguration = clientManagerConfiguration;
    }

    /**
     * 获得 IM 客户端管理器列表事件监听器，默认为 {@code null}。
     *
     * @return IM 客户端管理器列表事件监听器
     */
    public IMClientManagerListListener getClientManagerListListener() {
        return clientManagerListListener;
    }

    /**
     * 设置 IM 客户端管理器列表事件监听器，允许为 {@code null}。
     *
     * @param clientManagerListListener IM 客户端管理器列表事件监听器
     */
    public void setClientManagerListListener(IMClientManagerListListener clientManagerListListener) {
        this.clientManagerListListener = clientManagerListListener;
    }

    /**
     * 获得 IM 客户端拦截器，默认为 {@code null}。
     *
     * @return IM 客户端拦截器
     */
    public IMClientInterceptor getClientInterceptor() {
        return clientInterceptor;
    }

    /**
     * 设置 IM 客户端拦截器，允许为 {@code null}。
     *
     * @param clientInterceptor IM 客户端拦截器
     */
    public void setClientInterceptor(IMClientInterceptor clientInterceptor) {
        this.clientInterceptor = clientInterceptor;
    }

    @Override
    public String toString() {
        return "IMServerConfiguration{" +
                "port=" + port +
                ", backlog=" + backlog +
                ", socketConfiguration=" + socketConfiguration +
                ", clientIDGenerator=" + clientIDGenerator +
                ", maxWriteByteLength=" + maxWriteByteLength +
                ", clientListener=" + clientListener +
                ", clientTimeout=" + clientTimeout +
                ", poolSize=" + poolSize +
                ", clientManagerConfiguration=" + clientManagerConfiguration +
                ", clientManagerListListener=" + clientManagerListListener +
                ", clientInterceptor=" + clientInterceptor +
                '}';
    }
}
