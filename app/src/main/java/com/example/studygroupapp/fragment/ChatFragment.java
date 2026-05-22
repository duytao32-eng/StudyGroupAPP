package com.example.studygroupapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studygroupapp.R;
import com.example.studygroupapp.adapter.ChatAdapter;
import com.example.studygroupapp.model.Message;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private String groupId;
    private RecyclerView rvChat;
    private EditText edtMessageInput;
    private ImageView btnSend;

    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private String currentUserId;
    private String currentUserName = "Thành viên";

    public ChatFragment(String groupId) {
        this.groupId = groupId;
    }

    // NƠI XỬ LÝ THỜI GIAN THỰC CỦA PHẦN CHAT

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        rvChat = view.findViewById(R.id.rvChat);
        edtMessageInput = view.findViewById(R.id.edtMessageInput);
        btnSend = view.findViewById(R.id.btnSend);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Cấu hình RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true); // Đẩy tin nhắn cũ lên trên, mới ở dưới
        rvChat.setLayoutManager(layoutManager);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, currentUserId);
        rvChat.setAdapter(chatAdapter);

        // Lấy tên thật của user đang chat để đính kèm vào tin nhắn
        FirebaseFirestore.getInstance().collection("Users").document(currentUserId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.getString("fullName") != null) {
                        currentUserName = doc.getString("fullName");
                    }
                });

        listenForMessages(); // Bật "Camera giám sát" Firebase

        btnSend.setOnClickListener(v -> sendMessage());

        return view;
    }

    // GỬI TIN NHẮN REALTIME ĐẨY LÊN FIREBASE
    private void sendMessage() {
        String text = edtMessageInput.getText().toString().trim();
        if (text.isEmpty()) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Cấu trúc dữ liệu: Nhóm -> [ID_Nhóm] -> Tin nhắn -> [Các tin nhắn]
        Message newMessage = new Message(currentUserId, currentUserName, text, Timestamp.now());

        db.collection("Groups").document(groupId)
                .collection("Messages")
                .add(newMessage)
                .addOnSuccessListener(docRef -> edtMessageInput.setText("")); // Gửi xong thì xóa trắng ô nhập
    }

    // NGHE TIN NHẮN THEO THỜI GIAN THỰC (REAL-TIME)
    private void listenForMessages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Groups").document(groupId)
                .collection("Messages")
                .orderBy("timestamp") // Sắp xếp theo thứ tự thời gian
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(requireContext(), "Lỗi tải tin nhắn!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        messageList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Message msg = doc.toObject(Message.class);
                            messageList.add(msg);
                        }
                        chatAdapter.notifyDataSetChanged();
                        // Tự động cuộn xuống tin nhắn mới nhất dưới cùng
                        if (messageList.size() > 0) {
                            rvChat.scrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }
}