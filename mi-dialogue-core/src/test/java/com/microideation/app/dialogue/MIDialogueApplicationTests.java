package com.microideation.app.dialogue;

import com.microideation.app.dialogue.annotations.RestResourceField;
import com.microideation.app.dialogue.event.TestType;
import com.microideation.app.dialogue.service.SampleService;
import com.microideation.app.dialogue.support.TestClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MIDialogueApplication.class)
@WebAppConfiguration
public class MIDialogueApplicationTests {

    @Autowired
    private TestClass testClass;

    @Autowired
    private SampleService sampleService;

    @Test
    public void contextLoad() {}


    @Test
    public void testMethod() {

        sampleService.callMethod("This is a test", new String("test data"), new TestType("test data"));

    }

}
