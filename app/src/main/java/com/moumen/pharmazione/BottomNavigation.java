package com.moumen.pharmazione;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.experimental.UseExperimental;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.moumen.pharmazione.persistance.Document;
import com.moumen.pharmazione.persistance.User;
import com.moumen.pharmazione.ui.home.ShowFragment;
import com.moumen.pharmazione.ui.poster.PosterActivity;
import com.moumen.pharmazione.utils.MediaLoader;
import com.moumen.pharmazione.utils.OnActivityListener;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static com.moumen.pharmazione.utils.Util.PATH;
import static com.moumen.pharmazione.utils.Util.PATH_USER;

public class BottomNavigation extends AppCompatActivity implements NavController.OnDestinationChangedListener, OnActivityListener {

    private static final String TAG = "Bootom";
    NavController navController;
    BottomNavigationView navView;
    BadgeDrawable badge = null;
    SharedViewModel sharedViewModel;
    private ListenerRegistration registration;
    public static final String SETTINGS_TITLE = "settings";
    public static final String SETTING_NOTIFICATION = "notification_state";
    public static final String SETTING_ALREADY_SUBSCRIBED = "already_subscribed";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);
        sharedViewModel =  new ViewModelProvider(this).get(SharedViewModel.class);

//        Album.initialize(AlbumConfig.newBuilder(this)
//                .setAlbumLoader(new MediaLoader())
//                .build());

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        subscribeToMessaging();
        if(mAuth.getUid() != null) {
            Task<DocumentSnapshot> task =  FirebaseFirestore.getInstance().collection(PATH_USER).document(mAuth.getUid()).get();
            task.addOnSuccessListener(documentSnapshot -> sharedViewModel.getUserData().setValue(documentSnapshot.toObject(User.class)));
        }
//        sharedViewModel.getBadge().setValue(0);
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//        getWindow().setStatusBarColor(Color.rgb(249,249,249)

            navView = findViewById(R.id.nav_view);

            // Add New Item Activity
//            navView.getMenu()
//               .getItem(0)
//               .setOnMenuItemClickListener(o ->{
//                    Intent intent = new Intent(this, PosterActivity.class);
//                    startActivity(intent);
//                    return true;
//                });
            int firstPage = navView.getMenu().getItem(0).getItemId();
            navView.setSelectedItemId(firstPage);


            // AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);

            navController = Navigation.findNavController(this, R.id.nav_host_fragment);

            navController.addOnDestinationChangedListener(this);
            NavigationUI.setupWithNavController(navView, navController);

            sendRegistrationToServer();

    }

    private void sendRegistrationToServer() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user == null) return;
        onAtachListener();
        Map<String,Object> data = new HashMap<>();
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    // Get new FCM registration token
                    String token = task.getResult();

                    data.put("token",token);
                    db.collection("med-dwa-users")
                            .document(Objects.requireNonNull(mAuth.getUid())).set(data, SetOptions.merge());
                    // Log and toast
                    String msg =  token;
                    Log.d(TAG, msg);
            });
    }





    public EventListener<QuerySnapshot> getEventListener() {
        return (snapshots, error) -> {

            if (error != null) {
                System.err.println("Listen failed: " + error);
                return;
            }
            if(snapshots != null && snapshots.size() > 0){
                int itemId = navView.getMenu().getItem(2).getItemId();
                badge = navView.getOrCreateBadge(itemId);
                badge.setVisible(true);
                badge.setBadgeTextColor(Color.WHITE);
                badge.setNumber(snapshots.size());
                sharedViewModel.getBadge().setValue(snapshots.size());
            }else if (badge != null){
                badge.setVisible(false);
            }
        };
    }


    @Override
    public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
        switch (destination.getId()){
            case R.id.doctorsShowFrag:
            case R.id.specialityFrag:
            case R.id.showFragment:
                navView.setVisibility(View.GONE);
                navView.animate().setListener(new AnimatorListenerAdapter() {
                    boolean isCanceled = false;
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        isCanceled = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (isCanceled) return;
                        navView.setVisibility(View.GONE);
                    }
                });
                return;
            default:
                navView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDetachListener() {
        if(badge == null) return;
        badge.setVisible(false);
        registration.remove();
    }

    @Override
    public void onAtachListener() {
//        FirebaseAuth mAuth = FirebaseAuth.getInstance();
//        String uid = mAuth.getUid();
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        Query query = db.collection("don-besoin-binds")
//                .whereEqualTo("user_id", uid)
//                .whereEqualTo("seen", false);
//        registration = query.addSnapshotListener(getEventListener());
    }

    @Override
    public void onBindNotification(ImageButton v, Context context) {

//        sharedViewModel.getBadge().observe(this, doc ->
//                v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                    @Override
//                    @UseExperimental(markerClass = com.google.android.material.badge.ExperimentalBadgeUtils.class)
//                    public void onGlobalLayout() {
//                        BadgeDrawable badgeDrawable = BadgeDrawable.create(context);
//                        badgeDrawable.setVerticalOffset(20);
//                        badgeDrawable.setHorizontalOffset(15);
//                        badgeDrawable.setBadgeTextColor(Color.WHITE);
//                        badgeDrawable.setVisible(doc > 0);
//                        BadgeUtils.attachBadgeDrawable(badgeDrawable, v, findViewById(R.id.layout));
//                        v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                    }
//                })
//        );
    }

    private void subscribeToMessaging(){
        SharedPreferences prefs = getSharedPreferences(SETTINGS_TITLE, MODE_PRIVATE);

        // Getting value from shared preferences
        boolean isSubscriptionEnable = prefs.getBoolean(SETTING_NOTIFICATION, true);

        // if "isSubscriptionEnable" is true then check whether its already subscribed or not
        if (isSubscriptionEnable){

            boolean alreadySubscribed = prefs.getBoolean(SETTING_ALREADY_SUBSCRIBED, false);
            // if not already subscribed then subscribe to topic and save value to shared preferences
            if (!alreadySubscribed){
                FirebaseMessaging.getInstance().subscribeToTopic("global").addOnSuccessListener(aVoid -> Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_LONG).show());

                SharedPreferences.Editor editor = getSharedPreferences(SETTINGS_TITLE, MODE_PRIVATE).edit();
                editor.putBoolean(SETTING_ALREADY_SUBSCRIBED, true);
                editor.apply();
                // Toast.makeText(this, "Subscribed", Toast.LENGTH_LONG).show();
            } /*else {
                Toast.makeText(this, "Already subscribed", Toast.LENGTH_LONG).show();
            }*/
        }
    }
}
