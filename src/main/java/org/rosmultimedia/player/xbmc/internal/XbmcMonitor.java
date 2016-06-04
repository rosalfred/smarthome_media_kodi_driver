/**
 * This file is part of the Alfred package.
 *
 * (c) Mickael Gaillard <mick.gaillard@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.rosmultimedia.player.xbmc.internal;

import org.rosbuilding.common.media.IMonitor;

import smarthome_media_msgs.MediaAction;
import smarthome_media_msgs.StateData;


/**
 * Xbmc Monitor module.
 *
 * @author Erwan Le Huitouze <erwan.lehuitouze@gmail.com>
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
