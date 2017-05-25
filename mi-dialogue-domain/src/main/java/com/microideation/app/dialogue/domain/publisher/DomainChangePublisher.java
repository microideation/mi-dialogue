package com.microideation.app.dialogue.domain.publisher;

/**
 * Created by sandheepgr on 24/5/17.
 */
public interface DomainChangePublisher {

    /**
     * Method to return the channel for the events
     * @return : Return the channel name
     */
    String getChannelName();

    /**
     * Method to return the Resource class for the data
     *
     * @return : Resorce class for the domain
     */
    Class getResourceClass();

}
