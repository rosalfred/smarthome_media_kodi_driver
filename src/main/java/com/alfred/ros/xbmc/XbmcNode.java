/**
 * This file is part of the Alfred package.
 *
 * (c) Mickael Gaillard <mick.gaillard@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package com.alfred.ros.xbmc;

import media_msgs.MediaGetItem;
import media_msgs.MediaGetItemRequest;
import media_msgs.MediaGetItemResponse;
import media_msgs.MediaGetItems;
import media_msgs.MediaGetItemsRequest;
import media_msgs.MediaGetItemsResponse;
import media_msgs.StateData;
import media_msgs.ToggleMuteSpeaker;
import media_msgs.ToggleMuteSpeakerRequest;
import media_msgs.ToggleMuteSpeakerResponse;

import org.ros.dynamic_reconfigure.server.Server;
import org.ros.dynamic_reconfigure.server.Server.ReconfigureListener;
import org.ros.exception.ServiceException;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.service.ServiceResponseBuilder;
import org.xbmc.android.jsonrpc.api.call.JSONRPC.Ping;
import org.xbmc.android.jsonrpc.api.call.JSONRPC.Version;

import com.alfred.ros.media.BaseMediaNodeMain;
import com.alfred.ros.xbmc.internal.XbmcLibrary;
import com.alfred.ros.xbmc.internal.XbmcMonitor;
import com.alfred.ros.xbmc.internal.XbmcPlayer;
import com.alfred.ros.xbmc.internal.XbmcSpeaker;
import com.alfred.ros.xbmc.internal.XbmcSystem;
import com.alfred.ros.xbmc.jsonrpc.XbmcJson;

/**
 * Xbmc ROS Node.
 *
 * @author Erwan Le Huitouze <erwan.lehuitouze@gmail.com>
 *
 */
public class XbmcNode
        extends BaseMediaNodeMain
        implements ReconfigureListener<XbmcConfig> {

    public static final String SRV_MUTE_SPEAKER_TOGGLE = "speaker_mute_toggle";
    public static final String SRV_MEDIA_GET_ITEM = "get_item";
    public static final String SRV_MEDIA_GET_ITEMS = "get_items";

    private XbmcJson xbmcJson;

    static {
        nodeName = "xbmc";
    }

    @Override
    public void onStart(final ConnectedNode connectedNode) {
        super.onStart(connectedNode);
        this.startFinal();
    }

    @Override
    public void onShutdown(Node node) {
        super.onShutdown(node);
    }

    @Override
    protected void loadParameters() {
        this.logI("Load parameters.");

        this.prefix = String.format("/%s/", this.connectedNode.getParameterTree()
                .getString("~tf_prefix", "home/salon/xbmc"));
        this.fixedFrame = this.connectedNode.getParameterTree()
                .getString("~fixed_frame", "fixed_frame");
        this.rate = this.connectedNode.getParameterTree()
                .getInteger("~" + XbmcConfig.RATE, 1);

        if (this.rate <= 0) {
            this.rate = 1;
        }

        this.mac = this.connectedNode.getParameterTree()
                .getString("~mac", "00:01:2E:BC:16:33");
        this.host = this.connectedNode.getParameterTree()
                .getString("~ip", "192.168.0.38");
        this.port = this.connectedNode.getParameterTree()
                .getInteger("~port", 8080);
        this.user = this.connectedNode.getParameterTree()
                .getString("~user", "xbmc");
        this.password = this.connectedNode.getParameterTree()
                .getString("~password", "xbmc");

        this.logI(
                String.format("rate : %s\nprefix : %s\nfixedFrame : %s\nip : %s\nmac : %s\nport : %s\nuser : %s\npassword : %s",
                        this.rate,
                        this.prefix,
                        this.fixedFrame,
                        this.host,
                        this.mac,
                        this.port,
                        this.user,
                        this.password));

        this.serverReconfig = new Server<XbmcConfig>(
                this.connectedNode,
                new XbmcConfig(this.connectedNode),
                this);
    }

    @Override
    protected void initialize() {
        super.initialize();

        String url = String.format("http://%s:%d/jsonrpc",
                this.host,
                this.port);

        this.xbmcJson = new XbmcJson(url, this.user, this.password);

        this.monitor = new XbmcMonitor();
        this.player = new XbmcPlayer(this.xbmcJson, this);
        this.speaker = new XbmcSpeaker(this.xbmcJson, this);
        this.system = new XbmcSystem(this.xbmcJson, this);
        this.library = new XbmcLibrary(this.xbmcJson, this);
    }

    @Override
    protected void initServices() {
        super.initServices();

        this.connectedNode.newServiceServer(
                this.prefix + SRV_MUTE_SPEAKER_TOGGLE,
                ToggleMuteSpeaker._TYPE,
                new ServiceResponseBuilder<ToggleMuteSpeakerRequest, ToggleMuteSpeakerResponse>() {
                    @Override
                    public void build(ToggleMuteSpeakerRequest request,
                            ToggleMuteSpeakerResponse response) throws ServiceException {
                        XbmcNode.this.speaker.handleSpeakerMuteToggle(request, response);
                    }
                });

        this.connectedNode.newServiceServer(
                this.prefix + SRV_MEDIA_GET_ITEM,
                MediaGetItem._TYPE,
                new ServiceResponseBuilder<MediaGetItemRequest, MediaGetItemResponse>() {
                    @Override
                    public void build(MediaGetItemRequest request,
                            MediaGetItemResponse response) throws ServiceException {
                        XbmcNode.this.library.handleMediaGetItem(request, response);
                    }
                });

        this.connectedNode.newServiceServer(
                this.prefix + SRV_MEDIA_GET_ITEMS,
                MediaGetItems._TYPE,
                new ServiceResponseBuilder<MediaGetItemsRequest, MediaGetItemsResponse>() {
                    @Override
                    public void build(MediaGetItemsRequest request,
                            MediaGetItemsResponse response) throws ServiceException {
                        XbmcNode.this.library.handleMediaGetItems(request, response);
                    }
                });
    }

    @Override
    protected void connect() {
        this.logI(String.format("Connecting to %s:%s...", this.host, this.port));

        if (this.pingXbmc()) {
            this.stateData.setState(StateData.INIT);

            this.isConnected = true;
            Version.VersionResult version = this.xbmcJson.getResult(new Version());

            if (version.major >= 6) {
                this.logI(String.format(
                        "\tDetected XBMC JSON-RPC version : %d.%d.%d . Done",
                        version.major,
                        version.minor,
                        version.patch));
            } else {
                this.logI(String.format(
                        "\tDetected XBMC JSON-RPC version : %d.%d.%d . Upgrade your XBMC ! this drivers is only available for >= 6.x.x JSON-RPC",
                        version.major,
                        version.minor,
                        version.patch));
            }

            this.logI("\tConnected done.");
        } else {
            this.stateData.setState(StateData.SHUTDOWN);

            try {
                Thread.sleep(10000 / this.rate);
            } catch (InterruptedException e) {
                this.logE(e);
            }
        }
    }

    @Override
    public XbmcConfig onReconfigure(XbmcConfig config, int level) {
        this.rate = config.getInteger(XbmcConfig.RATE, this.rate);
        return config;
    }

    private boolean pingXbmc() {
        String ping = this.xbmcJson.getResult(new Ping());
        return ping != null && ping.equals("pong");
    }

    public StateData getStateData() {
        return this.stateData;
    }

    public ConnectedNode getNode() {
        return this.connectedNode;
    }
}
