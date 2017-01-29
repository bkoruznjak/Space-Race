package hr.from.bkoruznjak.spacerace.controller;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import hr.from.bkoruznjak.spacerace.R;
import hr.from.bkoruznjak.spacerace.databinding.ActivityMainBinding;
import hr.from.bkoruznjak.spacerace.model.adapter.HighScoreAdapter;
import hr.from.bkoruznjak.spacerace.model.firebase.HighScore;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;
    private ArrayList<HighScore> mHighScoreList = new ArrayList<>();
    private HighScoreAdapter mHighScoreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mHighScoreAdapter = new HighScoreAdapter(this, R.layout.item_high_score, mHighScoreList);
        mBinding.listHighscores.setAdapter(mHighScoreAdapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference highScoreReference = database.getReference("scores");
        Query highScoreReferenceQuery = highScoreReference.orderByChild("scores").limitToLast(10);
        highScoreReferenceQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    HighScore score = postSnapshot.getValue(HighScore.class);
                    Log.d("bbb", "dodajem na listu");
                    mHighScoreList.add(score);
                    mBinding.listHighscores.invalidateViews();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Prepare to load fastest time
        SharedPreferences prefs;
        SharedPreferences.Editor editor;
        prefs = getSharedPreferences("HiScores", MODE_PRIVATE);
        final Typeface newXDigitalTypeface = Typeface.createFromAsset(getAssets(), "fonts/new_x_digital.ttf");
        mBinding.buttonStartGame.setTypeface(newXDigitalTypeface);
        mBinding.buttonStartGame.setOnClickListener(new View.OnClickListener() {
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
                Log.d("bbb", "click control");
            }
        });
        final AlertDialog aliasDialog = aliasDialogBuilder.create();
        aliasDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_alias));
        aliasDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                aliasDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.aliasColor));
                final EditText userInput = (EditText) aliasDialog.findViewById(R.id.alias);
                userInput.getBackground().mutate().setColorFilter(getResources().getColor(R.color.aliasColor), PorterDuff.Mode.SRC_ATOP);
                userInput.setTypeface(newXDigitalTypeface);
                //filter only to uppercase and limit to 15
                userInput.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(15)});

                Button confirmButton = aliasDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                confirmButton.setTypeface(newXDigitalTypeface);
                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // User clicked OK button
                        if (userInput.getText().toString().isEmpty()) {
                            Toast.makeText(getApplicationContext(), "You left an empty callsign meathead", Toast.LENGTH_SHORT).show();
                        } else if (userInput.getText().toString().length() < 3) {
                            Toast.makeText(getApplicationContext(), "Your Nickname is too short", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("bbb", "confirmed alias:" + userInput.getText().toString());
                            aliasDialog.dismiss();
                        }

                    }
                });

            }
        });
        aliasDialog.setCanceledOnTouchOutside(false);
        aliasDialog.show();
    }
}
