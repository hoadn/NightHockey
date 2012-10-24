package nighthockey.game;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import nighthockey.game.Messages.GoalMessage;

import org.andengine.extension.multiplayer.protocol.adt.message.IMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.client.IClientMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.server.IServerMessage;
import org.andengine.extension.multiplayer.protocol.client.IServerMessageHandler;
import org.andengine.extension.multiplayer.protocol.client.connector.ServerConnector;
import org.andengine.extension.multiplayer.protocol.client.connector.SocketConnectionServerConnector;
import org.andengine.extension.multiplayer.protocol.client.connector.SocketConnectionServerConnector.ISocketConnectionServerConnectorListener;
import org.andengine.extension.multiplayer.protocol.server.IClientMessageHandler;
import org.andengine.extension.multiplayer.protocol.server.SocketServer;
import org.andengine.extension.multiplayer.protocol.server.SocketServer.ISocketServerListener;
import org.andengine.extension.multiplayer.protocol.server.connector.ClientConnector;
import org.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector;
import org.andengine.extension.multiplayer.protocol.server.connector.SocketConnectionClientConnector.ISocketConnectionClientConnectorListener;
import org.andengine.extension.multiplayer.protocol.shared.SocketConnection;
import org.andengine.extension.multiplayer.protocol.util.MessagePool;

import android.util.Log;

import com.badlogic.gdx.math.Vector2;

public class NetworkHandler {
	private static NetworkHandler networkHandler;
	private String ipAddress = "127.0.0.1";
	private int SERVER_PORT = 4746;
	private SocketServer<SocketConnectionClientConnector> mSocketServer;
	private ServerConnector<SocketConnection> mServerConnector;
	private final MessagePool<IMessage> mMessagePool = new MessagePool<IMessage>();
	private boolean isConnected = false;
	
	public static NetworkHandler getInstance() {
		if(networkHandler == null) {
			networkHandler = new NetworkHandler();
		}
		
		return networkHandler;
	}
	
	private NetworkHandler() {
	}
	
	public void startServer() {
		initMessagePool();
		
		Log.i("NETWORK", "startServer");
		initServer();
	}
	
	public void connectClient(String add) {
		ipAddress = add;
		initMessagePool();
		
		Log.i("NETWORK", "startClient");
		initClient();
	}
	
	public void sendGoalMessage(short goalMaker) {
		Messages.GoalMessage goal = (Messages.GoalMessage) mMessagePool.obtainMessage(Messages.MESSAGE_ID_SYNC);
		goal.setGoalMaker(goalMaker);
		
		try {
			mSocketServer.sendBroadcastServerMessage(goal);
		} catch (IOException e) {}
		
		mMessagePool.recycleMessage(goal);
	}
	
	public void sendInitMessage(String name){
		try{
			Messages.Init init = (Messages.Init) mMessagePool.obtainMessage(Messages.MESSAGE_ID_INIT);
			init.name = name;
			mServerConnector.sendClientMessage(init);
			mMessagePool.recycleMessage(init);
		}catch (final IOException e){
			Log.e("NETWORK ERROR", "EXEPTION:" + e.getMessage());
		}
	}
	
	public void sendSyncMessage(short ID, Vector2 position) {
		Messages.Synchrate serverSync = (Messages.Synchrate) mMessagePool.obtainMessage(Messages.MESSAGE_ID_SYNC);
		serverSync.set(ID, position.x, position.y);
		
		try {
			mSocketServer.sendBroadcastServerMessage(serverSync);
		} catch (IOException e) {}
		
		mMessagePool.recycleMessage(serverSync);
	}
	
	public void sendActionMessage(short ID, Vector2 velocity) {
		Log.i("NETWORK", "Send message");
		try {
			Messages.Move serverMove = (Messages.Move) mMessagePool.obtainMessage(Messages.MESSAGE_ID_MOVE);
			Messages.MoveClient moveClient = (Messages.MoveClient) mMessagePool.obtainMessage(Messages.MESSAGE_ID_MVCL);
			serverMove.set(ID, velocity.x, velocity.y);
			moveClient.set(ID, velocity.x, velocity.y);

			if(mSocketServer != null)
				mSocketServer.sendBroadcastServerMessage(serverMove);
			else if(mServerConnector != null)
				mServerConnector.sendClientMessage(moveClient);

			mMessagePool.recycleMessage(serverMove);
		} catch (final IOException e) {
			Log.e("NETWORK ERROR", "EXEPTION:" + e.getMessage());
		}
	}

