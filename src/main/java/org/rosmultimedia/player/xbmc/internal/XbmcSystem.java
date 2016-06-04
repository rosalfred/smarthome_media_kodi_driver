/**
 * This file is part of the Alfred package.
 *
 * (c) Mickael Gaillard <mick.gaillard@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.rosmultimedia.player.xbmc.internal;

import org.rosbuilding.common.ISystem;
import org.rosmultimedia.player.xbmc.XbmcNode;
import org.rosmultimedia.player.xbmc.jsonrpc.XbmcJson;
import org.xbmc.android.jsonrpc.api.call.System;

import smarthome_media_msgs.MediaAction;
import smarthome_media_msgs.StateData;

/**
 * Xbmc System module.
 *
 *
 * @author Erwan Le Huitouze <erwan.lehuitouze@gmail.com>
 *
 */
public class XbmcSystem implements ISystem<StateData, MediaAction> {
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
