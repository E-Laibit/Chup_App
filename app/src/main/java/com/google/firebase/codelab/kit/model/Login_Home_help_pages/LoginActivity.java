package com.google.firebase.codelab.kit.model.Login_Home_help_pages;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.codelab.kit.model.R;

public class LoginActivity extends HomePage {

    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;

    // UI references.
    private EditText mEmail, mPassword;
    private Button btnSignIn,btnSignOut,btnProceed, btnCreate;
    private TextView mStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmail =  findViewById(R.id.email);
        mPassword =  findViewById(R.id.password);
        btnSignIn =  findViewById(R.id.email_sign_in_button);
        btnSignOut =  findViewById(R.id.email_sign_out_button);
        btnProceed =  findViewById(R.id.proceedButton);
        btnCreate =  findViewById(R.id.createAccount);
        mStatus = findViewById(R.id.statusTextView);

        mAuth = FirebaseAuth.getInstance();

        btnProceed.setOnClickListener((View v) ->{
            Intent start = new Intent(LoginActivity.this, HomePage.class);
            startActivity(start);
        });

        btnSignIn.setOnClickListener((View v) ->{
            signIn(mEmail.getText().toString(), mPassword.getText().toString());
        });

        btnSignOut.setOnClickListener((View v) ->{
            mAuth.getInstance().signOut();
            updateUI(null);
            toastMessage("Signing Out...");
        });

        btnCreate.setOnClickListener((View v) ->{
            createAccount(mEmail.getText().toString(), mPassword.getText().toString());
        });

    }
        @Override
        public void onStart() {
            super.onStart();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            updateUI(currentUser);
        }

    private void createAccount(String email, String password){
        Log.d(TAG, "Create Account:"+email);
        if(!validateForm()){return;}

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "Create user with Email: Success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    user.updateEmail(email);
                    toastMessage("Succesfully created User!");
                    updateUI(user);
                }else{
                    Log.w(TAG, "CreateUserWithEmail: Falure", task.getException());
                    toastMessage("Invalid. Failure. ");
                    updateUI(null);
                }
            }
        });
    }

    private void signIn(String email, String password){
        Log.d(TAG, "sign-in" + email);
        if (!validateForm()){return;}

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            user.updateEmail(email);
                            toastMessage("Welcome!  " + email);
                            updateUI(user);

                        }else {
                            Log.w(TAG, "SignInWithEmail:failure", task.getException());
                            toastMessage("Authentication Failed.");
                            updateUI(null);
                        }

                        if (!task.isSuccessful()){
                            mStatus.setText("Failed Login");
                        }
                    }
                });
    }

    public void updateUI(FirebaseUser user) {
        if(user != null){
            btnSignIn.setVisibility(View.GONE);
            btnCreate.setVisibility(View.GONE);
            mEmail.setVisibility(View.GONE);
            mPassword.setVisibility(View.GONE);
            btnSignOut.setVisibility(View.VISIBLE);
            btnProceed.setVisibility(View.VISIBLE);

            mStatus.setText("Logged in: " + user.getEmail());
        }
        else {
            mStatus.setText("Logged-out");
            btnSignIn.setVisibility(View.VISIBLE);
            btnCreate.setVisibility(View.VISIBLE);
            mEmail.setVisibility(View.VISIBLE);
            mPassword.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.GONE);
            btnProceed.setVisibility(View.GONE);
        }
    }

    private boolean validateForm(){
        boolean valid = true;

        String email = mEmail.getText().toString();
        if(TextUtils.isEmpty(email)){
            mEmail.setError("Required.");
            valid = false;
        } else {
            mEmail.setError(null);
        }

        String password = mPassword.getText().toString();
        if (TextUtils.isEmpty(password)){
            mPassword.setError("Required.");
            valid = false;
        }else{
            mPassword.setError(null);
        }

        return valid;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}
