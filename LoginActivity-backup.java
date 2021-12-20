package com.example.sdas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sdas.Model.User;
import com.example.sdas.Utils.Common;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText inputEmail, inputPassword;
    private TextView textview,btnSignup,skip,btnReset;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private Button btnLogin;
    GoogleSignInClient mGoogleSignInClient;

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    private FirebaseAnalytics mFirebaseAnalytics;
    DatabaseReference user_information;
    private FirebaseAuth.AuthStateListener mAuthListener;


    public static final int RC_SIGN_IN = 777;
    private static final String TAG = "SignInActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();
        user_information = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);
        user_information.keepSynced(true);

        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        Paper.init(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        findViewById(R.id.sign_in_button).setOnClickListener(this);

        // [START initialize_auth]
        auth = FirebaseAuth.getInstance();
        // [END initialize_auth]


        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        textview = (TextView) findViewById(R.id.textView4);
        btnSignup = (TextView) findViewById(R.id.btn_signup);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnReset = (TextView) findViewById(R.id.btn_reset_password);

        signInButton.setOnClickListener(this);

//        signInButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(),"Pressed sign in",Toast.LENGTH_LONG).show();
//
//                signIn();
//            }
//        });

//        btnSignup.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
//            }
//        });
//
//        btnReset.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
//            }
//        });
//
//        btnLogin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                login();
//            }
//        });
//        textview.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                login();
//            }
//        });
    }

    public void login()
    {
        final String email = inputEmail.getText().toString();
        final String password = inputPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        //authenticate user
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {
                            // there was an error
                            if (password.length() < 6) {
                                inputPassword.setError("Password length should be more than 6 characters");
                            } else {
                                Toast.makeText(LoginActivity.this, "Auth Failed", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            Paper.book().write(Common.USER_UID_SAVE_KEY,currentUser);

                            final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            System.out.println("CURRENT USER UID: " + firebaseUser.getUid());

                            user_information.orderByKey()
                                    .equalTo(firebaseUser.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.getValue() == null)
                                            {
                                                Toast.makeText(getApplicationContext(), "User doesn't exist!", Toast.LENGTH_SHORT).show();
                                            }
                                            else{
                                                Common.loggedUser = dataSnapshot.child(firebaseUser.getUid()).getValue(User.class);

                                                DatabaseReference publicLocation;
                                                publicLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
                                                publicLocation.child(Common.loggedUser.getUid()).child("trackStatus").setValue(false);
                                            }

                                            Paper.book().write(Common.USER_UID_SAVE_KEY,Common.loggedUser.getUid());
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);

                            mEditor.putString("Hello", "True");
                            mEditor.commit();
                            finish();
                        }
                    }
                });

    }




    //Change UI according to user data.
//    public void updateUI(FirebaseUser account){
//
//        if(account != null){
//            Toast.makeText(this,"You Signed In successfully",Toast.LENGTH_LONG).show();
//            startActivity(new Intent(this,HomeActivity.class));
//
//        }else {
//            Toast.makeText(this,"You Didnt signed in",Toast.LENGTH_LONG).show();
//        }
//    }

    private void updateUI(GoogleSignInAccount user) {
        if (user != null) {
            Toast.makeText(this,"You Signed In successfully",Toast.LENGTH_LONG).show();
            startActivity(new Intent(this,HomeActivity.class));
        } else {
            Toast.makeText(this,"Google Sign-in Failed",Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                Toast.makeText(getApplicationContext(),"Pressed sign in",Toast.LENGTH_LONG).show();

                signIn();
                break;
            // ...
        }

    }
    // [END auth_with_google]

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

}