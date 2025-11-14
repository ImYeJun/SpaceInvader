package org.newdawn.spaceinvaders.game_object.ingame.player_skill.active_skill;

import org.newdawn.spaceinvaders.game_object.ingame.player.PlayerShip;
import org.newdawn.spaceinvaders.loop.GameLoop;

public class BasicActiveSkill extends ActiveSkill{
    private static final String SKILL_SPRITE_REF = "sprites/testActiveSkill.png";
    private static final String SKILL_NAME = "Test Active Skill";
    private static final long COOL_TIME = 5 << 16;

    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public BasicActiveSkill(){
        super();
    }
    public BasicActiveSkill(GameLoop gameLoop) {
        super(SKILL_NAME, SKILL_SPRITE_REF, COOL_TIME, gameLoop);
    }
    
    @Override
    public void activate() {
        System.out.println(SKILL_NAME + " 발동");
    }
}
