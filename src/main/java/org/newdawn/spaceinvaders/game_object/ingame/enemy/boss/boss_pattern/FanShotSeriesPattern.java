package org.newdawn.spaceinvaders.game_object.ingame.enemy.boss.boss_pattern;

import org.newdawn.spaceinvaders.fixed_point.FixedPointUtil;
import org.newdawn.spaceinvaders.game_object.ingame.bullet.EnemyBullet;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.boss.Boss;
import org.newdawn.spaceinvaders.loop.GameLoop;
import org.newdawn.spaceinvaders.loop.Loop;

public class FanShotSeriesPattern extends BossPattern {
    private boolean isExecuted = false;

    private static final long SHOOT_INTERVAL = FixedPointUtil.ZERO_2 + FixedPointUtil.ZERO_1;
    private long shootElapsed = 0;
    private int remainShootCount = 5;

    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public FanShotSeriesPattern() {
        super();
    }

    public FanShotSeriesPattern(Loop loop, Boss boss) {
        super(loop, boss);
    }

    @Override
    protected void process(long deltaTime) {
        super.process(deltaTime);

        if (!isExecuted) return;

        if (remainShootCount == 0){
            notifyPatternEnd();
            destroy();
            return;
        }
        else{
            if (shootElapsed < SHOOT_INTERVAL){
                shootElapsed += deltaTime;
                if (shootElapsed > SHOOT_INTERVAL){
                    shootElapsed = SHOOT_INTERVAL;
                }
            }
            else{
                shootBullets();
                remainShootCount--;
                shootElapsed = 0;
            }
        }
    }
    
    @Override
    public void executePattern() {
        System.out.println("FanShotSeriesPattern executed");
        isExecuted = true;
    }

    private void shootBullets() {
        EnemyBullet bullet;

        for (int i = 0; i < 9; i++){
            long shootPosOffsetX = FixedPointUtil.fromLong(-40 + 10 * i);
            long shootAngleOffset = FixedPointUtil.fromLong(-60 + 15 * i);

            bullet = new EnemyBullet((GameLoop)getLoop(), boss.getRotation() + shootAngleOffset, boss.getPosX() + shootPosOffsetX, boss.getPosY() + (63 << 16), 20l, FixedPointUtil.fromLong(350L));
            getLoop().addGameObject(bullet);
        }

    }
}
