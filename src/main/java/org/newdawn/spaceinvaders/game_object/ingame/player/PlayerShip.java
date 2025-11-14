package org.newdawn.spaceinvaders.game_object.ingame.player;

import java.util.HashMap;

import org.newdawn.spaceinvaders.enums.PlayerPassiveSkillType;
import org.newdawn.spaceinvaders.fixed_point.FixedPointUtil;
import org.newdawn.spaceinvaders.game_object.GameCharacter;
import org.newdawn.spaceinvaders.game_object.collision.Collider2D;
import org.newdawn.spaceinvaders.game_object.collision.ICollider2DOwner;
import org.newdawn.spaceinvaders.game_object.ingame.bullet.EnemyBullet;
import org.newdawn.spaceinvaders.game_object.ingame.bullet.PlayerBullet;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.Enemy;
import org.newdawn.spaceinvaders.game_object.ingame.laser.EnemyLaser;
import org.newdawn.spaceinvaders.game_object.ingame.player_skill.active_skill.ActiveSkill;
import org.newdawn.spaceinvaders.game_object.visual.SpriteRenderer;
import org.newdawn.spaceinvaders.loop.GameLoop;

public class PlayerShip extends GameCharacter{
    private int playerID;

    private int bulletDamage = 1;

    /** The speed at which the player's ship should move (pixels/sec) */
    private final long defaultMoveSpeed = 300L << 16;
    private long moveSpeed = defaultMoveSpeed;

    /** The time at which last fired a shot */
    private long lastFire = 0L;
    /** The interval between our players shot (s) */
    private final long defaultFiringIntreval = FixedPointUtil.ZERO_5;
    private long firingInterval = defaultFiringIntreval;

    private long activeSkillActivateElapsed = Long.MAX_VALUE;
    private boolean isActiveSkillActable = true;
    private ActiveSkill activeSkill = null;
    public void setActiveSkill(ActiveSkill activeSkill) { 
        this.activeSkill = activeSkill;
        isActiveSkillActable = true;
        activeSkillActivateElapsed = 0;
    }   
    public String getActiveSkillName() { 
        if (activeSkill != null) { return activeSkill.getName(); }
        return null;
    }
    public boolean isActiveSkillActable() { return isActiveSkillActable; }
    public Long getRemainCoolTime() {
        if (activeSkill == null || isActiveSkillActable) { return null; }
        return activeSkill.getCoolTime() - activeSkillActivateElapsed;
    }

    private int waveInitialShield = 0;
    private int currentShield = waveInitialShield; 
    public int getCurrentShield() { return currentShield; }

    private EnemyDetectionZone enemyDetectionZone;

