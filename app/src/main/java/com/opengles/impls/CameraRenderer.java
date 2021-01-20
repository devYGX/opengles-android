package com.opengles.impls;

import android.content.Context;
import android.util.SparseArray;

import com.opengles.AbsGlesFilter;
import com.opengles.GlesRenderer;
import com.opengles.IFrameExportCallback;
import com.opengles.ISurfaceTextureListener;

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

    private LinkedList<AbsGlesFilter> mFilterQueue
            = new LinkedList<>();
    private GL10 gl10;
    private EGLConfig eglConfig;

    private List<AbsGlesFilter> mFilterList = new ArrayList<>();
    private IFrameExportCallback callback;
    private boolean enableFrameExport;
    private boolean frameExportConfigChanged;
    private IFrameExportCallback takePicFrameExportCallback;

    public CameraRenderer(Context context,
                          int width,
                          int height,
                          int degree,
                          boolean mirror,
                          ISurfaceTextureListener iSurfaceTextureListener) {
        super(context, width, height);
        effectRenderer = new EffectRenderer(context, width, height, degree, mirror, iSurfaceTextureListener);
        displayRenderer = new DisplayRenderer(context);

        glesFrameExporter = new GlesFrameExporter(context);
    }

    public void addFilter(AbsGlesFilter filter) {
        mFilterQueue.add(filter);
    }

    @Override
    public void onRendererCreated(GL10 gl10, EGLConfig eglConfig) {
        this.gl10 = gl10;
        this.eglConfig = eglConfig;
        effectRenderer.onRendererCreated(gl10, eglConfig);
        displayRenderer.onFilterCreated(getWidth(), getHeight());
        glesFrameExporter.onFilterCreated(getWidth(), getHeight());
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        effectRenderer.onDrawFrame(gl10);

        if (!mFilterQueue.isEmpty()) {
            while (!mFilterQueue.isEmpty()) {
                AbsGlesFilter filter = mFilterQueue.poll();
                if (filter != null) {
                    filter.onFilterCreated(getWidth(), getHeight());
                    mFilterList.add(filter);
                }
            }
        }
        int outputTexture = effectRenderer.getOutputTexture();
        boolean hasFilter = false;
        for (AbsGlesFilter filter : mFilterList) {
            if (filter.getWidth() != getWidth() || filter.getHeight() != getHeight()) {
                filter.onFilterSizeChanged(getWidth(), getHeight());
            }
            filter.setInputTexture(outputTexture);
            filter.onDraw();
            outputTexture = filter.getOutputTexture();
            hasFilter = true;
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

            glesFrameExporter.setInputTexture(outputTexture);
            if (glesFrameExporter.getWidth() != getWidth()
                    || glesFrameExporter.getHeight() != getHeight()) {
                glesFrameExporter.onFilterSizeChanged(getWidth(), getHeight());
            }
            if (takePicFrameExportCallback != null) {
                glesFrameExporter.addFrameExportCallback(takePicFrameExportCallback);
            }
            glesFrameExporter.onDraw();

            synchronized (this) {
                if (takePicFrameExportCallback != null
                        && takePicFrameExportCallback == this.takePicFrameExportCallback) {
                    this.takePicFrameExportCallback = null;
                    glesFrameExporter.removeFrameExportCallback(takePicFrameExportCallback);
                }
            }
        }

        if (isWindowSurfaceCreated()) {
            displayRenderer.setInputTexture(outputTexture);
            if (displayRenderer.getWindowWidth() != getWindowSurfaceWidth()
                    || displayRenderer.getWindowHeight() != getWindowSurfaceHeight()
                    || displayRenderer.getWidth() != getWidth()
                    || displayRenderer.getHeight() != getHeight()) {
                displayRenderer.onWindowSizeChanged(getWindowSurfaceWidth(), getWindowSurfaceHeight(), getHeight(), getWidth());
            }
            displayRenderer.setHasFilter(hasFilter);
            displayRenderer.onDraw();
            outputTexture = displayRenderer.getOutputTexture();
        }


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

    private void setEnableFrameExport(boolean enableFrameExport) {
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
