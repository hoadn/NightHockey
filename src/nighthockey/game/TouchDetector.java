package nighthockey.game;

import java.util.Iterator;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import android.view.MotionEvent;

public class TouchDetector implements IOnSceneTouchListener {
	public static boolean listenTouch = true;
	
	private double startTime;
	private Vector2 downPosition;
	private boolean online = false;
	
	public TouchDetector(PhysicsWorld physics, boolean online) {
		this.online = online;
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if(pSceneTouchEvent.getAction() == MotionEvent.ACTION_DOWN) {			
			if(listenTouch) {
				//Check if game is online if it is don't listen other player turns
				if(online){
					if(NightHockeyActivity.SERVER){
						if(NightHockeyActivity.getTurn() != NightHockeyActivity.HOME){
							return false;
						}
					}
					else { // It's client
						if(NightHockeyActivity.getTurn() != NightHockeyActivity.VISITOR)
							return false;
					}
				}
				
				synchronized (NightHockeyActivity.lock) {
					Iterator<Body> bodies = NightHockeyActivity.getPhysics().getBodies();
					downPosition = new Vector2(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
					
					while(bodies.hasNext()) {
						Body body = bodies.next();
						Drawable player = (Drawable) body.getUserData();
						if(player == null) continue;
	
						if(player.getID() == Drawable.puckID) continue;
	
		
						Vector2 distance = new Vector2((player.getXposition() + player.getWidthOfSpite()/2) - downPosition.x,
								(player.getYposition() + player.getHeightOfSprite()/2) - downPosition.y);
						
						if(distance.x < 0) distance.x *= -1;
						if(distance.y < 0) distance.y *= -1;
						
						if(distance.y <= player.getWidthOfSpite() && distance.x <= player.getWidthOfSpite()) {
							if(player.getTeam() == NightHockeyActivity.getTurn()){
								startTime = System.currentTimeMillis();
			
								player.setActive(true);
								break;
							}
						}
					}
				}
			}
		} else if(pSceneTouchEvent.getAction() == MotionEvent.ACTION_UP) {		
			synchronized (NightHockeyActivity.lock) {
				Vector2 upPosition = new Vector2(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
				Iterator<Body> bodies = NightHockeyActivity.getPhysics().getBodies();
				
				while(bodies.hasNext()) {
					Body body = bodies.next();
					Drawable player = (Drawable) body.getUserData();
					if(player == null) continue;
					
					if(player.isActive()) {
						double deltaTime = (System.currentTimeMillis() - startTime) / 100;
						
						Vector2 distance = new Vector2(upPosition.x - downPosition.x, upPosition.y - downPosition.y);
						Vector2 speed = new Vector2(distance.x / (float)deltaTime, distance.y / (float)deltaTime);
						
						player.getBody().setLinearVelocity(speed);
						
						if(online) {
							NetworkHandler handler = NetworkHandler.getInstance();
							handler.sendActionMessage(player.getID(), speed);
						}
						
						/* Change turn */
						NightHockeyActivity.changeTurn();
						
						listenTouch = false;
						player.setActive(false);
						
						break;
					}
				}
			}
		}
			
		return true;
	}
}
