package com.opengles;

import java.nio.ByteBuffer;

public interface IFrameExportCallback {

    void onFrameExport(ByteBuffer byteBuffer, int width, int height);

}
