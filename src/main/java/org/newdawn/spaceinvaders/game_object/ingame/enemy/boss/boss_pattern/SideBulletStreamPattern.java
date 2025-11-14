package org.newdawn.spaceinvaders.game_object.ingame.enemy.boss.boss_pattern;

import org.newdawn.spaceinvaders.fixed_point.FixedPointUtil;
import org.newdawn.spaceinvaders.game_object.ingame.bullet.EnemyBullet;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.boss.Boss;
import org.newdawn.spaceinvaders.game_object.ingame.player.PlayerShip;
import org.newdawn.spaceinvaders.loop.GameLoop;
import org.newdawn.spaceinvaders.loop.Loop;

public class SideBulletStreamPattern extends BossPattern {
    private boolean isExecuted = false ;

    private static final long SHOOT_INTERVAL = FixedPointUtil.ZERO_2 + FixedPointUtil.ZERO_5;
    private long shootElapsed = 0;
    private int remainShootCount = 10;

    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public SideBulletStreamPattern() {
        super();
    }

    public SideBulletStreamPattern(Loop loop, Boss boss) {
        super(loop, boss);
    }

    @Override
    protected void process(long deltaTime) {
        super.process(deltaTime);

        if (!isExecuted) return;

        if (remainShootCount == 0) { 
            notifyPatternEnd();
            destroy();
            return;
        }
        if (shootElapsed < SHOOT_INTERVAL){
            shootElapsed += deltaTime;

            if (shootElapsed >= SHOOT_INTERVAL) { shootElapsed = SHOOT_INTERVAL; }
        }
        else{
            shootBullet();

            remainShootCount--;
            shootElapsed = 0;
        }
    }

    private void shootBullet() {
        PlayerShip target = ((GameLoop)getLoop()).getRandomAlivePlayerShip();

        if(target == null) return;

        long leftBulletSpawnPosX = FixedPointUtil.sub(boss.getPosX(), 250 << 16);
        long rightBulletSpawnPosX = FixedPointUtil.add(boss.getPosX(), 250 << 16);
        long bulletSpawnPosY = FixedPointUtil.add(boss.getPosY(), 53 << 16);

        long leftBulletSpawnAngle = FixedPointUtil.atan2(
            FixedPointUtil.sub(bulletSpawnPosY, target.getPosY()),
            FixedPointUtil.sub(leftBulletSpawnPosX, target.getPosX())
        ) + (90 << 16);

        long rightBulletSpawnAngle = FixedPointUtil.atan2(
            FixedPointUtil.sub(bulletSpawnPosY, target.getPosY()),
            FixedPointUtil.sub(rightBulletSpawnPosX, target.getPosX())
        ) + (90 << 16);

        EnemyBullet leftBullet = new EnemyBullet((GameLoop)getLoop(), leftBulletSpawnAngle, leftBulletSpawnPosX, bulletSpawnPosY, 0, 300l << 16);
        EnemyBullet rightBullet = new EnemyBullet((GameLoop)getLoop(), rightBulletSpawnAngle, rightBulletSpawnPosX, bulletSpawnPosY, 0, 300l << 16);

        getLoop().addGameObject(leftBullet);
        getLoop().addGameObject(rightBullet);
    }

    @Override
    public void executePattern() {
        System.out.println("SideBulletStreamPattern executed");
        isExecuted = true;
    }
}
