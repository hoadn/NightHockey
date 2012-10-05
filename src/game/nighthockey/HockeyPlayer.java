package game.nighthockey;

import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.view.MotionEvent;

class HockeyPlayer extends AnimatedSprite  implements  OnGestureListener {
	public static boolean listenTouch = true;

	private final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	private final Body body;
	private boolean team;
	private float lastTouchLocationX = -1;
	private float lastTouchLocationY = -1;
	float startX, startY,newX,newY;
	float startTime;
	
	public HockeyPlayer(final float pX, final float pY, final TiledTextureRegion pTextureRegion, 
						final VertexBufferObjectManager pVertexBufferObjectManager, PhysicsWorld physicsWorld, boolean team) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
		
		body = PhysicsFactory.createCircleBody(physicsWorld, this, BodyType.DynamicBody, FIXTURE_DEF);
		body.setLinearDamping(0.2f);
		body.setAngularDamping(0.2f);
		
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, true));
		this.animate(300);
	}
	
	public boolean isMoving() {
		if(body.getLinearVelocity() != new Vector2(0, 0))
			return true;
		return false;
	}

	@Override
	public boolean onAreaTouched(TouchEvent pSceneTouchEvent,float pTouchAreaLocalX, float pTouchAreaLocalY) {
		if(pSceneTouchEvent.getAction() == MotionEvent.ACTION_DOWN) {
			startX = pSceneTouchEvent.getX();
			startY = pSceneTouchEvent.getY();
			startTime = System.currentTimeMillis();
			//start the timer, though you may want to periodically reset this as it's not uncommon for the user to hold onto the sprite for a few seconds before deciding where to fling it

			} else if (pSceneTouchEvent.getAction() == MotionEvent.ACTION_UP) { 

			float deltaTime = System.currentTimeMillis() - startTime;
			newX = pSceneTouchEvent.getX();
			newY = pSceneTouchEvent.getY();

			float distance = Math.sqrt((newX-oldX) * (newX-oldX) + (newY-oldY) * (newY-oldY));
			float speed = distance / deltaTime;
			float trajectoryX = startX - newX;
			float trajectoryY = startY - newY;
			//then, send the sprite flying in the direction of trajectoryX, trajectoryY with the momentum speed.
			}
			
		if(listenTouch) { 
			float xPower = 0; 
			float yPower = 0;
			if(lastTouchLocationX != -1) {
				xPower = (pTouchAreaLocalX - lastTouchLocationX) * 10;
				yPower = (pTouchAreaLocalY - lastTouchLocationY) * 10;
			}
			 
			lastTouchLocationX = pTouchAreaLocalX;
			lastTouchLocationY = pTouchAreaLocalY;
			body.applyForce(new Vector2(xPower,yPower), body.getWorldCenter());
		}
		
		return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
	}

	@Override
	public void onGesture(GestureOverlayView arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
		// TODO Auto-generated method stub
		
	}
	@Override

}