package game.nighthockey;

import java.util.ArrayList;
import java.util.Iterator;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import android.view.Display;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class NightHockeyActivity extends SimpleBaseGameActivity  {
	/* Const varibles */
	public static int screenWidth = 800;
	public static int screenHeight = 480;
	
	/* Worlds(draw, physics) */
	private Scene scene;
	private static PhysicsWorld physics;
	
	public static PhysicsWorld getPhysics() {
		return physics;
	}
	
	/* Texture handles */
	private TextureRegion homeTexture;
	private TextureRegion visitorTexture;
	private TextureRegion puckTexture;
	private TextureRegion spotLightTexture;
	private BitmapTextureAtlas textureAtlas;
	
	/* HockeyPlayers */
	ArrayList<HockeyPlayer> hockeyPlayers = new ArrayList<HockeyPlayer>();	
	
	@Override
	public EngineOptions onCreateEngineOptions() {
	    final Display display = getWindowManager().getDefaultDisplay();
	    screenWidth = display.getWidth();
	    screenHeight = display.getHeight();
		final Camera camera = new Camera(0, 0, screenWidth, screenHeight);

		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(screenWidth, screenHeight), camera);
	}

	@Override
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
		textureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		homeTexture 	 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "home.png", 0, 0);
		visitorTexture 	 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "visitor.png", 64, 0);
		puckTexture 	 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "puck.png", 128, 0);
		spotLightTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "spotlight.png", 0, 64);
		textureAtlas.load();
	}

	@Override
	public Scene onCreateScene() {		
		/* check every 0.5 sec that is bodies moving or not */
		Timer timer = new Timer(0.5f, new Timer.TimerCalculator() {
		    public void onTick() {
				Iterator<Body> bodies = physics.getBodies();
				TouchDetector.listenTouch = true;
				
				while(bodies.hasNext()) {
					Body body = bodies.next();
					Vector2 bodyVelocity = body.getLinearVelocity();
					float velocitySquared = (float)bodyVelocity.len2();
					
					if(velocitySquared >= 0.1) {
						TouchDetector.listenTouch = false;
						break;
					}
				}
		    }
		});
		mEngine.registerUpdateHandler(timer);
	
		/* Init scene and physics */
		physics = new FixedStepPhysicsWorld(25 ,new Vector2(0, 0), false, 8, 3);
		scene = new Scene();
		scene.registerUpdateHandler(physics);
		scene.setBackground(new Background(0f, 0f, 0f));
		scene.setTouchAreaBindingOnActionDownEnabled(true);
		
		scene.setOnSceneTouchListener(new TouchDetector(physics));

		/* Crate game borders */
		final VertexBufferObjectManager vbo = this.getVertexBufferObjectManager();
		final Rectangle ground = new Rectangle(0, screenHeight - 2, screenWidth, 2, vbo);
		final Rectangle roof = new Rectangle(0, 0, screenWidth, 2, vbo);
		final Rectangle left = new Rectangle(0, 0, 2, screenHeight, vbo);
		final Rectangle right = new Rectangle(screenWidth - 2, 0, 2, screenHeight, vbo);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		PhysicsFactory.createBoxBody(physics, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physics, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physics, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physics, right, BodyType.StaticBody, wallFixtureDef);
		
		scene.attachChild(ground);
		scene.attachChild(roof);
		scene.attachChild(left);
		scene.attachChild(right);
		
		resetHockeyPlayers();
		
		return scene;
	}

	private void resetHockeyPlayers() {
		VertexBufferObjectManager vbo = this.getVertexBufferObjectManager();
	
		/* Set first team */
		hockeyPlayers.add(new HockeyPlayer(100, 100, homeTexture, vbo, physics));
		hockeyPlayers.add(new HockeyPlayer(100, 250, homeTexture, vbo, physics));
		hockeyPlayers.add(new HockeyPlayer(200, 100, homeTexture, vbo, physics));
		hockeyPlayers.add(new HockeyPlayer(200, 200, homeTexture, vbo, physics));
		hockeyPlayers.add(new HockeyPlayer(200, 300, homeTexture, vbo, physics));
		/* Set second team */
		hockeyPlayers.add(new HockeyPlayer(500, 150, visitorTexture, vbo, physics));
		hockeyPlayers.add(new HockeyPlayer(500, 250, visitorTexture, vbo, physics));
		hockeyPlayers.add(new HockeyPlayer(400, 100, visitorTexture, vbo, physics));
		hockeyPlayers.add(new HockeyPlayer(400, 200, visitorTexture, vbo, physics));
		hockeyPlayers.add(new HockeyPlayer(400, 300, visitorTexture, vbo, physics));

		Puck puck = new Puck(screenWidth/2, screenHeight/2, puckTexture, vbo, physics);
		SpotLight light = new SpotLight(100, 100, spotLightTexture, vbo);
		
		for(HockeyPlayer player : hockeyPlayers) {
			scene.registerTouchArea(player);
			scene.attachChild(player);
		}

		scene.attachChild(puck);
		scene.attachChild(light);
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
