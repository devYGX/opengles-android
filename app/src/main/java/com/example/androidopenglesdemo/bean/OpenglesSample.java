package com.example.androidopenglesdemo.bean;

import android.app.Activity;
import android.os.Bundle;

public class OpenglesSample {
    private String title;
    private Class<? extends Activity> targetActivity;
    private Bundle bundle;

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

    public OpenglesSample(String title, Class<? extends Activity> targetActivity, Bundle bundle) {
        this.title = title;
        this.targetActivity = targetActivity;
        this.bundle = bundle;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }
}
