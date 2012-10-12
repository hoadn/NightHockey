package game.nighthockey;

import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.opengl.GLES20;

public class SpotLight extends Sprite {
	private float velocity = 1.0f;
	private int direction = 0;

	private int minX = -500;
	private int maxX = 800;
	private int minY = 50;
	private int maxY = 450;
	
	public SpotLight(final float pX, final float pY, final TextureRegion pTextureRegion, 
			final VertexBufferObjectManager pVertexBufferObjectManager) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
		
		double random = Math.random();
		float x = (float) (random * NightHockeyActivity.screenWidth - 100);
		float y = (float) (random * NightHockeyActivity.screenHeight - 100);
		setPosition(x, y);
		
		setBlendingEnabled(true);
		setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
	}
	
    // Create an angle in this interval [baseAngle-otherAngle, baseAngle+otherAngle]
    public float createAngle(float baseAngle, float otherAngle) {

	float angle = baseAngle;
	
	angle += ((Math.random() * otherAngle) - (Math.random() * otherAngle)) * 2;

	return angle;
    }
	
	@Override
	protected void onManagedUpdate(float pSecondsElapsed) {
		float x = getX();
		float y = getY();
		
		// Random direction ( 1 chance on 10 to change direction )
		if ((Math.random() * 10) <= 1) {
		    direction = (int) createAngle(direction, 15.0f);
		}

	        // the object is on the limit, give him a new direction
	        // TODO : make it smooth ( you will see why ... ) 
	        // it's easy, I just didn't have the time to do it for now
		direction = (x < minX+20) ? ((int) createAngle(0, 15.0f)) : direction;
		direction = (x > maxX-20) ? ((int) createAngle(180, 15.0f)) : direction;
		direction = (y < minY+20) ? ((int) createAngle(90, 15.0f)) : direction;
		direction = (y > maxY-20) ? ((int) createAngle(270, 15.0f)) : direction;

		x = x + (float) (velocity * Math.cos(direction * Math.PI / 180));
		y = y + (float) (velocity * Math.sin(direction * Math.PI / 180));

		// Respect the bounds
		x = (x < minX) ? minX : x;
		x = (x > maxX) ? maxX : x;
		y = (y < minY) ? minY : y;
		y = (y > maxY) ? maxY : y;

		setPosition(x, y);
		
		super.onManagedUpdate(pSecondsElapsed);
	}
}
