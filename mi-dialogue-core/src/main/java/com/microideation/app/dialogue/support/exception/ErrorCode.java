package com.microideation.app.dialogue.support.exception;

/**
 * Created by sandheepgr on 20/4/14.
 */
public enum ErrorCode {



    ERR_NO_OBJECT_MAPPER_INSTANCE(1),
    ERR_JSON_MAPPING_EXCEPTION(2),
    ERR_DUPLICATE_SUBSCRIBER_NOT_SUPPORTED(3),
    ERR_INTEGRATION_NOT_AVAILABLE(4),
    ERR_EVENT_NAME_NOT_SPECIFIED(5),
    ERR_EVENT_SPECIFIC_SUBSCRIBER_NOT_SUPPORTED(6);



    private int value;

    ErrorCode(int value) {

        this.value = value;

    }




}
