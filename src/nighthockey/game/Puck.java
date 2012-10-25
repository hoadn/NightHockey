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

class Puck extends Sprite implements Drawable {
	final FixtureDef fixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	protected final Body body;
	protected boolean isHockeyplayer = false;
	private Vector2 startPosition;

	protected boolean isActive = false; /* if current body is set to move */
	
	public Puck(final float pX, final float pY, final TextureRegion pTextureRegion, 
						final VertexBufferObjectManager pVertexBufferObjectManager, PhysicsWorld physicsWorld) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
		fixtureDef.filter.categoryBits = NightHockeyActivity.CATEGORY_PUCK;
		fixtureDef.filter.maskBits = NightHockeyActivity.CATEGORY_WALL;
		body = PhysicsFactory.createCircleBody(physicsWorld, this, BodyType.DynamicBody, fixtureDef);
		
		body.setLinearDamping(0.3f);
		body.setAngularDamping(0.3f);
		body.setUserData(this);
		
		setAlpha(0.2f);
		
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, true));
		
		startPosition = new Vector2(body.getPosition());
	}
	
	public void resetPosition() {
		body.setLinearVelocity(0, 0);
		body.setTransform(startPosition, 0);
	}
	
	public short getID() {
		return puckID;
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
		return isActive;
	}

	@Override
	public Body getBody() {
		return body;
	}

	@Override
	public short getTeam() {
		return 0;
	}
}
