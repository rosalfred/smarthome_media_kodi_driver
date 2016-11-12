/**
 * This file is part of the Alfred package.
 *
 * (c) Mickael Gaillard <mick.gaillard@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.rosmultimedia.player.xbmc;

import org.xbmc.android.jsonrpc.api.call.JSONRPC.Ping;
import org.xbmc.android.jsonrpc.api.call.JSONRPC.Version;

import org.ros2.rcljava.RCLJava;
import org.ros2.rcljava.namespace.GraphName;
import org.ros2.rcljava.node.Node;
import org.ros2.rcljava.node.service.TriConsumer;
import org.ros2.rcljava.node.service.RMWRequestId;

import org.rosbuilding.common.BaseDriverNode;
import org.rosbuilding.common.media.MediaMessageConverter;
import org.rosbuilding.common.media.MediaStateDataComparator;
import org.rosmultimedia.player.xbmc.IXbmcNode;
import org.rosmultimedia.player.xbmc.XbmcConfig;
import org.rosmultimedia.player.xbmc.internal.XbmcLibrary;
import org.rosmultimedia.player.xbmc.internal.XbmcMonitor;
import org.rosmultimedia.player.xbmc.internal.XbmcPlayer;
import org.rosmultimedia.player.xbmc.internal.XbmcSpeaker;
import org.rosmultimedia.player.xbmc.internal.XbmcSystem;
import org.rosmultimedia.player.xbmc.jsonrpc.XbmcJson;

import smarthome_media_msgs.msg.StateData;
import smarthome_media_msgs.msg.MediaAction;
import smarthome_media_msgs.msg.MediaItem;
import smarthome_media_msgs.srv.MediaGetItem;
import smarthome_media_msgs.srv.MediaGetItem_Request;
import smarthome_media_msgs.srv.MediaGetItem_Response;
import smarthome_media_msgs.srv.MediaGetItems;
import smarthome_media_msgs.srv.MediaGetItems_Request;
import smarthome_media_msgs.srv.MediaGetItems_Response;
import smarthome_media_msgs.srv.ToggleMuteSpeaker;
import smarthome_media_msgs.srv.ToggleMuteSpeaker_Request;
import smarthome_media_msgs.srv.ToggleMuteSpeaker_Response;

/**
 * Xbmc ROS Node.
 *
 * @author Erwan Le Huitouze <erwan.lehuitouze@gmail.com>
 *
 */
public class XbmcNode extends BaseDriverNode<XbmcConfig, StateData, MediaAction> implements IXbmcNode {

    public static final String SRV_MUTE_SPEAKER_TOGGLE  = "~/speaker_mute_toggle";
    public static final String SRV_MEDIA_GET_ITEM       = "~/get_item";
    public static final String SRV_MEDIA_GET_ITEMS      = "~/get_items";

    private XbmcJson xbmcJson;

    private XbmcLibrary library;
    private XbmcSpeaker speaker;

    public XbmcNode() {
        super(
            new MediaStateDataComparator(),
            new MediaMessageConverter(),
            MediaAction.class.getName(),
            StateData.class.getName());
    }

    @Override
    public void onStart(final Node connectedNode) {
        super.onStart(connectedNode);
        this.startFinal();
    }

    @Override
    protected void onConnected() {
        this.getStateData().setState(StateData.ENABLE);
    }

    @Override
    protected void onDisconnected() {
        this.getStateData().setState(StateData.UNKNOWN);
    }

    @Override
    public void onNewMessage(MediaAction message) {
        if (message != null) {
            this.logI(String.format("Command \"%s\"... for %s",
                    message.getMethod(),
                    message.getUri()));

            super.onNewMessage(message);
        }
    }

    @Override
    protected void initialize() {
        super.initialize();

        String url = String.format("http://%s:%d/jsonrpc",
                this.configuration.getHost(),
                this.configuration.getPort());

        this.xbmcJson = new XbmcJson(
                url, this.configuration.getUser(), this.configuration.getPassword());

        this.library = new XbmcLibrary(this.xbmcJson, this);
        this.speaker = new XbmcSpeaker(this.xbmcJson, this);
        this.addModule(new XbmcMonitor());
        this.addModule(new XbmcPlayer(this.xbmcJson, this));
        this.addModule(new XbmcSystem(this.xbmcJson, this));
        this.addModule(this.speaker);
    }

    @Override
    protected void initTopics() {
        super.initTopics();

        try {
            this.getConnectedNode().<ToggleMuteSpeaker>createService(ToggleMuteSpeaker.class,
                    GraphName.getFullName(this.connectedNode, SRV_MUTE_SPEAKER_TOGGLE, null),
                    new TriConsumer<RMWRequestId, ToggleMuteSpeaker_Request, ToggleMuteSpeaker_Response>() {
                        @Override
                        public void accept(
                                final RMWRequestId header,
                                final ToggleMuteSpeaker_Request request,
                                final ToggleMuteSpeaker_Response response) {

                            XbmcNode.this.speaker.handleSpeakerMuteToggle(request, response);
                        }
                    });

            this.getConnectedNode().<MediaGetItem>createService(MediaGetItem.class,
                    GraphName.getFullName(this.connectedNode, SRV_MEDIA_GET_ITEM, null),
                    new TriConsumer<RMWRequestId, MediaGetItem_Request, MediaGetItem_Response>() {
                        @Override
                        public void accept(
                                final RMWRequestId header,
                                final MediaGetItem_Request request,
                                final MediaGetItem_Response response) {

                            XbmcNode.this.library.handleMediaGetItem(request, response);
                            logger.debug(response.getItem().getData());
                        }
                    });

            this.getConnectedNode().<MediaGetItems>createService(MediaGetItems.class,
                    GraphName.getFullName(this.connectedNode, SRV_MEDIA_GET_ITEMS, null),
                    new TriConsumer<RMWRequestId, MediaGetItems_Request, MediaGetItems_Response>() {
                        @Override
                        public void accept(
                                final RMWRequestId header,
                                final MediaGetItems_Request request,
                                final MediaGetItems_Response response) {

                            XbmcNode.this.library.handleMediaGetItems(request, response);
                            for (MediaItem media : response.getItems()) {
                                logger.debug(String.format("%s : %s ",
                                        media.getMediatype().getValue(),
                                        media.getData() ));
                            }
                        }
                    });

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected boolean connect() {
        boolean result = false;

        this.logI(String.format("Connecting to %s:%s...",
                this.configuration.getHost(),
                this.configuration.getPort()));

        if (this.pingXbmc()) {
            this.getStateData().setState(StateData.INIT);

            result = true;
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
            this.getStateData().setState(StateData.SHUTDOWN);

            try {
                Thread.sleep(10000 / this.configuration.getRate());
            } catch (InterruptedException e) {
                this.logE(e);
            }
        }

        return result;
    }

    @Override
    protected XbmcConfig makeConfiguration() {
        return new XbmcConfig(this.getConnectedNode());
    }

    private boolean pingXbmc() {
        String ping = this.xbmcJson.getResult(new Ping());
        return ping != null && ping.equals("pong");
    }

    public static void main(String[] args) throws InterruptedException {
      // Initialize RCL
      RCLJava.rclJavaInit();

      // Let's create a Node
      Node node = RCLJava.createNode("/home/salon", "kodi");

      XbmcNode samsung = new XbmcNode();
      samsung.onStart(node);

      RCLJava.spin(node);

      samsung.onShutdown(node);
      node.dispose();
      RCLJava.shutdown();
  }
}
