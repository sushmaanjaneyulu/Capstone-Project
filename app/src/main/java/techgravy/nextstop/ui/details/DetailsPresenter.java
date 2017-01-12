package techgravy.nextstop.ui.details;

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
import retrofit2.Retrofit;
import techgravy.nextstop.ui.home.model.Places;

/**
 * Created by aditlal on 12/01/17 - 12.
 */

public class DetailsPresenter implements DetailsContract.Presenter {
    CompositeDisposable mCompositeDisposable;
    private DetailsContract.View mView;
    public Retrofit retrofit;

    @Inject
    public DetailsPresenter(Retrofit retrofit, DetailsContract.View view) {
        mView = view;
        mCompositeDisposable = new CompositeDisposable();
        this.retrofit = retrofit;
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
    public void onStop() {
        mCompositeDisposable.dispose();
    }


}
