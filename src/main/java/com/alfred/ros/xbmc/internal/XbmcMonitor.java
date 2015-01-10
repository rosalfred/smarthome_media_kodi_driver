package com.alfred.ros.xbmc.internal;

import com.alfred.ros.media.IMonitor;

import media_msgs.MediaAction;
import media_msgs.StateData;


/**
 * Xbmc Monitor module.
 *
 */
public class XbmcMonitor implements IMonitor {

	@Override
	public void load(StateData stateData) {
		
	}
	
	@Override
	public void callbackCmdAction(MediaAction message, StateData stateData) {
		
	}
}
