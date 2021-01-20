package com.opengles;

import android.opengl.EGL14;
import android.opengl.EGLExt;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

public class OpenglesEnv {
    private static final String TAG = "OpenglesEnv";
    private int mEnvStatus;

    private static final int ENV_STATUS_DEFAULT = 0;
    private static final int ENV_STATUS_CREATED = 1;
    private static final int ENV_STATUS_DESTORY = 2;

    private GLThread glThread;

    public static final int RENDERMODE_WHEN_DIRTY = GLSurfaceView.RENDERMODE_WHEN_DIRTY;
    public static final int RENDERMODE_CONTINUOUSLY = GLSurfaceView.RENDERMODE_CONTINUOUSLY;

    private int mEGLContextClientVersion = 2;
    private Object surface;
    private int windowWidth;
    private int windowHeight;

    private final Object GLES_LOCK = new Object();

    public OpenglesEnv() {

    }

    public void initEnv() {
        if (mEnvStatus == ENV_STATUS_DESTORY) {
            throw new RuntimeException("Opengles Env Already Destory!");
        }

        if (mEnvStatus != ENV_STATUS_CREATED) {
            glThread = new GLThread(this);
            glThread.start();
            mEnvStatus = ENV_STATUS_CREATED;
        }
    }

    public void setRenderer(GlesRenderer renderer) {

        synchronized (GLES_LOCK) {
            glThread.pushRenderer(renderer);
            GLES_LOCK.notifyAll();
        }

    }

    public void setDisplaySurface(Object surface, int width, int height) {
        checkGlThread("GLThread not created, maybe you should call method initEnv() first");
        synchronized (GLES_LOCK) {
            this.surface = surface;
            this.windowWidth = width;
            this.windowHeight = height;
            glThread.createSurface = true;
            GLES_LOCK.notifyAll();
        }
    }

    public void requestRender() {
        checkGlThread("GLThread not created, maybe you should call method initEnv() first");
        synchronized (GLES_LOCK) {
            glThread.requestRender = true;
            GLES_LOCK.notifyAll();
        }
    }

    public void setRenderMode(int renderMode) {
        checkGlThread("GLThread not created, maybe you should call method initEnv() first");
        synchronized (GLES_LOCK) {
            glThread.renderMode = renderMode;
            GLES_LOCK.notifyAll();
        }
    }

