package com.example.androidopenglesdemo.glenv;

import android.content.Context;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public abstract class GlesRenderer {

    private final int width;
    private final int height;
    private final Context context;

    private boolean isWindowSurfaceCreated;
    private int windowSurfaceWidth;
    private int windowSurfaceHeight;

    public GlesRenderer(Context context, int width, int height) {
        this.context = context;
        this.width = width;
        this.height = height;
    }

    public Context getContext() {
        return context;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public abstract void onRendererCreated(GL10 gl10, EGLConfig eglConfig);

    public abstract void onDrawFrame(GL10 gl10);

    public abstract void onRendererDestory();

    public void onWindowSurfaceCreated(int width, int height) {
        isWindowSurfaceCreated = true;
        this.windowSurfaceWidth = width;
        this.windowSurfaceHeight = height;
    }

    public void onWindowSurfaceDestory() {
        isWindowSurfaceCreated = false;
    }

    public boolean isWindowSurfaceCreated() {
        return isWindowSurfaceCreated;
    }

    public int getWindowSurfaceHeight() {
        return windowSurfaceHeight;
    }

    public int getWindowSurfaceWidth() {
        return windowSurfaceWidth;
    }
}
