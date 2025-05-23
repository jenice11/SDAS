package com.example.sdas;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sdas.Model.User;
import com.example.sdas.Utils.Common;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1234;
    private static final String TAG = "TestingLogin";
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth auth;
    private DatabaseReference user_information;

    private EditText inputEmail, inputPassword;
    private ProgressBar progressBar;
    private TextView textview, btnSignup, btnReset;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        user_information = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);
        user_information.keepSynced(true);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        textview = (TextView) findViewById(R.id.textView4);
        btnSignup = (TextView) findViewById(R.id.btn_signup);
        btnLogin = (Button) findViewById(R.id.btn_login);
        btnReset = (TextView) findViewById(R.id.btn_reset_password);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignupActivity.class));
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ResetPasswordActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        Paper.init(this);
    }

    //normal login start
    public void login() {
        final String email = inputEmail.getText().toString();
        final String password = inputPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            inputEmail.setError("Email is empty");
            return;
        }
        if (isValidEmail(email) == false) {
            inputEmail.setError("Email format is invalid");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            inputPassword.setError("Password is empty");
            return;
        }
        if (password.length() < 6) {
            inputPassword.setError("Password too short, enter minimum 6 characters");
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
                            Toast.makeText(getApplicationContext(), "Login failed, wrong credentials", Toast.LENGTH_SHORT).show();
                        } else {
                            final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//                            System.out.println("CURRENT USER UID: " + firebaseUser.getUid());

                            if (firebaseUser.isEmailVerified()) {
                                user_information.orderByKey()
                                        .equalTo(firebaseUser.getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.getValue() == null) {
                                                    Toast.makeText(getApplicationContext(), "User doesn't exist!", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Common.loggedUser = dataSnapshot.child(firebaseUser.getUid()).getValue(User.class);

//                                                    DatabaseReference publicLocation;
//                                                    publicLocation = FirebaseDatabase.getInstance().getReference(Common.PUBLIC_LOCATION);
//                                                    publicLocation.child(Common.loggedUser.getUid()).child("trackStatus").setValue(false);
                                                }

                                                Paper.book().write(Common.USER_UID_SAVE_KEY, Common.loggedUser.getUid());
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(), "Email not verified\nPlease check your email", Toast.LENGTH_LONG).show();

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case DialogInterface.BUTTON_POSITIVE:
                                                        if (!firebaseUser.isEmailVerified()) {
                                                            firebaseUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(@NonNull Void unused) {
                                                                    Toast.makeText(getApplicationContext(), "Verification Email is sent \nPlease verify first before login", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    System.out.println("Email verification not send/failed");

                                                                }
                                                            });
                                                        } else {
                                                            System.out.println("Email verification xxx");
                                                        }
                                                        break;

                                                    case DialogInterface.BUTTON_NEGATIVE:
                                                        //No button clicked
                                                        break;
                                                }
                                            }
                                        };
                                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                        builder.setMessage("Do you want to resend email verification link?")
                                                .setPositiveButton("Yes", dialogClickListener)
                                                .setNegativeButton("No", dialogClickListener).show();
                                    }
                                }, 2500);
                            }
                        }
                    }
                });


    }

    //end normal login
    public final boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            Toast.makeText(getApplicationContext(), "Email is empty!", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            System.out.println("Pattern= " + android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches());

            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
}
