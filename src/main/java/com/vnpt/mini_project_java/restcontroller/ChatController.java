package com.vnpt.mini_project_java.restcontroller;

import com.google.firebase.database.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(
            @RequestParam String sender,
            @RequestParam String content) {
        try {
            String receiver;

            if (sender.equalsIgnoreCase("Admin") && content.startsWith("(")) {
                receiver = content.substring(1, content.indexOf(")")).trim();
            } else {
                receiver = sender;
            }

            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("chat/conversations")
                    .child(receiver);

            Map<String, Object> message = new HashMap<>();
            message.put("sender", sender);
            message.put("content", content);
            message.put("timestamp", System.currentTimeMillis());

            ref.push().setValueAsync(message);

            return ResponseEntity.ok("✅ Gửi tin nhắn thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Lỗi gửi tin: " + e.getMessage());
        }
    }

    @GetMapping(value = "/messages", produces = "application/json")
    public ResponseEntity<?> getAllMessages() {
        try {
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("chat/conversations");

            CompletableFuture<List<Map<String, Object>>> future = new CompletableFuture<>();

            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    List<Map<String, Object>> messages = new ArrayList<>();

                    for (DataSnapshot child : snapshot.getChildren()) {
                        if (child.hasChild("content") && child.hasChild("timestamp")) {
                            Map<String, Object> msg = new HashMap<>();
                            msg.put("id", child.getKey());
                            msg.put("sender", child.child("sender").getValue(String.class));
                            msg.put("content", child.child("content").getValue(String.class));
                            msg.put("timestamp", child.child("timestamp").getValue(Long.class));
                            messages.add(msg);
                        }
                        else {
                            for (DataSnapshot sub : child.getChildren()) {
                                if (sub.hasChild("content") && sub.hasChild("timestamp")) {
                                    Map<String, Object> msg = new HashMap<>();
                                    msg.put("id", sub.getKey());
                                    msg.put("sender", sub.child("sender").getValue(String.class));
                                    msg.put("content", sub.child("content").getValue(String.class));
                                    msg.put("timestamp", sub.child("timestamp").getValue(Long.class));
                                    messages.add(msg);
                                }
                            }
                        }
                    }
                    messages.sort(Comparator.comparingLong(m -> (Long) m.get("timestamp")));
                    future.complete(messages);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    future.completeExceptionally(error.toException());
                }
            });

            List<Map<String, Object>> result = future.get(10, TimeUnit.SECONDS);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Không thể lấy tin nhắn: " + e.getMessage());
        }
    }
}
