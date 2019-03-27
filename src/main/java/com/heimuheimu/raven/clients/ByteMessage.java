/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 heimuheimu
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
