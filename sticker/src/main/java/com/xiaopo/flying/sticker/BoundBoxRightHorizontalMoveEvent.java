package com.xiaopo.flying.sticker;

import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;
public class BoundBoxRightHorizontalMoveEvent implements StickerIconEvent {
    @Override
    public void onActionDown(StickerView stickerView, MotionEvent event) {
    }
    @Override
    public void onActionMove(StickerView stickerView, MotionEvent event) {
        stickerView.handleHorizontalRightMovement(event);
        Log.d("","Bound horizontal on action move");
    }
    @Override
    public void onActionUp(StickerView stickerView, MotionEvent event) {
        stickerView.handleHorizontalRightUp(event);
        if (stickerView.getOnStickerOperationListener() != null) {
            stickerView.getOnStickerOperationListener()
                    .onStickerHorizontalMovementFinished(stickerView.getCurrentSticker());
        }
    }
}
