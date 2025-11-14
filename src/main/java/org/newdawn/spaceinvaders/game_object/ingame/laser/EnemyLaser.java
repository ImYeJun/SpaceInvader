package org.newdawn.spaceinvaders.game_object.ingame.laser;

import org.newdawn.spaceinvaders.fixed_point.FixedPointUtil;
import org.newdawn.spaceinvaders.game_object.collision.ICollider2DOwner;
import org.newdawn.spaceinvaders.loop.Loop;

public class EnemyLaser extends Laser {
    private final static String SPRITE_REF = "sprites/enemyLaser.png";
    private final static int DAMAGE = 1;
    private final static long LIFE_DURATION = FixedPointUtil.ZERO_5;
    public static int getDamage() { return DAMAGE; }

    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public EnemyLaser() {
    super();
    }

    public EnemyLaser(Loop loop, long spawnPosX, long spawnPosY, long spawnOffset, long spawnAngle) {
        super(loop, spawnPosX, spawnPosY, spawnOffset, spawnAngle, SPRITE_REF, DAMAGE, LIFE_DURATION);
    }

    @Override
    public void collidedWith(ICollider2DOwner collider) {
        //* playerShip와의 충돌시 playerShip에게 줄 데미지는 PlayerShip 내에서 처리 함
    }
}
