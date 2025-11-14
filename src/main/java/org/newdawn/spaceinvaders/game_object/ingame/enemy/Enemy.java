package org.newdawn.spaceinvaders.game_object.ingame.enemy;

import java.util.ArrayList;

import event_bus.IEventBusSubscriber;
import org.newdawn.spaceinvaders.fixed_point.FixedPointUtil;
import org.newdawn.spaceinvaders.game_object.GameCharacter;
import org.newdawn.spaceinvaders.game_object.collision.Collider2D;
import org.newdawn.spaceinvaders.game_object.collision.ICollider2DOwner;
import org.newdawn.spaceinvaders.game_object.ingame.loot_item.LootItem;
import org.newdawn.spaceinvaders.game_object.ingame.player.PlayerShip;
import org.newdawn.spaceinvaders.game_object.logic.HiveMind;
import org.newdawn.spaceinvaders.game_object.logic.IHiveMindListener;
import org.newdawn.spaceinvaders.game_object.visual.SpriteRenderer;
import org.newdawn.spaceinvaders.loop.GameLoop;
import org.newdawn.spaceinvaders.loop.Loop;
import org.newdawn.spaceinvaders.loop.game_loop.EventBombUsed;
import org.newdawn.spaceinvaders.singleton.LootItemFactory;

public abstract class Enemy extends GameCharacter implements IHiveMindListener, IEventBusSubscriber {
    protected HiveMind hiveMind;

    //* 이미지 등록시 frames와 onHitFrames의 원소 갯수는 같아야 한다.
    protected ArrayList<String> frames = new ArrayList<>();
    protected ArrayList<String> onHitFrames = new ArrayList<>();
    protected long lastFrameChange = 0L;
    protected long frameDuration = FixedPointUtil.ZERO_25;
    /** The current frame of animation being displayed */
    protected int frameNumber;
    
    protected Boolean isSlowDown = false; // 현재 속도가 줄어든 상태가 지속 되는 시간인지에 대한 변수
    protected Boolean hasSlowDown = false; // 중복되어 속도가 줄어드는 것을 막기 위한 변수
    protected long slowDownRatio = FixedPointUtil.ZERO_5;
    protected long slowDownTime = 3 << 16;
    protected long slowDownElapsed = 0;

    protected Boolean isHitAnimation = false;
    protected final static long HIT_ANIMATION_DURATION = FixedPointUtil.ZERO_1;
    protected long hitAnimationElapsed = 0;

    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public Enemy(){
        super();
    }
    public Enemy(GameLoop gameLoop, long initialHealth){
        super(gameLoop, initialHealth);

        spriteRenderer = new SpriteRenderer(gameLoop);
        addSprites();
        addHitSprites();
        
        //* 자식 클래스에서 addSprites()을 구현 할 때, 적어도 1개 이상의 sprite를 frames에 삽입 했음을 전제로 한다.
        try{
            spriteRenderer.setSpriteRef(frames.get(0));
            addChild(spriteRenderer);
        }
        catch (IndexOutOfBoundsException exception){
            System.out.println("addSprites() 구현시 하나 이상의 sprite를 삽입하지 않았습니다.");
        }
        
        setCollider(gameLoop);

        gameLoop.getEventBus().register(EventBombUsed.class, this);
    }

    public Enemy(GameLoop gameLoop, HiveMind hiveMind, long initialHealth){
        this(gameLoop, initialHealth);

        this.hiveMind = hiveMind;
        this.hiveMind.addListener(this);
    }
    
    public Enemy(GameLoop gameLoop, long initialHealth, int healthBarWidth){
        this(gameLoop,  initialHealth);

        super.healthBarWidth = healthBarWidth;
    }
    public Enemy(GameLoop gameLoop, HiveMind hiveMind, long initialHealth, int healthBarWidth){
        this(gameLoop, hiveMind, initialHealth);

        super.healthBarWidth = healthBarWidth;
    }

    protected void setCollider(Loop loop){
        Collider2D collider2D = new Collider2D(loop, this);
        collider2D.boundsPosX = -spriteRenderer.getSpritePivotX();
        collider2D.boundsPosY = -spriteRenderer.getSpritePivotY();
        collider2D.boundsWidth = ((long)spriteRenderer.getSpriteWidth()) << 16;
        collider2D.boundsHeight = ((long)spriteRenderer.getSpriteHeight()) << 16;
        addChild(collider2D);
    }

