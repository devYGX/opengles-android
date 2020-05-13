package com.example.androidopenglesdemo.sample3;

import android.content.Context;

import com.example.androidopenglesdemo.glenv.GlesRenderer;
import com.example.androidopenglesdemo.glrendershape.ShapeRender;
import com.example.androidopenglesdemo.glrendershape.impls.Circle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TriangleRenderer extends GlesRenderer {
    private static final String TAG = "TriangleRenderer";
    private final ShapeRender triangleInstance;

    public TriangleRenderer(Context context, int width, int height) {
        super(context,width, height);
        triangleInstance = new Circle(context);
    }

    @Override
    public void onRendererCreated(GL10 gl10, EGLConfig eglConfig) {
        triangleInstance.onSurfaceCreated(gl10, eglConfig);
        triangleInstance.onSurfaceChanged(gl10, getWidth(), getHeight());
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        triangleInstance.onDrawFrame(gl10);
    }

    @Override
    public void onRendererDestory() {
    }
}
