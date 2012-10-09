package game.nighthockey;

import java.util.Iterator;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import android.util.Log;
import android.view.MotionEvent;

public class TouchDetector implements IOnSceneTouchListener {
	private PhysicsWorld physics;
	private double startTime;
	private Vector2 downPosition;
	public static boolean listenTouch = true;
	
	public TouchDetector(PhysicsWorld physics) {
		this.physics = physics;
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if(pSceneTouchEvent.getAction() == MotionEvent.ACTION_DOWN) {			
			if(listenTouch) {
				Iterator<Body> bodies = physics.getBodies();
				downPosition = new Vector2(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
				
				while(bodies.hasNext()) {
					Body body = bodies.next();
					HockeyPlayer player = (HockeyPlayer) body.getUserData();
					if(player == null) continue;
					if(player.isHockeyplayer == false) continue;
	
					Vector2 distance = new Vector2((player.getX() + player.getWidth()/2) - downPosition.x,(player.getY() + player.getHeight()/2) - downPosition.y);
					
					if(distance.x < 0) distance.x *= -1;
					if(distance.y < 0) distance.y *= -1;
					
					if(distance.y <= player.getWidth() && distance.x <= player.getWidth()) {
						startTime = System.currentTimeMillis();
	
						player.isActive = true;
						break;
	
					}
				}
			}
		} else if(pSceneTouchEvent.getAction() == MotionEvent.ACTION_UP) {
			Vector2 upPosition = new Vector2(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
			Iterator<Body> bodies = physics.getBodies();
			
			while(bodies.hasNext()) {
				Body body = bodies.next();
				HockeyPlayer player = (HockeyPlayer) body.getUserData();
				if(player == null) continue;
				
				if(player.isActive) {
					double deltaTime = (System.currentTimeMillis() - startTime) / 100;
					Log.i("Distance", "deltaTime f" + deltaTime);
					
					Vector2 distance = new Vector2(upPosition.x - downPosition.x, upPosition.y - downPosition.y);
					Vector2 speed = new Vector2(distance.x / (float)deltaTime, distance.y / (float)deltaTime);
					
					Log.i("Distance", "Speed " + speed.x + " " + speed.y);
					
					player.body.setLinearVelocity(speed);
					NetworkHandler handler = NetworkHandler.getInstance();
					handler.sendActionMessage(player.getID(), speed);
					
					listenTouch = false;
					player.isActive = false;
					break;
				}
			}
		}
			
		return true;
	}
}
