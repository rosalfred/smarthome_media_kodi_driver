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

import org.ros.message.Duration;
import org.rosbuilding.common.media.IPlayer;
import org.rosmultimedia.player.xbmc.XbmcNode;
import org.rosmultimedia.player.xbmc.jsonrpc.XbmcJson;
import org.xbmc.android.jsonrpc.api.call.GUI;
import org.xbmc.android.jsonrpc.api.call.Input;
import org.xbmc.android.jsonrpc.api.call.Player;
import org.xbmc.android.jsonrpc.api.call.Player.GetActivePlayers.GetActivePlayersResult;
import org.xbmc.android.jsonrpc.api.call.Playlist;
import org.xbmc.android.jsonrpc.api.model.GUIModel;
import org.xbmc.android.jsonrpc.api.model.GlobalModel.Toggle;
import org.xbmc.android.jsonrpc.api.model.ListModel;
import org.xbmc.android.jsonrpc.api.model.PlayerModel;
import org.xbmc.android.jsonrpc.api.model.PlayerModel.PositionTime;
import org.xbmc.android.jsonrpc.api.model.PlaylistModel;

import com.google.common.base.Strings;

import smarthome_media_msgs.MediaAction;
import smarthome_media_msgs.MediaType;
import smarthome_media_msgs.PlayerInfo;
import smarthome_media_msgs.StateData;

/**
 * Xbmc Player module.
 *
 * @author Erwan Le Huitouze <erwan.lehuitouze@gmail.com>
 *
 */
public class XbmcPlayer implements IPlayer {
    private static final String XBMC_PLUGIN_YOUTUBE_URL =
            "plugin://plugin.video.youtube/?action=play_video&videoid=%s";

    /**
     * Xbmc node.
     */
    @SuppressWarnings("unused")
    private XbmcNode xbmcNode;

    /**
     * Xbmc json-rpc.
     */
    private XbmcJson xbmcJson;

    /**
     * XbmcPlayer constructor.
     * @param xbmcJson {@link XbmcJson} xbmc json-rpc
     * @param xbmcNode {@link XbmcNode} xbmc node
     */
    public XbmcPlayer(XbmcJson xbmcJson, XbmcNode node) {
        this.xbmcJson = xbmcJson;
        this.xbmcNode = node;
    }

    @Override
    public void load(StateData statedata) {
        this.load(statedata.getPlayer());
    }

    public void load(PlayerInfo playerInfo) {
        this.resetInfo(playerInfo);

        List<GetActivePlayersResult> players = this.xbmcJson
                .getResults(new Player.GetActivePlayers());

        if (players != null && !players.isEmpty()) {
            GetActivePlayersResult player = players.get(0);
            this.updateInfo(playerInfo, player.playerid);
        }
    }

