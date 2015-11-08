/**
 * This file is part of the Alfred package.
 *
 * (c) Mickael Gaillard <mick.gaillard@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.rosmultimedia.player.xbmc;

import org.ros.node.ConnectedNode;
import org.rosbuilding.common.NodeConfig;

/**
 *
 * @author Erwan Le Huitouze <erwan.lehuitouze@gmail.com>
 *
 */
public class XbmcConfig extends NodeConfig {

    private String host;
    private int    port;
    private String user;
    private String password;

    public XbmcConfig(ConnectedNode connectedNode) {
        super(connectedNode, "home/salon/xbmc", "fixed_frame", 1);
    }

    @Override
    protected void loadParameters() {
        super.loadParameters();

        this.host = this.connectedNode.getParameterTree()
                .getString("~ip", "192.168.0.38");
        this.port = this.connectedNode.getParameterTree()
                .getInteger("~port", 8080);
        this.user = this.connectedNode.getParameterTree()
                .getString("~user", "xbmc");
        this.password = this.connectedNode.getParameterTree()
                .getString("~password", "xbmc");

//        this.logI(
//                String.format("rate : %s\nprefix : %s\nfixedFrame : %s\nip : %s\nmac : %s\nport : %s\nuser : %s\npassword : %s",
//                        this.rate,
//                        this.prefix,
//                        this.fixedFrame,
//                        this.host,
//                        this.mac,
//                        this.port,
//                        this.user,
//                        this.password));
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public String getUser() {
        return this.user;
    }

    public String getPassword() {
        return this.password;
    }
}
