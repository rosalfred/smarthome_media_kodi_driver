/**
 * This file is part of the Alfred package.
 *
 * (c) Mickael Gaillard <mick.gaillard@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.rosmultimedia.player.xbmc;

import org.ros2.rcljava.node.Node;
import org.rosbuilding.common.NodeDriverConnectedConfig;

/**
 *
 * @author Erwan Le Huitouze <erwan.lehuitouze@gmail.com>
 *
 */
public class XbmcConfig extends NodeDriverConnectedConfig {

    public XbmcConfig(Node connectedNode) {
        super(
                connectedNode,
                "/home/salon/xbmc/",
                "fixed_frame",
                1,
                "00:00:00:00:00:00",
                "192.168.0.68",
                8080L,
                "xbmc",
                "xbmc");
    }
}
