package com.microideation.app.dialogue.kafka.integration;

import com.microideation.app.dialogue.event.DialogueEvent;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

/**
 * Created by sandheepgr on 28/6/16.
 */
public class DialogueEventDeserializer implements Deserializer<DialogueEvent> {

    // Create the logger
    private Logger log = LoggerFactory.getLogger(DialogueEventDeserializer.class);

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public DialogueEvent deserialize(String topic, byte[] data) {

        try {
            if (data == null)
                return null;

            // Create the byte array input stream object with the data
            ByteArrayInputStream in = new ByteArrayInputStream(data);

            // Create the ObjectInputStream from the byte array
            ObjectInputStream is = new ObjectInputStream(in);

            // Return the object
            return (DialogueEvent) is.readObject();

        } catch (ClassNotFoundException e) {

            // Print the stack trace
            e.printStackTrace();

            // log the error
            log.error("ClassNotFoundException when deserializing " + e.getMessage());

            // return null
            return null;

        } catch (IOException e) {

            // Print the stack trace
            e.printStackTrace();

            // log the error
            log.error("IOException when de-serializing string to byte[] due to " + e.getMessage());

            // return null
            return null;
        }

    }

    @Override
    public void close() {

    }
}
