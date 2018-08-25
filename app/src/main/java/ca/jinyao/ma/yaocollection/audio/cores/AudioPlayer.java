package ca.jinyao.ma.yaocollection.audio.cores;

import android.annotation.SuppressLint;
import android.media.audiofx.Visualizer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Stack;

import ca.jinyao.ma.yaocollection.audio.animators.AudioVolumeAnimator;
import ca.jinyao.ma.yaocollection.audio.cachers.SongCacher;
import ca.jinyao.ma.yaocollection.audio.components.Playlist;
import ca.jinyao.ma.yaocollection.audio.components.Song;
import ca.jinyao.ma.yaocollection.audio.components.SongList;
import ca.jinyao.ma.yaocollection.audio.views.VisualizerView;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.DEFAULT;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.MODE_NORMAL;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.MODE_RANDOM;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.MODE_REPEAT;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.MODE_REPEAT_LIST;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.NONE;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.SECOND;
import static ca.jinyao.ma.yaocollection.audio.cores.AudioConfig.headersFor;

/**
 * Class AudioPlayer
 * create by jinyaoMa 0010 2018/8/10 0:34
 */
public class AudioPlayer {
    // Class variables
    private SongList songlist;
    private Stack<Integer> previousIndex;
    private int currentIndex;
    private int currentMode;
    private Boolean isReady;
    private int currentBufferPercentage;
    private int currentStoppingTimeLeft;

    // Animators
    private AudioVolumeAnimator audioVolumeAnimator;

    // Visualizer
    private Visualizer visualizer;

