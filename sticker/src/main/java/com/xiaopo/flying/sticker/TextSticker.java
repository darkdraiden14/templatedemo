package com.xiaopo.flying.sticker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

/**
 * Customize your sticker with text and image background.
 * You can place some text into a given region, however,
 * you can also add a plain text sticker. To support text
 * auto resizing , I take most of the code from AutoResizeTextView.
 * See https://adilatwork.blogspot.com/2014/08/android-textview-which-resizes-its-text.html
 * Notice: It's not efficient to add long text due to too much of
 * StaticLayout object allocation.
 * Created by liutao on 30/11/2016.
 */

public class TextSticker extends Sticker {

    /**
     * Our ellipsis string.
     */
    private static final String mEllipsis = "\u2026";

    private final Context context;
    private final Rect realBounds;
    private final Rect textRect;
    private final TextPaint textPaint;
    private Drawable drawable;
    private StaticLayout staticLayout;
    private Layout.Alignment alignment;
    private String text;


    /**
     * Upper bounds for text size.
     * This acts as a starting point for resizing.
     */
    private float maxTextSizePixels;

    /**
     * Lower bounds for text size.
     */
    private float minTextSizePixels;

    /**
     * Line spacing multiplier.
     */
    private float lineSpacingMultiplier = 1.0f;

    /**
     * Additional line spacing.
     */
    private float lineSpacingExtra = 0.0f;




    private float fontSize;
    private float fontScale;

    private final float minHeight=150;

    private float letterSpacing = 0;

