package com.qingyu.qcamera;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
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
    private String appPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "ShotOnX" + File.separator;
    private ImageView imageView;
    private static SharedPreferences sp;
    private String sShot_on;
    private String sPhote_by;
    private boolean isPersonized;
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
            personalized();
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
            Bitmap waterMark = null;
            if(!isPersonized){
                waterMark = BitmapFactory.decodeResource(this.getResources(),R.drawable.watermark_oneplus_3);
            }else
            {
                Bitmap temp = BitmapFactory.decodeResource(this.getResources(),R.drawable.watermark_empty_text);
                waterMark = drawNewWaterMark(this,temp);
            }
            //Log.e("waterMark",waterMark.toString());
            imageView.setImageBitmap(bitmap);
            imageView.setImageBitmap(addWaterMark(this,bitmap,waterMark));
        }
        }

    public void initData(){
        sp = getSharedPreferences("Shot_On", MODE_PRIVATE);
        sShot_on = sp.getString("shot_on","OnePlus 3");
        sPhote_by = sp.getString("photo_by","qingyuqy_");
        isPersonized = sp.getBoolean("isPersonized",false);
        tempfile = Environment.getExternalStorageDirectory().getPath() + File.separator + appPath + File.separator+ new Date().getTime() + ".jpg";
        Log.e("filePath",tempfile);
    }

    public void initView(){
        imageView = (ImageView) findViewById(R.id.imageView1);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_shot);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempfile = appPath+ new Date().getTime() + ".jpg";
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

    public Bitmap addWaterMark(Context context, Bitmap src, Bitmap watermark)
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
            if(w>h){

                Matrix matrix = new Matrix();
                matrix.postScale(1.1F, 1.1F);
                Bitmap temp = Bitmap.createBitmap(watermark, 0, 0, ww, wh, matrix, true);
                watermark = temp;
            }else{
                Matrix matrix = new Matrix();
                matrix.postScale(0.9F, 0.9F);
                Bitmap temp = Bitmap.createBitmap(watermark, 0, 0, ww, wh, matrix, true);
                watermark = temp;
            }
            cv.drawBitmap(watermark, 20, h - wh-20, null);
            // draw watermark into


      /*      if(isPersonized){
                cv.drawBitmap(watermark, 20,h-wh-20, null);
                String text = formatTextMark();
                TextPaint textPaint = new TextPaint();
                textPaint.setColor(Color.WHITE);
                textPaint.setTextSize(dp2px(context, 60));
                textPaint.setAntiAlias(true);
                StaticLayout layout = new StaticLayout(text, textPaint, w , Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
                cv.translate(ww+20,h-wh);
                layout.draw(cv);
            *//*    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(Color.WHITE);
                paint.setTextSize(dp2px(context, 56));
                Rect bounds = new Rect();
                paint.getTextBounds(text, 0, text.length(), bounds);*//*
                //cv.drawText(text,ww,h-wh,paint);

            }else {

            }*/

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
   public String formatTextMark(){
       String temp1 = getResources().getString(R.string.shot_on) +" " + sShot_on;
       String temp2= getResources().getString(R.string.photo_by) +" " + sPhote_by;
       Log.e("sShot_on",sShot_on);
       Log.e("sPhote_by",sPhote_by);
       return temp1 + "\r\n" + temp2;
   }

   public Bitmap drawNewWaterMark(Context context,Bitmap watermark){
       int w = watermark.getWidth();
       int h = watermark.getHeight();
       Bitmap newWaterMark = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
       Canvas canvas = new Canvas(newWaterMark);
       canvas.drawBitmap(watermark,0,0,null);
       String temp1 = getResources().getString(R.string.shot_on) +" " + sShot_on;
       String temp2= getResources().getString(R.string.photo_by) +" " + sPhote_by;
       Paint paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
       paint1.setColor(Color.WHITE);
       paint1.setTextSize(dp2px(context, 65));
       Rect bounds = new Rect();
       paint1.getTextBounds(temp1, 0, temp1.length(), bounds);

       int padding = bounds.height();
       canvas.drawText(temp1,w/5 + 20,h/2,paint1);

       Paint paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
       paint2.setColor(Color.WHITE);
       paint2.setTextSize(dp2px(context, 52));
       paint2.getTextBounds(temp2, 0, temp2.length(), bounds);
       canvas.drawText(temp2,w/5 + 20,h/2 +padding + 2,paint2);
       canvas.save(Canvas.ALL_SAVE_FLAG);
       // store
       canvas.restore();
       return newWaterMark;
   }
   public void personalized(){
        final Window win = getWindow();
        LayoutInflater mInflater = LayoutInflater.from(this);
        View popView = mInflater.inflate(R.layout.personalized, null);
        final EditText et_shot_on = (EditText) popView.findViewById(R.id.et_shoton);
        final EditText et_photo_by = (EditText) popView.findViewById(R.id.et_photoby);
        final CheckBox checkBox = (CheckBox) popView.findViewById(R.id.checkbox);
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

        if(isPersonized){
            checkBox.setChecked(true);
            et_shot_on.setEnabled(true);
            et_photo_by.setEnabled(true);
        }else {
            checkBox.setChecked(false);
            et_shot_on.setEnabled(false);
            et_photo_by.setEnabled(false);
        }
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    et_shot_on.setEnabled(true);
                    et_photo_by.setEnabled(true);
                }else {
                    et_shot_on.setEnabled(false);
                    et_photo_by.setEnabled(false);
                }
            }
        });

        sShot_on = sp.getString("shot_on","OnePlus 3");
        sPhote_by = sp.getString("photo_by","qingyuqy_");
        et_shot_on.setText(sShot_on);
        et_photo_by.setText(sPhote_by);
        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sp.edit();
                if(!checkBox.isChecked()){
                    editor.putBoolean("isPersonized",false);
                }else {
                editor.putString("shot_on",et_shot_on.getText().toString());
                editor.putString("photo_by",et_photo_by.getText().toString());
                    editor.putBoolean("isPersonized",true);}
                editor.commit();
                sShot_on = sp.getString("shot_on","OnePlus 3");
                sPhote_by = sp.getString("photo_by","qingyuqy_");
                isPersonized = sp.getBoolean("isPersonized",false);
                Toast.makeText(MainActivity.this, "Save Successfully", Toast.LENGTH_LONG).show();
                popWindow.dismiss();
            }
        });
    }

    public static void setbackgroundAlpha(float bgAlpha, Window win) {
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.alpha = bgAlpha; // 0.0-1.0
        win.setAttributes(lp);
    }

    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}

