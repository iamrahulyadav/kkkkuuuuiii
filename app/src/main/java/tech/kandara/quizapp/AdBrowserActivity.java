package tech.kandara.quizapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import tech.kandara.quizapp.Fragments.Home1Fragment;
import tech.kandara.quizapp.Library.ObservableWebView;

public class AdBrowserActivity extends AppCompatActivity {

    String url = "https://b.querylead.com/?aff=m&id=5f6430dcf5&source=kuizapp";
    ObservableWebView webView;
    int BROWSING_TIME=10;

    int ENERGY_REWARD=5;
    TextView tvreward;
    int previousEnergy;

    boolean scrolled = false;
    CountDownTimer countDownTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_browser);
        previousEnergy = ParseUser.getCurrentUser().getInt(PC.KEY_USER_AVAILABLE_ENERGY);
        webView = (ObservableWebView) findViewById(R.id.webView1);
        tvreward = (TextView) findViewById(R.id.tvReward);
        webView.getSettings().setJavaScriptEnabled(true);

        final ProgressDialog progressDialog = ProgressDialog.show(AdBrowserActivity.this, "Please Wait", "Processing");
        progressDialog.show();
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(PC.KEY_VERSION_OBJECT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                progressDialog.dismiss();
                if (e == null) {
                    if (objects.size() != 0) {
                        ParseObject currentVersion=objects.get(0);
                        ArrayList<String> urls= (ArrayList<String>) currentVersion.get(PC.KEY_VERSION_URLS);
                        int index=new Random().nextInt(urls.size());
                        url=urls.get(index);
                        BROWSING_TIME=currentVersion.getInt(PC.KEY_VERSION_BROWSING_TIME);
                        ENERGY_REWARD=currentVersion.getInt(PC.KEY_VERSION_ENERGY_REWARD);

                        currentVersion.increment(PC.KEY_VERSION_AD_SERVED);
                        currentVersion.saveInBackground();
                    }
                }

                webView.loadUrl(url);

                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return true;
                    }
                });



                webView.setOnScrollChangedCallback(new ObservableWebView.OnScrollChangedCallback() {
                    public void onScroll(int l, int t) {
                        if (!scrolled) {
                            scrolled = true;
                            countDownTimer = new CountDownTimer(BROWSING_TIME*1000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    tvreward.setText("Keep scrolling for "+(int)(millisUntilFinished/1000)+" seconds to get rewarded");
                                }

                                @Override
                                public void onFinish() {
                                    countDownTimer.cancel();
                                    tvreward.setText("You have got rewarded");

                                    finish();
                                    Toast.makeText(getApplicationContext(), "You are rewarded with 5 Energy", Toast.LENGTH_LONG).show();
                                    ParseUser.getCurrentUser().put(PC.KEY_USER_AVAILABLE_ENERGY, previousEnergy + ENERGY_REWARD);
                                    ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            Home1Fragment.energyTv.setText(ParseUser.getCurrentUser().getInt(PC.KEY_USER_AVAILABLE_ENERGY) + "");
                                        }
                                    });
                                }
                            };

                            countDownTimer.start();



                        }
                    }
                });


            }
        });




    }


    @Override
    public void onBackPressed() {

    }
}
