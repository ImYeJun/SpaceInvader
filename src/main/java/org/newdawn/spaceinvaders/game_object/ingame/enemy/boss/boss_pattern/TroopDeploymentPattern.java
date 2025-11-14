package org.newdawn.spaceinvaders.game_object.ingame.enemy.boss.boss_pattern;

import org.newdawn.spaceinvaders.PositionAngleSet;
import org.newdawn.spaceinvaders.fixed_point.FixedPointUtil;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.EnemyFactory;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.boss.Boss;
import org.newdawn.spaceinvaders.game_object.logic.HiveMind;
import org.newdawn.spaceinvaders.loop.GameLoop;
import org.newdawn.spaceinvaders.loop.Loop;

public class TroopDeploymentPattern extends BossPattern {
    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public TroopDeploymentPattern() {
        super();
    }

    public TroopDeploymentPattern(Loop loop, Boss boss) {
        super(loop, boss);
    }

    @Override
    public void executePattern() {
        System.out.println("TroopDeploymentPattern executed");
        GameLoop gameLoop = (GameLoop)getLoop();

        EnemyFactory enemyFactory = gameLoop.getEnemyFactory();
        HiveMind enemyHiveMind = gameLoop.getEnemyHiveMind();

        int spawnCount = 7; //* 갯수가 홀수임을 가정함
        long spawnPosXInterval = 100l << 16;
        long startSpawnPosX = FixedPointUtil.sub(boss.getPosX(), FixedPointUtil.mul(spawnPosXInterval, (spawnCount / 2) << 16));
        long raiderSpawnPosY = boss.getPosY() + (50l << 16);
        long guardianSpawnPosY = boss.getPosY() + (100l << 16);

        for (int i = 0;i < spawnCount; i++){
            long spawnPosX = startSpawnPosX + FixedPointUtil.mul(spawnPosXInterval, i << 16);
            if (i % 2 == 0){ 
                PositionAngleSet positionAngleSet = new PositionAngleSet(spawnPosX, raiderSpawnPosY);
                enemyFactory.spawnEnemy(enemyHiveMind, EnemyFactory.RAIDER, positionAngleSet);
            }
            else{
                PositionAngleSet positionAngleSet = new PositionAngleSet(spawnPosX, guardianSpawnPosY);
                enemyFactory.spawnEnemy(enemyHiveMind, EnemyFactory.GUARDIAN, positionAngleSet);
            }
        }

        notifyPatternEnd();
    }
}
