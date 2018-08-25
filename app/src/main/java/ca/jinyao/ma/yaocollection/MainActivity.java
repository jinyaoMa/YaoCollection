package ca.jinyao.ma.yaocollection;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import ca.jinyao.ma.yaocollection.audio.AudioService;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        AudioService.checkPermissionAndStart(this, requestCode, grantResults[0]);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AudioService.checkPermissionAndStart(this, 0, true);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        AudioService.checkPermissionAndStart(this, 0, false);

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}
