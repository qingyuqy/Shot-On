package com.qingyu.qcamera.services;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.qingyu.qcamera.MainActivity;
import com.qingyu.qcamera.receiver.ShotOnXBroadCastReceiver;
import com.qingyu.qcamera.utils.ImageUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;

/**
 * Created by QingYu on 2017/7/22.
 */

public class ShotOnXService extends Service {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final String ACTION_START = "com.qingyu.qcamera.automark.enabled";
    private static final String ACTION_STOP = "com.qingyu.qcamera.automark.disbaled";
    // ShotOnXBroadCastReceiver receiver;
   FileObserver observer;
    String defaultPath = Environment.getExternalStorageDirectory().getPath() + File.separator + Environment.DIRECTORY_DCIM + File.separator + "Camera";
    String appPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "ShotOnX" + File.separator;



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

        Log.e("ShotOnXService","ShotOnXService onStartCommand");
        String action = intent.getAction();
        if(ACTION_START.equals(action)){
            startWatching();
        }else{
            stopWatching();
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("ShotOnXService","ShotOnXService stopped");
        stopWatching();
       // unregisterReceiver(receiver);
    }

    public void startWatching(){
        observer = new FileObserver(defaultPath) {
            @Override
            public void onEvent(int event, String path) {
                if(event == FileObserver.CREATE&&!path.endsWith("lock")) {
                    drawMarker(defaultPath+File.separator+path);
                }
            }
        } ;
        observer.startWatching();
    }

    public void stopWatching(){
        if(observer!=null){
        observer.stopWatching();}
    }

    public void drawMarker(String path){
        Log.e("path",path);
        String newPath = appPath+new Date().getTime()+".jpg";
        Log.e("newPath",newPath);
        Bitmap temp = ImageUtils.getBitMap(path);
        if (temp!=null){
            temp = ImageUtils.compressImage(temp,2048,70);
            SharedPreferences sp = getSharedPreferences("Shot_On", MODE_PRIVATE);
            Bitmap newPic = ImageUtils.addWaterMark(this,sp,temp,newPath);
            //Log.e("1","1");
        }
      /*  if( ImageUtils.copyImage(path,newPath)){
            Bitmap bitmap = BitmapFactory.decodeFile(newPath);
            SharedPreferences sp = getSharedPreferences("Shot_On", MODE_PRIVATE);
            Bitmap newPic = ImageUtils.drawWaterMark(this,sp,bitmap,newPath);
        }*/
    }



}

