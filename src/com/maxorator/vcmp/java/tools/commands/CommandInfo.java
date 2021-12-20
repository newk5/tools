package com.maxorator.vcmp.java.tools.commands;

import java.lang.reflect.Method;

public class CommandInfo {
    public final CommandController controller;
    public final Method method;
    public final Method preMethod;
    public final Method postMethod;
    public final String name;
    public final String usage;
    public final CommandParameterInfo[] parameters;
    public BaseCommand baseCommand;
    public CommandValidator validator;

    public CommandInfo(CommandController controller, Method method, String name, String usage, CommandParameterInfo[] parameters) {
        this.controller = controller;
        this.method = method;
        this.name = name;
        this.usage = usage;
        this.parameters = parameters;
        this.preMethod = null;
        this.postMethod = null;

    }

    public CommandInfo(BaseCommand cmd, Method method, String name, String usage, CommandParameterInfo[] parameters, Method preMethod, Method postMethod) {
        this.baseCommand = cmd;
        this.method = method;
        this.name = name;
        this.usage = usage;
        this.parameters = parameters;
        this.controller = null;
        this.preMethod = preMethod;
        this.postMethod = postMethod;

    }

    public boolean endsWithString() {
        return parameters.length > 0 && parameters[parameters.length - 1].klass == String.class;
    }
}
