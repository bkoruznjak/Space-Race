package hr.from.bkoruznjak.spacerace;

import android.app.Activity;
import android.os.Bundle;

import hr.from.bkoruznjak.spacerace.view.SRView;

public class GameActivity extends Activity {

    SRView mGameSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGameSurfaceView = new SRView(this);
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
