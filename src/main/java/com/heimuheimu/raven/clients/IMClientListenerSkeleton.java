package com.heimuheimu.raven.clients;

import java.nio.ByteBuffer;

/**
 * {@link IMClientListener} 骨架类，可防止 IMClientListener 在后续版本增加方法时，带来的编译错误。
 *
 * <p>
 *     <strong>说明：</strong>监听器的实现类必须是线程安全的。
 * </p>
 *
 * @author heimuheimu
 */
public abstract class IMClientListenerSkeleton implements IMClientListener {

    @Override
    public void onReceived(IMClient client, ByteBuffer buffer) {
        // do nothing
    }

    @Override
    public void onSent(IMClient client, String[] ids) {
        // do nothing
    }
}
