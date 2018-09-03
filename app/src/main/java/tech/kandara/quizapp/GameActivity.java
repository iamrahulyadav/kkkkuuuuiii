package tech.kandara.quizapp;

import android.animation.Animator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ldoublem.loadingviewlib.view.LVBlazeWood;
import com.parse.ParseObject;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import tech.kandara.quizapp.Fragments.LeaderBoardFragment;
import tech.kandara.quizapp.Library.AnimateHorizontalProgressBar;
import tech.kandara.quizapp.Library.utils.MySettings;
import tech.kandara.quizapp.Service.MusicService;

public class GameActivity extends AppCompatActivity {


    /**
     * State Progress Layout
     */

    private LinearLayout lin1, lin2, lin3, lin4, lin5, lin6, lin7;

    /**
     * Ads and Stuffs
     */


    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private boolean currentLevelAdShown = false;

    /**
     * Three Screens (Loading, Main, Final)
     */

    RelativeLayout loadingScreen;
    RelativeLayout mainScreen;
    RelativeLayout finalScreen;

    /**
     * Main Screen Views
     */

    Button btnCurrentLevel;
    Button questionStartBtn;
    TextView question;
    AnimateHorizontalProgressBar timerProgressBar;
    Button answer1, answer2, answer3, answer4;
    LinearLayout lifelines;
    Button fiftyFiftyBtn, sixtySeconds, skipBtn;
    LVBlazeWood lvBlock;

    /**
     * Timer Stuffs
     */

    int TIME_FOR_EACH_QUESTION = 12000;
    CountDownTimer countDownTimer;
    long totalTimeTaken = 0;


    CountDownTimer countDownTimerForNextQuestion;
    boolean countDownTimerForNextQuestionHasStarted = false;

    private boolean gamePaused = false;

    /**
     * Quiz Stuffs
     */

    ArrayList<Question> questionList;
    ArrayList<String> questionIdList;
    int currentLevel = 1;
    boolean fiftyFiftyAvailable = true;
    boolean sixtySecondsAvailable = true;
    int totalCorrectQuestion = 0;
    int totalWrongAnswer = 0;
    boolean currentLevelIncremented = false;
    boolean currentLevelSoundPlayed = false;
    boolean TickTockPlayed = false;
    boolean musicStarted = false;
    int thislevelrepitition = 0;
    /**
     * Background Music
     */

    long thisLevelTime = 0;
    Intent bgMusicService;

    MySettings mySettings;


    MediaPlayer choiceMediaplayer;
    MediaPlayer successMediaplayer;
    MediaPlayer failedMediaPlayer;
    MediaPlayer correctAnsMediaPlayer;
    MediaPlayer tickTockMediaPlayer;
    MediaPlayer wrongAnswerMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        connectViews();

        startViews();

        showLoading();

        initQuizStuffs();


        startAdsLoading();