    public void destoryEnv() {
        checkGlThread("GLThread not created, maybe you should call method initEnv() first");
        synchronized (GLES_LOCK) {
            glThread.mExitFlag = true;
            GLES_LOCK.notifyAll();
            try {
                GLES_LOCK.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkGlThread(String thrMsg) {
        if (glThread == null) {
            throw new RuntimeException(thrMsg);
        }
    }

    private class GLThread extends Thread {

        private final WeakReference<OpenglesEnv> envWeakRef;
        private final int mEGLContextClientVersion;
        private EGLHelper mEglHelper;
        private boolean mExitFlag;
        private GL10 mGl;
        private int renderMode;
        private boolean requestRender = true;
        private boolean mSurfaceIsBad;
        private boolean createSurface;

        private LinkedList<GlesRenderer> mRenderQueue = new LinkedList<>();

        private GlesRenderer mRenderer;
        private Object nativeSurface;

        private GLThread(OpenglesEnv env) {
            mEGLContextClientVersion = env.mEGLContextClientVersion;
            envWeakRef = new WeakReference<>(env);
        }

        public void pushRenderer(GlesRenderer renderer) {
            mRenderQueue.push(renderer);
        }

        @Override
        public void run() {
            Log.d(TAG, "run: start1");
            setName("OpenglesEnvGLThread" + getId());
            mEglHelper = new EGLHelper(mEGLContextClientVersion);
            mEglHelper.start();
            if (!mEglHelper.createSurface(null)) {
                mSurfaceIsBad = true;
            }
            mGl = mEglHelper.createGL();
            mExitFlag = false;
            Log.d(TAG, "run: start");
            try {
                while (true) {
                    synchronized (GLES_LOCK) {

                        while (true) {
                            if (mExitFlag) {
                                return;
                            }

                            GlesRenderer poll = mRenderQueue.poll();
                            if (poll != null) {
                                if (mRenderer != null) {
                                    mRenderer.onRendererDestory();
                                }
                                poll.onRendererCreated(mGl, mEglHelper.mEglConfig);
                                mRenderer = poll;
                            }
                            if (readyToDraw()) {
                                break;
                            }
                            try {
                                GLES_LOCK.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    OpenglesEnv openglesEnv = envWeakRef.get();
                    if (createSurface && openglesEnv != null) {
                        boolean lastWindowSurfaceExist = nativeSurface != null;

                        nativeSurface = openglesEnv.surface;
                        boolean createSurfaceSuccess = mEglHelper.createSurface(nativeSurface);

                        if (createSurfaceSuccess) {
                            boolean nowWindowSurfaceExist = nativeSurface != null;
                            if (lastWindowSurfaceExist != nowWindowSurfaceExist) {
                                if (nowWindowSurfaceExist) {
                                    mRenderer.onWindowSurfaceCreated(openglesEnv.windowWidth, openglesEnv.windowHeight);
                                } else {
                                    mRenderer.onWindowSurfaceDestory();
                                }
                            }else if(lastWindowSurfaceExist){
                                mRenderer.onWindowSurfaceCreated(openglesEnv.windowWidth, openglesEnv.windowHeight);
                            }
                        }
                        createSurface = false;
                    }

                    requestRender = false;
                    mRenderer.onDrawFrame(mGl);
                    int swap = mEglHelper.swap();
                    switch (swap) {
                        case EGL10.EGL_SUCCESS:
                            break;
                        case EGL11.EGL_CONTEXT_LOST:
                            Log.i("GLThread", "egl context lost tid=" + getId());
                            // todo release surface and context, re create new

                            mEglHelper.destory();
                            mEglHelper.start();
                            if (!mEglHelper.createSurface(nativeSurface)) {
                                mSurfaceIsBad = true;
                            }
                            mGl = mEglHelper.createGL();
                            break;
                        default:
                            // Other errors typically mean that the current surface is bad,
                            // probably because the SurfaceView surface has been destroyed,
                            // but we haven't been notified yet.
                            // Log the error to help developers understand why rendering stopped.
                            EGLHelper.logEglErrorAsWarning("GLThread", "eglSwapBuffers", swap);

                            synchronized (GLES_LOCK) {
                                mSurfaceIsBad = true;
                                GLES_LOCK.notifyAll();
                            }
                            break;
                    }


                }
            } finally {
                mEglHelper.destory();
                synchronized (GLES_LOCK) {
                    GLES_LOCK.notifyAll();
                }
            }
        }

        private boolean readyToDraw() {
            return !mSurfaceIsBad && mRenderer != null && (requestRender || renderMode == OpenglesEnv.RENDERMODE_CONTINUOUSLY);
        }
    }


    private static class EGLHelper {

        private EGL10 mEgl;
        private EGLDisplay mEglDisplay;
        private EGLConfigChooser mEGLConfigChooser;
        private EGLConfig mEglConfig;

        private EGLContextFactory mEGLContextFactory;
        private EGLContext mEglContext;

        private EGLSurface mEglSurface;

        EGLHelper(int version) {
            mEGLConfigChooser = new SimpleEGLConfigChooser(true, version);
            mEGLContextFactory = new DefaultContextFactory(version);
        }

        void start() {

            mEgl = (EGL10) EGLContext.getEGL();
            mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
                throw new RuntimeException("eglGetDisplay failed");
            }
            int[] version = new int[2];
            if (!mEgl.eglInitialize(mEglDisplay, version)) {
                throw new RuntimeException("eglInitialize failed");
            }

            mEglConfig = mEGLConfigChooser.chooseConfig(mEgl, mEglDisplay);

            mEglContext = mEGLContextFactory.createContext(mEgl, mEglDisplay, mEglConfig);

            if (mEglContext == null || mEglContext == EGL10.EGL_NO_CONTEXT) {
                mEglContext = null;
                throw new RuntimeException("create context failed!");
            }
        }

        private void destroySurfaceImp() {
            if (mEglSurface != null && mEglSurface != EGL10.EGL_NO_SURFACE) {
                mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_SURFACE,
                        EGL10.EGL_NO_CONTEXT);
                mEgl.eglDestroySurface(mEglDisplay, mEglSurface);
                mEglSurface = null;
            }
        }

        public boolean createSurface(Object surfaceObj) {
            /*
             *  The window size has changed, so we need to create a new
             *  surface.
             */
            destroySurfaceImp();

            if (surfaceObj != null) {
                /*
                 * Create an EGL surface we can render into.
                 */
                try {
                    mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay, mEglConfig, surfaceObj, null);
                } catch (IllegalArgumentException e) {
                    // This exception indicates that the surface flinger surface
                    // is not valid. This can happen if the surface flinger surface has
                    // been torn down, but the application has not yet been
                    // notified via SurfaceHolder.Callback.surfaceDestroyed.
                    // In theory the application should be notified first,
                    // but in practice sometimes it is not. See b/4588890
                    e.printStackTrace();
                }
            } else {
                mEglSurface = mEgl.eglCreatePbufferSurface(mEglDisplay, mEglConfig, null);
            }


            if (mEglSurface == null || mEglSurface == EGL10.EGL_NO_SURFACE) {
                int error = mEgl.eglGetError();
                if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                    Log.e("EglHelper", "createWindowSurface returned EGL_BAD_NATIVE_WINDOW.");
                }
                return false;
            }

            /*
             * Before we can issue GL commands, we need to make sure
             * the context is current and bound to a surface.
             */
            if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
                /*
                 * Could not make the context current, probably because the underlying
                 * SurfaceView surface has been destroyed.
                 */
                Log.e("EGLHelper", "eglMakeCurrent" + mEgl.eglGetError());
                return false;
            }

            return true;
        }

        public GL10 createGL() {

            return (GL10) mEglContext.getGL();
        }

        public int swap() {
            if (!mEgl.eglSwapBuffers(mEglDisplay, mEglSurface)) {
                return mEgl.eglGetError();
            }
            return EGL10.EGL_SUCCESS;
        }

        public void destory() {
            destroySurfaceImp();
            destoryContextImpl();
        }

        private void destoryContextImpl() {
            if (mEglContext != null) {
                mEGLContextFactory.destroyContext(mEgl, mEglDisplay, mEglContext);
                mEglContext = null;
            }
            if (mEglDisplay != null) {
                mEgl.eglTerminate(mEglDisplay);
                mEglDisplay = null;
            }
        }

        public static void logEglErrorAsWarning(String tag, String function, int error) {
            Log.w(tag, formatEglError(function, error));
        }

        public static String formatEglError(String function, int error) {
            return function + " failed: " + /*EGLLogWrapper.getErrorString(error)*/error;
        }
    }

    /**
     * This class will choose a RGB_888 surface with
     * or without a depth buffer.
     */
    private static class SimpleEGLConfigChooser extends ComponentSizeChooser {
        public SimpleEGLConfigChooser(boolean withDepthBuffer, int eglContextClientVersion) {
            super(8, 8, 8, 0, withDepthBuffer ? 16 : 0, 0, eglContextClientVersion);
        }
    }

    private static class ComponentSizeChooser extends BaseConfigChooser {
        public ComponentSizeChooser(int redSize, int greenSize, int blueSize,
                                    int alphaSize, int depthSize, int stencilSize, int eglContextClientVersion) {
            super(new int[]{
                    EGL10.EGL_RED_SIZE, redSize,
                    EGL10.EGL_GREEN_SIZE, greenSize,
                    EGL10.EGL_BLUE_SIZE, blueSize,
                    EGL10.EGL_ALPHA_SIZE, alphaSize,
                    EGL10.EGL_DEPTH_SIZE, depthSize,
                    EGL10.EGL_STENCIL_SIZE, stencilSize,
                    EGL10.EGL_NONE}, eglContextClientVersion);
            mValue = new int[1];
            mRedSize = redSize;
            mGreenSize = greenSize;
            mBlueSize = blueSize;
            mAlphaSize = alphaSize;
            mDepthSize = depthSize;
            mStencilSize = stencilSize;
        }

        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                                      EGLConfig[] configs) {
            for (EGLConfig config : configs) {
                int d = findConfigAttrib(egl, display, config,
                        EGL10.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config,
                        EGL10.EGL_STENCIL_SIZE, 0);
                if ((d >= mDepthSize) && (s >= mStencilSize)) {
                    int r = findConfigAttrib(egl, display, config,
                            EGL10.EGL_RED_SIZE, 0);
                    int g = findConfigAttrib(egl, display, config,
                            EGL10.EGL_GREEN_SIZE, 0);
                    int b = findConfigAttrib(egl, display, config,
                            EGL10.EGL_BLUE_SIZE, 0);
                    int a = findConfigAttrib(egl, display, config,
                            EGL10.EGL_ALPHA_SIZE, 0);
                    if ((r == mRedSize) && (g == mGreenSize)
                            && (b == mBlueSize) && (a == mAlphaSize)) {
                        return config;
                    }
                }
            }
            return null;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display,
                                     EGLConfig config, int attribute, int defaultValue) {

            if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
                return mValue[0];
            }
            return defaultValue;
        }

