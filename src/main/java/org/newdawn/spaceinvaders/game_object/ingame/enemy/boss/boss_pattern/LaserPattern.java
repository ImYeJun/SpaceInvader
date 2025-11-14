/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package org.newdawn.spaceinvaders.game_object.ingame.enemy.boss.boss_pattern;

import org.newdawn.spaceinvaders.PositionAngleSet;
import org.newdawn.spaceinvaders.fixed_point.FixedPointUtil;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.SpawnSignal;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.boss.Boss;
import org.newdawn.spaceinvaders.game_object.ingame.laser.EnemyLaser;
import org.newdawn.spaceinvaders.game_object.ingame.player.PlayerShip;
import org.newdawn.spaceinvaders.loop.GameLoop;
import org.newdawn.spaceinvaders.loop.Loop;


public class LaserPattern extends BossPattern {
    private boolean isExecuted = false;
    
    private static final long WARNING_TIME = FixedPointUtil.ZERO_5 + (1 << 16);
    private long warnedElapsed;
    private static final long WARNER_SPAWN_INTERVAL = 100 << 16;

    private long spawnPosX;
    private long spawnPosY;
    private int spawnOffset;
    private long spawnAngle;

    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public LaserPattern() {
        super();
    }   

    public LaserPattern(Loop loop, Boss boss) {
        super(loop, boss);
    }

    @Override
    protected void process(long deltaTime) {
        super.process(deltaTime);

        if (!isExecuted) return;
        if (warnedElapsed < WARNING_TIME){
            warnedElapsed += deltaTime;

            if (warnedElapsed >= WARNING_TIME) { warnedElapsed = WARNING_TIME; }
        }
        else{
            shootLaser();
            destroy();
        }
    }

    
    @Override
    public void executePattern() {
        System.out.println("LaserPattern executed");
        isExecuted = true;

        PlayerShip target = ((GameLoop)getLoop()).getRandomAlivePlayerShip();

        if(target == null) return;
        
        spawnPosX = boss.getPosX();
        spawnPosY = boss.getPosY(); 
        spawnAngle = FixedPointUtil.atan2(target.getPosY() - boss.getPosY(), target.getPosX() - boss.getPosX());
        spawnOffset = 60;
        
        long currentWarnerSpawnPosX = spawnPosX + FixedPointUtil.mul(FixedPointUtil.cos(spawnAngle), WARNER_SPAWN_INTERVAL);
        long currentWarnerSpawnPosY = spawnPosY + FixedPointUtil.mul(FixedPointUtil.sin(spawnAngle), WARNER_SPAWN_INTERVAL);

        while (currentWarnerSpawnPosX > 0 && currentWarnerSpawnPosX < (800 << 16)
            && currentWarnerSpawnPosY > 0 && currentWarnerSpawnPosY < (600 << 16)) {
            PositionAngleSet positionAngleSet = new PositionAngleSet(currentWarnerSpawnPosX, currentWarnerSpawnPosY, spawnAngle + (270<< 16));
            SpawnSignal warner = new SpawnSignal((GameLoop)getLoop(), positionAngleSet, WARNING_TIME, SpawnSignal.ENEMY_SIGNAL);

            currentWarnerSpawnPosX += FixedPointUtil.mul(FixedPointUtil.cos(spawnAngle), WARNER_SPAWN_INTERVAL);
            currentWarnerSpawnPosY += FixedPointUtil.mul(FixedPointUtil.sin(spawnAngle), WARNER_SPAWN_INTERVAL);
            getLoop().addGameObject(warner);
        }
    }
    
    private void shootLaser() {
        spawnAngle += 90 << 16;
        EnemyLaser laser = new EnemyLaser(getLoop(), spawnPosX, spawnPosY, spawnOffset, spawnAngle);
        
        getLoop().addGameObject(laser);
        notifyPatternEnd();
    }
}
