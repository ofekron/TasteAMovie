package ofek.ron.tasteamovie;

import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ofek.ron.tasteamovie.genericdb.DataField;
import ofek.ron.tasteamovie.genericdb.DataTypeClass;
import ofek.ron.tasteamovie.genericdb.Database;
import ofek.ron.tasteamovie.genericdb.Table;

/**
 * Created by Ofek on 13/04/2016.
 */
public class MoviesDatabaseHandle  {
    private static final int DATABASE_VERSION = 1;
    private MoviesDatabase moviesDatabase;
    private Table<Movie> moviesTable;
    public static final String DATABASE_NAME = "MoviesDatabase";
    private Table<Video> videosTable;
    public void clear() {

            moviesTable.clear();
            videosTable.clear();

    }

    public ArrayList<Movie> getAll() {
        return moviesTable.getAll();
    }


    private class MoviesDatabase extends Database {


        public MoviesDatabase(Context context, int version) {
            super(context,DATABASE_NAME , version);
        }
        @Override
        protected void registerTables() {
            moviesTable = registerTable(new Table<Movie>(Movie.class));
            videosTable = registerTable(new Table<Video>(Video.class));
        }
    }



    public MoviesDatabaseHandle(Context context) {
        moviesDatabase = new MoviesDatabase(context,  DATABASE_VERSION);
    }

    public long add(Movie m) {

            if (!moviesTable.insertIfNotExists(m)) return -1;
            for (Video v : m.videos) {
                videosTable.insertIfNotExists(v);
            }
            return moviesTable.size();

    }
    public Table.Handle<Movie> getHandle() {
        final Table.Handle<Movie> handle = moviesTable.getHandle("1");
        return new Table.Handle<Movie>() {
            @Override
            public Movie get(int i) {
                Movie movie = handle.get(i);
                return movie;
            }

            @Override
            public int count() {

                    return handle.count();

            }

            @Override
            public Movie get(int i, Movie toBeFilled) {

                    Movie movie = handle.get(i, toBeFilled);
                    return movie;


            }

            @Override
            public void close() {

                handle.close();
            }

            @Override
            public void invalidate() {

                    handle.invalidate();

            }

            @Override
            public void setListener(Table.HandleListener listener) {
                handle.setListener(listener);
            }
        };
    }
    public void release() {

            if (moviesDatabase!=null) {
                moviesDatabase.close();
                moviesDatabase = null;
            }
            moviesTable = null;
            videosTable = null;


    }
    @DataTypeClass(defineKeys = "FOREIGN KEY(movie_id) REFERENCES Movie(id)")
    public static class Video implements Serializable {
        @DataField
        private int movie_id;
        @DataField(isKey = true)
        private String key;

        public Video() {
        }

        public Video(int movie_id, String key) {
            this.movie_id = movie_id;
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }
    @DataTypeClass
    public static class Movie implements Serializable {

        @DataField(isKey = true)
        private int id;
        @DataField
        private String name;
        @DataField
        private String description;
        @DataField
        private String posterPath;
        @DataField
        private String releaseDate;
        @DataField
        private float score;
        private List<Video> videos = new ArrayList<>(1);
        public Movie(int id, String name, String description,String posterPath,String releaseDate,float score) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.posterPath = posterPath;
            this.releaseDate= releaseDate;
            this.posterPath = posterPath;
            this.score = score;
        }

        public Movie() {
        }


        public String getPosterPath() {
            return "http://image.tmdb.org/t/p/w500" +posterPath;
        }

        public String getDescription() {
            return description;
        }

        public String getTitle() {
            return name;
        }


        public String getYear() {
            return releaseDate;
        }

        public float getScore() {
            return score;
        }

        public void addVideos(Video v) {
            videos.add(v);
        }


        public List<Video> getVideos(MoviesDatabaseHandle handle) {
            return handle.videosTable.getAll("movie_id="+id);
        }
    }
}
