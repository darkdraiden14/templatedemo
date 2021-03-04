package com.xiaopo.flying.sticker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

/**
 * @author wupanjie
 */
public class DrawableSticker extends Sticker {

    private Drawable drawable;
    private Rect realBounds;
    private final RectF imageRect;
    private final PointF mTouchOffset = new PointF();
    protected boolean shadowIcon = false;
    private Paint shadowPaint;
    private Bitmap bitmap;

    public DrawableSticker(Drawable drawable) {
        this.drawable = drawable;
        xDistance = 1;
        yDistance = 1;


        BitmapDrawable bitmapdrawable = (BitmapDrawable) drawable;
        if (bitmapdrawable.getBitmap() != null) {
            bitmap = bitmapdrawable.getBitmap();
        }
        realBounds = new Rect(0, 0, getWidth(), getHeight());
        imageRect = new RectF(0, 0, getFWidth(), getFHeight());
    }

    public DrawableSticker(Drawable drawable, boolean shadowIcon) {
        this(drawable);
        this.shadowIcon = shadowIcon;
        if (this.shadowIcon) {
            shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            shadowPaint.setStyle(Paint.Style.FILL);
            shadowPaint.setAntiAlias(true);
//      shadowPaint.setStrokeWidth(8);
            shadowPaint.setShadowLayer(30, 0, 0, Color.GRAY);


            Log.d("DrawableSticker", " shadowIcon = " + shadowIcon);
        }
    }


    @NonNull
    @Override
    public Drawable getDrawable() {
        return drawable;
    }

    @Override
    public DrawableSticker setDrawable(@NonNull Drawable drawable) {
        this.drawable = drawable;
        return this;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {

        Matrix matrix = getMatrix();
        canvas.save();
        canvas.concat(matrix);

        canvas.clipRect(realBounds);
        canvas.drawBitmap(bitmap, null, imageRect, shadowPaint);
        canvas.restore();
    }

    @NonNull
    @Override
    public DrawableSticker setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        drawable.setAlpha(alpha);
        return this;
    }

    @Override
    public int getWidth() {
        return (int) (drawable.getIntrinsicWidth() * xDistance * xScale);
    }

    @Override
    public int getHeight() {
        return (int) (drawable.getIntrinsicHeight() * yDistance * yScale);
    }

    @Override
    public float getFWidth() {
        return (drawable.getIntrinsicWidth() * xDistance * xScale);
    }

    @Override
    public float getFHeight() {
        return (drawable.getIntrinsicHeight() * yDistance * yScale);
    }


    @Override
    public void release() {
        super.release();
        if (drawable != null) {
            drawable = null;
        }
    }

    @Override
    public void movedLeftHorizontally(float distance) {
        xScale = distance;
        moveHorizontally();
    }

    @Override
    public void movedTopVertically(float distance) {
        yScale = distance;
        moveVertically();
    }

    @Override
    public void movedRightHorizontally(float distance) {
        xScale = distance;
        moveHorizontally();
    }

    @Override
    public void movedBottomVertically(float distance) {
        yScale = distance;
        moveVertically();
    }

    @Override
    public void moveHorizontally() {
        realBounds.set(0, 0, getWidth(), getHeight());
        float aspectRatio = drawable.getIntrinsicWidth() * 1f / drawable.getIntrinsicHeight();
        float height = (drawable.getIntrinsicHeight() * xDistance * xScale);
        if (height >= getFHeight()) {
            float top = ((height - getFHeight()) / 2f);//drawable.getIntrinsicHeight())/2);
            imageRect.set(0, -top, getFWidth(), height - top);
        } else {
            float width = (getFHeight() * aspectRatio);
            float left = ((width - getFWidth()) / 2f);//drawable.getIntrinsicWidth())/2);
            imageRect.set(-left, 0, width - left, getFHeight());
        }
    }

    @Override
    public void moveVertically() {
        realBounds.set(0, 0, getWidth(), getHeight());
        float aspectRatio = drawable.getIntrinsicWidth() * 1f / drawable.getIntrinsicHeight();
        float width = (drawable.getIntrinsicWidth() * yDistance * yScale);
        if (width >= getWidth()) {
            float left = ((width - getWidth()) / 2f);//drawable.getIntrinsicHeight())/2);
            imageRect.set(-left, 0, width - left, getHeight());
        } else {
            float height = (getWidth() / aspectRatio);
            float top = ((height - getHeight()) / 2f);//drawable.getIntrinsicWidth())/2);
            imageRect.set(0, -top, getWidth(), height - top);
        }
    }

    @Override
    public void upRightHorizontally(float distance) {
        xDistance *= distance;
        xScale = 1;
        moveHorizontally();
    }

    public void upLeftHorizontally(float distance) {
        xDistance *= distance;
        xScale = 1;
        moveHorizontally();
    }

    @Override
    public void upTopVertically(float distance) {
        yDistance *= distance;
        yScale = 1;
        moveVertically();
    }

    @Override
    public void upBottomVertically(float distance) {
        yDistance *= distance;
        yScale = 1;
        moveVertically();
    }


    public Bitmap resizeImage(Canvas canvas, Matrix matrix) {
        canvas.save();
        canvas.scale(xScale, yScale);
        /* draw whatever you want scaled at 0,0*/
        canvas.restore();
        return null;
    }

    public Bitmap xresizeImage(Canvas canvas, Matrix matrix) {
// convert image Drawable to bitmap
        canvas.save();
        canvas.concat(matrix);
        Bitmap bitmap = null;
//    if (drawable instanceof BitmapDrawable) {
        BitmapDrawable bitmapdrawable = (BitmapDrawable) drawable;
        if (bitmapdrawable.getBitmap() != null) {
            bitmap = bitmapdrawable.getBitmap();
        }
        Log.d("DrawableSticker", "resizebitmap bitmap != null = " + (bitmap != null));

//    final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight() / 2);
        final RectF rectF = new RectF(imageRect);
        final float roundPx = 0;

        paint.setAntiAlias(true);
//    canvas.drawARGB(0, 0, 0, 0);
//    paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
//    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//    canvas.drawBitmap(bitmap, imageRect, rect, null);
        canvas.scale(xScale, yScale);
        canvas.restore();


        return null;
    }

    public void xdraw(@NonNull Canvas canvas) {
        Matrix matrix = getMatrix();
        canvas.save();
        canvas.concat(matrix);

//    int height  = (int) (drawable.getIntrinsicHeight()*xScale);
//    if(height>getHeight()){
//      canvas.scale(xScale, xScale);
//    } else {
//      canvas.scale(yScale,yScale);
//    }
//    canvas.translate(xValue, yValue);
//    canvas.scale(xScale, yScale);
        /* draw whatever you want scaled at 0,0*/

        Rect imageBounds = new Rect(0, 0, getWidth(), getHeight()); //You should move this line to constructor. You know, performance and all.

//    canvas.save();                 //save the current clip. You can't use canvasBounds to restore it later, there is no setClipBounds()
        canvas.clipRect(imageBounds);
//    drawable.setBounds(realBounds);
//    drawable.draw(canvas);
        canvas.drawBitmap(bitmap, null, imageRect, null);
        canvas.restore();
//    resizeImage(canvas, matrix);

    }

}
