package com.heimuheimu.raven.clients;

/**
 * IM 客户端唯一 ID 生成器。
 *
 * <p>
 *     <strong>说明：</strong>IMClientIDGenerator 的实现类必须是线程安全的。
 * </p>
 *
 * @author heimuheimu
 */
public interface IMClientIDGenerator {

    /**
     * 生成一个 IM 客户端唯一 ID。
     *
     * @return 唯一 ID
     */
    String generate();
}
