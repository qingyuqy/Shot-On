package com.qingyu.qcamera;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.qingyu.qcamera.services.ShotOnXService;
import com.qingyu.qcamera.utils.ImageUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.jar.Manifest;

public class MainActivity extends AppCompatActivity {
    private static int CAMERA_REQUEST_THUMD = 1;
    private static int CAMERA_REQUEST_ORIGINAL = 2;
    private static int PICTURE_SELECT = 3;
    private String tempfile;
    private String appPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "ShotOnX" + File.separator;
    private ImageView imageView;
    private static SharedPreferences sp;
    private String sShot_on;
    private String sPhote_by;
    //private boolean isPersonized;
    private String markType;
    private String markType_phone = "机型水印";
    private String markType_time = "时间水印";
    private String markType_both ="机型和时间";
    private boolean autoMark = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initData();
        initView();
    }

    @Override
    protected void onDestroy() {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("autoMark",false);
        editor.commit();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            personalized();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_REQUEST_ORIGINAL){
            //Log.e("come here","come here");

            Bitmap bitmap=BitmapFactory.decodeFile(tempfile);
            Bitmap newPic = drawWaterMark(bitmap);

            //Log.e("waterMark",waterMark.toString());
            imageView.setImageBitmap(newPic);

        }
        if(requestCode == PICTURE_SELECT){
            if (data != null) {
                Uri uri = data.getData();
                Log.e("uri", uri+"");
                ContentResolver cr = this.getContentResolver();
                Bitmap newPic = null;
                try {
                    Bitmap temp = BitmapFactory.decodeStream(cr.openInputStream(uri));
                    newPic = drawWaterMark(temp);

                } catch (FileNotFoundException e) {
                    Log.e("Exception", e.getMessage(), e);
                }
                imageView.setImageBitmap(newPic);
            }
        }
        }



    public void initData(){
        sp = getSharedPreferences("Shot_On", MODE_PRIVATE);
        sShot_on = sp.getString("shot_on","OnePlus 3");
        sPhote_by = sp.getString("photo_by","qingyuqy_");
        //isPersonized = sp.getBoolean("isPersonized",false);
        markType = sp.getString("markType",markType_phone);
        autoMark = sp.getBoolean("autoMark",false);
        tempfile = appPath + new Date().getTime() + ".jpg";
        Log.e("filePath",tempfile);
    }

    public void initView() {
        imageView = (ImageView) findViewById(R.id.imageView1);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_shot);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initNewPopMenu(MainActivity.this, view);
            }
        });
        if(autoMark){
            Intent startIntent = new Intent(MainActivity.this,ShotOnXService.class);
            startIntent.setAction("com.qingyu.qcamera.automark.enabled");
            startIntent.setPackage(getPackageName());
            startService(startIntent);
        }
    }

   public void personalized(){
        final Window win = getWindow();
        LayoutInflater mInflater = LayoutInflater.from(this);
        View popView = mInflater.inflate(R.layout.personalized, null);
        final EditText et_shot_on = (EditText) popView.findViewById(R.id.et_shoton);
        final EditText et_photo_by = (EditText) popView.findViewById(R.id.et_photoby);
       // final CheckBox cb_personized = (CheckBox) popView.findViewById(R.id.cb_personized);
        final Switch st_autoMark = (Switch) popView.findViewById(R.id.st_autoMark);
        final Spinner sp_markType = (Spinner)popView.findViewById(R.id.sp_markType);
        final Button bt_save = (Button) popView.findViewById(R.id.bt_save);

       final PopupWindow popWindow = new PopupWindow(popView,
               ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
       popWindow.setFocusable(true);
       popWindow.setBackgroundDrawable(new BitmapDrawable());
       popWindow.setOutsideTouchable(true);
       popWindow.showAtLocation(popView, Gravity.CENTER_VERTICAL
               | Gravity.CENTER_HORIZONTAL, 0, 0);
       setbackgroundAlpha(0.7f, win);
       popWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

           @Override
           public void onDismiss() {
               setbackgroundAlpha(1f, win);
           }
       });

        if(markType.equals(markType_time) ){
            //cb_personized.setChecked(true);
            et_shot_on.setEnabled(false);
            et_photo_by.setEnabled(false);
            //sp_markType.setEnabled(true);
        }else {
            //cb_personized.setChecked(false);
            et_shot_on.setEnabled(true);
            et_photo_by.setEnabled(true);
           // sp_markType.setEnabled(false);
        }
     /*  cb_personized.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    et_shot_on.setEnabled(true);
                    et_photo_by.setEnabled(true);
                    sp_markType.setEnabled(true);
                }else {
                    et_shot_on.setEnabled(false);
                    et_photo_by.setEnabled(false);
                    sp_markType.setEnabled(false);
                }
            }
        });*/
        final SharedPreferences.Editor editor = sp.edit();
        sShot_on = sp.getString("shot_on","OnePlus 3");
        sPhote_by = sp.getString("photo_by","qingyuqy_");
        markType = sp.getString("markType",markType);
        autoMark = sp.getBoolean("autoMark",false);
        Log.e("markType",markType);
        et_shot_on.setText(sShot_on);
        et_photo_by.setText(sPhote_by);
        String[] marktypes = this.getResources().getStringArray(R.array.markTypes);
        int index = Arrays.asList(marktypes).indexOf(markType);
        sp_markType.setSelection(index);
        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              /*  if(!cb_personized.isChecked()){
                    editor.putBoolean("isPersonized",false);
                }else {
                    editor.putBoolean("isPersonized",true);*/
                    if(sp_markType.getSelectedItem().equals(markType_time)){
                        editor.putString("markType",markType_time);
                    }else if(sp_markType.getSelectedItem().equals(markType_phone)){
                        editor.putString("markType",markType_phone);
                        editor.putString("shot_on",et_shot_on.getText().toString());
                        editor.putString("photo_by",et_photo_by.getText().toString());
                    } else{
                        editor.putString("markType",markType_both);
                        editor.putString("shot_on",et_shot_on.getText().toString());
                        editor.putString("photo_by",et_photo_by.getText().toString());
                    }

                editor.commit();
               /* if(cb_autoMark.isChecked()){
                    Intent startIntent = new Intent(MainActivity.this, ShotOnXService.class);
                    startService(startIntent);
                }*/
                sShot_on = sp.getString("shot_on","OnePlus 3");
                sPhote_by = sp.getString("photo_by","qingyuqy_");
               // isPersonized = sp.getBoolean("isPersonized",false);
                markType = sp.getString("markType",markType_phone);
                Log.e("markType1",markType);
                Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_LONG).show();
                popWindow.dismiss();
            }
        });
       sp_markType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

           @Override
           public void onItemSelected(AdapterView<?> parent, View view,
                                      int position, long id) {
               Spinner spinner=(Spinner) parent;
               if(markType_time.equals((String)spinner.getItemAtPosition(position))){
                   et_shot_on.setEnabled(false);
                   et_photo_by.setEnabled(false);
                   editor.commit();
               }else{
                   et_shot_on.setEnabled(true);
                   et_photo_by.setEnabled(true);
                   editor.commit();
               }
           }

           @Override
           public void onNothingSelected(AdapterView<?> parent) {
               Toast.makeText(getApplicationContext(), "没有改变的处理", Toast.LENGTH_LONG).show();
           }

       });

       st_autoMark.setChecked(autoMark);
       st_autoMark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               if(isChecked){
                   Intent startIntent = new Intent(MainActivity.this,ShotOnXService.class);
                   startIntent.setAction("com.qingyu.qcamera.automark.enabled");
                   startIntent.setPackage(getPackageName());
                   startService(startIntent);
                   Toast.makeText(getApplicationContext(), "开启成功", Toast.LENGTH_LONG).show();
                   editor.putBoolean("autoMark",true);
                   editor.commit();
               }else{
                   Intent stopIntent = new Intent(MainActivity.this,ShotOnXService.class);
                   stopIntent.setAction("com.qingyu.qcamera.automark.disabled");
                   stopIntent.setPackage(getPackageName());
                   stopService(stopIntent);
                   Toast.makeText(getApplicationContext(), "关闭成功", Toast.LENGTH_LONG).show();
                   editor.putBoolean("autoMark",false);
                   editor.commit();
               }
           }});
   }

    public static void setbackgroundAlpha(float bgAlpha, Window win) {
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.alpha = bgAlpha; // 0.0-1.0
        win.setAttributes(lp);
    }
    public void initNewPopMenu(Context context, View view) {

        PopupMenu popMenu = new PopupMenu(context, view);
        popMenu.getMenuInflater()
                .inflate(R.menu.capture_menu, popMenu.getMenu());
        popMenu.show();
        popMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem arg0) {
                switch (arg0.getItemId()) {
                    case R.id.id_menu_select:
                        tempfile = appPath+ new Date().getTime() + ".jpg";
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                        intent.setType("image/*");
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(intent, PICTURE_SELECT);
                        break;
                    case R.id.id_menu_capture:
                         tempfile = appPath+ new Date().getTime() + ".jpg";
                         Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                         File outFile = new File(tempfile);
                         if(!outFile.getParentFile().exists()){
                             outFile.getParentFile().mkdirs();
                         }
                         Uri uri = Uri.fromFile(outFile);
                         intent2.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                         startActivityForResult(intent2, CAMERA_REQUEST_ORIGINAL);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    public Bitmap drawWaterMark(Bitmap src){
        return ImageUtils.drawWaterMark(MainActivity.this,sp,src,tempfile);
    }
    /*public Bitmap drawWaterMark(Bitmap src){
        Bitmap waterMark = null;
        Bitmap newPic = null;
    *//*        if(!isPersonized){
                waterMark = BitmapFactory.decodeResource(this.getResources(),R.drawable.watermark_oneplus_3);
                newPic = addWaterMark(this,bitmap,waterMark);
            }else{*//*
        if(markType.equals(markType_time)){
            newPic = ImageUtils.addTimeMark(this,src);
        }
        else if(markType.equals(markType_phone)){
            Bitmap temp = BitmapFactory.decodeResource(this.getResources(),R.drawable.watermark_empty_text);
            String[] texts = new String[2];
            texts[0] = getResources().getString(R.string.shot_on) +" " + sShot_on;
            texts[1] = getResources().getString(R.string.photo_by) +" " + sPhote_by;
            waterMark = ImageUtils.drawNewWaterMark(this,temp,texts);
            newPic = ImageUtils.addWaterMark(this,src,waterMark);
        }
        else{
            Bitmap temp1 = ImageUtils.addTimeMark(this,src);
            Bitmap temp2 = BitmapFactory.decodeResource(this.getResources(),R.drawable.watermark_empty_text);
            String[] texts = new String[2];
            texts[0] = getResources().getString(R.string.shot_on) +" " + sShot_on;
            texts[1] = getResources().getString(R.string.photo_by) +" " + sPhote_by;
            waterMark = ImageUtils.drawNewWaterMark(this,temp2,texts);
            newPic = ImageUtils.addWaterMark(this,temp1,waterMark);
        }

        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(tempfile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        newPic.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(new File(tempfile));
        intent.setData(uri);
        this.getApplicationContext().sendBroadcast(intent);
        return newPic;
    }*/

}

