/**
 * This file is part of the Alfred package.
 *
 * (c) Mickael Gaillard <mick.gaillard@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
package org.rosmultimedia.player.xbmc.internal;

import java.util.ArrayList;
import java.util.List;

import org.rosbuilding.common.media.ILibrary;
import org.rosmultimedia.player.media.model.Album;
import org.rosmultimedia.player.media.model.Media;
import org.rosmultimedia.player.media.model.Movie;
import org.rosmultimedia.player.media.model.Song;
import org.rosmultimedia.player.media.model.Tvshow;
import org.rosmultimedia.player.xbmc.IXbmcNode;
import org.rosmultimedia.player.xbmc.XbmcNode;
import org.rosmultimedia.player.xbmc.jsonrpc.XbmcJson;
import org.xbmc.android.jsonrpc.api.call.AudioLibrary;
import org.xbmc.android.jsonrpc.api.call.VideoLibrary.GetEpisodeDetails;
import org.xbmc.android.jsonrpc.api.call.VideoLibrary.GetEpisodes;
import org.xbmc.android.jsonrpc.api.call.VideoLibrary.GetMovieDetails;
import org.xbmc.android.jsonrpc.api.call.VideoLibrary.GetMovies;
import org.xbmc.android.jsonrpc.api.call.VideoLibrary.GetTVShows;
import org.xbmc.android.jsonrpc.api.model.AudioModel.AlbumDetail;
import org.xbmc.android.jsonrpc.api.model.AudioModel.SongDetail;
import org.xbmc.android.jsonrpc.api.model.ListModel.AlbumFilter;
import org.xbmc.android.jsonrpc.api.model.ListModel.AlbumFilterRule;
import org.xbmc.android.jsonrpc.api.model.ListModel.EpisodeFilter;
import org.xbmc.android.jsonrpc.api.model.ListModel.EpisodeFilterRule;
import org.xbmc.android.jsonrpc.api.model.ListModel.FilterRule;
import org.xbmc.android.jsonrpc.api.model.ListModel.FilterRule.Value;
import org.xbmc.android.jsonrpc.api.model.ListModel.Limits;
import org.xbmc.android.jsonrpc.api.model.ListModel.MovieFilter;
import org.xbmc.android.jsonrpc.api.model.ListModel.MovieFilterRule;
import org.xbmc.android.jsonrpc.api.model.ListModel.SongFilter;
import org.xbmc.android.jsonrpc.api.model.ListModel.SongFilterRule;
import org.xbmc.android.jsonrpc.api.model.ListModel.Sort;
import org.xbmc.android.jsonrpc.api.model.ListModel.TVShowFilter;
import org.xbmc.android.jsonrpc.api.model.ListModel.TVShowFilterRule;
import org.xbmc.android.jsonrpc.api.model.VideoModel.EpisodeDetail;
import org.xbmc.android.jsonrpc.api.model.VideoModel.FileDetail;
import org.xbmc.android.jsonrpc.api.model.VideoModel.ItemDetail;
import org.xbmc.android.jsonrpc.api.model.VideoModel.MovieDetail;
import org.xbmc.android.jsonrpc.api.model.VideoModel.TVShowDetail;

import com.google.common.base.Strings;
import com.google.common.collect.ObjectArrays;

import smarthome_media_msgs.MediaGetItemRequest;
import smarthome_media_msgs.MediaGetItemResponse;
import smarthome_media_msgs.MediaGetItemsRequest;
import smarthome_media_msgs.MediaGetItemsResponse;
import smarthome_media_msgs.MediaItem;
import smarthome_media_msgs.MediaType;

/**
 * Xbmc Libray Module.
 *
 * @author Erwan Le Huitouze <erwan.lehuitouze@gmail.com>
 *
 */
public class XbmcLibrary implements ILibrary {
	/**
	 * Xbmc node.
	 */
	private IXbmcNode xbmcNode;

	/**
	 * Xbmc json-rpc.
	 */
	private XbmcJson xbmcJson;

