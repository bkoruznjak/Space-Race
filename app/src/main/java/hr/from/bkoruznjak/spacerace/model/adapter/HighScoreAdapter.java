package hr.from.bkoruznjak.spacerace.model.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import hr.from.bkoruznjak.spacerace.R;
import hr.from.bkoruznjak.spacerace.model.firebase.HighScore;

/**
 * Created by bkoruznjak on 29/01/2017.
 */

public class HighScoreAdapter extends ArrayAdapter<HighScore> {

    public HighScoreAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public HighScoreAdapter(Context context, int resource, List<HighScore> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.item_high_score, null);
        }

        HighScore highScore = getItem(position);

        if (highScore != null) {
            TextView textScore = (TextView) v.findViewById(R.id.text_score);
            TextView textAlias = (TextView) v.findViewById(R.id.text_alias);

            if (textScore != null) {
                textScore.setText(Long.toString(highScore.getScore()));
            }

            if (textAlias != null) {
                textAlias.setText(highScore.getAlias());
            }
        }
        return v;
    }
}