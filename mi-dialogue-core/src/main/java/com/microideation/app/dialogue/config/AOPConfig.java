package com.microideation.app.dialogue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * Created by sandheepgr on 17/6/16.
 */
@Configuration
@ComponentScan(basePackages = {"com.microideation.app.dialogue.advisors","com.microideation.app.dialogue.support"})
public class AOPConfig {

    @Bean
    public HashMap<String,String> propertyMapping() {

        return new HashMap<>(0);

    }

}
