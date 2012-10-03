package game.nighthockey;

import java.util.ArrayList;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class NightHockeyActivity extends SimpleBaseGameActivity {
	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;
	private Scene mScene;
	private PhysicsWorld mPhysicsWorld;

	/* Holds our picture for drawing */
	private TiledTextureRegion mCircleFaceTextureRegion;
	private TextureRegion mPuckFaceTextureRegion;
	/* Points our pictures in assets */
	private BitmapTextureAtlas mBitmapTextureAtlas;
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}

	@Override
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 64, 128, TextureOptions.BILINEAR);
		this.mCircleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "face_circle_tiled.png", 0, 32, 2, 1);
		this.mPuckFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "face_puck.png", 32, 64);
		this.mBitmapTextureAtlas.load();
	}

	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
		this.mScene.setBackground(new Background(1, 1, 1));

		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);

		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);
		
		this.mScene.attachChild(ground);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);
		
		this.mScene.registerUpdateHandler(this.mPhysicsWorld);
		
		mScene.setTouchAreaBindingOnActionDownEnabled(true);
		
		resetHockeyPlayers();

		return this.mScene;
	}
	
	private void resetHockeyPlayers() {
		ArrayList<HockeyPlayer> hockeyPlayers = new ArrayList<NightHockeyActivity.HockeyPlayer>();	
		hockeyPlayers.add(new HockeyPlayer(100, 100, this.mCircleFaceTextureRegion, this.getVertexBufferObjectManager(), mPhysicsWorld));
		hockeyPlayers.add(new HockeyPlayer(200, 200, this.mCircleFaceTextureRegion, this.getVertexBufferObjectManager(), mPhysicsWorld));
		
		Puck puck = new Puck(100, 100, this.mPuckFaceTextureRegion, this.getVertexBufferObjectManager(), mPhysicsWorld);
		
		this.mScene.registerTouchArea(hockeyPlayers.get(0));
		this.mScene.registerTouchArea(hockeyPlayers.get(1));
				
		this.mScene.attachChild(hockeyPlayers.get(0));
		this.mScene.attachChild(hockeyPlayers.get(1));
		this.mScene.attachChild(puck);
	}
	
	private class HockeyPlayer extends AnimatedSprite {
		final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
		final Body body;
		
		float lastTouchLocationX = -1;
		float lastTouchLocationY = -1;
		
		public HockeyPlayer(final float pX, final float pY, final TiledTextureRegion pTextureRegion, 
							final VertexBufferObjectManager pVertexBufferObjectManager, PhysicsWorld physicsWorld) {
			super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
			body = PhysicsFactory.createCircleBody(physicsWorld, this, BodyType.DynamicBody, FIXTURE_DEF);
			body.setLinearDamping(0.2f);
			body.setAngularDamping(0.2f);
			
			physicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, true));
			this.animate(300);
		}

		@Override
		public boolean onAreaTouched(TouchEvent pSceneTouchEvent,float pTouchAreaLocalX, float pTouchAreaLocalY) {
			float xPower = 0; 
			float yPower = 0;
			if(lastTouchLocationX != -1) {
				xPower = (pTouchAreaLocalX - lastTouchLocationX) * 10;
				yPower = (pTouchAreaLocalY - lastTouchLocationY) * 10;
			}
			 
			lastTouchLocationX = pTouchAreaLocalX;
			lastTouchLocationY = pTouchAreaLocalY;
			body.applyForce(new Vector2(xPower,yPower), body.getWorldCenter());
			
			return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
		}
	}
	
	private class Puck extends Sprite {
		final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
		final Body body;
		
		public Puck(final float pX, final float pY, final TextureRegion pTextureRegion, 
							final VertexBufferObjectManager pVertexBufferObjectManager, PhysicsWorld physicsWorld) {
			super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
			body = PhysicsFactory.createCircleBody(physicsWorld, this, BodyType.DynamicBody, FIXTURE_DEF);
			
			body.setLinearDamping(0.3f);
			body.setAngularDamping(0.3f);
			
			physicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, true));
		}
	}

	@Override
	public void onResumeGame() {	
		super.onResumeGame();
	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();
	}
}