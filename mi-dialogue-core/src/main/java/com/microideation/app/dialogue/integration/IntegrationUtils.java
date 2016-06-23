package com.microideation.app.dialogue.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sandheepgr on 20/6/16.
 */
@Component
public class IntegrationUtils {

    @Autowired
    private Environment environment;

    @Resource
    private HashMap<String,String> propertyMapping;


    /**
     * Method to check if the text passed is a property and get the value
     * from the environment.
     *
     * @param text  : The text to be matched for the property
     * @return      : The property value if its starting with $ and has a matching value in
     *                environment
     *                Return the text itself is nothing matching
     */
    public String getEnvironmentProperty(String text) {

        // Check if the text is already been parsed
        if ( propertyMapping.containsKey(text)) {

            return propertyMapping.get(text);

        }


        // If the text does not start with $, then no need to do pattern
        if ( !text.startsWith("$") ) {

            // Add to the mapping with key and value as text
            propertyMapping.put(text,text);

            // If no match, then return the text as it is
            return text;

        }

        // Create the pattern
        Pattern pattern = Pattern.compile("\\Q${\\E(.+?)\\Q}\\E");

        // Create the matcher
        Matcher matcher = pattern.matcher(text);

        // If the matching is there, then add it to the map and return the value
        if( matcher.find() ) {

            // Store the value
            String key = matcher.group(1);

            // Get the value
            String value = environment.getProperty(key);

            // Store the value in the setting
            if ( value != null ) {

                // Store in the map
                propertyMapping.put(text,value);

                // return the value
                return value;

            }

        }

        // Add to the mapping with key and value as text
        propertyMapping.put(text,text);

        // If no match, then return the text as it is
        return text;

    }

}
