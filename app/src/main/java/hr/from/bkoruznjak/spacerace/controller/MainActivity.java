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

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

import hr.from.bkoruznjak.spacerace.R;
import hr.from.bkoruznjak.spacerace.contants.PreferenceKeyConstants;
import hr.from.bkoruznjak.spacerace.databinding.ActivityMainBinding;
import hr.from.bkoruznjak.spacerace.model.adapter.HighScoreAdapter;
import hr.from.bkoruznjak.spacerace.model.firebase.HighScore;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;
    private ArrayList<HighScore> mHighScoreList = new ArrayList<>();
    private HighScoreAdapter mHighScoreAdapter;
    private AlertDialog mAliasDialog;
    private SharedPreferences mPreferences;
    private DecimalFormat formatter = new DecimalFormat("#,###,###");

    private boolean isFirstRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mPreferences = getSharedPreferences(PreferenceKeyConstants.KEY_SHARED_PREFERENCES, MODE_PRIVATE);
        mHighScoreAdapter = new HighScoreAdapter(this, R.layout.item_high_score, mHighScoreList);
        mBinding.listHighscores.setAdapter(mHighScoreAdapter);

        isFirstRun = mPreferences.getBoolean(PreferenceKeyConstants.KEY_FIRST_RUN, true);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        Fabric.with(this, new Crashlytics());
        DatabaseReference highScoreReference = database.getReference("scores");
        Query highScoreReferenceQuery = highScoreReference.orderByChild("score").limitToLast(100);


        ChildEventListener highScoreChildEventListner = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                if (previousChildName == null) {
                    mHighScoreList.clear();
                }
                HighScore tempScore = dataSnapshot.getValue(HighScore.class);
                if (tempScore != null) {
                    mHighScoreList.add(dataSnapshot.getValue(HighScore.class));
                    Collections.sort(mHighScoreList);
                    Collections.reverse(mHighScoreList);
                    mBinding.listHighscores.invalidateViews();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                mBinding.listHighscores.invalidateViews();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                mBinding.listHighscores.invalidateViews();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                mBinding.listHighscores.invalidateViews();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("bbb", "db error:" + databaseError);
            }
        };
        highScoreReferenceQuery.addChildEventListener(highScoreChildEventListner);

        final Typeface newXDigitalTypeface = Typeface.createFromAsset(getAssets(), "fonts/new_x_digital.ttf");

        mBinding.buttonEditCallsign.setTypeface(newXDigitalTypeface);
        mBinding.buttonEditCallsign.setText(mPreferences.getString(PreferenceKeyConstants.KEY_ALIAS, "Callsign"));
        mBinding.buttonEditCallsign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAliasDialog.setCanceledOnTouchOutside(false);
                mAliasDialog.show();
            }
        });

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
        mAliasDialog = aliasDialogBuilder.create();
        mAliasDialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_alias));
        mAliasDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                mAliasDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.aliasColor));
                final EditText userInput = (EditText) mAliasDialog.findViewById(R.id.alias);
                userInput.getBackground().mutate().setColorFilter(getResources().getColor(R.color.aliasColor), PorterDuff.Mode.SRC_ATOP);
                userInput.setTypeface(newXDigitalTypeface);
                //filter only to uppercase and limit to 15
                userInput.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(15)});

                Button confirmButton = mAliasDialog.getButton(AlertDialog.BUTTON_POSITIVE);
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
                            String userAlias = userInput.getText().toString();
                            mPreferences.edit().putString(PreferenceKeyConstants.KEY_ALIAS, userAlias).apply();
                            mBinding.buttonEditCallsign.setText(userAlias);
                            mAliasDialog.dismiss();
                        }
                    }
                });

            }
        });
        //check if user even
        if (isFirstRun) {
            mAliasDialog.setCanceledOnTouchOutside(false);
            mAliasDialog.show();
            mPreferences.edit().putBoolean(PreferenceKeyConstants.KEY_FIRST_RUN, false).apply();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBinding.textPilotHighScore.setText(formatter.format(mPreferences.getLong(PreferenceKeyConstants.KEY_PERSONAL_HIGHSCORE, 0)));
    }
}
