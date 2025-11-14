package org.newdawn.spaceinvaders.enums;

public enum SpecialCommandType {
    // Enum constants with their associated string values
    GAME_END("game-end");

    // Field to store the string value
    private final String value;

    // Constructor to initialize the string value
    SpecialCommandType(String value) {
        this.value = value;
    }

    // Public method to get the string value
    public String getValue() {
        return value;
    }
    
    // Convert string to enum (factory method)
    public static SpecialCommandType fromValue(String value) {
        for (SpecialCommandType type : values()) {
            if (type.value.equalsIgnoreCase(value)) { // 대소문자 구분 없음
                return type;
            }
        }
        throw new IllegalArgumentException(String.format("SpecialCommandType에는 '%s'라는 Type이 없습니다.", value));
    }
}
