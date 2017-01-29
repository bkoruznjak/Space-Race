package hr.from.bkoruznjak.spacerace.model.firebase;

/**
 * Created by bkoruznjak on 29/01/2017.
 */

public class HighScore {

    private String alias;
    private long score;

    public HighScore() {
    }

    public HighScore(String alias, long score) {
        this.alias = alias;
        this.score = score;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }
}
