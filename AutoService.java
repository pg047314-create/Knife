java > com.example.knifeauto > AutoService.java`
package com.example.knifeauto;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.os.Handler;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Moments;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class AutoService extends AccessibilityService {
    public static boolean isRunning = false;
    private Handler handler = new Handler();
    private int SCREEN_WIDTH = MainActivity.SCREEN_WIDTH;
    private int SCREEN_HEIGHT = MainActivity.SCREEN_HEIGHT;

    private Scalar LOWER_RED1 = new Scalar(0, 120, 70);
    private Scalar UPPER_RED1 = new Scalar(10, 255, 255);
    private Scalar LOWER_RED2 = new Scalar(170, 120, 70);
    private Scalar UPPER_RED2 = new Scalar(180, 255, 255);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!= null) {
            isRunning = intent.getBooleanExtra("RUN", false);
            if(isRunning) handler.post(detectionLoop);
            else handler.removeCallbacks(detectionLoop);
        }
        return START_STICKY;
    }

    private Runnable detectionLoop = new Runnable() {
        @Override
        public void run() {
            if(isRunning){
                Bitmap screen = MainActivity.getLatestScreen();
                if(screen!= null){
                    int[] pos = detectRedCenter(screen);
                    if(pos!= null) performClick(pos[0], pos[1]);
                }
                handler.postDelayed(this, 80);
            }
        }
    };

    private int[] detectRedCenter(Bitmap bitmap) {
        Mat img = new Mat(); Utils.bitmapToMat(bitmap, img);
        Mat hsv = new Mat(); Imgproc.cvtColor(img, hsv, Imgproc.COLOR_RGB2HSV);
        Mat mask1 = new Mat(); Mat mask2 = new Mat(); Mat mask = new Mat();
        Core.inRange(hsv, LOWER_RED1, UPPER_RED1, mask1);
        Core.inRange(hsv, LOWER_RED2, UPPER_RED2, mask2);
        Core.add(mask1, mask2, mask);
        Moments moments = Imgproc.moments(mask);
        if(moments.m00 > 600){
            int cx = (int) (moments.m10 / moments.m00);
            int cy = (int) (moments.m01 / moments.m00);
            return new int[]{cx, cy};
        }
        return null;
    }

    private void performClick(int x, int y) {
        Path path = new Path(); path.moveTo(x, y);
        GestureDescription gesture = new GestureDescription.Builder()
               .addStroke(new GestureDescription.StrokeDescription(path, 0, 1)).build();
        dispatchGesture(gesture, null, null);
    }

    @Override public void onAccessibilityEvent(android.view.accessibility.AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
}