    private HashMap<PlayerPassiveSkillType, Integer> passiveSkills = new HashMap<>(); // < PlayerPassiveSkillType, level >
    public int getPassiveSkillLevel(PlayerPassiveSkillType type) { return passiveSkills.get(type); }
    public boolean isPasiveSkillMaxLevel(PlayerPassiveSkillType type) { return passiveSkills.get(type) == type.getMaxLevel(); }
    public void upgradePassiveSkill(PlayerPassiveSkillType type) { upgradePassiveSkill(type, 1);}
    public void upgradePassiveSkill(PlayerPassiveSkillType type, int amount){
        if (!isPasiveSkillMaxLevel(type)){
            //* 증가된 레벨 값을 0와 type의 최대 레벨 사이로 clamp한다
            int newLevel = Math.max(0, Math.min(type.getMaxLevel(), passiveSkills.get(type) + amount));
            passiveSkills.put(type, newLevel);

            applyPassiveSkill();
        }
    } 
    private void applyPassiveSkill() {
        for (PlayerPassiveSkillType type : passiveSkills.keySet()) {
            if (type == PlayerPassiveSkillType.FIRE_SPEED){
                applyFireSpeedPassiveSkill();
            }
            if (type == PlayerPassiveSkillType.DAMAGE_UP){
                applayDamageUpPassiveSkill();
            }
            if (type == PlayerPassiveSkillType.ADDITIONAL_ENGINE){
                applyAdditionalPassiveSkill();
            }
            if (type == PlayerPassiveSkillType.REPAIR_KIT){
                applyRepairKitPassiveSkill();
            }
        }
    }
    private void applyFireSpeedPassiveSkill() {
        long fireSpeedMultiplier;
        switch (passiveSkills.get(PlayerPassiveSkillType.FIRE_SPEED)) {
                case 1:
                    fireSpeedMultiplier = (1 << 16) + FixedPointUtil.ZERO_3;
                    break;
                case 2:
                    fireSpeedMultiplier = (1 << 16) + FixedPointUtil.ZERO_8;
                    break;
                case 3:
                    fireSpeedMultiplier = (2 << 16) + FixedPointUtil.ZERO_5;
                    break;
                default:
                    fireSpeedMultiplier = 1 << 16;
                    break;
            }
            firingInterval = FixedPointUtil.div(defaultFiringIntreval, fireSpeedMultiplier);
        }
    private void applayDamageUpPassiveSkill() {
        int newBulletDamage = 1;
        switch (passiveSkills.get(PlayerPassiveSkillType.DAMAGE_UP)) {
            case 1:
                newBulletDamage = 2; 
                break;
            case 2:
                newBulletDamage = 3; 
                break;
            case 3:
                newBulletDamage = 4; 
                break;
            case 4:
                newBulletDamage = 5; 
                break;
            case 5:
                newBulletDamage = 6; 
                break;
            default:
                newBulletDamage = 1;
                break;
        }
        bulletDamage = newBulletDamage;
    }
    private void applyAdditionalPassiveSkill() {
        long moveSpeedMultiplier = 1 << 16;
        switch (passiveSkills.get(PlayerPassiveSkillType.ADDITIONAL_ENGINE)) {
            case 1:
                moveSpeedMultiplier = (1 << 16) + FixedPointUtil.ZERO_15;
                break;
            case 2:
                moveSpeedMultiplier = (1 << 16) + FixedPointUtil.ZERO_3;
                break;
            case 3:
                moveSpeedMultiplier = (1 << 16) + FixedPointUtil.ZERO_5;
                break;
            default:
                moveSpeedMultiplier = 1 << 16;
                break;
        }
        moveSpeed = FixedPointUtil.mul(defaultMoveSpeed, moveSpeedMultiplier);
    }
    private void applyRepairKitPassiveSkill() {
        int initialShield = 1;
        switch (passiveSkills.get(PlayerPassiveSkillType.REPAIR_KIT)) {
            case 1:
                initialShield = 1;
                break;
            case 2:
                initialShield = 1;
                break;
            case 3:
                initialShield = 2;
                break;
            default:
                initialShield = 1;
                break;
        }
        waveInitialShield = initialShield;
    }
    
    private Boolean isSpeedUp = false;
    private long speedUpRatio = 2 << 16 + FixedPointUtil.ZERO_5;
    private long speedUpTime = 4 << 16;
    private long speedUpElapsed = 0;
    public void notifySpeedUp(){
        isSpeedUp = true;
        speedUpElapsed = 0;
    }

    private Boolean isSlowDown = false;
    private long slowDownElapsed = 0;
    private long slowDownRatio;
    private long slowDownTime;
    public void notifySlowDown(long slowDownRatio, long slowDownTime){
        isSlowDown = true;
        slowDownElapsed = 0;

        this.slowDownRatio = slowDownRatio;
        this.slowDownTime = slowDownTime;
    }

