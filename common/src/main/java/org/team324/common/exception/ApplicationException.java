package org.team324.common.exception;

/**
 * 应用异常类
 * 继承运行时异常类
 * @author crystalZ
 * @date 2024/5/31
 */
public class ApplicationException extends RuntimeException {

    private int code;

    private String error;


    public ApplicationException(int code, String message) {
        super(message);
        this.code = code;
        this.error = message;
    }

    /**
     * 获取应用异常枚举类
     * @param exceptionEnum
     */
    public ApplicationException(ApplicationExceptionEnum exceptionEnum) {
        super(exceptionEnum.getError());
        this.code   = exceptionEnum.getCode();
        this.error  = exceptionEnum.getError();
    }

    public int getCode() {
        return code;
    }

    public String getError() {
        return error;
    }


    /**
     *  avoid the expensive and useless stack trace for api exceptions
     *  @see Throwable#fillInStackTrace()
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

}
