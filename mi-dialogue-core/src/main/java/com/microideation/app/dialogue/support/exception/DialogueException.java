package com.microideation.app.dialogue.support.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * Created by sandheepgr on 20/6/16.
 */
public class DialogueException extends RuntimeException implements Serializable {

    private ErrorCode errorCode;

    private String message;



    public DialogueException(String message) {

        super(message);

        this.message = message;

    }


    public DialogueException(ErrorCode errorCode) {

        super();

        this.errorCode=errorCode;

    }


    public DialogueException(ErrorCode errorCode,String message) {

        super();

        this.errorCode = errorCode;

        this.message = message;
    }


    public DialogueException(RuntimeException e) {

        super(e.getMessage());

    }

    @JsonIgnore
    @Override
    public Throwable getCause(){
        return super.getCause();
    }

    @JsonIgnore
    @Override
    public StackTraceElement[] getStackTrace(){
        return super.getStackTrace();
    }


    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
