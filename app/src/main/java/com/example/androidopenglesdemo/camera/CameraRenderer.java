package com.example.androidopenglesdemo.camera;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.example.androidopenglesdemo.glenv.GlesRenderer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraRenderer extends GlesRenderer {

    private final EffectRenderer effectRenderer;
    private final DisplayRenderer displayRenderer;
    private final GlesFrameExporter glesFrameExporter;

    private LinkedList<IGlesFilter> mFilterQueue
            = new LinkedList<>();
    private GL10 gl10;
    private EGLConfig eglConfig;

    private List<IGlesFilter> mFilterList = new ArrayList<>();
    private IFrameExportCallback callback;
    private boolean enableFrameExport;
    private boolean frameExportConfigChanged;
    private IFrameExportCallback takePicFrameExportCallback;
    private IFrameExportCallback callback1;

    public CameraRenderer(Context context, int width, int height, ISurfaceTextureListener iSurfaceTextureListener) {
        super(context, width, height);
        effectRenderer = new EffectRenderer(context, width, height, iSurfaceTextureListener);
        displayRenderer = new DisplayRenderer(context);

        glesFrameExporter = new GlesFrameExporter(context);
    }

    public void addFilter(IGlesFilter filter) {
        mFilterQueue.add(filter);
    }

    @Override
    public void onRendererCreated(GL10 gl10, EGLConfig eglConfig) {
        this.gl10 = gl10;
        this.eglConfig = eglConfig;
        effectRenderer.onRendererCreated(gl10, eglConfig);
        displayRenderer.onRendererCreated(gl10, eglConfig);
        glesFrameExporter.setFilterSize(getWidth(), getHeight());
        glesFrameExporter.onRendererCreated(gl10, eglConfig);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        effectRenderer.onDrawFrame(gl10);

        if (!mFilterQueue.isEmpty()) {
            while (!mFilterQueue.isEmpty()) {
                IGlesFilter filter = mFilterQueue.poll();
                if (filter != null) {
                    filter.setFilterSize(getWidth(), getHeight());
                    filter.onRendererCreated(gl10, eglConfig);
                    mFilterList.add(filter);
                }
            }
        }
        int outputTexture = effectRenderer.getOutputTexture();
        for (IGlesFilter filter : mFilterList) {
            filter.setInputTexture(outputTexture, getWidth(), getHeight());
            filter.onDrawFrame(gl10);
            outputTexture = filter.getOutputTexture();
        }

        if (isWindowSurfaceCreated()) {
            displayRenderer.setInputTexture(outputTexture, getWindowSurfaceWidth(), getWindowSurfaceHeight());
            displayRenderer.onDrawFrame(gl10);
            outputTexture = displayRenderer.getOutputTexture();
        }

        if (frameExportConfigChanged) {
            frameExportConfigChanged = false;
            if (enableFrameExport) {
                glesFrameExporter.addFrameExportCallback(mDefaultFrameExport);
            } else {
                glesFrameExporter.removeFrameExportCallback(mDefaultFrameExport);
            }
        }
        // if callback
        IFrameExportCallback takePicFrameExportCallback = this.takePicFrameExportCallback;
        boolean enableFrameExport = this.enableFrameExport;
        if ((enableFrameExport)
                || (takePicFrameExportCallback != null)) {

            glesFrameExporter.setInputTexture(outputTexture, getWidth(), getHeight());
            if (takePicFrameExportCallback != null) {
                glesFrameExporter.addFrameExportCallback(takePicFrameExportCallback);
            }
            glesFrameExporter.onDrawFrame(gl10);

            synchronized (this) {
                if (takePicFrameExportCallback != null
                        && takePicFrameExportCallback == this.takePicFrameExportCallback) {
                    this.takePicFrameExportCallback = null;
                    glesFrameExporter.removeFrameExportCallback(takePicFrameExportCallback);
                    notifyAll();
                }
            }
        }

        //
    }

    private SparseArray<IFrameExportCallback> exportCallbackSparseArray
            = new SparseArray<>();
    private final IFrameExportCallback mDefaultFrameExport
            = new IFrameExportCallback() {
        @Override
        public void onFrameExport(ByteBuffer byteBuffer, int width, int height) {
            synchronized (this) {
                for (int i = 0; i < exportCallbackSparseArray.size(); i++) {
                    exportCallbackSparseArray.get(exportCallbackSparseArray.keyAt(i))
                            .onFrameExport(byteBuffer, width, height);
                    byteBuffer.position(0);
                }
            }
        }
    };

    @Override
    public void onRendererDestory() {
        effectRenderer.onRendererDestory();
    }

    public void registerGlesFrameExporter(IFrameExportCallback callback) {
        assert callback != null;
        synchronized (mDefaultFrameExport) {
            exportCallbackSparseArray.put(callback.hashCode(), callback);
            setEnableFrameExport(exportCallbackSparseArray.size() > 0);
        }
    }

    public void unregisterGlesFrameExporter(IFrameExportCallback callback) {
        assert callback != null;
        synchronized (mDefaultFrameExport) {
            exportCallbackSparseArray.remove(callback.hashCode());
            setEnableFrameExport(exportCallbackSparseArray.size() > 0);
        }
    }

    public void setEnableFrameExport(boolean enableFrameExport) {
        if (this.enableFrameExport != enableFrameExport) {
            this.enableFrameExport = enableFrameExport;
            frameExportConfigChanged = true;
        }
    }

    public void takePicture(IFrameExportCallback iFrameExportCallback) {
        if (iFrameExportCallback == null) return;
        takePicFrameExportCallback = iFrameExportCallback;
    }
}
