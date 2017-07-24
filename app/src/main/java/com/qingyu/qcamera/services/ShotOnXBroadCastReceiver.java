package com.qingyu.qcamera.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by QingYu on 2017/7/22.
 */

public class ShotOnXBroadCastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("Receiver","ShotOnXBroadCastReceiver" + intent.getAction());
        Cursor cursor = context.getContentResolver().query(intent.getData(),
                null, null, null, null);
        cursor.moveToFirst();
        String image_path = cursor.getString(cursor.getColumnIndex("_data"));
        Log.e("image_path",image_path);
        Toast.makeText(context, "New Photo is Saved as : -" + image_path,
                Toast.LENGTH_SHORT).show();
    }
}
