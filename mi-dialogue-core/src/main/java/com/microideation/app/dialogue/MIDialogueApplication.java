package com.microideation.app.dialogue;

import com.microideation.app.dialogue.event.TestType;
import com.microideation.app.dialogue.support.TestClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@SpringBootApplication
public class MIDialogueApplication {

	public static void main(String[] args) {


		SpringApplication.run(MIDialogueApplication.class, args);

	}


}

@org.springframework.web.bind.annotation.RestController
class RestController {

    @Autowired
    private TestClass testClass;

    @RequestMapping( path = "/call" , method = RequestMethod.GET)
    public void call() {

        testClass.callMethod("This is a test", new String("test data"), new TestType("test data"));


    }

}