package com.heimuheimu.raven.clients;

import java.util.Arrays;

/**
 * 以字节形式存储的消息，用于向 IM 客户端发送。
 *
 * <p><strong>说明：</strong>ByteMessage 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class ByteMessage {

    /**
     * 字节消息 ID
     */
    private final String id;

    /**
     * 字节消息内容
     */
    private final byte[] content;

    /**
     * 字节消息创建时间
     */
    private final long createdTime;

    /**
     * 构造一个字节消息。
     *
     * @param id 字节消息 ID
     * @param content 字节消息内容
     */
    public ByteMessage(String id, byte[] content) {
        this.id = id;
        this.content = content;
        this.createdTime = System.currentTimeMillis();
    }

    /**
     * 获得字节消息 ID。
     *
     * @return 字节消息 ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获得字节消息内容。
     *
     * @return 字节消息内容
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * 获得字节消息创建时间。
     *
     * @return 字节消息创建时间
     */
    public long getCreatedTime() {
        return createdTime;
    }

    @Override
    public String toString() {
        return "ByteMessage{" +
                "id='" + id + '\'' +
                ", content=" + Arrays.toString(content) +
                ", createdTime=" + createdTime +
                '}';
    }
}
