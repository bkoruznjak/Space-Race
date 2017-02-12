package hr.from.bkoruznjak.spacerace.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.util.Random;

import hr.from.bkoruznjak.spacerace.contants.BitmapSizeConstants;

/**
 * Created by bkoruznjak on 27/01/2017.
 */

public class EnemyShip {
    private double lastTime=0;
    //magic numbers, we modify these once we have the scale from the builder
    private int HITBOX_REDUCTON = 10;
    private int SPEED_PERCENTEGE_CHANGE_RANDOM=900;
    private Bitmap mBitmap;
    // A hit box for collision detection
    private Rect hitBox;
    private int x, y;
    private float speed = 1;

    private int maxX;
    private int minX;

    private int maxY;
    private int minY;
    private float mScale;
    private int sizeY;
    private int startSpeed;
    private EnemyShip(Bitmap mBitmap, int maxX, int maxY, int spawnX, int spawnY, int speed, float scale, int sizeY) {
        HITBOX_REDUCTON = (int) (HITBOX_REDUCTON * scale);
        this.mScale = scale;
        this.x = spawnX;
        this.y = spawnY;
        this.sizeY=sizeY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.minX = 0;
        this.minY = 0;
        this.speed =startSpeed= speed;
        this.mBitmap = mBitmap;

        this.hitBox = new Rect(x + HITBOX_REDUCTON, y + HITBOX_REDUCTON, sizeY - HITBOX_REDUCTON, sizeY* mBitmap.getWidth()/mBitmap.getHeight() - HITBOX_REDUCTON);
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public Rect getHitbox() {
        return hitBox;
    }

    public int getX() {
        return x;
    }

    // This is used by the SRView update() method to
    // Make an enemy out of bounds and force a re-spawn
    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void update(float playerSpeed) {
        if(lastTime!=0) {
            double  diffrence= System.currentTimeMillis()-lastTime;
            diffrence=diffrence/1000;
            // Move to the left
            x -= playerSpeed*diffrence;
            x -= speed*diffrence;

            //respawn when off screen
            if (x < minX - mBitmap.getWidth()) {
                Random generator = new Random();

                 if(generator.nextBoolean())   speed =startSpeed +  (generator.nextInt( SPEED_PERCENTEGE_CHANGE_RANDOM)*startSpeed/100);
                 else  speed =startSpeed;
                x = maxX;
                y = generator.nextInt(maxY) - mBitmap.getHeight();
            }

            // Refresh hit box location
            hitBox.left = x + HITBOX_REDUCTON;
            hitBox.top = y + HITBOX_REDUCTON;
            hitBox.right = x + sizeY* mBitmap.getWidth()/mBitmap.getHeight()  - HITBOX_REDUCTON;
            hitBox.bottom = y + sizeY - HITBOX_REDUCTON;
        }
        lastTime=System.currentTimeMillis();
    }

    public static class Builder {

        private Context mContext;
        private Bitmap mBitmap;
        private int x, y;
        private int mSpeed = 1;

        // Detect enemies leaving the screen
        private int maxX;

        // Spawn enemies within screen bounds
        private int maxY,sizeX,sizeY;
        private float scale;
        private float width;
        private float height;

        public Builder(Context context) {
            this.mContext = context;
        }

        public Builder bitmap(int bitmapResource) {
            this.mBitmap = BitmapFactory.decodeResource
                    (mContext.getResources(), bitmapResource);

            final float scale = mContext.getResources().getDisplayMetrics().density;
            this.scale = scale;
            this.width = mContext.getResources().getDisplayMetrics().widthPixels / 1000;
            this.height = mContext.getResources().getDisplayMetrics().heightPixels / 1000;
            int width = (int) (BitmapSizeConstants.WIDTH_ENEMY_SHIP_AIM * scale + 0.5f);
            int heigth = (int) (BitmapSizeConstants.HEIGHT_ENEMY_SHIP_AIM * scale + 0.5f);
            this.mBitmap = Bitmap.createScaledBitmap(mBitmap, width, heigth, false);
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
        public Builder screenX(int screenX) {
            this.maxX = screenX;
            return this;
        }

        public Builder screenY(int screenY) {
            this.maxY = screenY;
            return this;
        }

        public EnemyShip build() {

            Random generator = new Random();
            this.mSpeed = generator.nextInt(10) + 10;

            x = maxX;
            y = generator.nextInt(maxY) - mBitmap.getHeight();

            return new EnemyShip(mBitmap, maxX, maxY, x, y, mSpeed, scale,sizeY);
        }
    }
}