	private void initMessagePool() {		
		mMessagePool.registerMessage(Messages.MESSAGE_ID_SYNC, Messages.Synchrate.class);
		mMessagePool.registerMessage(Messages.MESSAGE_ID_MOVE, Messages.Move.class);
		mMessagePool.registerMessage(Messages.MESSAGE_ID_INIT, Messages.Init.class);
		mMessagePool.registerMessage(Messages.MESSAGE_ID_MVCL, Messages.MoveClient.class);
	}

	@Override
	protected void finalize() {
		if(this.mSocketServer != null) {
			try {
				mSocketServer.sendBroadcastServerMessage(new Messages.ConnectionCloseServer());
			} catch (final IOException e) {
				Log.e("NETWORK ERROR", "EXEPTION: finalize()" + e.getMessage());
			}
			mSocketServer.terminate();
		}

		if(mServerConnector != null) {
			mServerConnector.terminate();
		}
	}

	private void initServer() {
		Log.i("NETWORK", "initServer");
		mSocketServer = new SocketServer<SocketConnectionClientConnector>(SERVER_PORT, new ClientConnectorListener(), new ServerStateListener()) {
			@Override
			protected SocketConnectionClientConnector newClientConnector(final SocketConnection pSocketConnection) throws IOException {

                final SocketConnectionClientConnector clientConnector = new SocketConnectionClientConnector(pSocketConnection);
                
                clientConnector.registerClientMessage(Messages.MESSAGE_ID_MVCL, Messages.MoveClient.class, new IClientMessageHandler<SocketConnection>() {
                        @Override
                        public void onHandleMessage(final ClientConnector<SocketConnection> pClientConnector, final IClientMessage pClientMessage) throws IOException {
                            //Message was handled here
                        	Log.i("NETWORK", "I RECEVED MESSAGE");
                        }
                });
                return clientConnector;
			}
		};

		mSocketServer.start();
	}
	public boolean isConnected(){
		return isConnected;
	}
	
	private void initClient() {
		Log.i("NETWORK", "initClient");
		
		try {
			mServerConnector = new SocketConnectionServerConnector(new SocketConnection(new Socket(ipAddress, SERVER_PORT)), new ServerConnectorListener());
		} catch (UnknownHostException e) {
			Log.i("NETWORK ERROR", "initClient() UnknownException");
		} catch (IOException e) {
			Log.i("NETWORK ERROR", "initClient() IOException ");
		}
		
		registerMessages();
		
		try {
			mServerConnector.getConnection().start();
		} catch (final Throwable t) {
			Log.i("NETWORK ERROR", "initClient() " + t.getMessage());
		}
	}
	
	private void registerMessages() {
		mServerConnector.registerServerMessage(Messages.FLAG_MESSAGE_SERVER_CONNECTION_CLOSE, Messages.ConnectionCloseServer.class, new IServerMessageHandler<SocketConnection>() {
			@Override
			public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
				Log.i("NETWORK", "onHandleMessage: we lost connection to server");
			}
		});

		mServerConnector.registerServerMessage(Messages.MESSAGE_ID_SYNC, Messages.Synchrate.class, new IServerMessageHandler<SocketConnection>() {
			@Override
			public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
				Log.i("NETWORK", "onHandleMessage: SYNC");
				//final Messages.Synchrate sync = (Messages.Synchrate)pServerMessage;
			}
		});

		mServerConnector.registerServerMessage(Messages.MESSAGE_ID_MOVE, Messages.Move.class, new IServerMessageHandler<SocketConnection>() {
			@Override
			public void onHandleMessage(final ServerConnector<SocketConnection> pServerConnector, final IServerMessage pServerMessage) throws IOException {
				Log.i("NETWORK", "onHandleMessage: MOVE");
				//final Messages.Move move = (Messages.Move)pServerMessage;			
			}
		});
	}
	
	private class ServerConnectorListener implements ISocketConnectionServerConnectorListener {
		@Override
		public void onStarted(ServerConnector<SocketConnection> pServerConnector) {
			Log.i("NETWORK", "Client connected to server");	
			isConnected = true;
		}

		@Override
		public void onTerminated(ServerConnector<SocketConnection> pServerConnector) {
			Log.i("NETWORK", "Server onTerminated");
			isConnected = false;
		}
	}

	private class ServerStateListener implements ISocketServerListener<SocketConnectionClientConnector> {		
		@Override
		public void onStarted(final SocketServer<SocketConnectionClientConnector> pSocketServer) {
			Log.i("NETWORK", "Server onStarted 1");
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
			Log.i("NETWORK", "Server receive connection from client");
			isConnected = true;
		}

		@Override
		public void onTerminated(final ClientConnector<SocketConnection> pConnector) {
			Log.i("NETWORK", "Client Terminated");
			isConnected = false;
		}
	}
}
