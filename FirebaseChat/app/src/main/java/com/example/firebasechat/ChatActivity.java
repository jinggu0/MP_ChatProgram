package com.example.firebasechat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference mRef;
    FirebaseFirestore mFirestore;
    CollectionReference chatRef;

    ArrayList<String> messageItems = new ArrayList<>();
    ArrayAdapter messageAdapter;
    String chatName = "myChat";

    EditText etMessage;
    Button btnEnd, btnSend;
    ListView lvChat;
    String nickName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        etMessage = findViewById(R.id.etMessage);
        btnEnd = findViewById(R.id.btnEnd);
        btnSend = findViewById(R.id.btnSend);
        lvChat = findViewById(R.id.listView);

        messageAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, messageItems);
        lvChat.setAdapter(messageAdapter);

        mFirestore = FirebaseFirestore.getInstance();
        chatRef = mFirestore.collection(chatName);
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference("firebasechat");

        DatabaseReference nickNameRef = mRef.child("UserAccount").child(mAuth.getUid()).child("nickName");

        nickNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    nickName = dataSnapshot.getValue(String.class);
                    Log.d("TEST", "Nickname: " + nickName);
                }
                else {
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        chatRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                List<DocumentChange> documentChanges = value.getDocumentChanges();

                for (DocumentChange dc : documentChanges) {
                    DocumentSnapshot snapshot = dc.getDocument();

                    Map<String, Object> data = snapshot.getData();
                    String message = data.get("message").toString();
                    String nick = data.get("nick").toString();
                    String uid = data.get("uid").toString();

                    Log.d("TEST", "Message: " + message + ", Nick: " + nick + ", UID: " + uid);
                    messageItems.add(new String("(" + nick + ") >> " + message));
                    messageAdapter.notifyDataSetChanged();
                    lvChat.setSelection(lvChat.getCount() - 1);
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nick = nickName;
                String message = etMessage.getText().toString();
                String uid = mAuth.getUid();

                chatRef.add(new MessageItem(message, nick, uid));

                etMessage.setText("");
            }
        });

        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });

        lvChat.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (mAuth.getCurrentUser() != null) {
                    messageItems.remove(position);
                    messageAdapter.notifyDataSetChanged();
                }

                return true;
            }
        });
    }
}