package hr.from.bkoruznjak.spacerace.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.util.Random;

import hr.from.bkoruznjak.spacerace.R;

/**
 * Created by bkoruznjak on 27/01/2017.
 */

public class Planet {
    //change this if you add more planet pngs
    private static final int NUMBER_OF_PLANETS = 6;
    private Bitmap mBitmap;
    // A hit box for collision detection
    private Rect hitBox;
    private int x, y;
    private int speed = 1;

    private int maxX;
    private int minX;

    private int maxY;
    private int minY;

    private Planet(Bitmap mBitmap, int maxX, int maxY, int spawnX, int spawnY) {
        this.x = spawnX;
        this.y = spawnY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.minX = 0;
        this.minY = 0;
        this.mBitmap = mBitmap;
        this.hitBox = new Rect(x, y, mBitmap.getWidth(), mBitmap.getHeight());
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

    public void update(int playerSpeed) {

        // Move to the left
        x -= playerSpeed / 3;
        x -= speed;

        //respawn when off screen
        if (x < minX - mBitmap.getWidth()) {
            Random generator = new Random();
            speed = 1;
            x = maxX;
            y = generator.nextInt(maxY) - mBitmap.getHeight();
        }

        // Refresh hit box location
        hitBox.left = x;
        hitBox.top = y;
        hitBox.right = x + mBitmap.getWidth();
        hitBox.bottom = y + mBitmap.getHeight();
    }

    public static class Builder {

        private Context mContext;
        private Bitmap mBitmap;
        private int x, y;
        private int mSpeed = 1;

        // Detect enemies leaving the screen
        private int maxX;

        // Spawn enemies within screen bounds
        private int maxY;

        public Builder(Context context) {
            this.mContext = context;
        }

        public Builder screenX(int screenX) {
            this.maxX = screenX;
            return this;
        }

        public Builder screenY(int screenY) {
            this.maxY = screenY;
            return this;
        }

        public Planet build() {

            Random generator = new Random();
            this.mSpeed = 1;

            int planetId = 0;

            switch (generator.nextInt(NUMBER_OF_PLANETS)) {
                case 0:
                    planetId = R.drawable.planet_1;
                    break;
                case 1:
                    planetId = R.drawable.planet_2;
                    break;
                case 2:
                    planetId = R.drawable.planet_3;
                    break;
                case 3:
                    planetId = R.drawable.planet_4;
                    break;
                case 4:
                    planetId = R.drawable.planet_5;
                    break;
                case 5:
                    planetId = R.drawable.planet_6;
                    break;

            }

            planetId = (planetId == 0) ? R.drawable.planet_1 : planetId;

            this.mBitmap = BitmapFactory.decodeResource
                    (mContext.getResources(), planetId);

            final float scale = mContext.getResources().getDisplayMetrics().density;
            int width = (int) (256 * scale + 0.5f);
            int heigth = (int) (256 * scale + 0.5f);
            this.mBitmap = Bitmap.createScaledBitmap(mBitmap, width, heigth, false);

            x = maxX;
            y = generator.nextInt(maxY) - mBitmap.getHeight();

            return new Planet(mBitmap, maxX, maxY, x, y);
        }
    }
}
