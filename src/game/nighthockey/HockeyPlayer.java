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

class HockeyPlayer extends AnimatedSprite {
	public static boolean listenTouch = true;

	private final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	private final Body body;
	private boolean team;
	private float lastTouchLocationX = -1;
	private float lastTouchLocationY = -1;
	
	public HockeyPlayer(final float pX, final float pY, final TiledTextureRegion pTextureRegion, 
						final VertexBufferObjectManager pVertexBufferObjectManager, PhysicsWorld physicsWorld, boolean team) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
		this.setSize(60, 60);
		
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
			listenTouch = false;
		}
		
		return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
	}
}