package com.graduationproject.zakerly.intro.splash;

import android.util.Log;

import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseUser;
import com.graduationproject.zakerly.MainActivity;
import com.graduationproject.zakerly.R;
import com.graduationproject.zakerly.core.cache.Realm.RealmQueries;
import com.graduationproject.zakerly.core.constants.UserTypes;
import com.graduationproject.zakerly.core.models.Instructor;
import com.graduationproject.zakerly.core.models.User;
import com.graduationproject.zakerly.core.network.firebase.FireBaseAuthenticationClient;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SplashViewModel extends ViewModel {

    private final CompositeDisposable disposables;
    private final SplashRepository splashRepository;
    private final String TAG = "SPLASH_TAG";
    private final NavController navController;

    public SplashViewModel(SplashRepository splashRepository, SplashFragment splashFragment) {
        this.splashRepository = splashRepository;
        this.navController = NavHostFragment.findNavController(splashFragment);
        disposables = new CompositeDisposable();
    }

    public void navigateToNextDestination() {

        Disposable isFirstLaunchDisposable = splashRepository.getIsFirstLaunch()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(/*OnNext*/isFirstLaunch -> {
                            FirebaseUser user = FireBaseAuthenticationClient.getInstance().getCurrentUser();
                            User localUser = null;
                            if (user != null) {
                                localUser = new RealmQueries().getUser(user.getUid());
                            }

                            if (isFirstLaunch) {
                                navController.navigate(SplashFragmentDirections.actionSplashFragmentToOnBoardingFragment());
                                return;
                            } else if (null == user || localUser == null) {
                                navController.navigate(SplashFragmentDirections.actionSplashFragmentToLogInFragment());
                                return;
                            }

                            if (localUser.getType().equals(UserTypes.TYPE_STUDENT)) {
                                navController.navigate(SplashFragmentDirections.actionSplashFragmentToStudentAppNavigation());
                            } else {
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

