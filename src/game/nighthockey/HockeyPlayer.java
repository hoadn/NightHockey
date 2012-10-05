package game.nighthockey;

import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;

class HockeyPlayer extends AnimatedSprite  {
	public static boolean listenTouch = true;

	private final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	private final Body body;
	private boolean team;
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
}