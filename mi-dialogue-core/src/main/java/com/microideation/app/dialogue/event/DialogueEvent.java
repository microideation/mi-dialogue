package com.microideation.app.dialogue.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microideation.app.dialogue.support.exception.DialogueException;
import com.microideation.app.dialogue.support.exception.ErrorCode;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by sandheepgr on 17/6/16.
 */
public class DialogueEvent implements Serializable{

    private String payload;

    private Header headers;


    @JsonIgnore
    private transient ObjectMapper objectMapper;


    public DialogueEvent() {}

    public DialogueEvent(String payload) {

        this.payload = payload;

    }


    public <T> T getPayload(Class<T> type)  {

        // Check if the object mapper is set
        if ( objectMapper == null ) {

            throw new DialogueException(ErrorCode.ERR_NO_OBJECT_MAPPER_INSTANCE,"ObjectMapper instance is not set");

        }


        try {

            // return the object
            return objectMapper.readValue(this.payload,type);

        } catch (IOException e) {

            // Print the stack trace
            e.printStackTrace();

            // return null
            throw new DialogueException(ErrorCode.ERR_JSON_MAPPING_EXCEPTION,e.getMessage());

        }

    }

    public <T> T getPayload(ObjectMapper objectMapper, Class<T> type) {

        // Set the objectMapper
        this.objectMapper = objectMapper;

        // call the getPayload
        return getPayload(type);

    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Header getHeaders() {
        return headers;
    }

    public void setHeaders(Header headers) {
        this.headers = headers;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String toString() {
        return "DialogueEvent{" +
                "payload='" + payload + '\'' +
                '}';
    }
}
