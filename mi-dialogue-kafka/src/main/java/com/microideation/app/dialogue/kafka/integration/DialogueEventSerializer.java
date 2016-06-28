package com.microideation.app.dialogue.kafka.integration;

import com.microideation.app.dialogue.event.DialogueEvent;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * Created by sandheepgr on 28/6/16.
 */
public class DialogueEventSerializer implements Serializer<DialogueEvent> {

    // Create the logger
    private Logger log = LoggerFactory.getLogger(DialogueEventSerializer.class);

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, DialogueEvent data) {

        try {


            if (data == null)
                return null;

            // Create a ByteArrayOutputStream
            ByteArrayOutputStream b = new ByteArrayOutputStream();

            // Create an object output stream
            ObjectOutputStream oos = new ObjectOutputStream(b);

            // write the object to byte stream
            oos.writeObject(data);

            // return the byte array of the output stream
            return b.toByteArray();


        } catch (IOException e) {

            // log the error
            log.error("Error when serializing string to byte[] due to " + e.getMessage());

            // return null
            return null;
        }

    }

    @Override
    public void close() {

    }

}
