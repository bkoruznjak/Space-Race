package hr.from.bkoruznjak.spacerace.model;

import android.graphics.Rect;
import android.util.Log;

/**
 * Created by bkoruznjak on 29/01/2017.
 */

public class Explosion {

    private int originX;
    private int originY;
    private int currentFrameIndex;
    private int numberOfFrames;
    private float frameSize;
    private boolean isDone;

    //internal logic
    private Rect rectToBeDrawn;
    private Rect rectDestination;
    private float left;
    private float top;
    private float right;
    private float bottom;

    public Explosion(int originX, int originY, int numberOfFrames, float frameSize) {
        this.originX = originX;
        this.originY = originY;
        this.currentFrameIndex = 0;
        this.numberOfFrames = numberOfFrames;
        this.frameSize = frameSize;
        this.rectDestination = new Rect(originX, originY, (int) (originX + frameSize), (int) (originY + frameSize));
        this.rectToBeDrawn = new Rect(0, 0, (int) frameSize, (int) frameSize);
        Log.d("bbb", "created new explosion with left:" + rectToBeDrawn.left + ", top:" + rectToBeDrawn.top + ", right:" + rectToBeDrawn.right + ", bottom:" + rectToBeDrawn.bottom);

    }

    public int getOriginX() {
        return originX;
    }

    public void setOriginX(int originX) {
        this.originX = originX;
    }

    public int getOriginY() {
        return originY;
    }

    public void setOriginY(int originY) {
        this.originY = originY;
    }

    public int getCurrentFrameIndex() {
        return currentFrameIndex;
    }

    public void setCurrentFrameIndex(int currentFrameIndex) {
        this.currentFrameIndex = currentFrameIndex;
    }

    public boolean isDone() {
        return this.isDone;
    }

    public void increaseFrame() {
        if ((currentFrameIndex + 1) == numberOfFrames) {
            isDone = true;
        } else {
            //we move down the rectangle since we have 4 images per row and reset row
            if (currentFrameIndex != 0 && currentFrameIndex % 4 == 0) {
                rectToBeDrawn.top += frameSize;
                rectToBeDrawn.bottom += frameSize;
                //reset x to start
                rectToBeDrawn.left = 0;
                rectToBeDrawn.right = (int) frameSize;
            } else {
                rectToBeDrawn.left += frameSize;
                rectToBeDrawn.right += frameSize;
            }
            currentFrameIndex++;
//            rectToBeDrawn.left = (int) left;
//            rectToBeDrawn.top = (int) top;
//            rectToBeDrawn.right = (int) right;
//            rectToBeDrawn.bottom = (int) bottom;

        }
    }

    public float getFrameSize() {
        return frameSize;
    }

    public Rect getRectToBeDrawn() {
        return rectToBeDrawn;
    }

    public Rect getRectDestination() {
        return rectDestination;
    }
}