    // IjkMediaPlayer
    private IjkMediaPlayer ijkMediaPlayer;
    private Listener listener;
    private PlayingListener playingListener;
    @SuppressLint("HandlerLeak")
    private Handler processingHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.obj instanceof Song) {
                Song targetSong = (Song) msg.obj;
                Song currentSong = songlist.get(currentIndex);
                if (targetSong.songId.equals(currentSong.songId) &&
                        targetSong.getReference() == currentSong.getReference() &&
                        ijkMediaPlayer != null &&
                        ijkMediaPlayer.isPlaying()) {
                    playingListener.onSongProcessing(ijkMediaPlayer, ijkMediaPlayer.getDuration(), ijkMediaPlayer.getCurrentPosition());
                    Message message = Message.obtain();
                    message.obj = targetSong;
                    sendMessageDelayed(message, SECOND);
                }
            }
        }
    };
    @SuppressLint("HandlerLeak")
    private Handler stoppingHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            currentStoppingTimeLeft -= 1;
            if (currentStoppingTimeLeft > 0) {
                playingListener.onStopping(ijkMediaPlayer, currentStoppingTimeLeft, false);
                sendEmptyMessageDelayed(DEFAULT, SECOND);
            } else {
                if (forceTimerToStop) {
                    forceTimerToStop = false;
                    playingListener.onSongChanged(ijkMediaPlayer, songlist.get(currentIndex), currentIndex);
                } else {
                    playingListener.onStopping(ijkMediaPlayer, currentStoppingTimeLeft, true);
                    stop();
                }
            }
        }
    };
    private Boolean forceTimerToStop;

    /**
     * Constructor
     *
     * @param songlist     list of songs
     * @param currentIndex set index
     * @param currentMode  set mode
     * @throws Exception Empty SongList
     */
    public AudioPlayer(@NonNull SongList songlist, int currentIndex, int currentMode) throws Exception {
        initializeClassVariables();

        if (songlist.isEmpty()) {
            throw new Exception("Class AudioPlayer - EmptyPlaylistException");
        }
        this.songlist = songlist;

        if (currentIndex > NONE && currentIndex < songlist.size()) {
            this.currentIndex = currentIndex;
        }

        switch (currentMode) {
            case MODE_NORMAL:
            case MODE_REPEAT:
            case MODE_REPEAT_LIST:
            case MODE_RANDOM:
                this.currentMode = currentMode;
        }

        initializeIjkMediaPlayer();
        initializeAnimators();
    }

    /**
     * Stop timer
     */
    public void stopTimer() {
        currentStoppingTimeLeft = 0;
        forceTimerToStop = true;
    }

    /**
     * Stop IjkMediaPlayer with timer
     *
     * @param second stop time left
     * @return true if the stopping is ok, otherwise false
     */
    public Boolean stop(int second) {
        forceTimerToStop = false;
        if (second > 0 && !(currentStoppingTimeLeft > 0)) {
            currentStoppingTimeLeft = second;
            stoppingHandler.sendEmptyMessage(DEFAULT);
            return true;
        }
        return false;
    }

    /**
     * Stop IjkMediaPlayer
     */
    private void stop() {
        if (ijkMediaPlayer != null) {
            ijkMediaPlayer.reset();
            playingListener.onPlayStateChanged(ijkMediaPlayer, ijkMediaPlayer.isPlaying());
            isReady = false;
            ijkMediaPlayer.release();
            ijkMediaPlayer = null;
            visualizer.release();
            visualizer = null;
        }
    }

    /**
     * Get songList in JSON String format
     *
     * @return songList in JSON String format
     */
    public String getSongListJson() {
        return new Gson().toJson(songlist, SongList.class);
    }

    /**
     * Get playlist in JSON String format
     *
     * @param clearPath false to not clear http path, true to clear
     * @param id        playlist id
     * @param name      playlist name
     * @param cover     playlist cover
     * @return playlist in JSON String format
     */
    public String getPlaylistJson(Boolean clearPath, String id, String name, String cover) {
        if (clearPath) {
            for (Song song : songlist) {
                if (isOnlinePath(song.songPath)) {
                    song.songPath = null;
                }
            }
        }
        Playlist playlist = new Playlist(NONE, id, name, songlist);
        if (cover != null) {
            playlist.setCover(cover);
        }
        return new Gson().toJson(playlist, Playlist.class);
    }

    /**
     * Toggle Mode
     */
    public void toggleMode() {
        int nextMode = currentMode + 1;
        switch (nextMode) {
            case MODE_NORMAL:
            case MODE_REPEAT:
            case MODE_REPEAT_LIST:
            case MODE_RANDOM:
                currentMode = nextMode;
                break;
            default:
                currentMode = DEFAULT;
        }
        playingListener.onModeChanged(ijkMediaPlayer, currentMode);
    }

    /**
     * Change into a new songList, and play
     *
     * @param newSongList the new songList
     * @return false if the new songList is empty and stop the change, true if songList changed
     */
    public Boolean changePlaylistAndPlay(@NonNull SongList newSongList) {
        if (newSongList.isEmpty()) {
            return false;
        }
        initializeClassVariables();
        songlist = newSongList;
        playingListener.onPlaylistChanged(ijkMediaPlayer, songlist);
        play();
        return true;
    }

    /**
     * Add a new song to songList, and play the new song
     *
     * @param newSong the new song
     */
    public void addNewSongAndPlay(@NonNull Song newSong) {
        addNewSong(newSong);
        play(songlist.indexOf(newSong));
    }

    /**
     * Add a new song to songList
     *
     * @param newSong the new song
     */
    public void addNewSong(@NonNull Song newSong) {
        int index = songlist.indexOf(newSong);
        if (index >= 0) {
            playingListener.onPlaylistChanged(ijkMediaPlayer, songlist.get(index));
        } else {
            songlist.add(newSong);
            playingListener.onPlaylistChanged(ijkMediaPlayer, newSong);
        }
    }

    /**
     * Seek to another position
     *
     * @param position position
     */
    public void seekTo(int position) {
        if (ijkMediaPlayer != null &&
                ijkMediaPlayer.getDataSource() != null) {
            ijkMediaPlayer.seekTo(position);
        }
    }

    /**
     * Seek to another position by percentage
     *
     * @param percentage prefer 0 - 100
     */
    public void seekToByPercent(int percentage) {
        if (ijkMediaPlayer != null &&
                ijkMediaPlayer.getDataSource() != null) {
            ijkMediaPlayer.seekTo(ijkMediaPlayer.getDuration() * percentage / 100);
        }
    }

    /**
     * Toggle play
     */
    public void togglePlay() {
        if (ijkMediaPlayer == null) {
            initializeIjkMediaPlayer();
        }

        if (ijkMediaPlayer.isPlaying()) {
            playingListener.onPlayStateChanged(ijkMediaPlayer, false);
            audioVolumeAnimator.gradient(1, 0, ijkMediaPlayer, new AudioVolumeAnimator.VolumeListener() {
                @Override
                public void onCompleted(IjkMediaPlayer ijkMediaPlayer) {
                    ijkMediaPlayer.pause();
                }
            });

        } else {
            if (isReady) {
                ijkMediaPlayer.start();
                audioVolumeAnimator.gradient(0, 1, ijkMediaPlayer);
            } else if (ijkMediaPlayer.getDataSource() == null) {
                play();
            }

            Message message = Message.obtain();
            message.obj = songlist.get(currentIndex);
            processingHandler.sendMessage(message);

            playingListener.onPlayStateChanged(ijkMediaPlayer, true);
        }
    }

    /**
     * Prepare to play the selected song
     *
     * @param targetIndex index of the selected song
     */
    public void play(int targetIndex) {
        if (targetIndex >= DEFAULT && targetIndex < songlist.size()) {
            previousIndex.push(currentIndex);
            currentIndex = targetIndex;
            play();
        }
    }

    /**
     * Prepare to play previous song
     *
     * @return false if no previous song, otherwise true
     */
    public Boolean previous() {
        if (previousIndex.empty()) {
            return false;
        }

        int previousIndex = this.previousIndex.pop();
        currentIndex = previousIndex;
        play();
        return true;
    }

    /**
     * Prepare to play next song
     *
     * @param isManually false if onCompleted, true if onNextClicked
     */
    public void next(Boolean isManually) {
        int nextIndex;
        switch (currentMode) {
            case MODE_NORMAL:
                nextIndex = currentIndex + 1;
                if (nextIndex < songlist.size()) {
                    previousIndex.push(currentIndex);
                    currentIndex = nextIndex;
                    play();
                } else if (isManually) {
                    previousIndex.push(currentIndex);
                    currentIndex = DEFAULT;
                    play();
                }
                break;
            case MODE_REPEAT:
                if (isManually) {
                    nextIndex = currentIndex + 1;
                    previousIndex.push(currentIndex);
                    if (nextIndex < songlist.size()) {
                        currentIndex = nextIndex;
                    } else {
                        currentIndex = DEFAULT;
                    }
                }
                play();
                break;
            case MODE_REPEAT_LIST:
                nextIndex = currentIndex + 1;
                previousIndex.push(currentIndex);
                if (nextIndex < songlist.size()) {
                    currentIndex = nextIndex;
                } else {
                    currentIndex = DEFAULT;
                }
                play();
                break;
            case MODE_RANDOM:
                nextIndex = (int) Math.floor(Math.random() * songlist.size());
                if (nextIndex == currentIndex &&
                        nextIndex < (songlist.size() - 1)) {
                    nextIndex++;
                }
                previousIndex.push(currentIndex);
                currentIndex = nextIndex;
                play();
        }
        if (ijkMediaPlayer != null) {
            playingListener.onPlayStateChanged(ijkMediaPlayer, ijkMediaPlayer.isPlaying());
        }
    }

    /**
     * Prepare to play
     */
    private void play() {
        isReady = false;
        playingListener.onSongChanged(ijkMediaPlayer, songlist.get(currentIndex), currentIndex);
        playingListener.onPlayStateChanged(ijkMediaPlayer, true);

        final int ref = songlist.get(currentIndex).getReference();
        String id = songlist.get(currentIndex).songId;

        playingListener.onNetworkStart(ijkMediaPlayer);
        SongCacher.getSong(ref, id, new SongCacher.SongCacheListener() {
            @Override
            public void onCompleted(final String songPath) {
                if (ijkMediaPlayer == null) {
                    initializeIjkMediaPlayer();
                }
                songlist.get(currentIndex).songPath = songPath;

                audioVolumeAnimator.gradient(1, 0, ijkMediaPlayer, new AudioVolumeAnimator.VolumeListener() {
                    @Override
                    public void onCompleted(IjkMediaPlayer ijkMediaPlayer) {
                        ijkMediaPlayer.reset();
                        try {
                            ijkMediaPlayer.setDataSource(songPath, headersFor(ref));
                        } catch (IOException e) {
                            e.printStackTrace();
                            playingListener.onNetworkEnd(ijkMediaPlayer);
                            return;
                        }
                        ijkMediaPlayer.prepareAsync();
                    }
                });
            }

            @Override
            public void onError() {
                next(false);
            }
        });
    }

    /**
     * Set visualizer
     */
    public void setVisualizer(final VisualizerView visualizerView) {
        if (visualizer == null && ijkMediaPlayer != null) {
            visualizer = new Visualizer(ijkMediaPlayer.getAudioSessionId());
            visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
            visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                    visualizerView.updateVisualizer(waveform);
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                    return;
                }
            }, Visualizer.getMaxCaptureRate() / 2, true, false);
            visualizer.setEnabled(true);
        } else if (visualizer != null && ijkMediaPlayer != null) {
            visualizer.release();
            visualizer = new Visualizer(ijkMediaPlayer.getAudioSessionId());
            visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
            visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                    visualizerView.updateVisualizer(waveform);
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                    return;
                }
            }, Visualizer.getMaxCaptureRate() / 2, true, false);
            visualizer.setEnabled(true);
        }
    }

    /**
     * Check online path
     *
     * @param path path
     * @return false if local path, true if online path
     */
    private Boolean isOnlinePath(String path) {
        return path.startsWith("http");
    }

    /**
     * Initialize class variables
     */
    private void initializeClassVariables() {
        if (songlist != null) {
            songlist.clear();
        }
        songlist = null;
        if (previousIndex != null) {
            previousIndex.clear();
        }
        previousIndex = new Stack<>();
        currentIndex = DEFAULT;
        currentMode = DEFAULT;
        isReady = false;
        currentBufferPercentage = DEFAULT;
        currentStoppingTimeLeft = DEFAULT;

        forceTimerToStop = false;
    }

    /**
     * Initialize animators
     */
    private void initializeAnimators() {
        audioVolumeAnimator = new AudioVolumeAnimator();
    }

    /**
     * Initialize IjkMediaPlayer
     */
    private void initializeIjkMediaPlayer() {
        listener = new Listener();
        ijkMediaPlayer = new IjkMediaPlayer();
        ijkMediaPlayer.setOnPreparedListener(listener);
        ijkMediaPlayer.setOnBufferingUpdateListener(listener);
        ijkMediaPlayer.setOnCompletionListener(listener);
        ijkMediaPlayer.setOnErrorListener(listener);
    }

    /**
     * Interface PlayingListener
     * Contain all listener for tracking song changes
     */
    public interface PlayingListener {
        void onBufferingUpdate(IjkMediaPlayer ijkMediaPlayer, int percentage);

        void onSongChanged(IjkMediaPlayer ijkMediaPlayer, Song song, int currentIndex);

        void onSongProcessing(IjkMediaPlayer ijkMediaPlayer, long duration, long currentPosition);

        void onPlaylistChanged(IjkMediaPlayer ijkMediaPlayer, Song song);

        void onPlaylistChanged(IjkMediaPlayer ijkMediaPlayer, SongList songlist);

        void onPlayStateChanged(IjkMediaPlayer ijkMediaPlayer, Boolean isPlay);

        void onNetworkStart(IjkMediaPlayer ijkMediaPlayer);

        void onNetworkEnd(IjkMediaPlayer ijkMediaPlayer);

        void onModeChanged(IjkMediaPlayer ijkMediaPlayer, int mode);

        void onStopping(IjkMediaPlayer ijkMediaPlayer, int secondLeft, Boolean isStopped);

        void onError(IjkMediaPlayer ijkMediaPlayer, int code);
    }

    /**
     * Set PlayingListener
     *
     * @param playingListener PlayingListener
     */
    public void setListener(PlayingListener playingListener) {
        this.playingListener = playingListener;
    }

    /**
     * Refresh PlaylingListener
     */
    public void refreshListner() {
        if (ijkMediaPlayer != null) {
            playingListener.onBufferingUpdate(ijkMediaPlayer, currentBufferPercentage);
            playingListener.onSongChanged(ijkMediaPlayer, songlist.get(currentIndex), currentIndex);
            playingListener.onPlaylistChanged(ijkMediaPlayer, songlist);
            playingListener.onPlayStateChanged(ijkMediaPlayer, ijkMediaPlayer.isPlaying());
            playingListener.onModeChanged(ijkMediaPlayer, currentMode);
        }
    }

    /**
     * Class Listener
     * Contain all listeners for IjkMediaPlayer
     */
    private class Listener implements IMediaPlayer.OnPreparedListener,
            IMediaPlayer.OnBufferingUpdateListener,
            IMediaPlayer.OnCompletionListener,
            IMediaPlayer.OnErrorListener {

        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            iMediaPlayer.start();
            audioVolumeAnimator.gradient(0, 1, ijkMediaPlayer);
            isReady = true;

            Message message = Message.obtain();
            message.obj = songlist.get(currentIndex);
            processingHandler.sendMessage(message);

            playingListener.onPlayStateChanged(ijkMediaPlayer, true);
            playingListener.onNetworkEnd(ijkMediaPlayer);

            if (currentIndex + 1 < songlist.size()) {
                int ref = songlist.get(currentIndex).getReference();
                String id = songlist.get(currentIndex).songId;
                SongCacher.loadSong(ref, id);
            }
        }

        @Override
        public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
            currentBufferPercentage = i;
            playingListener.onBufferingUpdate(ijkMediaPlayer, i);
        }

        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            next(false);
        }

        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
            playingListener.onPlayStateChanged(ijkMediaPlayer, false);
            playingListener.onError(ijkMediaPlayer, i);
            playingListener.onNetworkEnd(ijkMediaPlayer);
            iMediaPlayer.reset();
            return false;
        }
    }
}
