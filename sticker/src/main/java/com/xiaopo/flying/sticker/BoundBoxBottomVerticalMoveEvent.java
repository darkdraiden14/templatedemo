package com.xiaopo.flying.sticker;

import android.view.MotionEvent;

public class BoundBoxBottomVerticalMoveEvent implements StickerIconEvent {
    @Override
    public void onActionDown(StickerView stickerView, MotionEvent event) {

    }

    @Override
    public void onActionMove(StickerView stickerView, MotionEvent event) {
        stickerView.handleVerticalBottomMovement(event);
    }

    @Override
    public void onActionUp(StickerView stickerView, MotionEvent event) {
        stickerView.handleVerticalBottomUp(event);
        if (stickerView.getOnStickerOperationListener() != null) {
            stickerView.getOnStickerOperationListener()
                    .onStickerVerticalMovementFinished(stickerView.getCurrentSticker());
        }

    }
}