    float textSize;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TextSticker(@NonNull Context context) {
        this(context, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TextSticker(@NonNull Context context, @Nullable Drawable drawable) {
        this.context = context;
        this.drawable = drawable;
        if (drawable == null) {
            this.drawable = ContextCompat.getDrawable(context, R.drawable.sticker_transparent_background);
        }
        xDistance=1;
        yDistance=1;

        textPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        realBounds = new Rect(0, 0, getWidth(), getHeight());
        textRect = new Rect(0, 0, getWidth(), getHeight());
        minTextSizePixels = convertSpToPx(20);
        maxTextSizePixels = convertSpToPx(32);
        alignment = Layout.Alignment.ALIGN_CENTER;
        fontSize = 20f;
        textPaint.setTextSize(fontSize);
        textPaint.setLetterSpacing(letterSpacing);
        alignment.getDeclaringClass();
        fontSize=textPaint.getTextSize();//convertSpToPx(textPaint.getTextSize());
        fontScale=1;


    }

    @Override public void draw(@NonNull Canvas canvas) {
        Matrix matrix = getMatrix();
        canvas.save();
        canvas.concat(matrix);
        if (drawable != null) {
            drawable.setBounds(realBounds);
            drawable.draw(canvas);
        }
        canvas.restore();
        canvas.save();
        canvas.concat(matrix);
        if (textRect.width() == getWidth()) {
            int dy = getHeight() / 2 - staticLayout.getHeight() / 2;
            // center vertical
            canvas.translate(0, dy);
        } else {
            int dx = textRect.left;
            int dy = textRect.top + textRect.height() / 2 - staticLayout.getHeight() / 2;
            canvas.translate(dx, dy);
        }
        staticLayout.draw(canvas);
        canvas.restore();
    }

    @Override public int getWidth() {
        return  (int)(drawable.getIntrinsicWidth() * xDistance * xScale) ;
    }

    @Override public int getHeight() {
        return  (int)(drawable.getIntrinsicHeight() * yDistance * yScale);
    }

    public void scaleText(float distance){
        float tempFontSize=fontSize*distance;
        Log.d("TextSticker","scale distance = "+distance+", scaleddensity = "+context.getResources().getDisplayMetrics().scaledDensity);
        Log.d("TextSticker","fontsize = "+fontSize+", tempFontSize = "+tempFontSize);
        drawFont(tempFontSize);
    }

    public float getFitTextSize(float newSize) {
        float nowWidth = textPaint.measureText(text);
         newSize = (float) getWidth() / nowWidth * textPaint.getTextSize();
        return newSize;
    }

    public void drawFont(){
        drawFont(fontSize);
        Log.d("TextSticker", "drawFont: "+textPaint.getTextSize());
    }

    public void drawFont(float fs){
        float minYDistance = calculateMinYDistance(fs);
//        if(yDistance<minYDistance){
            yDistance = minYDistance;
//        }
        realBounds.set(0, 0, getWidth(), getHeight());
        textRect.set(0, 0, getWidth(), getHeight());
//    setMaxTextSize(tempFontSize);
//    fontSize=tempFontSize;
        drawText(fs);
    }

    public float calculateMinYDistance(float fs){
        final CharSequence text = getText();
        int availableWidthPixels = getWidth();
        // Safety check
        // (Do not resize if the view does not have dimensions or if there is no text)
        if (text == null
                || text.length() <= 0
                || availableWidthPixels <= 0) {
            return 1;
        }
        int targetTextHeightPixels =
                getTextHeightPixels(text, availableWidthPixels, fs);

        if(targetTextHeightPixels*getMatrixScale(getMatrix())<minHeight){
            targetTextHeightPixels= (int) (minHeight/getMatrixScale(getMatrix()));
        }
        Log.d("TextSticker","targetheight = "+targetTextHeightPixels+", scaledHeight = "+targetTextHeightPixels*getMatrixScale(getMatrix()));
        return (targetTextHeightPixels/(1f*drawable.getIntrinsicHeight()));
    }

    public void drawText(float fs){
        textPaint.setTextSize(fs);
        staticLayout =
                new StaticLayout(this.text, textPaint, textRect.width(), alignment, lineSpacingMultiplier,
                        lineSpacingExtra, true);

    }


    public void onZoomFinished(float distance) {
        fontSize=fontSize*distance;
        Log.d("TextSticker", "onZoomFinished: "+textPaint.getTextSize());
        drawText(fontSize);
    }

    public void onFontSizeValue(float x)
    {
        fontSize=x;
        Log.d("TextSticker", "onFontSizeValue: "+textPaint.getTextSize());
        drawText(x);
    }

    @Override
    public void movedLeftHorizontally(float distance) {
//    xDistance += distance;
//    int oldWidth = getWidth();
        xScale = distance;
//    float tx = (getWidth()-oldWidth)/2f;
//    getMatrix().postTranslate(-tx,  0);
        moveHorizontally();
        Log.d("TextSticker", "movedLeftHorizontally: "+getFontSize());
    }

    @Override
    public void movedRightHorizontally(float distance) {
//    xDistance += distance;
        xScale = distance;
        moveHorizontally();
        Log.d("TextSticker", "movedRightHorizontally: "+getFontSize());
    }

    public void moveHorizontally() {
        Log.d("Text Sticker","width="+ getWidth());
//    Toast.makeText(context,"Sticker Text width="+getWidth(),Toast.LENGTH_LONG).show();
        realBounds.set(0, 0, getWidth(), getHeight());
        textRect.set(0, 0, getWidth(), getHeight());
//    resizeText();
//    drawText(fontSize);
        scaleText(1);
        Log.d("TextSticker", "moveHorizontally: "+getFontSize());

    }

    public void moveVertically() {
        realBounds.set(0, 0, getWidth(), getHeight());
        textRect.set(0, 0, getWidth(), getHeight());
//    resizeText();

        drawText(fontSize);
//    scaleText(1);
        Log.d("TextSticker", "moveVertically: "+getFontSize());
    }

    @Override
    public void movedTopVertically(float distance) {
//    yDistance += distance;
//    int oldHeight = getHeight();
        yScale = distance;

        float minYDistance = calculateMinYDistance(fontSize);
        if(yScale*yDistance<minYDistance){
            yScale = minYDistance/yDistance;
        }
//    float ty = (getHeight()-oldHeight)/2f;
//    getMatrix().postTranslate(0,  -ty);
        moveVertically();
        Log.d("TextSticker", "movedTopVertically: "+getFontSize());
    }

    @Override
    public void movedBottomVertically(float distance) {
//    yDistance += distance;
        yScale = distance;
        float minYDistance = calculateMinYDistance(fontSize);
        if(yScale*yDistance<minYDistance){
            yScale = minYDistance/yDistance;
        }
        moveVertically();
        Log.d("TextSticker", "movedBottomVertically: "+getFontSize());
    }

    @Override
    public void upRightHorizontally(float distance) {
        xDistance*=distance;
        xScale = 1;
        moveHorizontally();
//    getMatrix().postScale(distance,1);
        Log.d("TextSticker", "upRightHorizontally: "+getFontSize());
    }

    public void upLeftHorizontally(float distance) {
        xDistance*=distance;
        xScale = 1;
        moveHorizontally();
//    getMatrix().postScale(distance,1);
        Log.d("TextSticker", "upLeftHorizontally: "+getFontSize());
    }

    @Override
    public void upTopVertically(float distance) {
        yDistance*=distance;
        yScale = 1;
        float minYDistance = calculateMinYDistance(fontSize);
        if(yDistance<minYDistance){
            yDistance = minYDistance;
        }
        moveVertically();
        Log.d("TextSticker", "upTopVertically: "+getFontSize());
//    getMatrix().postScale(distance,1);
    }

    @Override
    public void upBottomVertically(float distance) {
        yDistance*=distance;
        yScale = 1;
        float minYDistance = calculateMinYDistance(fontSize);
        if(yDistance<minYDistance){
            yDistance = minYDistance;
        }
        moveVertically();
        Log.d("TextSticker", "upBottomVertically: "+getFontSize());
//    getMatrix().postScale(distance,1);
    }

    @Override
    public void release() {
        super.release();
        if (drawable != null) {
            drawable = null;
        }
    }

    @NonNull @Override
    public TextSticker setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        textPaint.setAlpha(alpha);
        return this;
    }

    @NonNull @Override public Drawable getDrawable() {
        return drawable;
    }

    @Override
    public TextSticker setDrawable(@NonNull Drawable drawable) {
        this.drawable = drawable;
        realBounds.set(0, 0, getWidth(), getHeight());
        textRect.set(0, 0, getWidth(), getHeight());
        return this;
    }

    @NonNull
    public TextSticker setDrawable(@NonNull Drawable drawable, @Nullable Rect region) {
        this.drawable = drawable;
        realBounds.set(0, 0, getWidth(), getHeight());
        if (region == null) {
            textRect.set(0, 0, getWidth(), getHeight());
        } else {
            textRect.set(region.left, region.top, region.right, region.bottom);
        }
        return this;
    }

    @NonNull public TextSticker setTypeface(@Nullable Typeface typeface) {
        textPaint.setTypeface(typeface);
        Log.d("TextSticker", "setTypeface: "+textPaint.getTypeface());
        return this;
    }

    @NonNull public Typeface getTypeface(){
        return textPaint.getTypeface();


    }

    @NonNull public TextSticker setUnderLine(boolean isUnderLime) {
        textPaint.setUnderlineText(isUnderLime);
        return this;
    }

    @NonNull public TextSticker setBold(boolean isBold) {
        textPaint.setFakeBoldText(isBold);
        return this;
    }

    @NonNull public TextSticker getBold(){
       textPaint.isFakeBoldText();
       return this;
    }

    @NonNull public TextSticker getItalic(){
        textPaint.getTextSkewX();
        return this;
    }

    @NonNull  public TextSticker getTextStrikeThrough(){
        textPaint.isStrikeThruText();
        return this;
    }

    @NonNull public TextSticker getUnderline()
    {
        textPaint.isUnderlineText();
        return this;
    }
    @NonNull public TextSticker setItalic( boolean isItalic ) {
        if (isItalic)
            textPaint.setTextSkewX(-0.25f);
        else
            textPaint.setTextSkewX(0);
        return this;
    }

    @NonNull public TextSticker setTextStrikeThrough(boolean isStrikeThroughRext) {
        textPaint.setStrikeThruText(isStrikeThroughRext);
        return this;
    }

    @NonNull public TextSticker setTextColor(@ColorInt int color) {
        textPaint.setColor(color);
        return this;
    }

    public  TextSticker  getTextColor(){
        textPaint.getColor();
        return this;
    }

    public TextSticker getLineSpacingMultiplier() {
        textPaint.getFontSpacing();
        return this;
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @NonNull
    public TextSticker getLetterSpacing() {
        textPaint.getLetterSpacing();
        return this;

    }

    @NonNull public TextSticker setTextAlign(@NonNull Layout.Alignment alignmnt) {
        this.alignment = alignmnt;
        Log.d("TextSticker", "setTextAlign: "+alignment +"::"+alignmnt);
        return this;
    }

    @NonNull  public  TextSticker  getAlignment( @NonNull Layout.Alignment align) {
        this.alignment = align;
        Log.d("TextSticker", "getAlignment: "+alignment +"align"+align);
        return this;
    }

    @NonNull public TextSticker setMaxTextSize(@Dimension(unit = Dimension.SP) float size) {
        minTextSizePixels =  size;
        textPaint.setTextSize(convertSpToPx(size));
        maxTextSizePixels = textPaint.getTextSize();
        fontSize = maxTextSizePixels;
        Log.d("TextSticker", "setMaxTextSize: "+size);
        return this;
    }

    @NonNull public TextSticker setFontSize(@Dimension(unit = Dimension.SP) float size) {

        fontSize = size/getCurrentScale();

        return this;
    }

    @NonNull public TextSticker setFontSizeWithoutScale(@Dimension(unit = Dimension.SP) float size) {
        fontSize = size;
        return this;
    }


    public float getFontSize() {
        return textPaint.getTextSize()*getCurrentScale();
    }
    /**
     * Sets the lower text size limit
     *
     * @param minTextSizeScaledPixels the minimum size to use for text in this view,
     * in scaled pixels.
     */
    @NonNull public TextSticker setMinTextSize(float minTextSizeScaledPixels) {
        minTextSizePixels = convertSpToPx(minTextSizeScaledPixels);
        return this;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @NonNull
    public TextSticker setLetterSpacing(float letterspacing) {
        textPaint.setLetterSpacing(letterspacing);
        letterSpacing = textPaint.getLetterSpacing();
        return this;
    }



    @NonNull public TextSticker setLineSpacing(float add, float multiplier) {

        lineSpacingMultiplier = multiplier;
        lineSpacingExtra = add;
        return this;
    }


    @NonNull public TextSticker setText(@Nullable String text) {
        this.text = text;
        return this;
    }

    @Nullable public String getText() {
        return text;
    }


    @NonNull public TextSticker resizeText() {
        drawFont();
        return this;
    }
    /**
     * Resize this view's text size with respect to its width and height
     * (minus padding). You should always call this method after the initialization.
     */
    @NonNull public TextSticker adjustFontSize() {
        final int availableHeightPixels = textRect.height();
        final int availableWidthPixels = textRect.width();
        final CharSequence text = getText();
        // Safety checkTextSticker: dr
        // (Do not resize if the view does not have dimensions or if there is no text)
        if (text == null
                || text.length() <= 0
                || availableHeightPixels <= 0
                || availableWidthPixels <= 0
                || maxTextSizePixels <= 0) {
            return this;
        }
        float targetTextSizePixels = maxTextSizePixels;
        int targetTextHeightPixels =
                getTextHeightPixels(text, availableWidthPixels, targetTextSizePixels);

        // Until we either fit within our TextView
        // or we have reached our minimum text size,
        // incrementally try smaller sizes
        while (targetTextHeightPixels > availableHeightPixels
                && targetTextSizePixels > 0 )//minTextSizePixels)
        {
            targetTextSizePixels = Math.max(targetTextSizePixels - 2, 1);//minTextSizePixels);

            targetTextHeightPixels =
                    getTextHeightPixels(text, availableWidthPixels, targetTextSizePixels);
        }

        // If we have reached our minimum text size and the text still doesn't fit,
        // append an ellipsis
        // (NOTE: Auto-ellipsize doesn't work hence why we have to do it here)
//    if (targetTextSizePixels == minTextSizePixels
//            && targetTextHeightPixels > availableHeightPixels) {
//      // Make a copy of the original TextPaint object for measuring
//      TextPaint textPaintCopy = new TextPaint(textPaint);
//      textPaintCopy.setTextSize(targetTextSizePixels);
//
//      // Measure using a StaticLayout instance
//      StaticLayout staticLayout =
//              new StaticLayout(text, textPaintCopy, availableWidthPixels, Layout.Alignment.ALIGN_NORMAL,
//                      lineSpacingMultiplier, lineSpacingExtra, false);
//
//      // Check that we have a least one line of rendered text
//      if (staticLayout.getLineCount() > 0) {
//        // Since the line at the specific vertical position would be cut off,
//        // we must trim up to the previous line and add an ellipsis
//        int lastLine = staticLayout.getLineForVertical(availableHeightPixels) - 1;
//
//        if (lastLine >= 0) {
//          int startOffset = staticLayout.getLineStart(lastLine);
//          int endOffset = staticLayout.getLineEnd(lastLine);
//          float lineWidthPixels = staticLayout.getLineWidth(lastLine);
//          float ellipseWidth = textPaintCopy.measureText(mEllipsis);
//
//          // Trim characters off until we have enough room to draw the ellipsis
//          while (availableWidthPixels < lineWidthPixels + ellipseWidth) {
//            endOffset--;
//            lineWidthPixels =
//                    textPaintCopy.measureText(text.subSequence(startOffset, endOffset + 1).toString());
//          }
//
//          setText(text.subSequence(0, endOffset) + mEllipsis);
//        }
//      }
//    }
        fontSize = targetTextSizePixels;
        drawText(targetTextSizePixels);
        return this;
    }

    /**
     * @return lower text size limit, in pixels.
     */

    public float getMinTextSizePixels() {
        return minTextSizePixels;
    }

    /**
     * Sets the text size of a clone of the view's {@link TextPaint} object
     * and uses a {@link StaticLayout} instance to measure the height of the text.
     *Q
     * @return the height of the text when placed in a view
     * with the specified width
     * and when the text has the specified size.
     */

    protected int getTextHeightPixels(@NonNull CharSequence source, int availableWidthPixels,
                                      float textSizePixels) {
        textPaint.setTextSize(textSizePixels);
        // It's not efficient to create a StaticLayout instance
        // every time when measuring, we can use StaticLayout.Builder
        // since api 23.
        StaticLayout staticLayout =
                new StaticLayout(source, textPaint, availableWidthPixels, Layout.Alignment.ALIGN_NORMAL,
                        lineSpacingMultiplier, lineSpacingExtra, true);
        return staticLayout.getHeight();
    }

    /**
     * @return the number of pixels which scaledPixels corresponds to on the device.
     */
    private float convertSpToPx(float scaledPixels) {
        return scaledPixels * context.getResources().getDisplayMetrics().scaledDensity;
    }

    private float convertPxToSp(float scaledPixels) {
        return scaledPixels / context.getResources().getDisplayMetrics().scaledDensity;
    }
}