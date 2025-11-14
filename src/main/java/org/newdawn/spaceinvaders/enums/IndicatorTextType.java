package org.newdawn.spaceinvaders.enums;

import java.awt.Color;
import java.awt.Font;

public enum IndicatorTextType {
    WARNING(Color.red, Font.BOLD),
    DEFAULT(Color.white, Font.PLAIN);

    private final Color color;
    private final int fontStyle;

    IndicatorTextType(Color color, int fontStyle){
        this.color = color;
        this.fontStyle = fontStyle;
    }

    public Color getColor() { return color; }
    public int getFontStyle() { return fontStyle; }
}
