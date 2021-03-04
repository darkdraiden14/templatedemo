package com.xiaopo.flying.sticker;

import android.util.Log;
import android.view.MotionEvent;

public class BoundBoxTopVerticalMoveEvent implements StickerIconEvent {
    @Override
    public void onActionDown(StickerView stickerView, MotionEvent event) {

    }

    @Override
    public void onActionMove(StickerView stickerView, MotionEvent event) {
        stickerView.handleVerticalTopMovement(event);

    }

    @Override
    public void onActionUp(StickerView stickerView, MotionEvent event) {
        stickerView.handleVerticalTopUp(event);
        if (stickerView.getOnStickerOperationListener() != null) {
            stickerView.getOnStickerOperationListener()
                    .onStickerVerticalMovementFinished(stickerView.getCurrentSticker());
        }
    }
}
