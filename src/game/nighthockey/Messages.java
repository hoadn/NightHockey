package game.nighthockey;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.andengine.extension.multiplayer.protocol.adt.message.server.ServerMessage;

import android.util.Log;

public class Messages implements ClientMessageFlags, ServerMessageFlags {
	public static final short MESSAGE_ID_SYNC = 1;
	public static final short MESSAGE_ID_MOVE = MESSAGE_ID_SYNC + 1;
	
	public Messages() {
		
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
