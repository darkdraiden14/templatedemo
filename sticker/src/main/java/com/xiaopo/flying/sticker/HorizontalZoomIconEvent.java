package com.xiaopo.flying.sticker;

import android.view.MotionEvent;

public class HorizontalZoomIconEvent implements StickerIconEvent {
    @Override
    public void onActionDown(StickerView stickerView, MotionEvent event) {

    }

    @Override
    public void onActionMove(StickerView stickerView, MotionEvent event) {
       // stickerView.strechHorizontally(event);
        stickerView.scaleHorizontalSticker(event);
    }

    @Override
    public void onActionUp(StickerView stickerView, MotionEvent event) {
        if (stickerView.getOnStickerOperationListener() != null) {
//            stickerView.getOnStickerOperationListener()
//                    .onStickerZoomFinished(stickerView.getCurrentSticker());
            stickerView.getOnStickerOperationListener().onStickerHorizontalScale(stickerView.getCurrentSticker());
        }
    }
}
