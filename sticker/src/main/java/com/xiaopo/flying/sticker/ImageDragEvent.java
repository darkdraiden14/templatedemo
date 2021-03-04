package com.xiaopo.flying.sticker;

import android.util.Log;
import android.view.MotionEvent;

public class ImageDragEvent implements StickerIconEvent
{

    private final static String TAG = ImageDragEvent.class.getSimpleName();

    @Override
    public void onActionDown(StickerView stickerView, MotionEvent event) {

    }

    @Override
    public void onActionMove(StickerView stickerView, MotionEvent event) {
        stickerView.stickerImageDrag(event);
    }

    @Override
    public void onActionUp(StickerView stickerView, MotionEvent event) {

        if (stickerView.getOnStickerOperationListener() != null) {
            stickerView.getOnStickerOperationListener()
                    .onStickerDragFinished(stickerView.getCurrentSticker());
        }


    }
}
