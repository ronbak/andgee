/*
 * wiigee - accelerometerbased gesture recognition
 * Copyright (C) 2007, 2008 Benjamin Poppinga
 * 
 * Developed at University of Oldenburg
 * Contact: benjamin.poppinga@informatik.uni-oldenburg.de
 *
 * This file is part of wiigee.
 *
 * wiigee is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package logic;

import java.util.Vector;

import log.Log;
import event.*;

/**
 * This class analyzes the WiimoteAccelerationEvents emitted from a Wiimote
 * and further creates and manages the different models for each type
 * of gesture. 
 * 
 * @author Benjamin 'BePo' Poppinga
 */
public class TriggeredProcessingUnit extends ProcessingUnit {

	// gesturespecific values
	private Gesture current; // current gesture

	private Vector<Gesture> trainsequence;
	
	// State variables
	private boolean learning, analyzing;
	
	public TriggeredProcessingUnit() {
		super();
		this.learning=false;
		this.analyzing=false;
		this.current=new Gesture();
		this.trainsequence=new Vector<Gesture>();
	}

	/** 
	 * Since this class implements the WiimoteListener this procedure is
	 * necessary. It contains the filtering (directional equivalence filter)
	 * and adds the incoming data to the current motion, we want to train
	 * or recognize.
	 * 
	 * @param event The acceleration event which has to be processed by the
	 * directional equivalence filter and which has to be added to the current
	 * motion in recognition or training process.
	 */
	public void accelerationReceived(AccelerationEvent event) {
		if(this.learning || this.analyzing) {
			this.current.add(event); // add event to gesture			
		}		
	}

	/** 
	 * This method is from the WiimoteListener interface. A button press
	 * is used to control the data flow inside the structures. 
	 * 
	 */
	public void buttonPressReceived(ButtonPressedEvent event) {
		this.handleStartEvent(event);
	}

	public void buttonReleaseReceived(ButtonReleasedEvent event) {
		this.handleStopEvent(event);
	}
	
//	@Override
//	public void infraredReceived(InfraredEvent event) {
//		// NOTHING TO DO HERE	
//	}
	
	public void motionStartReceived(MotionStartEvent event) {
		this.handleStartEvent(event);
	}
	
	public void motionStopReceived(MotionStopEvent event) {
		this.handleStopEvent(event);
	}
	
	public void handleStartEvent(ActionStartEvent event) {
		
		// TrainButton = record a gesture for learning
		if((!this.analyzing && !this.learning) && 
			event.isTrainInitEvent()) {
			Log.println("Training started!");
			this.learning=true;
			this.fireStateEvent(1);
		}
		
		// RecognitionButton = record a gesture for recognition
		if((!this.analyzing && !this.learning) && 
			event.isRecognitionInitEvent()) {
			Log.println("Recognition started!");
			this.analyzing=true;
			this.fireStateEvent(2);
		}
			
		// CloseGestureButton = starts the training of the model with multiple
		// recognized gestures, contained in trainsequence
		if((!this.analyzing && !this.learning) && 
			event.isCloseGestureInitEvent()) {
		
			if(this.trainsequence.size()>0) {
				Log.println("Training the model with "+this.trainsequence.size()+" gestures...");
				this.fireStateEvent(1);
				this.learning=true;
				
				GestureModel m = new GestureModel(this.gesturecount++);
				m.train(this.trainsequence);
				m.print();
				this.classifier.addGestureModel(m);
				
				this.trainsequence=new Vector<Gesture>();
				this.learning=false;
			} else {
				Log.println("There is nothing to do. Please record some gestures first.");
			}
		}
	}
	
	public void handleStopEvent(ActionStopEvent event) {
		if(this.learning) { // button release and state=learning, stops learning
			if(this.current.getCountOfData()>0) {
				Log.println("Finished recording (training)...");
				Log.println("Data: "+this.current.getCountOfData());
				Gesture gesture = new Gesture(this.current);
				this.trainsequence.add(gesture);
				this.current=new Gesture();
				this.learning=false;
			} else {
				Log.println("There is no data.");
				Log.println("Please train the gesture again.");
				this.learning=false; // ?
			}
		}
		
		else if(this.analyzing) { // button release and state=analyzing, stops analyzing
			if(this.current.getCountOfData()>0) {
				Log.println("Finished recording (recognition)...");
				Log.println("Compare gesture with "+this.gesturecount+" other gestures.");
				Gesture gesture = new Gesture(this.current);
				
				int recognized = this.classifier.classifyGesture(gesture);
				if(recognized!=-1) {
					double recogprob = this.classifier.getLastProbability();
					this.fireGestureEvent(recognized, recogprob);
					Log.println("######");
					Log.println("Gesture No. "+recognized+" recognized: "+recogprob);
					Log.println("######");
				} else {
					this.fireStateEvent(0);
					Log.println("######");
					Log.println("No gesture recognized.");
					Log.println("######");
				}
				
				this.current=new Gesture();
				this.analyzing=false;
			} else {
				Log.println("There is no data.");
				Log.println("Please recognize the gesture again.");
				this.analyzing=false; // ?
			}
		}
	}

	@Override
	public void loadGesture(String name) {
		this.gesturecount++;
		GestureModel g = util.FileIO.readFromFile(name);
		this.classifier.addGestureModel(g);	
	}

	@Override
	public void saveGesture(int id, String name) {
		util.FileIO.writeToFile(this.classifier.getGestureModel(id), name);		
	}

}
