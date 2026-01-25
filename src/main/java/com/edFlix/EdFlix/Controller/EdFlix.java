package com.edFlix.EdFlix.Controller;

import com.edFlix.EdFlix.Service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/edflix/secured-line")
public class EdFlix {

    protected FirebaseOptions options;
    protected FirebaseDatabase ref;

    public EdFlix(@Value("${DB}") String credentials) {
        try (
                InputStream is = new ByteArrayInputStream(
                        credentials.getBytes(StandardCharsets.UTF_8)
                )
        ) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(is))
                    .setDatabaseUrl("https://edflix-ittacademy-default-rtdb.firebaseio.com")
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            this.ref = FirebaseDatabase.getInstance();
        } catch (Exception e) {
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }

    @GetMapping("/test")
    public Map<String, String> test() {
        return new HashMap<>() {
            {
                put("msg", "con");
            }
        };
    }

    @Autowired
    private RegisterUser reg;

    @Autowired
    private VerifyUser ver;

    @Autowired
    private UserEnrolledCourses uec;

    @Autowired
    private CoursePlaylist cp;

    @Autowired
    private TermiateSession terminator;

    @PostMapping("/register_user")
    public Map<String, String> registerUser(
            @RequestBody Map<String, Object> payload
    ) {
        String signedSecuredAccessToken = reg.commit(payload, this.ref);

        //        SSAT
        return new HashMap<>() {
            {
                put("token", signedSecuredAccessToken);
            }
        };
    }

    @PostMapping("/verify_user")
    public Map<String, Boolean> verifyUser(
            @RequestBody Map<String, Object> payload
    ) {
        boolean result = false;
        try {
            result = ver.verify(payload, this.ref).get(); // wait for CompletableFuture
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, Boolean> response = new HashMap<>();
        response.put("token", result);
        return response;
    }

    @PostMapping("/user_courses")
    public Map<String, Set<String>> getUsersCourses(
            @RequestBody Map<String, Object> payload
    ) {
        CompletableFuture<List<String>> future;
        boolean result = false;
        try {
            result = ver.verify(payload, this.ref).get(); // wait for CompletableFuture
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (result) {
                //            it is guaranteed that user will have atleast one course as registered,
                //            if not it means the admin has not registered the user.
                Set<String> registeredCourses = uec
                        .getEnrolledCourses(payload, this.ref)
                        .get();
                return new HashMap<>() {
                    {
                        put("Courses", registeredCourses);
                    }
                };
            } else {
                return new HashMap<>() {
                    {
                        put("Courses", null);
                    }
                };
            }
        } catch (Exception e) {
            return new HashMap<>() {
                {
                    put("Courses", null);
                }
            };
        }
    }

    @PostMapping("/course")
    public Map<String, Map<String, Object>> getCourse(
            @RequestBody Map<String, Object> payload
    ) {
        System.out.println("Req");
        CompletableFuture<List<String>> future;
        boolean result = false;
        @SuppressWarnings("unchecked")
        Map<String, Object> _payload_ = (Map<String, Object>) payload.get(
                "payload"
        );
        try {
            result = ver
                    .verify(
                            new HashMap<>() {
                                {
                                    put("payload", _payload_.get("tokens"));
                                }
                            },
                            this.ref
                    )
                    .get(); // wait for CompletableFuture
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (result) {
                System.out.println("Auth user");

                String playlist = (String) _payload_.get("request");

                //                Check if user is enrolled in this course.
                @SuppressWarnings("unchecked")
                Boolean cf = uec
                        .isEnrolled(
                                playlist,
                                this.ref,
                                (ArrayList<String>) _payload_.get("tokens")
                        )
                        .get();
                return new HashMap<>() {
                    {
                        put("Stream", (cf ? cp.getPlaylist(playlist) : null));
                    }
                };
            } else {
                return new HashMap<>() {
                    {
                        put("Stream", null);
                    }
                };
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.out.println("Unauth");
            return new HashMap<>() {
                {
                    put("Stream", null);
                }
            };
        }
    }

    @PostMapping("terminate_session")
    public void termiateSession(@RequestBody String payload) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(payload);
        terminator.terminate(mapper.readValue(payload, Map.class), this.ref);
    }
}
