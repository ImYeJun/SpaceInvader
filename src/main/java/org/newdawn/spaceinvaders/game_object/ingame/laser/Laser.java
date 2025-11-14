package org.newdawn.spaceinvaders.game_object.ingame.laser;

import org.newdawn.spaceinvaders.fixed_point.FixedPointUtil;
import org.newdawn.spaceinvaders.game_object.GameObject2D;
import org.newdawn.spaceinvaders.game_object.collision.Collider2D;
import org.newdawn.spaceinvaders.game_object.collision.ICollider2DOwner;
import org.newdawn.spaceinvaders.game_object.visual.SpriteRenderer;
import org.newdawn.spaceinvaders.loop.Loop;

public abstract class Laser extends GameObject2D implements ICollider2DOwner{
    private long lifeDuration;
    private long spawnElapsed = 0;
    private SpriteRenderer spriteRenderer;

    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public Laser(){
        super();
    }
    public Laser(Loop loop, long spawnPosX, long spawnPosY, long spawnOffset, long spawnAngle, String spriteRef, int damage, long lifeDuration) {
        super(loop);

        this.lifeDuration = lifeDuration;

        spriteRenderer = new SpriteRenderer(loop);
        spriteRenderer.setSpriteRef(spriteRef);

        Collider2D collider2d = new Collider2D(loop, this);
        collider2d.boundsPosX = -spriteRenderer.getSpritePivotX();
        collider2d.boundsPosY = -spriteRenderer.getSpritePivotY();
        collider2d.boundsWidth = ((long)spriteRenderer.getSpriteWidth()) << 16;
        collider2d.boundsHeight = ((long)spriteRenderer.getSpriteHeight()) << 16;
        
        setRotation(spawnAngle);
        
        spawnAngle -= 90 << 16;
        spawnPosX += FixedPointUtil.mul(FixedPointUtil.cos(spawnAngle), FixedPointUtil.fromLong(512));
        spawnPosY += FixedPointUtil.mul(FixedPointUtil.sin(spawnAngle), FixedPointUtil.fromLong(512));
        spawnPosX += FixedPointUtil.mul(FixedPointUtil.cos(spawnAngle), FixedPointUtil.fromLong(spawnOffset));
        spawnPosY += FixedPointUtil.mul(FixedPointUtil.sin(spawnAngle), FixedPointUtil.fromLong(spawnOffset));
        setPos(spawnPosX, spawnPosY);
        
        addChild(spriteRenderer);
        addChild(collider2d);
    }

    @Override
    protected void process(long deltaTime) {
        super.process(deltaTime);
        if (isDestroyed()) { return; }

        if (spawnElapsed >= lifeDuration){
            destroy();
        }
        else{
            spawnElapsed += deltaTime;
        }
    }

    
}   
