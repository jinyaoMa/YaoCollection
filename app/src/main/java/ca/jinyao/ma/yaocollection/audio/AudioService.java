package ca.jinyao.ma.yaocollection.audio;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.InputStream;

import ca.jinyao.ma.yaocollection.R;
import ca.jinyao.ma.yaocollection.audio.widgets.AboutWidget;
import ca.jinyao.ma.yaocollection.audio.widgets.AlbumDetailWidget;
import ca.jinyao.ma.yaocollection.audio.widgets.ArtistDetailWidget;
import ca.jinyao.ma.yaocollection.audio.widgets.ControllerWidget;
import ca.jinyao.ma.yaocollection.audio.widgets.LyricWidget;
import ca.jinyao.ma.yaocollection.audio.widgets.PlaylistBrowserWidget;
import ca.jinyao.ma.yaocollection.audio.widgets.PlaylistDetailWidget;
import ca.jinyao.ma.yaocollection.audio.widgets.QuickAccessWidget;
import ca.jinyao.ma.yaocollection.audio.widgets.SearchBrowserWidget;
import ca.jinyao.ma.yaocollection.audio.widgets.SettingWidget;
import ca.jinyao.ma.yaocollection.audio.cachers.ImageCacher;
import ca.jinyao.ma.yaocollection.audio.cachers.LyricCacher;
import ca.jinyao.ma.yaocollection.audio.components.Album;
import ca.jinyao.ma.yaocollection.audio.components.Artist;
import ca.jinyao.ma.yaocollection.audio.components.Lyric;
import ca.jinyao.ma.yaocollection.audio.components.Playlist;
import ca.jinyao.ma.yaocollection.audio.components.Song;
import ca.jinyao.ma.yaocollection.audio.components.SongList;
import ca.jinyao.ma.yaocollection.audio.cores.AudioConfig;
import ca.jinyao.ma.yaocollection.audio.cores.AudioPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.MODE_NORMAL;

/**
 * Class AudioService
 * <p>
 * Permission:
 * android.permission.INTERNET
 * android.permission.WRITE_EXTERNAL_STORAGE
 * android.permission.READ_EXTERNAL_STORAGE
 * android.permission.SYSTEM_ALERT_WINDOW
 * android.permission.SYSTEM_OVERLAY_WINDOW
 * android.permission.RECORD_AUDIO
 * android.permission.MODIFY_AUDIO_SETTINGS
 * <p>
 * create by jinyaoMa 0025 2018/8/25 15:38
 */
public class AudioService extends Service {
    public final int ICON_RES_ID = R.mipmap.ic_music;
    public final String NAME = "Audio Online\n" +
            "QQ | 163";
    public final String AUTHOR = "By jinyaoMa";
    public final String VERSION = "VERSION\n" +
            "practice 0.1";
    public final String SOURCE = "SOURCE\n" +
            "https://github.com/jinyaoMa/YaoCollection";
    public final String THANKS = "DEPENDENCIES\n" +
            "Bilibili's ijkplayer | 0.8.8\n" +
            "Jonathan Hedley's jsoup | 1.11.3\n" +
            "Google's Gson | 2.8.5\n" +
            "Jake Wharton's ButterKnife | 8.8.1\n" +
            "Apache's commons-lang3 | 3.8\n" +
            "\nTHANKS!";

    public AudioService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public final String TAG = "AudioService";

    private QuickAccessWidget quickAccessWidget;
    private PlaylistBrowserWidget playlistBrowserWidget;
    private SearchBrowserWidget searchBrowserWidget;

    private PlaylistDetailWidget playlistDetailWidget;
    private ArtistDetailWidget artistDetailWidget;
    private AlbumDetailWidget albumDetailWidget;

    private ControllerWidget controllerWidget;
    private LyricWidget lyricWidget;
    private SettingWidget settingWidget;
    private AboutWidget aboutWidget;

    private AudioPlayer audioPlayer;

