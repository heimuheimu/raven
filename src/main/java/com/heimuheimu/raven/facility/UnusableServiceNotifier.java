package com.heimuheimu.raven.facility;

/**
 * 服务不可用通知接口。
 *
 * @param <T> 服务类型
 */
@FunctionalInterface
public interface UnusableServiceNotifier<T> {

    /**
     * 当服务关闭时，通过此接口进行通知。
     *
     * @param target 提供服务的目标类
     */
    void onClosed(T target);
}
