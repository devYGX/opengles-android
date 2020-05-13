package com.example.androidopenglesdemo.camera;

public interface PreviewCallback {

    void onPreviewFrame(byte[] buf, int width, int height, int degree, int cameraId);
}
