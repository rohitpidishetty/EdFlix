package com.edFlix.EdFlix.Service;

import com.edFlix.EdFlix.Util.RASecuredMessagingProtocol;
import com.google.api.core.ApiFuture;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import com.google.firebase.database.core.Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.security.MessageDigest;
import java.util.concurrent.CompletableFuture;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.api.core.ApiFuture;
import org.springframework.web.bind.annotation.CrossOrigin;

@Service
@CrossOrigin
public class RegisterUser {
    @Autowired
    RASecuredMessagingProtocol ra_smp;

    public String commit(Map<String, Object> data, FirebaseDatabase ref) {
        CompletableFuture<Map<String, String>> future = new CompletableFuture<>();

        ArrayList<Object> obj = (ArrayList<Object>) data.get("payload");
        String edFlixId = obj.get(0).toString(), edFlixPw = obj.get(1).toString();
        if (!edFlixId.contains("@"))
            return "-1";

        DatabaseReference dbRef = ref.getReference("/").child(edFlixId.substring(0, edFlixId.indexOf("@")));
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    Map<String, String> cred = (Map<String, String>) dataSnapshot.getValue();
                    if (cred.get("id").equals(edFlixId) && cred.get("pw").equals(edFlixPw)) {

                        future.complete(cred);
                    } else
                        future.complete(null);
                } else
                    future.complete(null);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        try {
            Map<String, String> cred = future.get();
            if (cred == null)
                return "-1:No user found";
            if (!cred.get("id").equals(edFlixId) || !cred.get("pw").equals(edFlixPw))
                return "-1:Credentials do not match";

            StringBuilder stream = new StringBuilder();
            for (Object o : obj)
                stream.append(o.toString()).append(":");

            StringBuilder hash = new StringBuilder();

            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] byteStream = md.digest(stream.toString().getBytes());
                for (byte b : byteStream) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hash.append('0');
                    hash.append(hex);
                }
                hash.append(":").append(ra_smp.encrypt(edFlixId)).append(":").append(ra_smp.encrypt(edFlixPw)).append(":0xffe0");
            } catch (Exception e) {
                return "-1:";
            }

            String newHash = hash.toString();

            if (cred.get("ssa_token") != null) {

                if (!cred.get("ssa_token").split(":")[0].equals(newHash.split(":")[0])) {
                    return "-1:Device authentication failed, this device is not registered";
                }
            } else {
                dbRef.child("ssa_token").setValueAsync(newHash).get();
            }
            return newHash;
        } catch (Exception e) {
            return "-1:";
        }
    }

}
