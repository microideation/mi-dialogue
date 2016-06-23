package com.microideation.app.dialogue.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Created by sandheepgr on 18/6/16.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface DialogueEventListener {
}