	/**
	 * XbmcLibrary constructor.
	 * @param xbmcJson {@link XbmcJson} xbmc json-rpc
	 * @param xbmcNode {@link XbmcNode} xbmc node
	 */
	public XbmcLibrary(XbmcJson xbmcJson, IXbmcNode node) {
		this.xbmcJson = xbmcJson;
		this.xbmcNode = node;
	}

	@Override
	public void handleMediaGetItem(MediaGetItemRequest request,
			MediaGetItemResponse response) {
		this.xbmcNode.logI("Service call MediaGetItem");

		int mediaId = request.getItem().getMediaid();
		MediaType mediaType = request.getItem().getMediatype();

		if (Strings.isNullOrEmpty(mediaType.getValue())) {
		    mediaType = this.xbmcNode.getStateData().getPlayer().getMediatype();
		}

		Media media = null;

		if (mediaId < 0) {
			mediaId = 0;
		}

		if (mediaType.getValue().equals(MediaType.VIDEO_MOVIE)) {
			media = this.getMovie(mediaId);
		} else if (mediaType.getValue().equals(MediaType.VIDEO_TVSHOW_EPISODE)) {
			media = this.getTvshowEpisode(mediaId);
		} else if (mediaType.getValue().equals(MediaType.AUDIO_SONG)) {
		    media = this.getAudioSong(mediaId);
		} else if (mediaType.getValue().equals(MediaType.AUDIO_ALBUM)) {
            media = this.getAudioAlbum(mediaId);
        }

		if (media == null) {
			//We need to send default message.
			media = new Media();
		}

		MediaItem result = this.xbmcNode.getNewMessageInstance(MediaItem._TYPE);

		result.setMediaid(media.getMediaid());
		result.setMediatype(mediaType);
		result.setData(media.toJson());

		response.setItem(result);
	}

	@Override
	public void handleMediaGetItems(MediaGetItemsRequest request,
			MediaGetItemsResponse response) {
		this.xbmcNode.logI("Service call MediaGetItems");

		int mediaId = request.getItem().getMediaid();
		MediaType mediaType = request.getItem().getMediatype();
		List<Media> medias = null;

		if (mediaId < 0) {
			mediaId = 0;
		}

		if (mediaType.getValue().equals(MediaType.VIDEO_MOVIE)) {
		    Movie media = Movie.fromJson(request.getItem().getData());
			medias = this.getMovies(media, null);
		} else if (mediaType.getValue().equals(MediaType.VIDEO_TVSHOW_EPISODE)) {
		    Tvshow media = Tvshow.fromJson(request.getItem().getData());
			medias = this.getTvshows(media, null);
		} else if (mediaType.getValue().equals(MediaType.VIDEO_TVSHOW)) {
		    Tvshow media = Tvshow.fromJson(request.getItem().getData());
			medias = this.getTvshowEpisodes(media, null);
		} else if (mediaType.getValue().equals(MediaType.AUDIO_SONG)) {
		    Song media = Song.fromJson(request.getItem().getData());
		    medias = this.getAudioSongs(media, null);
		} else if (mediaType.getValue().equals(MediaType.AUDIO_ALBUM)) {
		    Album media = Album.fromJson(request.getItem().getData());
            medias = this.getAudioAlbums(media, null);
        }

		if (medias == null) {
			//We need to send default message.
			medias = new ArrayList<Media>();
		}

		List<MediaItem> result = new ArrayList<MediaItem>();

		for (Media media : medias) {
            MediaItem item = this.xbmcNode.getNewMessageInstance(MediaItem._TYPE);

            item.setMediaid(media.getMediaid());
            item.setMediatype(mediaType);
            item.setData(media.toJson());

            result.add(item);
        }

		response.setItems(result);
	}

	/**
	 * Get movie from xbmc json-rpc.
	 * @param mediaId Id of the movie to find
	 * @return {@link Media}
	 */
	private Media getMovie(int mediaId) {
		Media result = null;

		if (mediaId > 0) {
			String[] properties = this.getMovieProperties();
			MovieDetail item = this.xbmcJson.getResult(new GetMovieDetails(
					mediaId, properties));
			result = this.getMovie(item);
		}

		return result;
	}

