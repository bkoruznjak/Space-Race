package hr.from.bkoruznjak.spacerace;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button newGameButton = (Button) findViewById(R.id.button_start_game);

        // Prepare to load fastest time
        SharedPreferences prefs;
        SharedPreferences.Editor editor;
        prefs = getSharedPreferences("HiScores", MODE_PRIVATE);

        // Load fastest time
        // if not available our high score = 1000000
        long fastestTime = prefs.getLong("fastestTime", 1000000);

        TextView textHighScore = (TextView) findViewById(R.id.text_high_score);
        textHighScore.setText(textHighScore.getText().toString().concat(Long.toString(fastestTime)));

        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startGameIntent = new Intent(MainActivity.this, GameActivity.class);
                startActivity(startGameIntent);
            }
        });
    }
}
