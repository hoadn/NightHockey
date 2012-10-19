package nighthockey.game;

import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

class Puck extends Sprite implements Drawable {
	final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	final Body body;
	protected boolean isHockeyplayer = false;
	final short ID = -1;
	protected boolean isActive = false; /* if current body is set to move */
	
	public Puck(final float pX, final float pY, final TextureRegion pTextureRegion, 
						final VertexBufferObjectManager pVertexBufferObjectManager, PhysicsWorld physicsWorld) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
		
		body = PhysicsFactory.createCircleBody(physicsWorld, this, BodyType.DynamicBody, FIXTURE_DEF);
		
		body.setLinearDamping(0.3f);
		body.setAngularDamping(0.3f);
		body.setUserData(this);
		
		setAlpha(0.2f);
		
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, true));
	}
	
	public short getID() {
		return ID;
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
	public void isActive(boolean active) {
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
}
