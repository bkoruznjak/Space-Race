package hr.from.bkoruznjak.spacerace.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.provider.Settings;

import hr.from.bkoruznjak.spacerace.R;
import hr.from.bkoruznjak.spacerace.contants.BitmapSizeConstants;

/**
 * Created by bkoruznjak on 26/01/2017.
 */

public class SpaceShip {

    private double lastTime=0;


    private final float GRAVITY_RATIO= 1.5f ;
    //Limit the bounds of the ship's speed
    private final float MIN_SPEED_FACTOR = 1;
    private final float MAX_SPEED_FACTOR = 2f;
    private final float BOSTER_FACTOR = 1.9f;
    //magic numbers, we modify these once we have the scale from the builder
    private float FLEME_EFFECT_RELATIVE_TO_SHIP=60;
    private float FLEME_EFFECT_BOOSTED_RELATIVE_TO_SHIP=110;
    private int FLAME_EFFECT_X_OFFSET = 20;
    private int HITBOX_REDUCTON = 10;
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
    private Rect hitBox, rectFlames,rectFlamesBoosted;
    private int shieldStrength;
    private int x, y, screenX, screenY;
    private float gravity;
    private float speedClimb;
    private float speed;
    private boolean boosting;
    private float mTimeSpentBoosting = 0f;
    private float mBoostStartTime = 0f;
    private float mBoostEndTime = 0f;
    private float mScale;
    private float minSpeed,maxSpeed;
    private float minSpeedClimb,maxSpeedClimb;
    private Builder builder;
    private int hitboxReducion;
    private SpaceShip(Builder builder) {
        FLAME_EFFECT_X_OFFSET =   (FLAME_EFFECT_X_OFFSET *builder.sizeY/100);
        //HITBOX_REDUCTON = (int) (HITBOX_REDUCTON * builder.scale);
        this.bitmap = builder.bitmap;
        this.builder=builder;
        this.effectTrailArray = builder.normalEffectTrailArray;
        this.effectTrailArrayEnhanced = builder.enhancedEffectTrailArray;
        this.x = builder.x;
        this.y = builder.y;
        this.screenX = builder.screenX;
        this.screenY = builder.screenY;
        this.speed = builder.speed;
        this.speedClimb= builder.speedClimb;
        this.gravity=-builder.speedClimb*GRAVITY_RATIO;
        this.maxSpeed=MAX_SPEED_FACTOR*speed;
        this.minSpeed=MIN_SPEED_FACTOR*speed;
        this.maxSpeedClimb=MAX_SPEED_FACTOR*speedClimb;
        this.minSpeedClimb=MIN_SPEED_FACTOR*speedClimb;
        this.maxY = this.screenY - bitmap.getHeight();
        this.minY = 0;
        hitboxReducion=HITBOX_REDUCTON*builder.sizeY/100;
        this.hitBox = new Rect(x + hitboxReducion, y +hitboxReducion,builder.sizeY* bitmap.getWidth()/bitmap.getHeight()- hitboxReducion,builder.sizeY - hitboxReducion);
        this.shieldStrength = 3;
        /*
        this.effectX = this.x + FLAME_EFFECT_X_OFFSET - effectTrailArray[0].getWidth();
        this.effectBoostedX = this.x + FLAME_EFFECT_X_OFFSET - effectTrailArrayEnhanced[0].getWidth();
        this.effectY = this.y + ((bitmap.getHeight() - effectTrailArray[0].getHeight()) / 2);
        this.effectBoostedY = this.y + ((bitmap.getHeight() - effectTrailArrayEnhanced[0].getHeight()) / 2);*/
        this.x = this.x+ (int) FLEME_EFFECT_BOOSTED_RELATIVE_TO_SHIP*hitBox.width()/100;
        this.effectX = this.x + FLAME_EFFECT_X_OFFSET - (int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.width()/100 ;
        this.effectBoostedX = this.x + FLAME_EFFECT_X_OFFSET - (int)FLEME_EFFECT_BOOSTED_RELATIVE_TO_SHIP*hitBox.width()/100;
        this.effectY = this.y + ((hitBox.height() -(int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.height()/100) / 2);
        this.effectBoostedY = this.y +((hitBox.height() - (int)FLEME_EFFECT_BOOSTED_RELATIVE_TO_SHIP*hitBox.height()/100) / 2);
        rectFlames=new Rect(effectX,effectY,effectX+(int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.height()/100,effectY+(int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.width()/100);
        rectFlamesBoosted=new Rect(effectBoostedX,effectBoostedY,effectBoostedX+(int) FLEME_EFFECT_BOOSTED_RELATIVE_TO_SHIP*hitBox.height()/100,effectBoostedY+(int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.width()/100);
        this.mScale = builder.scale;

    }

    public float getSpeed() {
        return this.speed;
    }

    public Rect getHitbox() {
        return this.hitBox;
    }

    public void resetShipAttributes() {
        this.x = builder.x;
        this.y = builder.y;
        this.speed = builder.speed;
        this.speedClimb= builder.speedClimb;
        shieldStrength = 3;
        mBoostStartTime = 0f;
        mBoostEndTime = 0f;
        mTimeSpentBoosting = 0f;
        // Refresh hit box location
        this.effectX = this.x + FLAME_EFFECT_X_OFFSET - (int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.width()/100 -hitBox.width();
        this.effectBoostedX = this.x + FLAME_EFFECT_X_OFFSET - (int)FLEME_EFFECT_BOOSTED_RELATIVE_TO_SHIP*hitBox.width()/100-hitBox.width();
        this.effectY = this.y + ((hitBox.height() -(int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.height()/100) / 2);
        this.effectBoostedY = this.y +((hitBox.height() - (int)FLEME_EFFECT_BOOSTED_RELATIVE_TO_SHIP*hitBox.height()/100) / 2);
        hitBox.left = x + hitboxReducion;
        hitBox.top = y + hitboxReducion;
        hitBox.right = x + builder.sizeY* bitmap.getWidth()/bitmap.getHeight()- hitboxReducion;
        hitBox.bottom = y +  builder.sizeY - hitboxReducion;
        this.effectY =hitBox.top + ((hitBox.height() -(int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.height()/100) / 2);
        this.effectBoostedY = hitBox.top +((hitBox.height() - (int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.height()/100) / 2);
        rectFlames=new Rect(effectX,effectY,effectX+(int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.height()/100,effectY+(int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.width()/100);
        rectFlamesBoosted=new Rect(effectBoostedX,effectY,effectBoostedX+(int)FLEME_EFFECT_BOOSTED_RELATIVE_TO_SHIP*hitBox.height()/100,effectY+(int) FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.width()/100);
        this.x = this.x+ rectFlamesBoosted.width();

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
         if(lastTime!=0) {
             double  diffrenceTime= System.currentTimeMillis()-lastTime;
             diffrenceTime=diffrenceTime/1000;

             // Are we boosting?
             if (boosting) {
                 // Speed up
                 speed +=   builder.speed*BOSTER_FACTOR*diffrenceTime;
                 speedClimb +=   builder.speedClimb*BOSTER_FACTOR*diffrenceTime;
             } else {
                 // Slow down
                 speed -=   builder.speed*BOSTER_FACTOR*diffrenceTime;
                 speedClimb -=  builder.speedClimb*BOSTER_FACTOR*diffrenceTime;

             }

             // Constrain top speed
             if (speed > maxSpeed) {
                 speed = maxSpeed ;
                 speedClimb = maxSpeedClimb;
             }

             // Never stop completely
             if (speed < minSpeed) {
                 speed = minSpeed;
                 speedClimb = minSpeedClimb ;
             }
             double diffrenceMove=(speedClimb+gravity)*diffrenceTime;
             // move the ship up or down
             y -= diffrenceMove;
             effectY -= diffrenceMove;
             effectBoostedY -= diffrenceMove;

             // But don't let ship stray off screen
             if (y <= minY) {
                 speedClimb = -gravity;
                 y = minY;

                 this.effectY =minY + ((hitBox.height() -(int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.height()/100) / 2);
                 this.effectBoostedY =minY +((hitBox.height() - (int) FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.height()/100) / 2);

                 rectFlames=new Rect(effectX,effectY,effectX+(int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.width()/100,effectY+(int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.width()/100);
                 rectFlamesBoosted=new Rect(effectBoostedX,effectBoostedY,effectBoostedX+(int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.width()/100,effectBoostedY+(int) FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.width()/100);

             }

            else if (y >=maxY) {
                 y = maxY;
                 speedClimb = -gravity;


                 this.effectY =maxY + ((hitBox.height() -(int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.height()/100) / 2);
                 this.effectBoostedY = maxY +((hitBox.height() - (int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.height()/100) / 2);
                 rectFlames=new Rect(effectX,effectY,effectX+(int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.width()/100,effectY+(int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.width()/100);
                 rectFlamesBoosted=new Rect(effectBoostedX,effectBoostedY,effectBoostedX+(int) FLEME_EFFECT_BOOSTED_RELATIVE_TO_SHIP*hitBox.width()/100,effectBoostedY+(int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.width()/100);
             }

             // Refresh hit box location
             hitBox.left = x + hitboxReducion;
             hitBox.top = y + hitboxReducion;
             hitBox.right = x + builder.sizeY* bitmap.getWidth()/bitmap.getHeight()- hitboxReducion;
             hitBox.bottom = y +  builder.sizeY -hitboxReducion;
             this.effectX =  hitBox.left  + FLAME_EFFECT_X_OFFSET - (int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.width()/100;
             this.effectBoostedX = hitBox.left + FLAME_EFFECT_X_OFFSET - (int)FLEME_EFFECT_BOOSTED_RELATIVE_TO_SHIP*hitBox.width()/100;
             this.effectY =hitBox.top + ((hitBox.height() -(int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.height()/100) / 2);
             this.effectBoostedY = hitBox.top +((hitBox.height() - (int)FLEME_EFFECT_BOOSTED_RELATIVE_TO_SHIP*hitBox.height()/100) / 2);
             rectFlames=new Rect(effectX,effectY,effectX+(int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.width()/100,effectY+(int)FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.height()/100);
             rectFlamesBoosted=new Rect(effectBoostedX,effectY,effectBoostedX+(int)FLEME_EFFECT_BOOSTED_RELATIVE_TO_SHIP*hitBox.width()/100,effectY+(int) FLEME_EFFECT_RELATIVE_TO_SHIP*hitBox.height()/100);
         }
        lastTime=System.currentTimeMillis();
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
    public  Rect getRectFlames(){
          return  rectFlames;
     }
    public  Rect getRectFlamesBoosted(){
        return  rectFlamesBoosted;
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
        private int x, y, screenX, screenY,sizeX,sizeY;
        private float speed;
        private float speedClimb;
        private float scale;


        public Builder(Context context) {
            this.context = context;
        }

        public Builder bitmap(int bitmapResource) {
            this.bitmap = BitmapFactory.decodeResource
                    (context.getResources(), bitmapResource);

            final float scale = context.getResources().getDisplayMetrics().density;
            this.scale = scale;
            int width = (int) (BitmapSizeConstants.WIDTH_PLAYER_SHIP_AIM * scale + 0.5f);
            int heigth = (int) (BitmapSizeConstants.HEIGHT_PLAYER_SHIP_AIM * scale + 0.5f);
            this.bitmap = Bitmap.createScaledBitmap(bitmap, width, heigth, false);

            //setup the effects
            //testing for fire effect
            int fire_width = (int) (BitmapSizeConstants.WIDTH_PLAYER_BOOST_OFF * scale + 0.5f);
            int fire_heigth = (int) (BitmapSizeConstants.HEIGHT_PLAYER_BOOST_OFF * scale + 0.5f);

            int fire_width_enhanced = (int) (BitmapSizeConstants.WIDTH_PLAYER_BOOST_ON * scale + 0.5f);
            int fire_heigth_enhanced = (int) (BitmapSizeConstants.HEIGHT_PLAYER_BOOST_ON * scale + 0.5f);
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
        public Builder sizeX(int size) {
            this.sizeX = size;
            return this;
        }

        public Builder sizeY(int size) {
            this.sizeY = size;
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

        public Builder speed(float speed) {
            this.speed = speed;
            return this;
        }
        public Builder speedClimb(float speed) {
            this.speedClimb = speed;
            return this;
        }
        public SpaceShip build() {
            return new SpaceShip(this);
        }

    }
}
