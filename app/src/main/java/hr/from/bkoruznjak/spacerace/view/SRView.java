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

import java.util.ArrayList;
import java.util.UUID;

import hr.from.bkoruznjak.spacerace.R;
import hr.from.bkoruznjak.spacerace.contants.PreferenceKeyConstants;
import hr.from.bkoruznjak.spacerace.model.EnemyShip;
import hr.from.bkoruznjak.spacerace.model.Explosion;
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
    final float mScale;
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
    private Bitmap mImgExplosionSprite;
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

    private ArrayList<Explosion> mExplosionList = new ArrayList<>();

    //hud related constants
    private float hudGameOverSize;
    private float hudGameOverY;
    private float hudHighestScoreSize;
    private float hudHighestScoreY;
    private float hudTimeSize;
    private float hudTimeY;
    private float hudDistanceCoveredSize;
    private float hudDistanceCoveredY;
    private float hudTapToRetrySize;
    private float hudTapToRetryY;
    private float hudHighScoreSize;
    private float hudHighScoreY;
    private float hudRecordScoreSize;
    private float hudRecordScoreY;


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

        mScale = context.getResources().getDisplayMetrics().density;
        // init the HUD
        hudGameOverSize = (35 * mScale) + 0.5f;
        hudGameOverY = (75 * mScale) + 0.5f;
        hudHighestScoreSize = (15 * mScale) + 0.5f;
        hudHighestScoreY = (120 * mScale) + 0.5f;
        hudTimeSize = (15 * mScale) + 0.5f;
        hudTimeY = (145 * mScale) + 0.5f;
        hudDistanceCoveredSize = (15 * mScale) + 0.5f;
        hudDistanceCoveredY = (170 * mScale) + 0.5f;
        hudTapToRetrySize = (30 * mScale) + 0.5f;
        hudTapToRetryY = (230 * mScale) + 0.5f;
        hudHighScoreSize = (40 * mScale) + 0.5f;
        hudHighScoreY = (300 * mScale) + 0.5f;
        hudRecordScoreSize = (50 * mScale) + 0.5f;
        hudRecordScoreY = (310 * mScale) + 0.5f;
        //load the shield graphics
        int life_size = (int) (16 * mScale + 0.5f);
        mImgLife = BitmapFactory.decodeResource
                (getResources(), R.drawable.img_heart);
        mImgLife = Bitmap.createScaledBitmap(mImgLife, life_size, life_size, false);
        //load the explosion graphic
        mImgExplosionSprite = BitmapFactory.decodeResource(getResources(), R.drawable.explosion_spritesheet);
        mImgExplosionSprite = Bitmap.createScaledBitmap(mImgExplosionSprite, life_size, life_size, false);
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
                    .bitmap(R.drawable.speedy)
                    .speed(50)
                    .x(50)
                    .y(50)
                    .screenX(mScreenX)
                    .screenY(mScreenY)
                    .build();

            this.mEnemy1 = new EnemyShip
                    .Builder(mContext)
                    .bitmap(R.drawable.enemy_ship)
                    .screenX(mScreenX)
                    .screenY(mScreenY)
                    .build();

            this.mEnemy2 = new EnemyShip
                    .Builder(mContext)
                    .bitmap(R.drawable.enemy_ship)
                    .screenX(mScreenX)
                    .screenY(mScreenY)
                    .build();

            this.mEnemy3 = new EnemyShip
                    .Builder(mContext)
                    .bitmap(R.drawable.enemy_ship)
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
            mExplosionList.add(new Explosion(mEnemy1.getX(), mEnemy1.getY(), 16, mImgExplosionSprite.getWidth()));
            mEnemy1.setX(-mEnemy1.getHitbox().right);

        }

        if (Rect.intersects
                (mPlayerShip.getHitbox(), mEnemy2.getHitbox())) {
            hitDetected = true;
            mExplosionList.add(new Explosion(mEnemy1.getX(), mEnemy1.getY(), 16, mImgExplosionSprite.getWidth()));
            mEnemy2.setX(-mEnemy2.getHitbox().right);
        }

        if (Rect.intersects
                (mPlayerShip.getHitbox(), mEnemy3.getHitbox())) {
            hitDetected = true;
            mExplosionList.add(new Explosion(mEnemy1.getX(), mEnemy1.getY(), 16, mImgExplosionSprite.getWidth()));
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
        mEnemy1.update(playerSpeed / 2);
        mEnemy2.update(playerSpeed / 2);
        mEnemy3.update(playerSpeed / 2);

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

            //draw explosions
            for (Explosion explosion : mExplosionList) {
                explosion.increaseFrame();
                if (explosion.isDone()) {
                    mExplosionList.remove(explosion);
                    Log.d("bbb", "finished explosion:" + explosion);
                } else {
                    //draw the next frame and increment;
                    Log.d("bbb", "frame:" + explosion.getCurrentFrameIndex() + "explosion:" + explosion);
                    Log.d("bbb", "left:" + explosion.getRectToBeDrawn().left + ", top:" + explosion.getRectToBeDrawn().top + ", right:" + explosion.getRectToBeDrawn().right + ", bottom:" + explosion.getRectToBeDrawn().bottom);
                    Log.d("bbb", "left:" + explosion.getRectDestination().left + ", top:" + explosion.getRectDestination().top + ", right:" + explosion.getRectDestination().right + ", bottom:" + explosion.getRectDestination().bottom);
                    mScreenCanvas.drawBitmap(mImgExplosionSprite, explosion.getRectToBeDrawn(), explosion.getRectDestination(), mBackgroundColor);
                }
            }

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

            } else {
                // Show end screen
                mHudColor.setTextSize(hudGameOverSize);
                mHudColor.setTextAlign(Paint.Align.CENTER);
                mScreenCanvas.drawText("Game Over", mScreenX / 2, hudGameOverY, mHudColor);
                mHudColor.setTextSize(hudHighestScoreSize);
                mScreenCanvas.drawText("Personal best:" +
                        mHighScore, mScreenX / 2, hudHighestScoreY, mHudColor);

                mScreenCanvas.drawText("Time:" + mTimeTakenDecimal +
                        "s", mScreenX / 2, hudTimeY, mHudColor);

                mScreenCanvas.drawText("Distance covered:" +
                        mDistanceCovered / 1000 + " Km", mScreenX / 2, hudDistanceCoveredY, mHudColor);

                mHudColor.setTextSize(hudTapToRetrySize);
                mScreenCanvas.drawText("Tap to replay!", mScreenX / 2, hudTapToRetryY, mHudColor);

                if (highScoreAchieved) {
                    mHudColor.setTextSize(hudRecordScoreSize);
                    mScreenCanvas.drawText("NEW RECORD: " + mPlayerScore, mScreenX / 2, hudRecordScoreY, mHudColor);
                } else {
                    mHudColor.setTextSize(hudHighScoreSize);
                    mScreenCanvas.drawText("SCORE: " + mPlayerScore, mScreenX / 2, hudHighScoreY, mHudColor);
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
