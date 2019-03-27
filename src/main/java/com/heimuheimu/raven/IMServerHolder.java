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

package com.heimuheimu.raven;

/**
 * 保存最后一个启动的 IMServer 实例，并通过静态方法进行获取，可解决 Spring 注入遇到的循环依赖问题（例如 IMClientListener 的实现需要依赖 IMServer）。
 *
 * <p><strong>注意：</strong> 通常一个应用应只启动一个 IMServer 实例。</p>
 * <p><strong>说明：</strong>IMServerHolder 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public final class IMServerHolder {

    private static volatile IMServer INSTANCE = null;

    static void add(IMServer server) {
        INSTANCE = server;
    }

    /**
     * 获得最后一个启动的 IMServer 实例，如果没有则返回 {@code null}。
     *
     * <p><strong>注意：</strong> 返回的 IMServer 实例可能已被关闭。</p>
     *
     * @return IMServer 实例，可能为 {@code null}
     */
    public static IMServer get() {
        return INSTANCE;
    }
}
