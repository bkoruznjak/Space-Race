package hr.from.bkoruznjak.spacerace.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import hr.from.bkoruznjak.spacerace.R;
import hr.from.bkoruznjak.spacerace.model.EnemyShip;
import hr.from.bkoruznjak.spacerace.model.Planet;
import hr.from.bkoruznjak.spacerace.model.SpaceDust;
import hr.from.bkoruznjak.spacerace.model.SpaceShip;

/**
 * Created by bkoruznjak on 26/01/2017.
 */

public class SRView extends SurfaceView implements Runnable, SRControl, GameControl {


    private static final int TARGET_FPS = 60;
    private Context mContext;
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;
    private EnemyShip mEnemy1;
    private EnemyShip mEnemy2;
    private EnemyShip mEnemy3;
    private SpaceDust[] mDustArray;
    private Thread gameThread = null;
    private SurfaceHolder mSurfaceHolder;
    private Canvas mScreenCanvas;
    private Paint mBackgroundColor;
    private Paint mStarColor;
    private Paint mHudColor;
    private SpaceShip mPlayerShip;
    private Planet mPlanet;
    private float mTargetFrameDrawTime;
    private float mDistanceRemaining;
    private long mStartTimeCurrentFrame;
    private long mDelta;
    private long mEndTimeCurrentFrame;
    private long mSleepTimeInMillis;
    private long mTimeTaken;
    private long mTimeStarted;
    private long mFastestTime;
    private int mScreenX;
    private int mScreenY;

    private boolean gameEnded;

    private volatile boolean playing;

    public SRView(Context context, int x, int y) {
        super(context);
        this.mContext = context;

        // Get a reference to a file called HiScores.
        // If id doesn't exist one is created
        mPrefs = context.getSharedPreferences("HiScores",
                context.MODE_PRIVATE);

        // Initialize the editor ready
        mEditor = mPrefs.edit();

        // Load fastest time from a entry in the file
        //  labeled "fastestTime"
        // if not available highscore = 1000000
        mFastestTime = mPrefs.getLong("fastestTime", 1000000);

        mScreenX = x;
        mScreenY = y;

        this.mTargetFrameDrawTime = 1000f / TARGET_FPS;
        this.mSurfaceHolder = getHolder();
        this.mBackgroundColor = new Paint();
        this.mStarColor = new Paint();
        this.mHudColor = new Paint();
        init();
    }

