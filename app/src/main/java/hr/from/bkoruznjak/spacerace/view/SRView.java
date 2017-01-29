package hr.from.bkoruznjak.spacerace.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

import hr.from.bkoruznjak.spacerace.R;
import hr.from.bkoruznjak.spacerace.contants.PreferenceKeyConstants;
import hr.from.bkoruznjak.spacerace.model.EnemyShip;
import hr.from.bkoruznjak.spacerace.model.Planet;
import hr.from.bkoruznjak.spacerace.model.SpaceDust;
import hr.from.bkoruznjak.spacerace.model.SpaceShip;
import hr.from.bkoruznjak.spacerace.model.firebase.HighScore;

/**
 * Created by bkoruznjak on 26/01/2017.
 */

public class SRView extends SurfaceView implements Runnable, SRControl, GameControl {


    private static final int TARGET_FPS = 60;
    //this is just a user safety feature to block immediate restart for 5secs after game ends.
    private static final int GAME_RESET_TIMEOUT_IN_MILLIS = 1500;
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
    private Bitmap mImgLife;
    private float mTargetFrameDrawTime;
    private float mDistanceCovered;
    private boolean highScoreAchieved;
    private long mPlayerScore;
    private long mTimeStartCurrentFrame;
    private long mDelta;
    private long mTimeEndCurrentFrame;
    private long mTimeSleepInMillis;
    private long mTimeTaken;
    private long mTimeResetDelayStart;
    private long mTimeResetDelayEnd;
    private double mTimeTakenDecimal;
    private long mTimeStarted;
    private long mHighScore;
    private int mScreenX;
    private int mScreenY;
    private int mSpecialEffectsIndex;

    private boolean gameEnded;

    private volatile boolean playing;

    public SRView(Context context, int x, int y) {
        super(context);
        this.mContext = context;

        // Get a reference to a file called HiScores.
        // If id doesn't exist one is created
        mPrefs = context.getSharedPreferences(PreferenceKeyConstants.KEY_SHARED_PREFERENCES,
                context.MODE_PRIVATE);

        mScreenX = x;
        mScreenY = y;

        this.mTargetFrameDrawTime = 1000f / TARGET_FPS;
        this.mSurfaceHolder = getHolder();
        this.mBackgroundColor = new Paint();
        this.mStarColor = new Paint();
        this.mHudColor = new Paint();

        final float scale = context.getResources().getDisplayMetrics().density;
        //load the shield graphics
        int life_size = (int) (16 * scale + 0.5f);
        mImgLife = BitmapFactory.decodeResource
                (context.getResources(), R.drawable.img_heart);
        mImgLife = Bitmap.createScaledBitmap(mImgLife, life_size, life_size, false);
        //load the explosin graphic
        init();
    }

    private void init() {

        gameEnded = false;
        mPlayerScore = 0l;
        highScoreAchieved = false;
        mHighScore = mPrefs.getLong(PreferenceKeyConstants.KEY_PERSONAL_HIGHSCORE, 0);
        if (playing) {
            this.mPlayerShip.resetShipAttributes();
            this.mEnemy1.setX(-mEnemy1.getHitbox().right);
            this.mEnemy2.setX(-mEnemy2.getHitbox().right);
            this.mEnemy3.setX(-mEnemy3.getHitbox().right);
        } else {
            this.mPlanet = new Planet
                    .Builder(mContext)
                    .screenX(mScreenX)
                    .screenY(mScreenY)
                    .build();

            this.mPlayerShip = new SpaceShip
                    .Builder(mContext)
                    .bitmap(R.drawable.viking)
                    .speed(50)
                    .x(50)
                    .y(50)
                    .screenX(mScreenX)
                    .screenY(mScreenY)
                    .build();

            this.mEnemy1 = new EnemyShip
                    .Builder(mContext)
                    .bitmap(R.drawable.protoss_scout)
                    .screenX(mScreenX)
                    .screenY(mScreenY)
                    .build();

            this.mEnemy2 = new EnemyShip
                    .Builder(mContext)
                    .bitmap(R.drawable.protoss_scout)
                    .screenX(mScreenX)
                    .screenY(mScreenY)
                    .build();

            this.mEnemy3 = new EnemyShip
                    .Builder(mContext)
                    .bitmap(R.drawable.protoss_scout)
                    .screenX(mScreenX)
                    .screenY(mScreenY)
                    .build();

            int numSpecs = 40;
            if (mDustArray == null) {
                mDustArray = new SpaceDust[40];
                for (int i = 0; i < numSpecs; i++) {
                    // Where will the dust spawn?
                    SpaceDust spec = new SpaceDust(mScreenX, mScreenY);
                    mDustArray[i] = spec;
                }
            }
        }

        // Reset time and distance covered
        mDistanceCovered = 0f;
        mTimeTaken = 0;
        mTimeTakenDecimal = 0;

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
            mEnemy1.setX(-mEnemy1.getHitbox().right);
        }

        if (Rect.intersects
                (mPlayerShip.getHitbox(), mEnemy2.getHitbox())) {
            hitDetected = true;
            mEnemy2.setX(-mEnemy2.getHitbox().right);
        }

        if (Rect.intersects
                (mPlayerShip.getHitbox(), mEnemy3.getHitbox())) {
            hitDetected = true;
            mEnemy3.setX(-mEnemy3.getHitbox().right);
        }

