package com.edFlix.EdFlix.Service;

import com.google.firebase.database.*;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TermiateSession {

    public void terminate(Map<String, String> user, FirebaseDatabase ref) {
        System.out.println(user);
        String edFlixId = user.get("edflixId");
        DatabaseReference dbRef = ref.getReference("/").child(edFlixId.substring(0, edFlixId.indexOf("@")));
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    dataSnapshot.getRef().child("ssa_token").removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            System.out.printf("%s Session terminated\n", edFlixId);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
