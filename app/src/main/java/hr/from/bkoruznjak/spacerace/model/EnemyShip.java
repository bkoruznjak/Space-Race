package hr.from.bkoruznjak.spacerace.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Random;

/**
 * Created by bkoruznjak on 27/01/2017.
 */

public class EnemyShip {
    private Bitmap mBitmap;
    private int x, y;
    private int speed = 1;

    private int maxX;
    private int minX;

    private int maxY;
    private int minY;

    private EnemyShip(Bitmap mBitmap, int maxX, int maxY, int spawnX, int spawnY) {
        this.mBitmap = mBitmap;
        this.maxX = maxX;
        this.maxY = maxY;
        this.minX = 0;
        this.minY = 0;

        this.x = spawnX;
        this.y = spawnY;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void update(int playerSpeed) {

        // Move to the left
        x -= playerSpeed;
        x -= speed;

        //respawn when off screen
        if (x < minX - mBitmap.getWidth()) {
            Random generator = new Random();
            speed = generator.nextInt(10) + 10;
            x = maxX;
            y = generator.nextInt(maxY) - mBitmap.getHeight();
        }
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

        public Builder bitmap(int bitmapResource) {
            this.mBitmap = BitmapFactory.decodeResource
                    (mContext.getResources(), bitmapResource);
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
            this.mSpeed = generator.nextInt(6) + 10;

            x = maxX;
            y = generator.nextInt(maxY) - mBitmap.getHeight();

            return new EnemyShip(mBitmap, maxX, maxY, x, y);
        }
    }
}