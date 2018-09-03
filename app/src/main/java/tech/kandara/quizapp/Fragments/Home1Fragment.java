package tech.kandara.quizapp.Fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.List;

import tech.kandara.quizapp.EnergyGameActivity;
import tech.kandara.quizapp.GameActivity;
import tech.kandara.quizapp.Library.AddCreditLimit;
import tech.kandara.quizapp.Library.TitlEffect.TiltEffectAttacher;
import tech.kandara.quizapp.MainActivity;
import tech.kandara.quizapp.PC;
import tech.kandara.quizapp.R;
import tech.kandara.quizapp.User;


public class Home1Fragment extends Fragment {

    // private AdView mAdView;
    LinearLayout playNowBtn;
    public static TextView fullNameTv, creditTv, energyTv;
    TextView tvTotalGames, tvTotalCreditWon;

    private InterstitialAd mInterstitialAd;
    private InterstitialAd mInterstitialAd2;

    private LinearLayout referFriend;
    private LinearLayout challengedFriend;


    Button addCreditBtn;
    static int GAME_LIMIT = 400;
    int PICK_IMAGE = 343;
    boolean showWebsiteAd = false;
    int newVersion;
    ImageView imageView;

    FirebaseAuth auth;


    public Home1Fragment() {
        // Required empty public constructor
    }


    public void loadInterstitialAd() {
        mInterstitialAd.loadAd(new AdRequest.Builder()
                /*.addTestDevice("9339BF107335CB968B9CFC2FE448DC84")*/.build());
    }

    public void loadInterstitialAd2() {
        mInterstitialAd.loadAd(new AdRequest.Builder()
                .addTestDevice("9339BF107335CB968B9CFC2FE448DC84").build());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        checkVersion();
        View view = inflater.inflate(R.layout.fragment_home1, container, false);

        imageView = view.findViewById(R.id.profileImg);
        tvTotalCreditWon = view.findViewById(R.id.tvTotalWon);
        addCreditBtn = view.findViewById(R.id.addCreditBtn);
        tvTotalGames = view.findViewById(R.id.tvTotalGames);
        referFriend = view.findViewById(R.id.referFriend);
        challengedFriend = view.findViewById(R.id.challengeFriend);
        playNowBtn = view.findViewById(R.id.playNow);
        fullNameTv = view.findViewById(R.id.fullName);
        creditTv = view.findViewById(R.id.credit);
        energyTv = view.findViewById(R.id.tvEnergy);
        addCreditBtn.setVisibility(View.VISIBLE);

        TiltEffectAttacher.attach(playNowBtn);
        challengedFriend.setVisibility(View.GONE);

        auth = FirebaseAuth.getInstance();
        FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
        // mFirebaseInstance.setPersistenceEnabled(true);

        final DatabaseReference mFirebaseDatabase = mFirebaseInstance.getReference("users");
        mFirebaseDatabase.keepSynced(true);


        mFirebaseDatabase.child("SCW0nWF1qXfddxfbhN57GFXvPcW2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final User user = dataSnapshot.getValue(User.class);
                tvTotalGames.setText(user.getTotal_game() + "");
                tvTotalCreditWon.setText(user.getTotal_credit_won() + "");
                imageView.setImageResource(getProfile(user.getUser_lvl()));
                fullNameTv.setText(user.getFirstname() + " " + user.getLastname());
                creditTv.setText(user.getAvail_cred() + "");
                energyTv.setText(user.getAvail_energy() + "");

                mInterstitialAd = new InterstitialAd(getContext());
                mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));
                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        //Toast.makeText(getActivity(), "2 Energy has been added to your account", Toast.LENGTH_LONG).show();