	/**
	 * Get episode tvshow from xbmc json-rpc.
	 * @param mediaId Id of the tvshow to find
	 * @return {@link Media}
	 */
	private Media getTvshowEpisode(int mediaId) {
		Media result = null;

		if (mediaId > 0) {
			String[] properties = this.getTvshowEpisodeProperties();
			EpisodeDetail item = this.xbmcJson.getResult(new GetEpisodeDetails(
					mediaId, properties));
			result = this.getTvshowEpisode(item);
		}

		return result;
	}

	/**
     * Get song from xbmc json-rpc.
     * @param mediaId Id of the song to find
     * @return {@link Media}
     */
    private Media getAudioSong(int mediaId) {
        Media result = null;

        if (mediaId > 0) {
            String[] properties = this.getAudioSongProperties();
            SongDetail item = this.xbmcJson.getResult(new AudioLibrary.GetSongDetails(
                    mediaId, properties));
            result = this.getAudioSong(item);
        }

        return result;
    }

    /**
     * Get album from xbmc json-rpc.
     * @param mediaId Id of the album to find
     * @return {@link Media}
     */
    private Media getAudioAlbum(int mediaId) {
        Media result = null;

        if (mediaId > 0) {
            String[] properties = this.getAudioAlbumProperties();
            AlbumDetail item = this.xbmcJson.getResult(new AudioLibrary.GetAlbumDetails(
                    mediaId, properties));
            result = this.getAudioAlbum(item);
        }

        return result;
    }

	/**
	 * Get movies from xbmc json-rpc.
	 * @param item {@link Media} with value for filtering
	 * @param limits Limits for the results list
	 * @return List of {@link Media}
	 */
	private List<Media> getMovies(Movie item, Limits limits) {
		List<Media> result = new ArrayList<Media>();
		List<MovieFilter> filters = new ArrayList<MovieFilter>();
		MovieFilter filter = null;
		String title = item.getTitle();

		if (!Strings.isNullOrEmpty(title)) {
			String[] titleParts = title.split(" ");

			for (String part : titleParts) {
				filters.add(this.getMovieFilter(
				        FilterRule.Operator.CONTAINS,
				        part,
				        MovieFilterRule.Field.TITLE));
			}
		}

		if (item.getMediaid() > 0) {
		    filters.add(this.getMovieFilter(
		            FilterRule.Operator.IS,
		            String.valueOf(item.getMediaid()),
		            "movieid"));
		}

		if (item.getYear() > 0) {
		    filters.add(this.getMovieFilter(
		            FilterRule.Operator.IS,
		            String.valueOf(item.getYear()),
		            MovieFilterRule.Field.YEAR));
		}

		if (item.getCast() != null && !item.getCast().isEmpty()) {
		    for (String actor : item.getCast()) {
                if (!Strings.isNullOrEmpty(actor)) {
                    filters.add(this.getMovieFilter(
                            FilterRule.Operator.CONTAINS,
                            actor,
                            MovieFilterRule.Field.ACTOR));
                }
            }
		}

		if (!filters.isEmpty()) {
			filter = new MovieFilter(new MovieFilter.And(filters));
		}

		Sort sort = new Sort(false, "label", "ascending");
		String[] properties = this.getMovieProperties();

		List<MovieDetail> items = this.xbmcJson.getResults(new GetMovies(
				limits, sort, filter, properties));

		if (items != null) {
    		for (MovieDetail itemDetail : items) {
    		    result.add(this.getMovie(itemDetail));
			}
		}

		return result;
	}

	private MovieFilter getMovieFilter(String operator, String value, String field) {
	    return new MovieFilter(new MovieFilterRule(
	            operator,
                new Value(value),
                field));
	}

