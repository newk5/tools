package com.maxorator.vcmp.java.tools.events;

import com.maxorator.vcmp.java.plugin.integration.EventHandler;
import com.maxorator.vcmp.java.plugin.integration.RootEventHandler;
import com.maxorator.vcmp.java.plugin.integration.placeable.CheckPoint;
import com.maxorator.vcmp.java.plugin.integration.placeable.GameObject;
import com.maxorator.vcmp.java.plugin.integration.placeable.Pickup;
import com.maxorator.vcmp.java.plugin.integration.player.Player;
import com.maxorator.vcmp.java.plugin.integration.server.Server;
import com.maxorator.vcmp.java.plugin.integration.vehicle.Vehicle;
import com.maxorator.vcmp.java.tools.commands.CommandController;
import com.maxorator.vcmp.java.tools.commands.CommandRegistry;
import com.maxorator.vcmp.java.tools.timers.TimerRegistry;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class DelegatingEventHandler extends RootEventHandler {

    private final Deque<EventHandler> delegates;
    public final TimerRegistry timers;
    public final CommandRegistry commands;

    public DelegatingEventHandler(Server server) {
        super(server);

        timers = new TimerRegistry();
        commands = new CommandRegistry(server);
        delegates = new ArrayDeque<>();
    }

    public void add(Object eventOrCommandHandler) {
        if (eventOrCommandHandler instanceof EventHandler) {
            delegates.addFirst((EventHandler) eventOrCommandHandler);
        }

        if (eventOrCommandHandler instanceof CommandController) {
            commands.addController((CommandController) eventOrCommandHandler);
        }
    }

    private long calculateUsedEventFlags() {
        long flags = 0;
        Map<String, Long> eventIndex = new HashMap<>();

        EventMethodName[] values = EventMethodName.values();
        for (int i = 0; i < values.length; i++) {
            eventIndex.put(values[i].name(), (long) i);
        }

        for (EventHandler eventHandler : delegates) {
            for (Method method : eventHandler.getClass().getDeclaredMethods()) {
                Long index = eventIndex.get(method.getName());
                if (index != null) {
                    flags |= 1L << index;
                }
            }
        }

        flags |= (1 << EventMethodName.onServerFrame.ordinal());
        flags |= (1 << EventMethodName.onPlayerCommand.ordinal());

        return flags;
    }

    public void takeOver() {
        server.rewireEvents(this, calculateUsedEventFlags());
    }

    @Override
    public void onServerLoadScripts() {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onServerLoadScripts();
        }
    }

    @Override
    public void onServerUnloadScripts() {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onServerUnloadScripts();
        }
    }

    @Override
    public boolean onServerInitialise() {
        boolean success = true;

        for (EventHandler eventHandler : delegates) {
            if (!eventHandler.onServerInitialise()) {
                success = false;
            }
        }

        return success;
    }

    @Override
    public void onServerShutdown() {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onServerShutdown();
        }
    }

    @Override
    public void onServerFrame() {
        timers.process();

        for (EventHandler eventHandler : delegates) {
            eventHandler.onServerFrame();
        }
    }

    @Override
    public void onPluginCommand(int identifier, String message) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPluginCommand(identifier, message);
        }
    }

    @Override
    public String onIncomingConnection(String name, String password, String ip) {
        String last = name;

        for (EventHandler eventHandler : delegates) {
            if (last != null) {
                last = eventHandler.onIncomingConnection(last, password, ip);
            }
        }

        return last;
    }

    @Override
    public void onClientScriptData(Player player, byte[] data) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onClientScriptData(player, data);
        }
    }

    @Override
    public void onPlayerConnect(Player player) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPlayerConnect(player);
        }
    }

    @Override
    public void onPlayerDisconnect(Player player, int reason) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPlayerDisconnect(player, reason);
        }
    }

    @Override
    public boolean onPlayerRequestClass(Player player, int classIndex) {
        boolean success = true;

        for (EventHandler eventHandler : delegates) {
            if (!eventHandler.onPlayerRequestClass(player, classIndex)) {
                success = false;
            }
        }

        return success;
    }

    @Override
    public boolean onPlayerRequestSpawn(Player player) {
        boolean success = true;

        for (EventHandler eventHandler : delegates) {
            if (!eventHandler.onPlayerRequestSpawn(player)) {
                success = false;
            }
        }

        return success;
    }

    @Override
    public void onPlayerSpawn(Player player) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPlayerSpawn(player);
        }
    }

    @Override
    public void onPlayerDeath(Player player, Player killer, int reason, int bodyPart) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPlayerDeath(player, killer, reason, bodyPart);
        }
    }

    @Override
    public void onPlayerUpdate(Player player, int updateType) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPlayerUpdate(player, updateType);
        }
    }

    @Override
    public boolean onPlayerRequestEnterVehicle(Player player, Vehicle vehicle, int slot) {
        boolean success = true;

        for (EventHandler eventHandler : delegates) {
            if (!eventHandler.onPlayerRequestEnterVehicle(player, vehicle, slot)) {
                success = false;
            }
        }

        return success;
    }

    @Override
    public void onPlayerEnterVehicle(Player player, Vehicle vehicle, int slot) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPlayerEnterVehicle(player, vehicle, slot);
        }
    }

    @Override
    public void onPlayerExitVehicle(Player player, Vehicle vehicle) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPlayerExitVehicle(player, vehicle);
        }
    }

    @Override
    public void onPlayerNameChange(Player player, String oldName, String newName) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPlayerNameChange(player, oldName, newName);
        }
    }

    @Override
    public void onPlayerStateChange(Player player, int oldState, int newState) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPlayerStateChange(player, oldState, newState);
        }
    }

    @Override
    public void onPlayerActionChange(Player player, int oldAction, int newAction) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPlayerActionChange(player, oldAction, newAction);
        }
    }

    @Override
    public void onPlayerOnFireChange(Player player, boolean isOnFire) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPlayerOnFireChange(player, isOnFire);
        }
    }

    @Override
    public void onPlayerCrouchChange(Player player, boolean isCrouching) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPlayerCrouchChange(player, isCrouching);
        }
    }

    @Override
    public void onPlayerGameKeysChange(Player player, int oldKeys, int newKeys) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPlayerGameKeysChange(player, oldKeys, newKeys);
        }
    }

    @Override
    public void onPlayerBeginTyping(Player player) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPlayerBeginTyping(player);
        }
    }

    @Override
    public void onPlayerEndTyping(Player player) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPlayerEndTyping(player);
        }
    }

    @Override
    public void onPlayerAwayChange(Player player, boolean isAway) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPlayerAwayChange(player, isAway);
        }
    }

    @Override
    public boolean onPlayerMessage(Player player, String message) {
        for (EventHandler eventHandler : delegates) {
            if (!eventHandler.onPlayerMessage(player, message)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean onPlayerCommand(Player player, String message) {
        if (commands.processCommand(player, message)) {
            return true;
        }

        for (EventHandler eventHandler : delegates) {
            if (eventHandler.onPlayerCommand(player, message)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onPlayerPrivateMessage(Player player, Player recipient, String message) {
        for (EventHandler eventHandler : delegates) {
            if (!eventHandler.onPlayerPrivateMessage(player, recipient, message)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onPlayerKeyBindDown(Player player, int keyBindIndex) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPlayerKeyBindDown(player, keyBindIndex);
        }
    }

    @Override
    public void onPlayerKeyBindUp(Player player, int keyBindIndex) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPlayerKeyBindUp(player, keyBindIndex);
        }
    }

    @Override
    public void onPlayerSpectate(Player player, Player spectated) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPlayerSpectate(player, spectated);
        }
    }

    @Override
    public void onVehicleUpdate(Vehicle vehicle, int updateType) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onVehicleUpdate(vehicle, updateType);
        }
    }

    @Override
    public void onVehicleExplode(Vehicle vehicle) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onVehicleExplode(vehicle);
        }
    }

    @Override
    public void onVehicleRespawn(Vehicle vehicle) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onVehicleRespawn(vehicle);
        }
    }

    @Override
    public void onObjectShot(GameObject object, Player player, int weaponId) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onObjectShot(object, player, weaponId);
        }
    }

    @Override
    public void onObjectTouched(GameObject object, Player player) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onObjectTouched(object, player);
        }
    }

    @Override
    public boolean onPickupPickAttempt(Pickup pickup, Player player) {
        for (EventHandler eventHandler : delegates) {
            if (!eventHandler.onPickupPickAttempt(pickup, player)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onPickupPicked(Pickup pickup, Player player) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPickupPicked(pickup, player);
        }
    }

    @Override
    public void onPickupRespawn(Pickup pickup) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPickupRespawn(pickup);
        }
    }

    @Override
    public void onCheckPointEntered(CheckPoint checkPoint, Player player) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onCheckPointEntered(checkPoint, player);
        }
    }

    @Override
    public void onCheckPointExited(CheckPoint checkPoint, Player player) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onCheckPointExited(checkPoint, player);
        }
    }

    @Override
    public void onPlayerCrashReport(Player player, String crashLog) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPlayerCrashReport(player, crashLog);
        }
    }

    @Override
    public void onPlayerModuleList(Player player, String list) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onPlayerModuleList(player, list);
        }
    }

    @Override
    public void onServerPerformanceReport(int entry, String[] descriptions, long[] times) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onServerPerformanceReport(entry, descriptions, times);
        }
    }

  /*  @Override
    public void onEntityStreamingChange(Player player, GameObject object, int entityType, boolean isDeleted) {
        for (EventHandler eventHandler : delegates) {
            eventHandler.onEntityStreamingChange(player, object, entityType, isDeleted);
        }
    }*/

}
