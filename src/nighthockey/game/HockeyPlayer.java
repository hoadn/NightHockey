package nighthockey.game;

import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

class HockeyPlayer extends Sprite  {
	public static short ID = 0;
	
	protected final Body body;
	protected boolean isActive = false; /* if current body is set to move */
	protected boolean isHockeyplayer = true;
	private final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	private short playerID;
	private Vector2 startLocation;
	
	public HockeyPlayer(final float pX, final float pY, final TextureRegion pTextureRegion, 
						final VertexBufferObjectManager pVertexBufferObjectManager, PhysicsWorld physicsWorld) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
		playerID = ID++;
		
		/* save location where player is in start */
		startLocation.set(pX, pX);
		
		body = PhysicsFactory.createCircleBody(physicsWorld, this, BodyType.DynamicBody, FIXTURE_DEF);
		body.setLinearDamping(0.8f);
		body.setAngularDamping(0.8f);
		
		setAlpha(0.2f);
		
		body.setUserData(this);
		
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, true));
	}
	
	public void resetPosition() {
		body.setTransform(startLocation, 0);
	}
	
	public short getID() {
		return playerID;
	}
}