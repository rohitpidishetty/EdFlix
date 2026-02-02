package com.edFlix.EdFlix.Service;

import com.edFlix.EdFlix.Controller.EdFlix;
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

    public final String delimeter = "          "; // 10 spaces, to make sure the session token is not tampered

    public CompletableFuture<Boolean> verify(Map<String, Object> payload, FirebaseDatabase ref) {

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        ArrayList<Object> data = (ArrayList<Object>) payload.get("payload");
        String[] client = (data.get(0).toString() + this.delimeter).split(":");
        String clientToken = client[0], clientEdFlixId = ra_smp.decrypt(client[1]), clientEdFlixPw = ra_smp.decrypt(client[2]);


        if (!clientEdFlixId.contains("@") || !clientEdFlixId.equals(data.get(1))) {
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

// Verify token
                try {

                    if (userData.get("ssa_token") == null) {
                        future.complete(false);
                        return;
                    }
                    future.complete(userData.get("ssa_token").equals(data.get(0).toString()));
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
