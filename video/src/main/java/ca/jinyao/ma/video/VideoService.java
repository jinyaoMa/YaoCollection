package ca.jinyao.ma.video;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.InputStream;

import ca.jinyao.ma.video.components.CatalogueTable;
import ca.jinyao.ma.video.components.VideoList;
import ca.jinyao.ma.video.cores.SourceBrowser;
import ca.jinyao.ma.video.cores.VideoConfig;
import ca.jinyao.ma.video.widgets.NavigationWidget;

/**
 * Class VideoService
 * create by jinyaoMa 0001 2018/9/1 17:55
 */
public class VideoService extends Service {





    public VideoService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public final String TAG = "VideoService";

    private NavigationWidget navigationWidget;


    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder builder;
    private Notification notification;
    private Bitmap currentNotificationIcon;
    private String currentNotificationTitle;
    private String currentNotificationText;
    private final String CHANNEL_ID = "VideoService_1";
    private final String CHANNEL_NAME = "VideoService_1";
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
        builder.setSmallIcon(R.mipmap.ic_icon_movie)
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

        VideoConfig.trustEveryone();

        setupNotification();
        setNotifyMessage(R.mipmap.ic_icon_movie, TAG, TAG);
        showNotification();
        startForeground(NOTIFICATION_ID, notification);

        navigationWidget = new NavigationWidget(this);

        navigationWidget.create();

        setListeners();
    }

    private void setListeners() {
        navigationWidget.setListener(new NavigationWidget.Listener() {
            @Override
            public void onVideoClick(Boolean isVideoOn) {
                navigationWidget.setVideoOn(!isVideoOn);
            }

            @Override
            public void onBrowseClick(Boolean isBrowseOn) {
                navigationWidget.setBrowseOn(!isBrowseOn);
            }

            @Override
            public void onSearchClick(Boolean isSearchOn) {
                navigationWidget.setSearchOn(!isSearchOn);
            }

            @Override
            public void onListClick(Boolean isListOn) {
                navigationWidget.setListOn(!isListOn);
            }
        });
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (navigationWidget != null) {
            navigationWidget.remove();
        }
    }

    private static Boolean isServiceOn = false;
    private static final String STOP_COMMAND = "stop";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getBooleanExtra(STOP_COMMAND, false)) {
            //audioPlayer.stop();----------------------------------------------------------------------------------------------------------------------------------------------------------
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
        } else if (!Settings.canDrawOverlays(activity)) {
            if (checkOverlay) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                activity.startActivityForResult(intent, requestCode);
                Toast.makeText(activity.getApplicationContext(), "VideoService - Request Overlay Permission", Toast.LENGTH_LONG).show();
            }
        } else {
            Intent intent = new Intent(activity, VideoService.class);
            activity.startForegroundService(intent);
            isServiceOn = true;
            return true;
        }
        return false;
    }

    public static void stop(Activity activity) {
        if (isServiceOn) {
            Intent intent = new Intent(activity, VideoService.class);
            intent.putExtra(STOP_COMMAND, true);
            activity.startForegroundService(intent);
            isServiceOn = false;
        }
    }

    public static Boolean isRunning() {
        return isServiceOn;
    }
}