	/**
	 * Get episodes tvshow from xbmc json-rpc.
	 * @param item {@link Media} with value for filtering
	 * @param limits Limits for the results list
	 * @return List of {@link Media}
	 */
	private List<Media> getTvshowEpisodes(Tvshow item, Limits limits) {
		List<Media> result = new ArrayList<Media>();
		List<EpisodeFilter> filters = new ArrayList<EpisodeFilter>();
		EpisodeFilter filter = null;
		String showtitle = item.getShowtitle();
		int season = item.getSeason();
		int episode = item.getEpisode();
		int playcount = item.getPlaycount();

		if (!Strings.isNullOrEmpty(showtitle)) {
			String[] titleParts = showtitle.split(" ");

			for (String part : titleParts) {
				filters.add(new EpisodeFilter(new EpisodeFilterRule("contains",
						new Value(part), "tvshow")));
			}
		}

		if (season > 0) {
			filters.add(new EpisodeFilter(new EpisodeFilterRule("is",
					new Value(String.valueOf(season)), "season")));
		}

		if (episode > 0) {
			filters.add(new EpisodeFilter(new EpisodeFilterRule("is",
					new Value(String.valueOf(season)), "episode")));
		}

		if (playcount > 0) {
			filters.add(new EpisodeFilter(new EpisodeFilterRule("is",
					new Value(String.valueOf(playcount)), "playcount")));
		}

		if (!filters.isEmpty()) {
			filter = new EpisodeFilter(new EpisodeFilter.And(filters));
		}

		String[] properties = this.getTvshowEpisodeProperties();
		List<EpisodeDetail> items = this.xbmcJson.getResults(new GetEpisodes(
				limits, filter, properties));

		if (items != null) {
			for (EpisodeDetail itemDetail : items) {
				result.add(this.getTvshowEpisode(itemDetail));
			}
		}

		return result;
	}

	/**
	 * Get tvshows from xbmc json-rpc
	 * @param item {@link Media} with value for filtering
	 * @param limits Limits for the results list
	 * @return List of {@link Media}
	 */
	private List<Media> getTvshows(Media item, Limits limits) {
		List<Media> result = new ArrayList<Media>();
		List<TVShowFilter> filters = new ArrayList<TVShowFilter>();
		TVShowFilter filter = null;
		String title = item.getTitle();

		if (!Strings.isNullOrEmpty(title)) {
			String[] titleParts = title.split(" ");

			for (String part : titleParts) {
				filters.add(new TVShowFilter(new TVShowFilterRule("contains",
						new Value(part), "title")));
			}
		}

		if (!filters.isEmpty()) {
			filter = new TVShowFilter(new TVShowFilter.And(filters));
		}

		Sort sort = new Sort(false, "label", "ascending");
		String[] properties = this.getTvshowProperties();

		List<TVShowDetail> items = this.xbmcJson.getResults(new GetTVShows(
				limits, sort, filter, properties));

		if (items != null) {
    		for (TVShowDetail itemDetail : items) {
    			result.add(this.getTvshow(itemDetail));
    		}
		}

		return result;
	}

	/**
     * Get songs from xbmc json-rpc.
     * @param item {@link Media} with value for filtering
     * @param limits Limits for the results list
     * @return List of {@link Media}
     */
    private List<Media> getAudioSongs(Media item, Limits limits) {
        List<Media> result = new ArrayList<Media>();
        List<SongFilter> filters = new ArrayList<SongFilter>();
        SongFilter filter = null;
        String showtitle = item.getTitle();
        int playcount = item.getPlaycount();

        if (!Strings.isNullOrEmpty(showtitle)) {
            String[] titleParts = showtitle.split(" ");

            for (String part : titleParts) {
                filters.add(new SongFilter(new SongFilterRule("contains",
                        new Value(part), "title")));
            }
        }

        if (playcount > 0) {
            filters.add(new SongFilter(new SongFilterRule("is",
                    new Value(String.valueOf(playcount)), "playcount")));
        }

        if (!filters.isEmpty()) {
            filter = new SongFilter(new SongFilter.And(filters));
        }

        String[] properties = this.getAudioSongProperties();
        List<SongDetail> items = this.xbmcJson.getResults(new AudioLibrary.GetSongs(
                limits, filter, properties));

        if (items != null) {
            for (SongDetail itemDetail : items) {
                result.add(this.getAudioSong(itemDetail));
            }
        }

        return result;
    }

