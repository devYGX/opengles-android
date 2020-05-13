package com.example.androidopenglesdemo.camera;

import android.graphics.SurfaceTexture;

import androidx.collection.ArrayMap;

public interface ICamera {

    int PARAM_DEGREE = 1;
    int PARAM_PREVIEW_SIZE = 2;

    void open(int camera);

    void setPreviewCallback(PreviewCallback previewCallback);

    void startPreview(SurfaceTexture surfaceTexture);

    void stopPreview();

    void destory();

    ArrayMap getCameraInfo();

}
