package ofek.ron.tasteamovie;

import android.app.Application;
import android.content.Intent;

/**
 * Created by Ofek on 13/04/2016.
 */
public class App extends Application {
    public static App context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        if ( ofek.ron.tasteamovie.FetchMoviesIntentService.currentPageIndex()==0 )
            startService(new Intent(this, ofek.ron.tasteamovie.FetchMoviesIntentService.class));
//        OkHttpClient okHttpClient = new OkHttpClient();
//        okHttpClient.networkInterceptors().add(new Interceptor() {
//            @Override
//            public Response intercept(Chain chain) throws IOException {
//                Response originalResponse = chain.proceed(chain.request());
//                return originalResponse.newBuilder().header("Cache-Control", "max-age=" + (60 * 60 * 24 * 365)).build();
//            }
//        });
//        okHttpClient.setCache(new Cache(getCacheDir(), Integer.MAX_VALUE));
//        OkHttpDownloader okHttpDownloader = new OkHttpDownloader(okHttpClient);
//        Picasso picasso = new Picasso.Builder(this).downloader(okHttpDownloader).build();
//        picasso.setIndicatorsEnabled(false);
//        picasso.setLoggingEnabled(false);
//        Picasso.setSingletonInstance(picasso);
    }
}
