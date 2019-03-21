package com.maxorator.vcmp.java.tools.commands;

import com.maxorator.vcmp.java.plugin.integration.player.Player;

public interface CommandController {
    boolean checkAccess(Player player);
}
