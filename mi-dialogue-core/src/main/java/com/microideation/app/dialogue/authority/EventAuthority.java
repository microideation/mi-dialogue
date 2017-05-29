package com.microideation.app.dialogue.authority;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by sandheepgr on 26/5/17.
 */
@Setter
@Getter
@ToString
public class EventAuthority implements Serializable {


    // Principal or username
    private String principal;

    // Authentication token
    private String authToken;

    // Extra params map
    @Setter(AccessLevel.NONE)
    private HashMap<String,Object> params = new HashMap<>(0);


    /**
     * Method to set the param value key pair
     * @param key      : The key value
     * @param value    : The value for the key
     */
    public void setExtraParam(String key, Object value) {

        // Initialize the params
        if ( params == null ) params = new HashMap<>(0);

        // Set the key and value
        params.put(key,value);

    }


    /**
     * Method to return the value corresponding to the key
     * @param key   : The key of the map
     * @return      : Return the value if the key is found
     *                Return null if the key is not found
     */
    public Object getExtraParam(String key) {

        return params.get(key);

    }

}
