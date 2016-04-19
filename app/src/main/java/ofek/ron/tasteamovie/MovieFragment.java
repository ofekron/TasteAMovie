package ofek.ron.tasteamovie;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;


public class MovieFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private ofek.ron.tasteamovie.MoviesDatabaseHandle.Movie movie;
    private ArrayList<WebView> webviews = new ArrayList<>();

    public MovieFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            movie = (ofek.ron.tasteamovie.MoviesDatabaseHandle.Movie) getArguments().getSerializable(ARG_PARAM1);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        for (WebView wv : webviews)
            wv.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        for (WebView wv : webviews)
            wv.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (WebView wv : webviews)
            wv.destroy();
        webviews.clear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_movie, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.movie_details);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView poster = (ImageView) view.findViewById(R.id.poster);
        Toolbar movieBar = (Toolbar) view.findViewById(R.id.moviebar);
        movieBar.setTitle(movie.getTitle());
        Glide.with(getActivity()).load(movie.getPosterPath()).centerCrop().into(poster);
        TextView desc = (TextView) view.findViewById(R.id.description);
        desc.setText(movie.getDescription());
        TextView year = (TextView) view.findViewById(R.id.year);
        year.setText(movie.getYear());
        TextView score = (TextView) view.findViewById(R.id.score);
        score.setText(String.format("%.1f/10", movie.getScore()));
        final LinearLayout trailers = (LinearLayout) view.findViewById(R.id.trailers);
        (new AsyncTask<Void,Void,List<ofek.ron.tasteamovie.MoviesDatabaseHandle.Video>>() {
            @Override
            protected List<ofek.ron.tasteamovie.MoviesDatabaseHandle.Video> doInBackground(Void... params) {
                ofek.ron.tasteamovie.MoviesDatabaseHandle moviesDatabaseHandle = new ofek.ron.tasteamovie.MoviesDatabaseHandle(getContext());

                List<ofek.ron.tasteamovie.MoviesDatabaseHandle.Video> videos = movie.getVideos(moviesDatabaseHandle);
                moviesDatabaseHandle.release();
                return videos;
            }

            @Override
            protected void onPostExecute(List<ofek.ron.tasteamovie.MoviesDatabaseHandle.Video> vs) {
                for ( final ofek.ron.tasteamovie.MoviesDatabaseHandle.Video v : vs) {
                    WebView webView = new WebView(getActivity());
                    webviews.add(webView);
                    embedVideo(trailers, webView, v.getKey());
                }
            }
        }).execute();

    }

    private void embedVideo(final LinearLayout trailers, final WebView wv, String key) {
        final String item = "http://www.youtube.com/embed/"+key+"?autoplay=1";
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        final int w1 = (int) (metrics.widthPixels / metrics.density), h1 = w1 * 3 / 5;
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebChromeClient(chromeClient);
        wv.getSettings().setPluginState(WebSettings.PluginState.ON);
        try {
            wv.loadData(
                    "<html><body><iframe class=\"youtube-player\" type=\"text/html\" width=\""
                            + (w1 - 20)
                            + "\" height=\""
                            + h1
                            + "\" src=\""
                            + item
                            + "\" frameborder=\"0\"></iframe></body></html>",
                    "text/html", "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        trailers.addView(wv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

    }


    private WebChromeClient chromeClient = new WebChromeClient() {

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
            if (view instanceof FrameLayout) {
                FrameLayout frame = (FrameLayout) view;
                if (frame.getFocusedChild() instanceof VideoView) {
                    VideoView video = (VideoView) frame.getFocusedChild();
                    frame.removeView(video);
                    video.start();
                }
            }

        }
    };
    public static MovieFragment newInstance(ofek.ron.tasteamovie.MoviesDatabaseHandle.Movie movie) {
        MovieFragment fragment = new MovieFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, movie);
        fragment.setArguments(args);
        return fragment;
    }


}
