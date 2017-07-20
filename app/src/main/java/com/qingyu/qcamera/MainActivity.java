package com.qingyu.qcamera;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static int REQUEST_ORIGINAL = 2;
    private String tempfile;
    private String appPath = "ShotOnOnePlus3";
    private ImageView imageView;

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ORIGINAL){
            //Log.e("come here","come here");
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(new File(tempfile));
            intent.setData(uri);
            this.getApplicationContext().sendBroadcast(intent);
            Bitmap bitmap=BitmapFactory.decodeFile(tempfile);
            Bitmap waterMark=BitmapFactory.decodeResource(this.getResources(),R.drawable.watermark_oneplus_3);
            //Log.e("waterMark",waterMark.toString());
            imageView.setImageBitmap(bitmap);
            imageView.setImageBitmap(addWaterMark(this,bitmap,waterMark));
        }
        }

    public void initData(){
        tempfile = Environment.getExternalStorageDirectory().getPath() + File.separator + appPath + File.separator+ new Date().getTime() + ".jpg";
        Log.e("filePath",tempfile);
    }

    public void initView(){
        imageView = (ImageView) findViewById(R.id.imageView1);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_shot);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File outFile = new File(tempfile);
                if(!outFile.getParentFile().exists()){
                    outFile.getParentFile().mkdirs();
                }
                Uri uri = Uri.fromFile(outFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(intent, REQUEST_ORIGINAL);
            }
        });
    }

    public Bitmap addWaterMark(Context gContext, Bitmap src, Bitmap watermark)
        {
            String tag = "createBitmap";
            Log.d(tag, "create a new bitmap");
            if (src == null)
            {
                return null;
            }
            int w = src.getWidth();
            int h = src.getHeight();
            int ww = watermark.getWidth();
            int wh = watermark.getHeight();
            // create the new blank bitmap with same weitgh and Hight
            Bitmap newPic = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas cv = new Canvas(newPic);
            // draw src into
            cv.drawBitmap(src, 0, 0, null);
            // draw watermark into
            cv.drawBitmap(watermark, 20,h-wh, null);
            // save all clip
            cv.save(Canvas.ALL_SAVE_FLAG);
            // store
            cv.restore();
            FileOutputStream stream = null;
            try {
                stream = new FileOutputStream(tempfile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            newPic.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            return newPic;
        }
    }

