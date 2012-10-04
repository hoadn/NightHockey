package game.nighthockey;

import org.andengine.engine.handler.IUpdateHandler;

public class Timer implements IUpdateHandler {
	private TimerCalculator callback;
	private float interval;
	
	private float ellapsed;

	public Timer(float pInterval, TimerCalculator timerCalculator) {
		interval = pInterval;
		callback = timerCalculator;
	}

	@Override
	public void onUpdate(float pSecondsElapsed) {
		ellapsed += pSecondsElapsed;
		if(ellapsed >= interval) {
			ellapsed -= interval;
			callback.onTick();
		}
	}
	
	@Override
	public void reset() {
		ellapsed = 0;	
	}

	public interface TimerCalculator {
		public void onTick();
	}
}

