package com.alfred.ros.xbmc.internal;

import media_msgs.MediaAction;
import media_msgs.SpeakerInfo;
import media_msgs.StateData;
import media_msgs.ToggleMuteSpeakerRequest;
import media_msgs.ToggleMuteSpeakerResponse;

import org.xbmc.android.jsonrpc.api.call.Application;
import org.xbmc.android.jsonrpc.api.model.ApplicationModel.PropertyValue;
import org.xbmc.android.jsonrpc.api.model.GlobalModel.Toggle;

import com.alfred.ros.media.ISpeaker;
import com.alfred.ros.xbmc.XbmcNode;
import com.alfred.ros.xbmc.jsonrpc.XbmcJson;


/**
 * Xbmc Speaker module.
 *
 */
public class XbmcSpeaker implements ISpeaker {
	/**
	 * Xbmc node.
	 */
	private XbmcNode xbmcNode;
	
	/**
	 * Xbmc json-rpc.
	 */
	private XbmcJson xbmcJson;
	
	/**
	 * XbmcSpeaker constructor.
	 * @param xbmcJson {@link XbmcJson} xbmc json-rpc
	 * @param xbmcNode {@link XbmcNode} xbmc node
	 */
	public XbmcSpeaker(XbmcJson xbmcjson, XbmcNode xbmcNode) {
		this.xbmcJson = xbmcjson;
		this.xbmcNode = xbmcNode;
	}
	
	@Override
	public void load(SpeakerInfo speakerInfo) {
		PropertyValue property = this.xbmcJson.getResult(
				new Application.GetProperties(
						PropertyValue.MUTED,
						PropertyValue.NAME,
						PropertyValue.VERSION,
						PropertyValue.VOLUME));
		
		if (property != null) {
    		speakerInfo.setMuted(property.muted);
    		speakerInfo.setLevel(property.volume);
		}
	}
	
	@Override
	public void callbackCmdAction(MediaAction message, StateData stateData) {
		switch (message.getMethod()) {
			case OP_MUTE:
			case OP_MUTE_TOGGLE:
				this.xbmcJson.getResult(new Application.SetMute(
						new Toggle(!stateData.getSpeaker().getMuted())));
				break;
			case OP_VOLUME_DOWN:
				int level = stateData.getSpeaker().getLevel() - LEVEL_STEP;
				
				if (level < LEVEL_MIN) {
					level = LEVEL_MIN;
				}
				
				this.xbmcJson.getResult(new Application.SetVolume(level));
				break;
			case OP_VOLUME_UP:
				level = stateData.getSpeaker().getLevel() + LEVEL_STEP;
				
				if (level > LEVEL_MAX) {
					level = LEVEL_MAX;
				}
				
				this.xbmcJson.getResult(new Application.SetVolume(level));
				break;
			case OP_VOLUME_TO:
				level = 50;
				
				if (level > LEVEL_MIN && level < LEVEL_MAX) {
					this.xbmcJson.getResult(new Application.SetVolume(level));
				}
				break;
		}
	}
	
	@Override
	public void handleSpeakerMuteToggle(ToggleMuteSpeakerRequest request,
			ToggleMuteSpeakerResponse response) {
		response.setState(!this.xbmcNode.getStateData().getSpeaker().getMuted());
		
		this.xbmcNode.logI(String.format("Service call %s : %s",
				XbmcNode.SRV_MUTE_SPEAKER_TOGGLE,
				this.xbmcNode.getStateData().getSpeaker().getMuted()));
		
		MediaAction message = this.xbmcNode.getNode().getTopicMessageFactory()
				.newFromType(MediaAction._TYPE);
		
		message.setMethod(XbmcSpeaker.OP_MUTE_TOGGLE);
		
		this.callbackCmdAction(message, this.xbmcNode.getStateData());
	}
}
