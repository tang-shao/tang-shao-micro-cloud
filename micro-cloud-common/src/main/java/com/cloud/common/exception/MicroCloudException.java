package com.cloud.common.exception;

/**
 * 自定义异常信息
 */
public class MicroCloudException extends RuntimeException {

    public MicroCloudException(String message){
        super(message);
    }


    public MicroCloudException(Throwable cause){
        super(cause);
    }

    public MicroCloudException(String message,Throwable cause) {
        super(message,cause);
    }
}
