package com.microideation.app.dialogue.annotations;

import java.lang.annotation.*;

/**
 * Created by sandheepgr on 26/6/16.
 */
@Target({ ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FieldMapping {

    public String url();
    public String paramName();

}