package com.qingyu.qcamera.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.qingyu.qcamera.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by QingYu on 2017/7/23.
 */

public class ImageUtils {
    private static ImageUtils imageUtils;
    public static ImageUtils getInstance(){
        if(imageUtils == null){
            imageUtils = new ImageUtils();
        }
        return imageUtils;
    }
    public Bitmap addWaterMark(Context context, Bitmap src, Bitmap watermark)
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

    public Bitmap addTimeMark(Context context,Bitmap src) {
        if (src == null)
        {
            return null;
        }

        int w = src.getWidth();
        int h = src.getHeight();

        Bitmap newPic = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newPic);
        cv.drawBitmap(src, 0, 0, null);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String text = df.format(new Date());
        float ratio = calculateRatio(src);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextSize(dp2px(context, 45)*ratio);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        cv.drawText(text,w- bounds.width(),h-bounds.height()-20*ratio,paint);
        cv.save();
        cv.restore();
        return newPic;
    }

    public Bitmap drawNewWaterMark(Context context,Bitmap watermark,String[] texts){
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
                paints[i] .setTextSize(dp2px(context, 56));
            }else{
                paints[i] .setTextSize(dp2px(context, 45));
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
}
