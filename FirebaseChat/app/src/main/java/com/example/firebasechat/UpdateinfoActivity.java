package com.example.firebasechat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UpdateinfoActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference mRef;
    EditText etEmail, etPassword, etName, etNick;
    Button btnUpdate, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updateinfo);

        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference("firebasechat");

        etEmail = findViewById(R.id.etUpdateEmail);
        etPassword = findViewById(R.id.etUpdatePassword);
        etName = findViewById(R.id.etUpdateName);
        etNick = findViewById(R.id.etUpdateNick);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnBack = findViewById(R.id.btnUpdateBack);

        if (mAuth.getCurrentUser() != null) {
            mRef.child("UserAccount").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    UserAccount user = snapshot.getValue(UserAccount.class);

                    etEmail.setText(user.getEmailId());
                    etPassword.setText(user.getPassword());
                    etName.setText(user.getUserName());
                    etNick.setText(user.getNickName());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                    Toast.makeText(UpdateinfoActivity.this, "DB Error", Toast.LENGTH_SHORT).show();
                }
            });
        }

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UserAccount user = new UserAccount();

                user.setEmailId(etEmail.getText().toString());
                user.setPassword(etPassword.getText().toString());
                user.setUserName(etName.getText().toString());
                user.setNickName(etNick.getText().toString());

                mRef.child("UserAccount").child(mAuth.getUid()).setValue(user);

                Toast.makeText(UpdateinfoActivity.this, "정보가 수정되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
    }
}