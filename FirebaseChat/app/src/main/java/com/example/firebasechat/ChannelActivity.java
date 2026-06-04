package com.example.firebasechat;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference mRef;
    FirebaseFirestore mFirestore;

    EditText etChannelName, etJoinCode;
    Button btnCreateChannel, btnJoinChannel, btnOpenChannel, btnManageChannel, btnPublicChannel, btnBack;
    ListView lvChannel;

    ArrayList<String> channelItems = new ArrayList<>();
    ArrayList<String> channelIds = new ArrayList<>();
    ArrayList<String> channelNames = new ArrayList<>();
    ArrayList<Boolean> channelOwners = new ArrayList<>();
    ArrayAdapter<String> channelAdapter;

    String uid;
    String nickName;
    int selected = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        uid = mAuth.getUid();
        mRef = FirebaseDatabase.getInstance().getReference("firebasechat");
        mFirestore = FirebaseFirestore.getInstance();

        etChannelName = findViewById(R.id.etChannelName);
        etJoinCode = findViewById(R.id.etJoinCode);
        btnCreateChannel = findViewById(R.id.btnCreateChannel);
        btnJoinChannel = findViewById(R.id.btnJoinChannel);
        btnOpenChannel = findViewById(R.id.btnOpenChannel);
        btnManageChannel = findViewById(R.id.btnManageChannel);
        btnPublicChannel = findViewById(R.id.btnPublicChannel);
        btnBack = findViewById(R.id.btnChannelBack);
        lvChannel = findViewById(R.id.lvChannel);

        channelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, channelItems);
        lvChannel.setAdapter(channelAdapter);

        loadNickName();

        lvChannel.setOnItemClickListener((parent, view, position, id) -> {
            selected = position;
            Toast.makeText(ChannelActivity.this, channelNames.get(position) + " 선택됨", Toast.LENGTH_SHORT).show();
        });

        btnPublicChannel.setOnClickListener(v -> openChannel("public", "공용 채널"));

        btnOpenChannel.setOnClickListener(v -> openSelectedChannel());

        btnCreateChannel.setOnClickListener(v -> createChannel());

        btnJoinChannel.setOnClickListener(v -> joinChannel());

        btnManageChannel.setOnClickListener(v -> manageChannel());

        btnBack.setOnClickListener(v -> finish());
    }

    void loadNickName() {
        mRef.child("UserAccount").child(uid).child("nickName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                nickName = snapshot.getValue(String.class);

                if (nickName == null || nickName.length() == 0) {
                    nickName = "익명";
                }

                loadChannels();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChannelActivity.this, "DB Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void loadChannels() {
        channelItems.clear();
        channelIds.clear();
        channelNames.clear();
        channelOwners.clear();

        channelItems.add("공용 채널");
        channelIds.add("public");
        channelNames.add("공용 채널");
        channelOwners.add(false);

        mFirestore.collection("channels")
                .whereArrayContains("members", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String name = doc.getString("name");
                        String ownerUid = doc.getString("ownerUid");

                        List<String> blacklist = (List<String>) doc.get("blacklist");

                        if (blacklist != null && blacklist.contains(nickName)) {
                            continue;
                        }

                        boolean isOwner = uid.equals(ownerUid);

                        channelIds.add(id);
                        channelNames.add(name);
                        channelOwners.add(isOwner);

                        if (isOwner) {
                            channelItems.add(name + " (내 채널)");
                        }
                        else {
                            channelItems.add(name);
                        }
                    }

                    channelAdapter.notifyDataSetChanged();
                });
    }

    void createChannel() {
        String name = etChannelName.getText().toString();
        String code = etJoinCode.getText().toString();

        if (name.length() == 0 || code.length() == 0) {
            Toast.makeText(this, "채널 이름과 참가 코드를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        mFirestore.collection("channels")
                .whereEqualTo("code", code)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(ChannelActivity.this, "이미 사용 중인 참가 코드입니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ArrayList<String> members = new ArrayList<>();
                    members.add(uid);

                    ArrayList<String> blacklist = new ArrayList<>();

                    Map<String, Object> channel = new HashMap<>();
                    channel.put("name", name);
                    channel.put("code", code);
                    channel.put("ownerUid", uid);
                    channel.put("ownerNick", nickName);
                    channel.put("members", members);
                    channel.put("blacklist", blacklist);

                    mFirestore.collection("channels").add(channel)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(ChannelActivity.this, "채널이 생성되었습니다.", Toast.LENGTH_SHORT).show();
                                etChannelName.setText("");
                                etJoinCode.setText("");
                                loadChannels();
                            });
                });
    }

    void joinChannel() {
        String code = etJoinCode.getText().toString();

        if (code.length() == 0) {
            Toast.makeText(this, "참가 코드를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        mFirestore.collection("channels")
                .whereEqualTo("code", code)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(ChannelActivity.this, "채널을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);

                    List<String> blacklist = (List<String>) doc.get("blacklist");

                    if (blacklist != null && blacklist.contains(nickName)) {
                        Toast.makeText(ChannelActivity.this, "블랙리스트에 등록된 사용자입니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    doc.getReference().update("members", FieldValue.arrayUnion(uid))
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(ChannelActivity.this, "채널에 참가했습니다.", Toast.LENGTH_SHORT).show();
                                etJoinCode.setText("");
                                loadChannels();
                            });
                });
    }

    void openSelectedChannel() {
        if (selected < 0 || selected >= channelIds.size()) {
            selected = 0;
        }

        openChannel(channelIds.get(selected), channelNames.get(selected));
    }

    void openChannel(String channelId, String channelName) {
        Intent intent = new Intent(ChannelActivity.this, ChatActivity.class);
        intent.putExtra("channelId", channelId);
        intent.putExtra("channelName", channelName);
        startActivity(intent);
    }

    void manageChannel() {
        if (selected <= 0 || selected >= channelIds.size()) {
            Toast.makeText(this, "관리할 비밀 채널을 선택하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!channelOwners.get(selected)) {
            Toast.makeText(this, "채널 소유주만 관리할 수 있습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] items = {"채널 이름 수정", "참가 코드 수정", "블랙리스트 추가", "블랙리스트 제거", "채널 삭제"};

        new AlertDialog.Builder(this)
                .setTitle("채널 관리")
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        updateChannelField("name", "새 채널 이름");
                    }
                    else if (which == 1) {
                        updateChannelField("code", "새 참가 코드");
                    }
                    else if (which == 2) {
                        updateBlacklist(true);
                    }
                    else if (which == 3) {
                        updateBlacklist(false);
                    }
                    else if (which == 4) {
                        deleteChannel();
                    }
                })
                .show();
    }

    void updateChannelField(String field, String title) {
        EditText input = new EditText(this);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(input)
                .setPositiveButton("확인", (dialog, which) -> {
                    String value = input.getText().toString();

                    if (value.length() == 0) {
                        Toast.makeText(ChannelActivity.this, "값을 입력하세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String id = channelIds.get(selected);

                    mFirestore.collection("channels").document(id).update(field, value)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(ChannelActivity.this, "수정되었습니다.", Toast.LENGTH_SHORT).show();
                                loadChannels();
                            });
                })
                .setNegativeButton("취소", null)
                .show();
    }

    void updateBlacklist(boolean add) {
        EditText input = new EditText(this);
        input.setHint("닉네임");

        new AlertDialog.Builder(this)
                .setTitle(add ? "블랙리스트 추가" : "블랙리스트 제거")
                .setView(input)
                .setPositiveButton("확인", (dialog, which) -> {
                    String nick = input.getText().toString();

                    if (nick.length() == 0) {
                        Toast.makeText(ChannelActivity.this, "닉네임을 입력하세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String id = channelIds.get(selected);
                    DocumentReference ref = mFirestore.collection("channels").document(id);

                    if (add) {
                        ref.update("blacklist", FieldValue.arrayUnion(nick))
                                .addOnSuccessListener(unused -> Toast.makeText(ChannelActivity.this, "블랙리스트에 추가되었습니다.", Toast.LENGTH_SHORT).show());
                    }
                    else {
                        ref.update("blacklist", FieldValue.arrayRemove(nick))
                                .addOnSuccessListener(unused -> Toast.makeText(ChannelActivity.this, "블랙리스트에서 제거되었습니다.", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    void deleteChannel() {
        new AlertDialog.Builder(this)
                .setTitle("채널 삭제")
                .setMessage("정말 채널을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> {
                    String id = channelIds.get(selected);
                    DocumentReference ref = mFirestore.collection("channels").document(id);

                    ref.collection("messages").get().addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            doc.getReference().delete();
                        }

                        ref.delete().addOnSuccessListener(unused -> {
                            Toast.makeText(ChannelActivity.this, "채널이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            selected = 0;
                            loadChannels();
                        });
                    });
                })
                .setNegativeButton("취소", null)
                .show();
    }
}