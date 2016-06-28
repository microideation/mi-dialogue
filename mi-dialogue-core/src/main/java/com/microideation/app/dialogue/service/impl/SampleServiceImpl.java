package com.microideation.app.dialogue.service.impl;

import com.microideation.app.dialogue.annotations.MasterResource;
import com.microideation.app.dialogue.annotations.Param;
import com.microideation.app.dialogue.annotations.FieldMapping;
import com.microideation.app.dialogue.annotations.RestResourceField;
import com.microideation.app.dialogue.event.TestType;
import com.microideation.app.dialogue.service.SampleService;
import org.springframework.stereotype.Service;

/**
 * Created by sandheepgr on 25/6/16.
 */
@Service
public class SampleServiceImpl implements SampleService {

    @RestResourceField(fieldMappings = {
            @FieldMapping(paramName = "testType",url = "test.inspirentz.com"),
            @FieldMapping(paramName = "object",url = "test.inspirentz.com")
    })
    public void callMethod(@Param(name = "object") Object object, String arg,  @MasterResource(name = "testType")TestType testType) {

        System.out.println("Inside callMethod : ");

    }

}
