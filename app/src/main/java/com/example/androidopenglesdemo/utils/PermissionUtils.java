package com.example.androidopenglesdemo.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.core.app.ActivityCompat;

import static androidx.core.content.PermissionChecker.PERMISSION_DENIED;
import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

public class PermissionUtils {

    private static ArrayMap<Object, ArrayMap<String, IAction>> gActionMap
            = new ArrayMap<>();

    public static <Aty extends Activity> void doOnPermission(Aty aty,
                                            String permission,
                                            IAction<Aty> action){
        int selfPermission = ActivityCompat.checkSelfPermission(aty, permission);
        if(selfPermission == PERMISSION_GRANTED){
            action.doAction(aty);
            return;
        }
        ArrayMap<String, IAction> map = gActionMap.get(aty);
        if(map == null){
            map = new ArrayMap<>();
            gActionMap.put(aty,map);
        }
        map.put(permission, action);
        // ActivityCompat.requestPermissions(aty,new String[]{permission},);
    }

    public static <Aty> void onPermissionRequest(Aty aty, String permisson){

    }

    public static <Aty> void removeOnPermissionAction(Aty aty){
        gActionMap.remove(aty);
    }
}
