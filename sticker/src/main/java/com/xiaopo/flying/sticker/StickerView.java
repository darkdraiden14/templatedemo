package com.xiaopo.flying.sticker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.nfc.Tag;
import android.os.Build;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.ViewCompat;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sticker View
 * @author wupanjie
 */
public class StickerView extends FrameLayout {


  private boolean showIcons;
  private boolean showCurrentActionIcon;
  private boolean showMoveIcons;
  private boolean showBorder;
  private boolean mIsExpandOrCollapse;
  private boolean mIsAdLoading;
  private boolean mIsAspectRatioSelected;
  private boolean isAutoSnapOn;
  private boolean isRulerLineOn;
  private final boolean bringToFrontCurrentSticker;
  private boolean isBgLock;
  private boolean isCanvasLock;

  public boolean isBgLock() {
    return isBgLock;
  }

  public void setBgLock(boolean bgLock) {
    isBgLock = bgLock;
  }

  @IntDef({
          ActionMode.NONE, ActionMode.DRAG, ActionMode.ZOOM_WITH_TWO_FINGER, ActionMode.ICON,
          ActionMode.CLICK
  })
  @Retention(RetentionPolicy.SOURCE)
  protected @interface ActionMode {
    int NONE = 0;
    int DRAG = 1;
    int ZOOM_WITH_TWO_FINGER = 2;
    int ICON = 3;
    int CLICK = 4;
  }

  @IntDef(flag = true, value = {FLIP_HORIZONTALLY, FLIP_VERTICALLY})
  @Retention(RetentionPolicy.SOURCE)
  protected @interface Flip {
  }

  private static final String TAG = "StickerView";

  private static final int DEFAULT_MIN_CLICK_DELAY_TIME = 200;

  public static final int FLIP_HORIZONTALLY = 1;
  public static final int FLIP_VERTICALLY = 1 << 1;

  public static final float OFFSET_RULER = 20f;

  private final List<Sticker> stickers = new ArrayList<>();
  private final List<BitmapStickerIcon> icons = new ArrayList<>(5);

  private final Paint borderPaint = new Paint();
  private final Paint rotationPaint=new Paint();
  private final Paint objectRulerPaint=new Paint();
  private final Paint blurPaint = new Paint();
  private final Paint iconPaint = new Paint();
  private final RectF stickerRect = new RectF();

  private final Matrix sizeMatrix = new Matrix();
  private final Matrix downMatrix = new Matrix();
  private final Matrix moveMatrix = new Matrix();

  // region storing variables
  private final float[] bitmapPoints = new float[8];
  private final float[] bounds = new float[8];
  private final float[] point = new float[2];
  private final PointF currentCenterPoint = new PointF();
  private final float[] tmp = new float[2];
  private PointF midPoint = new PointF();
  private List<PointF> midPointList = new ArrayList<>();
  private List<PointF> snapPointList = new ArrayList<>();

  // endregion
  private final int touchSlop;
  private int mTileWidth, mTileHeight;
  private int mSliceCount;
  private BitmapStickerIcon currentIcon;
  //the first point down position
  private float downX;
  private float downY;

  private float oldDistance = 0f;
  private float oldRotation = 0f;
  private float ooldDistance = 0f;
  private float pastMovedDistance = 0f;


  @ActionMode
  private int currentMode = ActionMode.NONE;

  private Sticker handlingSticker;

  private boolean locked;
  private boolean constrained;


  private boolean isTouchLocked =  false;


  private boolean isSelected;
  private OnStickerOperationListener onStickerOperationListener;

  private long lastClickTime = 0;
  private int minClickDelayTime = DEFAULT_MIN_CLICK_DELAY_TIME;
  int offsetX = 0;
  int offsetY = 0;
  int blurRadius = 5;
  boolean isVibratingAngle=false;

  public StickerView(Context context) {
    this(context, null);
  }

