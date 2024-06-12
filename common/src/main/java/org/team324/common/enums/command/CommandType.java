package org.team324.common.enums.command;

/**
 * @author crystalZ
 * @date 2024/6/12
 */
public enum CommandType {

    USER("4"),

    FRIEND("3"),

    GROUP("2"),

    MESSAGE("1"),

    ;

    private String commandType;

    public String getCommandType() {
        return commandType;
    }

    CommandType(String commandType) {
        this.commandType = commandType;
    }

    public static CommandType getCommandType(String ordinal) {
        for (int i = 0; i < CommandType.values().length; i++) {
            if (CommandType.values()[i].getCommandType().equals(ordinal)) {
                return CommandType.values()[i];
            }
        }
        return null;
    }

}