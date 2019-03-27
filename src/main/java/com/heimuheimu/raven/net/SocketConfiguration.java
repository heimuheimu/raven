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

package com.heimuheimu.raven.net;


import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.SocketChannel;

/**
 * {@link SocketChannel} 使用的 Socket 配置信息。
 *
 * <p><strong>说明：</strong>{@code SocketConfiguration} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class SocketConfiguration {
	
	/**
	 * {@link SocketChannel} 使用的默认 Socket 配置信息：
	 * <ul>
	 * <li>{@link StandardSocketOptions#SO_KEEPALIVE} 值为 {@code false}</li>
	 * <li>{@link StandardSocketOptions#TCP_NODELAY} 值为 {@code false}</li>
	 * <li>{@link StandardSocketOptions#SO_SNDBUF} 值为 32 KB</li>
	 * <li>{@link StandardSocketOptions#SO_SNDBUF} 值为 32 KB</li>
	 * </ul>
	 */
	public static final SocketConfiguration DEFAULT;
	
	static {
		SocketConfiguration config = new SocketConfiguration();
		config.setKeepAlive(false);
		config.setTcpNoDelay(false);
		config.setSendBufferSize(32 * 1024);
		config.setReceiveBufferSize(32 * 1024);
		DEFAULT = config;
	}
	
	/**
	 * @see StandardSocketOptions#SO_KEEPALIVE
	 */
	private volatile Boolean keepAlive = null;
	
	/**
	 * @see StandardSocketOptions#TCP_NODELAY
	 */
	private volatile Boolean tcpNoDelay = null;
	
	/**
	 * @see StandardSocketOptions#SO_SNDBUF
	 */
	private volatile Integer sendBufferSize = null;
	
	/**
	 * @see StandardSocketOptions#SO_RCVBUF
	 */
	private volatile Integer receiveBufferSize = null;

	/**
	 * 获得 {@link StandardSocketOptions#SO_KEEPALIVE} 配置值，如果为 {@code null}，则使用系统默认配置。
	 * 
	 * @return keepAlive 配置值，可能返回 {@code null}
	 */
	public Boolean getKeepAlive() {
		return keepAlive;
	}

	/**
	 * 设置 {@link StandardSocketOptions#SO_KEEPALIVE} 配置值， 如果为 {@code null}，则使用系统默认配置。
	 * 
	 * @param keepAlive keepAlive 配置值，允许为 {@code null}
	 * @throws UnsupportedOperationException 如果对 {@link #DEFAULT} 实例操作此方法，将会抛出此异常
	 */
	public void setKeepAlive(Boolean keepAlive) throws UnsupportedOperationException {
		if (this == DEFAULT) {
			throw new UnsupportedOperationException("Set `keepAlive` failed: `SocketConfiguration#DEFAULT could not be changed`.");
		}
		this.keepAlive = keepAlive;
	}

	/**
	 * 获得 {@link StandardSocketOptions#TCP_NODELAY} 配置值，如果为 {@code null}，则使用系统默认配置。
	 * 
	 * @return tcpNoDelay 配置值，可能返回 {@code null}
	 */
	public Boolean getTcpNoDelay() {
		return tcpNoDelay;
	}

	/**
	 * 设置 {@link StandardSocketOptions#TCP_NODELAY} 配置值，如果为 {@code null}，则使用系统默认配置。
	 * 
	 * @param tcpNoDelay tcpNoDelay 配置值，允许为 {@code null}
	 * @throws UnsupportedOperationException 如果对 {@link #DEFAULT} 实例操作此方法，将会抛出此异常
	 */
	public void setTcpNoDelay(Boolean tcpNoDelay) throws UnsupportedOperationException {
		if (this == DEFAULT) {
			throw new UnsupportedOperationException("Set `tcpNoDelay` failed: `SocketConfiguration#DEFAULT could not be changed`.");
		}
		this.tcpNoDelay = tcpNoDelay;
	}

	/**
	 * 获得 {@link StandardSocketOptions#SO_SNDBUF} 配置值，如果为 {@code null}，则使用系统默认配置。
	 * 
	 * @return sendBufferSize 配置值，可能返回 {@code null}
	 */
	public Integer getSendBufferSize() {
		return sendBufferSize;
	}

	/**
	 * 设置 {@link StandardSocketOptions#SO_SNDBUF} 配置值， 如果为 {@code null}，则使用系统默认配置。
	 * 
	 * @param sendBufferSize sendBufferSize 配置值，允许为 {@code null}
	 * @throws UnsupportedOperationException 如果对 {@link #DEFAULT} 实例操作此方法，将会抛出此异常
	 */
	public void setSendBufferSize(Integer sendBufferSize) throws UnsupportedOperationException {
		if (this == DEFAULT) {
			throw new UnsupportedOperationException("Set `sendBufferSize` failed: `SocketConfiguration#DEFAULT could not be changed`.");
		}
		this.sendBufferSize = sendBufferSize;
	}

	/**
	 * 获得 {@link StandardSocketOptions#SO_RCVBUF} 配置值，如果为 {@code null} ，则使用系统默认配置。
	 * 
	 * @return receiveBufferSize 配置值，可能返回 {@code null}
	 */
	public Integer getReceiveBufferSize() {
		return receiveBufferSize;
	}

	/**
	 * 设置 {@link StandardSocketOptions#SO_RCVBUF} 配置值，如果为 {@code null}，则使用系统默认配置。
	 * 
	 * @param receiveBufferSize receiveBufferSize 配置值，允许为 {@code null}
	 * @throws UnsupportedOperationException 如果对 {@link #DEFAULT} 实例操作此方法，将会抛出此异常
	 */
	public void setReceiveBufferSize(Integer receiveBufferSize) throws UnsupportedOperationException {
		if (this == DEFAULT) {
			throw new UnsupportedOperationException("Set `receiveBufferSize` failed: `SocketConfiguration#DEFAULT could not be changed`.");
		}
		this.receiveBufferSize = receiveBufferSize;
	}

	/**
	 * 将当前 Socket 配置信息应用到指定的 SocketChannel 上。
	 *
	 * @param socketChannel SocketChannel 实例
	 * @throws IOException Socket 配置信息设置过程中出现 IO 错误，将会抛出此异常
	 */
	public void apply(SocketChannel socketChannel) throws IOException {
		if (keepAlive != null) {
			socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, keepAlive);
		}
		if (tcpNoDelay != null) {
			socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, tcpNoDelay);
		}
		if (sendBufferSize != null) {
			socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, sendBufferSize);
		}
		if (receiveBufferSize != null) {
			socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, receiveBufferSize);
		}
	}

	@Override
	public String toString() {
		return "SocketConfiguration{" +
				"keepAlive=" + keepAlive +
				", tcpNoDelay=" + tcpNoDelay +
				", sendBufferSize=" + sendBufferSize +
				", receiveBufferSize=" + receiveBufferSize +
				'}';
	}
}
