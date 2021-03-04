package com.xiaopo.flying.sticker;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class AutoResizeTextSticker extends Sticker {
    private interface SizeTester {
        /**
         *
         * @param suggestedSize
         *            Size of text to be tested
         * @param availableSpace
         *            available space in which text must fit
         * @return an integer < 0 if after applying {@code suggestedSize} to
         *         text, it takes less space than {@code availableSpace}, > 0
         *         otherwise
         */
        public int onTestSize(int suggestedSize, RectF availableSpace);
    }

    private RectF mTextRect = new RectF();

    private RectF mAvailableSpaceRect;

    private Rect mRealBoud;

    private SparseIntArray mTextCachedSizes;

    private TextPaint mPaint;

    private float mMaxTextSize;

    private float mSpacingMult = 1.0f;

    private float mSpacingAdd = 0.0f;

    private float mMinTextSize = 20;

    private int mWidthLimit;

    private static final int NO_LINE_LIMIT = -1;
    private int mMaxLines;

    private boolean mEnableSizeCache = true;
    private boolean mInitiallized;

    private Context mContext;
    private String mText;

    private Drawable mDrawable;

    private StaticLayout mStaticLayout;

    private Layout.Alignment mAlignment;

    public AutoResizeTextSticker(Context context) {
        this.mContext = context;
        initialize();
    }

    public AutoResizeTextSticker(Context context, AttributeSet attrs) {
        this.mContext = context;

        initialize();
    }

    public AutoResizeTextSticker(Context context, AttributeSet attrs, int defStyle) {
        this.mContext = context;

        initialize();
    }

    private void initialize() {
        mPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        mMaxTextSize = Utils.convertSpToPx(mContext, 32);
        mAvailableSpaceRect = new RectF();
        mTextCachedSizes = new SparseIntArray();
        if (mMaxLines == 0) {
            // no value was assigned during construction
            mMaxLines = NO_LINE_LIMIT;
        }
        mInitiallized = true;
        this.mDrawable = ContextCompat.getDrawable(mContext, R.drawable.sticker_transparent_background);

    }


    private String getText()
    {
        return mText;
    }

    public void setText(final String text) {
        this.mText = text.toString();
        adjustTextSize(text.toString());
    }


    public void setTextSize(float size) {
        mMaxTextSize = size;
        mPaint.setTextSize(size);
        mTextCachedSizes.clear();
        adjustTextSize(getText().toString());
    }


    public void setMaxLines(int maxlines) {
        mMaxLines = maxlines;
        reAdjust();
    }

    public int getMaxLines() {
        return mMaxLines;
    }


    public void setSingleLine() {
        mMaxLines = 1;
        reAdjust();
    }


    public void setSingleLine(boolean singleLine) {
        if (singleLine) {
            mMaxLines = 1;
        } else {
            mMaxLines = NO_LINE_LIMIT;
        }
        reAdjust();
    }


    public void setLines(int lines) {
        mMaxLines = lines;
        reAdjust();
    }


    public void setTextSize(int unit, float size) {

        Context c = mContext;
        Resources r;

        if (c == null)
            r = Resources.getSystem();
        else
            r = c.getResources();
        mMaxTextSize = TypedValue.applyDimension(unit, size,
                r.getDisplayMetrics());
        mTextCachedSizes.clear();
        mPaint.setTextSize(mMaxTextSize);
    }


    public void setLineSpacing(float add, float mult) {
        mSpacingMult = mult;
        mSpacingAdd = add;
    }

    /**
     * Set the lower text size limit and invalidate the view
     *
     * @param minTextSize
     */
    public void setMinTextSize(float minTextSize) {
        mMinTextSize = minTextSize;
        reAdjust();
    }

    private void reAdjust() {
        adjustTextSize(getText().toString());
    }


    private void adjustTextSize(String string) {
        if (!mInitiallized) {
            return;
        }
        int startSize = (int) mMinTextSize;
        int heightLimit = getHeight();
        mWidthLimit = getWidth();
        mAvailableSpaceRect.right = mWidthLimit;
        mAvailableSpaceRect.bottom = heightLimit;
        setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                efficientTextSizeSearch(startSize, (int) mMaxTextSize,
                        mSizeTester, mAvailableSpaceRect));
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Matrix matrix = getMatrix();
        canvas.save();
        canvas.concat(matrix);
        if (mDrawable != null) {
            if (mRealBoud == null)
                mRealBoud = new Rect();
                        mAvailableSpaceRect.roundOut(mRealBoud);
            mDrawable.setBounds(mRealBoud);
            mDrawable.draw(canvas);
        }
        canvas.restore();

        canvas.save();
        canvas.concat(matrix);
        if (mTextRect.width() == getWidth()) {
            int dy = getHeight() / 2 - mStaticLayout.getHeight() / 2;
            // center vertical
            canvas.translate(0, dy);
        } else {
            float dx = mTextRect.left;
            float dy = mTextRect.top + mTextRect.height() / 2 - mStaticLayout.getHeight() / 2;
            canvas.translate(dx, dy);
        }
        mStaticLayout.draw(canvas);
        canvas.restore();
    }



    @NonNull
    public AutoResizeTextSticker setTypeface(@Nullable Typeface typeface) {
        mPaint.setTypeface(typeface);
        return this;
    }

    @NonNull
    public AutoResizeTextSticker setUnderLine(boolean isUnderLime) {
        mPaint.setUnderlineText(isUnderLime);
        return this;
    }

    @NonNull
    public AutoResizeTextSticker setBold(boolean isBold) {
        mPaint.setFakeBoldText(isBold);
        return this;
    }

    @NonNull
    public AutoResizeTextSticker setItalic( boolean isItalic ) {
        if (isItalic)
            mPaint.setTextSkewX(-0.25f);
        else
            mPaint.setTextSkewX(0);


        return this;
    }


    @NonNull
    public AutoResizeTextSticker setTextStrikeThrough(boolean isStrikeThroughRext) {
        mPaint.setStrikeThruText(isStrikeThroughRext);
        return this;
    }

    @NonNull
    public AutoResizeTextSticker setTextColor(@ColorInt int color) {
        mPaint.setColor(color);
        return this;
    }

    @NonNull
    public AutoResizeTextSticker setTextAlign(@NonNull Layout.Alignment alignment) {
        this.mAlignment = alignment;
        return this;
    }



    private final SizeTester mSizeTester = new SizeTester() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public int onTestSize(int suggestedSize, RectF availableSPace) {
            mPaint.setTextSize(suggestedSize);
            String text = getText().toString();
            boolean singleline = getMaxLines() == 1;
            if (singleline) {
                mTextRect.bottom = mPaint.getFontSpacing();
                mTextRect.right = mPaint.measureText(text);
                mStaticLayout = new StaticLayout(text, mPaint,
                        (int) mTextRect.width(), Layout.Alignment.ALIGN_NORMAL, mSpacingMult,
                        mSpacingAdd, true);
            } else {
                mStaticLayout = new StaticLayout(text, mPaint,
                        mWidthLimit, Layout.Alignment.ALIGN_NORMAL, mSpacingMult,
                        mSpacingAdd, true);
                // return early if we have more lines
                if (getMaxLines() != NO_LINE_LIMIT
                        && mStaticLayout.getLineCount() > getMaxLines()) {
                    return 1;
                }
                mTextRect.bottom = mStaticLayout.getHeight();
                int maxWidth = -1;
                for (int i = 0; i < mStaticLayout.getLineCount(); i++) {
                    if (maxWidth < mStaticLayout.getLineWidth(i)) {
                        maxWidth = (int) mStaticLayout.getLineWidth(i);
                    }
                }
                mTextRect.right = maxWidth;
            }

            mTextRect.offsetTo(0, 0);
            if (availableSPace.contains(mTextRect)) {
                // may be too small, don't worry we will find the best match
                return -1;
            } else {
                // too big
                return 1;
            }
        }
    };

    /**
     * Enables or disables size caching, enabling it will improve performance
     * where you are animating a value inside TextView. This stores the font
     * size against getText().length() Be careful though while enabling it as 0
     * takes more space than 1 on some fonts and so on.
     *
     * @param enable
     *            enable font size caching
     */
    public void enableSizeCache(boolean enable) {
        mEnableSizeCache = enable;
        mTextCachedSizes.clear();
        adjustTextSize(getText().toString());
    }

    private int efficientTextSizeSearch(int start, int end,
                                        SizeTester sizeTester, RectF availableSpace) {
        if (!mEnableSizeCache) {
            return binarySearch(start, end, sizeTester, availableSpace);
        }
        String text = getText().toString();
        int key = text == null ? 0 : text.length();
        int size = mTextCachedSizes.get(key);
        if (size != 0) {
            return size;
        }
        size = binarySearch(start, end, sizeTester, availableSpace);
        mTextCachedSizes.put(key, size);
        return size;
    }

    private static int binarySearch(int start, int end, SizeTester sizeTester,
                                    RectF availableSpace) {
        int lastBest = start;
        int lo = start;
        int hi = end - 1;
        int mid = 0;
        while (lo <= hi) {
            mid = (lo + hi) >>> 1;
            int midValCmp = sizeTester.onTestSize(mid, availableSpace);
            if (midValCmp < 0) {
                lastBest = lo;
                lo = mid + 1;
            } else if (midValCmp > 0) {
                hi = mid - 1;
                lastBest = hi;
            } else {
                return mid;
            }
        }
        // make sure to return last best
        // this is what should always be returned
        return lastBest;

    }

    @Override public int getWidth() {
        return  (int)(mDrawable.getIntrinsicWidth() * xScale) ;
    }

    @Override public int getHeight() {
        return  (int)(mDrawable.getIntrinsicHeight() * yScale);
    }

    @Override
    public void movedRightHorizontally(float scale) {
        xScale = scale;

        mAvailableSpaceRect.set(0, 0, getWidth(), getHeight());
        mTextRect.set(0, 0, getWidth(), getHeight());
        reAdjust();
    }
    @Override
    public void movedLeftHorizontally(float scale) {
        xScale = scale;

        mAvailableSpaceRect.set(0, 0, getWidth(), getHeight());
        mTextRect.set(0, 0, getWidth(), getHeight());
        reAdjust();
    }

//    @Override
//    public void movedVertically(float scale) {
//
//        yScale = scale;
//        mAvailableSpaceRect.set(0, 0, getWidth(), getHeight());
//        mTextRect.set(0, 0, getWidth(), getHeight());
//        reAdjust();
//    }

    @Override
    public void movedTopVertically(float distance) {

    }

    @Override
    public void movedBottomVertically(float distance)
    {

    }

    @Override public void release() {
        super.release();
        if (mDrawable != null) {
            mDrawable = null;
        }
    }


    @Override
    public Sticker setDrawable(@NonNull Drawable drawable) {
        this.mDrawable = drawable;
        return this;
    }

    @NonNull
    @Override
    public Drawable getDrawable() {
        return mDrawable;
    }

    @NonNull
    @Override
    public Sticker setAlpha(int alpha) {
        return null;
    }
}
