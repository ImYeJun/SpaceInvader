package org.newdawn.spaceinvaders.game_object.ingame.enemy;

import org.newdawn.spaceinvaders.PositionAngleSet;
import org.newdawn.spaceinvaders.game_object.GameObject;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.boss.Boss;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.common.Alien;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.common.Artillery;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.common.Guardian;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.common.Raider;
import org.newdawn.spaceinvaders.game_object.logic.HiveMind;
import org.newdawn.spaceinvaders.loop.GameLoop;

//TODO 싱글톤으로 바꾸기
public class EnemyFactory extends GameObject{
    GameLoop gameLoop;

    static public final int AILEN = 0;
    static public final int ARTILLERY = 1;
    static public final int GUARDIAN = 2;
    static public final int RAIDER = 3;
    static public final int BOSS = 4;

    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public EnemyFactory(){
        super();
    }
    public EnemyFactory(GameLoop gameLoop) {
        super(gameLoop);

        this.gameLoop = gameLoop;
    }

    public Enemy spawnEnemy(HiveMind hiveMind, int enemyType, PositionAngleSet positionAngleSet) {
        return spawnEnemy(hiveMind, enemyType, positionAngleSet, true);
    }
    public Enemy spawnEnemy(HiveMind hiveMind, int enemyType, PositionAngleSet positionAngleSet, boolean isWarning) {
        Enemy enemy = null;
        
        switch (enemyType) {
            case AILEN:
                enemy = new Alien(gameLoop, hiveMind);
                break;
            case ARTILLERY:
                enemy = new Artillery(gameLoop, hiveMind);
                break;
            case GUARDIAN:
                enemy = new Guardian(gameLoop, hiveMind);
                break;
            case RAIDER:
                enemy = new Raider(gameLoop, hiveMind);
                break;
            case BOSS:
                enemy = new Boss(gameLoop);
                break;
            default:
                System.err.println(enemyType + "은 존재하지 않은 enemyType 입니다.");
                return null;
        }

        gameLoop.notifyEnemyInstantiated();

        hiveMind.addListener(enemy);
        
        if (isWarning){
            SpawnSignal enemyWarner = new SpawnSignal(enemy, gameLoop, positionAngleSet, SpawnSignal.ENEMY_SIGNAL);

            gameLoop.addGameObject(enemyWarner);
            return enemy;
        }
        
        enemy.setPosRotation(positionAngleSet.positionX, positionAngleSet.positionY, positionAngleSet.angle);
        gameLoop.addEnemy(enemy);
        return enemy;
    }
}

