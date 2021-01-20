package com.opengles;

public interface GlesFilter {

    void onFilterCreated(int width, int height);

    void onFilterSizeChanged(int width, int height);

    void setInputTexture(int inputTexture);

    int getOutputTexture();

    void onDraw();

    void onFilterDestory();
}
