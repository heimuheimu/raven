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