    /**
     * Get albums from xbmc json-rpc.
     * @param item {@link Media} with value for filtering
     * @param limits Limits for the results list
     * @return List of {@link Media}
     */
    private List<Media> getAudioAlbums(Album item, Limits limits) {
        List<Media> result = new ArrayList<Media>();
        List<AlbumFilter> filters = new ArrayList<AlbumFilter>();
        AlbumFilter filter = null;
        String showtitle = item.getAlbum();
        int playcount = item.getPlaycount();

        if (!Strings.isNullOrEmpty(showtitle)) {
            String[] titleParts = showtitle.split(" ");

            for (String part : titleParts) {
                filters.add(new AlbumFilter(new AlbumFilterRule("contains",
                        new Value(part), "album")));
            }
        }

        if (playcount > 0) {
            filters.add(new AlbumFilter(new AlbumFilterRule("is",
                    new Value(String.valueOf(playcount)), "playcount")));
        }

        if (!filters.isEmpty()) {
            filter = new AlbumFilter(new AlbumFilter.And(filters));
        }

        String[] properties = this.getAudioAlbumProperties();
        List<AlbumDetail> items = this.xbmcJson.getResults(new AudioLibrary.GetAlbums(
                limits, filter, properties));

        if (items != null) {
            for (AlbumDetail itemDetail : items) {
                result.add(this.getAudioAlbum(itemDetail));
            }
        }

        return result;
    }

	/**
	 *
	 * @return List of basic media item properties
	 */
	private String[] getItemProperties() {
		String[] properties = { "title", "plot", "votes", "rating", "writer",
				"playcount", "runtime", "director", "originaltitle", "cast",
				"streamdetails", "lastplayed", "fanart", "thumbnail", "file",
				"resume", "dateadded"/*, "art"*/ };

		return properties;
	}

	/**
     *
     * @return List of basic media audio item properties
     */
    private String[] getAudioItemProperties() {
//        String[] properties = { "title", "votes", "rating", "writer",
//                "playcount", "runtime", "director", "originaltitle", "cast",
//                "streamdetails", "lastplayed", "fanart", "thumbnail", "file",
//                "resume", "dateadded"/*, "art"*/ };

        String[] properties = { "genre", "artist", "artistid", "displayartist",
                "genreid", "musicbrainzalbumartistid", "musicbrainzalbumid",
                "rating", "title", "year", "fanart", "thumbnail",
                "playcount" };

        return properties;
    }

	/**
	 *
	 * @return List of movie item properties
	 */
	private String[] getMovieProperties() {
		String[] properties = { "genre", "country", "year", "trailer",
				"tagline", "plotoutline", "studio", "mpaa", "imdbnumber",
				"set", "showlink", "top250", "sorttitle", "setid", "tag" };

		return ObjectArrays.concat(this.getItemProperties(), properties,
				String.class);
	}

	/**
	 *
	 * @return List of tvshow item properties
	 */
	private String[] getTvshowProperties() {
		String[] properties = { "genre", "year", "plot", "studio", "mpaa",
				"episode", "imdbnumber", "premiered", "sorttitle",
				"episodeguide", "season", "watchedepisodes", "tag" };

		return ObjectArrays.concat(this.getItemProperties(), properties,
				String.class);
	}

	/**
	 *
	 * @return List of episode tvshow item properties
	 */
	private String[] getTvshowEpisodeProperties() {
		String[] properties = { "firstaired", "art", "productioncode",
				"season", "episode", "showtitle", "tvshowid", "uniqueid" };

		return ObjectArrays.concat(this.getItemProperties(), properties,
				String.class);
	}

	/**
     *
     * @return List of song item properties
     */
    private String[] getAudioSongProperties() {
        String[] properties =  { "comment", "disc", "duration",
                "file", "lastplayed", "lyrics", "musicbrainzartistid",
                "musicbrainztrackid", "track", "album", "albumartist",
                "albumartistid", "albumid",  };

        return ObjectArrays.concat(this.getAudioItemProperties(), properties,
                String.class);
    }

    /**
     *
     * @return List of album item properties
     */
    private String[] getAudioAlbumProperties() {
        String[] properties =  { "description", "mood", "style",
              "theme", "type"};

        return ObjectArrays.concat(this.getAudioItemProperties(), properties,
                String.class);
    }

