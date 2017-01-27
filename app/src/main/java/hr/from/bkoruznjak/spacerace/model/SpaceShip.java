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

    public int getShieldStrength() {
        return this.shieldStrength;
    }

    public void setBoost() {
        boosting = true;
    }

    public void startBoost() {
        boosting = false;
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
