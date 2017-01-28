package hr.from.bkoruznjak.spacerace.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

/**
 * Created by bkoruznjak on 26/01/2017.
 */

public class SpaceShip {

    private final int GRAVITY = -12;
    //Limit the bounds of the ship's speed
    private final int MIN_SPEED = 1;
    private final int MAX_SPEED = 20;
    // Stop ship leaving the screen
    private int maxY;
    private int minY;

    private Bitmap bitmap;
    // A hit box for collision detection
    private Rect hitBox;
    private int shieldStrength;
    private int x, y, screenX, screenY;
    private int speed;
    private boolean boosting;
    private float mTimeSpentBoosting = 0f;
    private float mBoostStartTime = 0f;
    private float mBoostEndTime = 0f;

    private SpaceShip(Builder builder) {
        this.bitmap = builder.bitmap;
        this.x = builder.x;
        this.y = builder.y;
        this.screenX = builder.screenX;
        this.screenY = builder.screenY;
        this.speed = builder.speed;
        this.maxY = this.screenY - bitmap.getHeight();
        this.minY = 0;
        this.hitBox = new Rect(x, y, bitmap.getWidth(), bitmap.getHeight());
        this.shieldStrength = 3;
    }

    public int getSpeed() {
        return this.speed;
    }

    public Rect getHitbox() {
        return this.hitBox;
    }

    public void resetShipAttributes() {
        this.x = 50;
        this.y = 50;
        this.speed = 50;
        this.shieldStrength = 3;
        mBoostStartTime = 0f;
        mBoostEndTime = 0f;
        mTimeSpentBoosting = 0f;
        update();
    }

    public int getShieldStrength() {
        return this.shieldStrength;
    }

    public void setShieldStrength(int shieldStrength) {
        this.shieldStrength = shieldStrength;
    }

    public void setScreenX(int x) {
        this.x = x;
    }

    public void setScreenY(int y) {
        this.y = y;
    }

    public void reduceShieldStrength() {
        this.shieldStrength--;
    }

    public void startBoost() {
        boosting = true;
        mBoostStartTime = System.nanoTime() / 1000000;
        mBoostEndTime = 0f;
    }

    public void stopBoost() {
        boosting = false;
        mBoostEndTime = System.nanoTime() / 1000000;
        if (mBoostStartTime != 0f) {
            mTimeSpentBoosting += mBoostEndTime - mBoostStartTime;
        }
        mBoostEndTime = 0f;
        mBoostStartTime = 0f;
    }

    public void update() {

        // Are we boosting?
        if (boosting) {
            // Speed up
            speed += 2;
        } else {
            // Slow down
            speed -= 5;
        }

        // Constrain top speed
        if (speed > MAX_SPEED) {
            speed = MAX_SPEED;
        }

        // Never stop completely
        if (speed < MIN_SPEED) {
            speed = MIN_SPEED;
        }

        // move the ship up or down
        y -= speed + GRAVITY;

        // But don't let ship stray off screen
        if (y < minY) {
            y = minY;
        }

        if (y > maxY) {
            y = maxY;
        }

        // Refresh hit box location
        hitBox.left = x;
        hitBox.top = y;
        hitBox.right = x + bitmap.getWidth();
        hitBox.bottom = y + bitmap.getHeight();
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public float getTimeSpentBoosting() {
        return mTimeSpentBoosting;
    }

    public boolean isBoosting() {
        return boosting;
    }

    public static class Builder {
        private Context context;
        private Bitmap bitmap;
        private int x, y, screenX, screenY;
        private int speed;


        public Builder(Context context) {
            this.context = context;
        }

        public Builder bitmap(int bitmapResource) {
            this.bitmap = BitmapFactory.decodeResource
                    (context.getResources(), bitmapResource);

            final float scale = context.getResources().getDisplayMetrics().density;
            int width = (int) (58 * scale + 0.5f);
            int heigth = (int) (64 * scale + 0.5f);
            this.bitmap = Bitmap.createScaledBitmap(bitmap, width, heigth, false);
            return this;
        }

        public Builder x(int x) {
            this.x = x;
            return this;
        }

        public Builder y(int y) {
            this.y = y;
            return this;
        }

        public Builder screenX(int x) {
            this.screenX = x;
            return this;
        }

        public Builder screenY(int y) {
            this.screenY = y;
            return this;
        }

        public Builder speed(int speed) {
            this.speed = speed;
            return this;
        }

        public SpaceShip build() {
            return new SpaceShip(this);
        }

    }
}
