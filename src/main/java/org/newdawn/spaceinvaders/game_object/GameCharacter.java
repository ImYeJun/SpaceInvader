package org.newdawn.spaceinvaders.game_object;

import java.awt.Color;
import java.awt.Graphics2D;

import org.newdawn.spaceinvaders.fixed_point.FixedPointUtil;
import org.newdawn.spaceinvaders.game_object.collision.ICollider2DOwner;
import org.newdawn.spaceinvaders.game_object.visual.SpriteRenderer;
import org.newdawn.spaceinvaders.loop.Loop;

public abstract class GameCharacter extends Mover2D implements ICollider2DOwner{
    protected long maxHealth;
    protected long health;
    protected int healthBarWidth = 20;
    private final static int HEALTH_BAR_OFFSET_Y = 20;
    private final static int HEALTH_BAR_HEIGHT = 5;

    protected SpriteRenderer spriteRenderer;
    public SpriteRenderer getSpriteRenderer() { return spriteRenderer; }

    public void increaseHealth(long amount) { health += amount; }
    public void increaseHealth() { increaseHealth(1); }
    public void decreaseHealth(long amount) { 
        health = Math.max(0, health - amount);
    }
    public void decreaseHealth() { decreaseHealth(1); }
    public long getHealth() { return health; }

    public boolean isDead() { return health <= 0; }

    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public  GameCharacter(){
        super();
    }
    public GameCharacter(Loop loop, long intitalHealth){
        super(loop);

        maxHealth = intitalHealth;
        health = maxHealth;
    }
    public GameCharacter(Loop loop, long initialHealth, int healthBarWidth){
        this(loop, initialHealth);
        this.healthBarWidth = healthBarWidth;
    }

    @Override
    protected void draw(Graphics2D g) {
        super.draw(g);

        int healthBarPosX = FixedPointUtil.toInt(getPosX()) - healthBarWidth / 2;

        int healthBarPosY = FixedPointUtil.toInt(getPosY())
                + HEALTH_BAR_OFFSET_Y;

        g.setColor(Color.gray);
        g.fillRect(healthBarPosX, healthBarPosY, healthBarWidth, HEALTH_BAR_HEIGHT);

        g.setColor(Color.red);
        int fillHpBarWidth = (int)(healthBarWidth * ((double)health / maxHealth));
        g.fillRect(healthBarPosX, healthBarPosY, fillHpBarWidth, HEALTH_BAR_HEIGHT);
    }
}
