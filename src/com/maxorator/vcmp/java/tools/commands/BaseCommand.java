package com.maxorator.vcmp.java.tools.commands;

import com.maxorator.vcmp.java.plugin.integration.player.Player;

public abstract class BaseCommand {

    public Player player;

    public boolean checkAccess() {
        return true;
    }

    public void failedRun(Exception e) {

    }

}
