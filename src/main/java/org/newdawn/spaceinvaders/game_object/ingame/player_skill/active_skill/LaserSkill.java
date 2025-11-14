package org.newdawn.spaceinvaders.game_object.ingame.player_skill.active_skill;

import org.newdawn.spaceinvaders.game_object.ingame.laser.PlayerLaser;
import org.newdawn.spaceinvaders.loop.GameLoop;

public class LaserSkill extends ActiveSkill{
    private static final String SKILL_SPRITE_REF = "sprites/testActiveSkill.png";
    private static final String SKILL_NAME = "Laser Skill";    
    private static final long COOL_TIME = 5 << 16;

    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public LaserSkill(){
        super();
    }
    public LaserSkill(GameLoop gameLoop) {
        super(SKILL_NAME, SKILL_SPRITE_REF, COOL_TIME, gameLoop);
    }

    @Override
    public void activate() {
        long spawnPosX = playerShip.getPosX();
        long spawnPosY = playerShip.getPosY();
        long spawnAngle = playerShip.getRotation();
        PlayerLaser laser = new PlayerLaser(gameLoop, spawnPosX, spawnPosY, 20L, spawnAngle);
        gameLoop.addGameObject(laser);
    }
}
