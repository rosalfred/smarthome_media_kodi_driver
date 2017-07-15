/**
 * This file is part of the Alfred package.
 *
 * (c) Mickael Gaillard <mick.gaillard@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.rosmultimedia.player.xbmc.internal;

import java.util.List;

import org.rosbuilding.common.media.Monitor;

import smarthome_media_msgs.msg.MediaAction;
import smarthome_media_msgs.msg.StateData;


/**
 * Xbmc Monitor module.
 *
 * @author Erwan Le Huitouze <erwan.lehuitouze@gmail.com>
 *
 */
public class XbmcMonitor extends Monitor {

    @Override
    public void load(StateData stateData) {

    }

    @Override
    public void callbackCmdAction(MediaAction message, StateData stateData) {

    }

    @Override
    protected void initializeAvailableMethods(List<String> availableMethods) {

    }
}
