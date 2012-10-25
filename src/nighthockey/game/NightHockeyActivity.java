package nighthockey.game;

import java.util.ArrayList;
import java.util.Iterator;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.RepeatingSpriteBackground;
import org.andengine.entity.scene.background.SpriteBackground;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.HorizontalAlign;

import android.graphics.Typeface;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class NightHockeyActivity extends SimpleBaseGameActivity  {
	/* Environment */
	public static int screenWidth = 800;
	public static int screenHeight = 480;
	
	/* Worlds(draw, physics) */
	private Scene scene;
	private static PhysicsWorld physics;
	
	public static PhysicsWorld getPhysics() {
		return physics;
	}
	
	/* Game states and timers to it */
	public static boolean ONLINE_GAME = false;
	public static boolean SERVER = false;
	public static boolean SINGLE_GAME = true;
	Timer startTimer;
	
	/* Texture handles */
	private TextureRegion homeTexture;
	private TextureRegion visitorTexture;
	private TextureRegion puckTexture;
	private TextureRegion spotLightTexture;
	private SpriteBackground iceTexture;

	private BitmapTextureAtlas textureAtlas;
	
	/* Game objects and calculators */
	private ArrayList<HockeyPlayer> hockeyPlayers = new ArrayList<HockeyPlayer>();	
	private ArrayList<SpotLight> spotLights = new ArrayList<SpotLight>();
	private Puck puck;
	private Font mFont;
	private Text goalHome;
	private Text goalVisitor;
	private int homeGoals = 0;
	private int visitorGoals = 0;
	public static final short HOME = 0x1;
	public static final short VISITOR = 0x2;
	protected static short GOAL = 0x0;
	protected short NO_GOAL = GOAL;
	public static short TURN;
	
	/* Game collision categories */
	public static final short CATEGORY_PUCK = 0x1;
	public static final short CATEGORY_WALL = 0x2;
	public static final short CATEGORY_PLAYER = 0x3;
	public static final short CATEGORY_ALL = 0xFF;


	@Override
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, screenWidth, screenHeight);	
		
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(screenWidth, screenHeight), camera);
	}

	@Override
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");	
		textureAtlas	 = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		homeTexture 	 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "home.png", 0, 0);
		visitorTexture 	 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "visitor.png", 64, 0);
		puckTexture 	 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "puck.png", 128, 0);
		spotLightTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(textureAtlas, this, "spotlight.png", 0, 64);
		iceTexture = new RepeatingSpriteBackground(screenWidth, screenHeight, this.getTextureManager(), AssetBitmapTextureAtlasSource.create(this.getAssets(), "gfx/ice.jpg"), this.getVertexBufferObjectManager());
		textureAtlas.load();
		
		mFont = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 32);
		mFont.load();
	}
	
	public static void goalHasBeenMade(short goalMaker) {
		GOAL = goalMaker; 
	}

	@Override
	public Scene onCreateScene() {
		if(SERVER || ONLINE_GAME) SINGLE_GAME = false;
		
		/* check every 0.5 sec that is bodies moving or not */
		Timer moveCheckTimer = new Timer(0.5f, new Timer.TimerCalculator() {
		    public void onTick() {   		
	    		if(GOAL == VISITOR) {
	    			GOAL = NO_GOAL;
	    			setVisitorGoal();
	    			return;
	    		} else if(GOAL == HOME) {
	    			GOAL = NO_GOAL;
	    			setHomeGoal();
	    			return;
	    		}

				Iterator<Body> bodies = physics.getBodies();
				TouchDetector.listenTouch = true;
				
				while(bodies.hasNext()) {
					Body body = bodies.next();
					Vector2 bodyVelocity = body.getLinearVelocity();
					float velocitySquared = (float)bodyVelocity.len2();
					Drawable object = (Drawable) body.getUserData();
					if(object == null) continue;
					
					/* check if goal */
					if(object.getID() == Drawable.puckID && (SERVER || SINGLE_GAME)) {
						NetworkHandler nh = NetworkHandler.getInstance();
						if(object.getXposition() < 0) {
							setVisitorGoal();
							
							if(SERVER) {
								nh.sendGoalMessage(VISITOR);
							}
						} else if(object.getXposition() > screenWidth) {
							setHomeGoal();
							
							if(SERVER) {
								nh.sendGoalMessage(HOME);
							} 
						}
					}
									
					if(velocitySquared >= 0.1) {
						TouchDetector.listenTouch = false;
					} else if(SERVER && velocitySquared <= 0.1) { 
						NetworkHandler nh = NetworkHandler.getInstance();
						nh.sendSyncMessage(object.getID(), body.getPosition());
					} 
				}
		    }
		});
		/* Show lighting first 5 sec then start the game officially */
		startTimer = new Timer(5f, new Timer.TimerCalculator() {
		    public void onTick() {
		    	/* remove ligths and then set alpha correctly */
		    	for(SpotLight light : spotLights)
		    		scene.detachChild(light);
		    	
		    	for(HockeyPlayer player : hockeyPlayers) {
		    		player.setAlpha(1.0f);
		    	}
		    	
		    	puck.setAlpha(1.0f);
		    	
				scene.setOnSceneTouchListener(new TouchDetector(physics,ONLINE_GAME));
				/* stop this timer */
				mEngine.unregisterUpdateHandler(startTimer);
		    }
		});
		
		mEngine.registerUpdateHandler(moveCheckTimer);
		mEngine.registerUpdateHandler(startTimer);
	
		/* Init scene and physics */
		physics = new FixedStepPhysicsWorld(25 ,new Vector2(0, 0), false, 8, 3);
		scene = new Scene();
		scene.registerUpdateHandler(physics);
		scene.setBackground(iceTexture);
		scene.setTouchAreaBindingOnActionDownEnabled(true);

		/* Crate game borders */
		final VertexBufferObjectManager vbo = this.getVertexBufferObjectManager();
		final Rectangle ground = new Rectangle(0, screenHeight - 2, screenWidth, 2, vbo);
		final Rectangle roof = new Rectangle(0, 0, screenWidth, 2, vbo);
		final Rectangle leftRoof = new Rectangle(0, 0, 2, (float) (screenHeight*0.33), vbo);
		final Rectangle leftGround = new Rectangle(0,(float) (screenHeight*0.66),2,screenHeight,vbo);
		final Rectangle rightRoof = new Rectangle(screenWidth - 2, 0, 2, (float) (screenHeight*0.33), vbo);
		final Rectangle rightGround = new Rectangle(screenWidth - 2,(float) (screenHeight*0.66), 2,screenHeight, vbo);
		
		final Rectangle goalVisitor = new Rectangle(screenWidth - 10, (float) (screenHeight*0.33), 10, (float) (screenHeight*0.33), vbo);
		final Rectangle goalHome = new Rectangle(0, (float) (screenHeight*0.33), 10, (float) (screenHeight*0.33), vbo);
		
		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		final FixtureDef goalFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		wallFixtureDef.filter.categoryBits = CATEGORY_WALL;
		
		PhysicsFactory.createBoxBody(physics, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physics, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physics, leftRoof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physics, leftGround, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physics, rightRoof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physics, rightGround, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(physics, goalHome, BodyType.StaticBody, goalFixtureDef);
		PhysicsFactory.createBoxBody(physics, goalVisitor, BodyType.StaticBody, goalFixtureDef);	
		
		scene.attachChild(ground);
		scene.attachChild(roof);
		scene.attachChild(leftGround);
		scene.attachChild(leftRoof);
		scene.attachChild(rightRoof);
		scene.attachChild(rightGround);
		
		createHockeyPlayers();
		
		/* Draw goal situation */
		this.goalHome = new Text(100,40,mFont, "0", new TextOptions(HorizontalAlign.CENTER), vbo);
		this.goalVisitor = new Text(screenWidth - 100, 40, mFont, "0", new TextOptions(HorizontalAlign.CENTER), vbo);
		
		scene.attachChild(this.goalHome);
		scene.attachChild(this.goalVisitor);
		
		/* Set home team to start */
		TURN = HOME;
		
		return scene;
	}

	/* Client set goal from server */
	protected void setHomeGoal() {
		resetGame();
		homeGoals++;
		goalHome.setText("" + homeGoals);	
	}

	/* Client set goal from server */
	protected void setVisitorGoal() {
		resetGame();
		visitorGoals++;
		goalVisitor.setText("" + visitorGoals);	
	}

	protected void resetGame() {
		for(HockeyPlayer player : hockeyPlayers)
			player.resetPosition();
		
		puck.resetPosition();
	}

	private void createHockeyPlayers() {
		VertexBufferObjectManager vbo = this.getVertexBufferObjectManager();
	
		/* Set first team */
		hockeyPlayers.add(new HockeyPlayer((screenWidth/2) - 250, (screenHeight/2) - 60 , homeTexture, vbo, physics, HOME));
		hockeyPlayers.add(new HockeyPlayer((screenWidth/2) - 250, (screenHeight/2) + 50 , homeTexture, vbo, physics, HOME));
		hockeyPlayers.add(new HockeyPlayer((screenWidth/2) - 150, (screenHeight/2) - 110, homeTexture, vbo, physics, HOME));
		hockeyPlayers.add(new HockeyPlayer((screenWidth/2) - 150, (screenHeight/2) - 10, homeTexture, vbo, physics, HOME));
		hockeyPlayers.add(new HockeyPlayer((screenWidth/2) - 150, (screenHeight/2) + 100, homeTexture, vbo, physics, HOME));
		/* Set second team */
		hockeyPlayers.add(new HockeyPlayer((screenWidth/2) + 240, (screenHeight/2) - 60 , visitorTexture, vbo, physics, VISITOR));
		hockeyPlayers.add(new HockeyPlayer((screenWidth/2) + 240, (screenHeight/2) + 50 , visitorTexture, vbo, physics, VISITOR));
		hockeyPlayers.add(new HockeyPlayer((screenWidth/2) + 140, (screenHeight/2) - 110, visitorTexture, vbo, physics, VISITOR));
		hockeyPlayers.add(new HockeyPlayer((screenWidth/2) + 140, (screenHeight/2) - 10, visitorTexture, vbo, physics, VISITOR));
		hockeyPlayers.add(new HockeyPlayer((screenWidth/2) + 140, (screenHeight/2) + 100, visitorTexture, vbo, physics, VISITOR));

		puck = new Puck(screenWidth/2, screenHeight/2, puckTexture, vbo, physics);
		for(int i = 0; i <= 2; i++) {
			float random = (float) (50 + (Math.random() * (600 - 50) + 1));
			spotLights.add(new SpotLight(random, random, spotLightTexture, vbo));
		}
		
		for(HockeyPlayer player : hockeyPlayers) {
			scene.attachChild(player);
		}
		for(SpotLight light : spotLights) {
			scene.attachChild(light);
		}

		scene.attachChild(puck);
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
