package org.newdawn.spaceinvaders.game_object.ingame.loot_item;

import org.newdawn.spaceinvaders.game_object.ingame.player.PlayerShip;
import org.newdawn.spaceinvaders.loop.GameLoop;
import org.newdawn.spaceinvaders.loop.Loop;

public class CoinItem extends LootItem {
    private static final String SPRITE_REF = "sprites/coin.png";
    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public CoinItem(){
        super();
    }
    public CoinItem(Loop loop) {
        super(loop, SPRITE_REF);
    }

    protected void onCollideWithPlayerShip(PlayerShip ship){
        if (getLoop() instanceof GameLoop){
            GameLoop gameLoop = (GameLoop)getLoop();
            gameLoop.increaseCoin();
        }
    }
}
