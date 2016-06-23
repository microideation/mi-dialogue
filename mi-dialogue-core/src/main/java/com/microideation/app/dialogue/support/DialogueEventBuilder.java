package com.microideation.app.dialogue.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by sandheepgr on 18/6/16.
 */
@Component
public class DialogueEventBuilder {

    @Autowired
    private ObjectMapper objectMapper;

}

