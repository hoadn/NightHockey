package game.nighthockey;

import java.io.IOException;
import java.net.Socket;

import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
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
import org.andengine.util.debug.Debug;

import android.util.Log;

import com.badlogic.gdx.math.Vector2;

public class NetworkHandler implements ClientMessageFlags, ServerMessageFlags {
	private static NetworkHandler networkHandler;
	private static final String LOCALHOST_IP = "127.0.0.1";
	private static final int SERVER_PORT = 4746;
	private String mServerIP = LOCALHOST_IP;
	private SocketServer<SocketConnectionClientConnector> mSocketServer;
	private ServerConnector<SocketConnection> mServerConnector;
	private final MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();
	
	public static NetworkHandler getInstance() {
		if(networkHandler == null) {
			networkHandler = new NetworkHandler();
		}
		
		return networkHandler;
	}
	
	public NetworkHandler() {
		initMessagePool();		
		initServerAndClient();
	}
	
	public void sendActionMessage(short ID, Vector2 velocity) {
		try {
			final Messages.Synchrate moveFaceServerMessage = (Messages.Synchrate) mMessagePool.obtainMessage(Messages.MESSAGE_ID_SYNC);
			moveFaceServerMessage.set(ID, velocity.x, velocity.y);

			mSocketServer.sendBroadcastServerMessage(moveFaceServerMessage);

			mMessagePool.recycleMessage(moveFaceServerMessage);
		} catch (final IOException e) {
			Log.e("NETWORK ERROR", "EXEPTION:" + e.getMessage());
		}
	}
	


	private void initMessagePool() {
		mMessagePool.registerMessage(Messages.MESSAGE_ID_SYNC, Messages.Synchrate.class);
		mMessagePool.registerMessage(Messages.MESSAGE_ID_MOVE, Messages.Move.class);
	}

	@Override
	protected void finalize() {
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
	}

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
		mSocketServer = new SocketServer<SocketConnectionClientConnector>(SERVER_PORT, new ClientConnectorListener(), new ServerStateListener()) {
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
			mServerConnector = new SocketConnectionServerConnector(new SocketConnection(new Socket(this.mServerIP, SERVER_PORT)), new ServerConnectorListener());

			mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_CONNECTION_CLOSE, ConnectionCloseServerMessage.class, new IServerMessageHandler<SocketConnection>() {
				@Override
				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
					Log.i("NETWORK", "onHandleMessage: we lost connection to server");
				}
			});

			mServerConnector.registerServerMessage(Messages.MESSAGE_ID_SYNC, Messages.Synchrate.class, new IServerMessageHandler<SocketConnection>() {
				@Override
				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
					Log.i("NETWORK", "onHandleMessage: add face");
					final Messages.Synchrate addFaceServerMessage = (Messages.Synchrate)pServerMessage;
					addFace(addFaceServerMessage.mID, addFaceServerMessage.mX, addFaceServerMessage.mY);
				}
			});

			mServerConnector.registerServerMessage(Messages.MESSAGE_ID_MOVE, Messages.Move.class, new IServerMessageHandler<SocketConnection>() {
				@Override
				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
					Log.i("NETWORK", "onHandleMessage");
					final Messages.Move moveFaceServerMessage = (Messages.Move)pServerMessage;
					moveFace(moveFaceServerMessage.ID, moveFaceServerMessage.mX, moveFaceServerMessage.mY);
				}
			});

			mServerConnector.getConnection().start();
		} catch (final Throwable t) {
			Log.i("NETWORK ERROR", "" + t.getMessage());
		}
	}
	
	private class ServerConnectorListener implements ISocketConnectionServerConnectorListener {
		@Override
		public void onStarted(ServerConnector<SocketConnection> pServerConnector) {
			Log.i("NETWORK", "Server onStarted");
			
		}

		@Override
		public void onTerminated(ServerConnector<SocketConnection> pServerConnector) {
			Log.i("NETWORK", "Server onTerminated");
			
		}
	}

	private class ServerStateListener implements ISocketServerListener<SocketConnectionClientConnector> {
		@Override
		public void onStarted(final SocketServer<SocketConnectionClientConnector> pSocketServer) {
			Log.i("NETWORK", "Server onStarted");
		}

		@Override
		public void onTerminated(final SocketServer<SocketConnectionClientConnector> pSocketServer) {
			Log.i("NETWORK", "Server onTerminated");
		}

		@Override
		public void onException(final SocketServer<SocketConnectionClientConnector> pSocketServer, final Throwable pThrowable) {
			Log.i("NETWORK", "EXEPTION: " + pThrowable.getMessage());
		}
	}

	private class ClientConnectorListener implements ISocketConnectionClientConnectorListener {
		@Override
		public void onStarted(final ClientConnector<SocketConnection> pConnector) {
			Log.i("NETWORK", "Client onStarted");
		}

		@Override
		public void onTerminated(final ClientConnector<SocketConnection> pConnector) {
			Log.i("NETWORK", "Client onTerminated");
		}
	}
}