        private int[] mValue;
        // Subclasses can adjust these values:
        protected int mRedSize;
        protected int mGreenSize;
        protected int mBlueSize;
        protected int mAlphaSize;
        protected int mDepthSize;
        protected int mStencilSize;
    }

    private static abstract class BaseConfigChooser
            implements EGLConfigChooser {
        private int mEGLContextClientVersion;

        public BaseConfigChooser(int[] configSpec, int eglContextClientVersion) {
            mConfigSpec = filterConfigSpec(configSpec);
            mEGLContextClientVersion = eglContextClientVersion;
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int[] num_config = new int[1];
            if (!egl.eglChooseConfig(display, mConfigSpec, null, 0,
                    num_config)) {
                throw new IllegalArgumentException("eglChooseConfig failed");
            }

            int numConfigs = num_config[0];

            if (numConfigs <= 0) {
                throw new IllegalArgumentException(
                        "No configs match configSpec");
            }

            EGLConfig[] configs = new EGLConfig[numConfigs];
            if (!egl.eglChooseConfig(display, mConfigSpec, configs, numConfigs,
                    num_config)) {
                throw new IllegalArgumentException("eglChooseConfig#2 failed");
            }
            EGLConfig config = chooseConfig(egl, display, configs);
            if (config == null) {
                throw new IllegalArgumentException("No config chosen");
            }
            return config;
        }

        abstract EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                                        EGLConfig[] configs);

        protected int[] mConfigSpec;

        private int[] filterConfigSpec(int[] configSpec) {
            if (mEGLContextClientVersion != 2 && mEGLContextClientVersion != 3) {
                return configSpec;
            }
            /* We know none of the subclasses define EGL_RENDERABLE_TYPE.
             * And we know the configSpec is well formed.
             */
            int len = configSpec.length;
            int[] newConfigSpec = new int[len + 2];
            System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1);
            newConfigSpec[len - 1] = EGL10.EGL_RENDERABLE_TYPE;
            if (mEGLContextClientVersion == 2) {
                newConfigSpec[len] = EGL14.EGL_OPENGL_ES2_BIT;  /* EGL_OPENGL_ES2_BIT */
            } else {
                newConfigSpec[len] = EGLExt.EGL_OPENGL_ES3_BIT_KHR; /* EGL_OPENGL_ES3_BIT_KHR */
            }
            newConfigSpec[len + 1] = EGL10.EGL_NONE;
            return newConfigSpec;
        }
    }

    public interface EGLConfigChooser {
        /**
         * Choose a configuration from the list. Implementors typically
         * implement this method by calling
         * {@link EGL10#eglChooseConfig} and iterating through the results. Please consult the
         * EGL specification available from The Khronos Group to learn how to call eglChooseConfig.
         *
         * @param egl     the EGL10 for the current display.
         * @param display the current display.
         * @return the chosen configuration.
         */
        EGLConfig chooseConfig(EGL10 egl, EGLDisplay display);
    }

    public interface EGLContextFactory {
        EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig);

        void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context);
    }

    private static class DefaultContextFactory implements EGLContextFactory {
        private final int mEGLContextClientVersion;
        private int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

        public DefaultContextFactory(int eglContextClientVersion) {
            this.mEGLContextClientVersion = eglContextClientVersion;
        }

        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
            int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, mEGLContextClientVersion,
                    EGL10.EGL_NONE};

            return egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT,
                    mEGLContextClientVersion != 0 ? attrib_list : null);
        }

        public void destroyContext(EGL10 egl, EGLDisplay display,
                                   EGLContext context) {
            if (!egl.eglDestroyContext(display, context)) {
                Log.e("DefaultContextFactory", "display:" + display + " context: " + context);
                throw new RuntimeException("eglDestroyContex " + egl.eglGetError());
            }
        }
    }

    public interface EGLWindowSurfaceFactory {
        /**
         * @return null if the surface cannot be constructed.
         */
        EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config,
                                       Object nativeWindow);

        void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface);
    }

    private static class DefaultWindowSurfaceFactory implements EGLWindowSurfaceFactory {

        public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display,
                                              EGLConfig config, Object nativeWindow) {
            EGLSurface result = null;
            try {
                result = egl.eglCreateWindowSurface(display, config, nativeWindow, null);
            } catch (IllegalArgumentException e) {
                // This exception indicates that the surface flinger surface
                // is not valid. This can happen if the surface flinger surface has
                // been torn down, but the application has not yet been
                // notified via SurfaceHolder.Callback.surfaceDestroyed.
                // In theory the application should be notified first,
                // but in practice sometimes it is not. See b/4588890
            }
            return result;
        }

        public void destroySurface(EGL10 egl, EGLDisplay display,
                                   EGLSurface surface) {
            egl.eglDestroySurface(display, surface);
        }
    }
}
