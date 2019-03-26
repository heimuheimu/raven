package com.heimuheimu.raven.clients.support;

import com.heimuheimu.raven.clients.IMClientIDGenerator;

import java.util.UUID;

/**
 * IM 客户端唯一 ID 生成器，使用 UUID 作为客户端唯一 ID，保证集群环境下的客户端 ID 唯一。
 *
 * <p><strong>说明：</strong>IMClientUUIDGenerator 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class IMClientUUIDGenerator implements IMClientIDGenerator {

    @Override
    public String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
