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

import java.nio.ByteBuffer;

/**
 * IM 客户端事件监听器。
 *
 * <p>
 *     <strong>说明：</strong>监听器的实现类必须是线程安全的。
 * </p>
 *
 * @author heimuheimu
 */
public interface IMClientListener {

    /**
     * 当接收到 IM 客户端发送的数据时，将触发此事件。
     *
     * <p><strong>注意：</strong>该方法将在 IO 线程中执行，请勿执行耗时操作，如操作时间不可确定，建议内部采用异步方式执行。</p>
     * <p><strong>说明：</strong>如果该方法在执行过程中抛出异常，该 IM 客户端会被关闭，不允许其继续通信。</p>
     *
     * @param client 接收到数据的 IM 客户端，不允许为 {@code null}
     * @param buffer 接收到的数据，不允许为 {@code null}
     */
    void onReceived(IMClient client, ByteBuffer buffer);

    /**
     * 当向 IM 客户端发送字节消息成功时，将触发此事件。
     *
     * <p><strong>注意：</strong>该方法将在 IO 线程中执行，请勿执行耗时操作，如操作时间不可确定，建议内部采用异步方式执行。</p>
     * <p><strong>说明：</strong>如果该方法在执行过程中抛出异常，该 IM 客户端会被关闭，不允许其继续通信。</p>
     *
     * @param client 目标 IM 客户端，不允许为 {@code null}
     * @param ids 发送成功的字节消息 ID 数组，不会为 {@code null} 或空数组
     */
    void onSent(IMClient client, String[] ids);
}
