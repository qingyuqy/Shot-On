package com.qingyu.qcamera.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;

import com.qingyu.qcamera.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by QingYu on 2017/7/23.
 */

public class ImageUtils {
    static String markType_phone = "机型水印";
    static String markType_time = "时间水印";
    static String markType_both ="机型和时间";
/*    private static ImageUtils imageUtils;
    public static ImageUtils getInstance(){
        if(imageUtils == null){
            imageUtils = new ImageUtils();
        }
        return imageUtils;
    }*/

    public static Bitmap addWaterMark(Context context,SharedPreferences sp, Bitmap src,String target){

        if(src == null){
            return null;
        }
        String sShot_on = sp.getString("shot_on","Shot On OnePlus 3");
        String sPhote_by = sp.getString("photo_by","@qingyuqy_");
        String markType = sp.getString("markType",markType_phone);
        Bitmap newPic = null;
        Bitmap waterMark = null;
        Log.e("markType",markType);
        Log.e("target",target);

        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();

        switch (markType){
            case "时间水印":
                newPic = addTimeMark(context,src);
                break;
            case "机型水印":
                Bitmap temp = null;
                if(srcWidth>srcHeight){
                    temp = BitmapFactory.decodeResource(context.getResources(),R.drawable.watermark_empty_horizontal);
                }else{
                    temp = BitmapFactory.decodeResource(context.getResources(),R.drawable.watermark_empty_vertical);
                }

                String[] texts = new String[2];
                texts[0] = sShot_on;
                texts[1] = sPhote_by;
                waterMark = ImageUtils.drawMark(context,src,temp,texts);
                newPic = addPhoneMark(context,src,waterMark);
                break;
            case "机型和时间":
                Bitmap temp1 = ImageUtils.addTimeMark(context,src);
                Bitmap temp2 = null;
                if(srcWidth>srcHeight){
                    temp2 = BitmapFactory.decodeResource(context.getResources(),R.drawable.watermark_empty_horizontal);
                }else{
                    temp2 = BitmapFactory.decodeResource(context.getResources(),R.drawable.watermark_empty_vertical);
                }
                String[] texts1 = new String[2];
                texts1[0] = sShot_on;
                texts1[1] = sPhote_by;
                waterMark = ImageUtils.drawMark(context,src,temp2,texts1);
                newPic = addPhoneMark(context,temp1,waterMark);
        }
        File outFile = new File(target);
        if(!outFile.getParentFile().exists()){
            outFile.getParentFile().mkdirs();
        }
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(outFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(newPic!=null){
            newPic.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        }

        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(outFile);
        intent.setData(uri);
        context.getApplicationContext().sendBroadcast(intent);
        return newPic;
    }
    public static Bitmap addPhoneMark(Context context, Bitmap src, Bitmap watermark)
    {
        if (src == null)
        {
            return null;
        }
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int wmWidth = watermark.getWidth();
        int wmHeight = watermark.getHeight();

        Log.e("src+wm width",srcWidth + ":" + wmWidth);
        Log.e("src+wm Height",srcHeight + ":" + wmHeight);
        //create the a blank bitmap with same weitgh and Hight
        Bitmap newPic = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newPic);
        // draw src into
        cv.drawBitmap(src, 0, 0, null);
        float ratio = calculateRatio(src);
        int scaleWidth = Math.round(wmWidth * ratio);
        int scaleHeight = Math.round(wmHeight * ratio);
        Log.e("src+wm scaleWidth",srcWidth + ":" + scaleWidth);
        Log.e("src+wm scaleHeight",srcHeight + ":" + scaleHeight);
        Matrix matrix = new Matrix();
        matrix.postScale(ratio,ratio);
        Bitmap temp = Bitmap.createBitmap(watermark, 0, 0, wmWidth, wmHeight,matrix,true);
       // Bitmap temp = Bitmap.createBitmap(watermark, 0, 0, wmWidth, wmHeight);
        watermark = temp;
        cv.drawBitmap(watermark, 20*ratio, srcHeight - scaleHeight - 20*ratio, null);

        cv.save(Canvas.ALL_SAVE_FLAG);
        cv.restore();

        return newPic;
    }