  public StickerView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public StickerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    isSelected = true;
    touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    TypedArray a = null;
    try {
      a = context.obtainStyledAttributes(attrs, R.styleable.StickerView);
      showIcons = a.getBoolean(R.styleable.StickerView_showIcons, false);
      showBorder = a.getBoolean(R.styleable.StickerView_showBorder, false);
      bringToFrontCurrentSticker =
              a.getBoolean(R.styleable.StickerView_bringToFrontCurrentSticker, false);

      borderPaint.setAntiAlias(true);
      iconPaint.setAntiAlias(true);
      iconPaint.setColor(Color.TRANSPARENT);
      borderPaint.setColor(a.getColor(R.styleable.StickerView_borderColor, Color.WHITE));

//            borderPaint.setAlpha(a.getInteger(R.styleable.StickerView_borderAlpha, 128));
      borderPaint.setStrokeWidth(4);

      rotationPaint.setStrokeWidth(4);
      rotationPaint.setAntiAlias(true);
      rotationPaint.setColor(a.getColor(R.styleable.StickerView_borderColor, Color.GRAY));

      objectRulerPaint.setStrokeWidth(2);
      objectRulerPaint.setAntiAlias(true);
      objectRulerPaint.setColor(a.getColor(R.styleable.StickerView_borderColor, Color.BLUE));

      blurPaint.setAntiAlias(true);
      blurPaint.setStyle(Paint.Style.FILL);
      blurPaint.setColor(Color.GRAY);
//            blurPaint.setColor(Color.parseColor("#ececec"));
      blurPaint.setStrokeWidth(8);
      blurPaint.setMaskFilter(new BlurMaskFilter(
              blurRadius /* shadowRadius */,
              BlurMaskFilter.Blur.NORMAL));
      configDefaultIcons();
    } finally {
      if (a != null) {
        a.recycle();
      }

    }
  }


  public void configDefaultIcons() {

    BitmapStickerIcon rotateIcon = new BitmapStickerIcon(ContextCompat.getDrawable(getContext(),
            R.drawable.rotate),
            BitmapStickerIcon.MID_BOTTOM_CENTER);
    rotateIcon.setIconEvent(new RotateIconEvent());

    BitmapStickerIcon deleteIcon = new BitmapStickerIcon(
            ContextCompat.getDrawable(getContext(), R.drawable.rect_top_right_shadow),
            BitmapStickerIcon.RIGHT_TOP);
    deleteIcon.setIconEvent(new DeleteIconEvent());

    BitmapStickerIcon zoomTopLeft = new BitmapStickerIcon(
            ContextCompat.getDrawable(getContext(), R.drawable.rect_top_left_shadow),
            BitmapStickerIcon.LEFT_TOP);
    zoomTopLeft.setIconEvent(new ZoomIconEvent());

    BitmapStickerIcon zoomBottomLeft = new BitmapStickerIcon(
            ContextCompat.getDrawable(getContext(),R.drawable.rect_bottom_left_shadow),
            BitmapStickerIcon.LEFT_BOTTOM);
    zoomBottomLeft.setIconEvent(new ZoomIconEvent());

    BitmapStickerIcon zoomBottomRight = new BitmapStickerIcon(ContextCompat.getDrawable(getContext(),
            R.drawable.rect_bottom_right_shadow), BitmapStickerIcon.RIGHT_BOTOM);
    zoomBottomRight.setIconEvent(new ZoomIconEvent());

    BitmapStickerIcon topMiddleIcon = new BitmapStickerIcon(ContextCompat.getDrawable(getContext(),
            R.drawable.rect_top_middle),BitmapStickerIcon.MID_TOP);
    topMiddleIcon.setIconEvent(new BoundBoxTopVerticalMoveEvent());

    BitmapStickerIcon leftMiddleIcon = new BitmapStickerIcon(ContextCompat.getDrawable(getContext(),
            R.drawable.rect_left_middle),BitmapStickerIcon.MID_LEFT);
    leftMiddleIcon.setIconEvent(new BoundBoxLeftHorizontalMoveEvent());

    BitmapStickerIcon rightMiddleIcon = new BitmapStickerIcon(ContextCompat.getDrawable(getContext(),
            R.drawable.rect_right_middle),BitmapStickerIcon.MID_RIGHT);
    rightMiddleIcon.setIconEvent(new BoundBoxRightHorizontalMoveEvent());


    BitmapStickerIcon bottomMiddleIcon = new BitmapStickerIcon(ContextCompat.getDrawable(getContext(),
            R.drawable.rect_bottom_middle),BitmapStickerIcon.MID_BOTTOM);
    bottomMiddleIcon.setIconEvent(new BoundBoxBottomVerticalMoveEvent());


    BitmapStickerIcon rightObjectMoveIcon = new BitmapStickerIcon(ContextCompat.getDrawable(getContext(),
            R.drawable.rect_right_holder),BitmapStickerIcon.MID_RIGHT_CENTER);
    rightObjectMoveIcon.setIconEvent(new ImageDragEvent());


    icons.clear();
    icons.add(rotateIcon);
    icons.add(rightObjectMoveIcon);
    icons.add(deleteIcon);
    icons.add(zoomTopLeft);
    icons.add(zoomBottomLeft);
    icons.add(zoomBottomRight);
    icons.add(topMiddleIcon);
    icons.add(leftMiddleIcon);
    icons.add(rightMiddleIcon);
    icons.add(bottomMiddleIcon);

  }

  public void  rotateVibrator(){
    Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.CONTENTS_FILE_DESCRIPTOR));
    } else {
      //deprecated in API 26
      v.vibrate(50);
    }
  }



  public boolean isAutoSnapOn() {
    return isAutoSnapOn;
  }

  public void setAutoSnapOn(boolean autoSnapOn) {
    isAutoSnapOn = autoSnapOn;
  }

  public boolean isRulerLineOn() {
    return isRulerLineOn;
  }

  public void setRulerLineOn(boolean rulerLineOn) {
    isRulerLineOn = rulerLineOn;
  }


  /**
   * Swaps sticker at layer [[oldPos]] with the one at layer [[newPos]].
   * Does nothing if either of the specified layers doesn't exist.
   */
  public void swapLayers(int oldPos, int newPos)
  {
    if (stickers.size() > oldPos && stickers.size() > newPos)
    {
      Collections.swap(stickers, oldPos, newPos);
      invalidate();
    }
  }

  /**
   * Sends sticker from layer [[oldPos]] to layer [[newPos]].
   * Does nothing if either of the specified layers doesn't exist.
   */
  public void sendToLayer(int oldPos, int newPos) {
    if (stickers.size() >= oldPos && stickers.size() >= newPos) {
      Sticker s = stickers.get(oldPos);
      stickers.remove(oldPos);
      stickers.add(newPos, s);
      invalidate();
    }
  }

  @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    if (changed) {
      stickerRect.left = left;
      stickerRect.top = top;
      stickerRect.right = right;
      stickerRect.bottom = bottom;
    }
  }

  @Override protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);
    drawStickers(canvas);
  }

  public void resetIcons()
  {
    showBorder = false;
    showIcons  = false;

  }

  public void drawTextOnCanvas(Canvas canvas,float rotationDegree) {
    canvas.drawText(String.valueOf(rotationDegree),200,300,borderPaint);
    borderPaint.setTextSize(50);
  }

  public PointF findObjectSnapOffset(float offset) {
    float arr[] = new float[8];
    getStickerPoints(handlingSticker,arr);
    float p1=arr[0];
    float q1=arr[1];
    float p2=arr[2];
    float q2=arr[3];
    float p3=arr[4];
    float q3=arr[5];
    float p4=arr[6];
    float q4=arr[7];
    float midP=(p1+p4)/2;
    float midQ=(q1+q4)/2;
    float arrP[] = new float[]{p1, midP, p2};
    float arrQ[] = new float[]{q1, midQ, q3};

    boolean foundX = false;
    boolean foundY = false;
    PointF snapPoint = new PointF();
    for (int i = 0; i < stickers.size(); i++)
    {
      Sticker sticker = stickers.get(i);
      if (handlingSticker != sticker)
      {
        float arrPoints[] = new float[8];
        getStickerPoints(sticker,arrPoints);
        float x1=arrPoints[0];
        float y1=arrPoints[1];
        float x2=arrPoints[2];
        float y2=arrPoints[3];
        float x3=arrPoints[4];
        float y3=arrPoints[5];
        float x4=arrPoints[6];
        float y4=arrPoints[7];
        float midX=(x1+x4)/2;
        float midY=(y1+y4)/2;

        if(!foundX) {
          float arrX[] = new float[]{x1, midX, x2};
          for (float xpt : arrX) {
            for (float ppt : arrP) {
              if (Math.abs(xpt - ppt) <= offset) {
                snapPoint.x = xpt - ppt;
                foundX = true;
                break;
              }
            }
            if (foundX)
              break;
          }
        }

        if(!foundY) {
          float arrY[] = new float[]{y1, midY, y3};
          for (float ypt : arrY) {
            for (float qpt : arrQ) {
              if (Math.abs(ypt - qpt) <= offset) {
                snapPoint.y = ypt - qpt;
                foundY = true;
                break;
              }
            }
            if (foundY)
              break;
          }
        }
        if (foundX && foundY) {
          return snapPoint;
        }
      }
    }

    for (PointF sp: snapPointList) {
      if(!foundX) {
        float xpt = sp.x;
        for (float ppt : arrP) {
          if (Math.abs(xpt - ppt) <= offset) {
            snapPoint.x = xpt - ppt;
            foundX = true;
            break;
          }
        }
      }
      if(!foundY) {
        float ypt = sp.y;
        for (float qpt : arrQ) {
          if (Math.abs(ypt - qpt) <= offset) {
            snapPoint.y = ypt - qpt;
            foundY = true;
            break;
          }
        }
      }
      if (foundX && foundY) {
        return snapPoint;
      }
    }

    if (foundX || foundY){
      if(!foundX) {
        snapPoint.x = 0;
      } else if(!foundY) {
        snapPoint.y = 0;
      }
      return snapPoint;
    }
    return null;
  }

  public ArrayList<Set<Float>> findObjectRulerPoint(float offset) {
    float arr[] = new float[8];
    getStickerPoints(handlingSticker,arr);
    float p1=arr[0];
    float q1=arr[1];
    float p2=arr[2];
    float q2=arr[3];
    float p3=arr[4];
    float q3=arr[5];
    float p4=arr[6];
    float q4=arr[7];
    float midP=(p1+p4)/2;
    float midQ=(q1+q4)/2;
    float arrP[] = new float[]{p1, midP, p2};
    float arrQ[] = new float[]{q1, midQ, q3};

    ArrayList<Set<Float>> snapPoints = new ArrayList<>();
    snapPoints.add(new HashSet<Float>());//0 index for - x coordinate
    snapPoints.add(new HashSet<Float>());//1 index for - y coordinate
    for (int i = 0; i < stickers.size(); i++)
    {
      Sticker sticker = stickers.get(i);
      if (handlingSticker != sticker)
      {
        float arrPoints[] = new float[8];
        getStickerPoints(sticker,arrPoints);
        float x1=arrPoints[0];
        float y1=arrPoints[1];
        float x2=arrPoints[2];
        float y2=arrPoints[3];
        float x3=arrPoints[4];
        float y3=arrPoints[5];
        float x4=arrPoints[6];
        float y4=arrPoints[7];
        float midX=(x1+x4)/2;
        float midY=(y1+y4)/2;

        float arrX[] = new float[]{x1, midX, x2};
        for(float xpt: arrX){
          for(float ppt: arrP) {
            if (Math.abs(xpt - ppt) <= offset) {
              snapPoints.get(0).add(xpt);
            }
          }
        }

        float arrY[] = new float[]{y1, midY, y3};
        for(float ypt: arrY){
          for(float qpt: arrQ) {
            if (Math.abs(ypt - qpt) <= offset) {
              snapPoints.get(1).add(ypt);
            }
          }
        }
      }
    }
    for (PointF snapPoint: snapPointList) {
      float xpt = snapPoint.x;
      float ypt = snapPoint.y;
      for(float ppt: arrP) {
        if (Math.abs(xpt - ppt) <= offset) {
          snapPoints.get(0).add(xpt);
        }
      }
      for(float qpt: arrQ) {
        if (Math.abs(ypt - qpt) <= offset) {
          snapPoints.get(1).add(ypt);
        }
      }
    }
    return snapPoints;
  }

  public PointF findObjectSnapOffsetForResize(float offset) {
    float arr[] = new float[8];
    getStickerPoints(handlingSticker,arr);

    float minP=Float.MAX_VALUE;
    float minQ=Float.MAX_VALUE;
    float maxP=Float.MIN_VALUE;
    float maxQ=Float.MIN_VALUE;

    for(int i = 0 ;i < 4 ; i++){
      float temp = arr[i*2];
      if(temp < minP){
        minP = temp;
      }
      else if(temp > maxP){
        maxP = temp;
      }
    }
    for(int i = 0; i < 4; i++){
      float temp = arr[i*2+1];
      if(temp < minQ){
        minQ = temp;
      }
      else if(temp > maxQ){
        maxQ = temp;
      }
    }

    float arrP[] = new float[]{minP, maxP};
    float arrQ[] = new float[]{minQ, maxQ};

    boolean foundX = false;
    boolean foundY = false;
    PointF snapPoint = new PointF();
    for (int i = 0; i < stickers.size(); i++)
    {
      Sticker sticker = stickers.get(i);
      if (handlingSticker != sticker)
      {
        float arrPoints[] = new float[8];
        getStickerPoints(sticker,arrPoints);

        float minX=Float.MAX_VALUE;
        float minY=Float.MAX_VALUE;
        float maxX=Float.MIN_VALUE;
        float maxY=Float.MIN_VALUE;

        for(int j = 0 ; j < 4 ; j++){
          float tempX = arrPoints[j*2];
          if(tempX < minX){
            minX = tempX;
          }
          else if(tempX > maxX){
            maxX = tempX;
          }
        }
        for(int k = 0; k < 4; k++){
          float tempY = arrPoints[k*2+1];
          if(tempY < minY){
            minY = tempY;
          }
          else if(tempY > maxY){
            maxY = tempY;
          }
        }

        if(!foundX) {
          float arrX[] = new float[]{minX, maxX};
          for (float xpt : arrX) {
            for (float ppt : arrP) {
              if (Math.abs(xpt - ppt) <= offset) {
                snapPoint.x = xpt - ppt;
                foundX = true;
                break;
              }
            }
            if (foundX)
              break;
          }
        }

        if(!foundY) {
          float arrY[] = new float[]{minY, maxY};
          for (float ypt : arrY) {
            for (float qpt : arrQ) {
              if (Math.abs(ypt - qpt) <= offset) {
                snapPoint.y = ypt - qpt;
                foundY = true;
                break;
              }
            }
            if (foundY)
              break;
          }
        }
        if (foundX && foundY) {
          return snapPoint;
        }
      }
    }

    for (PointF sp: snapPointList) {
      if(!foundX) {
        float xpt = sp.x;
        for (float ppt : arrP) {
          if (Math.abs(xpt - ppt) <= offset) {
            snapPoint.x = xpt - ppt;
            foundX = true;
            break;
          }
        }
      }
      if(!foundY) {
        float ypt = sp.y;
        for (float qpt : arrQ) {
          if (Math.abs(ypt - qpt) <= offset) {
            snapPoint.y = ypt - qpt;
            foundY = true;
            break;
          }
        }
      }
      if (foundX && foundY) {
        return snapPoint;
      }
    }

    if (foundX || foundY){
      if(!foundX) {
        snapPoint.x = 0;
      } else if(!foundY) {
        snapPoint.y = 0;
      }
      return snapPoint;
    }
    return null;
  }

  public ArrayList<Set<Float>> findObjectRulerPointForResize(float offset) {
    float arr[] = new float[8];
    getStickerPoints(handlingSticker,arr);

    float minP=Float.MAX_VALUE;
    float minQ=Float.MAX_VALUE;
    float maxP=Float.MIN_VALUE;
    float maxQ=Float.MIN_VALUE;

    for(int i = 0 ;i < 4 ; i++){
      float temp = arr[i*2];
      if(temp < minP){
        minP = temp;
      }
      else if(temp > maxP){
        maxP = temp;
      }
    }
    for(int i = 0; i < 4; i++){
      float temp = arr[i*2+1];
      if(temp < minQ){
        minQ = temp;
      }
      else if(temp > maxQ){
        maxQ = temp;
      }
    }

    float arrP[] = new float[]{minP, maxP};
    float arrQ[] = new float[]{minQ, maxQ};

    ArrayList<Set<Float>> snapPoints = new ArrayList<>();
    snapPoints.add(new HashSet<Float>());//0 index for - x coordinate
    snapPoints.add(new HashSet<Float>());//1 index for - y coordinate
    for (int i = 0; i < stickers.size(); i++)
    {
      Sticker sticker = stickers.get(i);
      if (handlingSticker != sticker)
      {
        float arrPoints[] = new float[8];
        getStickerPoints(sticker,arrPoints);

        float minX=Float.MAX_VALUE;
        float minY=Float.MAX_VALUE;
        float maxX=Float.MIN_VALUE;
        float maxY=Float.MIN_VALUE;

        for(int j = 0 ; j < 4 ; j++){
          float tempX = arrPoints[j*2];
          if(tempX < minX){
            minX = tempX;
          }
          else if(tempX > maxX){
            maxX = tempX;
          }
        }
        for(int k = 0; k < 4; k++){
          float tempY = arrPoints[k*2+1];
          if(tempY < minY){
            minY = tempY;
          }
          else if(tempY > maxY){
            maxY = tempY;
          }
        }

        float arrX[] = new float[]{minX,maxX};
        for(float xpt: arrX){
          for(float ppt: arrP) {
            if (Math.abs(xpt - ppt) <= offset) {
              snapPoints.get(0).add(xpt);
            }
          }
        }

        float arrY[] = new float[]{minY,maxY};
        for(float ypt: arrY){
          for(float qpt: arrQ) {
            if (Math.abs(ypt - qpt) <= offset) {
              snapPoints.get(1).add(ypt);
            }
          }
        }
      }
    }
    for (PointF snapPoint: snapPointList) {
      float xpt = snapPoint.x;
      float ypt = snapPoint.y;
      for(float ppt: arrP) {
        if (Math.abs(xpt - ppt) <= offset) {
          snapPoints.get(0).add(xpt);
        }
      }
      for(float qpt: arrQ) {
        if (Math.abs(ypt - qpt) <= offset) {
          snapPoints.get(1).add(ypt);
        }
      }
    }
    return snapPoints;
  }
  protected void drawStickers(Canvas canvas) {
    for (int i = 0; i < stickers.size(); i++) {
      Sticker sticker = stickers.get(i);
      if (sticker != null) {
        sticker.draw(canvas);
      }
    }

    if (handlingSticker != null && !locked && (showBorder || showIcons)) {

      getStickerPoints(handlingSticker, bitmapPoints);

      float x1 = bitmapPoints[0];
      float y1 = bitmapPoints[1];
      float x2 = bitmapPoints[2];
      float y2 = bitmapPoints[3];
      float x3 = bitmapPoints[4];
      float y3 = bitmapPoints[5];
      float x4 = bitmapPoints[6];
      float y4 = bitmapPoints[7];
      float xmt = (x1+x2)/2;
      float ymt = (y1+y2)/2;
      float xmb = (x3+x4)/2;
      float ymb = (y3+y4)/2;

      float xmid=(xmt+xmb)/2;
      float ymid=(ymt+ymb)/2;

      float xmb1=(x2+x4)/2;
      float ymb1=(y2+y4)/2;

      PointF point=rotate_point(xmb,ymb,-90,new PointF(x3,y3));
      float xr=point.x;
      float yr=point.y;

      PointF pointmove=rotate_point(xmb1,ymb1,-90,new PointF(x4,y4));
      float xr1=pointmove.x;
      float yr1=pointmove.y;

      float xml = (x1+x3)/2;
      float yml = (y1+y3)/2;
      float xmr = (x2+x4)/2;
      float ymr = (y2+y4)/2;
      float xe = x1 - 100;
      float ye = y1 - 100;
      float rotationDegree = calculateRotation(x3, y3, x1, y1);

      ViewGroup.LayoutParams layoutParams = getLayoutParams();
      int width1= layoutParams.width;
      int height1 = layoutParams.height;
      float yMidSticker=(y1+y4)/2f;
      float yMidCanvas=height1/2f;
      float xMidSticker=(x1+x4)/2f;
      float xMidCanvas=width1/2f;

      if (showBorder) {
//                canvas.drawRect(20 + offsetX, 20 + offsetY, 100 + offsetX, 100 + offsetY, blurPaint);

        canvas.drawLine(x1+offsetX,y1+offsetY,x2+offsetX,y2+offsetY,blurPaint);
        canvas.drawLine(x1+offsetX,y1+offsetY,x3+offsetX,y3+offsetY,blurPaint);
        canvas.drawLine(x2+offsetX,y2+offsetY,x4+offsetX,y4+offsetY,blurPaint);
        canvas.drawLine(x4+offsetX,y4+offsetY,x3+offsetX,y3+offsetY,blurPaint);
        canvas.drawLine(x1, y1, x2, y2, borderPaint);
        canvas.drawLine(x1, y1, x3, y3, borderPaint);
        canvas.drawLine(x2, y2, x4, y4, borderPaint);
        canvas.drawLine(x4, y4, x3, y3, borderPaint);


//                canvas.drawLine(xmt,y2-100,xmt,ymt,new Paint());
//                drawTextOnCanvas(canvas,rotationDegree);
      } else if(currentIcon != null && currentIcon.getPosition()==BitmapStickerIcon.MID_BOTTOM_CENTER){
//                    && handlingSticker instanceof TextSticker) {
        int rot = (int)rotationDegree;//Math.round(rotation);
        if(rot % 90 == 0) {
          if(isVibratingAngle==false) {
            rotateVibrator();
          }
          canvas.drawLine(xmt,ymt,xmb,ymb,rotationPaint);
          canvas.drawLine(xml,yml,xmr,ymr,rotationPaint);
          isVibratingAngle=true;
        }
        else if(rot % 45 == 0){
          if(isVibratingAngle==false) {
            rotateVibrator();
          }
          float rotateWidth = x2 - x1;
          float rotateHeight = y4 - y2;
          float rotateDistance = (rotateWidth < rotateHeight) ? rotateWidth : rotateHeight;
          rotateDistance = rotateDistance / 2;
          canvas.drawLine(midPoint.x-rotateDistance,midPoint.y-rotateDistance,
                         midPoint.x+rotateDistance, midPoint.y+rotateDistance,rotationPaint);
          canvas.drawLine(midPoint.x-rotateDistance,midPoint.y+rotateDistance,
                         midPoint.x+rotateDistance, midPoint.y-rotateDistance,rotationPaint);
          isVibratingAngle=true;
        }
        else{
          isVibratingAngle=false;
        }
      }

      if (currentMode == ActionMode.DRAG && isRulerLineOn) {
        ArrayList<Set<Float>> arrSnapData = findObjectRulerPoint(1f);
        for(float xpt: arrSnapData.get(0)){
          canvas.drawLine(xpt, 0, xpt, height1, objectRulerPaint);
        }
        for(float ypt: arrSnapData.get(1)) {
          canvas.drawLine(0, ypt, width1, ypt, objectRulerPaint);
        }
      }
      else if(currentIcon != null && (currentIcon.getPosition() == BitmapStickerIcon.LEFT_TOP ||
              currentIcon.getPosition() == BitmapStickerIcon.LEFT_BOTTOM ||
              currentIcon.getPosition() == BitmapStickerIcon.RIGHT_BOTOM ||
              currentIcon.getPosition() == BitmapStickerIcon.MID_TOP ||
              currentIcon.getPosition() == BitmapStickerIcon.MID_BOTTOM ||
              currentIcon.getPosition() == BitmapStickerIcon.MID_LEFT ||
              currentIcon.getPosition() == BitmapStickerIcon.MID_RIGHT) && isRulerLineOn){
//        Log.d(TAG,"ZoomIconEvent for Corner Button");
        ArrayList<Set<Float>> arrSnapData = findObjectRulerPointForResize(1f);
        Log.d(TAG,"ZoomIconEvent for Corner Button len = "+arrSnapData.size());
        for(float xpt: arrSnapData.get(0)){
          canvas.drawLine(xpt, 0, xpt, height1, objectRulerPaint);
        }
        for(float ypt: arrSnapData.get(1)) {
          canvas.drawLine(0, ypt, width1, ypt, objectRulerPaint);
        }
      }
      //draw icons
      showMoveIcons = false;
      if (showIcons) {
        float rotation = calculateRotation(x4, y4, x3, y3);
        float width=calculateDistance(x1,y1,x2,y2);
        float height=calculateDistance(x2,y2,x4,y4);
//        showMoveIcons=width < 200 || height < 200;
          showMoveIcons=(height * width) < (200 * 200);

        for (int i = 0; i < icons.size(); i++) {
          BitmapStickerIcon icon = icons.get(i);
          boolean displayIcon = true;
          if(showCurrentActionIcon) {
            if(currentIcon!=icon) {
              displayIcon = false;
            }
          }
          switch (icon.getPosition()) {
            case BitmapStickerIcon.LEFT_TOP:
              if(showMoveIcons==true) {
//                            Log.d("rot___", "drawStickers2: " + rotation);
                displayIcon=false;
              }
              else
              {
                configIconMatrix(icon, x1, y1, rotation);
              }
              break;

            case BitmapStickerIcon.RIGHT_TOP:
              configIconMatrix(icon, x2, y2, rotation);
              break;

            case BitmapStickerIcon.LEFT_BOTTOM:
              configIconMatrix(icon, x3, y3, rotation);
              break;

            case BitmapStickerIcon.RIGHT_BOTOM:
              if(showMoveIcons==true)
              {
                displayIcon=false;
              }
              else
              {
                configIconMatrix(icon, x4, y4, rotation);

              }
              break;

            case BitmapStickerIcon.MID_TOP:
              if(showMoveIcons==true)
              {
                displayIcon=false;
              }
              else
              {
                configIconMatrix(icon,xmt, ymt, rotation);

              }
              break;
            case BitmapStickerIcon.MID_BOTTOM:
              if(showMoveIcons==true)
              {
                displayIcon=false;
              }
              else
              {
                Log.d("rot___", "drawStickers: " + rotation);
                configIconMatrix(icon,xmb, ymb, rotation);
              }
              break;

            case BitmapStickerIcon.MID_LEFT:
              if(showMoveIcons==true)
              {
                displayIcon=false;
              }
              else
              {
                Log.d("rot___", "drawStickers: " + rotation);
                configIconMatrix(icon,xml, yml, rotation);
              }
              break;

            case BitmapStickerIcon.MID_RIGHT:
              if(showMoveIcons==true)
              {
                displayIcon=false;
              }
              else
              {
                Log.d("rot___", "drawStickers: " + rotation);
                configIconMatrix(icon,xmr, ymr, rotation);
              }
              break;

            case BitmapStickerIcon.EXT_LEFT_TOP:
              if(showMoveIcons==true)
              {
                displayIcon=false;
              }
              else
              {
                configIconMatrixT(icon, x1, y1, rotation);

              }
              break;

            case BitmapStickerIcon.MID_BOTTOM_CENTER:
              Log.d("rot___", "drawStickers: " + rotation);
              configIconMatrix(icon,xr,yr,rotation);
//                            configIconMatrixTT(icon,xmb, ymb, rotation);
              break;

            case BitmapStickerIcon.MID_RIGHT_CENTER:
              if(showMoveIcons==true) {
                Log.d("rot___", "drawStickers: " + rotation);
                configIconMatrix(icon, xr1, yr1, rotation);
              } else {
                displayIcon = false;
              }
              break;

          }
          if(displayIcon) {
            icon.draw(canvas, iconPaint);
          }
        }
      }
    }
  }

  PointF rotate_point(float x5,float y5,float angle,PointF p)
  {
    float sin= (float) Math.sin(Math.toRadians(angle));
    float cos= (float) Math.cos(Math.toRadians(angle));
    p.x -= x5;
    p.y -= y5;
    float xnew = p.x * cos - p.y * sin;
    float ynew = p.x * sin + p.y * cos;
    // translate point back:
    p.x = (int) (xnew + x5);
    p.y = (int) (ynew + y5);
//      calculate point at x distance from bottom middle point vertically for rotate icon to set it position
    float d=calculateDistance(x5,y5,p.x,p.y);
    float t;
    t=70/d;
    float xt=(((1-t)*x5+t*p.x));
    float yt=(((1-t)*y5+t*p.y));

    p.x=xt;
    p.y=yt;
    return p;
  }

  public void stickerImageDrag(MotionEvent event) {
    if (handlingSticker != null) {
      currentMode = ActionMode.DRAG;

      moveMatrix.set(downMatrix);
      moveMatrix.postTranslate(event.getX() - downX, event.getY() - downY);
      handlingSticker.setMatrix(moveMatrix);
      if (constrained) {
        constrainSticker(handlingSticker);
      }

      if (isAutoSnapOn)
      {
        PointF snapPoint = returnSnapPoint(event);
        if (snapPoint != null)
        {
          if (handlingSticker != null) {
            handlingSticker.getMatrix().postTranslate(snapPoint.x, snapPoint.y);
          }
//        moveToSnapPoint(snapPoint);
        } else {
          PointF snapOffset = findObjectSnapOffset(OFFSET_RULER);
          if (handlingSticker != null && snapOffset!=null) {
            handlingSticker.getMatrix().postTranslate(snapOffset.x, snapOffset.y);
          }
        }
      }
    }
  }

  protected void configIconMatrix(@NonNull BitmapStickerIcon icon, float x, float y,
                                  float rotation) {
    icon.setX(x);
    icon.setY(y);
    icon.getMatrix().reset();

    icon.getMatrix().postRotate(rotation, icon.getWidth() / 2, icon.getHeight() / 2);
    icon.getMatrix().postTranslate(x - icon.getWidth() / 2, y - icon.getHeight() / 2);
  }
  protected void configIconMatrixT(@NonNull BitmapStickerIcon icon, float x, float y,
                                   float rotation) {
    icon.setX(x-100);
    icon.setY(y-100);
    icon.getMatrix().reset();

    icon.getMatrix().postRotate(rotation, icon.getWidth() / 2+100, icon.getHeight() / 2+100);
    icon.getMatrix().postTranslate(x  - icon.getWidth() / 2 -100 , y  - icon.getHeight() / 2 -100);

  }

  protected void configIconMatrixTT(@NonNull BitmapStickerIcon icon, float x, float y, float rotation) {
    icon.setX(x);
    icon.setY(y);
//        icon.setZ(z);
    icon.getMatrix().reset();

    icon.getMatrix().postRotate(rotation, icon.getWidth() / 2, icon.getHeight() /20 );
    icon.getMatrix().postTranslate(x  - icon.getWidth() / 2 , 50+y  - icon.getHeight() /20 );
  }

  @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
    mIsExpandOrCollapse = false;
    mIsAdLoading = false;
    if (locked || isBgLock) return super.onInterceptTouchEvent(ev);

    switch (ev.getAction()) {
      case MotionEvent.ACTION_DOWN:
        downX = ev.getX();
        downY = ev.getY();

        boolean isTouched = findCurrentIconTouched() != null || findHandlingSticker() != null;
        if (!isTouched)
        {
          isTouchLocked = true;
          onStickerOperationListener.onStickerTouchedOutside();
          resetIcons();
        }
        else
          isTouchLocked = false;


        return isTouched;
    }

    return super.onInterceptTouchEvent(ev);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    mIsExpandOrCollapse = false;
    mIsAdLoading = false;
    mIsAspectRatioSelected = false;
    getParent().requestDisallowInterceptTouchEvent(true);
    if (locked || isTouchLocked || isBgLock)  {
      return super.onTouchEvent(event);
    }

    int action = MotionEventCompat.getActionMasked(event);

    switch (action) {
      case MotionEvent.ACTION_DOWN:
        if (!onTouchDown(event)) {
          return false;
        }
        break;
      case MotionEvent.ACTION_POINTER_DOWN:
        oldDistance = calculateDistance(event);
        oldRotation = calculateRotation(event);

        midPoint = calculateMidPoint(event);

        if (handlingSticker != null && isInStickerArea(handlingSticker, event.getX(1),
                event.getY(1)) && findCurrentIconTouched() == null) {
//                    Log.d(TAG, "onTouchEvent: Current Stiicker Rotate");
          currentMode = ActionMode.ZOOM_WITH_TWO_FINGER;
          onZoomTouchDown(event);
        }
        break;

      case MotionEvent.ACTION_MOVE:
        if (isSelected)
        {
          handleCurrentMode(event);
          invalidate();
        }

        break;

      case MotionEvent.ACTION_UP:
        onTouchUp(event);
        break;

      case MotionEvent.ACTION_POINTER_UP:
        if (currentMode == ActionMode.ZOOM_WITH_TWO_FINGER && handlingSticker != null) {
            onZoomTouchUp(event);
          if (onStickerOperationListener != null) {
            onStickerOperationListener.onStickerZoomFinished(handlingSticker);
          }
        }
        currentMode = ActionMode.NONE;
        break;
    }

    return true;
  }

  public void setmIsAspectRatioSelected(boolean mIsAspectRatioSelected) {
    this.mIsAspectRatioSelected = mIsAspectRatioSelected;
  }

  public void lockImages(boolean isLocked)
  {
    locked = isLocked;

  }

  public void setIsSelected(boolean isSelected)
  {
    this.isSelected = isSelected;
  }

  public boolean ismIsAspectRatioSelected() {
    return mIsAspectRatioSelected;
  }

  public void setmIsAdLoading(boolean mIsAdLoading) {
    this.mIsAdLoading = mIsAdLoading;
  }

  /**
   * @param event MotionEvent received from {@link #onTouchEvent)
   * @return true if has touch something
   */
  protected boolean onTouchDown(@NonNull MotionEvent event) {
    currentMode = ActionMode.DRAG;

    downX = event.getX();
    downY = event.getY();

    midPoint = calculateMidPoint();
    oldDistance = calculateDistance(midPoint.x, midPoint.y, downX, downY);
    oldRotation = calculateRotation(midPoint.x, midPoint.y, downX, downY);
    ooldDistance = (float) Math.sqrt((midPoint.x-event.getX()) * (midPoint.x-event.getX()) +
            (midPoint.y-event.getY()) * (midPoint.y-event.getY()));

    pastMovedDistance = -1;

    currentIcon = findCurrentIconTouched();
    if (currentIcon != null) {
      currentMode = ActionMode.ICON;
      currentIcon.onActionDown(this, event);

      showBorder = false;
      showCurrentActionIcon=true;

    } else {
      handlingSticker = findHandlingSticker();
    }

    if (handlingSticker != null) {
      downMatrix.set(handlingSticker.getMatrix());
      if (bringToFrontCurrentSticker) {
        stickers.remove(handlingSticker);
        stickers.add(handlingSticker);
      }
      if (onStickerOperationListener != null){
        onStickerOperationListener.onStickerTouchedDown(handlingSticker);
      }
    }

    if (currentIcon == null && handlingSticker == null) {
      return false;
    }
    invalidate();
    return true;
  }

  protected void onTouchUp(@NonNull MotionEvent event) {
    long currentTime = SystemClock.uptimeMillis();

    if (currentMode == ActionMode.ICON && currentIcon != null && handlingSticker != null && isSelected) {
      currentIcon.onActionUp(this, event);
    }

    if (currentMode == ActionMode.DRAG
            && Math.abs(event.getX() - downX) < touchSlop
            && Math.abs(event.getY() - downY) < touchSlop
            && handlingSticker != null) {
      currentMode = ActionMode.CLICK;
      if (onStickerOperationListener != null) {
        isSelected = true;

        onStickerOperationListener.onStickerClicked(handlingSticker);
      }
      if (currentTime - lastClickTime < minClickDelayTime) {
        if (onStickerOperationListener != null) {
//          isSelected = false;
          onStickerOperationListener.onStickerDoubleTapped(handlingSticker);
          resetIcons();
        }
      }
    }

    if (currentMode == ActionMode.DRAG && handlingSticker != null) {
      if (onStickerOperationListener != null) {
        onStickerOperationListener.onStickerDragFinished(handlingSticker);
      }
    }


    showBorder = true;
    showIcons=true;
    showCurrentActionIcon=false;
    currentMode = ActionMode.NONE;
    lastClickTime = currentTime;
    invalidate();
  }

  protected void handleCurrentMode(@NonNull MotionEvent event) {
    switch (currentMode) {
      case ActionMode.NONE:
      case ActionMode.CLICK:
        break;
      case ActionMode.DRAG:
        stickerImageDrag(event);
        break;

      case ActionMode.ZOOM_WITH_TWO_FINGER:
        if (handlingSticker != null ) {
          float newDistance = calculateDistance(event);
          if(handlingSticker instanceof  TextSticker){
            ((TextSticker)handlingSticker).scaleText(newDistance / oldDistance);
            invalidate();
          }
          else{
            float newRotation = calculateRotation(event);
            moveMatrix.set(downMatrix);
            moveMatrix.postScale(newDistance / oldDistance, newDistance / oldDistance, midPoint.x,
                    midPoint.y);
            // moveMatrix.postRotate(newRotation - oldRotation, midPoint.x, midPoint.y);
            handlingSticker.setMatrix(moveMatrix);
          }
          if (onStickerOperationListener != null) {
            onStickerOperationListener.onStickerZoom(handlingSticker);
          }

        }
        break;

      case ActionMode.ICON:
        if (handlingSticker != null && currentIcon != null) {
          currentIcon.onActionMove(this, event);
//                    pastMovedDistance = calculateDistance(midPoint.x, midPoint.y, event.getX(), event.getY());
        }
        break;
    }
  }

  protected void onZoomTouchDown(@NonNull MotionEvent event){

  }

  protected void onZoomTouchUp(@NonNull MotionEvent event){
    if(handlingSticker instanceof TextSticker) {
      float newDistance = calculateDistance(event);
      ((TextSticker)handlingSticker).onZoomFinished(newDistance/oldDistance);
    }
  }

  private PointF returnSnapPoint(MotionEvent event)
  {
//        Log.e(TAG, "returnSnapPoint");
    PointF currentMidPoint;
    if(handlingSticker!=null) {
      currentMidPoint = handlingSticker.getMappedCenterPoint();
    } else {
      currentMidPoint = new PointF(event.getX(), event.getY());
    }

    for (PointF snapPoint: midPointList)
    {
      if (Math.abs(currentMidPoint.x - snapPoint.x) <= 50 && Math.abs(currentMidPoint.y - snapPoint.y) <= 50)
      {
        Log.e(TAG, "snapPint done");
        onStickerOperationListener.onSnapPointDone(snapPoint);
        return new PointF(snapPoint.x-currentMidPoint.x, snapPoint.y-currentMidPoint.y);//snapPoint;
      }
      else
        onStickerOperationListener.dismissRural();
    }
    return null;
  }

  private void moveToSnapPoint(PointF snapPoint)
  {
    float moveX = 0;
    float moveY = 0;

    float [] matrixValues = new float[9];
    handlingSticker.getMatrix().getValues(matrixValues);
    moveX = snapPoint.x - matrixValues[Matrix.MSCALE_X] * (float) handlingSticker.getWidth()/2 - matrixValues[Matrix.MTRANS_X];
    moveY = snapPoint.y - matrixValues[Matrix.MSCALE_Y] * (float) handlingSticker.getHeight()/2 - matrixValues[Matrix.MTRANS_Y];


    if (handlingSticker != null)
      handlingSticker.getMatrix().postTranslate(moveX,  moveY);

  }

  public void zoomCurrentSticker(@NonNull MotionEvent event) {
//    if(handlingSticker instanceof TextSticker) {
//      float newDistance =  calculateDistance(midPoint.x, midPoint.y, event.getX(), event.getY());
//      ((TextSticker)handlingSticker).onFontSizeValue(newDistance/oldDistance);
//    }
    zoomCurrentSticker(handlingSticker, event);
  }

  public void zoomCurrentSticker(@Nullable Sticker sticker, @NonNull MotionEvent event) {
    if (sticker != null) {
      float newDistance = calculateDistance(midPoint.x, midPoint.y, event.getX(), event.getY());
      float newRotation = calculateRotation(midPoint.x, midPoint.y, event.getX(), event.getY());

      moveMatrix.set(downMatrix);
      moveMatrix.postScale(newDistance / oldDistance, newDistance / oldDistance, midPoint.x,
              midPoint.y);
      handlingSticker.setMatrix(moveMatrix);

      PointF snapPointRuler = findObjectSnapOffsetForResize(OFFSET_RULER);
      if(snapPointRuler !=null){
        Log.d(TAG, "zoom snap offset x = "+snapPointRuler.x+", y = "+snapPointRuler.y);
        newDistance = calculateDistance(midPoint.x, midPoint.y, event.getX()+(snapPointRuler.x), event.getY()+(snapPointRuler.y));

        moveMatrix.set(downMatrix);
        moveMatrix.postScale(newDistance / oldDistance, newDistance / oldDistance, midPoint.x,
                midPoint.y);
        handlingSticker.setMatrix(moveMatrix);
      }

      if (onStickerOperationListener != null) {
        onStickerOperationListener.onStickerZoom(handlingSticker);
      }
    }

  }

  public void handleHorizontalLeftMovement(@NonNull MotionEvent event) {
//        float newDistance = calculateDistance(midPoint.x, midPoint.y, event.getX(), event.getY());
    float x1 = bitmapPoints[0];
    float y1 = bitmapPoints[1];
    float x2 = bitmapPoints[2];
    float y2 = bitmapPoints[3];
    float x4 = bitmapPoints[6];
    float y4 = bitmapPoints[7];
    float mx24 = (x2+x4)/2f;
    float my24 = (y2+y4)/2f;
    float newDistance = calculateDistance(mx24, my24, event.getX(), event.getY());
    float newRotation = calculateRotation(x2, y2, x1, y1);
    Log.d(TAG, "handleHorizontalLeftMovement newrot = "+newRotation+", oldrot = "+oldRotation);
    float d=(pastMovedDistance==-1)?0:newDistance-pastMovedDistance;
    float tx= (float) (d*Math.cos(Math.toRadians(newRotation)));
    float ty= (float) (d*Math.sin(Math.toRadians(newRotation)));
    handlingSticker.getMatrix().postTranslate(-tx , -ty);
    float scale = (newDistance) / (2f*oldDistance);

    handlingSticker.setXScale(scale);
    PointF snapPointRuler = findObjectSnapOffsetForResize(OFFSET_RULER);
    if(snapPointRuler !=null){
      pastMovedDistance = newDistance;
      newDistance = calculateDistance(mx24, my24, event.getX()+snapPointRuler.x, event.getY()+snapPointRuler.y);
      d=(pastMovedDistance==-1)?0:newDistance-pastMovedDistance;
      tx= (float) (d*Math.cos(Math.toRadians(newRotation)));
      ty= (float) (d*Math.sin(Math.toRadians(newRotation)));
      handlingSticker.getMatrix().postTranslate(-tx , -ty);

      scale = (newDistance) / (2f*oldDistance);
    }

    handlingSticker.movedLeftHorizontally(scale);
    invalidate();

    pastMovedDistance = newDistance;
  }

  public void xhandleHorizontalLeftMovement(@NonNull MotionEvent event) {
    float newDistance = calculateDistance(midPoint.x, midPoint.y, event.getX(), event.getY());
    float x1 = bitmapPoints[0];
    float y1 = bitmapPoints[1];
    float x2 = bitmapPoints[2];
    float y2 = bitmapPoints[3];
    float x3 = bitmapPoints[4];
    float y3 = bitmapPoints[5];
    float mx13 = (x1+x3)/2f;
    float my13 = (y1+y3)/2f;
//        float newDistance = calculateDistance(mx13, my13, event.getX(), event.getY());

    float newRotation = calculateRotation(x2, y2, x1, y1);
    float oldWidth=handlingSticker.getWidth();

    float scale = (newDistance +oldDistance) / (2f*oldDistance);
//        float scale = (newDistance) / (2f*oldDistance);
    handlingSticker.movedLeftHorizontally(scale);
    invalidate();

    float newWidth=handlingSticker.getWidth();
    Log.d(TAG, "handleHorizontalLeftMovement newrot = "+newRotation+", oldrot = "+oldRotation);
    float d=newWidth-oldWidth;
//        float d=newWidth-oldWidth;

    float vx = x2-x1;
    float vy = y2-y1;
    float mv = (float) Math.sqrt(vx*vx+vy*vy);
    float dux = d*vx/mv;
    float duy = d*vy/mv;
    float tx = dux/2f;//x1-dux;
    float ty = duy/2f;//y1-duy;
//
//        float d1=calculateDistance(x1,y1,x2,y2);
//        float t;
//        t=newWidth/d1;
//        float tx=(((1-t)*x2+t*x1));
//        float ty=(((1-t)*y2+t*y1));

//        float tx= (float) (d*Math.cos(Math.toRadians(newRotation)))/2f;
//        float ty= (float) (d*Math.sin(Math.toRadians(newRotation)))/2f;
    handlingSticker.getMatrix().postTranslate(-tx , -ty);
    invalidate();

  }

  public void handleHorizontalRightMovement(@NonNull MotionEvent event) {
//        float newDistance = calculateDistance(midPoint.x, midPoint.y, event.getX(), event.getY());
    float x1 = bitmapPoints[0];
    float y1 = bitmapPoints[1];
    float x2 = bitmapPoints[2];
    float y2 = bitmapPoints[3];
    float x3 = bitmapPoints[4];
    float y3 = bitmapPoints[5];
    float mx13 = (x1+x3)/2f;
    float my13 = (y1+y3)/2f;
    float newDistance = calculateDistance(mx13, my13, event.getX(), event.getY());
    float scale = (newDistance) / (2f*oldDistance);

    handlingSticker.setXScale(scale);
    PointF snapPointRuler = findObjectSnapOffsetForResize(OFFSET_RULER);
    if(snapPointRuler !=null){
      newDistance = calculateDistance(mx13, my13, event.getX()+snapPointRuler.x, event.getY()+snapPointRuler.y);
      scale = (newDistance) / (2f*oldDistance);
    }

    handlingSticker.movedRightHorizontally(scale);
    invalidate();
  }

  public void handleVerticalTopMovement(@NonNull MotionEvent event) {
//        float newDistance = calculateDistance(midPoint.x, midPoint.y, event.getX(), event.getY());
    float x2 = bitmapPoints[2];
    float y2 = bitmapPoints[3];
    float x3 = bitmapPoints[4];
    float y3 = bitmapPoints[5];
    float x4 = bitmapPoints[6];
    float y4 = bitmapPoints[7];
    float mx34 = (x3+x4)/2f;
    float my34 = (y3+y4)/2f;
    float newDistance = calculateDistance(mx34, my34, event.getX(), event.getY());
    float newRotation = calculateRotation(x4, y4, x2, y2);
    Log.d(TAG, "handleHorizontalLeftMovement newrot = "+newRotation+", oldrot = "+oldRotation);
    float d=(pastMovedDistance==-1)?0:newDistance-pastMovedDistance;
    float tx= (float) (d*Math.cos(Math.toRadians(newRotation)));
    float ty= (float) (d*Math.sin(Math.toRadians(newRotation)));
    handlingSticker.getMatrix().postTranslate(-tx , -ty);
    float scale = (newDistance) / (2f*oldDistance);

    handlingSticker.setYScale(scale);
    PointF snapPointRuler = findObjectSnapOffsetForResize(OFFSET_RULER);
    if(snapPointRuler !=null){
      pastMovedDistance = newDistance;
      newDistance = calculateDistance(mx34, my34, event.getX()+snapPointRuler.x, event.getY()+snapPointRuler.y);
      d=(pastMovedDistance==-1)?0:newDistance-pastMovedDistance;
      tx= (float) (d*Math.cos(Math.toRadians(newRotation)));
      ty= (float) (d*Math.sin(Math.toRadians(newRotation)));
      handlingSticker.getMatrix().postTranslate(-tx , -ty);

      scale = (newDistance) / (2f*oldDistance);
    }

    handlingSticker.movedTopVertically(scale);
    invalidate();

    pastMovedDistance = newDistance;
  }

  public void handleVerticalBottomMovement(@NonNull MotionEvent event) {
//        float newDistance = calculateDistance(midPoint.x, midPoint.y, event.getX(), event.getY());
    float x1 = bitmapPoints[0];
    float y1 = bitmapPoints[1];
    float x2 = bitmapPoints[2];
    float y2 = bitmapPoints[3];
    float mx = (x1+x2)/2f;
    float my = (y1+y2)/2f;
    float newDistance = calculateDistance(mx, my, event.getX(), event.getY());
    float scale = (newDistance) / (2f*oldDistance);

    handlingSticker.setYScale(scale);
    PointF snapPointVertical = findObjectSnapOffsetForResize(OFFSET_RULER);
    if(snapPointVertical !=null){
      newDistance=calculateDistance(mx, my, event.getX()+snapPointVertical.x, event.getY()+snapPointVertical.y);
      scale = (newDistance) / (2f*oldDistance);
    }

    handlingSticker.movedBottomVertically(scale);
    invalidate();
  }

  public void handleHorizontalRightUp(MotionEvent event) {
//        float newDistance = calculateDistance(midPoint.x, midPoint.y, event.getX(), event.getY());
    float x1 = bitmapPoints[0];
    float y1 = bitmapPoints[1];
    float x2 = bitmapPoints[4];
    float y2 = bitmapPoints[5];
    float mx = (x1+x2)/2f;
    float my = (y1+y2)/2f;
    float newDistance = calculateDistance(mx, my, event.getX(), event.getY());
    float scale = (newDistance) / (2f*oldDistance);
    handlingSticker.upRightHorizontally(scale);
    invalidate();
//        scaleVerticalSticker(event);


//        moveMatrix.set(handlingSticker.getMatrix());
////        float newDistance = (float) Math.abs(midPoint.x-event.getX());
//
//        PointF pf = handlingSticker.getMappedCenterPoint();
//        moveMatrix.postScale(scale, 1, pf.x, pf.y);
//
//        handlingSticker.setMatrix(moveMatrix);
  }

  public void handleHorizontalLeftUp(MotionEvent event) {
//        float newDistance = calculateDistance(midPoint.x, midPoint.y, event.getX(), event.getY());
    float x1 = bitmapPoints[2];
    float y1 = bitmapPoints[3];
    float x2 = bitmapPoints[6];
    float y2 = bitmapPoints[7];
    float mx = (x1+x2)/2f;
    float my = (y1+y2)/2f;
    float newDistance = calculateDistance(mx, my, event.getX(), event.getY());
    float scale = (newDistance) / (2f*oldDistance);
    handlingSticker.upLeftHorizontally(scale);
    invalidate();
  }

  public void handleVerticalBottomUp(MotionEvent event) {
//        float newDistance = calculateDistance(midPoint.x, midPoint.y, event.getX(), event.getY());
    float x1 = bitmapPoints[0];
    float y1 = bitmapPoints[1];
    float x2 = bitmapPoints[2];
    float y2 = bitmapPoints[3];
    float mx = (x1+x2)/2f;
    float my = (y1+y2)/2f;
    float newDistance = calculateDistance(mx, my, event.getX(), event.getY());
    float scale = (newDistance) / (2f*oldDistance);
    handlingSticker.upBottomVertically(scale);

    invalidate();
  }

  public void handleVerticalTopUp(MotionEvent event) {
//        float newDistance = calculateDistance(midPoint.x, midPoint.y, event.getX(), event.getY());
    float x1 = bitmapPoints[4];
    float y1 = bitmapPoints[5];
    float x2 = bitmapPoints[6];
    float y2 = bitmapPoints[7];
    float mx = (x1+x2)/2f;
    float my = (y1+y2)/2f;
    float newDistance = calculateDistance(mx, my, event.getX(), event.getY());
    float scale = (newDistance) / (2f*oldDistance);
    handlingSticker.upTopVertically(scale);

    invalidate();
  }

  public void scaleHorizontalSticker(@NonNull MotionEvent event) {
    scaleHorizontalSticker(handlingSticker,event);
  }

  public void scaleHorizontalSticker(@NonNull Sticker sticker,@Nullable MotionEvent event) {
    if (sticker != null) {
      moveMatrix.set(downMatrix);
      float newDistance = (float) Math.abs(midPoint.y-event.getY());
      moveMatrix.postScale(1, newDistance / oldDistance, midPoint.x, midPoint.y);
      handlingSticker.setMatrix(moveMatrix);
    }
  }

  public void scaleVerticalSticker(@NonNull MotionEvent event)
  {
    scaleVerticalSticker(handlingSticker,event);
  }

  public void scaleVerticalSticker(@NonNull Sticker sticker,@Nullable MotionEvent event)
  {
    if (sticker != null) {
      moveMatrix.set(downMatrix);
      float newDistance = (float) Math.abs(midPoint.x-event.getX());
      moveMatrix.postScale(newDistance / oldDistance, 1, midPoint.x, midPoint.y);

      handlingSticker.setMatrix(moveMatrix);
    }
  }

  public void rotateotateCurrentSticker(@NonNull MotionEvent event) {
    rotateCurrentSticker(handlingSticker, event);

  }

  public void rotateCurrentSticker(@Nullable Sticker sticker, @NonNull MotionEvent event) {
    if (sticker != null) {
      float offset = 2.5f;
      float newDistance = calculateDistance(midPoint.x, midPoint.y, event.getX(), event.getY());
      float newRotation = calculateRotation(midPoint.x, midPoint.y, event.getX(), event.getY());

      moveMatrix.set(downMatrix);
      moveMatrix.postRotate(newRotation - oldRotation, midPoint.x, midPoint.y);

      float matrixRotation = getMatrixAngle(moveMatrix);
      boolean setAngle = false;
      float rotationMod = matrixRotation % 45f;
      if((rotationMod < -45 + offset) || (rotationMod > 0-offset && rotationMod < 0+offset) || (rotationMod > 45-offset)){
        float divide = matrixRotation / 45f;
        newRotation = 45f * Math.round(divide);
        setAngle = true;
      }
      if(setAngle) {
        moveMatrix.postRotate(newRotation - matrixRotation, midPoint.x, midPoint.y);
      }

      handlingSticker.setMatrix(moveMatrix);
    }
  }

  public float getMatrixAngle(@NonNull Matrix matrix) {
    return (float) Math.toDegrees(-(Math.atan2(getMatrixValue(matrix, Matrix.MSKEW_X),
            getMatrixValue(matrix, Matrix.MSCALE_X))));
  }

  public float getMatrixValue(@NonNull Matrix matrix, @IntRange(from = 0, to = 9) int valueIndex) {
    final float[] matrixValues = new float[9];
    matrix.getValues(matrixValues);
    return matrixValues[valueIndex];
  }

  public void setmIsExpandOrCollapse(boolean mIsExpandOrCollapse) {
    this.mIsExpandOrCollapse = mIsExpandOrCollapse;
  }

  public void setmTileWidth(int tileWidth)
  {
    this.mTileWidth = tileWidth;
  }

  public void setmTileHeight(int tileHeight)
  {
    this.mTileHeight = tileHeight;
  }

  public void setSliceCount(int sliceCount)
  {
    this.mSliceCount = sliceCount;
  }
  protected void constrainSticker(@NonNull Sticker sticker) {
    float moveX = 0;
    float moveY = 0;
    int width = getWidth();
    int height = getHeight();
    sticker.getMappedCenterPoint(currentCenterPoint, point, tmp);
    if (currentCenterPoint.x < 0) {
      moveX = -currentCenterPoint.x;
    }

    if (currentCenterPoint.x > width) {
      moveX = width - currentCenterPoint.x;
    }

    if (currentCenterPoint.y < 0) {
      moveY = -currentCenterPoint.y;
    }

    if (currentCenterPoint.y > height) {
      moveY = height - currentCenterPoint.y;
    }

    sticker.getMatrix().postTranslate(moveX, moveY);

  }

  @Nullable protected BitmapStickerIcon findCurrentIconTouched() {
//        Log.d(TAG, "onTouchEvent: Current Stiicker event:: icons size ="+icons.size());
    for (BitmapStickerIcon icon : icons) {
      if(showMoveIcons) {
        if(icon.getPosition() == BitmapStickerIcon.MID_LEFT || icon.getPosition() == BitmapStickerIcon.MID_RIGHT
                || icon.getPosition() == BitmapStickerIcon.MID_TOP || icon.getPosition() == BitmapStickerIcon.MID_BOTTOM
                || icon.getPosition() == BitmapStickerIcon.LEFT_TOP || icon.getPosition() == BitmapStickerIcon.RIGHT_BOTOM) {
          continue;
        }
      } else {
        if(icon.getPosition() == BitmapStickerIcon.MID_RIGHT_CENTER) {
          continue;
        }
      }
      float x = icon.getX() - downX;
      float y = icon.getY() - downY;
      float distance_pow_2 = x * x + y * y;
      if (distance_pow_2 <= Math.pow(icon.getIconRadius() + icon.getIconRadius(), 2)) {
//                Log.d(TAG, "onTouchEvent: Current Stiicker event::"+icon.getIconEvent());
        return icon;
      }
    }

    return null;
  }

  /**
   * find the touched Sticker
   **/
  @Nullable protected Sticker findHandlingSticker() {
    for (int i = stickers.size() - 1; i >= 0; i--) {
      if (isInStickerArea(stickers.get(i), downX, downY)) {
        return stickers.get(i);
      }
    }
    return null;
  }

  protected boolean isInStickerArea(@NonNull Sticker sticker, float downX, float downY) {
    tmp[0] = downX;
    tmp[1] = downY;
    return sticker.contains(tmp);
  }

  @NonNull protected PointF calculateMidPoint(@Nullable MotionEvent event) {
    if (event == null || event.getPointerCount() < 2) {
      midPoint.set(0, 0);
      return midPoint;
    }
    float x = (event.getX(0) + event.getX(1)) / 2;
    float y = (event.getY(0) + event.getY(1)) / 2;
    midPoint.set(x, y);
    return midPoint;
  }

  @NonNull protected PointF calculateMidPoint() {
    if (handlingSticker == null) {
      midPoint.set(0, 0);
      return midPoint;
    }
    handlingSticker.getMappedCenterPoint(midPoint, point, tmp);
    return midPoint;
  }

  public  void updateAspectRatio(float scaleX, float scaleY)
  {

  }

  /**
   * calculate rotation in line with two fingers and x-axis
   **/
  protected float calculateRotation(@Nullable MotionEvent event) {
    if (event == null || event.getPointerCount() < 2) {
      return 0f;
    }
    return calculateRotation(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
  }

  protected float calculateRotation(float x1, float y1, float x2, float y2) {
    double x = x1 - x2;
    double y = y1 - y2;
    double radians = Math.atan2(y, x);
    return (float) Math.toDegrees(radians);
  }

  /**
   * calculate Distance in two fingers
   **/
  protected float calculateDistance(@Nullable MotionEvent event) {
    if (event == null || event.getPointerCount() < 2) {
      return 0f;
    }
    return calculateDistance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
  }

  protected float calculateDistance(float x1, float y1, float x2, float y2) {
    double x = x1 - x2;
    double y = y1 - y2;

    return (float) Math.sqrt(x * x + y * y);
  }

  @Override protected void onSizeChanged(int w, int h, int oldW, int oldH) {
    super.onSizeChanged(w, h, oldW, oldH);
    for (int i = 0; i < stickers.size(); i++) {
      Sticker sticker = stickers.get(i);
      if (sticker != null) {
        transformSticker(sticker);
      }
    }
  }

  /**
   * Sticker's drawable will be too bigger or smaller
   * This method is to transform it to fit
   * step 1Ã¯Â¼Å¡let the center of the sticker image is coincident with the center of the View.
   * step 2Ã¯Â¼Å¡Calculate the zoom and zoom
   **/
  protected void transformSticker(@Nullable Sticker sticker) {
    if (sticker == null) {
      Log.e(TAG, "transformSticker: the bitmapSticker is null or the bitmapSticker bitmap is null");
      return;
    }

    if (mIsExpandOrCollapse || mIsAdLoading || mIsAspectRatioSelected)
    {
      return;
    }
    sizeMatrix.reset();

    float width = getWidth();
    float height = getHeight();
    float stickerWidth = sticker.getWidth();
    float stickerHeight = sticker.getHeight();
    //step 1
    float offsetX = (width - stickerWidth) / 2;
    float offsetY = (height - stickerHeight) / 2;

    sizeMatrix.postTranslate(offsetX, offsetY);

    //step 2
    float scaleFactor;
    if (width < height) {
      scaleFactor = width / stickerWidth;
    } else {
      scaleFactor = height / stickerHeight;
    }

    sizeMatrix.postScale(scaleFactor / 2f, scaleFactor / 2f, width / 2f, height / 2f);

    sticker.getMatrix().reset();
    sticker.setMatrix(sizeMatrix);

    invalidate();
  }

  public void flipCurrentSticker(int direction) {
    flip(handlingSticker, direction);
  }

  public void flip(@Nullable Sticker sticker, @Flip int direction) {
    if (sticker != null) {
      sticker.getCenterPoint(midPoint);
      if ((direction & FLIP_HORIZONTALLY) > 0) {
        sticker.getMatrix().preScale(-1, 1, midPoint.x, midPoint.y);
        sticker.setFlippedHorizontally(!sticker.isFlippedHorizontally());
      }
      if ((direction & FLIP_VERTICALLY) > 0) {
        sticker.getMatrix().preScale(1, -1, midPoint.x, midPoint.y);
        sticker.setFlippedVertically(!sticker.isFlippedVertically());
      }

      if (onStickerOperationListener != null) {
        onStickerOperationListener.onStickerFlipped(sticker);
      }

      invalidate();
    }
  }

  public boolean replace(@Nullable Sticker sticker) {
    return replace(sticker, true);
  }

  public boolean replace(@Nullable Sticker sticker, boolean needStayState) {
    if (handlingSticker != null && sticker != null) {
      float width = getWidth();
      float height = getHeight();
      if (needStayState) {
        sticker.setMatrix(handlingSticker.getMatrix());
        sticker.setFlippedVertically(handlingSticker.isFlippedVertically());
        sticker.setFlippedHorizontally(handlingSticker.isFlippedHorizontally());
        sticker.setXScale(handlingSticker.getXScale());
        sticker.setYScale(handlingSticker.getYScale());
        sticker.setXDistance(handlingSticker.getXDistance());
        sticker.setYDistance(handlingSticker.getYDistance());
      } else {
        handlingSticker.getMatrix().reset();
        // reset scale, angle, and put it in center
        float offsetX = (width - handlingSticker.getWidth()) / 2f;
        float offsetY = (height - handlingSticker.getHeight()) / 2f;
        sticker.getMatrix().postTranslate(offsetX, offsetY);

        float scaleFactor;
        if (width < height) {
          scaleFactor = width / handlingSticker.getDrawable().getIntrinsicWidth();
        } else {
          scaleFactor = height / handlingSticker.getDrawable().getIntrinsicHeight();
        }
        sticker.getMatrix().postScale(scaleFactor / 2f, scaleFactor / 2f, width / 2f, height / 2f);
      }
      int index = stickers.indexOf(handlingSticker);
      stickers.set(index, sticker);
      handlingSticker = sticker;

      invalidate();
      return true;
    } else {
      return false;
    }
  }

  public boolean remove(@Nullable Sticker sticker) {
    if (stickers.contains(sticker)) {
      stickers.remove(sticker);
      if (onStickerOperationListener != null) {
        onStickerOperationListener.onStickerDeleted(sticker);
      }
      if (handlingSticker == sticker) {
        handlingSticker = null;
      }
      invalidate();

      return true;
    } else {
      Log.d(TAG, "remove: the sticker is not in this StickerView");

      return false;
    }
  }

  public boolean removeCurrentSticker() {
    return remove(handlingSticker);
  }

  public void removeAllStickers() {
    stickers.clear();
    if (handlingSticker != null) {
      handlingSticker.release();
      handlingSticker = null;
    }
    invalidate();
  }

  @NonNull public StickerView addSticker(@NonNull Sticker sticker) {
    return addSticker(sticker, Sticker.Position.CENTER);
  }

  public StickerView addSticker(@NonNull final Sticker sticker,
                                final @Sticker.Position int position) {
    if (ViewCompat.isLaidOut(this)) {
      addStickerImmediately(sticker, position);
    } else {
      post(new Runnable() {
        @Override public void run() {
          addStickerImmediately(sticker, position);
        }
      });
    }
    return this;
  }


  public StickerView addStickerWithatrix(@NonNull final Sticker  sticker, final Matrix matrix)
  {
    if (ViewCompat.isLaidOut(this)) {
      addStickerImmediately(sticker, matrix);
    } else {
      post(new Runnable() {
        @Override public void run() {
          addStickerImmediately(sticker, matrix);
        }
      });
    }
    return this;
  }

  protected void addStickerImmediately(@NonNull Sticker sticker, Matrix matrix)
  {
    handlingSticker = sticker;
    sticker.setMatrix(matrix);
    stickers.add(sticker);
    if (onStickerOperationListener != null) {
      onStickerOperationListener.onStickerAdded(sticker);
    }
    invalidate();
  }

  protected void addStickerImmediately(@NonNull Sticker sticker, @Sticker.Position int position) {
    setStickerPosition(sticker, position);


    float scaleFactor, widthScaleFactor, heightScaleFactor;

    widthScaleFactor = (float) (getWidth() /1) / sticker.getDrawable().getIntrinsicWidth();
    heightScaleFactor = (float) (getHeight() /1) / sticker.getDrawable().getIntrinsicHeight();
    scaleFactor = widthScaleFactor > heightScaleFactor ? heightScaleFactor : widthScaleFactor;

    sticker.getMatrix()
            .postScale(scaleFactor / 2, scaleFactor / 2, getWidth() / 2, getHeight() / 2);

    handlingSticker = sticker;
    stickers.add(sticker);
    if (onStickerOperationListener != null) {
      onStickerOperationListener.onStickerAdded(sticker);
    }
    invalidate();
  }

  public void setStickerPosition(@NonNull Sticker sticker, @Sticker.Position int position) {
    float width = getWidth();
    float height = getHeight();
    float offsetX = width - sticker.getWidth();
    float offsetY = height - sticker.getHeight();
    if ((position & Sticker.Position.TOP) > 0) {
      offsetY /= 4f;
      Log.d(TAG, "setStickerPosition: Offset"+offsetY);
    } else if ((position & Sticker.Position.BOTTOM) > 0) {
      offsetY *= 3f / 4f;
    } else {
      offsetY /= 2f;
    }
    if ((position & Sticker.Position.LEFT) > 0) {
      offsetX /= 4f;
    } else if ((position & Sticker.Position.RIGHT) > 0) {
      offsetX *= 3f / 4f;
    } else {
      offsetX /= 2f;
    }
    sticker.getMatrix().postTranslate(offsetX, offsetY);
  }

  @NonNull public float[] getStickerPoints(@Nullable Sticker sticker) {
    float[] points = new float[8];
    getStickerPoints(sticker, points);
    return points;
  }

  public void getStickerPoints(@Nullable Sticker sticker, @NonNull float[] dst) {
    if (sticker == null) {
      Arrays.fill(dst, 0);
      return;
    }
    sticker.getBoundPoints(bounds);
    sticker.getMappedPoints(dst, bounds);
  }

  public void save(@NonNull File file) {
    try {
      StickerUtils.saveImageToGallery(file, createBitmap());
      StickerUtils.notifySystemGallery(getContext(), file);
    } catch (IllegalArgumentException | IllegalStateException ignored) {
      //
    }
  }

  @NonNull public Bitmap createBitmap() throws OutOfMemoryError {
    handlingSticker = null;
    Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    this.draw(canvas);
    return bitmap;
  }

  public int getStickerCount() {
    return stickers.size();
  }

  public boolean isNoneSticker() {
    return getStickerCount() == 0;
  }

  public boolean isLocked() {
    return locked;
  }

  @NonNull public StickerView setLocked(boolean locked) {
    this.locked = locked;
    invalidate();
    return this;
  }

  @NonNull public StickerView setMinClickDelayTime(int minClickDelayTime) {
    this.minClickDelayTime = minClickDelayTime;
    return this;
  }

  public int getMinClickDelayTime() {
    return minClickDelayTime;
  }

  public boolean isConstrained() {
    return constrained;
  }

  @NonNull public StickerView setConstrained(boolean constrained) {
    this.constrained = constrained;
    postInvalidate();
    return this;
  }



  @NonNull public StickerView setOnStickerOperationListener(
          @Nullable OnStickerOperationListener onStickerOperationListener) {
    this.onStickerOperationListener = onStickerOperationListener;
    return this;
  }

  @Nullable public OnStickerOperationListener getOnStickerOperationListener() {
    return onStickerOperationListener;
  }

  @Nullable public Sticker getCurrentSticker() {
    return handlingSticker;
  }

  @NonNull public List<BitmapStickerIcon> getIcons() {
    return icons;
  }

  public void setIcons(@NonNull List<BitmapStickerIcon> icons) {
    this.icons.clear();
    this.icons.addAll(icons);
    invalidate();
  }


  public void  addPoints(List<PointF> midPints) {
    midPointList.clear();
    midPointList.addAll(midPints);
    addSnapPoints(midPints);
  }

  private void addSnapPoints(List<PointF> midPints) {
    ViewGroup.LayoutParams layoutParams = getLayoutParams();
    snapPointList.clear();
    snapPointList.add(new PointF(1,0));
    snapPointList.add(new PointF(layoutParams.width,layoutParams.height));
    snapPointList.addAll(midPointList);
  }

  public void removePoints(List<PointF> points)
  {
    midPointList.removeAll(points);
  }

  public void removeLastSnapPointsSpecifiedByCount(int count)
  {
    do {
      if(!midPointList.isEmpty())
        midPointList.remove(midPointList.size() - 1);
      count--;
    }while (count > 0);
  }

  public void addPoint(PointF pointF)
  {
    midPointList.add(pointF);
  }





  public interface OnStickerOperationListener {
    void onStickerAdded(@NonNull Sticker sticker);

    void onStickerClicked(@NonNull Sticker sticker);

    void onStickerDeleted(@NonNull Sticker sticker);

    void onStickerDragFinished(@NonNull Sticker sticker);

    void onStickerTouchedOutside();

    void onStickerTouchedDown(@NonNull Sticker sticker);

    void onStickerZoom(@NonNull Sticker sticker);

    void onStickerZoomFinished(@NonNull Sticker sticker);

    void onStickerHorizontalScale(@Nullable Sticker sticker);

    void onStickerVerticalScale(@Nullable Sticker sticker);

    void onStickerRotateFinished(@NonNull Sticker sticker);

    void onStickerFlipped(@NonNull Sticker sticker);

    void onStickerDoubleTapped(@NonNull Sticker sticker);

    void onSnapPointDone(PointF pointF);
    void dismissRural();

    void onStickerVerticalMovementFinished(@NonNull Sticker sticker);

    void onStickerHorizontalMovementFinished(@NonNull Sticker sticker);
  }
}