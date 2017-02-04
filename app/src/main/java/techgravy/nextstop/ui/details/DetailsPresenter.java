package techgravy.nextstop.ui.details;

import com.github.aurae.retrofit2.LoganSquareConverterFactory;
import com.github.simonpercic.oklog3.OkLogInterceptor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import techgravy.nextstop.BuildConfig;
import techgravy.nextstop.ui.details.model.AutoJson_POI;
import techgravy.nextstop.ui.details.model.POI;
import techgravy.nextstop.ui.details.model.WeatherModel;
import techgravy.nextstop.ui.home.model.Places;
import techgravy.nextstop.utils.FirebaseJSONUtil;
import timber.log.Timber;

/**
 * Created by aditlal on 12/01/17 - 12.
 */

class DetailsPresenter implements DetailsContract.Presenter {
    private CompositeDisposable mCompositeDisposable;
    private DetailsContract.View mView;
    public Retrofit retrofit;
    private DatabaseReference mDatabaseReference;


    @Inject
    DetailsPresenter(Retrofit retrofit, DetailsContract.View view) {
        mView = view;
        mCompositeDisposable = new CompositeDisposable();
        this.retrofit = retrofit;
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

    }

    @Override
    public void computePlaceTags(Places places) {
        Observable.just(places)
                .subscribeOn(Schedulers.computation())
                .map(places1 -> {
                    List<String> returnTags = new ArrayList<>();
                    if (places.family().equals("true"))
                        returnTags.add("Family");
                    if (places.entertainment().equals("true"))
                        returnTags.add("Entertainment");
                    if (places.sun().equals("true"))
                        returnTags.add("Sun");
                    if (places.adventure().equals("true"))
                        returnTags.add("Adventure");

                    if (places.landmarks().equals("true"))
                        returnTags.add("Landmarks");
                    if (places.sports().equals("true"))
                        returnTags.add("Water Sports");
                    if (places.nightlife().equals("true"))
                        returnTags.add("Night Life");
                    if (places.food().equals("true"))
                        returnTags.add("Food");
                    if (places.shopping().equals("true"))
                        returnTags.add("Shopping");
                    if (places.luxury().equals("true"))
                        returnTags.add("Luxury");
                    if (places.cityscape().equals("true"))
                        returnTags.add("Cityscape");
                    if (places.history().equals("true"))
                        returnTags.add("History");
                    if (places.picturesque().equals("true"))
                        returnTags.add("Picturesque");
                    if (places.beaches().equals("true"))
                        returnTags.add("Beaches");
                    if (places.island().equals("true"))
                        returnTags.add("Island");
                    if (places.romantic().equals("true"))
                        returnTags.add("Romantic");
                    if (places.art().equals("true"))
                        returnTags.add("Art");
                    Collections.sort(returnTags);
                    return returnTags;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<String>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(List<String> value) {
                        mView.loadPlaceTags(value);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public void getWeather(String cityName) {
        OkLogInterceptor okLogInterceptor =
                OkLogInterceptor.builder()
                        .setLogInterceptor(url -> {
                            Timber.tag("API_TAG").d(url);
                            return true;
                        }).withAllLogData().build();
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.addInterceptor(okLogInterceptor);
        Retrofit weatherRetrofit = new Retrofit.Builder()
                .addConverterFactory(LoganSquareConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BuildConfig.WEATHER_URL)
                .client(client.build())
                .build();
        weatherRetrofit.create(WeatherApi.class)
                .getTodaysForecast(cityName, BuildConfig.WEATHER_API_KEY, "metric")
                .subscribeOn(Schedulers.io())
                .map(weatherResponse ->
                        WeatherModel.builder()
                                .weatherID(weatherResponse.getList().get(0).getWeather().get(0).getId())
                                .icon(weatherResponse.getList().get(0).getWeather().get(0).getIcon())
                                .temp(weatherResponse.getList().get(0).getMain().getTemp())
                                .build())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<WeatherModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(WeatherModel value) {
                        mView.loadWeather(value);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }


    @Override
    public void getPOI(String cityName) {
        Query query = mDatabaseReference.child("place_details").child(cityName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    try {
                        List<POI> results = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            POI poi = FirebaseJSONUtil.deserialize(snapshot, AutoJson_POI.class);
                            Timber.tag("POI").d(poi.place());
                            results.add(poi);
                        }
                        mView.loadSearchResults(results);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
     /*   retrofit.create(GoogleApiInterface.class).textSearch("Attractions in "+ cityName)
                .subscribeOn(Schedulers.io())
                .map(SearchResponse::getResultsList)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<SearchResults>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(List<SearchResults> results) {
                        for (SearchResults result : results)
                            Timber.tag("SearchResults").d("result = " + result.toString());
                        mView.loadSearchResults(results);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Timber.tag("SearchResults").e(e, e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Timber.tag("SearchResults").d("onComplete Called");
                    }
                });*/

    }


    @Override
    public void onStop() {
        mCompositeDisposable.dispose();
    }
}