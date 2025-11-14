package org.newdawn.spaceinvaders.sprite;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import org.newdawn.spaceinvaders.fixed_point.FixedPointUtil;

/**
 * A sprite to be displayed on the screen. Note that a sprite
 * contains no state information, i.e. its just the image and 
 * not the location. This allows us to use a single sprite in
 * lots of different places without having to store multiple 
 * copies of the image.
 * 
 * @author Kevin Glass
 */
public class Sprite {
	/** The image to be drawn for this sprite */
	private Image image;
        private long pivotX, pivotY;
        private long scale;
        private AffineTransform transform = new AffineTransform();
	private int width;
	private int height;
	/**
	 * Create a new sprite based on an image
	 * 
	 * @param image The image that is this sprite
	 */
	public Sprite(Image image, long pivotX, long pivotY, long scale) {
        this.image = image;

        this.scale = scale;

        this.pivotX = FixedPointUtil.mul(pivotX, scale);
        this.pivotY = FixedPointUtil.mul(pivotY, scale);

        this.transform.setToTranslation(
                FixedPointUtil.toDouble(-this.pivotX),
                FixedPointUtil.toDouble(-this.pivotY));
        this.transform.scale(FixedPointUtil.toDouble(this.scale), FixedPointUtil.toDouble(this.scale));

        width = FixedPointUtil.toInt(
                FixedPointUtil.mul(
                        FixedPointUtil.fromLong(image.getWidth(null)),
                        scale
                        )
                );
        height = FixedPointUtil.toInt(
                FixedPointUtil.mul(
                        FixedPointUtil.fromLong(image.getHeight(null)),
                        scale
                )
        );
	}
	
	/**
	 * Get the width of the drawn sprite
	 * 
	 * @return The width in pixels of this sprite
	 */
	public int getWidth() {
        return width;
	}

	/**
	 * Get the height of the drawn sprite
	 * 
	 * @return The height in pixels of this sprite
	 */
	public int getHeight() {
		return height;
	}

    public long getPivotX() {
        return pivotX;
    }
    public long getPivotY() {
        return pivotY;
    }

//	/**
//	 * Draw the sprite onto the graphics context provided
//	 *
//	 * @param g The graphics context on which to draw the sprite
//	 * @param x The x location at which to draw the sprite
//	 * @param y The y location at which to draw the sprite
//	 */
//	public void draw(Graphics2D g, int x, int y) {
//		g.drawImage(image,x,y,null);
//	}
    private AffineTransform tempTransform = new AffineTransform();
    public void draw(Graphics2D g, AffineTransform xform) {
        tempTransform.setTransform(xform);
        tempTransform.concatenate(transform);
        g.drawImage(image, tempTransform, null);
    }
}