    private void init() {

        gameEnded = false;

        this.mPlanet = new Planet
                .Builder(mContext)
                .screenX(mScreenX)
                .screenY(mScreenY)
                .build();

        this.mPlayerShip = new SpaceShip
                .Builder(mContext)
                .bitmap(R.drawable.ship)
                .speed(50)
                .x(50)
                .y(50)
                .screenX(mScreenX)
                .screenY(mScreenY)
                .build();

        this.mEnemy1 = new EnemyShip
                .Builder(mContext)
                .bitmap(R.drawable.enemy)
                .screenX(mScreenX)
                .screenY(mScreenY)
                .build();

        this.mEnemy2 = new EnemyShip
                .Builder(mContext)
                .bitmap(R.drawable.enemy)
                .screenX(mScreenX)
                .screenY(mScreenY)
                .build();

        this.mEnemy3 = new EnemyShip
                .Builder(mContext)
                .bitmap(R.drawable.enemy)
                .screenX(mScreenX)
                .screenY(mScreenY)
                .build();

        int numSpecs = 40;
        mDustArray = new SpaceDust[40];
        for (int i = 0; i < numSpecs; i++) {
            // Where will the dust spawn?
            SpaceDust spec = new SpaceDust(mScreenX, mScreenY);
            mDustArray[i] = spec;
        }

        // Reset time and distance
        mDistanceRemaining = 10000;// 10 km
        mTimeTaken = 0;

        // Get start time
        mTimeStarted = System.currentTimeMillis();
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
        boolean hitDetected = false;
        if (Rect.intersects
                (mPlayerShip.getHitbox(), mEnemy1.getHitbox())) {
            hitDetected = true;
            mEnemy1.setX(-500);
        }

        if (Rect.intersects
                (mPlayerShip.getHitbox(), mEnemy2.getHitbox())) {
            hitDetected = true;
            mEnemy2.setX(-500);
        }

        if (Rect.intersects
                (mPlayerShip.getHitbox(), mEnemy3.getHitbox())) {
            hitDetected = true;
            mEnemy3.setX(-500);
        }

        if (hitDetected) {
            mPlayerShip.reduceShieldStrength();
            if (mPlayerShip.getShieldStrength() < 0) {
                gameEnded = true;
            }
        }

        mPlayerShip.update();
        // Update the enemies
        int playerSpeed = mPlayerShip.getSpeed();
        mPlanet.update(playerSpeed);
        mEnemy1.update(playerSpeed);
        mEnemy2.update(playerSpeed);
        mEnemy3.update(playerSpeed);

        for (int i = 0; i < mDustArray.length; i++) {
            (mDustArray[i]).update(playerSpeed);
        }

        if (!gameEnded) {
            //subtract distance to home planet based on current speed
            mDistanceRemaining -= mPlayerShip.getSpeed();

            //How long has the player been flying
            mTimeTaken = System.currentTimeMillis() - mTimeStarted;
        }

        //Completed the game!
        if (mDistanceRemaining < 0) {
            //check for new fastest time
            if (mTimeTaken < mFastestTime) {
                mEditor.putLong("fastestTime", mTimeTaken);
                mEditor.apply();
                mFastestTime = mTimeTaken;
            }

            // avoid ugly negative numbers
            // in the HUD
            mDistanceRemaining = 0;

            // Now end the game
            gameEnded = true;
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


            for (int i = 0; i < mDustArray.length; i++) {
                mScreenCanvas.drawPoint((mDustArray[i]).getX(), (mDustArray[i]).getY(), mStarColor);
            }

            //draw the planet
            mScreenCanvas.drawBitmap(
                    mPlanet.getBitmap(),
                    mPlanet.getX(),
                    mPlanet.getY(),
                    mBackgroundColor);

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

            if (!gameEnded) {
                // Draw the hud
                mHudColor.setTextAlign(Paint.Align.LEFT);
                mHudColor.setColor(Color.argb(255, 255, 255, 255));
                mHudColor.setTextSize(25);
                mScreenCanvas.drawText("Fastest:" + mFastestTime + "s", 10, 20, mHudColor);
                mScreenCanvas.drawText("Time:" + mTimeTaken + "s", mScreenX / 2, 20, mHudColor);
                mScreenCanvas.drawText("Distance:" +
                        mDistanceRemaining / 1000 +
                        " KM", mScreenX / 3, mScreenY - 20, mHudColor);

                mScreenCanvas.drawText("Shield:" +
                        mPlayerShip.getShieldStrength(), 10, mScreenY - 20, mHudColor);

                mScreenCanvas.drawText("Speed:" +
                        mPlayerShip.getSpeed() * 60 +
                        " MPS", (mScreenX / 3) * 2, mScreenY - 20, mHudColor);
            } else {
                // Show pause screen
                mHudColor.setTextSize(80);
                mHudColor.setTextAlign(Paint.Align.CENTER);
                mScreenCanvas.drawText("Game Over", mScreenX / 2, 100, mHudColor);
                mHudColor.setTextSize(25);
                mScreenCanvas.drawText("Fastest:" +
                        mFastestTime + "s", mScreenX / 2, 160, mHudColor);

                mScreenCanvas.drawText("Time:" + mTimeTaken +
                        "s", mScreenX / 2, 200, mHudColor);

                mScreenCanvas.drawText("Distance remaining:" +
                        mDistanceRemaining / 1000 + " KM", mScreenX / 2, 240, mHudColor);

                mHudColor.setTextSize(80);
                mScreenCanvas.drawText("Tap to replay!", mScreenX / 2, 350, mHudColor);
            }


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
                mPlayerShip.stopBoost();
                // Do something here
                break;

            // Has the player touched the screen?
            case MotionEvent.ACTION_DOWN:
                mPlayerShip.startBoost();
                // If we are currently on the pause screen, start a new game
                if (gameEnded) {
                    init();
                }
                break;
        }

        return true;
    }
}
