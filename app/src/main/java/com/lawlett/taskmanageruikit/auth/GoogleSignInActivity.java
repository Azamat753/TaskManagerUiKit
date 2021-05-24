package com.lawlett.taskmanageruikit.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.policy.PrivatePolicyActivity;
import com.lawlett.taskmanageruikit.splash.SplashActivity;
import com.lawlett.taskmanageruikit.utils.PlannerDialog;

public class GoogleSignInActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;
    private SignInButton google_button;
    private Button newGoogleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_sign_in);
        createRequest();
        mAuth = FirebaseAuth.getInstance();
        initViews();
        newGoogleBtn.setOnClickListener(v -> {
            if (v == newGoogleBtn) {
                google_button.performClick();
            }
            PlannerDialog.showPlannerDialog(this,getString(R.string.planner) ,getString(R.string.you_sure_read_private_policy), this::signIn);
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, SplashActivity.class));
        finish();
    }

    private void initViews() {
        google_button = findViewById(R.id.google_signIn);
        newGoogleBtn = findViewById(R.id.new_google_btn);
    }

    private void createRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
                Toast.makeText(GoogleSignInActivity.this, R.string.wait, Toast.LENGTH_SHORT).show();
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    startActivity(new Intent(getApplicationContext(), SplashActivity.class));
                    finish();
                }
            } else {
                Toast.makeText(GoogleSignInActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void openPrivatePolicy(View view) {
        startActivity(new Intent(GoogleSignInActivity.this, PrivatePolicyActivity.class));
    }
}