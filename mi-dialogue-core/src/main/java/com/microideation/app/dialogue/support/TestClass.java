package com.microideation.app.dialogue.support;

import com.microideation.app.dialogue.annotations.Param;
import com.microideation.app.dialogue.annotations.RestResourceField;
import com.microideation.app.dialogue.event.TestType;
import org.springframework.stereotype.Component;

/**
 * Created by sandheepgr on 24/6/16.
 */
@Component
public class TestClass {

    //@RestResourceField(path = "/test",argName = "myArg")
    public void callMethod(Object object, String arg, @Param(name = "testType") TestType testType) {

        System.out.println("Inside callMethod : ");

    }

}
