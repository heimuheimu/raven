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

import java.nio.channels.SocketChannel;

/**
 * IM 客户端拦截器。
 *
 * <p>
 *     <strong>说明：</strong>拦截器的实现类必须是线程安全的。应优先考虑继承 {@link IMClientInterceptorSkeleton} 骨架类进行实现，
 *     防止 IMClientInterceptor 在后续版本增加方法时，带来的编译错误。
 * </p>
 *
 * @author heimuheimu
 */
public interface IMClientInterceptor {

    /**
     * 当 IMServer 与请求建立连接的 IM 客户端建立 Socket 连接后，将会调用此方法，如果该方法返回 {@code true}，IMServer 会保持该 IM 客户端
     * 连接并与其继续通信，如果返回 {@code false}，IMServer 会关闭该 IM 客户端，不允许其继续通信。
     *
     * <p><strong>说明：</strong>如果该方法在执行过程中抛出异常，IMServer 会关闭该 IM 客户端，不允许其继续通信。</p>
     *
     * @param socketChannel IMServer 与 IM 客户端建立 Socket 连接，不会为 {@code null}
     * @return 是否允许该 IM 客户端继续与 IMServer 进行通信
     */
    boolean canConnect(SocketChannel socketChannel);

    /**
     * 当 IM 客户端创建成功后，将会调用此方法。
     *
     * <p><strong>说明：</strong>如果该方法在执行过程中抛出异常，IMServer 会关闭该 IM 客户端，不允许其继续通信。</p>
     *
     * @param imClient IM 客户端，不会为 {@code null}
     */
    void onCreated(IMClient imClient);

    /**
     * 当 IM 客户端关闭后，将会调用此方法。
     *
     * @param imClient IM 客户端，不会为 {@code null}
     */
    void onClosed(IMClient imClient);
}
