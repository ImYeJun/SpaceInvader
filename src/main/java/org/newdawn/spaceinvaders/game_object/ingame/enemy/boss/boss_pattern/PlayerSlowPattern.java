package org.newdawn.spaceinvaders.game_object.ingame.enemy.boss.boss_pattern;

import org.newdawn.spaceinvaders.fixed_point.FixedPointUtil;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.boss.Boss;
import org.newdawn.spaceinvaders.loop.GameLoop;
import org.newdawn.spaceinvaders.loop.Loop;

public class PlayerSlowPattern extends BossPattern {
    private static final long PLAYER_SLOW_DOWN_RATIO = FixedPointUtil.ZERO_8;
    private static final long PLAYER_SLOW_DOWN_TIME = 5 << 16;

    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public PlayerSlowPattern() {
        super();
    }
    
    public PlayerSlowPattern(Loop loop, Boss boss) {
        super(loop, boss);
    }

    @Override
    public void executePattern() {
        System.out.println("PlayerSlowPattern executed");

        GameLoop gameLoop = (GameLoop)getLoop();
        gameLoop.notifyPlayerShipsSlowDown(PLAYER_SLOW_DOWN_RATIO, PLAYER_SLOW_DOWN_TIME);
        
        notifyPatternEnd();
    }
}
