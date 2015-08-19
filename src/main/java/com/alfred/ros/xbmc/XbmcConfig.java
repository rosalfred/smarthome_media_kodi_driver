/**
 * This file is part of the Alfred package.
 *
 * (c) Mickael Gaillard <mick.gaillard@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package com.alfred.ros.xbmc;

import org.ros.dynamic_reconfigure.server.BaseConfig;
import org.ros.node.ConnectedNode;

/**
 *
 * @author Erwan Le Huitouze <erwan.lehuitouze@gmail.com>
 *
 */
public class XbmcConfig extends BaseConfig {

    public static final String RATE = "rate";

    public XbmcConfig(ConnectedNode connectedNode) {
        super(connectedNode);

        this.addField(RATE, "int", 0, "rate processus", 1, 0, 200);
    }

}
