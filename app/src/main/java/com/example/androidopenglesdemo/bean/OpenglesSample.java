package com.example.androidopenglesdemo.bean;

import android.app.Activity;

public class OpenglesSample {
    private String title;
    private Class<? extends Activity> targetActivity;

    public OpenglesSample(String title, Class<? extends Activity> targetActivity) {
        this.title = title;
        this.targetActivity = targetActivity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Class<? extends Activity> getTargetActivity() {
        return targetActivity;
    }

    public void setTargetActivity(Class<? extends Activity> targetActivity) {
        this.targetActivity = targetActivity;
    }
}
