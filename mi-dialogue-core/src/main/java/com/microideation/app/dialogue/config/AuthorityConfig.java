package com.microideation.app.dialogue.config;

import com.microideation.app.dialogue.authority.DialogueAuthorityManager;
import com.microideation.app.dialogue.authority.EventAuthority;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by sandheepgr on 26/5/17.
 */
@Configuration
public class AuthorityConfig {

    @Bean
    @ConditionalOnMissingBean(DialogueAuthorityManager.class)
    public DialogueAuthorityManager dialogueAuthorityManager(){

        return new DialogueAuthorityManagerImpl();

    }


    // Dummy class implementing the bean DialogueAuthorityManager
    private class DialogueAuthorityManagerImpl implements DialogueAuthorityManager {

        @Override
        public EventAuthority getEventAuthority() {
            return null;
        }

        @Override
        public void setEventAuthorityAuthContext(EventAuthority eventAuthority) {
            return;
        }
    }


}
