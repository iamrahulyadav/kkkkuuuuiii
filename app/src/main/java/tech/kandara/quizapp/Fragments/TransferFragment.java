package tech.kandara.quizapp.Fragments;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.ldoublem.loadingviewlib.view.LVBlock;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tech.kandara.quizapp.Adapter.TransferAdapter;
import tech.kandara.quizapp.MainActivity;
import tech.kandara.quizapp.PC;
import tech.kandara.quizapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransferFragment extends Fragment {


    private AdView mAdView;
    String cells[] = {"Ncell", "NTC"};
    String amounts[] = {"Rs. 50", "Rs. 100"};
    Spinner cellSpinner;
    Spinner amountSpinner;
    Button requestBtn;
    EditText phNumberField;
    RelativeLayout loadingScreen, otherScreen;
    ListView listView;
    int debited = 2000;
    private CallbackManager callbackManager;
    private LoginManager loginManager;
    int RC_SIGN_IN = 2304;
    GoogleApiClient mGoogleApiClient;

    private InterstitialAd mInterstitialAd;

    public TransferFragment() {
    }


    public void loadInterstitialAd() {
        mInterstitialAd.loadAd(new AdRequest.Builder()
                .addTestDevice("9339BF107335CB968B9CFC2FE448DC84").build());
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_transfer, container, false);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mInterstitialAd = new InterstitialAd(getActivity());
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

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .enableAutoManage(getActivity() /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        }
                    })
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }
        callbackManager = CallbackManager.Factory.create();
        mAdView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("9339BF107335CB968B9CFC2FE448DC84").build();
        mAdView.loadAd(adRequest);
        LVBlock lvBlock = view.findViewById(R.id.lv_block);
        lvBlock.startAnim();
        cellSpinner = view.findViewById(R.id.cellSpinner);
        amountSpinner = view.findViewById(R.id.transferAmount);
        listView = view.findViewById(R.id.listView);
        requestBtn = view.findViewById(R.id.makeTransferBtn);
        phNumberField = view.findViewById(R.id.phoneNumber);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (ParseUser.getCurrentUser().getString(PC.KEY_USER_PHONE_NUMBER) != null) {
            phNumberField.setText(ParseUser.getCurrentUser().getString(PC.KEY_USER_PHONE_NUMBER));
        }
        loadingScreen = view.findViewById(R.id.loadingScreen);
        otherScreen = view.findViewById(R.id.otherScreen);
        ArrayAdapter cellAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, cells);
        final ArrayAdapter amountAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, amounts);
        cellSpinner.setAdapter(cellAdapter);
        amountSpinner.setAdapter(amountAdapter);
        queryTransfer();

        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (!phNumberField.getText().toString().isEmpty()) {
                    if ((ParseUser.getCurrentUser().getBoolean(PC.KEY_USER_FACEBOOK_LINKED) || ParseUser.getCurrentUser().getBoolean(PC.KEY_USER_GOOGLEPLUS_LINKED))) {

                        switch (amountSpinner.getSelectedItemPosition()) {
                            case 0:
                                debited = 1000;
                                break;

                            case 1:
                                debited = 2000;
                                break;

                        }
                        if (mInterstitialAd.isLoaded()) {
                            mInterstitialAd.show();
                        }

                        if (ParseUser.getCurrentUser().getInt(PC.KEY_USER_AVAILABLE_CREDIT) >= debited) {

                            showYesNoDialog("Post", "I dont want money", new OnYesClicked() {
                                @Override
                                public void onDone() {
                                    FacebookSdk.sdkInitialize(getActivity());

                                    callbackManager = CallbackManager.Factory.create();

                                    List<String> permissionNeeds = Arrays.asList("publish_actions");
                                    loginManager = LoginManager.getInstance();

                                    loginManager.logInWithPublishPermissions(getActivity(), permissionNeeds);

                                    loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                                        @Override
                                        public void onSuccess(LoginResult loginResult) {
                                            showLoading();
                                            Bitmap image;
                                            int third = 1000;
                                            int fourth = 2000;
                                            if (debited == third) {
                                                image = BitmapFactory.decodeResource(getResources(), R.drawable.iwon50);
                                            } else if (debited == fourth) {

                                                image = BitmapFactory.decodeResource(getResources(), R.drawable.iwon100);
                                            } else {

                                                image = BitmapFactory.decodeResource(getResources(), R.drawable.iwon50);
                                            }
                                            SharePhoto photo = new SharePhoto.Builder()
                                                    .setBitmap(image)
                                                    .setCaption("I have just won Rs. " + (debited / 20) + " playing #kuiz\nDownload Kuiz to Learn and Earn\nhttps://play.google.com/store/apps/details?id=tech.kandara.quizapp")
                                                    .build();

                                            SharePhotoContent content = new SharePhotoContent.Builder()
                                                    .addPhoto(photo)

                                                    .build();

                                            ShareApi.share(content, new FacebookCallback<Sharer.Result>() {
                                                @Override
                                                public void onSuccess(final Sharer.Result result) {

                                                    if (ParseUser.getCurrentUser().getInt(PC.KEY_USER_AVAILABLE_CREDIT) >= debited) {
                                                        showLoading();

                                                        ParseUser.getCurrentUser().increment(PC.KEY_USER_AVAILABLE_CREDIT, -debited);
                                                        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                                                            @Override
                                                            public void done(ParseException e) {

                                                                if (e == null) {
                                                                    if (ParseUser.getCurrentUser().getInt(PC.KEY_USER_AVAILABLE_CREDIT) > 0) {

                                                                        ParseObject transfer = new ParseObject(PC.KEY_TRANSFER_OBJECT);
                                                                        transfer.put(PC.KEY_TRANSFER_POST_URL, result.getPostId());
                                                                        transfer.put(PC.KEY_TRANSFER_USER_ID, ParseUser.getCurrentUser().getObjectId());
                                                                        transfer.put(PC.KEY_TRANSFER_PHONE_NUMBER, phNumberField.getText().toString());
                                                                        transfer.put(PC.KEY_TRANSFER_IS_PROCESSED, false);
                                                                        transfer.put(PC.KEY_TRANSFER_IS_REJECTED, false);
                                                                        transfer.put(PC.KEY_TRANSFER_CELL, cells[cellSpinner.getSelectedItemPosition()]);
                                                                        transfer.put(PC.KEY_TRANSFER_AMOUNT, amounts[amountSpinner.getSelectedItemPosition()]);
                                                                        transfer.saveInBackground(new SaveCallback() {
                                                                            @Override
                                                                            public void done(ParseException e) {

                                                                                if (e == null) {

                                                                                    Toast.makeText(getActivity(), "Your trasfer has been requested!", Toast.LENGTH_LONG).show();
                                                                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                    startActivity(intent);
                                                                                    getActivity().finish();
                                                                                } else {
                                                                                    if (e.getCode() == ParseException.CONNECTION_FAILED) {
                                                                                        Toast.makeText(getActivity(), "Connection Failed!\nPlease Try Again", Toast.LENGTH_LONG).show();
                                                                                    } else {
                                                                                        Toast.makeText(getActivity(), "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                    hideLoading();
                                                                                }
                                                                            }
                                                                        });
                                                                    } else {
                                                                        ParseUser.getCurrentUser().increment(PC.KEY_USER_AVAILABLE_CREDIT, debited);
                                                                        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                                                                            @Override
                                                                            public void done(ParseException e) {
                                                                                Toast.makeText(getActivity(), "You do not have enough Koins", Toast.LENGTH_LONG).show();
                                                                            }
                                                                        });
                                                                    }
                                                                } else {
                                                                    if (mInterstitialAd.isLoaded()) {
                                                                        mInterstitialAd.show();
                                                                    }
                                                                    Toast.makeText(getActivity(), "You do not have enough Koins", Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });


                                                    } else {

                                                        hideLoading();
                                                        Toast.makeText(getActivity(), "You do not have enough Koin to make this request", Toast.LENGTH_LONG).show();
                                                    }
                                                }

                                                @Override
                                                public void onCancel() {
                                                    hideLoading();
                                                    Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG).show();
                                                }

                                                @Override
                                                public void onError(FacebookException error) {
                                                    hideLoading();
                                                    Toast.makeText(getActivity(), "Cancelled " + error.getMessage(), Toast.LENGTH_LONG).show();

                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancel() {
                                            Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG).show();

                                        }

                                        @Override
                                        public void onError(FacebookException error) {
                                            Toast.makeText(getActivity(), "Cancelled " + error.getMessage(), Toast.LENGTH_LONG).show();

                                        }
                                    });


                                }


                            }, null);


                        } else {
                            Toast.makeText(getActivity(), "You do not have enough Koin to make this transfer", Toast.LENGTH_LONG).show();

                        }


                    } else {

                        Dialog builder;
                        builder = new Dialog(getActivity());

                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.alert_social_login,
                                null);
                        builder.setContentView(dialogView);


                        Button fbLoginBtn = dialogView.findViewById(R.id.fb);
                        Button gplusLoginBtn = dialogView.findViewById(R.id.gplus);


                        gplusLoginBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                                startActivityForResult(signInIntent, RC_SIGN_IN);
                            }
                        });

                        fbLoginBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {


                                loginManager = LoginManager.getInstance();
                                loginManager.logInWithReadPermissions(getActivity(), Arrays.asList(
                                        "public_profile", "email"));
                                loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                                    @Override
                                    public void onSuccess(LoginResult loginResult) {
                                        // App code
                                        GraphRequest request = GraphRequest.newMeRequest(
                                                loginResult.getAccessToken(),
                                                new GraphRequest.GraphJSONObjectCallback() {
                                                    @Override
                                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                                        Log.e("DETAILS", object.toString());
                                                        try {
                                                            final String id = object.getString("id");
                                                            String firstName = object.getString("first_name");
                                                            String lastName = object.getString("last_name");
                                                            String photoLink = "https://graph.facebook.com/" + id + "/picture?type=large";
                                                            JSONObject timeRange = object.getJSONObject("age_range");
                                                            String gender = object.getString("gender");
                                                            int age = 0;
                                                            int minAge = 0, maxAge = 0;
                                                            if (timeRange.has("min")) {
                                                                minAge = timeRange.getInt("min");
                                                            }
                                                            if (timeRange.has("max")) {
                                                                maxAge = timeRange.getInt("max");
                                                            }
                                                            if (minAge == 0 && maxAge == 0) {
                                                                age = 0;
                                                            } else if (minAge != 0 && maxAge == 0) {
                                                                age = minAge + 2;
                                                            } else if (minAge == 0 && maxAge != 0) {
                                                                age = maxAge - 2;
                                                            } else if (minAge != 0 && maxAge != 0) {
                                                                age = (minAge + maxAge) / 2;
                                                            }


                                                            final ParseUser currentUser = ParseUser.getCurrentUser();
                                                            currentUser.setUsername(id);
                                                            currentUser.setEmail(id + "@noemail.com");
                                                            currentUser.setPassword("QuizApp");
                                                            currentUser.put(PC.KEY_USER_LAST_NAME, lastName);
                                                            currentUser.put(PC.KEY_USER_FIRST_NAME, firstName);
                                                            currentUser.put(PC.KEY_USER_AGE, age);
                                                            currentUser.put(PC.KEY_USER_FACEBOOK_LINKED, true);
                                                            currentUser.put(PC.KEY_USER_GENDER, gender);
                                                            currentUser.put(PC.KEY_USER_PHOTOLINK, photoLink);
                                                            currentUser.saveInBackground(new SaveCallback() {
                                                                @Override
                                                                public void done(ParseException e) {

                                                                    if (e != null) {
                                                                        Log.e("Sign Up Error", e.getMessage());
                                                                        if ((e.getCode() == 202) || (e.getCode() == ParseException.INVALID_SESSION_TOKEN)) {
                                                                            ParseUser.getCurrentUser().deleteInBackground(new DeleteCallback() {
                                                                                @Override
                                                                                public void done(ParseException e) {
                                                                                    ParseUser.logInInBackground(id, "QuizApp", new LogInCallback() {
                                                                                        @Override
                                                                                        public void done(ParseUser user, ParseException e) {
                                                                                            if (e != null) {
                                                                                                if (e.getCode() == ParseException.CONNECTION_FAILED) {
                                                                                                    Toast.makeText(getActivity(), "Connection Failed!\nPlease Try Again", Toast.LENGTH_LONG).show();
                                                                                                }

                                                                                            } else {
                                                                                                getActivity().finish();
                                                                                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                                                                                intent.putExtra(PC.GUEST_MODE, true);
                                                                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                                startActivity(intent);
                                                                                            }
                                                                                        }
                                                                                    });
                                                                                }
                                                                            });

                                                                        } else if (e.getCode() == ParseException.CONNECTION_FAILED) {
                                                                            Toast.makeText(getActivity(), "Connection Failed!\nPlease Try Again", Toast.LENGTH_LONG).show();
                                                                        }
                                                                    } else {
                                                                        getActivity().finish();
                                                                        Intent intent = new Intent(getActivity(), MainActivity.class);
                                                                        intent.putExtra(PC.GUEST_MODE, true);
                                                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                        startActivity(intent);
                                                                    }
                                                                }
                                                            });


                                                        } catch (JSONException e) {
                                                            Log.e("Json Error", e.getMessage());
                                                        }
                                                    }


                                                }


                                        );
                                        Bundle parameters = new Bundle();
                                        parameters.putString("fields", "id,cover,name,first_name,last_name,age_range,link,gender,picture,updated_time,verified");
                                        request.setParameters(parameters);
                                        request.executeAsync();
                                    }

                                    @Override
                                    public void onCancel() {
                                        Log.e("SS", "Cancelled");
                                    }

                                    @Override
                                    public void onError(FacebookException exception) {
                                        Log.e("Error", exception.getMessage());
                                    }
                                });
                            }
                        });

                        builder.show();
                        Toast.makeText(getActivity(), "Please login with facebook or google plus to withdraw your Koin", Toast.LENGTH_LONG).show();
                    }
                } else {
                    hideLoading();
                    phNumberField.setError("Please enter this field!");

                }
            }
        });
        return view;
    }

    private void queryTransfer() {
        ParseQuery query = new ParseQuery(PC.KEY_TRANSFER_OBJECT);
        query.setLimit(20);
        query.addDescendingOrder("createdAt");
        query.whereEqualTo(PC.KEY_TRANSFER_USER_ID, ParseUser.getCurrentUser().getObjectId());
        showLoading();
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                hideLoading();
                if (e == null) {
                    Log.e("Transfer Size", objects.size() + "");
                    TransferAdapter adapter = new TransferAdapter((ArrayList<ParseObject>) objects, getActivity());
                    listView.setAdapter(adapter);
                } else {
                    if (e.getCode() == ParseException.CONNECTION_FAILED) {
                        Toast.makeText(getActivity(), "Connection Failed!\nPlease Try Again", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }


    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            final GoogleSignInAccount acct = result.getSignInAccount();
            final ParseUser currentUser = ParseUser.getCurrentUser();
            currentUser.setUsername(acct.getId());
            currentUser.setEmail(acct.getEmail());
            currentUser.setPassword("QuizApp");
            currentUser.put(PC.KEY_USER_LAST_NAME, acct.getFamilyName());
            currentUser.put(PC.KEY_USER_FIRST_NAME, acct.getGivenName());
            currentUser.put(PC.KEY_USER_GOOGLEPLUS_LINKED, true);
            currentUser.put(PC.KEY_USER_PHOTOLINK, acct.getPhotoUrl().toString());
            currentUser.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {

                    if (e != null) {
                        Log.e("Sign Up Error", e.getMessage());
                        if ((e.getCode() == 202) || (e.getCode() == ParseException.INVALID_SESSION_TOKEN)) {
                            ParseUser.getCurrentUser().deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    ParseUser.logInInBackground(acct.getId(), "QuizApp", new LogInCallback() {
                                        @Override
                                        public void done(ParseUser user, ParseException e) {
                                            if (e != null) {
                                                if (e.getCode() == ParseException.CONNECTION_FAILED) {
                                                    Toast.makeText(getActivity(), "Connection Failed!\nPlease Try Again", Toast.LENGTH_LONG).show();
                                                }

                                            } else {
                                                getActivity().finish();
                                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                                intent.putExtra(PC.GUEST_MODE, true);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                            }
                                        }
                                    });
                                }
                            });

                        } else if (e.getCode() == ParseException.CONNECTION_FAILED) {
                            Toast.makeText(getActivity(), "Connection Failed!\nPlease Try Again", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        getActivity().finish();
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        intent.putExtra(PC.GUEST_MODE, true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }
            });
        } else {
            Toast.makeText(getActivity(), "Could not sign into google", Toast.LENGTH_LONG).show();
        }
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
     * @param yes
     * @param no
     * @param onYesClicked
     * @param onNoClicked
     */

    public void showYesNoDialog(String yes, String no, final OnYesClicked onYesClicked, final OnNoClicked onNoClicked) {


        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_transfer_request,
                null);

        final CheckBox checkBox = dialogView.findViewById(R.id.termsCheckbox);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (checkBox.isChecked()) {
                            onYesClicked.onDone();
                        } else {
                            Toast.makeText(getActivity(), "Please agree to terms and conditions first!", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        if (onNoClicked != null) {
                            onNoClicked.onDone();
                        }
                        break;
                }
            }
        };
        builder.setCancelable(false);
        builder.setView(dialogView);
        builder.setPositiveButton(yes, dialogClickListener)
                .setNegativeButton(no, dialogClickListener).show();
    }

    public void showYesNoDialog2(String title, String yes, String no, final OnYesClicked onYesClicked, final OnNoClicked onNoClicked) {


        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_show,
                null);

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
        builder.setView(dialogView);
        builder.setCancelable(false);
        builder.setPositiveButton(yes, dialogClickListener)
                .show();
    }


    public void showLoading() {
        loadingScreen.setVisibility(View.VISIBLE);
        otherScreen.setVisibility(View.GONE);
    }

    public void hideLoading() {
        loadingScreen.setVisibility(View.GONE);
        otherScreen.setVisibility(View.VISIBLE);
    }

}
