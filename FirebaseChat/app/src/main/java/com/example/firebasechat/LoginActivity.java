package com.example.firebasechat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference mRef;
    EditText etEmail, etPassword;
    Button btnLogin, btnSignup, btnUpdate, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference("firebasechat");

        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnLoginSignup);
        btnUpdate = findViewById(R.id.btnLoginUpdate);
        btnLogout = findViewById(R.id.btnLoginOut);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mAuth.getCurrentUser() == null) {
                    Intent mIntent = new Intent(LoginActivity.this, SignupActivity.class);
                    startActivity(mIntent);
                }
                else {
                    Toast.makeText(LoginActivity.this, "이미 로그인 되어 있습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = etEmail.getText().toString();
                String pw = etPassword.getText().toString();

                mAuth.signInWithEmailAndPassword(email, pw).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, email + "님 로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                            Intent mIntent = new Intent(LoginActivity.this, ChannelActivity.class);
                            startActivity(mIntent);
                        }
                        else {
                            Toast.makeText(LoginActivity.this, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mAuth.getCurrentUser() != null) {
                    Intent mIntent = new Intent(LoginActivity.this, UpdateinfoActivity.class);
                    startActivity(mIntent);
                }
                else {
                    Toast.makeText(LoginActivity.this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mAuth.getCurrentUser() != null) {
                    String email = mAuth.getCurrentUser().getEmail();
                    Toast.makeText(LoginActivity.this, email + " 님 로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                }
                else {
                    Toast.makeText(LoginActivity.this, "이미 로그아웃 되어 있습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}