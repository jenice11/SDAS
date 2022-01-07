package com.example.sdas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sdas.Model.User;
import com.example.sdas.Utils.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class SignupActivity extends AppCompatActivity {

    private TextView skip, textview, signin, btnResetPassword;
    private EditText inputEmail, inputPassword, inputName;
    private Button btnSignUp;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();

        auth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        textview = (TextView) findViewById(R.id.textView4);
        btnResetPassword = (TextView) findViewById(R.id.btn_reset_password);
//        skip = (TextView) findViewById(R.id.skip);
        signin = (TextView) findViewById(R.id.sign_in_button);
        btnSignUp = (Button) findViewById(R.id.sign_up_button);
        inputName = (EditText) findViewById(R.id.name);
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);


        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, ResetPasswordActivity.class));
            }
        });

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this,LoginActivity.class));
            }
        });

//        skip.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(SignupActivity.this, HomeActivity.class));
//                mEditor.putString("Hello", "False");
//                mEditor.commit();
//            }
//        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
        textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    public void register()
    {
        final String name = inputName.getText().toString().trim();
        final String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        boolean c1 = false,c2 = false, c3= false;

        if (TextUtils.isEmpty(name)) {
            inputEmail.setError("Name is empty");
            return;
        }else{
            c1 = true;
        }

        if(isValidEmail(email) == true){
            System.out.println("Email format valid");
            c2 = true;
        }
        else{
            inputEmail.setError("Email format is invalid");
        }

//        if (TextUtils.isEmpty(email)) {
//            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
//            return;
//        }

        if (TextUtils.isEmpty(password)) {
            inputPassword.setError("Password is empty");
            return;
        }

        if (password.length() < 6) {
            inputPassword.setError("Password too short, enter minimum 6 characters");
            return;
        }else{
            c3 = true;
        }

        if(c1 == true && c2 == true && c3 == true){
            progressBar.setVisibility(View.VISIBLE);
            final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            //create user
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.GONE);
                            if (!task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Registration failed." + task.getException(),
                                        Toast.LENGTH_SHORT).show();
                            } else {

                                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                User user = new User(uid, email, name);
                                mDatabase.child(Common.USER_INFORMATION).child(uid).setValue(user);
                                updateToken(firebaseUser);

                                Toast.makeText(getApplicationContext(), "Registration Successful", Toast.LENGTH_SHORT).show();


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

                                new Handler().postDelayed(new Runnable() {
                                    @Override public void run() {
                                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                    } }, 3800);




                            }
                        }
                    });
        }





    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    private void updateToken(final FirebaseUser firebaseUser) {
        final DatabaseReference tokens = FirebaseDatabase.getInstance()
                .getReference(Common.TOKENS);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        tokens.child(firebaseUser.getUid())
                                .setValue(instanceIdResult.getToken());
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast toast = Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT);
                View view =toast.getView();
                view.setBackgroundColor(Color.GREEN); //any color your want
                toast.show();
            }
        });

    }

    public final boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            Toast.makeText(getApplicationContext(), "Email is empty", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            System.out.println("Pattern= " + android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches());

            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }


}
