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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import ofek.ron.tasteamovie.genericdb.Table;

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
    private Table.Handle<MoviesDatabaseHandle.Movie> handle;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swiperefresh;
    private MoviesDatabaseHandle moviesDatabaseHandle;
    private boolean isFetching;
    private GridLayoutManager layoutManager;


    public static MoviesFragment newInstance() {
        MoviesFragment fragment = new MoviesFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public MoviesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        fetchReciever = new BroadcastReceiver() {
            public int startIndex;

            @Override
            public void onReceive(Context context, Intent intent) {
                if (adapter != null ) {

                    switch (intent.getIntExtra(FetchMoviesIntentService.ARG_WHAT_IS_IT_ABOUT,0)) {
                        case FetchMoviesIntentService.BROADCAST_FETCHED_A_MOVIE:
                            handle.invalidate();
                            int index = intent.getIntExtra(FetchMoviesIntentService.ARG_LAST_FETCH_INDEX, -1);
                            adapter.notifyItemInserted(index);
                            Log.d("insert",index+"/"+handle.count());
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_movies, container, false);
    }


    private class MovieViewHolder extends RecyclerView.ViewHolder implements Callback {

        private final ImageView image;
        private final Picasso picasso;
        private final RelativeLayout.LayoutParams layoutParams;
        private final TextView index;
        private final TextView indexBig;
        private final View parent;
        private final OvershootInterpolator overshootInterpolator;
        private final TextView name;
        private MoviesDatabaseHandle.Movie holdingMovie = new MoviesDatabaseHandle.Movie();

        public MovieViewHolder(View itemView) {

            super(itemView);
            parent = itemView;

            image = (ImageView) itemView.findViewById(R.id.iv);
            picasso = Picasso.with(getActivity());

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
            overshootInterpolator = new OvershootInterpolator(2);
        }

        public void setMovieHolding(MoviesDatabaseHandle.Movie movie,int i) {
            image.setImageBitmap(null);
            name.setText(movie.getTitle());
            if ( image.getHeight()!=0 && image.getWidth()!=0) {
                layoutParams.height=image.getHeight();
                layoutParams.width=image.getWidth();
                image.setLayoutParams(layoutParams);
                picasso.cancelRequest(image);
                picasso.load(movie.getPosterPath()).fit().centerCrop().noFade().noPlaceholder().into(image, this);
                index.setVisibility(View.VISIBLE);
                indexBig.setVisibility(View.VISIBLE);
                index.setText(Integer.toString(i + 1));
                indexBig.setText(Integer.toString(i+1));

            } else {
                picasso.cancelRequest(image);
                picasso.load(movie.getPosterPath()).into(image,this);
                index.setVisibility(View.INVISIBLE);
                indexBig.setVisibility(View.INVISIBLE);
            }


        }

        public MoviesDatabaseHandle.Movie getMovieObject() {
            return holdingMovie;
        }

        @Override
        public void onSuccess() {
            image.setScaleX(0.75f);
            image.setScaleY(0.75f);
            image.animate().scaleX(1).scaleY(1).setInterpolator(overshootInterpolator).setDuration(250);
        }

        @Override
        public void onError() {

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
        layoutManager = new GridLayoutManager(getActivity(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = createAdapter();
        swiperefresh = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        swiperefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {

                        moviesDatabaseHandle.clear();
                        handle.invalidate();
                        adapter.notifyDataSetChanged();
                        FetchMoviesIntentService.rewind();
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AsyncTask.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        FetchMoviesIntentService.start();
                                    }
                                });
                            }
                        });

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
        final Intent serviceIntent = new Intent(getActivity(), FetchMoviesIntentService.class);
        return new RecyclerView.Adapter() {
            ofek.ron.tasteamovie.Stopper s = new Stopper();
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_movie, parent, false);
                return new MovieViewHolder(v);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
                MovieViewHolder holder1 = (MovieViewHolder) holder;
                holder1.setMovieHolding(handle.get(position, holder1.getMovieObject()), position);


            }

            @Override
            public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
                super.onViewAttachedToWindow(holder);
                final int whenToFetch = getItemCount() - 10;
                int position = holder.getAdapterPosition();
                if (!isFetching && position > whenToFetch && s.elapsed()>1000 ) {
                    s.restart();
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            getContext().startService(serviceIntent);
                        }
                    });
                }
            }

            @Override
            public int getItemCount() {
                return handle.count();
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
        moviesDatabaseHandle = new MoviesDatabaseHandle(getActivity());
        handle = moviesDatabaseHandle.getAll();
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
     * >Communicating picasso Other Fragments</a> for more information.
     */
    public interface OnMovieClickListener {
        // TODO: Update argument type and name
        public void onMovieClicked(MoviesDatabaseHandle.Movie movie);
    }

}
