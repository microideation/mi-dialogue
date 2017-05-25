package com.microideation.app.dialogue.domain.config;

import com.microideation.app.dialogue.domain.support.AutowireHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sandheepgr on 25/5/17.
 */
@Configuration
public class DomainChangeConfig {

    @Bean
    public AutowireHelper autowireHelper(){
        return AutowireHelper.getInstance();
    }

}