                        loadInterstitialAd();
                        super.onAdClosed();
                    }

                    @Override
                    public void onAdOpened() {
                        super.onAdOpened();
                    }
                });
                loadInterstitialAd();

                mInterstitialAd2 = new InterstitialAd(getActivity());
                mInterstitialAd2.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));
                mInterstitialAd2.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        if (user.getAvail_energy() >= 10) {
                            //TODO:WTF???
                            final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "Please Wait", "Processing");
                            progressDialog.show();
                            user.setAvail_energy(user.getAvail_energy() - 10);
                            mFirebaseDatabase.child("avail_energy").setValue(user.getAvail_energy());
                            if (user.getAvail_energy() < 0) {
                                user.setAvail_energy(user.getAvail_energy() + 10);
                                mFirebaseDatabase.child("avail_energy").setValue(user.getAvail_energy());
                            } else
                                progressDialog.dismiss();
                            if (canPlayGame()) {
                                startActivity(new Intent(getActivity(), GameActivity.class));
                            } else {
                                showBuyGamesDialog(user);
                            }

                        }
                        loadInterstitialAd2();
                        super.onAdClosed();
                    }

                    @Override
                    public void onAdOpened() {
                        super.onAdOpened();
                    }
                });


                loadInterstitialAd();
                loadInterstitialAd2();

                if ((user.isFb_linked() || user.isGplus_linked())) {

                    referFriend.setVisibility(View.VISIBLE);
                } else {
                    referFriend.setVisibility(View.GONE);
                }

                //TODO:objectid
                referFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShareDialog shareDialog;
                        FacebookSdk.sdkInitialize(getActivity());
                        shareDialog = new ShareDialog(getActivity());
                        ShareHashtag shareHashTag = new ShareHashtag.Builder().setHashtag("#kuiz").build();
                        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                                .setShareHashtag(shareHashTag)
                                .setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=tech.kandara.quizapp"))
                                .setQuote("Use referral code : " + user.getReferral_code() + " to get 15 Koins on Kuiz")
                                .build();

                        shareDialog.show(linkContent);
                    }
                });


                addCreditBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        if (mInterstitialAd.isLoaded()) {
                            user.setAvail_energy(user.getAvail_energy() + 2);
                            mFirebaseDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("avail_energy").setValue(user.getAvail_energy());
                            energyTv.setText(user.getAvail_energy() + "");
                            mInterstitialAd.show();
                            loadInterstitialAd();
                        } else {
                            final AddCreditLimit addCreditLimit = new AddCreditLimit(getActivity());
                            final Calendar calendar = Calendar.getInstance();
                            if (!addCreditLimit.isLimitReached(calendar.getTimeInMillis())) {

                                addCreditLimit.addCredit(calendar.getTimeInMillis());
                                user.setAvail_energy(user.getAvail_energy() + 2);
                                mFirebaseDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("avail_energy").setValue(user.getAvail_energy());
                                energyTv.setText(user.getAvail_energy() + "");
                                loadInterstitialAd();
                            } else {
                                Toast.makeText(getActivity(), "You have already added 20 Koins less than 5 mins ago. Please wait for some time or try watching ad if available", Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        challengedFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), EnergyGameActivity.class));
            }
        });

        playNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategoryDialogue();

            }
        });

        return view;
    }


    public void checkVersion() {


        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            final int version = pInfo.versionCode;
            //TODO:version?

           /* ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(PC.KEY_VERSION_OBJECT);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (objects.size() != 0) {
                            ParseObject versionObj = objects.get(0);
                            newVersion = versionObj.getInt(PC.KEY_VERSION_CODE);
                            String versionChanges = versionObj.getString(PC.KEY_VERSION_CHANGES);
                            GAME_LIMIT = versionObj.getInt(PC.KEY_LIMIT);
                            showWebsiteAd = versionObj.getBoolean(PC.WATCH_AD_ENABLED);
                            if (newVersion > version) {
                                showYesNoDialog3(versionChanges, "There is a new version available. You need to update before you can use.", "Update", "", new GameActivity.OnYesClicked() {
                                    @Override
                                    public void onDone() {
                                        final String appPackageName = getActivity().getPackageName(); // getPackageName() from Context or Activity object
                                        try {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                        } catch (android.content.ActivityNotFoundException anfe) {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                        }
                                    }
                                }, new GameActivity.OnNoClicked() {
                                    @Override
                                    public void onDone() {

                                    }
                                });
                            }
                        }
                    }
                }
            });*/
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean canPlayGame() {

        return true;
    }

    public void showBuyGamesDialog(final User user) {

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_buy_games,
                null);

        final RadioGroup schemes = dialogView.findViewById(R.id.schemes);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        switch (schemes.getCheckedRadioButtonId()) {
                            case R.id.scheme1:
                                int price = 10;
                                int energy = 5;
                                int game = 5;
                                if (user.getAvail_cred() > price) {

                                    final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "Please Wait", "Processing");
                                    progressDialog.show();
                                    user.setAvail_energy(user.getAvail_energy() + energy);
                                    user.setAvail_cred(user.getAvail_cred() - price);
                                    user.setTotal_game(GAME_LIMIT - game);
                                    FirebaseDatabase.getInstance().getReference("users").
                                            child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .setValue(user.getAvail_energy()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isComplete()) {
                                                progressDialog.dismiss();
                                                restartGame();
                                                Toast.makeText(getActivity(), "You have successfully bought the game", Toast.LENGTH_LONG).show();

                                            } else {
                                                Toast.makeText(getActivity(), "Error " + task.getException(), Toast.LENGTH_LONG).show();

                                            }
                                        }
                                    });


                                } else {
                                    Toast.makeText(getActivity(), "You do not have enough Koin", Toast.LENGTH_LONG).show();
                                }
                                break;


                            case R.id.scheme2:
                                int price2 = 16;
                                int energy2 = 10;
                                int game2 = 10;
                                if (user.getAvail_cred() > price2) {

                                    final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "Please Wait", "Processing");
                                    progressDialog.show();
                                    user.setAvail_energy(user.getAvail_energy() + energy2);
                                    user.setAvail_cred(user.getAvail_cred() - price2);
                                    user.setTotal_game(GAME_LIMIT - game2);
                                    FirebaseDatabase.getInstance().getReference("users").
                                            child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .setValue(user.getAvail_energy()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isComplete()) {
                                                progressDialog.dismiss();
                                                restartGame();
                                                Toast.makeText(getActivity(), "You have successfully bought the game", Toast.LENGTH_LONG).show();

                                            } else {
                                                Toast.makeText(getActivity(), "Error " + task.getException(), Toast.LENGTH_LONG).show();

                                            }
                                        }
                                    });


                                } else {
                                    Toast.makeText(getActivity(), "You do not have enough Koin", Toast.LENGTH_LONG).show();
                                }

                                break;

                            case R.id.scheme3:
                                int price3 = 30;
                                int energy3 = 25;
                                int game3 = 25;
                                if (user.getAvail_cred() > price3) {

                                    final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "Please Wait", "Processing");
                                    progressDialog.show();
                                    user.setAvail_energy(user.getAvail_energy() + energy3);
                                    user.setAvail_cred(user.getAvail_cred() - price3);
                                    user.setTotal_game(GAME_LIMIT - game3);
                                    FirebaseDatabase.getInstance().getReference("users").
                                            child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .setValue(user.getAvail_energy()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isComplete()) {
                                                progressDialog.dismiss();
                                                restartGame();
                                                Toast.makeText(getActivity(), "You have successfully bought the game", Toast.LENGTH_LONG).show();

                                            } else {
                                                Toast.makeText(getActivity(), "Error " + task.getException(), Toast.LENGTH_LONG).show();

                                            }
                                        }
                                    });


                                } else {
                                    Toast.makeText(getActivity(), "You do not have enough Koin", Toast.LENGTH_LONG).show();
                                }
                                break;

                            case R.id.scheme4:
                                int price4 = 50;
                                int energy4 = 50;
                                int game4 = 50;
                                if (user.getAvail_cred() > price4) {

                                    final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "Please Wait", "Processing");
                                    progressDialog.show();
                                    user.setAvail_energy(user.getAvail_energy() + energy4);
                                    user.setAvail_cred(user.getAvail_cred() - price4);
                                    user.setTotal_game(GAME_LIMIT - game4);
                                    FirebaseDatabase.getInstance().getReference("users").
                                            child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .setValue(user.getAvail_energy()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isComplete()) {
                                                progressDialog.dismiss();
                                                restartGame();
                                                Toast.makeText(getActivity(), "You have successfully bought the game", Toast.LENGTH_LONG).show();

                                            } else {
                                                Toast.makeText(getActivity(), "Error " + task.getException(), Toast.LENGTH_LONG).show();

                                            }
                                        }
                                    });


                                } else {
                                    Toast.makeText(getActivity(), "You do not have enough Koin", Toast.LENGTH_LONG).show();
                                }
                                break;

                            default:
                                Toast.makeText(getActivity(), "Please select one option", Toast.LENGTH_LONG).show();
                                break;
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:

                        break;
                }
            }
        };
        builder.setView(dialogView);
        builder.setPositiveButton("Buy", dialogClickListener)
                .setNegativeButton("Cancel", dialogClickListener).show();
    }

    public void restartGame() {
        getActivity().finish();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        getActivity().startActivity(intent);
    }


    public void showYesNoDialog3(String versionChangeText, String title, String yes, String no, final GameActivity.OnYesClicked onYesClicked, final GameActivity.OnNoClicked onNoClicked) {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        onYesClicked.onDone();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        if (onNoClicked != null) {
                            onNoClicked.onDone();
                        }
                        break;
                }
            }
        };


        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_version_changes,
                null);
        TextView versionChanges = (TextView) dialogView.findViewById(R.id.versionChanges);
        versionChanges.setText(versionChangeText.replace("--", "\n"));

        builder.setView(dialogView);
        builder.setCancelable(false);
        builder.setMessage(title).setPositiveButton(yes, dialogClickListener).show();
    }

    public void showCategoryDialogue() {


        final AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity());

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_select_category,
                null);
        LinearLayout gk = dialogView.findViewById(R.id.gk);
        LinearLayout beer = dialogView.findViewById(R.id.beer);
        LinearLayout grammar = dialogView.findViewById(R.id.grammar);
        LinearLayout facebook = dialogView.findViewById(R.id.facebook);
        LinearLayout fifa = dialogView.findViewById(R.id.fifa);
        LinearLayout premierleague = dialogView.findViewById(R.id.premierleague);
        LinearLayout got = dialogView.findViewById(R.id.got);
        LinearLayout science = dialogView.findViewById(R.id.science);
        LinearLayout quotation = dialogView.findViewById(R.id.quotation);
        LinearLayout solar = dialogView.findViewById(R.id.solar);
        LinearLayout saarc = dialogView.findViewById(R.id.saarc);
        LinearLayout addQuestions = dialogView.findViewById(R.id.addQuestion);
        addQuestions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showYesNoDialog("If you are interested in writing interesting questions for Kuiz, then we have an excellent offer for you. Write 10 MCQs on the topic of your choice and send us. Your questions will be featured in our app. \nSend us an email with the questions", "OK", "Cancel", new GameActivity.OnYesClicked() {
                    @Override
                    public void onDone() {
                        Intent i = new Intent(Intent.ACTION_SEND);
                        /*i.setType("message/rfc822");
                        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"kandara.tech2015@gmail.com"});
                        i.putExtra(Intent.EXTRA_SUBJECT, "Hello");
                        i.putExtra(Intent.EXTRA_TEXT   , "From "+ ParseUser.getCurrentUser().getUsername()+"\n");*/
                        try {
                            startActivity(Intent.createChooser(i, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new GameActivity.OnNoClicked() {
                    @Override
                    public void onDone() {

                    }
                });
            }
        });

        gk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), GameActivity.class);
                intent.putExtra("title", PC.TAG_NEPAL_HISTORY);
                startActivity(intent);
            }
        });
        beer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), GameActivity.class);
                intent.putExtra("title", PC.TAG_BEER);
                startActivity(intent);
            }
        });
        grammar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), GameActivity.class);
                intent.putExtra("title", PC.TAG_GRAMMAR);
                startActivity(intent);
            }
        });
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), GameActivity.class);
                intent.putExtra("title", PC.TAG_FACEBOOK);
                startActivity(intent);
            }
        });
        fifa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), GameActivity.class);
                intent.putExtra("title", PC.TAG_FIFA);
                startActivity(intent);
            }
        });
        premierleague.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), GameActivity.class);
                intent.putExtra("title", PC.TAG_PREMIER_LEAGUE);
                startActivity(intent);
            }
        });
        got.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), GameActivity.class);
                intent.putExtra("title", PC.TAG_GAME_OF_THRONES);
                startActivity(intent);
            }
        });
        science.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), GameActivity.class);
                intent.putExtra("title", PC.TAG_SCIENCE);
                startActivity(intent);
            }
        });
        quotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), GameActivity.class);
                intent.putExtra("title", PC.TAG_QUOTES);
                startActivity(intent);
            }
        });
        solar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), GameActivity.class);
                intent.putExtra("title", PC.TAG_SOLAR_SYSTEM);
                startActivity(intent);
            }
        });
        saarc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), GameActivity.class);
                intent.putExtra("title", PC.TAG_SAARC);
                startActivity(intent);
            }
        });


        builder.setView(dialogView);
        builder.setCancelable(true);
        builder.show();
    }


    public void showYesNoDialog(String title, String yes, String no, final GameActivity.OnYesClicked onYesClicked, final GameActivity.OnNoClicked onNoClicked) {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        onYesClicked.onDone();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        if (onNoClicked != null) {
                            onNoClicked.onDone();
                        }
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(title).setPositiveButton(yes, dialogClickListener).setNegativeButton(no, dialogClickListener).show();
    }


    public int getProfile(int level) {
        switch (level + 1) {
            case 1:
                return R.drawable.fox1;

            case 2:
                return R.drawable.fox1;

            case 3:
                return R.drawable.fox2;

            case 4:
                return R.drawable.fox3;

            case 5:
                return R.drawable.fox4;

            case 6:
                return R.drawable.fox5;
        }
        return R.drawable.fox5;
    }
}