        if (hitDetected && !gameEnded) {
            //we vibrate to notify user he got rekt by enemy
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            mPlayerShip.reduceShieldStrength();
            if (mPlayerShip.getShieldStrength() < 0) {
                gameEnded = true;
                mTimeResetDelayStart = System.currentTimeMillis();
                if (mPlayerShip.isBoosting()) {
                    mPlayerShip.stopBoost();
                }
                //calculate your score
                mPlayerScore = (mPlayerShip.getTimeSpentBoosting() != 0) ? (long) (mDistanceCovered * mPlayerShip.getTimeSpentBoosting() / 1000) : (long) mDistanceCovered;

                //todo check if its better than the other scores from firebase
                if (mPlayerScore > mHighScore) {
                    highScoreAchieved = true;
                    mPrefs.edit().putLong(PreferenceKeyConstants.KEY_PERSONAL_HIGHSCORE, mPlayerScore).apply();

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("scores/".concat(UUID.randomUUID().toString()));
                    myRef.setValue(new HighScore(mPrefs.getString(PreferenceKeyConstants.KEY_ALIAS, "Callsign"), mPlayerScore));
                }
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
            mDistanceCovered += mPlayerShip.getSpeed();
            //How long has the player been flying
            mTimeTaken = System.currentTimeMillis() - mTimeStarted;
            mTimeTakenDecimal = mTimeTaken / 1000.0d;
        }
    }

    @Override
    public void draw() {
        if (mSurfaceHolder.getSurface().isValid()) {
            //Get start time for FPS calcualtion
            mTimeStartCurrentFrame = System.nanoTime() / 1000000;
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

            if (mSpecialEffectsIndex >= 3) {
                mSpecialEffectsIndex = 0;
            }

            if (mPlayerShip.isBoosting()) {
                mScreenCanvas.drawBitmap(
                        mPlayerShip.getEffectTrailArrayEnhanced()[mSpecialEffectsIndex],
                        mPlayerShip.getEffectBoostedX(),
                        mPlayerShip.getEffectBoostedY(),
                        mBackgroundColor);
            } else {
                mScreenCanvas.drawBitmap(
                        mPlayerShip.getEffectTrailArray()[mSpecialEffectsIndex],
                        mPlayerShip.getEffectX(),
                        mPlayerShip.getEffectY(),
                        mBackgroundColor);
            }
            mSpecialEffectsIndex++;

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
                mScreenCanvas.drawText("Personal best:" + mHighScore, 10, 20, mHudColor);
                mScreenCanvas.drawText("Flight Time:" + mTimeTakenDecimal + "s", mScreenX / 2, 20, mHudColor);
                mScreenCanvas.drawText("Distance:" +
                        mDistanceCovered / 1000 +
                        " KM", mScreenX / 2, mScreenY - 20, mHudColor);

                for (int i = 0; i < mPlayerShip.getShieldStrength(); i++) {

                    int xcoordinate = 10 + ((mImgLife.getHeight() + 20) * i);
                    mScreenCanvas.drawBitmap(
                            mImgLife,
                            xcoordinate,
                            mScreenY - mImgLife.getHeight(),
                            mBackgroundColor);
                }


                mScreenCanvas.drawText("Velocity:" +
                        mPlayerShip.getSpeed() * 60 +
                        " mps", (mScreenX / 3) * 2, mScreenY - 20, mHudColor);
            } else {
                // Show end screen
                mHudColor.setTextSize(80);
                mHudColor.setTextAlign(Paint.Align.CENTER);
                mScreenCanvas.drawText("Game Over", mScreenX / 2, 100, mHudColor);
                mHudColor.setTextSize(25);
                mScreenCanvas.drawText("Highest score:" +
                        mHighScore, mScreenX / 2, 160, mHudColor);

                mScreenCanvas.drawText("Time:" + mTimeTakenDecimal +
                        "s", mScreenX / 2, 200, mHudColor);

                mScreenCanvas.drawText("Distance covered:" +
                        mDistanceCovered / 1000 + " Km", mScreenX / 2, 240, mHudColor);

                mHudColor.setTextSize(80);
                mScreenCanvas.drawText("Tap to replay!", mScreenX / 2, 350, mHudColor);

                if (highScoreAchieved) {
                    mHudColor.setTextSize(140);
                    mScreenCanvas.drawText("NEW RECORD: " + mPlayerScore, mScreenX / 2, 550, mHudColor);
                } else {
                    mHudColor.setTextSize(120);
                    mScreenCanvas.drawText("SCORE: " + mPlayerScore, mScreenX / 2, 500, mHudColor);
                }

            }

            // Unlock and draw the scene
            mSurfaceHolder.unlockCanvasAndPost(mScreenCanvas);
            //Get end time for FPS calcualtion
            mTimeEndCurrentFrame = System.nanoTime() / 1000000;
        }
    }

    @Override
    public void control() {
        try {
            //calculate FPS
            mDelta = mTimeEndCurrentFrame - mTimeStartCurrentFrame;
            mTimeSleepInMillis = (long) (mTargetFrameDrawTime - mDelta);
            if (mTimeSleepInMillis > 0) {
                gameThread.sleep(mTimeSleepInMillis);
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
                if (!gameEnded) {
                    mPlayerShip.stopBoost();
                }
                // Do something here
                break;

            // Has the player touched the screen?
            case MotionEvent.ACTION_DOWN:
                // If we are currently on the pause screen, start a new game
                if (gameEnded) {
                    mTimeResetDelayEnd = System.currentTimeMillis();
                    if ((mTimeResetDelayEnd - mTimeResetDelayStart) >= GAME_RESET_TIMEOUT_IN_MILLIS) {
                        mTimeResetDelayStart = 0;
                        mTimeResetDelayEnd = 0;
                        init();
                    }
                } else {
                    mPlayerShip.startBoost();
                }
                break;
        }

        return true;
    }
}
