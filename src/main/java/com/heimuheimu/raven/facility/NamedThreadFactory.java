package com.heimuheimu.raven.facility;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 可自定义线程名称的线程工厂。
 *
 * <p><strong>说明：</strong>NamedThreadFactory 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class NamedThreadFactory implements ThreadFactory {

    /**
     * 线程名称前缀
     */
    private final String prefix;

    /**
     * 下一个线程名称中使用的数字
     */
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    /**
     * 构造一个 NamedThreadFactory 实例。
     *
     * @param prefix 线程名称前缀
     */
    public NamedThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName(prefix + threadNumber.getAndIncrement());
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}
