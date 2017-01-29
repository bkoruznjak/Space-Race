package hr.from.bkoruznjak.spacerace;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;

import hr.from.bkoruznjak.spacerace.view.SRView;

public class GameActivity extends AppCompatActivity {

    SRView mGameSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get a Display object to access screen details
        Display display = getWindowManager().getDefaultDisplay();
        // Load the resolution into a Point object
        Point size = new Point();
        display.getSize(size);

        // Also passing in the screen resolution to the constructor
        mGameSurfaceView = new SRView(this, size.x, size.y);
        setContentView(mGameSurfaceView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGameSurfaceView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGameSurfaceView.resume();
    }
}
