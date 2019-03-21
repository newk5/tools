package com.maxorator.vcmp.java.tools.commands;

import java.lang.reflect.Method;

public class CommandInfo {
    public final CommandController controller;
    public final Method method;
    public final String name;
    public final String usage;
    public final CommandParameterInfo[] parameters;

    public CommandInfo(CommandController controller, Method method, String name, String usage, CommandParameterInfo[] parameters) {
        this.controller = controller;
        this.method = method;
        this.name = name;
        this.usage = usage;
        this.parameters = parameters;
    }

    public boolean endsWithString() {
        return parameters.length > 0 && parameters[parameters.length - 1].klass == String.class;
    }
}
