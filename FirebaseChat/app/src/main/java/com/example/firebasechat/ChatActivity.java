package com.example.firebasechat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference mRef;
    FirebaseFirestore mFirestore;
    CollectionReference chatRef;
    ListenerRegistration chatListener;

    ArrayList<String> messageItems = new ArrayList<>();
    ArrayAdapter<String> messageAdapter;

    EditText etMessage;
    Button btnEnd, btnSend;
    ListView lvChat;

    String nickName;
    String channelId = "public";
    String channelName = "공용 채널";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();

        if (intent != null) {
            String id = intent.getStringExtra("channelId");
            String name = intent.getStringExtra("channelName");

            if (id != null) {
                channelId = id;
            }

            if (name != null) {
                channelName = name;
            }
        }

        setTitle(channelName);

        etMessage = findViewById(R.id.etMessage);
        btnEnd = findViewById(R.id.btnEnd);
        btnSend = findViewById(R.id.btnSend);
        lvChat = findViewById(R.id.listView);

        messageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messageItems);
        lvChat.setAdapter(messageAdapter);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mFirestore = FirebaseFirestore.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference("firebasechat");

        if (channelId.equals("public")) {
            chatRef = mFirestore.collection("myChat");
        }
        else {
            chatRef = mFirestore.collection("channels").document(channelId).collection("messages");
        }

        DatabaseReference nickNameRef = mRef.child("UserAccount").child(mAuth.getUid()).child("nickName");

        nickNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    nickName = dataSnapshot.getValue(String.class);
                    Log.d("TEST", "Nickname: " + nickName);
                }

                if (nickName == null || nickName.length() == 0) {
                    nickName = "익명";
                }

                startChatListener();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = etMessage.getText().toString();
                String uid = mAuth.getUid();

                if (message.length() == 0) {
                    return;
                }

                chatRef.add(new MessageItem(message, nickName, uid));
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
            public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {

                if (mAuth.getCurrentUser() != null) {
                    messageItems.remove(position);
                    messageAdapter.notifyDataSetChanged();
                }

                return true;
            }
        });
    }

    void startChatListener() {
        messageItems.clear();
        messageAdapter.notifyDataSetChanged();

        if (chatListener != null) {
            chatListener.remove();
        }

        chatListener = chatRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if (error != null || value == null) {
                    return;
                }

                for (DocumentChange dc : value.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        DocumentSnapshot snapshot = dc.getDocument();

                        Map<String, Object> data = snapshot.getData();

                        if (data == null) {
                            continue;
                        }

                        Object messageObj = data.get("message");
                        Object nickObj = data.get("nick");
                        Object uidObj = data.get("uid");

                        if (messageObj == null || nickObj == null || uidObj == null) {
                            continue;
                        }

                        String message = messageObj.toString();
                        String nick = nickObj.toString();
                        String uid = uidObj.toString();

                        Log.d("TEST", "Channel: " + channelId + ", Message: " + message + ", Nick: " + nick + ", UID: " + uid);

                        messageItems.add("(" + nick + ") >> " + message);
                        messageAdapter.notifyDataSetChanged();
                        lvChat.setSelection(lvChat.getCount() - 1);
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (chatListener != null) {
            chatListener.remove();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("채널 선택");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().toString().equals("채널 선택")) {
            Intent intent = new Intent(ChatActivity.this, ChannelActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}