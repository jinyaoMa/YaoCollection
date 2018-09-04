package ca.jinyao.ma.yaocollection;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.Switch;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.jinyao.ma.audio.AudioService;
import ca.jinyao.ma.video.TestActivity;
import ca.jinyao.ma.video.VideoService;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private final int AUDIO_SERVICE_REQUEST_CODE = 101;
    private final int AUDIO_SERVICE_TAG = 101;
    private final int VIDEO_SERVICE_REQUEST_CODE = 102;
    private final int VIDEO_SERVICE_TAG = 102;

    @BindView(R.id.sAudio)
    Switch sAudio;
    @BindView(R.id.sVideo)
    Switch sVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        sAudio.setTag(AUDIO_SERVICE_TAG);
        sAudio.setOnCheckedChangeListener(this);
        sAudio.setChecked(AudioService.isRunning());

        sVideo.setTag(VIDEO_SERVICE_TAG);
        sVideo.setOnCheckedChangeListener(this);
        sVideo.setChecked(VideoService.isRunning());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        sAudio.setChecked(AudioService.checkPermissionAndStart(this, requestCode, grantResults[0]));
        sVideo.setChecked(VideoService.checkPermissionAndStart(this, requestCode, grantResults[0]));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        sAudio.setChecked(AudioService.checkPermissionAndStart(this, requestCode, false));
        sVideo.setChecked(VideoService.checkPermissionAndStart(this, requestCode, false));
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch ((int) compoundButton.getTag()) {
            case AUDIO_SERVICE_TAG:
                if (b) {
                    if (VideoService.isRunning()) {
                        sVideo.setChecked(false);
                    }
                    sAudio.setChecked(AudioService.checkPermissionAndStart(this, AUDIO_SERVICE_REQUEST_CODE, true));
                } else {
                    AudioService.stop(this);
                }
                break;
            case VIDEO_SERVICE_TAG:
                if (b) {
                    if (AudioService.isRunning()) {
                        sAudio.setChecked(false);
                    }
                    sVideo.setChecked(VideoService.checkPermissionAndStart(this, VIDEO_SERVICE_REQUEST_CODE, true));
                } else {
                    VideoService.stop(this);
                }
        }
    }
}
