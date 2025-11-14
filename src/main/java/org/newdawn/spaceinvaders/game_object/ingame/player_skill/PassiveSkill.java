package org.newdawn.spaceinvaders.game_object.ingame.player_skill;

import org.newdawn.spaceinvaders.enums.IndicatorTextType;
import org.newdawn.spaceinvaders.enums.PlayerPassiveSkillType;
import org.newdawn.spaceinvaders.game_object.ingame.player.PlayerShip;
import org.newdawn.spaceinvaders.loop.GameLoop;

public class PassiveSkill extends PlayerSkill{
    private PlayerPassiveSkillType type;
    public PlayerPassiveSkillType getType() { return type; }

    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public PassiveSkill(){
        super();
    }
    public PassiveSkill(PlayerPassiveSkillType type, GameLoop gameLoop) {
        super(type.getName(), type.getSpriteRef(), gameLoop);

        this.type = type;
    }

    @Override
    public boolean onAcquire(GameLoop gameLoop, PlayerShip playerShip) {
        if (playerShip.isPasiveSkillMaxLevel(type)) {
            gameLoop.showIndicatorText("'" + type.name() + "'" + " 패시브 스킬은 현재 최대 레벨 입니다.", IndicatorTextType.WARNING);
            return false;
        }
        playerShip.upgradePassiveSkill(type);
        this.playerShip = playerShip;
        gameLoop.notifySkillStoreItemAcquired();
        return true;
    }

    @Override
    public int getPrice(PlayerShip playerShip) {
        return playerShip.getPassiveSkillLevel(getType())*2 + 1;
    }

    @Override
    public String getPriceString(PlayerShip playerShip) {
        if (playerShip.isPasiveSkillMaxLevel(type)){
            return "?";
        }
        else{
            return Integer.toString(getPrice(playerShip));
        }
    }
}
