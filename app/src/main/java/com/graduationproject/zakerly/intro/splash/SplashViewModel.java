package com.graduationproject.zakerly.intro.splash;

import android.util.Log;

import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.graduationproject.zakerly.MainActivity;
import com.graduationproject.zakerly.R;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SplashViewModel extends ViewModel {

    private CompositeDisposable disposables;
    private SplashRepository splashRepository;
    private String TAG = "SPLASH_TAG";
    private NavController navController;

    public SplashViewModel(SplashRepository splashRepository, SplashFragment splashFragment) {
        this.splashRepository = splashRepository;
        this.navController = NavHostFragment.findNavController(splashFragment);
        disposables  =new CompositeDisposable();
    }

    public void navigateToNextDestination() {

     Disposable isFirstLaunchDisposable =  splashRepository.getIsFirstLaunch()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(/*OnNext*/isFirstLaunch -> {

                            if (isFirstLaunch) {
                                navController.navigate(SplashFragmentDirections.actionSplashFragmentToOnBoardingFragment());
                            } else {
                                navController.navigate(SplashFragmentDirections.actionSplashFragmentToLogInFragment());
                            }
                        },
                        /*OnError*/throwable -> Log.d(TAG, throwable.getLocalizedMessage()));

        disposables.add(isFirstLaunchDisposable);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.dispose();
    }
}

