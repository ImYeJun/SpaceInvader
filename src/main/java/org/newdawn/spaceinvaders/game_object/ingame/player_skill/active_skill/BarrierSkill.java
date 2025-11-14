package org.newdawn.spaceinvaders.game_object.ingame.player_skill.active_skill;

import org.newdawn.spaceinvaders.fixed_point.FixedPointUtil;
import org.newdawn.spaceinvaders.game_object.ingame.player.Barrier;
import org.newdawn.spaceinvaders.game_object.ingame.player.PlayerShip;
import org.newdawn.spaceinvaders.loop.GameLoop;

public class BarrierSkill extends ActiveSkill{
    private static final String SKILL_SPRITE_REF = "sprites/testActiveSkill.png";
    private static final String SKILL_NAME = "Barrier Skill";
    private static final long COOL_TIME = 5 << 16;

    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public BarrierSkill(){
        super();
    }
    public BarrierSkill(GameLoop gameLoop) {
        super(SKILL_NAME, SKILL_SPRITE_REF, COOL_TIME, gameLoop);
    }

    @Override
    public void activate() {
        long spawnAngle = playerShip.getRotation();
        long spawnX = playerShip.getPosX();
        long spawnY = playerShip.getPosY();
        Barrier barrier = new Barrier(gameLoop, spawnX, spawnY, 30L<< 16, spawnAngle);
        gameLoop.addGameObject(barrier);
        System.out.println("배리어 소환~");
    }
}