	/**
	 * Convert {@link ItemDetail} to {@link Media}.
	 * @param media {@link ItemDetail} rpc
	 * @return Get {@link Media} from {@link ItemDetail} rpc
	 */
	private void getItemBase(Media video, ItemDetail media) {

		if (media != null) {
			video.setTitle(media.title);
			video.setPlot(media.plot);
			video.setPlaycount(media.playcount);
			// Video.Cast cast
			// Video.Streams streamdetails
			video.setLastplayed(media.lastplayed);
			video.setFanart(XbmcUtils.getImageUrl(media.fanart));
			video.setThumbnail(XbmcUtils.getImageUrl(media.thumbnail));
			video.setFile(media.file);
			// #video.resume.position = media["resume"]["position"]
			// #video.resume.total = media["resume"]["total"]
			video.setDateadded(media.dateadded);
			// #Media.Artwork art
		}
	}

	/**
	 * Convert {@link FileDetail} to {@link Media}.
	 * @param file {@link FileDetail} rpc
	 * @return Get {@link Media} from {@link FileDetail} rpc
	 */
	private void getItem(Media media, FileDetail file) {
		if (file != null) {
			this.getItemBase(media, file);
			media.setRuntime(file.runtime);
			media.setDirector(file.director);
		}
	}

	/**
	 * Convert {@link MovieDetail} to {@link Media}.
	 * @param media {@link MovieDetail} rpc
	 * @return Get {@link Media} from {@link MovieDetail} rpc
	 */
	private Media getMovie(MovieDetail media) {
		Movie movie = new Movie();

		if (media != null) {
		    this.getItem(movie, media);

			movie.setMediaid(media.movieid);
			movie.setSetid(media.setid);
			movie.setSet(media.set);
			movie.setPlotoutline(media.plotoutline);
			movie.setSorttitle(media.sorttitle);
			movie.setYear(media.year);
			movie.setShowlink(media.showlink);
			movie.setTop250(media.top250);
			movie.setTrailer(media.trailer);
			movie.setCountry(media.country);
			movie.setStudio(media.studio);
			movie.setGenre(media.genre);
			movie.setTag(media.tag);
			movie.setTagline(media.tagline);
			movie.setImdbnumber(media.imdbnumber);
			movie.setMpaa(media.mpaa);
			movie.setVotes(media.votes);
			movie.setRating(media.rating);
			movie.setWriter(media.writer);
			movie.setOriginaltitle(media.originaltitle);
		}

		return movie;
	}

	/**
	 * Convert {@link TVShowDetail} to {@link Media}.
	 * @param media {@link TVShowDetail} rpc
	 * @return Get {@link Media} from {@link TVShowDetail} rpc
	 */
	private Media getTvshow(TVShowDetail media) {
		Tvshow tvshow = new Tvshow();

		if (media != null) {
			this.getItemBase(tvshow, media);

			tvshow.setMediaid(media.tvshowid);
			tvshow.setEpisode(media.episode);
			tvshow.setSeason(media.season);
		}

		return tvshow;
	}

	/**
	 * Convert {@link EpisodeDetail} to {@link Media}.
	 * @param media {@link EpisodeDetail} rpc
	 * @return Get {@link Media} from {@link EpisodeDetail} rpc
	 */
	private Media getTvshowEpisode(EpisodeDetail media) {
		Tvshow tvshow = new Tvshow();

		if (media != null) {
			this.getItem(tvshow, media);

			tvshow.setMediaid(media.episodeid);
			tvshow.setEpisode(media.episode);
			tvshow.setSeason(media.season);
			tvshow.setTvshowid(media.tvshowid);
			// video.setuniqueid = media["uniqueid"]
			tvshow.setShowtitle(media.showtitle);
			tvshow.setFirstaired(media.firstaired);
			tvshow.setProductioncode(media.productioncode);
			tvshow.setVotes(media.votes);
			tvshow.setRating(media.rating);
			tvshow.setWriter(media.writer);
			tvshow.setOriginaltitle(media.originaltitle);
		}

		return tvshow;
	}