    private long reflexibleElapsed = 0;
    private final long reflexibleTime = 2L << 16;
    private boolean isReflexible = false;;

    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public PlayerShip(){
        super();
    }
    public PlayerShip(GameLoop gameLoop, int playerID) {
        super(gameLoop, 3);

        this.playerID = playerID;

        for (PlayerPassiveSkillType type : PlayerPassiveSkillType.values()) {
            passiveSkills.put(type, 0);
        }

        spriteRenderer = new SpriteRenderer(gameLoop);
        spriteRenderer.setSpriteRef("sprites/ship.gif");
        addChild(spriteRenderer);

        Collider2D collider2D = new Collider2D(gameLoop, this);
        collider2D.boundsPosX = -spriteRenderer.getSpritePivotX();
        collider2D.boundsPosY = -spriteRenderer.getSpritePivotY();
        collider2D.boundsWidth = ((long)spriteRenderer.getSpriteWidth()) << 16;
        collider2D.boundsHeight = ((long)spriteRenderer.getSpriteHeight()) << 16;
        addChild(collider2D);

        enemyDetectionZone = new EnemyDetectionZone(gameLoop, this);
        addChild(enemyDetectionZone);
    }

    protected void process(long deltaTime) {
        super.process(deltaTime);

        GameLoop gameLoop = (GameLoop)getLoop();

        //region 이동
        velocityX = 0;
        if ((gameLoop.isKeyInputPressed(playerID, "left")) && (!gameLoop.isKeyInputPressed(playerID, "right"))) {
            velocityX = -moveSpeed;
        } else if ((gameLoop.isKeyInputPressed(playerID,"right")) && (!gameLoop.isKeyInputPressed(playerID,"left"))) {
            velocityX = moveSpeed;
        }

        velocityY = 0;
        if ((gameLoop.isKeyInputPressed(playerID,"up")) && (!gameLoop.isKeyInputPressed(playerID,"down"))) {
            velocityY = -moveSpeed;
        } else if ((gameLoop.isKeyInputPressed(playerID,"down")) && (!gameLoop.isKeyInputPressed(playerID,"up"))) {
            velocityY = moveSpeed;
        }

        //* 만약 SpeedUp된 상태면 PlayerShip의 이동 속도를 증가 시킴
        if (isSpeedUp){
            if (speedUpElapsed >= speedUpTime){
                isSpeedUp = false;
                speedUpElapsed = 0;
            }
            else{
                speedUpElapsed += deltaTime;
                velocityX = FixedPointUtil.mul(velocityX, speedUpRatio);
                velocityY = FixedPointUtil.mul(velocityY, speedUpRatio);
            }
        }

        //* 만약 SlowUp된 상태면 PlayerShip의 이동 속도를 감소 시킴
        if (isSlowDown){
            if (slowDownElapsed >= slowDownTime){
                isSlowDown = false;
                slowDownElapsed = 0;
            }
            else{
                slowDownElapsed += deltaTime;
                velocityX = FixedPointUtil.mul(velocityX, slowDownRatio);
                velocityY = FixedPointUtil.mul(velocityY, slowDownRatio);
            }
        }

        //endregion

        //region 마우스를 향해 회전
        long mousePosX = FixedPointUtil.fromLong(gameLoop.getMousePosX(playerID));
        long mousePosY = FixedPointUtil.fromLong(gameLoop.getMousePosY(playerID));

        long fromMeToMouseX = mousePosX - getPosX();
        long fromMeToMouseY = mousePosY - getPosY();

        setRotation(FixedPointUtil.atan2(fromMeToMouseY,  fromMeToMouseX) + (90 << 16));
        //endregion

        // 탄 발사
        if (gameLoop.isKeyInputPressed(playerID,"mouse_button_left")) {
            tryToFire();
        }
        if (gameLoop.isKeyInputJustPressed(playerID,"mouse_button_right")) {
            tryToDoActiveSkill(deltaTime);
        }

        //region 화면 밖으로 나가지 못하게 제약
        if ((velocityX <= 0L) && (getPosX() < (16 << 16) + FixedPointUtil.ZERO_5)) {
            setPosX((16 << 16) + FixedPointUtil.ZERO_5);
        }
        if ((velocityX >= 0L) && (getPosX() > ((800L << 16)) - (16 << 16) + FixedPointUtil.ZERO_5)) {
            setPosX(((800L << 16)) - (16 << 16) + FixedPointUtil.ZERO_5);
        }

        if ((velocityY <= 0L) && (getPosY() < (16 << 16) + FixedPointUtil.ZERO_5)) {
            setPosY((16 << 16) + FixedPointUtil.ZERO_5);
        }
        if ((velocityY >= 0L) && (getPosY() > ((600L << 16)) - (16 << 16) + FixedPointUtil.ZERO_5)) {
            setPosY(((600L << 16)) - (16 << 16) + FixedPointUtil.ZERO_5);
        }
        //endregion

        if (!isActiveSkillActable){
            if (activeSkillActivateElapsed >= activeSkill.getCoolTime()){
                isActiveSkillActable = true;
                activeSkillActivateElapsed = 0;
            }
            else{
                activeSkillActivateElapsed += deltaTime;
            }
        }

        if (isReflexible){
            if (reflexibleElapsed >= reflexibleTime){
                isReflexible = false;
                reflexibleElapsed = 0;
            }
            else{
                reflexibleElapsed += deltaTime;
            }
        }
    }

