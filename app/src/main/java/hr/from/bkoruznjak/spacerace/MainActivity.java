package hr.from.bkoruznjak.spacerace;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import hr.from.bkoruznjak.spacerace.model.firebase.HighScore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference highScoreReference = database.getReference("scores");
        Query highScoreReferenceQuery = highScoreReference.orderByChild("scores").limitToLast(2);
        highScoreReferenceQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    HighScore score = postSnapshot.getValue(HighScore.class);
                    Log.d("bbb", " values is " + score.getAlias() + " " + score.getScore());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

        AlertDialog.Builder aliasDialogBuilder = new AlertDialog.Builder(this);
        aliasDialogBuilder.setView(R.layout.dialog_alias);

        aliasDialogBuilder.setPositiveButton("Commit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                EditText userInput = (EditText) ((AlertDialog) dialog).findViewById(R.id.alias);
                Log.d("bbb", "confirmed alias:" + userInput.getText().toString());
            }
        });
        final AlertDialog aliasDialog = aliasDialogBuilder.create();
        aliasDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_alias));
        aliasDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                aliasDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.aliasColor));
                EditText userInput = (EditText) aliasDialog.findViewById(R.id.alias);
                userInput.getBackground().mutate().setColorFilter(getResources().getColor(R.color.aliasColor), PorterDuff.Mode.SRC_ATOP);

            }
        });
        aliasDialog.setCanceledOnTouchOutside(false);
        aliasDialog.show();


    }
}
