package com.maxorator.vcmp.java.tools.commands;

public class CommandParameterInfo {

    public final Class<?> klass;
    public final boolean mayNotFind;
    public final boolean fuzzySearch;
    public final boolean allMatch;

    public CommandParameterInfo(Class<?> klass, boolean mayNotFind, boolean fuzzySearch, boolean allMatch) {
        this.klass = klass;
        this.mayNotFind = mayNotFind;
        this.fuzzySearch = fuzzySearch;
        this.allMatch = allMatch;
    }

}
