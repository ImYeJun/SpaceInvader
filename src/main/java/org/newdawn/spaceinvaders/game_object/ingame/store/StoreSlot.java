package org.newdawn.spaceinvaders.game_object.ingame.store;

import java.awt.Color;
import java.awt.Graphics2D;

import event_bus.IEventBusSubscriber;
import org.newdawn.spaceinvaders.enums.IndicatorTextType;
import org.newdawn.spaceinvaders.enums.PlayerPassiveSkillType;
import org.newdawn.spaceinvaders.fixed_point.FixedPointUtil;
import org.newdawn.spaceinvaders.game_object.GameObject;
import org.newdawn.spaceinvaders.game_object.GameObject2D;
import org.newdawn.spaceinvaders.game_object.collision.Collider2D;
import org.newdawn.spaceinvaders.game_object.collision.ICollider2DOwner;
import org.newdawn.spaceinvaders.game_object.gui.TextRenderer;
import org.newdawn.spaceinvaders.game_object.ingame.player.PlayerShip;
import org.newdawn.spaceinvaders.game_object.ingame.player_skill.PassiveSkill;
import org.newdawn.spaceinvaders.game_object.visual.SpriteRenderer;
import org.newdawn.spaceinvaders.loop.GameLoop;
import org.newdawn.spaceinvaders.loop.Loop;
import org.newdawn.spaceinvaders.loop.game_loop.EventStoreSectionEnded;

public class StoreSlot extends GameObject2D implements ICollider2DOwner, IEventBusSubscriber {
    private IStoreItem item;
    private GameLoop gameLoop;
    private SpriteRenderer spriteRenderer;
    private TextRenderer itemNameText;
    private TextRenderer priceText;
    
    public IStoreItem getItem() { return item; }
    
    // Kryo 역직렬화를 위한 매개변수 없는 생성자
    public StoreSlot(){
        super();
    }
    public StoreSlot(GameLoop loop, IStoreItem item, long spawnX, long spawnY) {
        super(loop);
        this.item = item;
        this.gameLoop = loop;

        spriteRenderer = new SpriteRenderer(loop);
        spriteRenderer.setSpriteRef(item.getSpriteRef());

        long spriteHalfHeight = FixedPointUtil.div(spriteRenderer.getSpriteHeight() << 16, 2 << 16);
        itemNameText = new TextRenderer(loop, item.getName(), 10);
        itemNameText.alignment = 1;
        itemNameText.setPos(getPosX(), getPosY() + spriteHalfHeight);
        itemNameText.setFontStyle(1);

        priceText = new TextRenderer(loop, "가격", 10, Color.yellow);
        itemNameText.alignment = 1;
        priceText.setPos(getPosX(), getPosY() + spriteHalfHeight + (13 << 16));

        addChild(spriteRenderer);
        addChild(itemNameText);
        addChild(priceText);

        Collider2D collider2D = new Collider2D(loop, this);
        collider2D.boundsPosX = -spriteRenderer.getSpritePivotX();
        collider2D.boundsPosY = -spriteRenderer.getSpritePivotY();
        collider2D.boundsWidth = ((long)spriteRenderer.getSpriteWidth()) << 16;
        collider2D.boundsHeight = ((long)spriteRenderer.getSpriteHeight()) << 16;
        addChild(collider2D);

        setPosX(spawnX);
        setPosY(spawnY);

        setSortingLayer(-100);
        spriteRenderer.setSortingLayer(-100);
        itemNameText.setSortingLayer(-100);
        priceText.setSortingLayer(-100);
        collider2D.setSortingLayer(-100);

        gameLoop.getEventBus().register(EventStoreSectionEnded.class, this);
    }

    @Override
    protected void draw(Graphics2D g) {
        priceText.setText(getItem().getPriceString(gameLoop.getMyPlayerShip()));
        super.draw(g);
    }

    @Override
    public void collidedWith(ICollider2DOwner collider) {
        if (collider instanceof PlayerShip){
            PlayerShip playerShip = (PlayerShip) collider;

            if(gameLoop.decreaseCoin(getItem().getPrice(playerShip))){
                if(item.onAcquire(gameLoop, playerShip)){
                    destroy();
                }
                else{
                    gameLoop.increaseCoin(getItem().getPrice(playerShip)); //* IStoreItem의 내부 구매 조건이 충족 되지 않았다면, 환불해줌.
                }
            }
            else{
                gameLoop.showIndicatorText("코인 갯수가 부족 합니다!", IndicatorTextType.WARNING);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        gameLoop.getEventBus().unregister(EventStoreSectionEnded.class, this);
    }

    @Override
    public void notify(Object event) {
        if (event instanceof EventStoreSectionEnded){
            destroy();
        }
    }
}
