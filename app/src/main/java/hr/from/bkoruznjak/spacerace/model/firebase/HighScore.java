package hr.from.bkoruznjak.spacerace.model.firebase;

/**
 * Created by bkoruznjak on 29/01/2017.
 */

public class HighScore implements Comparable<HighScore> {

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

    @Override
    public int compareTo(HighScore other) {
        // compareTo should return < 0 if this is supposed to be
        // less than other, > 0 if this is supposed to be greater than
        // other and 0 if they are supposed to be equal
        if (this.score < other.score) {
            return -1;
        } else if (this.score > other.score) {
            return 1;
        } else {
            return 0;
        }
    }

}
