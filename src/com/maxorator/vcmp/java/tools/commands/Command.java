package com.maxorator.vcmp.java.tools.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    String name() default "";
    String usage() default "";
}
