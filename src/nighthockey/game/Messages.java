package nighthockey.game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.andengine.extension.multiplayer.protocol.adt.message.client.ClientMessage;
import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;
import org.andengine.extension.physics.box2d.PhysicsWorld;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import android.util.Log;

public class Messages implements ClientMessageFlags, ServerMessageFlags {
	public static final short MESSAGE_ID_SYNC = 1;
	public static final short MESSAGE_ID_MOVE = 2;
	public static final short MESSAGE_ID_INIT = 3;
	public static final short MESSAGE_ID_MVCL = 4;
	
	public Messages() {
	}
	
	public static class ConnectionCloseServer extends ServerMessage {
		public ConnectionCloseServer() {
		}

		@Override
		public short getFlag() {
			return FLAG_MESSAGE_SERVER_CONNECTION_CLOSE;
		}

		@Override
		protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
		}

		@Override
		protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
		}
	}
	
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
			Log.i("NETWORK", "set message");
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
			Log.i("NETWORK", "set message");
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
			
			PhysicsWorld physics = NightHockeyActivity.getPhysics();
			Iterator<Body> bodies = physics.getBodies();
			TouchDetector.listenTouch = true;
			
			while(bodies.hasNext()) {
				Body body = bodies.next();
				HockeyPlayer player = (HockeyPlayer) body.getUserData();
				if(player == null) continue;
				
				if(player.getID() == ID)
					player.body.setLinearVelocity(new Vector2(mX,mY));
			}
		}

		@Override
		protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
			Log.i("NETWORK", "onWriteTransmissionData");
			pDataOutputStream.writeShort(ID);
			pDataOutputStream.writeFloat(this.mX);
			pDataOutputStream.writeFloat(this.mY);
		}
	}
	
	public static class MoveClient extends ClientMessage {
		public short ID;
		public float mX;
		public float mY;
		
		public MoveClient() {
		}

		public MoveClient(final short pID, final float pX, final float pY) {
			ID = pID;
			mX = pX;
			mY = pY;
		}

		public void set(final short pID, final float pX, final float pY) {
			Log.i("NETWORK", "set message");
			ID = pID;
			mX = pX;
			mY = pY;
		}

		@Override
		public short getFlag() {
			return MESSAGE_ID_MVCL;
		}

		@Override
		protected void onReadTransmissionData(final DataInputStream pDataInputStream) throws IOException {
			ID = pDataInputStream.readShort();
			mX = pDataInputStream.readFloat();
			mY = pDataInputStream.readFloat();
			
			Log.i("NETWORK", "CLIENT onReadTransmissionData:" + ID + " " + mX + " " + mY);
			
			PhysicsWorld physics = NightHockeyActivity.getPhysics();
			Iterator<Body> bodies = physics.getBodies();
			TouchDetector.listenTouch = true;
			
			while(bodies.hasNext()) {
				Body body = bodies.next();
				HockeyPlayer player = (HockeyPlayer) body.getUserData();
				if(player == null) continue;
				
				if(player.getID() == ID)
					player.body.setLinearVelocity(new Vector2(mX,mY));
			}
		}

		@Override
		protected void onWriteTransmissionData(final DataOutputStream pDataOutputStream) throws IOException {
			Log.i("NETWORK", "onWriteTransmissionData");
			pDataOutputStream.writeShort(ID);
			pDataOutputStream.writeFloat(this.mX);
			pDataOutputStream.writeFloat(this.mY);
		}
	}
	
	public static class Init extends ClientMessage {
		public String name;
		public Init(){
			
		}

		@Override
		public short getFlag() {
			return MESSAGE_ID_INIT;
		}

		@Override
		protected void onReadTransmissionData(DataInputStream pDataInputStream)
				throws IOException {
			name = pDataInputStream.readUTF();
		}

		@Override
		protected void onWriteTransmissionData(
				DataOutputStream pDataOutputStream) throws IOException {
			pDataOutputStream.writeUTF(name);
		}	
	}
}
