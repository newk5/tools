package com.maxorator.vcmp.java.tools.commands;

import com.maxorator.vcmp.java.plugin.integration.generic.Colour;
import com.maxorator.vcmp.java.plugin.integration.vehicle.Vehicle;
import com.maxorator.vcmp.java.plugin.integration.placeable.GameObject;
import com.maxorator.vcmp.java.plugin.integration.player.Player;
import com.maxorator.vcmp.java.plugin.integration.server.Server;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CommandRegistry {

    protected final Server server;
    protected final Map<String, CommandInfo> commands;
    protected final List<Class<?>> supportedTypes;
    protected String prefix;
    private Colour colour;
    private String usagePrefix = "Usage: ";
    private boolean caseInsensitive;

    public CommandRegistry(Server server) {
        this.server = server;
        this.commands = new HashMap<>();
        this.supportedTypes = new ArrayList<>();

        supportedTypes.add(String.class);
        supportedTypes.add(Integer.class);
        supportedTypes.add(int.class);
        supportedTypes.add(Float.class);
        supportedTypes.add(float.class);
        supportedTypes.add(Boolean.class);
        supportedTypes.add(boolean.class);
        supportedTypes.add(Player.class);
        supportedTypes.add(Vehicle.class);
        supportedTypes.add(GameObject.class);

        prefix = "";
        colour = new Colour(0xFFFF5500);
    }

    public void configureMessages(Colour colour, String prefix) {
        this.colour = colour;
        this.prefix = prefix;
    }

    protected void sendResponse(Player player, String message) {
        server.sendClientMessage(player, colour, prefix + message);
    }

    protected Method getPostCmdMethod(BaseCommand c) {
        for (Method method : c.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostCommand.class)) {
                return method;
            }
        }
        return null;
    }

    protected boolean hasSameParams(Method m1, Method m2) {
        if (m1.getParameterCount() == m2.getParameterCount()) {
            return Arrays.equals(m1.getParameterTypes(), m2.getParameterTypes());
        }
        return false;
    }

    protected Method getPreCmdMethod(BaseCommand c) {
        for (Method method : c.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(PreCommand.class)) {
                return method;
            }
        }
        return null;
    }

    protected void processMethod(BaseCommand controller, Method method, Command config) {
        Class<?>[] types = method.getParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();
        String commandName = config.name().isEmpty() ? method.getName().toLowerCase() : config.name();
        String builtUsage = "";

        CommandParameterInfo[] parameters = new CommandParameterInfo[types.length];

        for (int i = 0; i < types.length; i++) {
            boolean mayNotFind = false;
            boolean fuzzySearch = false;
            boolean allMatch = false;

            for (int j = 0; j < annotations[i].length; j++) {
                if (annotations[i + 1][j] instanceof PartialMatch) {
                    fuzzySearch = true;
                } else if (annotations[i][j] instanceof NullIfNotFound) {
                    mayNotFind = true;
                } else if (annotations[i][j] instanceof AllMatch) {
                    allMatch = true;
                }
            }

            if (!supportedTypes.contains(types[i])) {
                System.err.println("Cannot add command " + commandName + ": Parameter " + (i + 2) + " is of unsupported type " + types[i + 1].getName() + ".");
                return;
            }

            builtUsage += "<" + types[i].getSimpleName().toLowerCase() + "> ";

            parameters[i] = new CommandParameterInfo(types[i], mayNotFind, fuzzySearch, allMatch);
        }

        String usage = config.usage().isEmpty() ? builtUsage : config.usage();

        Method pre = getPreCmdMethod(controller);
        Method post = getPostCmdMethod(controller);
        boolean preHasSameSig = hasSameParams(pre, method);
        boolean postHasSameSig = hasSameParams(post, method);

        CommandInfo commandInfo = new CommandInfo(controller, method, commandName, usage, parameters, preHasSameSig ? pre : null, postHasSameSig ? post : null);

        if (config.validator() != null) {
            Constructor[] ctors = config.validator().getConstructors();
            if (ctors.length == 0) {
                System.err.println("ERROR: Failed to add converter " + config.name() + ", must have a default constructor!");
            } else {
                try {
                    Object instance = ctors[0].newInstance();
                    if (instance instanceof CommandValidator) {
                        commandInfo.validator = (CommandValidator) instance;
                    } else {
                        System.err.println("ERROR: Failed to add converter " + config.name() + ", class must extend CommandValidator");

                    }
                } catch (Exception invocationTargetException) {
                }

            }
        }

        commands.put(commandInfo.name, commandInfo);
    }

    protected void processMethod(CommandController controller, Method method, Command config) {
        Class<?>[] types = method.getParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();
        String commandName = config.name().isEmpty() ? method.getName().toLowerCase() : config.name();
        String builtUsage = "";

        if (types.length == 0 || !types[0].isAssignableFrom(Player.class)) {
            System.err.println("Cannot add command " + commandName + ": First parameter must be Player.");
            return;
        }

        CommandParameterInfo[] parameters = new CommandParameterInfo[types.length - 1];

        for (int i = 0; i < types.length - 1; i++) {
            boolean mayNotFind = false;
            boolean fuzzySearch = false;
            boolean allMatch = false;

            for (int j = 0; j < annotations[i + 1].length; j++) {
                if (annotations[i + 1][j] instanceof PartialMatch) {
                    fuzzySearch = true;
                } else if (annotations[i + 1][j] instanceof NullIfNotFound) {
                    mayNotFind = true;
                } else if (annotations[i + 1][j] instanceof AllMatch) {
                    allMatch = true;
                }
            }

            if (!supportedTypes.contains(types[i])) {
                System.err.println("Cannot add command " + commandName + ": Parameter " + (i + 2) + " is of unsupported type " + types[i + 1].getName() + ".");
                return;
            }

            builtUsage += "<" + types[i + 1].getSimpleName().toLowerCase() + "> ";

            parameters[i] = new CommandParameterInfo(types[i + 1], mayNotFind, fuzzySearch, allMatch);
        }

        String usage = config.usage().isEmpty() ? builtUsage : config.usage();

        CommandInfo commandInfo = new CommandInfo(controller, method, commandName, usage, parameters);

        if (config.validator() != null) {
            Constructor[] ctors = config.validator().getConstructors();
            if (ctors.length == 0) {
                System.err.println("ERROR: Failed to add converter " + config.name() + ", must have a default constructor!");
            } else {
                try {
                    Object instance = ctors[0].newInstance();
                    if (instance instanceof CommandValidator) {
                        commandInfo.validator = (CommandValidator) instance;
                    } else {
                        System.err.println("ERROR: Failed to add converter " + config.name() + ", class must extend CommandValidator");

                    }
                } catch (Exception invocationTargetException) {
                }

            }
        }
        commands.put(commandInfo.name, commandInfo);
    }

    public void addController(CommandController controller) {
        for (Method method : controller.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Command.class)) {
                processMethod(controller, method, method.getAnnotation(Command.class));
            }
        }
    }

    public void addCmd(BaseCommand controller) {
        for (Method method : controller.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Command.class)) {
                processMethod(controller, method, method.getAnnotation(Command.class));
                break;
            }
        }
    }

    private Float parseAsFloat(Player player, String value) {
        try {
            return Float.valueOf(value);
        } catch (NumberFormatException e) {
            sendResponse(player, String.format("%s is not a valid float.", value));
            throw new AbortCommandException(e);
        }
    }

    private Integer parseAsInteger(Player player, String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            sendResponse(player, String.format("%s is not a valid integer.", value));
            throw new AbortCommandException(e);
        }
    }

    private Boolean parseAsBoolean(Player player, String value) {
        if ("true".equals(value) || "t".equals(value) || "on".equals(value)) {
            return true;
        } else if ("false".equals(value) || "f".equals(value) || "off".equals(value)) {
            return false;
        } else {
            Integer intValue = parseAsInteger(player, value);

            if (intValue == 1) {
                return false;
            } else if (intValue == 0) {
                return true;
            } else {
                sendResponse(player, String.format("Invalid toggle value %s. Must be either 0/1, t/f, true/false or on/off.", value));
                throw new AbortCommandException(null);
            }
        }
    }

    private Player parseAsPlayer(Player player, String value, CommandParameterInfo parameterInfo) {
        Player target;

        if (value.charAt(0) == '#' && !parameterInfo.allMatch) {
            target = server.getPlayer(parseAsInteger(player, value.substring(1)));
        } else {
            target = server.findPlayer(value);

            if (target == null && parameterInfo.fuzzySearch) {
                List<String> matches = new ArrayList<>();

                for (Player inst : server.getAllPlayers()) {
                    String name = inst.getName();

                    if (name.contains(value)) {
                        target = inst;
                        matches.add(name);
                    }
                }

                if (matches.size() > 1) {
                    sendResponse(player, String.format("More than 1 match for name '%s': '%s', '%s' and %d others.", value, matches.get(0), matches.get(1), matches.size() - 2));
                    throw new AbortCommandException(null);
                }
            } else if (target == null && parameterInfo.allMatch) {

                boolean isNumeric = value.chars().allMatch(Character::isDigit);

                if (isNumeric) {
                    target = server.getPlayer(Integer.valueOf(value));
                    if (target != null) {
                        return target;
                    }
                }

                for (Player inst : server.getAllPlayers()) {
                    String name = inst.getName();

                    if (name.toLowerCase().contains(value.toLowerCase())) {
                        return inst;
                    }
                }

            }
        }

        if (target == null && !parameterInfo.mayNotFind) {
            sendResponse(player, String.format("Found no matches for player '%s'.", value));
            throw new AbortCommandException(null);
        }

        return target;
    }

    private Vehicle parseAsVehicle(Player player, String value, CommandParameterInfo parameterInfo) {
        int vehicleId = parseAsInteger(player, value);
        Vehicle target = server.getVehicle(vehicleId);

        if (target == null && !parameterInfo.mayNotFind) {
            sendResponse(player, String.format("Vehicle %d does not exist.", vehicleId));
            throw new AbortCommandException(null);
        }

        return target;
    }

    private GameObject parseAsObject(Player player, String value, CommandParameterInfo parameterInfo) {
        int objectId = parseAsInteger(player, value);
        GameObject target = server.getObject(objectId);

        if (target == null && !parameterInfo.mayNotFind) {
            sendResponse(player, String.format("Object %d does not exist.", objectId));
            throw new AbortCommandException(null);
        }

        return target;
    }

    private Object parseParameter(Player player, CommandParameterInfo parameterInfo, String value) {
        if (parameterInfo.klass == Integer.class || parameterInfo.klass == int.class) {
            return parseAsInteger(player, value);
        } else if (parameterInfo.klass == Float.class || parameterInfo.klass == float.class) {
            return parseAsFloat(player, value);
        } else if (parameterInfo.klass == Boolean.class || parameterInfo.klass == boolean.class) {
            return parseAsBoolean(player, value);
        } else if (parameterInfo.klass == Player.class) {
            return parseAsPlayer(player, value, parameterInfo);
        } else if (parameterInfo.klass == Vehicle.class) {
            return parseAsVehicle(player, value, parameterInfo);
        } else if (parameterInfo.klass == GameObject.class) {
            return parseAsObject(player, value, parameterInfo);
        } else {
            return value;
        }
    }

    private boolean runCommand(Player player, CommandInfo command, String[] parameters) {
        if (command.parameters.length != parameters.length) {
            return false;
        }

        try {

            if (command.controller != null) {
                Object[] values = new Object[command.parameters.length + 1];
                values[0] = player;

                boolean isValid = command.validator == null;

                if (command.validator != null) {
                    isValid = command.validator.isValid(player);
                }

                if (isValid) {
                    for (int i = 0; i < command.parameters.length; i++) {
                        values[i + 1] = parseParameter(player, command.parameters[i], parameters[i]);
                    }
                    command.method.invoke(command.controller, values);
                }

            } else {
                Object[] values = new Object[command.parameters.length];

                boolean isValid = command.validator == null;

                if (command.validator != null) {
                    isValid = command.validator.isValid(player);
                }
                if (isValid) {

                    for (int i = 0; i < command.parameters.length; i++) {
                        values[i] = parseParameter(player, command.parameters[i], parameters[i]);
                    }

                    command.baseCommand.player = player;
                    if (command.preMethod != null) {
                        command.preMethod.invoke(command.baseCommand, values);
                    }
                    command.method.invoke(command.baseCommand, values);
                    if (command.postMethod != null) {
                        command.postMethod.invoke(command.baseCommand, values);
                    }
                }

            }

        } catch (AbortCommandException e) {
            return true;
        } catch (Exception e) {
            if (player != null) {
                sendResponse(player, "Something went wrong.");
            }
            if (command.baseCommand != null) {
                command.baseCommand.failedRun(e);
            }
            throw new RuntimeException(e);
        }

        return true;
    }

    private CommandInfo getCaseInsensitiveCommand(String cmd) {
        for (Entry<String, CommandInfo> e : commands.entrySet()) {
            if (e.getKey().equalsIgnoreCase(cmd)) {
                return e.getValue();
            }
        }
        return null;
    }

    public boolean processCommand(Player player, String message) {
        String[] parts = message.trim().split("\\s+", 2);
        CommandInfo command = commands.get(parts[0]);

        if (command == null) {
            if (this.caseInsensitive) {
                command = getCaseInsensitiveCommand(parts[0]);
                if (command == null) {
                    return false;
                }
            } else {
                return false;
            }

        }

        if (command.controller != null) {
            if (!command.controller.checkAccess(player)) {
                return false;
            }
        } else {
            if (!command.baseCommand.checkAccess()) {
                return false;
            }
        }

        String[] parameters = new String[0];

        if (parts.length > 1) {
            if (command.endsWithString()) {
                parameters = parts[1].split("\\s+", command.parameters.length);
            } else {
                parameters = parts[1].split("\\s+");
            }
        }

        if (!runCommand(player, command, parameters)) {
            if (player != null) {
                sendResponse(player, String.format(this.usagePrefix + "/%s %s", command.name, command.usage));
            }
        }

        return true;
    }

    /**
     * @return the usagePrefix
     */
    public String getUsagePrefix() {
        return usagePrefix;
    }

    /**
     * @param usagePrefix the usagePrefix to set
     */
    public void setUsagePrefix(String usagePrefix) {
        this.usagePrefix = usagePrefix;
    }

    /**
     * @return the colour
     */
    public Colour getPrefixColour() {
        return colour;
    }

    /**
     * @param colour the colour to set
     */
    public void setPrefixColour(Colour colour) {
        this.colour = colour;
    }

    /**
     * @return the caseInsensitive
     */
    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    /**
     * @param caseInsensitive the caseInsensitive to set
     */
    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }
}
