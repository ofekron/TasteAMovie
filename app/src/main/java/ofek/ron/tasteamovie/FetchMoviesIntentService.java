package ofek.ron.tasteamovie;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import java.util.List;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.Video;
import info.movito.themoviedbapi.model.core.MovieResultsPage;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class FetchMoviesIntentService extends IntentService {

    public static final String ACTION_FETCH_PROGRESS_BROADCAST = "ACTION_FETCH_PROGRESS_BROADCAST";
    public static final String ARG_LAST_FETCH_INDEX = "LAST_FETCH_INDEX";
    public static final String ARG_LAST_FETCH_PAGE_INDEX = ARG_LAST_FETCH_INDEX;
    private static final String PAGE_IM_AT = "page_im_at";
    public static final String ARG_WHAT_IS_IT_ABOUT = "ARG_WHAT_IS_IT_ABOUT";
    public static final int BROADCAST_FETCH_STARTED = 0;
    public static final int BROADCAST_FETCHED_A_MOVIE = 1;
    public static final int BROADCAST_FETCH_DONE = 2;
    private LocalBroadcastManager localBroadcastManager;



    public FetchMoviesIntentService() {
        super("FetchMoviesIntentService");
    }
    public static void rewind() {
        SharedPreferences dsp = PreferenceManager.getDefaultSharedPreferences(App.context);
        dsp.edit().putInt(PAGE_IM_AT,0).commit();
    }
    public static int currentPageIndex() {
        SharedPreferences dsp = PreferenceManager.getDefaultSharedPreferences(App.context);
        return dsp.getInt(PAGE_IM_AT, 0);
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        ofek.ron.tasteamovie.MoviesDatabaseHandle moviesDatabaseHandle = new ofek.ron.tasteamovie.MoviesDatabaseHandle(this);
        SharedPreferences dsp = PreferenceManager.getDefaultSharedPreferences(this);
        int pageIndex = dsp.getInt(PAGE_IM_AT, 0);
        send(BROADCAST_FETCH_STARTED, pageIndex);
        try {

            TmdbMovies movies = new TmdbApi("99550c297a654a006bde5a374d232d72").getMovies();
            if (movies == null) return;
            MovieResultsPage page = movies.getPopularMovies("en", pageIndex);
            if (page == null) return;
            dsp.edit().putInt(PAGE_IM_AT, pageIndex + 1).commit();
            int totalPages = page.getTotalPages();
            int totalResults = page.getTotalResults();

            page = movies.getPopularMovies("en", pageIndex);
            List<MovieDb> results = page.getResults();
            for (MovieDb m : results) {
                ofek.ron.tasteamovie.MoviesDatabaseHandle.Movie movie = new ofek.ron.tasteamovie.MoviesDatabaseHandle.Movie(m.getId(), m.getOriginalTitle(), m.getOverview(), m.getPosterPath(), m.getReleaseDate(), m.getVoteAverage());
                List<Video> en = movies.getVideos(m.getId(), "en");
                for (Video v : en) {
                    if (v.getSite().equalsIgnoreCase("youtube"))
                        movie.addVideos(new ofek.ron.tasteamovie.MoviesDatabaseHandle.Video(m.getId(), v.getKey()));
                }
                long size = moviesDatabaseHandle.add(movie);
                if (size!=-1) send(BROADCAST_FETCHED_A_MOVIE, (int) (size - 1));
            }
            send(BROADCAST_FETCH_DONE, pageIndex);
        } catch (Throwable t) {
            t.printStackTrace();
            send(BROADCAST_FETCH_DONE, -1);
        } finally {
            moviesDatabaseHandle.release();
        }
    }




    private void send(int what, int extra) {
        Intent fetchBroadcast = new Intent(ACTION_FETCH_PROGRESS_BROADCAST);
        fetchBroadcast.putExtra(ARG_WHAT_IS_IT_ABOUT, what);
        fetchBroadcast.putExtra(ARG_LAST_FETCH_INDEX, extra);
        localBroadcastManager.sendBroadcast(fetchBroadcast);
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static void start() {
        App.context.startService(new Intent(App.context, FetchMoviesIntentService.class));
    }
}