    private void tryToDoActiveSkill(long deltaTime) {
        if (activeSkill != null){
            if (isActiveSkillActable){
                activeSkill.activate();
                activeSkillActivateElapsed = -deltaTime; //* textUI 띄울 때 activeSkill.getCoolTime() 부터 보이도록 하는 용도로 -deltaTime을 할당
                isActiveSkillActable = false;
            }
        }
    }

    @Override
    public void collidedWith(ICollider2DOwner collider) {
        if (collider instanceof Enemy) {
            onHurt(collider);
        }
        else if (collider instanceof EnemyBullet){
            onHurt(collider);
            
            EnemyBullet enemyBullet = (EnemyBullet)collider;
            enemyBullet.onHitByPlayerShip();
        }
        else if (collider instanceof EnemyLaser){
            onHurt(collider);
        }
    }

    private void onHurt(ICollider2DOwner collider){
        //* Enemy Laser 맞으면 바로 죽음
        if (collider instanceof EnemyLaser){
            decreaseHealth(health);
            ((GameLoop)getLoop()).notifyPlayerShipDeath();
            destroy();
        }

        if (isReflexible && collider instanceof EnemyBullet){
            GameLoop gameLoop = (GameLoop)getLoop();
            EnemyBullet enemyBullet = (EnemyBullet)collider;

            PlayerBullet bullet = new PlayerBullet(
            bulletDamage,
            gameLoop, 
            enemyBullet.getRotation(), 
            getPosX(), 
            getPosY(), 
            -30,
            -enemyBullet.getSpawnSpeed()
            );

            gameLoop.addGameObject(bullet);

            return;
        }
        if (currentShield > 0){
            currentShield -= 1;
            return;
        }
        if (--health == 0){
            ((GameLoop)getLoop()).notifyPlayerShipDeath();
            destroy();
        }
    }

    public void onWaveStart(){
        currentShield = waveInitialShield;
    }

    public void notifyReflectionionEvent(){
        isReflexible = true;
        reflexibleElapsed = 0;
    }

    /**
     * Attempt to fire a shot from the player. Its called "try"
     * since we must first check that the player can fire at this
     * point, i.e. has he/she waited long enough between shots
     */
    public void tryToFire() {
        GameLoop gameLoop = (GameLoop)getLoop();
        // check that we have waiting long enough to fire
        if (gameLoop.getCurrentTime() - lastFire < firingInterval) {
            return;
        }

        // if we waited long enough, create the shot entity, and record the time.
        lastFire = gameLoop.getCurrentTime();

        PlayerBullet bullet = new PlayerBullet(
            bulletDamage,
            gameLoop, 
            getRotation(), 
            getPosX(), 
            getPosY(), 
            -30,
            FixedPointUtil.fromLong(-300)
            );

        gameLoop.addGameObject(bullet);
    }

}
