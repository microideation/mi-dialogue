package com.microideation.app.dialogue.authority;

/**
 * Created by sandheepgr on 26/5/17.
 */
public interface DialogueAuthorityManager {

    /**
     * Method to return the event owner object containing the EventAuthority
     * The implementing service need to populate the EventAuthority fields and
     * return the object
     *
     * @return : The EventAuthority instance containing the current user
     *           information
     */
    EventAuthority getEventAuthority();

    /**
     * Method to set the SecurityContext when the SubscribeEvent annotated method
     * is invoked ( SubscribeEventAdvisor ).
     * The implementing service need to have the SecurityContext set using the
     * principal in the EventAuthority object passed
     *
     * @param eventAuthority : The EventAuthority object received
     */
    void setEventAuthorityAuthContext(EventAuthority eventAuthority);


}