    public static Bitmap addTimeMark(Context context,Bitmap src) {
        if (src == null)
        {
            return null;
        }

        int w = src.getWidth();
        int h = src.getHeight();

        Bitmap newPic = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newPic);
        cv.drawBitmap(src, 0, 0, null);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String text = df.format(new Date());
        float ratio = calculateRatio(src);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        if(w>h){
            paint.setTextSize(dp2px(context, 50)*ratio);
        }else{
            paint.setTextSize(dp2px(context, 45)*ratio);
        }

        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        cv.drawText(text,w- bounds.width()-20*ratio,h-bounds.height()-20*ratio,paint);
        cv.save();
        cv.restore();
        return newPic;
    }

    public static Bitmap drawMark(Context context,Bitmap src,Bitmap watermark,String[] texts){
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int w = watermark.getWidth();
        int h = watermark.getHeight();
        Bitmap newWaterMark = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newWaterMark);
        canvas.drawBitmap(watermark,0,0,null);

        int textCount = texts.length;
        Paint[] paints = new Paint[textCount];
        int paddingLeft = w/4;
        int paddingTop = h/2;
        for(int i = 0;i<textCount;i++){
            paints[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            paints[i] .setColor(Color.WHITE);
            if(i == 0){
                if(srcWidth>srcHeight){
                    paints[i] .setTextSize(165);
                }else {
                    paints[i] .setTextSize(148);
                }
            }else{
                if(srcWidth>srcHeight){
                    paints[i] .setTextSize(118);
                }else {
                    paints[i] .setTextSize(108);
                }
            }
            Rect bounds = new Rect();
            paints[i] .getTextBounds(texts[i], 0, texts[i].length(), bounds);
            canvas.drawText(texts[i],paddingLeft,paddingTop,paints[i]);
            paddingTop = paddingTop + bounds.height();
        }
        canvas.save();
        // store
        canvas.restore();
        return newWaterMark;
    }

    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static float calculateRatio(Bitmap src){
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        float ratioWidth;
        float ratioHeight;
        if(srcWidth>srcHeight){
            ratioWidth = (float)srcWidth/4640;
            ratioHeight = (float)srcHeight/3480;
        }else{
            ratioWidth = (float)srcWidth/3480;
            ratioHeight = (float)srcHeight/4640;
        }
        float RATIO = Math.min(ratioWidth, ratioHeight);
        Log.e("ratio",RATIO+"");
        return RATIO;
    }


    public static boolean copyImage(String fromPath, String toPath) {
        boolean result = false;
        InputStream inStream = null;
        FileOutputStream fs = null;
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldFile = new File(fromPath);
            if (oldFile.exists()&&oldFile.canRead()) {
                FileInputStream fileInputStream = new FileInputStream(oldFile);
                int len = fileInputStream.available();

                Log.e("len",len+"");
                if(len > 0){
                    byte[] bt = new byte[len];
                    int count;
                    FileOutputStream fileOutputStream = new FileOutputStream(toPath);
                    while((count=fileInputStream.read(bt)) > 0){
                        fileOutputStream.write(bt,0,count);
                    }
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    result = true;
                }
                fileInputStream.close();
                }
            }

        catch (Exception e) {
            e.printStackTrace();
        }

        Log.e("copy",result+"");
      return result;
    }

    public static Bitmap getBitMap(String path){
        Bitmap temp;
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        temp = BitmapFactory.decodeFile(path, opt);
        return temp;
    }

    public static  Bitmap compressImage(Bitmap image,int size,int options) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        image.compress(Bitmap.CompressFormat.JPEG, 80, baos);

        while (baos.toByteArray().length / 1024 > size) {
            options -= 10;
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());

        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }
}
