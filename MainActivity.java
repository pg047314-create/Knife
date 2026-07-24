java > com.example.knifeauto > MainActivity.java`
package com.example.knifeauto;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static Bitmap latestScreen = null;
    public static int SCREEN_WIDTH = 720; // TUMHARA RESOLUTION
    public static int SCREEN_HEIGHT = 1600;

    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;

    public static Bitmap getLatestScreen() { return latestScreen; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(projectionManager.createScreenCaptureIntent(), 100);

        Button btn = findViewById(R.id.btnStart);
        btn.setOnClickListener(v -> {
            Intent service = new Intent(this, AutoService.class);
            service.putExtra("RUN",!AutoService.isRunning);
            startService(service);
            btn.setText(AutoService.isRunning? "STOP" : "START");
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            setupVirtualDisplay();
            startScreenCapture();
        }
    }

    private void setupVirtualDisplay() {
        imageReader = ImageReader.newInstance(SCREEN_WIDTH, SCREEN_HEIGHT, PixelFormat.RGBA_8888, 2);
        virtualDisplay = mediaProjection.createVirtualDisplay("ScreenCapture",
                SCREEN_WIDTH, SCREEN_HEIGHT, 320,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(), null, null);
    }

    private void startScreenCapture() {
        imageReader.setOnImageAvailableListener(reader -> {
            Image image = reader.acquireLatestImage();
            if (image!= null) {
                Image.Plane[] planes = image.getPlanes();
                android.graphics.Bitmap bitmap = Bitmap.createBitmap(SCREEN_WIDTH, SCREEN_HEIGHT, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(planes[0].getBuffer());
                latestScreen = bitmap;
                image.close();
            }
        }, null);
    }
}
