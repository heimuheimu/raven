package com.heimuheimu.raven.exception;

/**
 * 当 IMClientManager 在达到可管理的最大 IM 客户端数量，无法注册新的 IMClient 时，将会抛出此异常。
 *
 * @author heimuheimu
 */
public class RejectedRegisterException extends RavenException {

    private static final long serialVersionUID = 4873400511679736875L;

    public RejectedRegisterException(String message) {
        super(message);
    }

    public RejectedRegisterException(String message, Throwable cause) {
        super(message, cause);
    }
}
