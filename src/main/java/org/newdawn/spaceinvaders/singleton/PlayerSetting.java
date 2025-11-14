package org.newdawn.spaceinvaders.singleton;

import java.awt.event.KeyEvent;
import java.util.HashMap;

public class PlayerSetting {
    private final static PlayerSetting INSTANCE = new PlayerSetting();
    public static PlayerSetting getInstance()
    {
        return INSTANCE;
    }

    HashMap<String, Integer> keySetting;
    HashMap<Integer, String> keyToInputName = new HashMap<Integer, String>();

    public static final int MOUSE_BUTTON_LEFT = -1;
    public static final int MOUSE_BUTTON_RIGHT = -2;

    public PlayerSetting(){
        HashMap<String, Integer> newKeySetting = new HashMap<>();

        newKeySetting.put("left", KeyEvent.VK_A);
        newKeySetting.put("right", KeyEvent.VK_D);
        newKeySetting.put("up", KeyEvent.VK_W);
        newKeySetting.put("down", KeyEvent.VK_S);

        newKeySetting.put("escape", KeyEvent.VK_ESCAPE);
        newKeySetting.put("accept", KeyEvent.VK_ENTER);

        newKeySetting.put("record", KeyEvent.VK_R);

        newKeySetting.put("mouse_button_left", MOUSE_BUTTON_LEFT);
        newKeySetting.put("mouse_button_right", MOUSE_BUTTON_RIGHT);

        setKeySetting(newKeySetting);
    }

    private void setKeySetting(HashMap<String, Integer>  keySetting){
        this.keySetting = keySetting;

        keyToInputName.clear();
        for(String key : keySetting.keySet()){
            keyToInputName.put(keySetting.get(key), key);
        }
    }

    public String KeyToInputName(int key){
        return keyToInputName.get(key);
    }
}
