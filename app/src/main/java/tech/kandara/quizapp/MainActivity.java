package tech.kandara.quizapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import tech.kandara.quizapp.Fragments.BlankFragment;
import tech.kandara.quizapp.Fragments.FAQFragment;
import tech.kandara.quizapp.Fragments.Home1Fragment;
import tech.kandara.quizapp.Fragments.LeaderBoardFragment;
import tech.kandara.quizapp.Fragments.SettingsFragment;
import tech.kandara.quizapp.Fragments.TransferFragment;
import tech.kandara.quizapp.Library.utils.MySettings;

public class MainActivity extends AppCompatActivity {


    private TabLayout tabLayout;
    private ViewPager viewPager;
    private int[] tabIcons = {
            R.drawable.ic_home_black_24dp,
            R.drawable.ic_error_black_24dp,
            R.drawable.ic_notification,
            R.drawable.ic_transfer1,
            R.drawable.ic_leaderboard,
            R.drawable.ic_settings_black_24dp,
    };
    MySettings settings;
    private ViewPagerAdapter adapter;
    private InterstitialAd mInterstitialAd;


    private AdView mAdView;
    //int PICK_IMAGE=343;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("9339BF107335CB968B9CFC2FE448DC84").build();
        mAdView.loadAd(adRequest);

        initialize();
    }

    private void initialize() {
        /*ParseInstallation installation=ParseInstallation.getCurrentInstallation();
        installation.put(PC.INSTALLATION_USER_ID, ParseUser.getCurrentUser().getObjectId());
        installation.saveInBackground();*/

        settings = new MySettings(MainActivity.this);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        viewPager.setOffscreenPageLimit(5);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(tabLayout.getWindowToken(), 0);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        mInterstitialAd = new InterstitialAd(MainActivity.this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                loadInterstitialAd();
                super.onAdClosed();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
            }
        });
        loadInterstitialAd();
        setupTabIcons();
    }


    public void navigateLogin(){
        Intent intent = new Intent(MainActivity.this, EntryScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        this.finish();
    }


    public void loadInterstitialAd() {
        mInterstitialAd.loadAd(new AdRequest.Builder()
                .addTestDevice("9339BF107335CB968B9CFC2FE448DC84").build());
    }





    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[4]);
        tabLayout.getTabAt(2).setIcon(tabIcons[5]);
        //tabLayout.getTabAt(3).setIcon(tabIcons[5]);
        //tabLayout.getTabAt(4).setIcon(tabIcons[5]);
//        tabLayout.getTabAt(5).setIcon(tabIcons[5]);
    }



    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new Home1Fragment(), "Home");
        //adapter.addFrag(new FAQFragment(), "FAQ");
        //adapter.addFrag(new BlankFragment(), "Notifications");
       // adapter.addFrag(new TransferFragment(), "Transfers");
        adapter.addFrag(new LeaderBoardFragment(), "LeaderBoard");
        adapter.addFrag(new SettingsFragment(), "Settings");
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = adapter.getItem(viewPager.getCurrentItem());
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }


    @Override
    public void onBackPressed() {

        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
        super.onBackPressed();
    }
}
