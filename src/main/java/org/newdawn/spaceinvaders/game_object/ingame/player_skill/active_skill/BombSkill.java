package org.newdawn.spaceinvaders.game_object.ingame.player_skill.active_skill;

import org.newdawn.spaceinvaders.fixed_point.FixedPointUtil;
import org.newdawn.spaceinvaders.game_object.effect.BlankScreenEffect;
import org.newdawn.spaceinvaders.game_object.ingame.player.PlayerShip;
import org.newdawn.spaceinvaders.loop.GameLoop;
import org.newdawn.spaceinvaders.loop.game_loop.EventBombUsed;

public class BombSkill extends ActiveSkill {
    private static final String SKILL_SPRITE_REF = "sprites/testActiveSkill.png";
    private static final String SKILL_NAME = "Bomb Skill";
    private static final long COOL_TIME = 5 << 16;

    private static final long BOMB_EFFECT_DURATION = FixedPointUtil.ZERO_1;

    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public BombSkill(){
        super();
    }
    public BombSkill(GameLoop gameLoop) {
        super(SKILL_NAME, SKILL_SPRITE_REF, COOL_TIME, gameLoop);
    }

    @Override
    public void activate() {
        gameLoop.getEventBus().publish(new EventBombUsed());

        new BlankScreenEffect(gameLoop, BOMB_EFFECT_DURATION);
    }
}