	/**
     * Convert {@link SongDetail} to {@link Media}.
     * @param media {@link SongDetail} rpc
     * @return Get {@link Media} from {@link SongDetail} rpc
     */
    private Media getAudioSong(SongDetail media) {
        Song song = new Song();

        if (media != null) {
//            song = this.getItem(media);
//
            song.setMediaid(media.songid);
            song.setTitle(media.title);
            song.setFanart(media.fanart);
            song.setThumbnail(media.thumbnail);
            song.setFile(media.file);
            song.setAlbum(media.album);
            //song.set media.albumartist
            //song.set media.albumartistid
            song.setAlbumid(media.albumid);
            song.setArtist(media.artist);
            //song.setArtistid(media.artistid);
            song.setComment(media.comment);
            song.setDisc(media.disc);
            song.setDisplayartist(media.displayartist);
            song.setDuration(media.duration);
            song.setGenre(media.genre);
            //song.set media.genreid
            song.setLabel(media.label);
            song.setLastplayed(media.lastplayed);
            song.setLyrics(media.lyrics);
            song.setMusicbrainzalbumartistid(media.musicbrainzalbumartistid);
            song.setMusicbrainzalbumid(media.musicbrainzalbumid);
            song.setMusicbrainzartistid(media.musicbrainzartistid);
            song.setMusicbrainztrackid(media.musicbrainztrackid);
            song.setPlaycount(media.playcount);
            song.setRating(media.rating);
            song.setTrack(media.track);
            song.setYear(media.year);
            song.setRating(media.rating);

//            video.getTvshow().setEpisode(media.episode);
//            video.getTvshow().setSeason(media.season);
//            video.getTvshow().setTvshowid(media.tvshowid);
//            video.getTvshow().setuniqueid = media["uniqueid"]
//            video.getTvshow().setShowtitle(media.showtitle);
//            video.getTvshow().setFirstaired(media.firstaired);
//            video.getTvshow().setProductioncode(media.productioncode);
//            video.setVotes(media.votes);
//            video.setWriter(media.writer);
//            video.setOriginaltitle(media.originaltitle);
        }

        return song;
    }

    /**
     * Convert {@link AlbumDetail} to {@link Media}.
     * @param media {@link AlbumDetail} rpc
     * @return Get {@link Media} from {@link AlbumDetail} rpc
     */
    private Media getAudioAlbum(AlbumDetail media) {
        Album album = new Album();

        if (media != null) {
//            album = this.getItem(media);
//
            album.setMediaid(media.albumid);
            album.setTitle(media.title);
            album.setLabel(media.label);
            album.setAlbum(media.label);
            album.setFanart(media.fanart);
            album.setThumbnail(media.thumbnail);
            //album.setAlbum(media.albumlabel);
            album.setDescription(media.description);
            album.setMood(media.mood);
            album.setStyle(media.style);
            album.setTheme(media.theme);
            album.setType(media.type);
            //album.set media.albumartist
            //album.set media.albumartistid
            album.setAlbumid(media.albumid);
            album.setArtist(media.artist);
            //album.setArtistid(media.artistid);
            album.setDisplayartist(media.displayartist);
            album.setGenre(media.genre);
            //album.set media.genreid
            album.setLabel(media.label);
            album.setMusicbrainzalbumartistid(media.musicbrainzalbumartistid);
            album.setMusicbrainzalbumid(media.musicbrainzalbumid);
            album.setPlaycount(media.playcount);
            album.setRating(media.rating);
            album.setYear(media.year);
            album.setRating(media.rating);

//            video.getTvshow().setEpisode(media.episode);
//            video.getTvshow().setSeason(media.season);
//            video.getTvshow().setTvshowid(media.tvshowid);
//            // video.getTvshow().setuniqueid = media["uniqueid"]
//            video.getTvshow().setShowtitle(media.showtitle);
//            video.getTvshow().setFirstaired(media.firstaired);
//            video.getTvshow().setProductioncode(media.productioncode);
//            video.setVotes(media.votes);
//            video.setWriter(media.writer);
//            video.setOriginaltitle(media.originaltitle);
        }

        return album;
    }
}
