package com.graduationproject.zakerly;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.facebook.CallbackManager;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import com.graduationproject.zakerly.authentication.signIn.SignInFragment;
import com.graduationproject.zakerly.authentication.signIn.SignInFragmentDirections;
import com.graduationproject.zakerly.core.base.BaseActivity;
import com.graduationproject.zakerly.core.cache.DataStoreManger;
import com.graduationproject.zakerly.core.cache.Realm.RealmQueries;
import com.graduationproject.zakerly.core.constants.AuthTypes;
import com.graduationproject.zakerly.core.constants.MeetingAttendeesTypes;
import com.graduationproject.zakerly.core.constants.NotificationType;
import com.graduationproject.zakerly.core.constants.UserTypes;
import com.graduationproject.zakerly.core.models.NotificationData;
import com.graduationproject.zakerly.core.models.User;
import com.graduationproject.zakerly.core.network.firebase.FireBaseAuthenticationClient;
import com.graduationproject.zakerly.core.network.firebase.FirebaseDataBaseClient;
import com.graduationproject.zakerly.core.services.FirebaseNotificationService;
import com.graduationproject.zakerly.databinding.ActivityMainBinding;
import com.graduationproject.zakerly.core.network.GoogleClient;
import com.graduationproject.zakerly.intro.splash.SplashFragmentDirections;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.graduationproject.zakerly.core.constants.BottomNavigationConstants;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;

