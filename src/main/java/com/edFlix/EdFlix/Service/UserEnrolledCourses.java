package com.edFlix.EdFlix.Service;

import com.google.firebase.database.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Service
@CrossOrigin
public class UserEnrolledCourses {

    @Autowired
    com.edFlix.EdFlix.Util.RASecuredMessagingProtocol ra_smp;

    public CompletableFuture<Set<String>> getEnrolledCourses(Map<String, Object> payload, FirebaseDatabase ref) {

        CompletableFuture<Set<String>> future = new CompletableFuture<>();
        String[] client = payload.get("payload").toString().split(":");
        String edFlixId = ra_smp.decrypt(client[1]), edFlixPw = ra_smp.decrypt(client[2]);
        if (!edFlixId.contains("@")) {
            future.complete(null);
            return future;
        }
        String uid = edFlixId.substring(0, edFlixId.indexOf("@"));
        DatabaseReference dbRef = ref.getReference("/").child(uid);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> data = (Map<String, Object>) dataSnapshot.getValue();
                    Map<String, Boolean> d = (Map<String, Boolean>) data.get("course-access");
                    future.complete(d.keySet());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.complete(null);
            }
        });
        return future;
    }

    public CompletableFuture<String> isEnrolled(String cid, FirebaseDatabase ref, ArrayList<String> uid) {

        String UID = ra_smp.decrypt(uid.get(0).split(":")[1]);
        CompletableFuture<String> future = new CompletableFuture<>();
        DatabaseReference dbRef = ref.getReference("/").child(UID.substring(0, UID.indexOf("@")));
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<?, ?> data2 = (Map<?, ?>) ((Map<?, ?>) dataSnapshot.getValue()).get("course-access");
                    if (data2.containsKey(cid)) {
                        if (new Date().getTime() <= (long) data2.get(cid)) {

                            future.complete("accessible:" + data2.get(cid));
                         
                        } else {
                            future.complete("course_timed_out");
                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.complete("unregistered");
            }
        });
        return future;
    }
}
