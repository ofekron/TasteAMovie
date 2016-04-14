package ofek.ron.tasteamovie;

import android.util.Log;

/**
 * Created by Ofek on 13/04/2016.
 */
public class Stopper {
    private String tag = "STOPPER";
    private long time;

    public Stopper(String tag) {
        this.tag = tag;
    }
    public Stopper() {

    }

    public void restart() {
        time = System.currentTimeMillis();
    }
    public void printElapsed() {
        Log.d(tag,Long.toString(System.currentTimeMillis()-time));
    }

    public long elapsed() {
        return System.currentTimeMillis()-time;
    }
}
