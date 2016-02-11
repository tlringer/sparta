package sparta.checkers.quals;

import org.checkerframework.framework.qual.SubtypeOf;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * An @Extra annotation contains a key K and a type T (Source and Sink). 
 * This means that the key K maps to a value of type T. 
 * In the case of the information flow system, the type T represents data 
 * and constrains its possible flows.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE_PARAMETER, ElementType.TYPE_USE,
/* The following only added to make Eclipse work. */
ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.LOCAL_VARIABLE })
@SubtypeOf({})
public @interface Extra {
    String key() default "";
    FlowPermission[] source() default {FlowPermission.EXTRA_DEFAULT};
    FlowPermission[] sink() default {FlowPermission.EXTRA_DEFAULT};
}