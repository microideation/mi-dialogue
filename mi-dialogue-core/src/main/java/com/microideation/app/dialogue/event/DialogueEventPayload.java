package com.microideation.app.dialogue.event;

import com.microideation.app.dialogue.authority.EventAuthority;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by sandheepgr on 31/3/17.
 */
@Setter
@Getter
@ToString
public abstract class DialogueEventPayload implements Serializable {



    private EventAuthority eventAuthority;


    /**
     * Method to set the event authority
     *
     * @param eventAuthority : The EventAuthority object to be set
     */
    public void setEventAuthority(EventAuthority eventAuthority) {

        // Set to the object only if not null
        if ( eventAuthority != null ) {

            this.eventAuthority = eventAuthority;

        }

    }

}
