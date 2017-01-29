package hr.from.bkoruznjak.spacerace.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by bkoruznjak on 29/01/2017.
 */

public class ScoreTextView extends TextView {

    public ScoreTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ScoreTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScoreTextView(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/new_x_digital.ttf");
            setTypeface(tf);
        }
    }

}