package com.microideation.app.dialogue.advisors;

import com.microideation.app.dialogue.annotations.FieldMapping;
import com.microideation.app.dialogue.annotations.MasterResource;
import com.microideation.app.dialogue.annotations.Param;
import com.microideation.app.dialogue.annotations.RestResourceField;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;


/**
 * Created by sandheepgr on 24/6/16.
 */
@Aspect
@Component
public class RestResourceFieldAdvisor {

    @Pointcut(value="execution(public * *(..))")
    public void anyPublicMethod() {  }


    @Around(value = "anyPublicMethod() && @annotation(restResourceField)")
    public void restResourceField(ProceedingJoinPoint joinPoint,RestResourceField restResourceField) throws Throwable {

        // Get the method signature
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();

        // Get the interfaceMethod
        Method interfaceMethod = methodSignature.getMethod();

        // Get the implementation method of the interfaceMethod ( in-case of the proxy classes like service.
        // for the normal beans, interfaceMethod and implementationMethod would be same )
        Method implementationMethod = joinPoint.getTarget().getClass().getMethod(interfaceMethod.getName(),interfaceMethod.getParameterTypes());

        // Get the list of the arguments from the joinPoints
        Object args[] = joinPoint.getArgs();

        // Get the object
        Object masterResource = getMasterResource(implementationMethod,args);

        // Get the fieldMaping
        HashMap<String,FieldMapping> fieldMap = getParamFieldMap(restResourceField);




        // Log the resource
        System.out.println("Master resource : " + masterResource);

       /* Annotation[][] annotations=  implementationMethod.getParameterAnnotations();

        for ( int i = 0 ; i < annotations.length ; i++ ) {

            for (Annotation annotation : annotations[i]) {

                if ( Param.class.isInstance(annotation)) {

                    System.out.println("found him");

                }

            }
            
        }
        

        Object args[] = joinPoint.getArgs();
        System.out.println("args " + args);*/
        joinPoint.proceed();


    }



    protected String getMasterFieldValue(Object master, String fieldName) {

        // Get the value
        try {

            // Get the field
            Field field = ReflectionUtils.findField(master.getClass(), fieldName);

            // Check if the fields exists
            if ( field == null ) return null;

            // Make the field accessible
            ReflectionUtils.makeAccessible(field);

            // Get the values
            Object value = field.get(master);

            // If the value is not null, return the string
            if ( value != null ) {

                return value.toString();

            }

        } catch (IllegalAccessException e) {

            e.printStackTrace();

        }


        // Return null
        return null;

    }


    protected HashMap<String,FieldMapping> getParamFieldMap(RestResourceField restResourceField) {

        // Create the Hashmap
        HashMap<String,FieldMapping> paramFieldMap = new HashMap<>(0);

        // Get the list of the mappings
        FieldMapping fieldMappings[] = restResourceField.fieldMappings();

        // If the fieldMappings is null or empty, then return the empty map
        if ( fieldMappings == null || fieldMappings.length == 0 )  return paramFieldMap;

        // Iterate through the mappings
        for ( FieldMapping fieldMapping : fieldMappings ) {

            // Add to map with they key as the param name and the value
            // as the fieldMapping object
            paramFieldMap.put(fieldMapping.paramName(),fieldMapping);

        }

        // Return the argument map
        return paramFieldMap;

    }


    protected HashMap<String,Object> getArgumentParamMap(Method method, Object args[]) {

        // Create the Hashmap
        HashMap<String,Object> argumentMap = new HashMap<>(0);

        // Get the annotations
        Annotation[][] annotations=  method.getParameterAnnotations();

        // Iterate through the args
        for (int i = 0; i < args.length; i++) {

            // Get the annotations for the args
            for (int j = 0; j < annotations[i].length  ; j++) {

                // Check if the annotation is a instance of the Param
                if (Param.class.isInstance(annotations[i][j]) ) {

                    // Cast the object to param
                    Param param = (Param) annotations[i][j];

                    // Add to the hashmap with key as param name and value as the
                    // arg object
                    argumentMap.put(param.name(),args[i]);


                }

            }

        }


        // Return the argument map
        return argumentMap;

    }


    protected Object getMasterResource(Method method, Object args[]) {

        // Get the annotations
        Annotation[][] annotations=  method.getParameterAnnotations();

        // Iterate through the args
        for (int i = 0; i < args.length; i++) {

            // Get the annotations for the args
            for (int j = 0; j < annotations[i].length  ; j++) {

                // Check if the annotation is a instance of the MasterResource
                if (MasterResource.class.isInstance(annotations[i][j]) ) {

                    // Return the object;
                    return args[i];

                }

            }

        }

        // return null;
        return null;

    }

}
