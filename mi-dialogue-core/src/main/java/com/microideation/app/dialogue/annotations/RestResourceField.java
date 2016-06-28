package com.microideation.app.dialogue.annotations;

import java.lang.annotation.*;

/**
 * Created by sandheepgr on 24/6/16.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RestResourceField {


    FieldMapping[] fieldMappings();






   /* @Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Mappings {

        RestResourceField[] values();

    }*/
}
