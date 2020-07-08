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

import com.heimuheimu.raven.constant.BeanStatusEnum;
import com.heimuheimu.raven.exception.RavenException;
import com.heimuheimu.raven.facility.UnusableServiceNotifier;
import com.heimuheimu.raven.monitor.ByteMessageMonitor;
import com.heimuheimu.raven.monitor.IMClientMonitor;
import com.heimuheimu.raven.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 封装与 IM 客户端建立的连接。
 *
 * <p><strong>说明：</strong>IMClient 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class IMClient implements Closeable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(IMClient.class);

    private static final Logger RAVEN_IM_CLIENT_LOG = LoggerFactory.getLogger("RAVEN_IM_CLIENT_LOG");

    /**
     * 发送字节消息信息监控器
     */
    private static final ByteMessageMonitor BYTE_MESSAGE_MONITOR = ByteMessageMonitor.getInstance();

    /**
     * 当前 IM 客户端唯一 ID
     */
    private final String id;

    /**
     * IMServer 与当前 IM 客户端建立的 SocketChannel
     */
    private final SocketChannel socketChannel;

    /**
     * SocketChannel 选择器
     */
    private final Selector selector;

    /**
     * IM 客户端单次写入操作允许写入的最大字节数，如果小于等于 0，则没有限制
     */
    private final int maxWriteByteLength;

    /**
     * IM 客户端事件监听器，允许为 {@code null}
     */
    private final IMClientListener clientListener;

    /**
     * IM 客户端不可用通知器，允许为 {@code null}
     */
    private final UnusableServiceNotifier<IMClient> unusableServiceNotifier;

    /**
     * 当前 IMClient 实例所处状态
     */
    private volatile BeanStatusEnum state = BeanStatusEnum.NORMAL;

    /**
     * IM 客户端最后活跃时间，即最后收到 IM 客户端发送的数据时间
     */
    private volatile long lastActiveTime = System.currentTimeMillis();

    /**
     * IM 客户端是否处于只读模式，访问此变量需先获得锁 {@link #writeLock}
     */
    private boolean readonly = true;

    /**
     * 当前正在写入的数据缓存，访问此变量需先获得锁 {@link #writeLock}
     */
    private ByteBuffer writeBuffer = null;

    /**
     * 当前正在写入的数据对应的字节消息 ID 数组，访问此变量需先获得锁 {@link #writeLock}
     */
    private String[] messageIdArray = null;

    /**
     * 当前正在写入的数据对应的字节消息创建时间数组，该数组大小、顺序与 {@link #messageIdArray} 一致，访问此变量需先获得锁 {@link #writeLock}
     */
    private long[] messageCreatedTimeArray = null;

    /**
     * 存放待写入的字节消息列表，访问此变量需先获得锁 {@link #writeLock}
     */
    private List<ByteMessage> messageList = new ArrayList<>();

    /**
     * IM 客户端写数据使用的私有锁
     */
    private final Object writeLock = new Object();

    /**
     * 构造一个 IMClient 实例。
     *
     * @param id IM 客户端唯一 ID，不允许为 {@code null}
     * @param socketChannel 与 IM 客户端建立的 SocketChannel，不允许为 {@code null}
     * @param selector IM 客户端使用的 SocketChannel 选择器，允许为 {@code null}
     * @param maxWriteByteLength IM 客户端单次写入操作允许写入的最大字节数，如果小于等于 0，则没有限制
     * @param clientListener IM 客户端事件监听器，允许为 {@code null}
     * @param unusableServiceNotifier IM 客户端不可用通知器，允许为 {@code null}
     */
    public IMClient(String id, SocketChannel socketChannel, Selector selector,
                    int maxWriteByteLength, IMClientListener clientListener,
                    UnusableServiceNotifier<IMClient> unusableServiceNotifier) {
        this.id = id;
        this.socketChannel = socketChannel;
        this.selector = selector;
        this.maxWriteByteLength = maxWriteByteLength;
        this.clientListener = clientListener;
        this.unusableServiceNotifier = unusableServiceNotifier;
    }

    /**
     * 获得当前 IM 客户端唯一 ID。
     *
     * @return 当前 IM 客户端唯一 ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获得 IMServer 与当前 IM 客户端建立的 SocketChannel。
     *
     * @return IMServer 与当前 IM 客户端建立的 SocketChannel
     */
    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    /**
     * 获得 IM 客户端最后活跃时间，即最后收到 IM 客户端发送的数据时间。
     *
     * @return IM 客户端最后活跃时间
     */
    public long getLastActiveTime() {
        return lastActiveTime;
    }

    /**
     * 判断当前 IM 客户端是否可用。
     *
     * @return 当前 IM 客户端是否可用
     */
    public boolean isActive() {
        return state == BeanStatusEnum.NORMAL;
    }

    /**
     * 接收 IM 客户端发送的数据。
     *
     * @param buffer IM 客户端发送的数据，不允许为 {@code null}
     */
    public void receive(ByteBuffer buffer) {
        lastActiveTime = System.currentTimeMillis();
        if (clientListener != null) {
            clientListener.onReceived(this, buffer);
        }
    }

    /**
     * 异步向 IM 客户端发送一条字节消息，发送成功后，将通过 {@link IMClientListener#onSent(IMClient, String[])} 方法进行通知。
     *
     * @param message 字节消息，不允许为 {@code null}
     * @throws IllegalArgumentException 如果 {@code message} 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果当前 IM 客户端未初始化或已关闭，将会抛出此异常
     * @throws RavenException 如果在发送过程中发生其它未知错误，将会抛出此异常
     */
    public void asyncSend(ByteMessage message) throws IllegalArgumentException, IllegalStateException, RavenException {
        if (message == null) {
            BYTE_MESSAGE_MONITOR.onError();
            String errorMessage = "IMClient fails to send message: `null ByteMessage`. `client`:`" + toString() + "`.";
            LOGGER.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        if (state != BeanStatusEnum.NORMAL) {
            BYTE_MESSAGE_MONITOR.onError();
            String errorMessage = "IMClient fails to send message: `illegal state`. `messageId`:`" + message.getId() +
                    "`. `client`:`" + toString() + "`.";
            LOGGER.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        synchronized (writeLock) {
            messageList.add(message);
            try {
                setReadonly(false);
            } catch (Exception e) {
                BYTE_MESSAGE_MONITOR.onError();
                LinkedHashMap<String, Object> params = new LinkedHashMap<>();
                try {
                    params.put("remote", socketChannel.getRemoteAddress());
                    params.put("local", socketChannel.getLocalAddress());
                } catch (Exception ignored) {}
                params.put("id", getId());
                RAVEN_IM_CLIENT_LOG.error("IMClient fails to set readonly." + LogBuildUtil.build(params), e);
                close();

                String errorMessage = "IMClient fails to send message: `set readonly failed`. `messageId`:`" + message.getId() +
                        "`. `client`:`" + toString() + "`.";
                LOGGER.error(errorMessage, e);
                throw new RavenException(errorMessage, e);
            }
        }

        BYTE_MESSAGE_MONITOR.onCreated(message.getContent().length);
    }

    /**
     * 获得向 IM 客户端发送的数据缓存，有可能返回 {@code null}。
     *
     * @return 向 IM 客户端发送的数据缓存
     */
    public ByteBuffer getBufferForWrite() {
        synchronized (writeLock) {
            if (writeBuffer == null) {
                buildByteBufferForWrite();
                if (maxWriteByteLength > 0) {
                    if (writeBuffer != null && writeBuffer.remaining() > maxWriteByteLength) {
                        writeBuffer.limit(maxWriteByteLength);
                    }
                }
            }
            return writeBuffer;
        }
    }

    /**
     * IMClientManager 在完成一次写入操作后，将会调用此方法通知当前 IM 客户端。
     */
    public void afterWrite() {
        lastActiveTime = System.currentTimeMillis();
        synchronized (writeLock) {
            if (writeBuffer == null) {
                if (messageList.isEmpty()) { // 没有新的字节消息，将 IMClient 切换为只读模式
                    setReadonly(true);
                }
            } else if (writeBuffer.remaining() == 0) {
                if (writeBuffer.limit() == writeBuffer.capacity()) { // 数据写入完成
                    for (long messageCreatedTime : messageCreatedTimeArray) {
                        BYTE_MESSAGE_MONITOR.onSent(System.currentTimeMillis() - messageCreatedTime);
                    }
                    messageCreatedTimeArray = null;

                    if (clientListener != null) {
                        clientListener.onSent(this, messageIdArray);
                    }
                    messageIdArray = null;
                    writeBuffer = null;
                    if (messageList.isEmpty()) { // 并且没有新的字节消息，将 IMClient 切换为只读模式
                        setReadonly(true);
                    }
                } else {
                    int newLimit = Math.min(writeBuffer.limit() + maxWriteByteLength, writeBuffer.capacity());
                    writeBuffer.limit(newLimit);
                }
            }
        }
    }

    private void buildByteBufferForWrite() {
        synchronized (writeLock) {
            if (!messageList.isEmpty()) {
                if (messageList.size() == 1) {
                    ByteMessage message = messageList.get(0);
                    messageIdArray = new String[] {message.getId()};
                    messageCreatedTimeArray = new long[] {message.getCreatedTime()};
                    writeBuffer = ByteBuffer.wrap(message.getContent());
                } else {
                    messageIdArray = new String[messageList.size()];
                    messageCreatedTimeArray = new long[messageList.size()];

                    int byteLength = 0;
                    for (int i = 0; i < messageList.size(); i++) {
                        ByteMessage message = messageList.get(i);
                        messageIdArray[i] = message.getId();
                        messageCreatedTimeArray[i] = message.getCreatedTime();
                        byteLength += message.getContent().length;
                    }

                    byte[] mergedContent = new byte[byteLength];
                    int offset = 0;
                    for (ByteMessage message : messageList) {
                        byte[] content = message.getContent();
                        System.arraycopy(content, 0, mergedContent, offset, content.length);
                        offset += content.length;
                    }

                    writeBuffer = ByteBuffer.wrap(mergedContent);
                }
                messageList = new ArrayList<>();
            }
        }
    }

    private void setReadonly(boolean readonly) {
        synchronized (writeLock) {
            if (isActive() && this.readonly != readonly) {
                if (!readonly) {
                    interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                } else {
                    interestOps(SelectionKey.OP_READ);
                }
                this.readonly = readonly;
            }
        }
    }

    private void interestOps(int ops) {
        if (selector == null || !selector.isOpen()) {
            String errorMessage = "IMClient fails to interest operations: `invalid selector`. `client`:`"
                    + toString() + "`.";
            LOGGER.error(errorMessage);
            throw new RavenException(errorMessage);
        }

        SelectionKey selectionKey = socketChannel.keyFor(selector);
        if (selectionKey == null) {
            String errorMessage = "IMClient fails to interest operations: `null selectionKey`. `client`:`"
                    + toString() + "`.";
            LOGGER.error(errorMessage);
            throw new RavenException(errorMessage);
        }
        try {
            selectionKey.interestOps(ops);
            selector.wakeup();
        } catch (Exception e) {
            String errorMessage = "IMClient fails to interest operations: `unexpected error`. `client`:`"
                    + toString() + "`.";
            LOGGER.error(errorMessage, e);
            throw new RavenException(errorMessage, e);
        }
    }

    @Override
    public synchronized void close() {
        if (state != BeanStatusEnum.CLOSED) {
            long startTime = System.currentTimeMillis();
            state = BeanStatusEnum.CLOSED;
            LinkedHashMap<String, Object> params = new LinkedHashMap<>();
            params.put("id", id);

            try {
                if (socketChannel.isOpen()) {
                    params.put("remote", socketChannel.getRemoteAddress());
                    params.put("local", socketChannel.getLocalAddress());
                }
            } catch (Exception ignored) {}

            try {
                if (selector != null && selector.isOpen()) {
                    SelectionKey selectionKey = socketChannel.keyFor(selector);
                    if (selectionKey != null) {
                        selectionKey.cancel();
                        selector.wakeup();
                    }
                }

                socketChannel.configureBlocking(true);
                socketChannel.setOption(StandardSocketOptions.SO_LINGER, 0); // avoid TIME_WAIT being a problem
                socketChannel.close();
                params.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                RAVEN_IM_CLIENT_LOG.info("Closed IMClient.{}", LogBuildUtil.build(params));
            } catch (Exception e) {
                params.put("cost", (System.currentTimeMillis() - startTime) + "ms");
                RAVEN_IM_CLIENT_LOG.error("IMClient fails to close: `unexpected error`." + LogBuildUtil.build(params), e);
                IMClientMonitor.getInstance().onError(IMClientMonitor.ERROR_CODE_FAILS_TO_CLOSE);
            } finally {
                if (unusableServiceNotifier != null) {
                    unusableServiceNotifier.onClosed(this);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "IMClient{" +
                "id='" + id + '\'' +
                ", socketChannel=" + socketChannel +
                ", selector=" + selector +
                ", maxWriteByteLength=" + maxWriteByteLength +
                ", clientListener=" + clientListener +
                ", unusableServiceNotifier=" + unusableServiceNotifier +
                ", state=" + state +
                ", lastActiveTime=" + lastActiveTime +
                ", readonly=" + readonly +
                '}';
    }
}
