package org.newdawn.spaceinvaders.enums;

import org.newdawn.spaceinvaders.sprite.SpriteRef;

public enum PlayerPassiveSkillType {
    FIRE_SPEED("FireSpeed", "sprites/testPassiveSkill.png", 3),
    DAMAGE_UP("DamageUp", "sprites/testPassiveSkill.png", 5),
    ADDITIONAL_ENGINE("AdditionalEngine", "sprites/testPassiveSkill.png", 3),
    REPAIR_KIT("RepairKit", "sprites/testPassiveSkill.png", 3);

    private final String name;
    private final String spriteRef;
    private final int maxLevel;
    
    PlayerPassiveSkillType(String name, String spriteRef, int maxLevel) {
        this.name = name;
        this.spriteRef = spriteRef;
        this.maxLevel = maxLevel;
    }
    
    public String getName() { return name; }
    public String getSpriteRef() { return spriteRef; }
    public int getMaxLevel() { return maxLevel; }
}