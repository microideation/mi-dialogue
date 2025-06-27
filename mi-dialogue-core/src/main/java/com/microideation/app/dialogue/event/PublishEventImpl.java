package com.microideation.app.dialogue.event;

import com.microideation.app.dialogue.annotations.PublishEvent;

import java.lang.annotation.Annotation;

/**
 * Created by sandheepgr on 29/3/17.
 */
public class PublishEventImpl implements PublishEvent {

    private EventStore eventStore;
    private String eventName;
    private String channelName;
    private boolean isPersistent;
    private PublishType publishType;
    private Class<? extends Annotation> annotationType;
    private boolean isSetAuthority;
    private int partitionCount;
    private int replicationFactor;


    // Annotation overridden methods
    @Override
    public EventStore eventStore() {
        return eventStore;
    }

    @Override
    public String eventName() {
        return eventName;
    }

    @Override
    public boolean isPersistent() {
        return isPersistent;
    }

    @Override
    public String channelName() {
        return channelName;
    }

    @Override
    public PublishType publishType() {
        return publishType;
    }

    @Override
    public boolean isSetAuthority() {
        return isSetAuthority;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return annotationType;
    }

    @Override
    public int partitionCount() {
        return partitionCount;
    }

    @Override
    public int replicationFactor() {
        return replicationFactor;
    }


    // Setter methods
    public void setEventStore(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public void setPersistent(boolean persistent) {
        isPersistent = persistent;
    }

    public void setPublishType(PublishType publishType) {
        this.publishType = publishType;
    }

    public void setAnnotationType(Class<? extends Annotation> annotationType) {
        this.annotationType = annotationType;
    }

    public void setAuthority(boolean isSetAuthority) {
        this.isSetAuthority = isSetAuthority;
    }

    public void setPartitionCount(int partitionCount) {
        this.partitionCount = partitionCount;
    }

    public void setReplicationFactor(int replicationFactor) {
        this.replicationFactor = replicationFactor;
    }
}
