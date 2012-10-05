package game.nighthockey;

import android.os.Bundle;
import android.app.Activity;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;
import org.andengine.extension.multiplayer.protocol.client.IServerMessageHandler;
import org.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
import org.andengine.extension.multiplayer.protocol.client.connector.SocketConnectionServerConnector;
import org.andengine.extension.multiplayer.protocol.client.connector.SocketConnectionServerConnector.ISocketConnectionServerConnectorListener;
import org.andengine.extension.multiplayer.protocol.server.SocketServer;
import org.andengine.extension.multiplayer.protocol.server.SocketServer.ISocketServerListener;
import org.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector;
import org.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector.ISocketConnectionClientConnectorListener;
import org.andengine.extension.multiplayer.protocol.shared.SocketConnection;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.SurfaceGestureDetector;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;

import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class NightHockeyActivity extends SimpleBaseGameActivity implements ClientMessageFlags, ServerMessageFlags, OnGestureListener  {
	/* Const varibles */
	private static final int CAMERA_WIDTH = 800;
	private static final int CAMERA_HEIGHT = 480;
	private final boolean HOME = true;
	private final boolean VISITOR = false;
	
	private GestureDetector gestureScanner;
	
	/* Worlds(draw, physics) */
	private Scene scene;
	private PhysicsWorld physics;
	
	/* Texture handles */
	private TiledTextureRegion playerFace;
	private TextureRegion puckFace;
	private BitmapTextureAtlas textureAtlas;
	
	/* HockeyPlayers */
	ArrayList<HockeyPlayer> hockeyPlayers = new ArrayList<HockeyPlayer>();	
	
	public NightHockeyActivity() {
		this.initMessagePool();
		gestureScanner = new GestureDetector(this);
	}
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}

	@Override
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
		textureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		playerFace = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.textureAtlas, this, "face_circle_tiled.png", 0, 32, 2, 1);
		puckFace = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.textureAtlas, this, "face_puck.png", 32, 64);
		textureAtlas.load();
	}

	
	@Override
	public Scene onCreateScene() {
		/* check every 0.5 sec that is bodies moving or not */
		Timer timer = new Timer(0.5f, new Timer.TimerCalculator() {
		    public void onTick() {
				Iterator<Body> bodies = physics.getBodies();
				HockeyPlayer.listenTouch = true;
				
				while(bodies.hasNext()) {
					Body body = bodies.next();
					Vector2 bodyVelocity = body.getLinearVelocity();
					float velocitySquared = (float)bodyVelocity.len2();
					
					if(velocitySquared >= 0.1) {
						HockeyPlayer.listenTouch = false;
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
		scene.setBackground(new Background(1, 1, 1));
		scene.setTouchAreaBindingOnActionDownEnabled(true);

		/* Crate game borders */
		final VertexBufferObjectManager vbo = this.getVertexBufferObjectManager();
		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, vbo);
		final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vbo);
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vbo);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vbo);

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
		
		//onCreateScene2(scene);
		
		return scene;
	}

	private void resetHockeyPlayers() {
		TiledTextureRegion texture = this.playerFace;
		VertexBufferObjectManager vbo = this.getVertexBufferObjectManager();
	
		/* Set first team */
		hockeyPlayers.add(new HockeyPlayer(100, 150, texture, vbo, physics, HOME));
		hockeyPlayers.add(new HockeyPlayer(100, 250, texture, vbo, physics, HOME));
		hockeyPlayers.add(new HockeyPlayer(200, 100, texture, vbo, physics, HOME));
		hockeyPlayers.add(new HockeyPlayer(200, 200, texture, vbo, physics, HOME));
		hockeyPlayers.add(new HockeyPlayer(200, 300, texture, vbo, physics, HOME));
		/* Set second team */
		hockeyPlayers.add(new HockeyPlayer(500, 150, texture, vbo, physics, VISITOR));
		hockeyPlayers.add(new HockeyPlayer(500, 250, texture, vbo, physics, VISITOR));
		hockeyPlayers.add(new HockeyPlayer(400, 100, texture, vbo, physics, VISITOR));
		hockeyPlayers.add(new HockeyPlayer(400, 200, texture, vbo, physics, VISITOR));
		hockeyPlayers.add(new HockeyPlayer(400, 300, texture, vbo, physics, VISITOR));

		Puck puck = new Puck(CAMERA_WIDTH/2, CAMERA_HEIGHT/2, puckFace, vbo, physics);
		
		for(HockeyPlayer player : hockeyPlayers) {
			scene.registerTouchArea(player);
			scene.attachChild(player);
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
	
	/*************************************************************************************/
	
	
	// ===========================================================
	// Constants
	// ===========================================================

	private static final String LOCALHOST_IP = "127.0.0.1";

	private static final int SERVER_PORT = 4746;

	private static final short FLAG_MESSAGE_SERVER_ADD_FACE = 1;
	private static final short FLAG_MESSAGE_SERVER_MOVE_FACE = FLAG_MESSAGE_SERVER_ADD_FACE + 1;

	private static final int DIALOG_CHOOSE_SERVER_OR_CLIENT_ID = 0;
	private static final int DIALOG_ENTER_SERVER_IP_ID = DIALOG_CHOOSE_SERVER_OR_CLIENT_ID + 1;
	private static final int DIALOG_SHOW_SERVER_IP_ID = DIALOG_ENTER_SERVER_IP_ID + 1;

	// ===========================================================
	// Fields
	// ===========================================================

	private BitmapTextureAtlas mBitmapTextureAtlas;
	private ITextureRegion mFaceTextureRegion;

	private int mFaceIDCounter;
	private final SparseArray<Sprite> mFaces = new SparseArray<Sprite>();

	private String mServerIP = LOCALHOST_IP;
	private SocketServer<SocketConnectionClientConnector> mSocketServer;
	private ServerConnector<SocketConnection> mServerConnector;

	private final MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();

	private void initMessagePool() {
		this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_ADD_FACE, AddFaceServerMessage.class);
		this.mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_MOVE_FACE, MoveFaceServerMessage.class);
	}


	public Scene onCreateScene2(Scene scene) {
		initServerAndClient();
		
		Log.i("NETWORK", "onScene2");
		/* We allow only the server to actively send around messages. */
		if(mSocketServer != null) {
			scene.setOnSceneTouchListener(new IOnSceneTouchListener() {
				@Override
				public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
					if(pSceneTouchEvent.isActionDown()) {
						try {
							final AddFaceServerMessage addFaceServerMessage = (AddFaceServerMessage) mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_ADD_FACE);
							addFaceServerMessage.set(3, 32, 3232);

							mSocketServer.sendBroadcastServerMessage(addFaceServerMessage);

							mMessagePool.recycleMessage(addFaceServerMessage);
						} catch (final IOException e) {
							Debug.e(e);
						}
						return true;
					} else {
						return true;
					}
				}
			});

			scene.setOnAreaTouchListener(new IOnAreaTouchListener() {
				@Override
				public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
					try {
						final MoveFaceServerMessage moveFaceServerMessage = (MoveFaceServerMessage) mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_MOVE_FACE);
						moveFaceServerMessage.set(3333, 3223, 3223);

						mSocketServer.sendBroadcastServerMessage(moveFaceServerMessage);

						mMessagePool.recycleMessage(moveFaceServerMessage);
					} catch (final IOException e) {
						Debug.e(e);
						return false;
					}
					return true;
				}
			});
			scene.setTouchAreaBindingOnActionDownEnabled(true);
		}

		return scene;
	}

	@Override
	protected void onDestroy() {
		if(this.mSocketServer != null) {
			try {
				mSocketServer.sendBroadcastServerMessage(new ConnectionCloseServerMessage());
			} catch (final IOException e) {
				Debug.e(e);
			}
			mSocketServer.terminate();
		}

		if(mServerConnector != null) {
			mServerConnector.terminate();
		}

		super.onDestroy();
	}

	@Override
	public boolean onKeyUp(final int pKeyCode, final KeyEvent pEvent) {
		switch(pKeyCode) {
			case KeyEvent.KEYCODE_BACK:
				this.finish();
				return true;
		}
		return super.onKeyUp(pKeyCode, pEvent);
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public void addFace(final int pID, final float pX, final float pY) {
		Log.i("NETWORK", "addFace");
	}

	public void moveFace(final int pID, final float pX, final float pY) {
		/* Find and move the face. */
		Log.i("NETWORK", "moveFace");
	}

	private void initServerAndClient() {
		Log.i("NETWORK", "initClientAndServer");
		this.initServer();

		/* Wait some time after the server has been started, so it actually can start up. */
		try {
			Thread.sleep(500);
		} catch (final Throwable t) {
			Debug.e(t);
		}

		this.initClient();
	}

	private void initServer() {
		Log.i("NETWORK", "initServer");
		mSocketServer = new SocketServer<SocketConnectionClientConnector>(SERVER_PORT, new ExampleClientConnectorListener(), new ExampleServerStateListener()) {
			@Override
			protected SocketConnectionClientConnector newClientConnector(final SocketConnection pSocketConnection) throws IOException {
				Log.i("NETWORK", "newClientConnector");
				return new SocketConnectionClientConnector(pSocketConnection);
			}
		};

		mSocketServer.start();
	}

	private void initClient() {
		Log.i("NETWORK", "initClient");
		try {
			mServerConnector = new SocketConnectionServerConnector(new SocketConnection(new Socket(this.mServerIP, SERVER_PORT)), new ExampleServerConnectorListener());

			mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_CONNECTION_CLOSE, ConnectionCloseServerMessage.class, new IServerMessageHandler<SocketConnection>() {
				@Override
				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
					Log.i("NETWORK", "onHandleMessage");
					finish();
				}
			});

			mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_ADD_FACE, AddFaceServerMessage.class, new IServerMessageHandler<SocketConnection>() {
				@Override
				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
					Log.i("NETWORK", "onHandleMessage");
					final AddFaceServerMessage addFaceServerMessage = (AddFaceServerMessage)pServerMessage;
					addFace(addFaceServerMessage.mID, addFaceServerMessage.mX, addFaceServerMessage.mY);
				}
			});

			mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_MOVE_FACE, MoveFaceServerMessage.class, new IServerMessageHandler<SocketConnection>() {
				@Override
				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
					Log.i("NETWORK", "onHandleMessage");
					final MoveFaceServerMessage moveFaceServerMessage = (MoveFaceServerMessage)pServerMessage;
					moveFace(moveFaceServerMessage.mID, moveFaceServerMessage.mX, moveFaceServerMessage.mY);
				}
			});

			mServerConnector.getConnection().start();
		} catch (final Throwable t) {
			Debug.e(t);
		}
	}

	private void log(final String pMessage) {
		Debug.d(pMessage);
	}

	private void toast(final String pMessage) {
		this.log(pMessage);
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(NightHockeyActivity.this, pMessage, Toast.LENGTH_SHORT).show();
			}
		});
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public static class AddFaceServerMessage extends ServerMessage {
		private int mID;
		private float mX;
		private float mY;

		public AddFaceServerMessage() {

		}

		public AddFaceServerMessage(final int pID, final float pX, final float pY) {
			this.mID = pID;
			this.mX = pX;
			this.mY = pY;
		}

		public void set(final int pID, final float pX, final float pY) {
			this.mID = pID;
			this.mX = pX;
			this.mY = pY;
		}

		@Override
		public short getFlag() {
			return FLAG_MESSAGE_SERVER_ADD_FACE;
		}

		@Override
		protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
			Log.i("NETWORK", "onReadTransmissionData");
			this.mID = pDataInputStream.readInt();
			this.mX = pDataInputStream.readFloat();
			this.mY = pDataInputStream.readFloat();
		}

		@Override
		protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
			Log.i("NETWORK", "onWriteTransmissionData");
			pDataOutputStream.writeInt(this.mID);
			pDataOutputStream.writeFloat(this.mX);
			pDataOutputStream.writeFloat(this.mY);
		}
	}

	public static class MoveFaceServerMessage extends ServerMessage {
		private int mID;
		private float mX;
		private float mY;

		public MoveFaceServerMessage() {

		}

		public MoveFaceServerMessage(final int pID, final float pX, final float pY) {
			this.mID = pID;
			this.mX = pX;
			this.mY = pY;
		}

		public void set(final int pID, final float pX, final float pY) {
			this.mID = pID;
			this.mX = pX;
			this.mY = pY;
		}

		@Override
		public short getFlag() {
			return FLAG_MESSAGE_SERVER_MOVE_FACE;
		}

		@Override
		protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
			Log.i("NETWORK", "onReadTransmissionData");
			this.mID = pDataInputStream.readInt();
			this.mX = pDataInputStream.readFloat();
			this.mY = pDataInputStream.readFloat();
		}

		@Override
		protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
			Log.i("NETWORK", "onWriteTransmissionData");
			pDataOutputStream.writeInt(this.mID);
			pDataOutputStream.writeFloat(this.mX);
			pDataOutputStream.writeFloat(this.mY);
		}
	}

	private class ExampleServerConnectorListener implements ISocketConnectionServerConnectorListener {
		@Override
		public void onStarted(final ServerConnector<SocketConnection> pConnector) {
			toast("CLIENT: Connected to server.");
			Log.i("NETWORK", "onStarted");
		}

		@Override
		public void onTerminated(final ServerConnector<SocketConnection> pConnector) {
			toast("CLIENT: Disconnected from Server...");
			Log.i("NETWORK", "onTerminated");
			finish();
		}
	}

	private class ExampleServerStateListener implements ISocketServerListener<SocketConnectionClientConnector> {
		@Override
		public void onStarted(final SocketServer<SocketConnectionClientConnector> pSocketServer) {
			toast("SERVER: Started.");
			Log.i("NETWORK", "onStarted");
		}

		@Override
		public void onTerminated(final SocketServer<SocketConnectionClientConnector> pSocketServer) {
			toast("SERVER: Terminated.");
			Log.i("NETWORK", "onTerminated");
		}

		@Override
		public void onException(final SocketServer<SocketConnectionClientConnector> pSocketServer, final Throwable pThrowable) {
			Debug.e(pThrowable);
			toast("SERVER: Exception: " + pThrowable);
		}
	}

	private class ExampleClientConnectorListener implements ISocketConnectionClientConnectorListener {
		@Override
		public void onStarted(final ClientConnector<SocketConnection> pConnector) {
			toast("SERVER: Client connected: " + pConnector.getConnection().getSocket().getInetAddress().getHostAddress());
			Log.i("NETWORK", "onStarted");
		}

		@Override
		public void onTerminated(final ClientConnector<SocketConnection> pConnector) {
			toast("SERVER: Client disconnected: " + pConnector.getConnection().getSocket().getInetAddress().getHostAddress());
			Log.i("NETWORK", "onTerminated");
		}
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		Log.i("TOUCH", "FLING");
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
}