    @Override
    public void callbackCmdAction(MediaAction message, StateData stateData) {
        switch (message.getMethod()) {
        case OP_PAUSE:
            this.xbmcJson.getResult(new Player.PlayPause(1, new Toggle(false)));
            break;

        case OP_PLAY:
            this.xbmcJson.getResult(new Player.PlayPause(1, new Toggle(true)));
            break;

        case OP_PLAYPAUSE:
            this.xbmcJson.getResult(new Player.PlayPause(1));
            break;

        case OP_STOP:
            this.xbmcJson.getResult(new Player.Stop(1));
            break;

        case OP_SPEED:
            if (stateData.getPlayer().getCanseek()) {
                int speed = Integer.parseInt(message.getData().get(0));
                this.xbmcJson.getResult(new Player.SetSpeed(1, speed));
            }
            break;

        case OP_SPEED_UP:
            if (stateData.getPlayer().getCanseek()) {
                int speed = 2;
                this.xbmcJson.getResult(new Player.SetSpeed(1, speed));
            }
            break;

        case OP_SPEED_DOWN:
            if (stateData.getPlayer().getCanseek()) {
                int speed = -2;
                this.xbmcJson.getResult(new Player.SetSpeed(1, speed));
            }
            break;

        case OP_OPEN:
            this.xbmcJson.getResult(new Player.Open(this.makeItem(message)));
            break;

        case OP_SEEK:
            if (stateData.getPlayer().getCanseek()) {
                this.xbmcJson.getResult(new Player.Seek(1, this
                        .getPositionTime(message)));
            }
            break;

        case OP_NEXT:
            if (stateData.getPlayer().getCanseek()) {
                this.xbmcJson.getResult(new Player.GoTo(1, Player.GoTo.To.NEXT));
            }
            break;

        case OP_PREVIOUS:
            if (stateData.getPlayer().getCanseek()) {
                this.xbmcJson.getResult(new Player.GoTo(1, Player.GoTo.To.PREVIOUS));
            }
            break;

        case OP_ADD_PLAYLIST:
            // this.xbmcJson.getResult(new Playlist.Add(1, new
            // PlaylistModel.Item()));
            break;

        case OP_INS_PLAYLIST:
            // this.xbmcJson.getResult(new Playlist.Insert(1, 10, new
            // PlaylistModel.Item()));
            break;

        case OP_REM_PLAYLIST:
            // this.xbmcJson.getResult(new Playlist.Add(1, new
            // PlaylistModel.Item()));
            break;

        case OP_CLR_PLAYLIST:
            this.xbmcJson.getResult(new Playlist.Clear(1));
            break;

        case OP_BACK:
            this.xbmcJson.getResult(new Input.Back());
            break;

        case OP_HOME:
            this.xbmcJson.getResult(new Input.Home());
            break;

        case OP_INFO:
            this.xbmcJson.getResult(new Input.Info());
            break;

        case OP_DISPLAY:
            this.xbmcJson.getResult(new Input.ShowOSD());
            break;

        case OP_SELECT:
            if (stateData.getPlayer().getCanseek()) {
                message.setMethod(OP_PLAYPAUSE);
                this.callbackCmdAction(message, stateData);
            } else {
                this.xbmcJson.getResult(new Input.Select());
            }
            break;

        case OP_CONTEXT:
            this.xbmcJson.getResult(new Input.ContextMenu());
            break;

        case OP_UP:
            if (stateData.getPlayer().getCanseek()) {
                this.xbmcJson.getResult(new Player.Seek(1, Player.Seek.Value.BIGFORWARD));
            } else {
                this.xbmcJson.getResult(new Input.Up());
            }
            break;

        case OP_DOWN:
            if (stateData.getPlayer().getCanseek()) {
                this.xbmcJson.getResult(new Player.Seek(1, Player.Seek.Value.BIGBACKWARD));
            } else {
                this.xbmcJson.getResult(new Input.Down());
            }
            break;

        case OP_LEFT:
            if (stateData.getPlayer().getCanseek()) {
                this.xbmcJson.getResult(new Player.Seek(1, Player.Seek.Value.SMALLBACKWARD));
            } else {
                this.xbmcJson.getResult(new Input.Left());
            }
            break;

        case OP_RIGHT:
            if (stateData.getPlayer().getCanseek()) {
                this.xbmcJson.getResult(new Player.Seek(1, Player.Seek.Value.SMALLFORWARD));
            } else {
                this.xbmcJson.getResult(new Input.Right());
            }
            break;

        case OP_TXT:
            this.xbmcJson
            .getResult(new Input.SendText(message.getData().get(0)));
            break;
        }
    }

    /**
     * Reset all info from {@link PlayerInfo}.
     * @param playerInfo {@link PlayerInfo} to update
     */
    private void resetInfo(PlayerInfo playerInfo) {
        playerInfo.setCanseek(false);
        playerInfo.setMediaid(-1);
        playerInfo.getMediatype().setValue("");
        playerInfo.setStamp(new Duration(0, 0));
        playerInfo.setTotaltime(new Duration(0, 0));
        playerInfo.setSpeed(0);
        playerInfo.setSubtitleenabled(false);
        playerInfo.setFile("");
        playerInfo.setTitle("");
        playerInfo.setThumbnail("");
    }

