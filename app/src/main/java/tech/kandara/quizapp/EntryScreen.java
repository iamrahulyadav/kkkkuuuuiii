package tech.kandara.quizapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ldoublem.loadingviewlib.view.LVBlazeWood;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tech.kandara.quizapp.Fragments.TransferFragment;
import tech.kandara.quizapp.Library.utils.GuestIdGenerator;

public class EntryScreen extends AppCompatActivity {

    CallbackManager callbackManager;
    RelativeLayout loadingScreen;
    RelativeLayout mainScreen;
    LVBlazeWood lvBlock;
    Button guestMode;
    LoginManager loginManager;
    Button facebookLoginBtn;
    Button googlePlusBtn;
    GoogleApiClient mGoogleApiClient;
    int RC_SIGN_IN = 2304;

    FirebaseAuth auth;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            navigateToHome();
        }


        setContentView(R.layout.activity_entry_screen);
        callbackManager = CallbackManager.Factory.create();
        googlePlusBtn = findViewById(R.id.gplusBtn);
        googlePlusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading();
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
        facebookLoginBtn = findViewById(R.id.fb);
        guestMode = findViewById(R.id.guestMode);
        guestMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showLoading();
                signUpGuestUser(new SignUpListener() {
                    @Override
                    public void signedUp() {
                        Intent intent = new Intent(EntryScreen.this, MainActivity.class);
                        intent.putExtra(PC.GUEST_MODE, true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
        lvBlock = findViewById(R.id.lv_block);
        //Loading Animation
        lvBlock.startAnim();
        loadingScreen = findViewById(R.id.loadingScreen);
        mainScreen = findViewById(R.id.otherScreen);
        facebookLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading();
                loginManager = LoginManager.getInstance();

                loginManager.logInWithReadPermissions(EntryScreen.this, Arrays.asList(
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
                                            final String firstName = object.getString("first_name");
                                            final String lastName = object.getString("last_name");
                                            final String photoLink = "https://graph.facebook.com/" + id + "/picture?type=large";


                                            auth.createUserWithEmailAndPassword(id + "@noemail.com", "QuizApp")
                                                    .addOnCompleteListener(EntryScreen.this, new OnCompleteListener<AuthResult>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                                            if (!task.isSuccessful()) {
                                                                Toast.makeText(EntryScreen.this, "Connection Failed!\nPlease Try Again",
                                                                        Toast.LENGTH_SHORT).show();
                                                                showMainScreen();
                                                                Log.d("fail", "fail:" + task.getException());
                                                            } else {
                                                                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
                                                                FirebaseDatabase.getInstance().getReference("app_title").setValue("Kuiz");
                                                                mFirebaseDatabase = FirebaseDatabase.getInstance().getReference("users");
                                                                mFirebaseDatabase.keepSynced(true);
                                                                auth.getCurrentUser();
                                                                User user = new User(auth.getCurrentUser().getEmail(), firstName, lastName,
                                                                        photoLink, 0, 20, 0, 0,
                                                                        1, false, true, false);
                                                                mFirebaseDatabase.child(auth.getCurrentUser().getUid()).setValue(user);

                                                                navigateToHome();
                                                            }
                                                        }
                                                    });


                                        } catch (JSONException e) {
                                            Log.e("Json Error", e.getMessage());
                                        }
                                    }


                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,cover,name,first_name,last_name,age_range,link,gender,picture,updated_time,verified");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        showMainScreen();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        showMainScreen();
                        Log.e("Error", exception.getMessage());
                        // App code
                    }
                });
            }
        });

    }

    public void showReferralDialog(String yes, String no, final TransferFragment.OnYesClicked onYesClicked, final TransferFragment.OnNoClicked onNoClicked) {


        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(EntryScreen.this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_referral,
                null);

        final EditText refCode = dialogView.findViewById(R.id.refCodeField);

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (!refCode.getText().toString().isEmpty()) {
                            String code = refCode.getText().toString();
                            mFirebaseInstance = FirebaseDatabase.getInstance();
                            mFirebaseDatabase = mFirebaseInstance.getReference();
                            Query query = mFirebaseDatabase.child("users").orderByChild("referral_code");
                            query.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                                        if (!postSnapshot.getValue(User.class).getEmail().equals("50822029580993@noemail.com")) {
                                            Toast.makeText(EntryScreen.this, "No such referral code found", Toast.LENGTH_LONG).show();
                                        } else {
                                            Message message = new Message();
                                            message.setMessage("Hello there\\nYou have successfully referred a friend. As a reward we have added 15 Koins to your account.\\nEnjoy!");
                                            message.setTitle("Referral Reward Received");
                                            message.setSeen(false);
                                            FirebaseDatabase.getInstance().getReference("messages").child(auth.getCurrentUser().getUid()).setValue(message);


                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        } else {
                            Toast.makeText(EntryScreen.this, "Please enter your refer", Toast.LENGTH_SHORT).show();
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


    public void signUpGuestUser(final SignUpListener signUpListener) {
        auth.signInAnonymously().addOnCompleteListener(EntryScreen.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d("anonymous:onComplete:", "" + task.isSuccessful());
                // progressBar.setVisibility(View.GONE);
                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    Log.d("auth fail", "" + task.getException());
                    Toast.makeText(EntryScreen.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                    Log.d("fail", "fail:" + task.getException());
                    showMainScreen();
                } else {
                    Toast.makeText(EntryScreen.this, "successfully logged in", Toast.LENGTH_LONG).show();
                    mFirebaseInstance = FirebaseDatabase.getInstance();
                    String guestId = new GuestIdGenerator().nextGuestId();

                    mFirebaseDatabase = mFirebaseInstance.getReference("users");

                    User user = new User(guestId + "@noemail.com", "Guest", "Guest", "",
                            "guest", 0, 0, 20, 0, 1,
                            1, false, false, false);
                    user.setPhotolink("https://pbs.twimg.com/profile_images/691113743296643072/WUm4qms6_400x400.png");

                   /* String[] names = {"John", "Tim", "Sam", "Ben"};
                    List nameList = new ArrayList<String>(Arrays.asList(names));
                    user.setQnList(nameList);*/
                    mFirebaseDatabase.child(auth.getCurrentUser().getUid()).setValue(user);
                    signUpListener.signedUp();
                }

            }
        });

    }

    public interface SignUpListener {
        void signedUp();
    }

    public void navigateToHome() {
        Intent intent = new Intent(EntryScreen.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    /**
     * Shows LOading Screen
     * Hides Main Screen and FInal Screen
     */


    public void showLoading() {
        loadingScreen.setVisibility(View.VISIBLE);
        mainScreen.setVisibility(View.GONE);
    }

    /**
     * Shows Main Screen
     * Hides Loading and FInal Screen
     */

    public void showMainScreen() {
        loadingScreen.setVisibility(View.GONE);
        mainScreen.setVisibility(View.VISIBLE);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            final GoogleSignInAccount acct = result.getSignInAccount();
            auth.createUserWithEmailAndPassword(acct.getEmail(), "QuizApp")
                    .addOnCompleteListener(EntryScreen.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Log.d("auth fail", "" + task.getException());
                                Toast.makeText(EntryScreen.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                Log.d("fail", "fail:" + task.getException());
                                // showMainScreen();
                            } else {
                                //Toast.makeText(EntryScreen.this, "successfully logged in", Toast.LENGTH_LONG).show();
                                mFirebaseInstance = FirebaseDatabase.getInstance();
                                String guestId = new GuestIdGenerator().nextGuestId();

                                mFirebaseDatabase = mFirebaseInstance.getReference("users");
                                auth.getCurrentUser();
                                User guser = new User("gender", 0, 20, 0,
                                        0, 1, 22, false, false, true);
                                try {
                                    guser.setLastname(acct.getFamilyName());
                                    guser.setFirstname(acct.getGivenName());
                                    guser.setPhotolink(acct.getPhotoUrl().toString());
                                } catch (NullPointerException e) {
                                    guser.setLastname("Ano");
                                    guser.setFirstname("nymous");
                                    guser.setPhotolink("https://pbs.twimg.com/profile_images/691113743296643072/WUm4qms6_400x400.png");
                                }

                                mFirebaseDatabase.child(auth.getCurrentUser().getUid()).setValue(guser);
                                showReferralDialog("Use", "Skip", new TransferFragment.OnYesClicked() {
                                    @Override
                                    public void onDone() {
                                        navigateToHome();
                                    }
                                }, new TransferFragment.OnNoClicked() {
                                    @Override
                                    public void onDone() {
                                        navigateToHome();
                                    }
                                });

                            }
                        }
                    });

        } else {
            showMainScreen();
            Toast.makeText(EntryScreen.this, "Could not sign into google " + result.getStatus().getStatusCode(), Toast.LENGTH_LONG).show();
        }
    }


}