import es.dmoral.toasty.Toasty;
import io.realm.Realm;
import timber.log.Timber;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MAIN_ACTIVITY";
    private ActivityMainBinding binding;
    private ChipNavigationBar navigationBar;
    private GoogleClient googleClient;
    private CallbackManager callbackManager;
    NavHostFragment navHostFragment;
    NavController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Realm.init(getApplicationContext());
        if (FireBaseAuthenticationClient.getInstance().getCurrentUser() != null) {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Timber.tag(TAG).w(task.getException(), "Fetching FCM registration token failed");
                            return;
                        }
                        String token = task.getResult();
                        FirebaseDataBaseClient.getInstance()
                                .setToken(token)
                                .addOnCompleteListener(command -> Timber.d("onCreate: %s", command.isSuccessful()));
                        DataStoreManger.getInstance(this).setToken(token);
                    });

        }
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        controller = navHostFragment.getNavController();
        initViews();
        initListeners();

        navigationBar.setMenuResource(R.menu.student_bottom_menu);
        setNavigationVisibility(false);
        navigationBar.setItemSelected(R.id.home, true);

        googleClient = GoogleClient.getInstance();
        googleClient.createRequest(this);

        //used for facebook sign in
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().setLoginBehavior(LoginBehavior.WEB_ONLY);

        if (getIntent() != null) {
            NotificationData notificationData = getIntent().getParcelableExtra("NOTIFICATION_DATA");
            String notificationType = getIntent().getStringExtra("NOTIFICATION_TYPE");


            if (notificationData != null) {
                switch (notificationType) {

                    case "VIDEO_MEETING":
                    case "VOICE_MEETING": {
                        navHostFragment.getNavController()
                                .navigate(AuthNavigationDirections.actionToRequestCall(notificationData, MeetingAttendeesTypes.RECEIVER));
                        break;
                    }

                    case "CANCEL": {
                        controller.navigateUp();
                        break;
                    }

                }
            } else {
                JitsiMeetConferenceOptions options = null;
                try {
                    Uri uri = this.getIntent().getData();
                    if (uri == null) return;
                    URL url = new URL(uri.getScheme(), uri.getHost(), uri.getPath());
                    String room = url.getPath();
                    Log.d(TAG, "onCreate: " + room);
                    options = new JitsiMeetConferenceOptions.Builder()
                            .setServerURL(new URL("https://meet.jit.si"))
                            .setRoom(room)
                            .setAudioMuted(false)
                            .setVideoMuted(false)
                            .setAudioOnly(false)
                            .setWelcomePageEnabled(false)
                            .build();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                controller.navigateUp();
                JitsiMeetActivity.launch(this, options);
            }
        }
    }

    private void initViews() {
        navigationBar = binding.bottomNavigation;
    }

    public void setMenu(int menuId) {
        navigationBar.setMenuResource(menuId);
    }

    private void initListeners() {

        navigationBar.setOnItemSelectedListener(id -> {
            FirebaseUser firebaseUser = FireBaseAuthenticationClient.getInstance().getCurrentUser();
            if (firebaseUser != null && controller.getCurrentDestination().getId() != R.id.splashFragment) {
                String uid = firebaseUser.getUid();
                User user = new RealmQueries().getUser(uid);
                switch (id) {
                    case R.id.home: {
                        if (user.getType().equals(UserTypes.TYPE_STUDENT))
                            controller.navigate(R.id.navigate_to_student_home);
                        else {
                            controller.navigate(InstructorAppNavigationDirections.actionToTeacherHomeFragment());
                        }
                        break;
                    }

                    case R.id.profile: {
                        if (user.getType().equals(UserTypes.TYPE_STUDENT))
                            controller.navigate(R.id.navigate_to_student_profile);
                        else {
                            controller.navigate(R.id.action_to_profile_instructor);
                        }
                        break;
                    }

                    case R.id.favorite: {

                        if (user.getType().equals(UserTypes.TYPE_STUDENT))
                            controller.navigate(R.id.navigate_to_favorite);
                        break;
                    }

                    case R.id.settings: {
                        controller.navigate(R.id.navigate_to_settings);
                        break;
                    }

                    case R.id.notification: {
                        controller.navigate(R.id.navigate_to_notifications);
                        break;
                    }

                    case R.id.seclude: {
                        controller.navigate(R.id.action_global_scheduleFragment);
                        break;
                    }
                }
            }
        });
    }

    public void setNavigationVisibility(boolean visible) {
        navigationBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setSelectedPage(int page) {

        switch (page) {
            case BottomNavigationConstants.HOME_PAGE:
                navigationBar.setItemSelected(R.id.home, true);
                break;

            case BottomNavigationConstants.FAVORITE_PAGE:
                navigationBar.setItemSelected(R.id.favorite, true);
                break;
            case BottomNavigationConstants.ACCOUNT_PAGE:
                navigationBar.setItemSelected(R.id.profile, true);

                break;
            case BottomNavigationConstants.NOTIFICATION_PAGE:
                navigationBar.setItemSelected(R.id.notification, true);
            case BottomNavigationConstants.SETTINGS_PAGE:
                navigationBar.setItemSelected(R.id.settings, true);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // this line used in  facebook signIn
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GoogleClient.RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String fName = account.getDisplayName();
                String lName = account.getFamilyName();
                String email = account.getEmail();

                FirebaseDataBaseClient.getInstance().getUser(email).addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if ((snapshot.getChildrenCount() > 0)) {
                            Toasty.success(MainActivity.this, R.string.user_signin_success).show();
                            RealmQueries queries = new RealmQueries();
                            GoogleClient.getInstance().firebaseAuthWithGoogle(account.getIdToken()).
                                    addOnSuccessListener(authResult -> {
                                        FirebaseDataBaseClient.getInstance().doWithUserObject(email, student -> {
                                            FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
                                                String uid = authResult.getUser().getUid();
                                                student.getUser().setUID(uid);
                                                FirebaseDataBaseClient.getInstance().addStudent(student);
                                                queries.addStudent(student);
                                                setMenu(R.menu.student_bottom_menu);
                                                controller.navigate(R.id.action_signInFragment_to_student_app_navigation);
                                            });
                                            return true;
                                        }, (instructor -> {
                                            FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
                                                String uid = authResult.getUser().getUid();
                                                instructor.getUser().setUID(uid);
                                                FirebaseDataBaseClient.getInstance().addInstructor(instructor);
                                                queries.addTeacher(instructor);
                                                setMenu(R.menu.instructor_bottom_menu);
                                                controller.navigate(R.id.action_instructor_app_navigation);
                                            });
                                            return true;
                                        }), s -> {
                                            Log.d("Sing in error", "signIn: " + s);
                                            return true;
                                        });
                                    })
                                    .addOnFailureListener(e -> Toasty.info(MainActivity.this, e.getLocalizedMessage()).show());
                        } else {
                            controller.navigate(SignInFragmentDirections.actionSignInFragmentToSignUpFragment(AuthTypes.AUTH_G_MAIL, account.getIdToken(), fName, lName, email));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d(TAG, "onCancelled: " + "CANCELED");
                    }
                });

            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public GoogleClient getGoogleClient() {
        return googleClient;
    }

    public CallbackManager getFacebookCallbackManager() {
        return callbackManager;
    }
}