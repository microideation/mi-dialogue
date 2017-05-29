package com.microideation.app.dialogue.annotations;

import com.microideation.app.dialogue.event.EventStore;
import com.microideation.app.dialogue.event.PublishType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by sandheepgr on 17/6/16.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD) //can use in method only.
public @interface PublishEvent {

    EventStore eventStore();
    String eventName() default "";
    boolean isPersistent() default false;
    String channelName();
    PublishType publishType() default PublishType.BROADCAST;
    boolean isSetAuthority() default true;

}
