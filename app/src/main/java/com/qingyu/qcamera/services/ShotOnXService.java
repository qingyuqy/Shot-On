package com.qingyu.qcamera.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by QingYu on 2017/7/22.
 */

public class ShotOnXService extends Service {

    ShotOnXBroadCastReceiver receiver;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {

        Log.e("ShotOnXService","ShotOnXService");
        receiver = new ShotOnXBroadCastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.camera.NEW_PICTURE");
        registerReceiver(receiver,filter);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