    @Override
    protected void process(long deltaTime) {
        super.process(deltaTime);

        // since the move tells us how much time has passed
        // by we can use it to drive the animation, however
        // its the not the prettiest solution
        lastFrameChange += deltaTime;

        // if we need to change the frame, update the frame number
        // and flip over the sprite in use
        if (lastFrameChange > frameDuration) {
            // reset our frame change time counter
            lastFrameChange = 0;

            // update the frame
            frameNumber++;
            if (frameNumber >= frames.size()) {
                frameNumber = 0;
            }
            
            if (isHitAnimation){ spriteRenderer.setSpriteRef(onHitFrames.get(frameNumber)); }
            else{ spriteRenderer.setSpriteRef(frames.get(frameNumber)); }
        }

        if (isSlowDown){
            if (slowDownElapsed >= slowDownTime){
                onSlowDownEffectEnd();
            }
            else{
                slowDownElapsed += deltaTime;
            }
        }

        if (isHitAnimation){
            if (hitAnimationElapsed >= HIT_ANIMATION_DURATION){
                isHitAnimation = false;
                hitAnimationElapsed = 0;
            }
            else{
                hitAnimationElapsed += deltaTime;
            }
        }
    }

    @Override
    public void decreaseHealth(long amount) {
        super.decreaseHealth(amount);
        
        if (isDead()){
            destroy();

            ((GameLoop)getLoop()).notifyAlienKilled();// GameLoop에 부고소식 전달
            
            LootItem item = LootItemFactory.getInstance().instantiateRandomItem((GameLoop)getLoop());
            
            if (item != null){
                item.setPos(getPosX(), getPosY());
            }
        }
    }

    public void onHit(int damage){
        if(isDestroyed()) return;
        
        decreaseHealth(damage);

        isHitAnimation = true;
        hitAnimationElapsed = 0;

        spriteRenderer.setSpriteRef(onHitFrames.get(frameNumber));
    }

    @Override
    public void collidedWith(ICollider2DOwner collider){
        if (collider instanceof PlayerShip){
            collideWithPlayerShip();
        }
    }

    @Override
    public void notify(Object event){
        if (event instanceof EventBombUsed){
            decreaseHealth((GameLoop.BOMB_DAMAGE));
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

        ((GameLoop)getLoop()).getEventBus().unregister(EventBombUsed.class, this);
    }

    protected void collideWithPlayerShip(){
        if (--health <= 0){
            destroy();
        }
    }
    
    /**
     * Enemy 객체가 가질 {@code Sprite}를 등록하는 메서드이다.
     *
     * <p>등록은 {@code frames.add()}를 통해 수행하며,
     * 동일 메서드가 2회 이상 호출될 경우 애니메이션으로 실행된다.
     * 각 프레임의 재생 시간은 {@code frameDuration}에 의해 결정된다.</p>
     *
     * <p><b>주의:</b> 구현 시 반드시 하나 이상의 {@code Sprite}를
     * {@code frames}에 삽입해야 한다.</p>
    */
    protected abstract void addSprites();
    /**
     * Enemy 객체의 타격 효과를 표시할 {@code Sprite}를 등록하는 메서드이다.
     *
     * <p>등록은 {@code onHitFreames.add()}를 통해 수행하며,
     * 동일 메서드가 2회 이상 호출될 경우 애니메이션으로 실행된다.
     * 각 프레임의 재생 시간은 {@code frameDuration}에 의해 결정된다.</p>
     *
     * <p><b>주의:</b> 구현 시 반드시 하나 이상의 {@code Sprite}를
     * {@code onHitFreames}에 삽입해야 한다.</p>
    */
    protected abstract void addHitSprites();


    /**
     * FrozenItem의 지속시간이 끝났을 때, 원래 속도로 복구하는 로직을 구현한다
     */
    protected void onSlowDownEffectEnd(){
        isSlowDown = false;
        hasSlowDown = false;
        slowDownElapsed = 0;

        if (velocityX != 0) { velocityX = FixedPointUtil.div(velocityX, slowDownRatio); }
        if (velocityY != 0) { velocityY = FixedPointUtil.div(velocityY, slowDownRatio); }
    }

    public void requestSlowDown(){
        isSlowDown = true;
        slowDownElapsed = 0;

        if (!hasSlowDown){
            velocityX = FixedPointUtil.mul(velocityX, slowDownRatio);
            velocityY = FixedPointUtil.mul(velocityY, slowDownRatio);

            hasSlowDown = true;
        }
    }
    
    // @Override
    // public void onDestroy() {
    //     super.onDestroy();
    //     ((GameLoop)getLoop()).notifyAlienKilled();// GameLoop에 부고소식 전달
    // }
}
