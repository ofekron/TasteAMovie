package ofek.ron.tasteamovie;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements MoviesFragment.OnMovieClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState==null)
            getSupportFragmentManager().beginTransaction().replace(R.id.content, ofek.ron.tasteamovie.MoviesFragment.newInstance()).commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMovieClicked(ofek.ron.tasteamovie.MoviesDatabaseHandle.Movie movie) {
        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_right, R.anim.slide_left,R.anim.slide_right, R.anim.slide_left).replace(R.id.content, ofek.ron.tasteamovie.MovieFragment.newInstance(movie)).addToBackStack(null).commit();
    }
}
