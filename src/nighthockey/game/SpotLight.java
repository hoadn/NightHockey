package nighthockey.game;

import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.opengl.GLES20;

public class SpotLight extends Sprite {
	private float velocity = 1.0f;
	private int direction = 0;

	private int minX = 0;
	private int maxX = 700;
	private int minY = 0;
	private int maxY = 200;
	
	public SpotLight(final float pX, final float pY, final TextureRegion pTextureRegion, 
			final VertexBufferObjectManager pVertexBufferObjectManager) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);	
		setPosition(pX, pY);
		
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
		
		if ((Math.random() * 10) <= 1) {
		    direction = (int) createAngle(direction, 15.0f);
		}

		direction = (x < minX+20) ? ((int) createAngle(0, 15.0f)) : direction;
		direction = (x > maxX-20) ? ((int) createAngle(180, 15.0f)) : direction;
		direction = (y < minY+20) ? ((int) createAngle(90, 15.0f)) : direction;
		direction = (y > maxY-20) ? ((int) createAngle(270, 15.0f)) : direction;

		x = x + (float) (velocity * Math.cos(direction * Math.PI / 180));
		y = y + (float) (velocity * Math.sin(direction * Math.PI / 180));

		x = (x < minX) ? minX : x;
		x = (x > maxX) ? maxX : x;
		y = (y < minY) ? minY : y;
		y = (y > maxY) ? maxY : y;

		setPosition(x, y);
		
		super.onManagedUpdate(pSecondsElapsed);
	}
}
