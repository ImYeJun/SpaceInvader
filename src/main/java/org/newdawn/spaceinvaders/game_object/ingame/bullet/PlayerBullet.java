package org.newdawn.spaceinvaders.game_object.ingame.bullet;

import org.newdawn.spaceinvaders.game_object.collision.ICollider2DOwner;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.Enemy;
import org.newdawn.spaceinvaders.loop.GameLoop;

public class PlayerBullet extends Bullet{
    private static final String SPRITE_REF = "sprites/shot.gif";

    private int damage;
    public int getDamage() { return damage; }

    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public PlayerBullet() {
        super();
    }
    public PlayerBullet(int damage, GameLoop gameLoop, long spawnAngle, long spawnCentralX, long spawnCentralY, long spawnOffset, long spawnSpeed) {
        super(gameLoop, spawnAngle, spawnCentralX, spawnCentralY, spawnOffset, spawnSpeed, SPRITE_REF);
        this.damage = damage;
    }
    
    @Override
    public void collidedWith(ICollider2DOwner collider) {
        super.collidedWith(collider);

        // if we've hit an Enemy, kill it!
        if (collider instanceof Enemy) {
            Enemy enemy = (Enemy) collider;

            if(enemy.isDestroyed()) return;

            enemy.onHit(damage);
            destroy();

            used = true;
        }
    }
}
