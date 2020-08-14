package com.maxorator.vcmp.java.tools.commands;

public class CommandParameterInfo {

    public final Class<?> klass;
    public final boolean mayNotFind;
    public final boolean fuzzySearch;
    public final boolean allMatch;
    public final boolean optional;

    public CommandParameterInfo(Class<?> klass, boolean mayNotFind, boolean fuzzySearch, boolean allMatch, boolean optional) {
        this.klass = klass;
        this.mayNotFind = mayNotFind;
        this.fuzzySearch = fuzzySearch;
        this.allMatch = allMatch;
        this.optional = optional;
    }

}
