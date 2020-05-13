package com.example.androidopenglesdemo.camera.impl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import androidx.collection.ArrayMap;
import androidx.collection.SimpleArrayMap;

import com.example.androidopenglesdemo.camera.CameraUtils;
import com.example.androidopenglesdemo.camera.ICamera;
import com.example.androidopenglesdemo.camera.PreviewCallback;

import java.io.IOException;

public class CameraImpl implements ICamera {
    private static final String TAG = "CameraImpl";
    private final Context context;
    private Camera camera;
    private Camera.Size previewSize;
    private byte[] callbackBuffer;
    private PreviewCallback previewCallback;
    private int degree;
    private int cameraId;
    private ArrayMap mCameraInfo = new ArrayMap();

    public CameraImpl(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void open(int cameraId) {
        this.cameraId = cameraId;
        this.camera = Camera.open(cameraId);
        Camera.Parameters parameters = this.camera.getParameters();
        degree = CameraUtils.getCameraDisplayRotation(context, cameraId);
        previewSize = parameters.getPreviewSize();
        camera.setDisplayOrientation(degree);

        mCameraInfo.put(ICamera.PARAM_DEGREE, degree);
        mCameraInfo.put(ICamera.PARAM_PREVIEW_SIZE, new int[]{previewSize.width, previewSize.height});
        Log.d(TAG, "open:camera " + ", " + camera);
    }

    @Override
    public void setPreviewCallback(PreviewCallback previewCallback) {
        this.previewCallback = previewCallback;
        callbackBuffer = new byte[previewSize.width * previewSize.height * 3 / 2];
        camera.setPreviewCallbackWithBuffer(cameraPreviewCallback);
        camera.addCallbackBuffer(callbackBuffer);
        Log.d(TAG, "setPreviewCallback: ");
    }

    private Camera.PreviewCallback cameraPreviewCallback
            = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            PreviewCallback previewCallback = CameraImpl.this.previewCallback;
            if(previewCallback != null) {
                previewCallback.onPreviewFrame(data, previewSize.width, previewSize.height, degree, cameraId);
                camera.addCallbackBuffer(callbackBuffer);
            }
        }
    };

    @Override
    public void startPreview(SurfaceTexture surfaceTexture) {
        try {
            camera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();

        Log.d(TAG, "startPreview: ");
    }

    @Override
    public void stopPreview() {
        try {
            camera.setPreviewTexture(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.stopPreview();
    }

    @Override
    public void destory() {
        camera.release();
        camera = null;
    }

    @Override
    public ArrayMap getCameraInfo() {
        return mCameraInfo;
    }
}
