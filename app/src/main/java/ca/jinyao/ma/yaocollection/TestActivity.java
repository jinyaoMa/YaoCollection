package ca.jinyao.ma.yaocollection;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Vitamio.isInitialized(this);

        MediaController mediaController = new MediaController(this);

        VideoView videoView = findViewById(R.id.videoView);
        videoView.setVideoPath("/sdcard/123.mkv");
        videoView.setMediaController(mediaController);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
