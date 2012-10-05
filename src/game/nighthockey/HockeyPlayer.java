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

class HockeyPlayer extends AnimatedSprite  {
	private final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	protected final Body body;
	private boolean team;
	protected boolean isActive = false; /* if current body is set to move */
	protected boolean isHockeyplayer = true;
	
	public HockeyPlayer(final float pX, final float pY, final TiledTextureRegion pTextureRegion, 
						final VertexBufferObjectManager pVertexBufferObjectManager, PhysicsWorld physicsWorld, boolean team) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
		
		body = PhysicsFactory.createCircleBody(physicsWorld, this, BodyType.DynamicBody, FIXTURE_DEF);
		body.setLinearDamping(0.8f);
		body.setAngularDamping(0.8f);
		
		body.setUserData(this);
		
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, true));
		this.animate(300);
	}
}