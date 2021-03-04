package com.xiaopo.flying.sticker;

import android.util.Log;
import android.view.MotionEvent;

public class RotateIconEvent implements StickerIconEvent
{

    private final static String TAG = RotateIconEvent.class.getSimpleName();

    @Override
    public void onActionDown(StickerView stickerView, MotionEvent event) {

    }

    @Override
    public void onActionMove(StickerView stickerView, MotionEvent event) {
        Log.e(TAG, "onActionMove");
        stickerView.rotateotateCurrentSticker(event);
    }

    @Override
    public void onActionUp(StickerView stickerView, MotionEvent event) {

        Log.e(TAG, "onActionUp");


        if (stickerView.getOnStickerOperationListener() != null) {
            stickerView.getOnStickerOperationListener()
                    .onStickerRotateFinished(stickerView.getCurrentSticker());
        }

    }
}
