package nighthockey.game;

import com.badlogic.gdx.physics.box2d.Body;

public interface Drawable {
	public Body getBody();
	public short getID();
	public int getXposition();	// TODO capsulate x and y, width and height
	public int getYposition();
	public int getWidthOfSpite();
	public int getHeightOfSprite();
	public void setActive(boolean active);
	public boolean isActive();
	
	public short puckID = -1;
}
