package com.edFlix.EdFlix.Service;

import com.edFlix.EdFlix.Util.RASecuredMessagingProtocol;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@CrossOrigin
public class VerifyUser {
    @Autowired
    RASecuredMessagingProtocol ra_smp;

    protected static Map<?, ?> data;

    public CompletableFuture<Boolean> verify(Map<String, Object> payload, FirebaseDatabase ref) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        ArrayList<Object> data = (ArrayList<Object>) payload.get("payload");
        String[] client = data.get(0).toString().split(":");
        String clientToken = client[0], clientEdFlixId = ra_smp.decrypt(client[1]), clientEdFlixPw = ra_smp.decrypt(client[2]);

        if (!clientEdFlixId.contains("@")) {
            future.complete(false);
            return future;
        }

        DatabaseReference dbRef = ref.getReference("/").child(clientEdFlixId.substring(0, clientEdFlixId.indexOf("@")));

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    future.complete(false);
                    return;
                }

                Map<String, String> userData = (Map<String, String>) snapshot.getValue();
                if (!userData.get("id").equals(clientEdFlixId) || !userData.get("pw").equals(clientEdFlixPw)) {
                    future.complete(false);
                    return;
                }

                // Building token
                StringBuilder stream = new StringBuilder();
                stream.append(clientEdFlixId).append(":").append(clientEdFlixPw).append(":");
                for (int i = 1; i < data.size(); i++) stream.append(data.get(i)).append(":");

                try {
                    MessageDigest md = MessageDigest.getInstance("SHA-256");
                    byte[] byteStream = md.digest(stream.toString().getBytes());
                    StringBuilder hash = new StringBuilder();
                    for (byte b : byteStream) {
                        String hex = Integer.toHexString(0xff & b);
                        if (hex.length() == 1) hash.append('0');
                        hash.append(hex);
                    }
                    String newHash = hash.toString();
                    if (userData.get("ssa_token") == null) {
                        future.complete(false);
                        return;
                    }
                    if (!userData.get("ssa_token").split(":")[0].equals(newHash.split(":")[0])) {
                        future.complete(false);
                    }
                    future.complete(newHash.equals(clientToken));
                } catch (Exception e) {
                    future.complete(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.complete(false);
            }
        });

        return future;
    }

}
