package hr.from.bkoruznjak.spacerace.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by bkoruznjak on 26/01/2017.
 */

public class SpaceShip {
    private Bitmap bitmap;
    private int x, y;
    private int speed;

    private SpaceShip(Builder builder) {
        this.bitmap = builder.bitmap;
        this.x = builder.x;
        this.y = builder.y;
        this.speed = builder.speed;
    }

    public void update() {
        x++;
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
        private int x, y;
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

        public Builder speed(int speed) {
            this.speed = speed;
            return this;
        }

        public SpaceShip build() {
            return new SpaceShip(this);
        }

    }
}
