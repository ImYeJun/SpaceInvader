package org.newdawn.spaceinvaders.game_object.ingame.player;

import org.newdawn.spaceinvaders.fixed_point.FixedPointUtil;
import org.newdawn.spaceinvaders.game_object.GameObject2D;
import org.newdawn.spaceinvaders.game_object.collision.Collider2D;
import org.newdawn.spaceinvaders.game_object.collision.ICollider2DOwner;
import org.newdawn.spaceinvaders.game_object.ingame.bullet.EnemyBullet;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.Enemy;
import org.newdawn.spaceinvaders.game_object.visual.SpriteRenderer;
import org.newdawn.spaceinvaders.loop.GameLoop;

public class EnemyDetectionZone extends GameObject2D implements ICollider2DOwner{
    private static final long DETECT_RANGE = 18 << 16;
    private static final int DETECT_SCORE = 100;

    private static final long DETECT_INTERVAL = FixedPointUtil.ZERO_1;
    private long detectElapsed = 0;
    private boolean hasDetected = false;

    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public EnemyDetectionZone(){
        super();
    }
    public EnemyDetectionZone(GameLoop loop, PlayerShip ship) {
        super(loop);

        Collider2D collider2d = new Collider2D(loop, this);

        SpriteRenderer spriteRenderer = ship.getSpriteRenderer();
        collider2d.boundsPosX = -spriteRenderer.getSpritePivotX() - DETECT_RANGE;
        collider2d.boundsPosY = -spriteRenderer.getSpritePivotY() - DETECT_RANGE;
        collider2d.boundsWidth = (((long)spriteRenderer.getSpriteWidth()) << 16) + FixedPointUtil.mul(DETECT_RANGE, 2 << 16);
        collider2d.boundsHeight = (((long)spriteRenderer.getSpriteHeight()) << 16) + FixedPointUtil.mul(DETECT_RANGE, 2 << 16);
        collider2d.setDrawBounds(true);

        addChild(collider2d);
    }

    @Override
    protected void process(long deltaTime) {
        super.process(deltaTime);

        if (hasDetected){
            if (detectElapsed >= DETECT_INTERVAL){
                hasDetected = false;
                detectElapsed = 0;
            }
            else{
                detectElapsed += deltaTime;
            }
        }
    }

    @Override
    public void collidedWith(ICollider2DOwner collider) {
        if (!hasDetected){
            if (collider instanceof Enemy || collider instanceof EnemyBullet){
                GameLoop gameLoop = (GameLoop)getLoop();
                gameLoop.increaseScore(DETECT_SCORE);
                hasDetected = true;
            }
        }
    }
    
}
