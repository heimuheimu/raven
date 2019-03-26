package com.heimuheimu.raven.exception;

/**
 * Raven 框架使用的根异常，通常用于将捕获异常转换为非捕获异常。
 *
 * @author heimuheimu
 */
public class RavenException extends RuntimeException {

    private static final long serialVersionUID = 4188145144237342405L;

    public RavenException(String message) {
        super(message);
    }

    public RavenException(String message, Throwable cause) {
        super(message, cause);
    }
}
