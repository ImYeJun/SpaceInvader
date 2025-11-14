package org.newdawn.spaceinvaders.game_object.ingame.player_skill;

import org.newdawn.spaceinvaders.game_object.ingame.player.PlayerShip;
import org.newdawn.spaceinvaders.game_object.ingame.store.IStoreItem;
import org.newdawn.spaceinvaders.loop.GameLoop;



public abstract class PlayerSkill implements IStoreItem {
    protected String skillNameField;
    protected String skillSpriteRefField;
    protected GameLoop gameLoop;

    protected PlayerShip playerShip;

    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public  PlayerSkill(){
        super();
    }
    public PlayerSkill(String skillName, String skillSpriteRef, GameLoop gameLoop) {
        this.skillNameField = skillName;
        this.skillSpriteRefField = skillSpriteRef;
        this.gameLoop = gameLoop;
    }
    
    @Override
    public String getName() {
        return skillNameField;
    }

    @Override
    public String getSpriteRef() {
        return skillSpriteRefField;
    }
}
