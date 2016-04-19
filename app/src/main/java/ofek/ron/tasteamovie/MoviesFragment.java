package ofek.ron.tasteamovie;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnMovieClickListener} interface
 * to handle interaction events.
 * Use the {@link MoviesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MoviesFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnMovieClickListener mListener;
    private BroadcastReceiver fetchReciever;
    private LocalBroadcastManager localBroadcastManager;
    private RecyclerView.Adapter adapter;
    //private Table.Handle<MoviesDatabaseHandle.Movie> handle;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swiperefresh;
    private MoviesDatabaseHandle moviesDatabaseHandle;
    private boolean isFetching;
    private GridLayoutManager layoutManager;
    private RequestManager glide;
    private ArrayList<MoviesDatabaseHandle.Movie> list;
    private int currentListSize;


    public static MoviesFragment newInstance() {
        MoviesFragment fragment = new MoviesFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public MoviesFragment() {
        // Required empty public constructor
    }
    Runnable notifyOnFetchedOne = new Runnable() {

        @Override
        public void run() {

        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        glide = Glide.with(getActivity());
        moviesDatabaseHandle = new MoviesDatabaseHandle(getActivity());
        new UpdateListAsyncTask().execute();
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        fetchReciever = new BroadcastReceiver() {
            public int startIndex;

            @Override
            public void onReceive(Context context, final Intent intent) {
                if (adapter != null ) {

                    switch (intent.getIntExtra(FetchMoviesIntentService.ARG_WHAT_IS_IT_ABOUT,0)) {
                        case FetchMoviesIntentService.BROADCAST_FETCHED_A_MOVIE:
                            int intExtra = intent.getIntExtra(FetchMoviesIntentService.ARG_LAST_FETCH_INDEX, -1);
                            new UpdateListAsyncTask().execute(intExtra);
                            break;
                        case FetchMoviesIntentService.BROADCAST_FETCH_DONE:
                            swiperefresh.setRefreshing(false);
                            isFetching = false;
                            int extra = intent.getIntExtra(FetchMoviesIntentService.ARG_LAST_FETCH_INDEX, -1);
                            if (extra==-1)
                                Toast.makeText(context,context.getString(R.string.couldnt_fetch),Toast.LENGTH_SHORT).show();
                            break;
                        case FetchMoviesIntentService.BROADCAST_FETCH_STARTED:
                            swiperefresh.setRefreshing(true);
                            isFetching = true;
                            break;
                    }

                }
            }
        };
    }

    private class UpdateListAsyncTask extends  AsyncTask<Integer, Integer, Integer> {
        private ArrayList<MoviesDatabaseHandle.Movie> locallist;

        @Override
        protected Integer doInBackground(Integer... params) {
            locallist = moviesDatabaseHandle.getAll();
            return params==null || params.length==0 ? null : params[0];
        }

        @Override
        protected void onPostExecute(Integer onPost) {
            super.onPostExecute(onPost);
            list = locallist;
            currentListSize = list.size();
            if (onPost!=null) adapter.notifyItemInserted(onPost);
            else adapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_movies, container, false);
    }


    private class MovieViewHolder extends RecyclerView.ViewHolder  {

        private final ImageView image;
        private final RelativeLayout.LayoutParams layoutParams;
        private final TextView index;
        private final TextView indexBig;
        private final View parent;
        private final TextView name;
        private MoviesDatabaseHandle.Movie holdingMovie = new MoviesDatabaseHandle.Movie();
        public MovieViewHolder(View itemView) {

            super(itemView);
            parent = itemView;

            image = (ImageView) itemView.findViewById(R.id.iv);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null && holdingMovie != null) {
                        mListener.onMovieClicked(holdingMovie);
                    }
                }
            });
            index = (TextView) itemView.findViewById(R.id.textView);
            indexBig = (TextView) itemView.findViewById(R.id.textViewBehind);
            name = (TextView) itemView.findViewById(R.id.name);
            layoutParams = new RelativeLayout.LayoutParams(image.getWidth(), image.getHeight());

        }

        public void setMovieHolding(MoviesDatabaseHandle.Movie movie,int i) {
            image.setImageBitmap(null);
            holdingMovie = movie;
            name.setText(movie.getTitle());
            glide.load(movie.getPosterPath()).centerCrop().into(image);
            index.setText(Integer.toString(i + 1));
            indexBig.setText(Integer.toString(i + 1));
        }

        public MoviesDatabaseHandle.Movie getMovieObject() {
            return holdingMovie;
        }


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.pop_movies);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        layoutManager = new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.screen_width_units));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(50);
        adapter = createAdapter();
        final Intent serviceIntent = new Intent(getActivity(), FetchMoviesIntentService.class);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            /**
             * Callback method to be invoked when RecyclerView's scroll state changes.
             *
             * @param recyclerView The RecyclerView whose scroll state has changed.
             * @param newState     The updated scroll state. One of {@link #SCROLL_STATE_IDLE},
             *                     {@link #SCROLL_STATE_DRAGGING} or {@link #SCROLL_STATE_SETTLING}.
             */
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (RecyclerView.SCROLL_STATE_IDLE != newState) return;
                final int whenToFetch = currentListSize - 10;
                int position = layoutManager.findLastVisibleItemPosition();
                if (!isFetching && position > whenToFetch) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            getContext().startService(serviceIntent);
                        }
                    });
                }
            }

            /**
             * Callback method to be invoked when the RecyclerView has been scrolled. This will be
             * called after the scroll has completed.
             * <p/>
             * This callback will also be called if visible item range changes after a layout
             * calculation. In that case, dx and dy will be 0.
             *
             * @param recyclerView The RecyclerView which scrolled.
             * @param dx           The amount of horizontal scroll.
             * @param dy           The amount of vertical scroll.
             */
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        swiperefresh = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swiperefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        moviesDatabaseHandle.clear();
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                FetchMoviesIntentService.rewind();
                                FetchMoviesIntentService.start();
                            }
                        });
                        new UpdateListAsyncTask().execute();


                    }
                }
        );
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);
//        AppCompatActivity activity = (AppCompatActivity) getActivity();
//        activity.getSupportActionBar().setHomeButtonEnabled(false);
//        activity.getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    private RecyclerView.Adapter createAdapter() {

        return new RecyclerView.Adapter() {
            ofek.ron.tasteamovie.Stopper s = new Stopper("onViewAttachedToWindow");
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_movie, parent, false);
                MovieViewHolder movieViewHolder = new MovieViewHolder(v);

                return movieViewHolder;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
                MovieViewHolder holder1 = (MovieViewHolder) holder;
                holder1.setMovieHolding(list.get(position), position);
            }

            @Override
            public int getItemCount() {
                return currentListSize;
            }
        };
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (OnMovieClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnMovieClickListener");
        }

        //handle = moviesDatabaseHandle.getHandle();

    }
    @Override
    public void onResume() {
        super.onResume();
        localBroadcastManager.registerReceiver(fetchReciever, new IntentFilter(FetchMoviesIntentService.ACTION_FETCH_PROGRESS_BROADCAST));

    }

    @Override
    public void onPause() {
        super.onPause();
        localBroadcastManager.unregisterReceiver(fetchReciever);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        moviesDatabaseHandle.release();
        moviesDatabaseHandle = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating glide Other Fragments</a> for more information.
     */
    public interface OnMovieClickListener {
        // TODO: Update argument type and name
        public void onMovieClicked(MoviesDatabaseHandle.Movie movie);
    }

}
