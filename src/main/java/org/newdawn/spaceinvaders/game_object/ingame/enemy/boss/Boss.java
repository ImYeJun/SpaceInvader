package org.newdawn.spaceinvaders.game_object.ingame.enemy.boss;

import java.awt.Color;
import java.awt.Graphics2D;

import org.newdawn.spaceinvaders.game_object.gui.TextRenderer;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.Enemy;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.boss.boss_pattern.BossPattern;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.boss.boss_pattern.FanShotSeriesPattern;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.boss.boss_pattern.LaserPattern;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.boss.boss_pattern.PlayerSlowPattern;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.boss.boss_pattern.SideBulletStreamPattern;
import org.newdawn.spaceinvaders.game_object.ingame.enemy.boss.boss_pattern.TroopDeploymentPattern;
import org.newdawn.spaceinvaders.loop.GameLoop;

public class Boss extends Enemy{
    private TextRenderer bossHealthText;

    private final long firstPatternStartDelay = 3 << 16;
    private long firstPatternStartElapsed = 0;
    private boolean hasFirstPatternStarted = false;

    private final long patternInterval = 1 << 16;
    private long patternElapsed = 0;
    private int currentPatternIndex = -1;
    private boolean isPatternEnded = true;
    private BossPattern currentPattern;

    //* 패턴 다 돌고나면, 처음 부터 다시 패턴 순회함.
    private static int[] patternSequence = {
        BossPattern.SIDE_BULLET_STREAM,
        BossPattern.FAN_SHOT_SERIES,
        BossPattern.PLAYER_SLOW,
        BossPattern.SIDE_BULLET_STREAM,
        BossPattern.FAN_SHOT_SERIES,
        BossPattern.PLAYER_SLOW,
        BossPattern.TROOP_DEPLOYMENT,
        BossPattern.SIDE_BULLET_STREAM,
        BossPattern.FAN_SHOT_SERIES,
        BossPattern.PLAYER_SLOW,
        BossPattern.SIDE_BULLET_STREAM,
        BossPattern.FAN_SHOT_SERIES,
        BossPattern.PLAYER_SLOW,
        BossPattern.TROOP_DEPLOYMENT,
        BossPattern.LASER
    };

    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public Boss(){
        super();
    }
    
    public Boss(GameLoop gameLoop){
        super(gameLoop, 500, 300);

        bossHealthText = new TextRenderer(getLoop(), "", 30, Color.red);
        bossHealthText.setPos(400 << 16, 0);
        bossHealthText.setAlignment(1);
        bossHealthText.setSortingLayer(100);
        gameLoop.addGameObject(bossHealthText);
    }

    @Override
    protected void addSprites() {
        frames.add("sprites/enemy/boss.png");
    }

    @Override
    protected void addHitSprites() {
        onHitFrames.add("sprites/enemy/bossOnHit.png");
    }

    @Override
    public void onBroadcast() {}
    
    @Override
    protected void draw(Graphics2D g){
        super.draw(g);

        bossHealthText.setText(Long.toString(health));
    }

    @Override
    protected void process(long deltaTime) {
        super.process(deltaTime);

        if (!hasFirstPatternStarted){
            firstPatternStartElapsed += deltaTime;
            if (firstPatternStartElapsed >= firstPatternStartDelay){
                hasFirstPatternStarted = true;
                firstPatternStartElapsed = firstPatternStartDelay;
            }
            return;
        }

        if (!isPatternEnded) return;
        if (patternElapsed < patternInterval){
            patternElapsed += deltaTime;
            if (patternElapsed > patternInterval){
                patternElapsed = patternInterval;
            }
        }
        else{
            isPatternEnded = false;
            patternElapsed = 0;

            executeNextPattern();
        }
    }
    
    private void executeNextPattern() {
        currentPatternIndex++;

        int currentPatternType = patternSequence[currentPatternIndex % patternSequence.length];
        currentPattern = null;

        switch (currentPatternType) {
            case BossPattern.SIDE_BULLET_STREAM:
                currentPattern = new SideBulletStreamPattern(getLoop(), this);
                break;
            case BossPattern.FAN_SHOT_SERIES:
                currentPattern = new FanShotSeriesPattern(getLoop(), this);
                break;
            case BossPattern.PLAYER_SLOW:
                currentPattern = new PlayerSlowPattern(getLoop(), this);
                break;
            case BossPattern.TROOP_DEPLOYMENT:
                currentPattern = new TroopDeploymentPattern(getLoop(), this);
                break;
            case BossPattern.LASER:
                currentPattern = new LaserPattern(getLoop(), this);
                break;
            default:
                throw new IllegalStateException("Unknown boss pattern type: " + currentPatternType);
        }
        
        getLoop().addGameObject(currentPattern);
        currentPattern.executePattern();
    }

    public void endCurrentPattern(){ isPatternEnded = true; }
}
