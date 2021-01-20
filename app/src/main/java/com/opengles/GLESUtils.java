package com.opengles;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

public class GLESUtils {
    private static final String TAG = "GLESUtils";

    /**
     * @param type
     * @param shaderCode
     * @return
     * @see GLES20#GL_FRAGMENT_SHADER
     * @see GLES20#GL_VERTEX_SHADER
     */
    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        if (shader == 0) {
            //todo create shader failed;
            Log.d(TAG, "loadShader: create shader failed");
        }

        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        int[] params = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, params, 0);
        if (params[0] == 0) {
            // todo could not compile shader source
            String s = GLES20.glGetShaderInfoLog(shader);
            Log.d(TAG, "loadShader: " + s);
            GLES20.glDeleteShader(shader);
        }
        return shader;
    }

    public static int createGlProgram(Context context, String[] assetsVertex, String[] assetsFrag) {
        try {
            int program = GLES20.glCreateProgram();
            for (String vertex : assetsVertex) {
                String vertexCode = IOUtils.loadAsString(context.getAssets().open(vertex));
                int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexCode);
                Log.d(TAG, "createGlProgram:vertexShader " + vertexShader);
                GLES20.glAttachShader(program, vertexShader);
            }
            for (String frag : assetsFrag) {
                String fragCode = IOUtils.loadAsString(context.getAssets().open(frag));
                int fragShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragCode);
                Log.d(TAG, "createGlProgram:fragShader " + fragShader);
                GLES20.glAttachShader(program, fragShader);
            }

            GLES20.glLinkProgram(program);
            int[] params = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, params, 0);
            if (params[0] != GLES20.GL_TRUE) {
                Log.d(TAG, "createGlProgram: could not link gles program: " + GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = -1;
            }
            Log.d(TAG, "createGlProgram: " + program);
            return program;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "createGlProgram: failed");
        return -1;
    }

    public static int createGlProgram(Context context, String assetsVertex, String assetsFrag) {
        try {
            int program = GLES20.glCreateProgram();
            String vertexCode = IOUtils.loadAsString(context.getAssets().open(assetsVertex));
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexCode);
            Log.d(TAG, "createGlProgram:vertexShader " + vertexShader);
            GLES20.glAttachShader(program, vertexShader);
            String fragCode = IOUtils.loadAsString(context.getAssets().open(assetsFrag));
            int fragShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragCode);
            Log.d(TAG, "createGlProgram:fragShader " + fragShader);
            GLES20.glAttachShader(program, fragShader);

            GLES20.glLinkProgram(program);
            int[] params = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, params, 0);
            if (params[0] != GLES20.GL_TRUE) {
                Log.d(TAG, "createGlProgram: could not link gles program: " + GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = -1;
            }
            Log.d(TAG, "createGlProgram: " + program);
            return program;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "createGlProgram: failed");
        return -1;
    }

    public static void targetTexParameterf(int target) {
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

    public static void genTextureWithParameter(
            int length, int[] textures, int offset,
            int gl_Format,
            int width, int height) {
        GLES20.glGenTextures(length,textures,offset);
        for (int i = offset; i < length; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,textures[i]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,
                    0,
                    gl_Format,
                    width,
                    height,0,
                    gl_Format,GLES20.GL_UNSIGNED_BYTE,null);
            targetTexParameterf(GLES20.GL_TEXTURE_2D);
        }
        // 清楚纹理绑定,
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,0);
    }

    public static void glBindFrameTexture(int frame, int texture) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,frame);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D,
                texture,0);
    }

    public static void glUnbindFrameTexture() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);
    }
}
