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
import android.util.Log;

import com.badlogic.gdx.math.Vector2;

public class NetworkHandler implements ClientMessageFlags, ServerMessageFlags {
	private static NetworkHandler networkHandler;
	private String ipAddress = "127.0.0.1";
	private int SERVER_PORT = 4746;
	private SocketServer<SocketConnectionClientConnector> mSocketServer;
	private ServerConnector<SocketConnection> mServerConnector;
	private final MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();
	
	public static NetworkHandler getInstance() {
		if(networkHandler == null) {
			networkHandler = new NetworkHandler();
		}
		
		return networkHandler;
	}
	
	public void startServer() {
		initMessagePool();
		Log.e("NETWORK ERROR", "test message");
		
		Log.i("NETWORK", "startServer");
		initServer();
	}
	
	public void connectClient(String add) {
		ipAddress = add;
		initMessagePool();
		
		Log.i("NETWORK", "startClient");
		initClient();
	}
	
	public void sendActionMessage(short ID, Vector2 velocity) {
		try {
			Move moveFaceServerMessage = (Move) mMessagePool.obtainMessage(MESSAGE_ID_MOVE);
			moveFaceServerMessage.set(ID, velocity.x, velocity.y);

			if(mSocketServer != null)
				mSocketServer.sendBroadcastServerMessage(moveFaceServerMessage);
			else
				Log.e("NETWORK ERROR", "mSocketServer is null");

			mMessagePool.recycleMessage(moveFaceServerMessage);
		} catch (final IOException e) {
			Log.e("NETWORK ERROR", "EXEPTION:" + e.getMessage());
		}
	}

	private void initMessagePool() {		
		mMessagePool.registerMessage(MESSAGE_ID_SYNC, Synchrate.class);
		mMessagePool.registerMessage(MESSAGE_ID_MOVE, Move.class);
	}

	@Override
	protected void finalize() {
		if(this.mSocketServer != null) {
			try {
				mSocketServer.sendBroadcastServerMessage(new ConnectionCloseServerMessage());
			} catch (final IOException e) {
				Log.e("NETWORK ERROR", "EXEPTION:" + e.getMessage());
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
			mServerConnector = new SocketConnectionServerConnector(new SocketConnection(new Socket(ipAddress, SERVER_PORT)), new ServerConnectorListener());

			mServerConnector.registerServerMessage(FLAG_MESSAGE_SERVER_CONNECTION_CLOSE, ConnectionCloseServerMessage.class, new IServerMessageHandler<SocketConnection>() {
				@Override
				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
					Log.i("NETWORK", "onHandleMessage: we lost connection to server");
				}
			});

			mServerConnector.registerServerMessage(MESSAGE_ID_SYNC, Synchrate.class, new IServerMessageHandler<SocketConnection>() {
				@Override
				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
					Log.i("NETWORK", "onHandleMessage: add face");
					final Synchrate addFaceServerMessage = (Synchrate)pServerMessage;
					addFace(addFaceServerMessage.mID, addFaceServerMessage.mX, addFaceServerMessage.mY);
				}
			});

			mServerConnector.registerServerMessage(MESSAGE_ID_MOVE, Move.class, new IServerMessageHandler<SocketConnection>() {
				@Override
				public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
					Log.i("NETWORK", "onHandleMessage");
					final Move moveFaceServerMessage = (Move)pServerMessage;
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
	
	/**************************************************************************************/
	public static final short MESSAGE_ID_SYNC = 1;
	public static final short MESSAGE_ID_MOVE = 2;
	
	public static class Synchrate extends ServerMessage {
		public short mID;
		public float mX;
		public float mY;
		
		public Synchrate() {
			
		}

		public Synchrate(final short pID, final float pX, final float pY) {
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
			return MESSAGE_ID_SYNC;
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

	public static class Move extends ServerMessage {
		public short ID;
		public float mX;
		public float mY;
		
		public Move() {
			
		}

		public Move(final short pID, final float pX, final float pY) {
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
			return MESSAGE_ID_MOVE;
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
}
