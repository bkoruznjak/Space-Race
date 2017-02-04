package hr.from.bkoruznjak.spacerace.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import hr.from.bkoruznjak.spacerace.R;

/**
 * Created by bkoruznjak on 26/01/2017.
 */

public class SpaceShip {

    //magic numbers, we modify these once we have the scale from the builder
    private static int FLAME_EFFECT_X_OFFSET = 15;
    private static int HITBOX_REDUCTON = 10;
    private final int GRAVITY = -12;
    //Limit the bounds of the ship's speed
    private final int MIN_SPEED = 1;
    private final int MAX_SPEED = 25;
    // Stop ship leaving the screen
    private int maxY;
    private int minY;
    private Bitmap bitmap;
    private Bitmap[] effectTrailArray = new Bitmap[4];
    private Bitmap[] effectTrailArrayEnhanced = new Bitmap[4];
    //fire effect coordinates
    private int effectX;
    private int effectY;
    private int effectBoostedX;
    private int effectBoostedY;
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
        this.effectTrailArray = builder.normalEffectTrailArray;
        this.effectTrailArrayEnhanced = builder.enhancedEffectTrailArray;
        this.x = builder.x;
        this.y = builder.y;
        this.screenX = builder.screenX;
        this.screenY = builder.screenY;
        this.speed = builder.speed;
        this.maxY = this.screenY - bitmap.getHeight();
        this.minY = 0;
        this.hitBox = new Rect(x + HITBOX_REDUCTON, y + HITBOX_REDUCTON, bitmap.getWidth() - HITBOX_REDUCTON, bitmap.getHeight() - HITBOX_REDUCTON);
        this.shieldStrength = 3;
        this.effectX = this.x + FLAME_EFFECT_X_OFFSET - effectTrailArray[0].getWidth();
        this.effectBoostedX = this.x + FLAME_EFFECT_X_OFFSET - effectTrailArrayEnhanced[0].getWidth();
        this.effectY = this.y + ((bitmap.getHeight() - effectTrailArray[0].getHeight()) / 2);
        this.effectBoostedY = this.y + ((bitmap.getHeight() - effectTrailArrayEnhanced[0].getHeight()) / 2);
    }

    public int getSpeed() {
        return this.speed;
    }

    public Rect getHitbox() {
        return this.hitBox;
    }

    public void resetShipAttributes() {
        x = 50;
        y = 50;
        speed = 50;
        shieldStrength = 3;
        mBoostStartTime = 0f;
        mBoostEndTime = 0f;
        mTimeSpentBoosting = 0f;
        // Refresh hit box location
        effectX = x + FLAME_EFFECT_X_OFFSET - effectTrailArray[0].getWidth();
        effectBoostedX = x + FLAME_EFFECT_X_OFFSET - effectTrailArrayEnhanced[0].getWidth();
        effectY = y + ((bitmap.getHeight() - effectTrailArray[0].getHeight()) / 2);
        effectBoostedY = y + ((bitmap.getHeight() - effectTrailArrayEnhanced[0].getHeight()) / 2);
        hitBox.left = x + HITBOX_REDUCTON;
        hitBox.top = y + HITBOX_REDUCTON;
        hitBox.right = x + bitmap.getWidth() - HITBOX_REDUCTON;
        hitBox.bottom = y + bitmap.getHeight() - HITBOX_REDUCTON;
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
        effectY -= speed + GRAVITY;
        effectBoostedY -= speed + GRAVITY;

        // But don't let ship stray off screen
        if (y < minY) {
            y = minY;
            effectY = minY + ((bitmap.getHeight() - effectTrailArray[0].getHeight()) / 2);
            effectBoostedY = minY + ((bitmap.getHeight() - effectTrailArrayEnhanced[0].getHeight()) / 2);
        }

        if (y > maxY) {
            y = maxY;
            effectY = maxY + ((bitmap.getHeight() - effectTrailArray[0].getHeight()) / 2);
            effectBoostedY = maxY + ((bitmap.getHeight() - effectTrailArrayEnhanced[0].getHeight()) / 2);
        }

        // Refresh hit box location
        hitBox.left = x + HITBOX_REDUCTON;
        hitBox.top = y + HITBOX_REDUCTON;
        hitBox.right = x + bitmap.getWidth() - HITBOX_REDUCTON;
        hitBox.bottom = y + bitmap.getHeight() - HITBOX_REDUCTON;
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

    public Bitmap[] getEffectTrailArray() {
        return effectTrailArray;
    }

    public int getEffectX() {
        return effectX;
    }

    public int getEffectY() {
        return effectY;
    }

    public Bitmap[] getEffectTrailArrayEnhanced() {
        return effectTrailArrayEnhanced;
    }

    public int getEffectBoostedY() {
        return effectBoostedY;
    }

    public int getEffectBoostedX() {
        return effectBoostedX;
    }

    public static class Builder {
        private Context context;
        private Bitmap bitmap;
        private Bitmap[] normalEffectTrailArray = new Bitmap[4];
        private Bitmap[] enhancedEffectTrailArray = new Bitmap[4];
        private int x, y, screenX, screenY;
        private int speed;


        public Builder(Context context) {
            this.context = context;
        }

        public Builder bitmap(int bitmapResource) {
            this.bitmap = BitmapFactory.decodeResource
                    (context.getResources(), bitmapResource);

            final float scale = context.getResources().getDisplayMetrics().density;
            FLAME_EFFECT_X_OFFSET = (int) (FLAME_EFFECT_X_OFFSET * scale);
            HITBOX_REDUCTON = (int) (HITBOX_REDUCTON * scale);
            int width = (int) (58 * scale + 0.5f);
            int heigth = (int) (64 * scale + 0.5f);
            this.bitmap = Bitmap.createScaledBitmap(bitmap, width, heigth, false);

            //setup the effects
            //testing for fire effect
            int fire_width = (int) (36 * scale + 0.5f);
            int fire_heigth = (int) (20 * scale + 0.5f);

            int fire_width_enhanced = (int) (50 * scale + 0.5f);
            int fire_heigth_enhanced = (int) (34 * scale + 0.5f);
            Bitmap fire1 = BitmapFactory.decodeResource
                    (context.getResources(), R.drawable.fire_one);
            Bitmap fire1_boosted = Bitmap.createScaledBitmap(fire1, fire_width_enhanced, fire_heigth_enhanced, false);
            fire1 = Bitmap.createScaledBitmap(fire1, fire_width, fire_heigth, false);


            Bitmap fire2 = BitmapFactory.decodeResource
                    (context.getResources(), R.drawable.fire_two);
            Bitmap fire2_boosted = Bitmap.createScaledBitmap(fire2, fire_width_enhanced, fire_heigth_enhanced, false);
            fire2 = Bitmap.createScaledBitmap(fire2, fire_width, fire_heigth, false);

            Bitmap fire3 = BitmapFactory.decodeResource
                    (context.getResources(), R.drawable.fire_three);
            Bitmap fire3_boosted = Bitmap.createScaledBitmap(fire3, fire_width_enhanced, fire_heigth_enhanced, false);
            fire3 = Bitmap.createScaledBitmap(fire3, fire_width, fire_heigth, false);

            Bitmap fire4 = BitmapFactory.decodeResource
                    (context.getResources(), R.drawable.fire_four);
            Bitmap fire4_boosted = Bitmap.createScaledBitmap(fire4, fire_width_enhanced, fire_heigth_enhanced, false);
            fire4 = Bitmap.createScaledBitmap(fire4, fire_width, fire_heigth, false);

            normalEffectTrailArray[0] = fire1;
            normalEffectTrailArray[1] = fire2;
            normalEffectTrailArray[2] = fire3;
            normalEffectTrailArray[3] = fire4;

            enhancedEffectTrailArray[0] = fire1_boosted;
            enhancedEffectTrailArray[1] = fire2_boosted;
            enhancedEffectTrailArray[2] = fire3_boosted;
            enhancedEffectTrailArray[3] = fire4_boosted;

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
