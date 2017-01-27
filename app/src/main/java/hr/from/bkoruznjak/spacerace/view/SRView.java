package hr.from.bkoruznjak.spacerace.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

import hr.from.bkoruznjak.spacerace.R;
import hr.from.bkoruznjak.spacerace.model.EnemyShip;
import hr.from.bkoruznjak.spacerace.model.SpaceDust;
import hr.from.bkoruznjak.spacerace.model.SpaceShip;

/**
 * Created by bkoruznjak on 26/01/2017.
 */

public class SRView extends SurfaceView implements Runnable, SRControl, GameControl {

    private static final int TARGET_FPS = 60;
    private EnemyShip mEnemy1;
    private EnemyShip mEnemy2;
    private EnemyShip mEnemy3;
    private ArrayList<SpaceDust> mDustList = new
            ArrayList<SpaceDust>();
    private Thread gameThread = null;
    private SurfaceHolder mSurfaceHolder;
    private Canvas mScreenCanvas;
    private Paint mBackgroundColor;
    private Paint mStarColor;
    private SpaceShip mPlayerShip;
    private float mTargetFrameDrawTime;
    private long mStartTimeCurrentFrame;
    private long mDelta;
    private long mEndTimeCurrentFrame;
    private long mSleepTimeInMillis;
    private volatile boolean playing;

    public SRView(Context context, int x, int y) {
        super(context);
        init(context, x, y);
    }

    private void init(Context context, int screenX, int screenY) {
        this.mTargetFrameDrawTime = 1000f / TARGET_FPS;
        this.mSurfaceHolder = getHolder();
        this.mBackgroundColor = new Paint();
        this.mStarColor = new Paint();

        this.mPlayerShip = new SpaceShip
                .Builder(context)
                .bitmap(R.drawable.ship)
                .speed(50)
                .x(50)
                .y(50)
                .screenX(screenX)
                .screenY(screenY)
                .build();

        this.mEnemy1 = new EnemyShip
                .Builder(context)
                .bitmap(R.drawable.enemy)
                .screenX(screenX)
                .screenY(screenY)
                .build();

        this.mEnemy2 = new EnemyShip
                .Builder(context)
                .bitmap(R.drawable.enemy)
                .screenX(screenX)
                .screenY(screenY)
                .build();

        this.mEnemy3 = new EnemyShip
                .Builder(context)
                .bitmap(R.drawable.enemy)
                .screenX(screenX)
                .screenY(screenY)
                .build();

        int numSpecs = 40;
        for (int i = 0; i < numSpecs; i++) {
            // Where will the dust spawn?
            SpaceDust spec = new SpaceDust(screenX, screenY);
            mDustList.add(spec);
        }
    }

    @Override
    public void run() {

        while (playing) {
            update();
            draw();
            control();
        }

    }

    @Override
    public void update() {

        // Collision detection on new positions
        // Before move because we are testing last frames
        // position which has just been drawn

        // If you are using images in excess of 100 pixels
        // wide then increase the -100 value accordingly
        if (Rect.intersects
                (mPlayerShip.getHitbox(), mEnemy1.getHitbox())) {
            mEnemy1.setX(-500);
        }

        if (Rect.intersects
                (mPlayerShip.getHitbox(), mEnemy2.getHitbox())) {
            mEnemy2.setX(-500);
        }

        if (Rect.intersects
                (mPlayerShip.getHitbox(), mEnemy3.getHitbox())) {
            mEnemy3.setX(-500);
        }

        mPlayerShip.update();
        // Update the enemies
        int playerSpeed = mPlayerShip.getSpeed();
        mEnemy1.update(playerSpeed);
        mEnemy2.update(playerSpeed);
        mEnemy3.update(playerSpeed);

        for (SpaceDust spaceDust : mDustList) {
            spaceDust.update(playerSpeed);
        }
    }

    @Override
    public void draw() {
        if (mSurfaceHolder.getSurface().isValid()) {
            //Get start time for FPS calcualtion
            mStartTimeCurrentFrame = System.nanoTime() / 1000000;
            //First we lock the area of memory we will be drawing to
            mScreenCanvas = mSurfaceHolder.lockCanvas();

            // Rub out the last frame
            mScreenCanvas.drawColor(Color.argb(255, 0, 0, 0));

            // White specs of dust
            mStarColor.setColor(Color.argb(255, 255, 255, 255));
            //Draw the dust from our arrayList
            for (SpaceDust spaceDust : mDustList) {
                mScreenCanvas.drawPoint(spaceDust.getX(), spaceDust.getY(), mStarColor);
            }

            // Draw the player
            mScreenCanvas.drawBitmap(
                    mPlayerShip.getBitmap(),
                    mPlayerShip.getX(),
                    mPlayerShip.getY(),
                    mBackgroundColor);

            mScreenCanvas.drawBitmap
                    (mEnemy1.getBitmap(),
                            mEnemy1.getX(),
                            mEnemy1.getY(),
                            mBackgroundColor);

            mScreenCanvas.drawBitmap
                    (mEnemy2.getBitmap(),
                            mEnemy2.getX(),
                            mEnemy2.getY(),
                            mBackgroundColor);

            mScreenCanvas.drawBitmap
                    (mEnemy3.getBitmap(),
                            mEnemy3.getX(),
                            mEnemy3.getY(),
                            mBackgroundColor);

            // Unlock and draw the scene
            mSurfaceHolder.unlockCanvasAndPost(mScreenCanvas);
            //Get end time for FPS calcualtion
            mEndTimeCurrentFrame = System.nanoTime() / 1000000;
        }
    }

    @Override
    public void control() {
        try {
            //calculate FPS
            mDelta = mEndTimeCurrentFrame - mStartTimeCurrentFrame;
            mSleepTimeInMillis = (long) (mTargetFrameDrawTime - mDelta);
            if (mSleepTimeInMillis > 0) {
                gameThread.sleep(mSleepTimeInMillis);
            }
        } catch (InterruptedException e) {
            Log.e("bbb", "InterruptedException:" + e);
        }

    }

    @Override
    public void start() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void pause() {
        Log.d("bbb", "pausing");
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("bbb", "InterruptedException:" + e);
        }
    }

    @Override
    public void resume() {
        Log.d("bbb", "resuming");
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        // There are many different events in MotionEvent
        // We care about just 2 - for now.
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            // Has the player lifted their finger up?
            case MotionEvent.ACTION_UP:
                // Do something here
                break;

            // Has the player touched the screen?
            case MotionEvent.ACTION_DOWN:
                // Do something here
                break;
        }

        return true;
    }
}
