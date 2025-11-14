package org.newdawn.spaceinvaders.game_object.ingame.laser;

import org.newdawn.spaceinvaders.fixed_point.FixedPointUtil;
import org.newdawn.spaceinvaders.game_object.collision.ICollider2DOwner;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.Enemy;
import org.newdawn.spaceinvaders.loop.Loop;

public class PlayerLaser extends Laser {
    private final static String SPRITE_REF = "sprites/testLaser.png";
    private final static int DAMAGE = 10;
    private final static long LIFE_DURATION = FixedPointUtil.ZERO_5;

    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public PlayerLaser() {
        super();
    }

    public PlayerLaser(Loop loop, long spawnPosX, long spawnPosY, long spawnOffset, long spawnAngle) {
        super(loop, spawnPosX, spawnPosY, spawnOffset, spawnAngle, SPRITE_REF, DAMAGE, LIFE_DURATION);
    }

    @Override
    public void collidedWith(ICollider2DOwner collider) {
        if (collider instanceof Enemy){
            Enemy enemy = (Enemy)collider;
            enemy.onHit(DAMAGE);
        }
    }
}
