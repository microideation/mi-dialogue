package com.microideation.app.dialogue.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microideation.app.dialogue.authority.EventAuthority;
import com.microideation.app.dialogue.dictionary.DialogueHeaderKeys;
import com.microideation.app.dialogue.support.exception.DialogueException;
import com.microideation.app.dialogue.support.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sandheepgr on 17/6/16.
 */
@Slf4j
public class DialogueEvent implements Serializable{

    // Variable holding the payload in JSON string format
    private String payload;

    // The headers ( Hashmap )
    private Header headers;


    @JsonIgnore
    private transient ObjectMapper objectMapper;


    /**
     * Default constructor
     * Initializes the header
     */
    public DialogueEvent() {
        this.headers = new Header();
    }

    /**
     * Constructor accepting the payload
     * Initializes the  Header
     * @param payload : The payload string
     */
    public DialogueEvent(String payload) {

        this.payload = payload;
        this.headers = new Header();

    }


    /**
     * Method to get the payload based on the passed type
     *
     * @param type : The Class object type refernece
     * @param <T>  : The Type reference for the payload object
     * @return     : Return the payload in the specified type
     *
     */
    public <T> T getPayload(Class<T> type)  {

        // Check if the object mapper is set
        if ( objectMapper == null ) {

            throw new DialogueException(ErrorCode.ERR_NO_OBJECT_MAPPER_INSTANCE,"ObjectMapper instance is not set");

        }


        try {

            // Parse the object to retValue
            T retValue = objectMapper.readValue(this.payload,type);

            // If the retValue is instance of DialogueEventPayload, then set the eventAuthority
            if ( retValue instanceof DialogueEventPayload ) {

                ((DialogueEventPayload) retValue).setEventAuthority(getEventAuthority());

            }

            // return the object
            return retValue;

        } catch (IOException e) {

            // Print the stack trace
            e.printStackTrace();

            // return null
            throw new DialogueException(ErrorCode.ERR_JSON_MAPPING_EXCEPTION,e.getMessage());

        }

    }


    /**
     * Get the payload by passing a customer object mapper to be used
     *
     * @param objectMapper : The object mapper instance to be used
     * @param type         : The class reference for the payload
     * @param <T>          : The TypeReference for the payload object
     * @return             : Return the object
     */
    public <T> T getPayload(ObjectMapper objectMapper, Class<T> type) {

        // Set the objectMapper
        this.objectMapper = objectMapper;

        // call the getPayload
        return getPayload(type);

    }


    /**
     * Get the payload in raw json string format.
     *
     * @return : Return the payload in string format
     */
    public String getPayload() {
        return payload;
    }


    /**
     * Method to get the EventAuthority object from the values
     * set in the headers for the auth
     *
     * @return : null if the headers are empty or if there are no auth headers
     *           Return the eventAuthority object
     */
    public EventAuthority getEventAuthority() {

        // Check if headers are set
        if( headers == null || headers.size() == 0 )  return null;

        // Check if the headers contains the EventAuthority fields
        if (!headers.containsKey(DialogueHeaderKeys.AUTH_PRINCIPAL) &&
            !headers.containsKey(DialogueHeaderKeys.AUTH_TOKEN) &&
            !headers.containsKey(DialogueHeaderKeys.AUTH_PARAMS)) {

            // return null
            return null;
        }

        // Create the EventAuthority
        EventAuthority eventAuthority = new EventAuthority();

        // Check if the principal is set
        if ( headers.containsKey(DialogueHeaderKeys.AUTH_PRINCIPAL) )
            eventAuthority.setPrincipal(headers.get(DialogueHeaderKeys.AUTH_PRINCIPAL).toString());

        // Check if the token is set
        if ( headers.containsKey(DialogueHeaderKeys.AUTH_TOKEN) )
            eventAuthority.setPrincipal(headers.get(DialogueHeaderKeys.AUTH_TOKEN).toString());

        // Check if the params are set
        if ( headers.containsKey(DialogueHeaderKeys.AUTH_PARAMS) ) {

            try {

                // Convert the params to HashMap
                HashMap<String,Object> params = (HashMap<String, Object>) headers.get(DialogueHeaderKeys.AUTH_PARAMS);

                // Iterate the params
                for(Map.Entry<String,Object> entry: params.entrySet()) {

                    // Add to the eventAuthority
                    eventAuthority.setExtraParam(entry.getKey(),entry.getValue());

                }

            } catch (Exception e) {

                // Log the error
                log.error("getEventAuthority -> Error setting auth params - Message: " + e.getMessage());

                // Print the stack trace
                e.printStackTrace();

            }
        }

        // Return the event authority object
        return eventAuthority;
    }


    /**
     * Method to set the authority header from the EventAuthority
     *
     * @param eventAuthority : EventAuthority object
     */
    public void setAuthorityHeader(EventAuthority eventAuthority) {
     
        // Check if the eventAuthority is null
        if ( eventAuthority == null ) return ;

        // Set the principal if its existing
        if ( eventAuthority.getPrincipal() != null )
            headers.put(DialogueHeaderKeys.AUTH_PRINCIPAL,eventAuthority.getPrincipal());

        // Set the token if its existing
        if ( eventAuthority.getAuthToken() != null )
            headers.put(DialogueHeaderKeys.AUTH_TOKEN,eventAuthority.getAuthToken());

        // Set the auth param if its existing
        if ( eventAuthority.getParams() != null && eventAuthority.getParams().size() > 0 )
            headers.put(DialogueHeaderKeys.AUTH_PARAMS,eventAuthority.getParams());

    }


    /*
     * Getters and Setters
     */
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
                ", headers=" + headers +
                '}';
    }
}
