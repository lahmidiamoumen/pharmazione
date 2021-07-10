package com.moumen.pharmazione;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.moumen.pharmazione.intro.IntroItem;
import com.moumen.pharmazione.intro.IntroViewPagerAdapter;
import com.moumen.pharmazione.utils.UIUtil;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    private ViewPager screenPager;
    IntroViewPagerAdapter introViewPagerAdapter ;
    TabLayout tabIndicator;
    Button btnNext;
    int position = 0 ;
    Button btnGetStarted;
    Animation btnAnim ;
    TextView tvSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // make the activity on full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
                    );

        UIUtil.setWhiteNavigationBar( findViewById(android.R.id.content), this );

        setContentView(R.layout.activity_intro);

        // ini views
        btnNext = findViewById(R.id.btn_next);
        btnGetStarted = findViewById(R.id.btn_get_started);
        tabIndicator = findViewById(R.id.tab_indicator);
        btnAnim = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.button_animation);
        tvSkip = findViewById(R.id.tv_skip);

        // fill list screen
        final List<IntroItem> mList = new ArrayList<>();
        mList.add(new IntroItem( getResources().getString(R.string.intro_title1),
                                 getResources().getString(R.string.intro_desc1),
                                 R.drawable.ic_information_tab_rafiki));


        mList.add(new IntroItem( getResources().getString(R.string.intro_title2),
                                 getResources().getString(R.string.intro_desc2),
                                 R.drawable.ic_goal));

        mList.add(new IntroItem( getResources().getString(R.string.intro_title4),
                getResources().getString(R.string.intro_desc3),
                R.drawable.ic_intro2));

        mList.add(new IntroItem("",
                "Tous ces avantages avec un accès 100% gratuit.\n",
                R.drawable.ic_free));

        // setup viewpager
        screenPager = findViewById(R.id.screen_viewpager);
        introViewPagerAdapter = new IntroViewPagerAdapter(this,mList);
        screenPager.setAdapter(introViewPagerAdapter);

        // setup tablayout with viewpager
        tabIndicator.setupWithViewPager(screenPager);

        // next button click Listener
        btnNext.setOnClickListener(v -> {

            position = screenPager.getCurrentItem();
//            if(position == 0){
//                btnNext.setText("J'accepte");
//            }
//            else
            btnNext.setText(getResources().getString(R.string.suivant));
            if(position ==2){
                tvSkip.setVisibility(View.VISIBLE);
            }
            if (position < mList.size()) {
                position++;
                screenPager.setCurrentItem(position);
            }

            if (position == mList.size()-1) { // when we reach to the last screen
                loaddLastScreen();
            }

        });

        // tablayout add change listener
        tabIndicator.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab.getPosition() == mList.size()-1) {
                    loaddLastScreen();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Get Started button click listener
        btnGetStarted.setOnClickListener(v -> {

            //open main activity
            Intent mainActivity = new Intent(getApplicationContext(),BottomNavigation.class);
            mainActivity.putExtra("firstTime",true);
            startActivity(mainActivity);
            // also we need to save a boolean value to storage so next time when the user run the app
            // we could know that he is already checked the intro screen activity
            // i'm going to use shared preferences to that process
            savePrefsData();
            finish();

        });

        // skip button click listener
        tvSkip.setOnClickListener(v -> {
            //open main activity
            Intent mainActivity = new Intent(getApplicationContext(),BottomNavigation.class);

            startActivity(mainActivity);
            // also we need to save a boolean value to storage so next time when the user run the app
            // we could know that he is already checked the intro screen activity
            // i'm going to use shared preferences to that process
            savePrefsData();
            finish();
        });
    }

    private void savePrefsData() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("myPrefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isIntroOpnend",true);
        editor.apply();
    }

    // show the GETSTARTED Button and hide the indicator and the next button
    private void loaddLastScreen() {

        btnNext.setVisibility(View.INVISIBLE);
        btnGetStarted.setVisibility(View.VISIBLE);
        tvSkip.setVisibility(View.INVISIBLE);
        tabIndicator.setVisibility(View.INVISIBLE);
        // TODO : ADD an animation the getstarted button
        // setup animation
        btnGetStarted.setAnimation(btnAnim);



    }
}
