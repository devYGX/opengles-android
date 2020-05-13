package com.example.androidopenglesdemo.camera;

import android.content.Context;
import android.hardware.Camera;
import android.view.Surface;
import android.view.WindowManager;

public class CameraUtils {
    /**
     * 获取摄像头显示角度
     * @param ctx
     * @param cameraId
     * @return
     */
    public static int getCameraDisplayRotation(Context ctx, int cameraId) {
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);

        int rotation = wm.getDefaultDisplay().getRotation();

        int degree = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 0;
                break;
            case Surface.ROTATION_90:
                degree = 90;
                break;
            case Surface.ROTATION_180:
                degree = 180;
                break;
            case Surface.ROTATION_270:
                degree = 270;
                break;
        }

        try{
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return (360 - ((cameraInfo.orientation + degree) % 360)) % 360;
            } else {
                return (cameraInfo.orientation - degree + 360) % 360;
            }
        }catch (Exception e){
            return 0;
        }
    }
}
