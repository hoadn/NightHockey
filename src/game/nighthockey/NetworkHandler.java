package game.nighthockey;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
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
import org.andengine.util.debug.Debug;
import com.badlogic.gdx.math.Vector2;
import android.util.Log;

public class NetworkHandler implements ClientMessageFlags, ServerMessageFlags {
	private static NetworkHandler networkHandler;
	private static final String LOCALHOST_IP = "127.0.0.1";
	private static final int SERVER_PORT = 4746;
	private static final short FLAG_MESSAGE_SERVER_ADD_FACE = 1;
	private static final short FLAG_MESSAGE_SERVER_MOVE_FACE = FLAG_MESSAGE_SERVER_ADD_FACE + 1;
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
			final SynchrateMessage moveFaceServerMessage = (SynchrateMessage) mMessagePool.obtainMessage(FLAG_MESSAGE_SERVER_ADD_FACE);
			moveFaceServerMessage.set(ID, velocity.x, velocity.y);

			mSocketServer.sendBroadcastServerMessage(moveFaceServerMessage);

			mMessagePool.recycleMessage(moveFaceServerMessage);
		} catch (final IOException e) {
			Log.e("NETWORK ERROR", "EXEPTION:" + e.getMessage());
		}
	}
	


	private void initMessagePool() {
		mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_ADD_FACE, SynchrateMessage.class);
		mMessagePool.registerMessage(FLAG_MESSAGE_SERVER_MOVE_FACE, MoveMessage.class);
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
					Log.i("NETWORK", "onHandleMessage: we lost connection to server");
				}
			});

			mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_ADD_FACE, SynchrateMessage.class, new IServerMessageHandler<SocketConnection>() {
				@Override
				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
					Log.i("NETWORK", "onHandleMessage: add face");
					final SynchrateMessage addFaceServerMessage = (SynchrateMessage)pServerMessage;
					addFace(addFaceServerMessage.mID, addFaceServerMessage.mX, addFaceServerMessage.mY);
				}
			});

			mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_MOVE_FACE, MoveMessage.class, new IServerMessageHandler<SocketConnection>() {
				@Override
				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
					Log.i("NETWORK", "onHandleMessage");
					final MoveMessage moveFaceServerMessage = (MoveMessage)pServerMessage;
					moveFace(moveFaceServerMessage.ID, moveFaceServerMessage.mX, moveFaceServerMessage.mY);
				}
			});

			mServerConnector.getConnection().start();
		} catch (final Throwable t) {
			Log.i("NETWORK ERROR", "" + t.getMessage());
		}
	}

	public static class SynchrateMessage extends ServerMessage {
		private short mID;
		private float mX;
		private float mY;

		public SynchrateMessage() {

		}

		public SynchrateMessage(final short pID, final float pX, final float pY) {
			mID = pID;
			mX = pX;
			mY = pY;
		}

		public void set(final short pID, final float pX, final float pY) {
			mID = pID;
			mX = pX;
			mY = pY;
		}

		@Override
		public short getFlag() {
			return FLAG_MESSAGE_SERVER_ADD_FACE;
		}

		@Override
		protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
			mID = pDataInputStream.readShort();
			mX = pDataInputStream.readFloat();
			mY = pDataInputStream.readFloat();
			
			Log.i("NETWORK", "SERVER onReadTransmissionData " + mID + " " + mX + " " + " " + mY);
		}

		@Override
		protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
			Log.i("NETWORK", "onWriteTransmissionData");
			pDataOutputStream.writeShort(mID);
			pDataOutputStream.writeFloat(mX);
			pDataOutputStream.writeFloat(mY);
		}
	}

	public static class MoveMessage extends ServerMessage {
		private short ID;
		private float mX;
		private float mY;

		public MoveMessage() {

		}

		public MoveMessage(final short pID, final float pX, final float pY) {
			ID = pID;
			mX = pX;
			mY = pY;
		}

		public void set(final short pID, final float pX, final float pY) {
			ID = pID;
			mX = pX;
			mY = pY;
		}

		@Override
		public short getFlag() {
			return FLAG_MESSAGE_SERVER_MOVE_FACE;
		}

		@Override
		protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
			ID = pDataInputStream.readShort();
			mX = pDataInputStream.readFloat();
			mY = pDataInputStream.readFloat();
			
			Log.i("NETWORK", "CLIENT onReadTransmissionData:" + ID + " " + mX + " " + mY);
		}

		@Override
		protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
			Log.i("NETWORK", "onWriteTransmissionData");
			pDataOutputStream.writeShort(ID);
			pDataOutputStream.writeFloat(this.mX);
			pDataOutputStream.writeFloat(this.mY);
		}
	}

	private class ExampleServerConnectorListener implements ISocketConnectionServerConnectorListener {
		@Override
		public void onStarted(final ServerConnector<SocketConnection> pConnector) {
			Log.i("NETWORK", "onStarted");
		}

		@Override
		public void onTerminated(final ServerConnector<SocketConnection> pConnector) {
			Log.i("NETWORK", "onTerminated");
		}
	}

	private class ExampleServerStateListener implements ISocketServerListener<SocketConnectionClientConnector> {
		@Override
		public void onStarted(final SocketServer<SocketConnectionClientConnector> pSocketServer) {
			Log.i("NETWORK", "onStarted");
		}

		@Override
		public void onTerminated(final SocketServer<SocketConnectionClientConnector> pSocketServer) {
			Log.i("NETWORK", "onTerminated");
		}

		@Override
		public void onException(final SocketServer<SocketConnectionClientConnector> pSocketServer, final Throwable pThrowable) {
			Log.i("NETWORK", "EXEPTION: " + pThrowable.getMessage());
		}
	}

	private class ExampleClientConnectorListener implements ISocketConnectionClientConnectorListener {
		@Override
		public void onStarted(final ClientConnector<SocketConnection> pConnector) {
			Log.i("NETWORK", "onStarted");
		}

		@Override
		public void onTerminated(final ClientConnector<SocketConnection> pConnector) {
			Log.i("NETWORK", "onTerminated");
		}
	}
}
