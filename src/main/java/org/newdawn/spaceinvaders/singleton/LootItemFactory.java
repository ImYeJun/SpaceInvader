
package org.newdawn.spaceinvaders.singleton;

import java.util.HashMap;

import org.newdawn.spaceinvaders.fixed_point.FixedPointUtil;
import org.newdawn.spaceinvaders.game_object.ingame.loot_item.CoinItem;
import org.newdawn.spaceinvaders.game_object.ingame.loot_item.LootItem;
import org.newdawn.spaceinvaders.game_object.ingame.loot_item.ScoringItem;
import org.newdawn.spaceinvaders.loop.GameLoop;

public class LootItemFactory {
    private final static LootItemFactory INSTANCE = new LootItemFactory();
    public static LootItemFactory getInstance()
    {
        return INSTANCE;
    }

    //TODO pseudo random으로 선택하게 만들기
    //TODO Object Pulling을 만들기 -> Loop의 GameObject에 넣어놓기
    private final HashMap<String, Long> _itemWeights = new HashMap<>(); //* <아이템 이름, 가중치
    public LootItemFactory() {
        _itemWeights.put("none", 60L);
        // _itemWeights.put("shield", 10L);
        // _itemWeights.put("battery", 10L);
        _itemWeights.put("coin", 35L);
        _itemWeights.put("scoring", 5L);
        // _itemWeights.put("frozen", 10L);
    }

    public LootItem instantiateRandomItem(GameLoop gameLoop){
        long totalWeight = 0L;
        LootItem lootItem = null;

        for (String key : _itemWeights.keySet()){
            totalWeight += _itemWeights.get(key);
        }


        long random = FixedPointUtil.toInt(
                // totalWeight가 고정 소수점이 아니라 정수라서 걍 곱해도 됨
                // bound 매개변수는 exclusive하므로 1을 더함
                gameLoop.getRandom().nextLong(FixedPointUtil.ONE + 1L) * totalWeight
        );

        for (String key : _itemWeights.keySet()){
            random -= _itemWeights.get(key);
            
            //TODO 생성된 아이템을 Loop의 GameObject 배열에 넣기
            if (random <= 0){
                switch (key) {
                    case "coin":
                        lootItem = new CoinItem(gameLoop);
                        break;
                    case "scoring":
                        lootItem = new ScoringItem(gameLoop);
                        break;
                    default: // 키 값이 none인 경우이다.
                        // System.out.println("LootItem 소환 안됨");
                        break;
                }

                return lootItem;
            }
        }

        throw new RuntimeException("가중치 랜덤 알고리즘에 문제 존재");
    }
}
