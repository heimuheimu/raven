package com.heimuheimu.raven.clients;

import java.nio.channels.SocketChannel;

/**
 * {@link IMClientInterceptor} 骨架类，可防止 IMClientInterceptor 在后续版本增加方法时，带来的编译错误。
 *
 * <p>
 *     <strong>说明：</strong>拦截器的实现类必须是线程安全的。
 * </p>
 *
 * @author heimuheimu
 */
public abstract class IMClientInterceptorSkeleton implements IMClientInterceptor {

    @Override
    public boolean canConnect(SocketChannel socketChannel) {
        return true;
    }

    @Override
    public void onCreated(IMClient imClient) {
        // do nothing
    }

    @Override
    public void onClosed(IMClient imClient) {
        // do nothing
    }
}
