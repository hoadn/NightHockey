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

class HockeyPlayer extends Sprite implements Drawable {
	public static short ID = 0;
	
	protected final Body body;
	protected boolean isActive = false; /* if current body is set to move */
	protected boolean isHockeyplayer = true;
	private final FixtureDef fixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	private short playerID;
	private Vector2 startLocation;
	private short team;
	
	public HockeyPlayer(float pX, float pY, final TextureRegion pTextureRegion, 
						VertexBufferObjectManager pVertexBufferObjectManager, PhysicsWorld physicsWorld, short team) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
		playerID = ID++;
		this.team = team;

		
		fixtureDef.filter.categoryBits = NightHockeyActivity.CATEGORY_PLAYER;
		fixtureDef.filter.maskBits = NightHockeyActivity.CATEGORY_ALL;
		body = PhysicsFactory.createCircleBody(physicsWorld, this, BodyType.DynamicBody, fixtureDef);
		body.setLinearDamping(0.8f);
		body.setAngularDamping(0.8f);
		
		setAlpha(0.3f);
		
		body.setUserData(this);
		
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, true));
		
		/* save location where player is in start */
		startLocation = new Vector2(body.getPosition());
	}
	
	public void resetPosition() {
		body.setLinearVelocity(0, 0);
		body.setTransform(startLocation, 0);
	}
	
	public short getID() {
		return playerID;
	}

	@Override
	public int getXposition() {
		return (int) getX();
	}

	@Override
	public int getYposition() {
		return (int) getY();
	}

	@Override
	public int getWidthOfSpite() {
		return (int) getWidth();
	}

	@Override
	public int getHeightOfSprite() {
		return (int) getHeight();
	}

	@Override
	public void setActive(boolean active) {
		isActive = active;
	}

	@Override
	public boolean isActive() {
		if(NightHockeyActivity.TURN == team)
			return isActive;
		else
			return false;
	}

	@Override
	public Body getBody() {
		return body;
	}
}