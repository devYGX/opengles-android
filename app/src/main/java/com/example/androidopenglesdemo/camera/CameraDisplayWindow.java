package com.example.androidopenglesdemo.camera;

public class CameraDisplayWindow {

    private final Object nativeWindow;
    private final int width;
    private final int height;

    public CameraDisplayWindow(Object nativeWindow, int width, int height){
        this.nativeWindow = nativeWindow;
        this.width = width;
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Object getNativeWindow() {
        return nativeWindow;
    }
}