    private final String PREF_AUDIO = "pref_audio";
    private final String LAST_X = "lastX";
    private final String LAST_Y = "lastY";
    private final String LAST_X_CONTROL = "lastXControl";
    private final String LAST_Y_CONTROL = "lastYControl";
    private final String LAST_MODE = "lastMode";
    private final String LAST_SONGLIST = "lastSonglist";
    private final String LAST_SONG_INDEX = "lastSongIndex";
    private final String LAST_PROXY = "lastProxy";
    private int lastX;
    private int lastY;
    private int lastXControl;
    private int lastYControl;
    private int lastMode;
    private SongList lastSonglist;
    private int lastSongIndex;
    private Boolean lastProxy;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder builder;
    private Notification notification;
    private Bitmap currentNotificationIcon;
    private String currentNotificationTitle;
    private String currentNotificationText;
    private final String CHANNEL_ID = "AudioService_1";
    private final String CHANNEL_NAME = "AudioService_1";
    private final int NOTIFICATION_ID = 101;

    private void setupNotification() {
        mNotificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        mNotificationManager.createNotificationChannel(notificationChannel);
        builder = new NotificationCompat.Builder(this, CHANNEL_ID);
    }

    private void setNotifyMessage(Bitmap cover, String title, String artist) {
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(cover)
                .setContentTitle(title)
                .setContentText(artist);
        notification = builder.build();
        notification.flags = NotificationCompat.FLAG_ONGOING_EVENT | NotificationCompat.FLAG_ONLY_ALERT_ONCE;

        currentNotificationIcon = cover;
        currentNotificationTitle = title;
        currentNotificationText = artist;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        lastX = -1;
        lastY = -1;
        lastXControl = -1;
        lastYControl = -1;
        lastMode = MODE_NORMAL;
        lastSonglist = new SongList();
        lastSongIndex = 0;
        lastProxy = false;

        AudioConfig.trustEveryone();
        setDefaultCache();

        setupNotification();
        setNotifyMessage(R.mipmap.ic_launcher, TAG, TAG);
        showNotification();
        startForeground(NOTIFICATION_ID, notification);

        sharedPreferences = getSharedPreferences(PREF_AUDIO, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        quickAccessWidget = new QuickAccessWidget(this);
        quickAccessWidget.setForeground(R.mipmap.ic_music);
        quickAccessWidget.setBackground(R.mipmap.ic_background);

        playlistBrowserWidget = new PlaylistBrowserWidget(this);
        searchBrowserWidget = new SearchBrowserWidget(this);

        playlistDetailWidget = new PlaylistDetailWidget(this);
        artistDetailWidget = new ArtistDetailWidget(this);
        albumDetailWidget = new AlbumDetailWidget(this);

        controllerWidget = new ControllerWidget(this);
        lyricWidget = new LyricWidget(this);
        settingWidget = new SettingWidget(this);
        aboutWidget = new AboutWidget(this);

        aboutWidget.setInfo(ICON_RES_ID, NAME, VERSION, AUTHOR, SOURCE, THANKS);

        quickAccessWidget.create();

        setListeners();
    }

    private void setListeners() {
        quickAccessWidget.setOnClickListener(new QuickAccessWidget.OnClickListener() {
            @Override
            public void onClick() {
                loadPreference();

                if (lastX < 0 && lastY < 0) {
                    playlistBrowserWidget.create();
                } else {
                    playlistBrowserWidget.create(lastX, lastY);
                }

                if (lastXControl < 0 && lastYControl < 0) {
                    controllerWidget.create();
                } else {
                    controllerWidget.create(lastXControl, lastYControl);
                }
                if (!lastSonglist.isEmpty()) {
                    if (lastSongIndex >= 0) {
                        controllerWidget.setSonglist(lastSonglist);
                        controllerWidget.setSongInfo(lastSongIndex);
                        createAudioPlayerOnce(lastSonglist);
                    }
                }

                settingWidget.setProxyEnable(lastProxy);
            }
        });

        playlistBrowserWidget.setListener(new PlaylistBrowserWidget.Listener() {
            @Override
            public void onPlaylistClick(Playlist playlist) {
                playlistDetailWidget.create(playlist);
            }

            @Override
            public void onSwitchClick() {
                searchBrowserWidget.create(playlistBrowserWidget.getPositionX(), playlistBrowserWidget.getPositionY());
                playlistBrowserWidget.remove();
                removeAllSubWidgets();
                controllerWidget.create();
            }

            @Override
            public void onCloseWindow(int x, int y) {
                removeAllSubWidgets();

                lastX = x;
                lastY = y;
                lastXControl = controllerWidget.getPositionX();
                lastYControl = controllerWidget.getPositionY();

                recordPreference();
            }
        });

        searchBrowserWidget.setListener(new SearchBrowserWidget.Listener() {
            @Override
            public void onSongClick(Song song) {
                play(song);
            }

            @Override
            public void onAlbumClick(Album album) {
                albumDetailWidget.create(album);
            }

            @Override
            public void onArtistClick(Artist artist) {
                artistDetailWidget.create(artist);
            }

            @Override
            public void onPlaylistClick(Playlist playlist) {
                playlistDetailWidget.create(playlist);
            }

            @Override
            public void onSwitchClick() {
                playlistBrowserWidget.create(searchBrowserWidget.getPositionX(), searchBrowserWidget.getPositionY());
                searchBrowserWidget.remove();
                removeAllSubWidgets();
                controllerWidget.create();
            }

            @Override
            public void onCloseWindow(int x, int y) {
                removeAllSubWidgets();

                lastX = x;
                lastY = y;
                lastXControl = controllerWidget.getPositionX();
                lastYControl = controllerWidget.getPositionY();

                recordPreference();
            }
        });

        playlistDetailWidget.setListener(new PlaylistDetailWidget.Listener() {
            @Override
            public void onPlayAll(Playlist playlist) {
                play(playlist.songList);
            }

            @Override
            public void onPlay(Song song) {
                play(song);
            }
        });

        artistDetailWidget.setListener(new ArtistDetailWidget.Listener() {
            @Override
            public void onPlayAll(SongList songs) {
                play(songs);
            }

            @Override
            public void onPlay(Song song) {
                play(song);
            }

            @Override
            public void onAlbumClick(Album album) {
                albumDetailWidget.create(album);
            }
        });

        albumDetailWidget.setListener(new AlbumDetailWidget.Listener() {
            @Override
            public void onPlayAll(SongList songs) {
                play(songs);
            }

            @Override
            public void onPlay(Song song) {
                play(song);
            }

            @Override
            public void onArtistClick(Artist artist) {
                artistDetailWidget.create(artist);
            }
        });

        controllerWidget.setListener(new ControllerWidget.Listener() {
            @Override
            public void onProgressUpdate(int newProgress, int max) {
                if (audioPlayer != null) {
                    audioPlayer.seekTo(newProgress);
                }
            }

            @Override
            public void onSettingClick(Boolean isSettingOpen) {
                if (isSettingOpen) {
                    settingWidget.remove();
                } else {
                    settingWidget.create();
                }
                controllerWidget.setSettingState(!isSettingOpen);
            }

            @Override
            public void onAboutClick(Boolean isAboutOpen) {
                if (isAboutOpen) {
                    aboutWidget.remove();
                } else {
                    aboutWidget.create();
                }
                controllerWidget.setAboutState(!isAboutOpen);
            }

            @Override
            public void onLyricClick(int ref, String id, Boolean isLyricOpen) {
                if (ref < 0 || id.isEmpty()) {
                    return;
                }

                lyricWidget.create();
                controllerWidget.setLyricState(true);
            }

            @Override
            public void onTimerClick(Boolean isTimerOpen) {
                if (audioPlayer != null) {
                    if (!isTimerOpen && audioPlayer.stop(20 * 60)) {
                        controllerWidget.setTimerState(true);
                    } else {
                        audioPlayer.stopTimer();
                        controllerWidget.setTimerState(false);
                    }
                }
            }

            @Override
            public void onPlayClick() {
                if (audioPlayer != null) {
                    audioPlayer.togglePlay();
                }
            }

            @Override
            public void onPrevClick() {
                if (audioPlayer != null) {
                    audioPlayer.previous();
                }
            }

            @Override
            public void onNextClick() {
                if (audioPlayer != null) {
                    audioPlayer.next(true);
                }
            }

            @Override
            public void onModeClick() {
                if (audioPlayer != null) {
                    audioPlayer.toggleMode();
                }
            }

            @Override
            public void onListItemClick(int index, Song song) {
                if (audioPlayer != null) {
                    audioPlayer.play(index);
                }
            }
        });

        lyricWidget.setListener(new LyricWidget.Listener() {
            @Override
            public void onClose() {
                controllerWidget.setLyricState(false);
            }
        });

        settingWidget.setListener(new SettingWidget.Listener() {
            @Override
            public void onProxyChange(Boolean isOn) {
                lastProxy = isOn;
            }
        });

        aboutWidget.setListener(new AboutWidget.Listener() {
            @Override
            public void onClose() {
                controllerWidget.setAboutState(false);
            }
        });
    }

    private void play(SongList songs) {
        if (createAudioPlayerOnce(songs)) {
            audioPlayer.togglePlay();
        } else {
            audioPlayer.changePlaylistAndPlay(songs);
        }
    }

    private void play(Song song) {
        SongList songs = new SongList();
        songs.add(song);
        if (createAudioPlayerOnce(songs)) {
            audioPlayer.togglePlay();
        } else {
            audioPlayer.addNewSongAndPlay(song);
        }
    }

    private Boolean createAudioPlayerOnce(SongList songs) {
        if (audioPlayer == null) {
            try {
                if (lastSongIndex < songs.size()) {
                    audioPlayer = new AudioPlayer(songs, lastSongIndex, lastMode);
                } else {
                    audioPlayer = new AudioPlayer(songs, 0, lastMode);
                }
                audioPlayer.setListener(new AudioPlayer.PlayingListener() {
                    @Override
                    public void onBufferingUpdate(IjkMediaPlayer ijkMediaPlayer, int percentage) {
                        controllerWidget.setLoading(percentage);
                    }

                    @Override
                    public void onSongChanged(IjkMediaPlayer ijkMediaPlayer, final Song song, int currentIndex) {
                        recordPreference();
                        lastSongIndex = currentIndex;
                        controllerWidget.setSongInfo(currentIndex);
                        ImageCacher.getImage(song.getCoverPath(), new ImageCacher.ImageCacheListener() {
                            @Override
                            public void onCompleted(Bitmap bitmap, String path) {
                                setNotifyMessage(bitmap, song.songTitle, song.getArtists().getNameString());
                                showNotification();
                            }
                        });
                        LyricCacher.getLyric(song.getReference(), song.songId, new LyricCacher.LyricCacheListener() {
                            @Override
                            public void onCompleted(Lyric original, Lyric translate) {
                                lyricWidget.setLyric(original);
                            }
                        });
                    }

                    @Override
                    public void onSongProcessing(IjkMediaPlayer ijkMediaPlayer, long duration, long currentPosition) {
                        controllerWidget.setProgress(currentPosition, duration);
                        lyricWidget.goToTimeline(currentPosition);
                    }

                    @Override
                    public void onPlaylistChanged(IjkMediaPlayer ijkMediaPlayer, Song song) {
                        controllerWidget.addSong(song);
                        if (audioPlayer != null) {
                            lastSonglist = new Gson().fromJson(audioPlayer.getSongListJson(), SongList.class);
                        }
                    }

                    @Override
                    public void onPlaylistChanged(IjkMediaPlayer ijkMediaPlayer, SongList songlist) {
                        controllerWidget.setSonglist(songlist);
                    }

                    @Override
                    public void onPlayStateChanged(IjkMediaPlayer ijkMediaPlayer, Boolean isPlay) {
                        if (isPlay) {
                            audioPlayer.setVisualizer(quickAccessWidget.getVisualizerView());
                        }

                        controllerWidget.setPlayState(isPlay);
                    }

                    @Override
                    public void onNetworkStart(IjkMediaPlayer ijkMediaPlayer) {
                        controllerWidget.setLoadingNetwork(true);
                    }

                    @Override
                    public void onNetworkEnd(IjkMediaPlayer ijkMediaPlayer) {
                        controllerWidget.setLoadingNetwork(false);
                    }

                    @Override
                    public void onModeChanged(IjkMediaPlayer ijkMediaPlayer, int mode) {
                        controllerWidget.setModeState(mode);
                        lastMode = mode;
                    }

                    @Override
                    public void onStopping(IjkMediaPlayer ijkMediaPlayer, int secondLeft, Boolean isStopped) {
                        int second = secondLeft % 60;
                        int minute = secondLeft / 60;
                        setNotifyMessage(R.drawable.ic_action_timer, currentNotificationTitle, String.format("%02d:%02d", minute, second));
                        showNotification();
                        if (isStopped) {
                            controllerWidget.setTimerState(false);
                        }
                    }

                    @Override
                    public void onError(IjkMediaPlayer ijkMediaPlayer, int code) {
                        Log.e(TAG, "Error code: " + code);
                    }
                });
                audioPlayer.refreshListner();

                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void removeAllSubWidgets() {
        if (playlistDetailWidget != null) {
            playlistDetailWidget.remove();
        }
        if (artistDetailWidget != null) {
            artistDetailWidget.remove();
        }
        if (albumDetailWidget != null) {
            albumDetailWidget.remove();
        }
        if (controllerWidget != null) {
            controllerWidget.remove();
        }
        if (settingWidget != null) {
            settingWidget.remove();
        }
        if (aboutWidget != null) {
            aboutWidget.remove();
        }
        controllerWidget.setSettingState(false);
        controllerWidget.setAboutState(false);
    }

    private void loadPreference() {
        lastX = sharedPreferences.getInt(LAST_X, -1);
        lastY = sharedPreferences.getInt(LAST_Y, -1);
        lastXControl = sharedPreferences.getInt(LAST_X_CONTROL, -1);
        lastYControl = sharedPreferences.getInt(LAST_Y_CONTROL, -1);
        lastMode = sharedPreferences.getInt(LAST_MODE, MODE_NORMAL);
        lastSonglist = new Gson().fromJson(sharedPreferences.getString(LAST_SONGLIST, "[]"), SongList.class);
        lastSongIndex = sharedPreferences.getInt(LAST_SONG_INDEX, 0);
        lastProxy = sharedPreferences.getBoolean(LAST_PROXY, false);
    }

    private void recordPreference() {
        editor.putInt(LAST_X, lastX);
        editor.putInt(LAST_Y, lastY);
        editor.putInt(LAST_X_CONTROL, lastXControl);
        editor.putInt(LAST_Y_CONTROL, lastYControl);
        editor.putInt(LAST_SONG_INDEX, lastSongIndex);
        editor.putInt(LAST_MODE, lastMode);
        editor.putBoolean(LAST_PROXY, lastProxy);
        if (audioPlayer != null) {
            editor.putString(LAST_SONGLIST, audioPlayer.getSongListJson());
        }
        editor.commit();
    }

    private void setNotifyMessage(int resId, String title, String artist) {
        InputStream inputStream = getResources().openRawResource(resId);
        setNotifyMessage(BitmapFactory.decodeStream(inputStream), title, artist);
    }

    private void showNotification() {
        if (notification != null) {
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private void clearNotification(Bitmap cover, String title, String artist) {
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    private void setDefaultCache() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            AudioConfig.setLyricCachePath(AudioConfig.lyricCachePath);
            AudioConfig.setImageCachePath(AudioConfig.imageCachePath);
            AudioConfig.setSongCachePath(AudioConfig.songCachePath);
            AudioConfig.setLyricCacheLimit(1000);
            AudioConfig.setImageCacheLimit(1000);
            AudioConfig.setSongCacheLimit(100);
        }
    }

    @Override
    public void onDestroy() {
        if (quickAccessWidget != null) {
            quickAccessWidget.remove();
        }
        if (playlistBrowserWidget != null) {
            playlistBrowserWidget.remove();
        }
        if (searchBrowserWidget != null) {
            searchBrowserWidget.remove();
        }
        if (lyricWidget != null) {
            lyricWidget.remove();
        }

        removeAllSubWidgets();
        recordPreference();

        super.onDestroy();
    }

    private static Boolean isServiceOn = false;
    private static final String STOP_COMMAND = "stop";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getBooleanExtra(STOP_COMMAND, false)) {
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public static Boolean checkPermissionAndStart(Activity activity, int requestCode, int grantResult) {
        if (grantResult == PackageManager.PERMISSION_DENIED) {
            return false;
        }
        return checkPermissionAndStart(activity, requestCode, true);
    }

    public static Boolean checkPermissionAndStart(Activity activity, int requestCode, Boolean checkOverlay) {
        if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
        } else if (activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            activity.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, requestCode);
        } else if (!Settings.canDrawOverlays(activity)) {
            if (checkOverlay) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                activity.startActivityForResult(intent, requestCode);
                Toast.makeText(activity.getApplicationContext(), "AudioService - Request Overlay Permission", Toast.LENGTH_LONG).show();
            }
        } else {
            Intent intent = new Intent(activity, AudioService.class);
            activity.startForegroundService(intent);
            isServiceOn = true;
            return true;
        }
        return false;
    }

    public static void stop(Activity activity) {
        if (isServiceOn) {
            Intent intent = new Intent(activity, AudioService.class);
            intent.putExtra(STOP_COMMAND, true);
            activity.startForegroundService(intent);
            isServiceOn = false;
        }
    }

    public static Boolean isRunning() {
        return isServiceOn;
    }
}
