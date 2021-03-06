package logic;

import java.util.Vector;

import log.Log;
import event.AccelerationEvent;
import event.ButtonPressedEvent;
import event.ButtonReleasedEvent;
import event.DeviceListener;
import event.GestureEvent;
import event.GestureListener;
import event.MotionStartEvent;
import event.MotionStopEvent;
import event.StateEvent;

public abstract class ProcessingUnit implements DeviceListener {

	// Classifier
	protected Classifier classifier;
	
	// Listener
	private Vector<GestureListener> listen = new Vector<GestureListener>();
	
	protected int gesturecount;
	
	public ProcessingUnit() {
		this.classifier = new Classifier();
		this.gesturecount=0;
	}
	
	/** 
	 * Add an GestureListener to receive Gesture/StateEvents.
	 * 
	 * @param g
	 * 	Class which implements GestureListener interface
	 */
	public void addGestureListener(GestureListener g) {
		this.listen.add(g);
	}
	
	protected void fireGestureEvent(int id, double probability) {
		GestureEvent w = new GestureEvent(this, id, probability);
		for(int i=0; i<this.listen.size(); i++) {
			this.listen.get(i).gestureReceived(w);
		}
	}
	
	protected void fireStateEvent(int state) {
		StateEvent w = new StateEvent(this, state);
		for(int i=0; i<this.listen.size(); i++) {
			this.listen.get(i).stateReceived(w);
		}
	}
	
	public abstract void accelerationReceived(AccelerationEvent event);

	public abstract void buttonPressReceived(ButtonPressedEvent event);

	public abstract void buttonReleaseReceived(ButtonReleasedEvent event);

//	public abstract void infraredReceived(InfraredEvent event);

	public abstract void motionStartReceived(MotionStartEvent event);

	public abstract void motionStopReceived(MotionStopEvent event);
	
	/**
	 * Resets the complete gesturemodel. After reset no gesture is known
	 * to the system.
	 */
	public void reset() {
		if(this.gesturecount>0) {
			this.classifier.clear();
			Log.println("### Model reset ###");
		} else {
			Log.println("There doesn't exist any data to reset.");
		}
	}
	
	// File IO
	public abstract void loadGesture(String name);
	
	public abstract void saveGesture(int id, String name);
	
}
