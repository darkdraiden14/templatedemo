package com.xiaopo.flying.sticker;

import android.view.MotionEvent;

public class VerticalZoomIconEvent implements StickerIconEvent {
    @Override
    public void onActionDown(StickerView stickerView, MotionEvent event) {

    }

    @Override
    public void onActionMove(StickerView stickerView, MotionEvent event) {
        // stickerView.strechVertically(event);
        stickerView.scaleVerticalSticker(event);    }

    @Override
    public void onActionUp(StickerView stickerView, MotionEvent event) {
        if (stickerView.getOnStickerOperationListener() != null) {
            //            stickerView.getOnStickerOperationListener()
            //                    .onStickerZoomFinished(stickerView.getCurrentSticker());
            stickerView.getOnStickerOperationListener().onStickerVerticalScale(stickerView.getCurrentSticker());
        }
    }
}