    /**
     * Update PlayerInfo from xbmc.
     * @param playerInfo {@link PlayerInfo} to update
     * @param playerid Id of active player
     */
    private void updateInfo(PlayerInfo playerInfo, int playerid) {
        PlayerModel.PropertyValue playerProperty = this.xbmcJson
                .getResult(new Player.GetProperties(playerid,
                        // "canrepeat",
                        // "canmove",
                        // "canshuffle",
                        "speed",
                        "percentage",
                        "audiostreams",
                        // "position",
                        // "repeat",
                        // "currentsubtitle",
                        // "canrotate",
                        // "canzoom",
                        // "canchangespeed",
                        // "partymode",
                        // "subtitles",
                        "canseek",
                        "time",
                        "totaltime",
                        // "shuffled",
                        "currentaudiostream",
                        // "live",
                        "subtitleenabled"));

        GUIModel.PropertyValue guiProperty = this.xbmcJson
                .getResult(new GUI.GetProperties(
                        "currentwindow",
                        "currentcontrol"));

        ListModel.AllItems itemProperty = this.xbmcJson
                .getResult(new Player.GetItem(playerid,
                        "file",
                        "title",
                        "thumbnail"));

        playerInfo.setCanseek(true);

        if (playerProperty != null) {
            playerInfo.setStamp(new Duration(playerProperty.time.hours
                    * 60 * 60 + playerProperty.time.minutes * 60
                    + playerProperty.time.seconds,
                    playerProperty.time.milliseconds * 100));

            playerInfo.setTotaltime(new Duration(
                    playerProperty.totaltime.hours * 60 * 60
                    + playerProperty.totaltime.minutes * 60
                    + playerProperty.totaltime.seconds,
                    playerProperty.totaltime.milliseconds * 100));

            playerInfo.setSpeed(playerProperty.speed);
            playerInfo.setCanseek(playerProperty.canseek);
            playerInfo.setSubtitleenabled(playerProperty.subtitleenabled);
        }

        if (guiProperty != null) {
            // xbmc window id : http://wiki.xbmc.org/?title=Window_IDs
            playerInfo.setCanseek(
                    guiProperty.currentwindow.id == 12005 // video fullscreen
                    || guiProperty.currentwindow.id == 12006); //audio visualization
        }

        if (itemProperty != null) {
            playerInfo.setMediaid(0);
            playerInfo.getMediatype().setValue(itemProperty.type);

            if (!playerInfo.getMediatype().getValue().equals(MediaType.UNKNOW)) {
                playerInfo.setMediaid(itemProperty.id);
            }

            playerInfo.setFile(itemProperty.file);
            playerInfo.setTitle(itemProperty.title);
            playerInfo.setThumbnail(XbmcUtils.getImageUrl(itemProperty.thumbnail));
        }
    }

    /**
     * Convert {@link MediaAction} message to {@link PlaylistModel.Item}.
     * @param msg
     * @return
     */
    private PlaylistModel.Item makeItem(MediaAction msg) {
        PlaylistModel.Item item = null;
        String uri = msg.getUri();

        if (!Strings.isNullOrEmpty(uri)) {
            if (uri.startsWith(IPlayer.URI_MEDIA_IMDB)) {
                //Get mediaid from database with imdbid

            } else if (uri.startsWith(IPlayer.URI_MEDIA_YOUTUBE)) {
                item = new PlaylistModel.Item(
                        new PlaylistModel.Item.File(String.format(
                                XBMC_PLUGIN_YOUTUBE_URL,
                                uri.replace(IPlayer.URI_MEDIA_YOUTUBE, ""))));
            } else if (msg.getType().equals(MediaType.VIDEO_MOVIE)) {
                item = new PlaylistModel.Item(new PlaylistModel.Item.Movieid(
                        Integer.parseInt(uri.replace(IPlayer.URI_MEDIA, ""))));
            } else if (msg.getType().equals(MediaType.VIDEO_TVSHOW_EPISODE)) {
                item = new PlaylistModel.Item(new PlaylistModel.Item.Episodeid(
                        Integer.parseInt(uri.replace(IPlayer.URI_MEDIA, ""))));
            }
        }

        return item;
    }

    /**
     * Convert {@link MediaAction} data to {@link PositionTime}.
     * @param msg Data to convert
     * @return {@link PositionTime}
     */
    private PositionTime getPositionTime(MediaAction msg) {
        int time = Integer.parseInt(msg.getData().get(0));

        int hours = time / 3600;
        int rem = time % 3600;
        int minutes = rem / 60;
        int seconds = rem % 60;

        return new PositionTime(hours, 0, minutes, seconds);
    }
}
