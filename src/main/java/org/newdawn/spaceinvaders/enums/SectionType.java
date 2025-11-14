package org.newdawn.spaceinvaders.enums;

public enum SectionType {
    // Enum constants with their associated string values
    NEW_WAVE("new-wave"),
    STORE("store");

    // Field to store the string value
    private final String value;

    // Constructor to initialize the string value
    SectionType(String value) {
        this.value = value;
    }

    // Public method to get the string value
    public String getValue() {
        return value;
    }
    
    // Convert string to enum (factory method)
    public static SectionType fromValue(String value) {
        for (SectionType type : values()) {
            
            if (type.value.equalsIgnoreCase(value)) { // 대소문자 구분 없음
                return type;
            }
        }
        throw new IllegalArgumentException(String.format("SectionCommandType에는 '%s'라는 Type이 없습니다.", value));
    }
}