        collectQuestionsFromServer();


    }

    /**
     * Connect Views with XML
     */

    public void connectViews() {
        lvBlock = findViewById(R.id.lv_block);
        btnCurrentLevel = findViewById(R.id.currentLevelButton);
        questionStartBtn = findViewById(R.id.startBtn);
        question = findViewById(R.id.questionText);
        lifelines = findViewById(R.id.lifelines);
        lifelines.setVisibility(View.GONE);
        loadingScreen = findViewById(R.id.loadingScreen);
        finalScreen = findViewById(R.id.finalScreen);
        mainScreen = findViewById(R.id.otherScreen);
        timerProgressBar = findViewById(R.id.timeTracker);
        answer1 = findViewById(R.id.answer1);
        answer2 = findViewById(R.id.answer2);
        answer3 = findViewById(R.id.answer3);
        answer4 = findViewById(R.id.answer4);
        fiftyFiftyBtn = findViewById(R.id.fiftyFifty);
        skipBtn = findViewById(R.id.skipBtn);
        sixtySeconds = findViewById(R.id.sixtySecond);
        mAdView = findViewById(R.id.adView);
        //Linear Layouts
        lin1 = findViewById(R.id.lin1);
        lin2 = findViewById(R.id.lin2);
        lin3 = findViewById(R.id.lin3);
        lin4 = findViewById(R.id.lin4);
        lin5 = findViewById(R.id.lin5);
        lin6 = findViewById(R.id.lin6);
        lin7 = findViewById(R.id.lin7);
    }

    /**
     * Starts Ads Loading
     */

    public void startAdsLoading() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("9339BF107335CB968B9CFC2FE448DC84").build();
        mAdView.loadAd(adRequest);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                gamePaused = false;
                super.onAdClosed();
            }

            @Override
            public void onAdOpened() {
                gamePaused = true;
                super.onAdOpened();
            }
        });
        loadInterstitialAd();
    }

    /**
     * Load Ad into interstitial
     */


    public void loadInterstitialAd() {
        mInterstitialAd.loadAd(new AdRequest.Builder()
                .addTestDevice("9339BF107335CB968B9CFC2FE448DC84").build());
    }


    /**
     * Plays Background Music
     */

    public void initBackgroundMusic() {
        bgMusicService = new Intent(GameActivity.this, MusicService.class);
        startService(bgMusicService);
    }

    /**
     * Initialize Views
     */

    public void startViews() {
        //Loading Animation
        lvBlock.startAnim();


        mySettings = new MySettings(GameActivity.this);

        //State Progress Bar
        btnCurrentLevel.setText("1");

        setCorrectProgress(0);
        //Start Button
        questionStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentLevel == 1) {
                    if (mySettings.isSoundOn()) {
                        musicStarted = true;
                        initBackgroundMusic();
                    }
                }
                startQuiz();

            }
        });
        countDownTimerForNextQuestion = new CountDownTimer(2300, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                countDownTimerForNextQuestionHasStarted = false;
                if (!gamePaused) {
                    thislevelrepitition = 0;
                    increaseLevelState();
                    startQuiz();
                } else {
                    if (thislevelrepitition >= 4) {
                        thislevelrepitition = 0;
                        increaseLevelState();
                        startQuiz();
                    } else {
                        thislevelrepitition++;
                        countDownTimerForNextQuestion.start();
                    }
                }
            }
        };


    }

    public void setUpTimer(final int totalTime) {
        timerProgressBar.setMax(totalTime / 10);
        timerProgressBar.setProgress(totalTime / 10);
        countDownTimer = new CountDownTimer(totalTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TickTockPlayed = true;
                if (!gamePaused) {
                    PlayTickTockSound();
                }
                thisLevelTime = totalTime - millisUntilFinished;
                timerProgressBar.setProgress((int) millisUntilFinished / 10);
            }

            @Override
            public void onFinish() {
                countDownTimer.cancel();
                if (!gamePaused) {
                    PlayWrongAnswerSound();
                }
                showUIWrong(0, 0, true);

            }
        };
    }


    /**
     * Init Quiz Stuffs
     */

    public void initQuizStuffs() {
        questionList = new ArrayList<>();
        questionIdList = new ArrayList<>();
    }

    /**
     * Shows LOading Screen
     * Hides Main Screen and FInal Screen
     */


    public void showLoading() {
        finalScreen.setVisibility(View.GONE);
        loadingScreen.setVisibility(View.VISIBLE);
        mainScreen.setVisibility(View.GONE);
    }

    /**
     * Shows Main Screen
     * Hides Loading and FInal Screen
     */

    public void showMainScreen() {
        finalScreen.setVisibility(View.GONE);
        loadingScreen.setVisibility(View.GONE);
        mainScreen.setVisibility(View.VISIBLE);
    }

    /**
     * Shows Game Over Screen
     * Hides Main and Loading Screen
     */

    public void showFinal() {
        finalScreen.setVisibility(View.VISIBLE);
        loadingScreen.setVisibility(View.GONE);
        mainScreen.setVisibility(View.GONE);
    }


    @Override
    protected void onPause() {
        gamePaused = true;
        if (countDownTimerForNextQuestionHasStarted) {
            countDownTimerForNextQuestion.cancel();
        }

        if (musicStarted) {
            if (mySettings.isSoundOn()) {
                Intent intent = new Intent(GameActivity.this, MusicService.class);
                intent.putExtra("pause", true);
                startService(intent);


            }
        }


        super.onPause();
    }

    @Override
    protected void onResume() {
        gamePaused = false;
        if (countDownTimerForNextQuestionHasStarted) {
            countDownTimerForNextQuestion.start();
        }

        if (musicStarted) {
            if (mySettings.isSoundOn()) {
                Intent intent = new Intent(GameActivity.this, MusicService.class);
                intent.putExtra("play", true);
                startService(intent);
            }
        }

        if (mySettings.isSoundOn()) {
            if (choiceMediaplayer != null) {
                choiceMediaplayer = MediaPlayer.create(GameActivity.this, R.raw.popsound);
                choiceMediaplayer.start();
                choiceMediaplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        choiceMediaplayer.release();
                    }
                });
            }

        }

        super.onResume();
    }

    @Override
    protected void onDestroy() {

        if (musicStarted) {
            if (mySettings.isSoundOn()) {
                Intent intent = new Intent(GameActivity.this, MusicService.class);
                stopService(intent);
            }
        }

        super.onDestroy();
    }


    /**
     * Returns State Number as stated in StateProgressBar.class
     *
     * @param x
     * @return
     */


    /**
     * If Back button is pressed the app will ask if it was pressed accidentally
     */

    @Override
    public void onBackPressed() {
        showYesNoDialog("You still have a quiz running. Do you really want to leave?", "Quit Game", "Cancel", new OnYesClicked() {
            @Override
            public void onDone() {
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            }
        }, null);
    }


    /**
     * Play Game Sounds
     * Pop for Each Answer
     * Wrong and Right for respective answers
     */


    public void PlayChoicePopSound() {

        if (mySettings.isSoundOn()) {
            choiceMediaplayer = MediaPlayer.create(GameActivity.this, R.raw.popsound);
            choiceMediaplayer.start();
            choiceMediaplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    choiceMediaplayer.release();
                }
            });
        }
    }

    public void PlaySuccessSound() {

        if (mySettings.isSoundOn()) {
            successMediaplayer = MediaPlayer.create(GameActivity.this, R.raw.success);
            successMediaplayer.start();
            successMediaplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    successMediaplayer.release();
                }
            });
        }
    }

    public void PlayFailedSound() {

        if (mySettings.isSoundOn()) {
            failedMediaPlayer = MediaPlayer.create(GameActivity.this, R.raw.failed);
            failedMediaPlayer.start();
            failedMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    failedMediaPlayer.release();
                }
            });
        }
    }


    public void PlayCorrectAnswerSound() {

        if (mySettings.isSoundOn()) {
            if (!currentLevelSoundPlayed) {
                currentLevelSoundPlayed = true;
                correctAnsMediaPlayer = MediaPlayer.create(GameActivity.this, R.raw.ansright);
                correctAnsMediaPlayer.start();
                correctAnsMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        correctAnsMediaPlayer.release();
                    }
                });
            }
        }
    }

    public void PlayTickTockSound() {

        if (mySettings.isSoundOn()) {
            if (TickTockPlayed) {
                TickTockPlayed = false;
                tickTockMediaPlayer = MediaPlayer.create(GameActivity.this, R.raw.tick);
                tickTockMediaPlayer.start();
                tickTockMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        tickTockMediaPlayer.release();
                    }
                });
            }
        }
    }

    public void PlayWrongAnswerSound() {

        if (mySettings.isSoundOn()) {
            if (!currentLevelSoundPlayed) {
                currentLevelSoundPlayed = true;
                wrongAnswerMediaPlayer = MediaPlayer.create(GameActivity.this, R.raw.answrong);
                wrongAnswerMediaPlayer.start();
                wrongAnswerMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        wrongAnswerMediaPlayer.release();
                    }
                });
            }
        }
    }


    /**
     * Checks if there is an existing session available in the server
     */


    /**
     * Animates State ProgressBar when increasing the level
     */


    private void increaseLevelState() {
        YoYo.with(Techniques.SlideOutUp)
                .duration(700)
                .onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {

                        btnCurrentLevel.setText(currentLevel + "");
                        YoYo.with(Techniques.SlideInDown)
                                .duration(700)
                                .playOn(btnCurrentLevel);
                    }
                })
                .playOn(btnCurrentLevel);
    }

    /**
     * Interface for Yes Clicked
     */

    public interface OnYesClicked {
        void onDone();
    }

    /**
     * Interface for NO Clicked
     */

    public interface OnNoClicked {
        void onDone();
    }

    /**
     * Displays a Yes No Dialog containing title. Also has actions for both Yes and No Options
     *
     * @param title
     * @param yes
     * @param no
     * @param onYesClicked
     * @param onNoClicked
     */

    public void showYesNoDialog(String title, String yes, String no, final OnYesClicked onYesClicked, final OnNoClicked onNoClicked) {

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

        AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
        builder.setMessage(title).setPositiveButton(yes, dialogClickListener)
                .setNegativeButton(no, dialogClickListener).show();
    }


    /**
     * Restores the previous session into questions and four answer with their respective UIs
     *
     * @param correct
     * @param selected
     * @param second
     * @param third
     * @param fourth
     * @param correctAnswer
     * @param selectedAnswer
     * @param secondAnswer
     * @param thirdAnswer
     * @param fourthAnswer
     */


    /**
     * Converts String to an Array of Question objects
     *
     * @param questions
     * @return
     */


    /**
     * Starts the quiz
     */

    public void startQuiz() {
        thisLevelTime = 0;
        //Sets the background of each question to the default background
        answer1.setBackgroundResource(R.drawable.bg_button);
        answer2.setBackgroundResource(R.drawable.bg_button);
        answer3.setBackgroundResource(R.drawable.bg_button);
        answer4.setBackgroundResource(R.drawable.bg_button);

        //Makes the question visible
        question.setVisibility(View.VISIBLE);
        question.setVisibility(View.VISIBLE);

        //Makes the answers invisible so that they can be animated
        answer1.setVisibility(View.GONE);
        answer2.setVisibility(View.GONE);
        answer3.setVisibility(View.GONE);
        answer4.setVisibility(View.GONE);

        //Disables the answers so that they cant be clicked unless all of the answers are popped
        disableAnswerClick();

        //Hides the StartButton before displaying all answers
        questionStartBtn.setVisibility(View.GONE);

        //Gets the current question
        Question currentQuestion = questionList.get(currentLevel - 1);
        ArrayList<String> wrongAnswers = currentQuestion.getWronganswer();


        //Shuffles the question for better result
        Collections.shuffle(wrongAnswers);

        //Select a random position for right answers
        Random random = new Random();
        int rightAnswer = (random.nextInt(4)) + 1;

        //Sets the question to textView
        question.setText(currentQuestion.getQuestion());

        //Set ad status to initial
        currentLevelAdShown = false;

        //Set parameters to initial
        currentLevelIncremented = false;
        currentLevelSoundPlayed = false;

        //Load interstitial Ads
        loadInterstitialAd();

        //Fills up the answers
        switch (rightAnswer) {
            case 1:
                answer1.setText(currentQuestion.getRightAnswer());
                answer2.setText(wrongAnswers.get(0));
                answer3.setText(wrongAnswers.get(1));
                answer4.setText(wrongAnswers.get(2));
                addListeners(rightAnswer);
                break;
            case 2:
                answer2.setText(currentQuestion.getRightAnswer());
                answer1.setText(wrongAnswers.get(0));
                answer3.setText(wrongAnswers.get(1));
                answer4.setText(wrongAnswers.get(2));
                addListeners(rightAnswer);
                break;
            case 3:
                answer3.setText(currentQuestion.getRightAnswer());
                answer2.setText(wrongAnswers.get(0));
                answer1.setText(wrongAnswers.get(1));
                answer4.setText(wrongAnswers.get(2));
                addListeners(rightAnswer);
                break;
            case 4:
                answer4.setText(currentQuestion.getRightAnswer());
                answer2.setText(wrongAnswers.get(0));
                answer3.setText(wrongAnswers.get(1));
                answer1.setText(wrongAnswers.get(2));
                addListeners(rightAnswer);
                break;
        }

        //Starts animating the qesutions
        question.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.Landing)
                .duration(2500)
                .onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        if (!gamePaused) {
                            PlayChoicePopSound();
                        }
                        answer1.setVisibility(View.VISIBLE);
                        YoYo.with(Techniques.BounceIn)
                                .duration(250)
                                .onEnd(new YoYo.AnimatorCallback() {
                                    @Override
                                    public void call(Animator animator) {
                                        if (!gamePaused) {
                                            PlayChoicePopSound();
                                        }
                                        answer2.setVisibility(View.VISIBLE);
                                        YoYo.with(Techniques.BounceIn)
                                                .duration(250)
                                                .onEnd(new YoYo.AnimatorCallback() {
                                                    @Override
                                                    public void call(Animator animator) {
                                                        if (!gamePaused) {
                                                            PlayChoicePopSound();
                                                        }
                                                        answer3.setVisibility(View.VISIBLE);
                                                        YoYo.with(Techniques.BounceIn)
                                                                .duration(250)
                                                                .onEnd(new YoYo.AnimatorCallback() {
                                                                    @Override
                                                                    public void call(Animator animator) {
                                                                        if (!gamePaused) {
                                                                            PlayChoicePopSound();
                                                                        }

                                                                        answer4.setVisibility(View.VISIBLE);
                                                                        YoYo.with(Techniques.BounceIn)
                                                                                .duration(250)
                                                                                .onEnd(new YoYo.AnimatorCallback() {
                                                                                    @Override
                                                                                    public void call(Animator animator) {

                                                                                        setUpTimer(TIME_FOR_EACH_QUESTION);
                                                                                        countDownTimer.start();

                                                                                        fiftyFiftyBtn.setVisibility(View.VISIBLE);
                                                                                        skipBtn.setVisibility(View.VISIBLE);
                                                                                        sixtySeconds.setVisibility(View.VISIBLE);
                                                                                        lifelines.setVisibility(View.VISIBLE);
                                                                                        timerProgressBar.setVisibility(View.VISIBLE);

                                                                                        YoYo.with(Techniques.BounceIn).duration(400).playOn(skipBtn);
                                                                                        YoYo.with(Techniques.BounceIn).duration(400).playOn(fiftyFiftyBtn);
                                                                                        YoYo.with(Techniques.BounceIn).duration(400).playOn(sixtySeconds);
                                                                                        YoYo.with(Techniques.BounceIn).duration(400).playOn(timerProgressBar);


                                                                                        answer1.setEnabled(true);
                                                                                        answer2.setEnabled(true);
                                                                                        answer3.setEnabled(true);
                                                                                        answer4.setEnabled(true);
                                                                                    }
                                                                                })
                                                                                .playOn(answer4);
                                                                    }
                                                                })
                                                                .playOn(answer3);
                                                    }
                                                })
                                                .playOn(answer2);
                                    }
                                })
                                .playOn(answer1);
                    }
                })
                .playOn(question);
    }


    /**
     * Saves the current position of answers
     *
     * @param correct
     * @param selected
     * @param second
     * @param third
     * @param fourth
     */


    /**
     * Adds Listener based on the position of correct answer
     *
     * @param right
     */

    public void addListeners(final int right) {
        sixtySeconds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sixtySecondsAvailable) {
                    sixtySeconds.setEnabled(false);
                    sixtySecondsAvailable = false;
                    countDownTimer.cancel();
                    setUpTimer(20000);
                    countDownTimer.start();
                }
            }
        });

        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel < 9) {
                    currentLevel++;
                    countDownTimer.cancel();
                    increaseLevelState();

                    lifelines.setVisibility(View.GONE);
                    timerProgressBar.setVisibility(View.GONE);
                    startQuiz();
                } else {
                    Toast.makeText(getApplicationContext(), "You cant skip this question", Toast.LENGTH_LONG).show();
                }
            }
        });
        switch (right) {
            case 1:
                fiftyFiftyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (fiftyFiftyAvailable) {
                            fiftyFiftyBtn.setEnabled(false);
                            fiftyFiftyAvailable = false;
                            int s = new Random().nextInt(3);
                            switch (s) {
                                case 0:
                                    answer3.setEnabled(false);
                                    answer4.setEnabled(false);
                                    break;


                                case 1:
                                    answer2.setEnabled(false);
                                    answer4.setEnabled(false);
                                    break;


                                case 2:
                                    answer2.setEnabled(false);
                                    answer3.setEnabled(false);
                                    break;


                            }
                        }
                    }
                });
                answer1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUIRight(right, 1);
                    }
                });
                answer2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUIWrong(right, 2, false);
                    }
                });
                answer3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUIWrong(right, 3, false);
                    }
                });
                answer4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUIWrong(right, 4, false);
                    }
                });
                break;
            case 2:

                fiftyFiftyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (fiftyFiftyAvailable) {
                            fiftyFiftyBtn.setEnabled(false);
                            fiftyFiftyAvailable = false;
                            int s = new Random().nextInt(3);
                            switch (s) {
                                case 0:
                                    answer3.setEnabled(false);
                                    answer4.setEnabled(false);
                                    break;


                                case 1:
                                    answer1.setEnabled(false);
                                    answer4.setEnabled(false);
                                    break;


                                case 2:
                                    answer1.setEnabled(false);
                                    answer3.setEnabled(false);
                                    break;


                            }
                        }
                    }
                });
                answer2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUIRight(right, 2);
                    }
                });
                answer1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUIWrong(right, 1, false);
                    }
                });
                answer3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUIWrong(right, 3, false);
                    }
                });
                answer4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUIWrong(right, 4, false);
                    }
                });
                break;
            case 3:
                fiftyFiftyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (fiftyFiftyAvailable) {
                            fiftyFiftyBtn.setEnabled(false);
                            fiftyFiftyAvailable = false;
                            int s = new Random().nextInt(3);
                            switch (s) {
                                case 0:
                                    answer1.setEnabled(false);
                                    answer4.setEnabled(false);
                                    break;


                                case 1:
                                    answer2.setEnabled(false);
                                    answer4.setEnabled(false);
                                    break;


                                case 2:
                                    answer1.setEnabled(false);
                                    answer2.setEnabled(false);
                                    break;


                            }
                        }
                    }
                });
                answer3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUIRight(right, 3);
                    }
                });
                answer2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUIWrong(right, 2, false);
                    }
                });
                answer1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUIWrong(right, 1, false);
                    }
                });
                answer4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUIWrong(right, 4, false);
                    }
                });
                break;
            case 4:
                fiftyFiftyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (fiftyFiftyAvailable) {
                            fiftyFiftyBtn.setEnabled(false);
                            fiftyFiftyAvailable = false;
                            int s = new Random().nextInt(3);
                            switch (s) {
                                case 0:
                                    answer1.setEnabled(false);
                                    answer2.setEnabled(false);
                                    break;


                                case 1:
                                    answer1.setEnabled(false);
                                    answer3.setEnabled(false);
                                    break;


                                case 2:
                                    answer2.setEnabled(false);
                                    answer3.setEnabled(false);
                                    break;


                            }
                        }
                    }
                });
                answer4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUIRight(right, 4);
                    }
                });
                answer2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUIWrong(right, 2, false);
                    }
                });
                answer3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUIWrong(right, 3, false);
                    }
                });
                answer1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUIWrong(right, 1, false);
                    }
                });
                break;
        }
    }

    /**
     * Disabled clicking of answer before all answer are displayed
     */

    private void disableAnswerClick() {
        answer1.setEnabled(false);
        answer2.setEnabled(false);
        answer3.setEnabled(false);
        answer4.setEnabled(false);
    }

    /**
     * Shows Wrong UI
     *
     * @param rightAnswer
     * @param userAnswer
     * @param timeUp
     */

    public void showUIWrong(int rightAnswer, int userAnswer, boolean timeUp) {
        if (!gamePaused) {
            if (mySettings.isVibrationOn()) {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(500);
            }
        }
        totalWrongAnswer++;
        disableAnswerClick();
        if (!currentLevelIncremented && currentLevel <= 7) {
            currentLevelIncremented = true;
            currentLevel++;
        }
        showButtonColor(rightAnswer, userAnswer, timeUp);
        lifelines.setVisibility(View.GONE);
    }

    /**
     * Shows Right UI
     */

    public void showUIRight(int rightAnswer, int userAnswer) {

        disableAnswerClick();
        totalCorrectQuestion++;

        if (!currentLevelIncremented && currentLevel <= 7) {
            currentLevelIncremented = true;
            currentLevel++;
        }
        increaseCorrectState();
        showButtonColor(rightAnswer, userAnswer, false);
        lifelines.setVisibility(View.GONE);
    }

    public void increaseCorrectState() {
        YoYo.with(Techniques.TakingOff)
                .duration(700)
                .onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        setCorrectProgress(totalCorrectQuestion);
                        YoYo.with(Techniques.Landing)
                                .duration(700)
                                .playOn(findViewById(R.id.correctLins));
                    }
                })
                .playOn(findViewById(R.id.correctLins));
    }


    /**
     * Displays pulse animation on right answer button when the answer is Right
     *
     * @param view
     */

    public void showBtnAnimationRight(View view, final onAnimationEnd onAnimationEnd) {
        long duration = 700;
        if (onAnimationEnd != null) {
            duration = 1200;
        }
        if (!gamePaused) {
            PlayCorrectAnswerSound();
        }
        YoYo.with(Techniques.Pulse)
                .duration(duration)
                .onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        if (onAnimationEnd != null) {
                            onAnimationEnd.onEnd();
                        }

                    }
                })
                .playOn(view);
    }

    /**
     * Shows Correct Button Animation when the answer is wrong
     *
     * @param view
     */

    public void showCorrectBtnAnimationOnWrong(View view, final onAnimationEnd onAnimationEnd) {
        long duration = 700;
        if (onAnimationEnd != null) {
            duration = 1200;
        }
        if (!gamePaused) {
            PlayWrongAnswerSound();
        }
        YoYo.with(Techniques.Pulse)
                .duration(duration)
                .onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {

                        if (onAnimationEnd != null) {
                            onAnimationEnd.onEnd();
                        }
                    }
                })
                .playOn(view);
    }

    /**
     * Shows Wrong button animation
     *
     * @param view
     */

    public void showBtnAnimationWrong(View view, final onAnimationEnd onAnimationEnd) {
        long duration = 700;
        if (onAnimationEnd != null) {
            duration = 1200;
        }
        if (!gamePaused) {
            PlayWrongAnswerSound();
        }
        YoYo.with(Techniques.Shake)
                .duration(duration)
                .onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        if (onAnimationEnd != null) {
                            onAnimationEnd.onEnd();
                        }
                    }
                })
                .playOn(view);
    }

    /**
     * Hides other buttons when answer is right
     *
     * @param right
     */

    public void hideOtherButtonAnimation(int right) {

        switch (right) {
            case 1:
                YoYo.with(Techniques.ZoomOut).duration(700).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        answer2.setVisibility(View.GONE);
                    }
                }).playOn(answer2);
                YoYo.with(Techniques.ZoomOut).duration(700).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        answer3.setVisibility(View.GONE);
                    }
                }).playOn(answer3);
                YoYo.with(Techniques.ZoomOut).duration(700).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        answer4.setVisibility(View.GONE);
                    }
                }).playOn(answer4);
                break;
            case 2:
                YoYo.with(Techniques.ZoomOut).duration(700).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        answer1.setVisibility(View.GONE);
                    }
                }).playOn(answer1);
                YoYo.with(Techniques.ZoomOut).duration(700).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        answer3.setVisibility(View.GONE);
                    }
                }).playOn(answer3);
                YoYo.with(Techniques.ZoomOut).duration(700).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        answer4.setVisibility(View.GONE);
                    }
                }).playOn(answer4);
                break;
            case 3:
                YoYo.with(Techniques.ZoomOut).duration(700).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        answer1.setVisibility(View.GONE);
                    }
                }).playOn(answer1);
                YoYo.with(Techniques.ZoomOut).duration(700).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        answer2.setVisibility(View.GONE);
                    }
                }).playOn(answer2);
                YoYo.with(Techniques.ZoomOut).duration(700).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        answer4.setVisibility(View.GONE);
                    }
                }).playOn(answer4);
                break;
            case 4:
                YoYo.with(Techniques.ZoomOut).duration(700).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        answer1.setVisibility(View.GONE);
                    }
                }).playOn(answer1);
                YoYo.with(Techniques.ZoomOut).duration(700).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        answer2.setVisibility(View.GONE);
                    }
                }).playOn(answer2);
                YoYo.with(Techniques.ZoomOut).duration(700).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        answer3.setVisibility(View.GONE);
                    }
                }).playOn(answer3);
                break;
        }
    }


    public interface onAnimationEnd {
        void onEnd();
    }


    public void hideButton(int x) {
        switch (x) {
            case 1:
                YoYo.with(Techniques.TakingOff).duration(250).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        answer1.setVisibility(View.GONE);
                    }
                }).playOn(answer1);
                break;
            case 2:
                YoYo.with(Techniques.TakingOff).duration(250).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        answer2.setVisibility(View.GONE);
                    }
                }).playOn(answer2);
                break;
            case 3:
                YoYo.with(Techniques.TakingOff).duration(250).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        answer3.setVisibility(View.GONE);
                    }
                }).playOn(answer3);
                break;
            case 4:
                YoYo.with(Techniques.TakingOff).duration(250).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        answer4.setVisibility(View.GONE);
                    }
                }).playOn(answer4);
                break;
        }
    }

    public ArrayList<Integer> getOtherIndices(int a, int b) {
        ArrayList<String> ss = new ArrayList<>();
        ss.add(getNum(1));
        ss.add(getNum(2));
        ss.add(getNum(3));
        ss.add(getNum(4));

        if (a > b) {
            ss.remove(a - 1);
            ss.remove(b - 1);
        }
        if (a < b) {
            ss.remove(b - 1);
            ss.remove(a - 1);
        }

        ArrayList<Integer> wer = new ArrayList<>();
        for (String string : ss) {
            wer.add(getStringNum(string));
        }
        return wer;
    }

    public String getNum(int s) {
        switch (s) {
            case 1:
                return "one";
            case 2:
                return "two";
            case 3:
                return "three";
            case 4:
                return "four";
        }
        return null;
    }

    public int getStringNum(String s) {
        switch (s) {
            case "one":
                return 1;
            case "two":
                return 2;
            case "three":
                return 3;
            case "four":
                return 4;
        }
        return 0;
    }

    /**
     * Show the final button background based on the rightanswer and user choice
     *
     * @param rightAnswer
     * @param userAnswer
     * @param timeUp
     */


    public void showButtonColor(int rightAnswer, int userAnswer, boolean timeUp) {
        timerProgressBar.setVisibility(View.INVISIBLE);
        totalTimeTaken += thisLevelTime;
        countDownTimer.cancel();
        if ((!fiftyFiftyAvailable) && (!sixtySecondsAvailable)) {
            lifelines.setVisibility(View.GONE);
        }
        timerProgressBar.setVisibility(View.INVISIBLE);
        if (!timeUp) {
            if (rightAnswer == userAnswer) {
                hideOtherButtonAnimation(rightAnswer);
                switch (rightAnswer) {
                    case 1:
                        if (currentLevel > 7) {
                            showBtnAnimationRight(answer1, new onAnimationEnd() {
                                @Override
                                public void onEnd() {
                                    showFinalEnding();
                                }
                            });
                        } else {
                            showBtnAnimationRight(answer1, null);
                        }
                        answer1.setBackgroundResource(R.drawable.bg_button_start);
                        break;
                    case 2:
                        if (currentLevel > 7) {
                            showBtnAnimationRight(answer2, new onAnimationEnd() {
                                @Override
                                public void onEnd() {
                                    showFinalEnding();
                                }
                            });
                        } else {
                            showBtnAnimationRight(answer2, null);
                        }
                        answer2.setBackgroundResource(R.drawable.bg_button_start);
                        break;
                    case 3:
                        if (currentLevel > 7) {
                            showBtnAnimationRight(answer3, new onAnimationEnd() {
                                @Override
                                public void onEnd() {
                                    showFinalEnding();
                                }
                            });
                        } else {
                            showBtnAnimationRight(answer3, null);
                        }
                        answer3.setBackgroundResource(R.drawable.bg_button_start);
                        break;
                    case 4:
                        if (currentLevel > 7) {
                            showBtnAnimationRight(answer4, new onAnimationEnd() {
                                @Override
                                public void onEnd() {
                                    showFinalEnding();
                                }
                            });
                        } else {
                            showBtnAnimationRight(answer4, null);
                        }
                        answer4.setBackgroundResource(R.drawable.bg_button_start);
                        break;
                }
            } else {


                ArrayList<Integer> otherAnswers = getOtherIndices(rightAnswer, userAnswer);
                for (int s : otherAnswers) {
                    hideButton(s);
                }


                Log.e("SSSSSSSSSS", otherAnswers.toString());
                switch (rightAnswer) {
                    case 1:
                        if (currentLevel > 7) {
                            showCorrectBtnAnimationOnWrong(answer1, new onAnimationEnd() {
                                @Override
                                public void onEnd() {
                                    showFinalEnding();
                                }
                            });
                        } else {
                            showCorrectBtnAnimationOnWrong(answer1, null);
                        }
                        answer1.setBackgroundResource(R.drawable.bg_button_start);
                        break;
                    case 2:
                        if (currentLevel > 7) {
                            showCorrectBtnAnimationOnWrong(answer2, new onAnimationEnd() {
                                @Override
                                public void onEnd() {
                                    showFinalEnding();
                                }
                            });
                        } else {
                            showCorrectBtnAnimationOnWrong(answer2, null);
                        }
                        answer2.setBackgroundResource(R.drawable.bg_button_start);
                        break;
                    case 3:
                        if (currentLevel > 7) {
                            showCorrectBtnAnimationOnWrong(answer3, new onAnimationEnd() {
                                @Override
                                public void onEnd() {
                                    showFinalEnding();
                                }
                            });
                        } else {
                            showCorrectBtnAnimationOnWrong(answer3, null);
                        }
                        answer3.setBackgroundResource(R.drawable.bg_button_start);
                        break;
                    case 4:
                        if (currentLevel > 7) {
                            showCorrectBtnAnimationOnWrong(answer4, new onAnimationEnd() {
                                @Override
                                public void onEnd() {
                                    showFinalEnding();
                                }
                            });
                        } else {
                            showCorrectBtnAnimationOnWrong(answer4, null);
                        }
                        answer4.setBackgroundResource(R.drawable.bg_button_start);
                        break;
                }


                switch (userAnswer) {
                    case 1:
                        if (currentLevel > 7) {
                            showBtnAnimationWrong(answer1, new onAnimationEnd() {
                                @Override
                                public void onEnd() {
                                    showFinalEnding();
                                }
                            });
                        } else {
                            showBtnAnimationWrong(answer1, null);
                        }
                        answer1.setBackgroundResource(R.drawable.bg_button_wrong);
                        break;
                    case 2:
                        if (currentLevel > 7) {
                            showBtnAnimationWrong(answer2, new onAnimationEnd() {
                                @Override
                                public void onEnd() {
                                    showFinalEnding();
                                }
                            });
                        } else {
                            showBtnAnimationWrong(answer2, null);
                        }
                        answer2.setBackgroundResource(R.drawable.bg_button_wrong);
                        break;
                    case 3:
                        if (currentLevel > 7) {
                            showBtnAnimationWrong(answer3, new onAnimationEnd() {
                                @Override
                                public void onEnd() {
                                    showFinalEnding();
                                }
                            });
                        } else {
                            showBtnAnimationWrong(answer3, null);
                        }
                        answer3.setBackgroundResource(R.drawable.bg_button_wrong);
                        break;
                    case 4:
                        if (currentLevel > 7) {
                            showBtnAnimationWrong(answer4, new onAnimationEnd() {
                                @Override
                                public void onEnd() {
                                    showFinalEnding();
                                }
                            });
                        } else {
                            showBtnAnimationWrong(answer4, null);
                        }
                        answer4.setBackgroundResource(R.drawable.bg_button_wrong);
                        break;
                }
            }
        } else {
            if (currentLevel > 7) {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                    currentLevelAdShown = true;
                }
                showFinalEnding();
            }
            answer1.setBackgroundResource(R.drawable.bg_button_timeout);
            answer2.setBackgroundResource(R.drawable.bg_button_timeout);
            answer3.setBackgroundResource(R.drawable.bg_button_timeout);
            answer4.setBackgroundResource(R.drawable.bg_button_timeout);
        }

        if (currentLevel <= 7) {
            countDownTimerForNextQuestion.start();
            countDownTimerForNextQuestionHasStarted = true;
        }

    }

    public void showFinalEnding() {
        TextView tvYouHaveWon = findViewById(R.id.youHaveWon);
        TextView tvEnergy = findViewById(R.id.tvEnergy);
        LinearLayout money = findViewById(R.id.money);
        LinearLayout bannerResult = findViewById(R.id.resultBanner);
        TextView tvWonCredit = findViewById(R.id.tvWonCredit);
        TextView tvTotalCorrect = findViewById(R.id.tvTotalCorrect);
        tvTotalCorrect.setText("Correct : " + totalCorrectQuestion + "/7");
        final Button restartBtn = findViewById(R.id.restartGameBtn);
        final Button endGameBtn = findViewById(R.id.endGameBtn);
        restartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO:pars
                /*if (ParseUser.getCurrentUser().getInt(PC.KEY_USER_AVAILABLE_ENERGY) >= 10) {
                    final ProgressDialog progressDialog = ProgressDialog.show(GameActivity.this, "Please Wait", "Processing");
                    progressDialog.show();
                    ParseUser.getCurrentUser().increment(PC.KEY_USER_AVAILABLE_ENERGY, -10);
                    ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            finish();
                            Intent intent = new Intent(GameActivity.this, GameActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "You do not have enough energy", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(GameActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();

                }*/

            }
        });
        endGameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        if (musicStarted) {
            if (mySettings.isSoundOn()) {
                Intent intent = new Intent(GameActivity.this, MusicService.class);
                stopService(intent);
            }
        }
        if (totalCorrectQuestion >= 7) {
            if (!gamePaused) {
                PlaySuccessSound();
            }
            //TODO:pars
            /*tvEnergy.setText(getStars() * 10 + "");
            tvTotalCorrect.setTextColor(getResources().getColor(R.color.colorGreen));
            tvWonCredit.setText(getPrizeKoin() + "");
            tvYouHaveWon.setText("SUCCESS");
            bannerResult.setBackgroundColor(getResources().getColor(R.color.colorGreen));
            ParseUser.getCurrentUser().increment(PC.KEY_USER_AVAILABLE_CREDIT, getPrizeKoin());
            ParseUser.getCurrentUser().increment(PC.KEY_USER_TOTAL_CREDIT_WON, getPrizeKoin());
            ParseUser.getCurrentUser().put(PC.KEY_USER_LEVEL, getUserLevel(ParseUser.getCurrentUser().getInt(PC.KEY_USER_TOTAL_CREDIT_WON)));
            ParseUser.getCurrentUser().increment(PC.KEY_USER_AVAILABLE_ENERGY, (getStars() * 10));
            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(GameActivity.this, getPrizeKoin() + " Koin has been added to your account", Toast.LENGTH_LONG).show();
                    }
                }
            });*/
        } else {
            if (!gamePaused) {
                PlaySuccessSound();
            }
            tvEnergy.setText("0");
            tvTotalCorrect.setTextColor(getResources().getColor(R.color.colorRed));
            tvWonCredit.setText(getPrizeKoin() + "");
            tvYouHaveWon.setText("GAME OVER");
            bannerResult.setBackgroundColor(getResources().getColor(R.color.colorRed));
            //TODO:pars
            /*ParseUser.getCurrentUser().increment(PC.KEY_USER_AVAILABLE_CREDIT, getPrizeKoin());
            ParseUser.getCurrentUser().increment(PC.KEY_USER_TOTAL_CREDIT_WON, getPrizeKoin());
            ParseUser.getCurrentUser().put(PC.KEY_USER_LEVEL, getUserLevel(ParseUser.getCurrentUser().getInt(PC.KEY_USER_TOTAL_CREDIT_WON)));
            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(GameActivity.this, getPrizeKoin() + " Koin has been added to your account", Toast.LENGTH_LONG).show();
                    }
                }
            });*/

        }
        showFinal();
        YoYo.with(Techniques.BounceInUp).duration(600).onEnd(new YoYo.AnimatorCallback() {
            @Override
            public void call(Animator animator) {
                ImageView star1 = findViewById(R.id.star1);
                ImageView star2 = findViewById(R.id.star2);
                ImageView star3 = findViewById(R.id.star3);

                if (totalCorrectQuestion >= 7) {
                    showStar(getStars(), star1, star2, star3, new OnYesClicked() {
                        @Override
                        public void onDone() {
                            restartBtn.setVisibility(View.VISIBLE);
                            YoYo.with(Techniques.Landing).duration(700).onEnd(new YoYo.AnimatorCallback() {
                                @Override
                                public void call(Animator animator) {
                                    endGameBtn.setVisibility(View.VISIBLE);
                                    YoYo.with(Techniques.Landing).duration(700).onEnd(new YoYo.AnimatorCallback() {
                                        @Override
                                        public void call(Animator animator) {

                                        }
                                    }).playOn(endGameBtn);
                                }
                            }).playOn(restartBtn);

                        }
                    });
                } else {
                    restartBtn.setVisibility(View.VISIBLE);
                    YoYo.with(Techniques.Landing).duration(700).onEnd(new YoYo.AnimatorCallback() {
                        @Override
                        public void call(Animator animator) {
                            endGameBtn.setVisibility(View.VISIBLE);
                            YoYo.with(Techniques.Landing).duration(700).onEnd(new YoYo.AnimatorCallback() {
                                @Override
                                public void call(Animator animator) {

                                }
                            }).playOn(endGameBtn);
                        }
                    }).playOn(restartBtn);
                }

            }
        }).playOn(finalScreen);

    }

    public int getPrizeKoin() {
        return totalCorrectQuestion;
    }

    public int getStars() {
        if (totalCorrectQuestion > 0 && totalCorrectQuestion <= 5) {
            return 1;
        } else if (totalCorrectQuestion > 5 && totalCorrectQuestion <= 6) {
            if (fiftyFiftyAvailable) {
                return 2;
            } else {
                return 1;
            }
        } else {
            if (fiftyFiftyAvailable) {
                return 3;
            } else {
                return 2;
            }
        }
    }

    public boolean canShowAds() {
        switch (currentLevel) {
            case 1:
                return false;

            case 2:
                return false;

            case 3:
                return false;

            case 4:
                return true;


            case 5:
                switch (totalCorrectQuestion) {
                    case 2:
                        return true;
                    case 4:
                        return true;
                    case 3:
                        return true;

                    default:
                        return false;
                }


            case 6:
                return true;


            case 7:
                switch (totalCorrectQuestion) {
                    case 7:
                        return true;
                    case 6:
                        return true;
                    case 5:
                        return true;
                    case 4:
                        return true;
                    case 3:
                        return true;

                    default:
                        return false;
                }


            case 8:
                return true;


            case 9:
                switch (totalCorrectQuestion) {
                    case 9:
                        return true;
                    case 8:
                        return true;
                    case 7:
                        return true;

                    default:
                        return false;
                }


            case 10:
                switch (totalCorrectQuestion) {
                    case 10:
                        return true;
                    case 9:
                        return true;
                    case 8:
                        return true;
                    case 7:
                        return true;

                    default:
                        return false;
                }

            default:
                return false;
        }


    }

    public int getUserLevel(int creditwon) {
        creditwon += getPrizeKoin();
        if (creditwon > 0 && creditwon <= 50) {
            return 1;
        } else if (creditwon > 50 && creditwon <= 100) {
            return 2;
        } else if (creditwon > 100 && creditwon <= 200) {
            return 3;
        } else if (creditwon > 200 && creditwon <= 350) {
            return 4;
        } else if (creditwon > 350 && creditwon <= 550) {
            return 5;
        } else if (creditwon > 550 && creditwon <= 800) {
            return 6;
        } else if (creditwon > 800 && creditwon <= 1150) {
            return 7;
        } else if (creditwon > 1150 && creditwon <= 1600) {
            return 8;
        } else if (creditwon > 1600 && creditwon <= 2000) {
            return 9;
        } else if (creditwon > 2000 && creditwon <= 3000) {
            return 10;
        } else if (creditwon > 3000 && creditwon <= 5000) {
            return 11;
        } else if (creditwon > 5000 && creditwon <= 8000) {
            return 12;
        } else if (creditwon > 8000 && creditwon <= 13000) {
            return 13;
        } else if (creditwon > 13000 && creditwon <= 20000) {
            return 14;
        } else if (creditwon > 20000 && creditwon <= 30000) {
            return 15;
        } else if (creditwon > 30000 && creditwon <= 50000) {
            return 16;
        } else {
            return 16;
        }
    }

    public void showStar(int x, final ImageView star1, final ImageView star2, final ImageView star3, final OnYesClicked onYesClicked) {
        switch (x) {
            case 1:
                YoYo.with(Techniques.TakingOff).duration(500).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        star1.setImageResource(R.drawable.ic_full_star);
                        YoYo.with(Techniques.Landing).duration(500).onEnd(new YoYo.AnimatorCallback() {
                            @Override
                            public void call(Animator animator) {
                                onYesClicked.onDone();
                            }
                        }).playOn(star1);
                    }
                }).playOn(star1);
                break;
            case 2:
                YoYo.with(Techniques.TakingOff).duration(500).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        star1.setImageResource(R.drawable.ic_full_star);
                        YoYo.with(Techniques.Landing).duration(500).onEnd(new YoYo.AnimatorCallback() {
                            @Override
                            public void call(Animator animator) {
                                YoYo.with(Techniques.TakingOff).duration(500).onEnd(new YoYo.AnimatorCallback() {
                                    @Override
                                    public void call(Animator animator) {
                                        star2.setImageResource(R.drawable.ic_full_star);
                                        YoYo.with(Techniques.Landing).duration(500).onEnd(new YoYo.AnimatorCallback() {
                                            @Override
                                            public void call(Animator animator) {
                                                onYesClicked.onDone();
                                            }
                                        }).playOn(star2);
                                    }
                                }).playOn(star2);
                            }
                        }).playOn(star1);
                    }
                }).playOn(star1);
                break;
            case 3:
                YoYo.with(Techniques.TakingOff).duration(500).onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        star1.setImageResource(R.drawable.ic_full_star);
                        YoYo.with(Techniques.Landing).duration(500).onEnd(new YoYo.AnimatorCallback() {
                            @Override
                            public void call(Animator animator) {
                                YoYo.with(Techniques.TakingOff).duration(500).onEnd(new YoYo.AnimatorCallback() {
                                    @Override
                                    public void call(Animator animator) {
                                        star2.setImageResource(R.drawable.ic_full_star);
                                        YoYo.with(Techniques.Landing).duration(500).onEnd(new YoYo.AnimatorCallback() {
                                            @Override
                                            public void call(Animator animator) {
                                                YoYo.with(Techniques.TakingOff).duration(500).onEnd(new YoYo.AnimatorCallback() {
                                                    @Override
                                                    public void call(Animator animator) {
                                                        star3.setImageResource(R.drawable.ic_full_star);
                                                        YoYo.with(Techniques.Landing).duration(500).onEnd(new YoYo.AnimatorCallback() {
                                                            @Override
                                                            public void call(Animator animator) {
                                                                onYesClicked.onDone();
                                                            }
                                                        }).playOn(star3);
                                                    }
                                                }).playOn(star3);
                                            }
                                        }).playOn(star2);
                                    }
                                }).playOn(star2);
                            }
                        }).playOn(star1);
                    }
                }).playOn(star1);
                break;
        }
    }

    /**
     * Collects questions from server
     */

    public void collectQuestionsFromServer() {

        FirebaseUser cur_user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference user_db = FirebaseDatabase.getInstance().getReference("user_qnList");
        final DatabaseReference qn_db = FirebaseDatabase.getInstance().getReference("questions");

        user_db.child("SCW0nWF1qXfddxfbhN57GFXvPcW2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<String> user_qnlist = (List<String>) dataSnapshot.getValue();
                if (user_qnlist != null) {
                    if (user_qnlist.size() != 0) {
                        qn_db.orderByChild("totalRequest").limitToFirst(11).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                List<Question> qnList = new ArrayList<>();
                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                     if(!user_qnlist.contains(postSnapshot.getKey())){
                                        qnList.add(postSnapshot.getValue(Question.class));
                                        Log.d("tag",postSnapshot.getValue(Question.class).getQuestion());

                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*
        ArrayList<String> questions = (ArrayList<String>) ParseUser.getCurrentUser().get(PC.KEY_QUESTIONS_ID);

        ParseQuery<ParseObject> query = ParseQuery.getQuery(PC.KEY_OBJECT_QUESTION);
        String title=getIntent().getStringExtra("title");
            query.whereEqualTo("Title", title);
        if (questions != null) {
            if (questions.size() != 0) {
                query.whereNotContainedIn("objectId", questions);
            }
        }
        query.addAscendingOrder(PC.KEY_QUIZ_TOTAL_REQUEST);
        query.setLimit(11);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> quizList, ParseException e) {
                if (e == null) {
                    ArrayList<String> objectIds = new ArrayList<>();
                    if (quizList.size() != 0) {
                        for (ParseObject eachQ : quizList) {
                            questionList.add(getQuestionFromParseObject(eachQ));
                            questionIdList.add(eachQ.getObjectId());
                            eachQ.increment(PC.KEY_QUIZ_TOTAL_REQUEST);
                            eachQ.saveInBackground();
                            objectIds.add(eachQ.getObjectId());
                        }
                        Collections.shuffle(questionList);
                        ParseUser.getCurrentUser().increment(PC.KEY_USER_TOTAL_GAME);
                        ParseUser.getCurrentUser().addAll(PC.KEY_QUESTIONS_ID, objectIds);
                        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                showMainScreen();

                                *//*

                                String rulePref = "Rules";
                                final String showagain = "dontshowAgain";

                                SharedPreferences sharedPreferences = getSharedPreferences(rulePref, Context.MODE_PRIVATE);
                                final SharedPreferences.Editor editor = sharedPreferences.edit();

                                if (!sharedPreferences.getBoolean(showagain, false)) {

                                    AlertDialog.Builder builder;
                                    builder = new AlertDialog.Builder(GameActivity.this);

                                    LayoutInflater inflater = getLayoutInflater();
                                    View dialogView = inflater.inflate(R.layout.alert_game_rules,
                                            null);
                                    final CheckBox checkBox = (CheckBox) dialogView.findViewById(R.id.dontShowAgainvb);


                                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    if (checkBox.isChecked()) {
                                                        editor.putBoolean(showagain, true);
                                                        editor.commit();
                                                    }
                                                    break;

                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    break;
                                            }
                                        }
                                    };
                                    builder.setView(dialogView);
                                    builder.setCancelable(false);
                                    builder.setPositiveButton("Ok", dialogClickListener).show();
                                }

                                *//*
                            }
                        });
                    } else {
                        *//*
                        if (ParseUser.getCurrentUser().getInt(PC.KEY_USER_REPEAT_QUESTION) >= 1) {

                            String fullname=ParseUser.getCurrentUser().getString(PC.KEY_USER_FIRST_NAME)+" "+ParseUser.getCurrentUser().getString(PC.KEY_USER_LAST_NAME);
                            showYesNoDialog("Hello "+fullname+",\nYou have completed all the questions of our database 2 times. Please wait for few days while we enter more questions to our database.", "Ok", "cancel", new OnYesClicked() {
                                @Override
                                public void onDone() {

                                    Intent intent = new Intent(GameActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();

                                }
                            }, new OnNoClicked() {
                                @Override
                                public void onDone() {

                                    Intent intent = new Intent(GameActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();

                                }
                            });


                        } else {


*//*
                            ArrayList<String> ss = new ArrayList<>();
                            ss.add("ss");
                            int current = ParseUser.getCurrentUser().getInt(PC.KEY_USER_REPEAT_QUESTION);

                            ParseUser.getCurrentUser().put(PC.KEY_USER_REPEAT_QUESTION, (current + 1));
                            ParseUser.getCurrentUser().put(PC.KEY_QUESTIONS_ID, ss);
                            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    collectQuestionsFromServer();
                                }
                            });


                        *//*
                        }
                        *//*
                    }
                } else {
                    if (e.getCode() == ParseException.CONNECTION_FAILED) {
                        showYesNoDialog("Unable to connect to internet!. Do you want to connect again?", "Retry", "Go Back", new OnYesClicked() {
                            @Override
                            public void onDone() {
                                collectQuestionsFromServer();
                            }
                        }, new OnNoClicked() {
                            @Override
                            public void onDone() {
                                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else if (e.getCode() == ParseException.INVALID_SESSION_TOKEN) {
                        Toast.makeText(getApplicationContext(), "Please log out and log in to play", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(GameActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(getApplicationContext(), "Error : "+e.getMessage(), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(GameActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });*/
    }

    /*

    public void collectQuestionsFromServer(final int i) {
        if (i >= 19) {
            Collections.shuffle(questionList);
            ParseUser.getCurrentUser().increment(PC.KEY_USER_TOTAL_GAME);
            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (mySettings.isSoundOn()) {
                        intorMediaPlayer.stop();
                        intorMediaPlayer.release();
                    }
                    showMainScreen();
                }
            });
        } else {
            ArrayList<String> questions = (ArrayList<String>) ParseUser.getCurrentUser().get(PC.KEY_QUESTIONS_ID);

            ParseQuery<ParseObject> query = ParseQuery.getQuery(PC.KEY_OBJECT_QUESTION);
            query.whereEqualTo(PC.KEY_QUIZ_CATEGORY, "category" + i);
            if (questions != null) {
                if (questions.size() != 0) {
                    query.whereNotContainedIn("objectId", questions);
                }
            }
            query.addAscendingOrder(PC.KEY_QUIZ_TOTAL_REQUEST);
            query.setLimit(1);
            query.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> quizList, ParseException e) {
                    if (e == null) {
                        if (quizList.size() != 0) {
                            ParseObject eachQ = quizList.get(0);
                            questionList.add(getQuestionFromParseObject(eachQ));
                            questionIdList.add(eachQ.getObjectId());
                            eachQ.increment(PC.KEY_QUIZ_TOTAL_REQUEST);
                            eachQ.saveInBackground();
                            ParseUser.getCurrentUser().add(PC.KEY_QUESTIONS_ID, eachQ.getObjectId());
                            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    collectQuestionsFromServer(i + 1);
                                }
                            });
                        } else {
                            ArrayList<String> ss = new ArrayList<>();
                            ss.add("ss");
                            ParseUser.getCurrentUser().put(PC.KEY_QUESTIONS_ID, ss);
                            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    collectQuestionsFromServer(i);
                                }
                            });
                        }
                    } else {
                        if (e.getCode() == ParseException.CONNECTION_FAILED) {
                            showYesNoDialog("Unable to connect to internet!. Do you want to connect again?", "Retry", "Go Back", new OnYesClicked() {
                                @Override
                                public void onDone() {
                                    collectQuestionsFromServer(i);
                                }
                            }, new OnNoClicked() {
                                @Override
                                public void onDone() {
                                    Intent intent = new Intent(GameActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    }
                }
            });
        }
    }
    */

    /**
     * Retrieves Question from ParseObject
     *
     * @param parseQuestion
     * @return
     */

    public Question getQuestionFromParseObject(ParseObject parseQuestion) {
        Question question = new Question();
        question.setCategory(Integer.parseInt(parseQuestion.getString(PC.KEY_QUIZ_CATEGORY).replace("category", "")));
        question.setLevel(Integer.parseInt(parseQuestion.getString(PC.KEY_QUIZ_LEVEL).replace("level", "")));
        question.setQuestion(parseQuestion.getString(PC.KEY_QUIZ_QUESTION));
        question.setRightAnswer(parseQuestion.getString(PC.KEY_QUIZ_RIGHT_ANSWER));

        ArrayList<String> wronganswerList = new ArrayList<>();
        wronganswerList.add(parseQuestion.getString(PC.KEY_QUIZ_WRONG_ANSWER1));
        wronganswerList.add(parseQuestion.getString(PC.KEY_QUIZ_WRONG_ANSWER2));
        wronganswerList.add(parseQuestion.getString(PC.KEY_QUIZ_WRONG_ANSWER3));

        question.setWronganswer(wronganswerList);
        return question;
    }

    public void setCorrectProgress(int x) {
        switch (x) {
            case 0:
                lin1.setBackgroundResource(R.drawable.bg_left_rounded_unactive);
                lin2.setBackgroundColor(getResources().getColor(R.color.cardAlternateBackground));
                lin3.setBackgroundColor(getResources().getColor(R.color.cardAlternateBackground));
                lin4.setBackgroundColor(getResources().getColor(R.color.cardAlternateBackground));
                lin5.setBackgroundColor(getResources().getColor(R.color.cardAlternateBackground));
                lin6.setBackgroundColor(getResources().getColor(R.color.cardAlternateBackground));
                lin7.setBackgroundResource(R.drawable.bg_right_rounded_unactive);
                break;
            case 1:
                lin1.setBackgroundResource(R.drawable.bg_left_rounded_active);
                lin2.setBackgroundColor(getResources().getColor(R.color.cardAlternateBackground));
                lin3.setBackgroundColor(getResources().getColor(R.color.cardAlternateBackground));
                lin4.setBackgroundColor(getResources().getColor(R.color.cardAlternateBackground));
                lin5.setBackgroundColor(getResources().getColor(R.color.cardAlternateBackground));
                lin6.setBackgroundColor(getResources().getColor(R.color.cardAlternateBackground));
                lin7.setBackgroundResource(R.drawable.bg_right_rounded_unactive);
                break;
            case 2:
                lin1.setBackgroundResource(R.drawable.bg_left_rounded_active);
                lin2.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                lin3.setBackgroundColor(getResources().getColor(R.color.cardAlternateBackground));
                lin4.setBackgroundColor(getResources().getColor(R.color.cardAlternateBackground));
                lin5.setBackgroundColor(getResources().getColor(R.color.cardAlternateBackground));
                lin6.setBackgroundColor(getResources().getColor(R.color.cardAlternateBackground));
                lin7.setBackgroundResource(R.drawable.bg_right_rounded_unactive);
                break;
            case 3:
                lin1.setBackgroundResource(R.drawable.bg_left_rounded_active);
                lin2.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                lin3.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                lin4.setBackgroundColor(getResources().getColor(R.color.cardAlternateBackground));
                lin5.setBackgroundColor(getResources().getColor(R.color.cardAlternateBackground));
                lin6.setBackgroundColor(getResources().getColor(R.color.cardAlternateBackground));
                lin7.setBackgroundResource(R.drawable.bg_right_rounded_unactive);
                break;
            case 4:
                lin1.setBackgroundResource(R.drawable.bg_left_rounded_active);
                lin2.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                lin3.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                lin4.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                lin5.setBackgroundColor(getResources().getColor(R.color.cardAlternateBackground));
                lin6.setBackgroundColor(getResources().getColor(R.color.cardAlternateBackground));
                lin7.setBackgroundResource(R.drawable.bg_right_rounded_unactive);
                break;
            case 5:
                lin1.setBackgroundResource(R.drawable.bg_left_rounded_active);
                lin2.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                lin3.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                lin4.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                lin5.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                lin6.setBackgroundColor(getResources().getColor(R.color.cardAlternateBackground));
                lin7.setBackgroundResource(R.drawable.bg_right_rounded_unactive);
                break;
            case 6:
                lin1.setBackgroundResource(R.drawable.bg_left_rounded_active);
                lin2.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                lin3.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                lin4.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                lin5.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                lin6.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                lin7.setBackgroundResource(R.drawable.bg_right_rounded_unactive);
                break;
            case 7:
                lin1.setBackgroundResource(R.drawable.bg_left_rounded_active);
                lin2.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                lin3.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                lin4.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                lin5.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                lin6.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                lin7.setBackgroundResource(R.drawable.bg_right_rounded_active);
                break;
        }
    }
    public class CustomComparator implements Comparator<User> {
        @Override
        public int compare(User o1, User o2) {
            return o2.getTotal_credit_won()-o1.getTotal_credit_won();
        }
    }

}
