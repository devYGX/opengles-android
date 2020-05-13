package com.example.androidopenglesdemo.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.SparseArray;

import androidx.collection.ArrayMap;

import com.example.androidopenglesdemo.camera.filterimpl.GrayFilter;
import com.example.androidopenglesdemo.glenv.OpenglesEnv;

public class CameraManager {
    private static final String TAG = "CameraManager";
    private static CameraManager gCameraManager;
    private final Handler mCameraHandler;
    private Context context;
    private ICamera iCamera;
    private int[] previewSize;
    private OpenglesEnv openglesEnv;
    private ArrayMap cameraInfo;
    private CameraDisplayWindow nativeWindow;
    private SparseArray<PreviewCallback> mPreviewCallback = new SparseArray<>();
    private CameraRenderer renderer;

    private CameraManager() {
        HandlerThread ht = new HandlerThread("Camera");
        ht.start();
        mCameraHandler = new Handler(ht.getLooper());
    }

    public static CameraManager getManager() {
        if (gCameraManager == null) {
            synchronized (CameraManager.class) {
                gCameraManager = new CameraManager();
            }
        }
        return gCameraManager;
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();
    }

    public void start() {
        mCameraHandler.post(new Runnable() {
            @Override
            public void run() {
                initCamera();
            }
        });
    }

    public void stop() {
        mCameraHandler.post(new Runnable() {
            @Override
            public void run() {
                doReleaseCamera();
            }
        });
    }

    private void doReleaseCamera() {
        ICamera iCamera = CameraManager.this.iCamera;
        if (iCamera != null) {
            iCamera.startPreview(null);
            iCamera.setPreviewCallback(null);
            iCamera.stopPreview();
            iCamera.destory();
        }
        CameraManager.this.iCamera = null;
    }

    public void release() {
        mCameraHandler.post(new Runnable() {
            @Override
            public void run() {
                doReleaseCamera();
                nativeWindow = null;
            }
        });
    }

    public void registerPreviewCallback(final PreviewCallback previewCallback) {
        if (previewCallback == null) return;
        mCameraHandler.post(new Runnable() {
            @Override
            public void run() {
                mPreviewCallback.put(previewCallback.hashCode(), previewCallback);
            }
        });
    }

    public void unregisterPreviewCallback(final PreviewCallback previewCallback) {
        if (previewCallback == null) return;
        mCameraHandler.post(new Runnable() {
            @Override
            public void run() {
                mPreviewCallback.remove(previewCallback.hashCode());
            }
        });
    }

    public void setDisplay(final CameraDisplayWindow nativeWindow) {
        mCameraHandler.post(new Runnable() {
            @Override
            public void run() {
                initCamera();
                CameraManager.this.nativeWindow = nativeWindow;
                if (nativeWindow == null) {
                    openglesEnv.setDisplaySurface(null, 0, 0);
                } else {
                    openglesEnv.setDisplaySurface(
                            nativeWindow.getNativeWindow(),
                            nativeWindow.getWidth(),
                            nativeWindow.getHeight());
                }
            }
        });
    }

    private ISurfaceTextureListener mEffectListener
            = new ISurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture) {
            iCamera.startPreview(surfaceTexture);
            iCamera.setPreviewCallback(previewCallback);
            surfaceTexture.setOnFrameAvailableListener(onFrameAvailableListener);
        }
    };

    private SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener
            = new SurfaceTexture.OnFrameAvailableListener() {
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            openglesEnv.requestRender();
        }
    };

    private void initCamera() {
        if (iCamera == null) {
            iCamera = CameraFactory.newCamera(context);
            iCamera.open(0);
            cameraInfo = iCamera.getCameraInfo();
            previewSize = (int[]) cameraInfo.get(ICamera.PARAM_PREVIEW_SIZE);
            openglesEnv = new OpenglesEnv();
            openglesEnv.initEnv();
            renderer = new CameraRenderer(context, previewSize[0], previewSize[1], mEffectListener);
            openglesEnv.setRenderer(renderer);
            renderer.addFilter(new GrayFilter(context));
            CameraDisplayWindow nativeWindow = this.nativeWindow;
            if (nativeWindow != null) {
                openglesEnv.setDisplaySurface(
                        nativeWindow.getNativeWindow(),
                        nativeWindow.getWidth(),
                        nativeWindow.getHeight());
            }

        }
    }

    private PreviewCallback previewCallback
            = new PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] buf, int width, int height, int degree, int cameraId) {
            int size = mPreviewCallback.size();
            for (int i = 0; i < size; i++) {
                mPreviewCallback.get(mPreviewCallback.keyAt(i))
                        .onPreviewFrame(buf, width, height, degree, cameraId);
            }
        }
    };

    public void takePicture(final IFrameExportCallback iFrameExportCallback) {
        mCameraHandler.post(new Runnable() {
            @Override
            public void run() {
                renderer.takePicture(iFrameExportCallback);
            }
        });
    }

    public void registerFrameExportCallback(final IFrameExportCallback exportCallback) {
        mCameraHandler.post(new Runnable() {
            @Override
            public void run() {
                renderer.registerGlesFrameExporter(exportCallback);
            }
        });
    }

    public void unregisterFrameExportCallback(final IFrameExportCallback exportCallback) {
        mCameraHandler.post(new Runnable() {
            @Override
            public void run() {
                renderer.unregisterGlesFrameExporter(exportCallback);
            }
        });
    }
}
