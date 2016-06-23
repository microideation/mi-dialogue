package com.microideation.app.dialogue.autoconfig;

import com.microideation.app.dialogue.MIDialogueApplication;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by sandheepgr on 19/6/16.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import({MIDialogueApplication.class})
public @interface EnableDialogue {
}
