package ca.jinyao.ma.video;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.xunlei.downloadlib.XLTaskHelper;
import com.xunlei.downloadlib.parameter.XLTaskInfo;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class TestActivity extends AppCompatActivity {

    String urlString = "thunder://QUFodHRwOi8vdmlwLnp1aWt1OC5jb20vMTgwNi/kuJzkuqzmkJzmn6XlrphSRS0xMi5tcDRaWg==";
    Button btnDownload;
    Button btnStop;
    TextView tvStatus;
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                long taskId = (long) msg.obj;
                XLTaskInfo taskInfo = XLTaskHelper.instance(getApplicationContext()).getTaskInfo(taskId);
                tvStatus.setText(
                        "fileSize:" + taskInfo.mFileSize
                                + " downSize:" + taskInfo.mDownloadSize
                                + " speed:" + convertFileSize(taskInfo.mDownloadSpeed)
                                + "/s dcdnSpeed:" + convertFileSize(taskInfo.mAdditionalResDCDNSpeed)
                                + "/s filePath:" + "/sdcard/" + XLTaskHelper.instance(getApplicationContext()).getFileName(urlString)
                );
                handler.sendMessageDelayed(handler.obtainMessage(0, taskId), 1000);
            }
        }
    };


    public String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f M" : "%.1f M", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f K" : "%.1f K", f);
        } else
            return String.format("%d B", size);
    }


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



        XLTaskHelper.init(this);
        btnDownload = findViewById(R.id.btnDownload);
        btnStop = findViewById(R.id.btnStop);
        tvStatus = findViewById(R.id.tvStatus);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    taskId = XLTaskHelper.instance(TestActivity.this).addThunderTask(urlString, "/sdcard/", null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handler.sendMessage(handler.obtainMessage(0, taskId));
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                XLTaskHelper.instance(TestActivity.this).stopTask(taskId);
            }
        });

    }

    long taskId = 0;

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
