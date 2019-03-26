package com.heimuheimu.raven.clients;

/**
 * IM 客户端管理器列表事件监听器，可监听列表中 {@code IMClientManager} 的创建、关闭、恢复等事件。
 *
 * <p>
 *     <strong>说明：</strong>监听器的实现类必须是线程安全的。
 * </p>
 *
 * @author heimuheimu
 */
public interface IMClientManagerListListener {

    /**
     * 当 IMClientManager 在 IMClientManagerList 初始化过程被创建成功时，将会触发此事件。
     *
     * @param name IM 客户端管理器名称
     */
    void onCreated(String name);

    /**
     * 当 IMClientManager 恢复时，将会触发此事件。
     *
     * @param name IM 客户端管理器名称
     */
    void onRecovered(String name);

    /**
     * 当 IMClientManager 关闭时，将会触发此事件。
     *
     * @param name IM 客户端管理器名称
     */
    void onClosed(String name);
}
