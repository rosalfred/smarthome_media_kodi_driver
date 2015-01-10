package com.alfred.ros.xbmc;

import org.ros.dynamic_reconfigure.server.BaseConfig;
import org.ros.node.ConnectedNode;

public class XbmcConfig extends BaseConfig {

    public static final String RATE = "rate";

    public XbmcConfig(ConnectedNode connectedNode) {
        super(connectedNode);

        this.addField(RATE, "int", 0, "rate processus", 1, 0, 200);
    }

}
