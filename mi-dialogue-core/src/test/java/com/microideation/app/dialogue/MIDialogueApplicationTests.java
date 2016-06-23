package com.microideation.app.dialogue;

import com.microideation.app.dialogue.sample.service.SampleService;
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

    /*@Autowired
    private SampleService sampleService;
*/
    @Test
    public void contextLoad() {}

    /*@Test
    public void testRedis() {

        sampleService.publishToRedis();

    }


    @Test
    public void testRabbit() {

        sampleService.publishToRabbit();

    }*/

}
