package com.alfred.ros.xbmc.internal;

import media_msgs.MediaAction;
import media_msgs.StateData;

import org.xbmc.android.jsonrpc.api.call.System;

import com.alfred.ros.media.ISystem;
import com.alfred.ros.xbmc.XbmcNode;
import com.alfred.ros.xbmc.jsonrpc.XbmcJson;

/**
 * Xbmc System module.
 *
 */
public class XbmcSystem implements ISystem {
    /**
     * Xbmc node.
     */
    private XbmcNode xbmcNode;

    /**
     * Xbmc json-rpc.
     */
    private XbmcJson xbmcJson;

    /**
     * XbmcSystem constructor.
     * @param xbmcJson {@link XbmcJson} xbmc json-rpc
     * @param xbmcNode {@link XbmcNode} xbmc node
     */
    public XbmcSystem(XbmcJson xbmcJson, XbmcNode node) {
        this.xbmcJson = xbmcJson;
        this.xbmcNode = node;
    }

    @Override
    public void load(StateData stateData) {

    }

    @Override
    public void callbackCmdAction(MediaAction message, StateData stateData) {
        switch (message.getMethod()) {
        case OP_POWER:
            this.xbmcNode.wakeOnLan();
            break;
        case OP_SHUTDOWN:
            this.xbmcJson.getResult(new System.Shutdown());
            break;
        default:

        }
    }

}
