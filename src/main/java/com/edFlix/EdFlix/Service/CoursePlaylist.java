package com.edFlix.EdFlix.Service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@CrossOrigin
public class CoursePlaylist {

   private final String accountKey;
   private final String accountName;
   private final String containerName;

   private final StorageSharedKeyCredential credential;
   private final BlobServiceClient blobServiceClient;

   // Constructor injection for environment variables
   public CoursePlaylist(
           @Value("${ACCOUNT_KEY}") String accountKey,
           @Value("${ACCOUNT_NAME}") String accountName,
           @Value("${CONTAINER_NAME}") String containerName) {

       this.accountKey = Objects.requireNonNull(accountKey, "ACCOUNT_KEY cannot be null");
       this.accountName = Objects.requireNonNull(accountName, "ACCOUNT_NAME cannot be null");
       this.containerName = Objects.requireNonNull(containerName, "CONTAINER_NAME cannot be null");

       // Initialize Azure Blob credential and client
       this.credential = new StorageSharedKeyCredential(this.accountName, this.accountKey);
       this.blobServiceClient = new BlobServiceClientBuilder()
               .endpoint("https://" + this.accountName + ".blob.core.windows.net")
               .credential(this.credential)
               .buildClient();
   }

   // Generate a SAS URL for a blob
   public String sas_appended_url(String blobPath) {
       int expiryMinutes = 60;

       BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

       OffsetDateTime expiryTime = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(expiryMinutes);

       BlobSasPermission permissions = new BlobSasPermission()
               .setReadPermission(true)
               .setListPermission(true);

       BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions);

       String sasToken = containerClient.generateSas(sasValues);

       return String.format(
               "https://%s.blob.core.windows.net/%s/%s?%s",
               accountName, containerName, blobPath, sasToken
       );
   }

   // Static playlist map
   private final Map<String, List<Map<String, String>>> courses = new HashMap<>() {{
       put("cpl8", Arrays.asList(
               createLecture("1", "What is programming language ?", "sample/master.m3u8"),
               createLecture("2", "Why C programming language ?", "sample/master.m3u8"),
               createLecture("3", "How to declare variables ?", "sample/master.m3u8"),
               createLecture("4", "How to debug your code ?", "sample/master.m3u8")
       ));
   }};

   // Helper method to create lecture entries
   private Map<String, String> createLecture(String classNumber, String topic, String blobPath) {
       Map<String, String> lecture = new HashMap<>();
       lecture.put("class", classNumber);
       lecture.put("topic", topic);
       lecture.put("url", sas_appended_url(blobPath));
       return lecture;
   }

   // Java playlist generator
   private List<Map<String, String>> javaPlayList() {
       List<String> topics = Arrays.asList(
               "💻 Introduction to Java and Setup",
               "🏗️ Structure of a Java Program",
               "👋 Hello World Program in Java",
               "🔢 Variables and Data Storage in Java",
               "📊 Java Data Types Explained",
               "🖋️ Programming with Java Data Types",
               "🔄 Type Casting and Conversion in Java",
               "➕➖ Operators in Java",
               "🛠️ Operator Usage in Java Programs",
               "📜 Working with the String Class",
               "📐 Java Math Class and Utility Methods",
               "❓ Conditional Statements in Java",
               "🔀 Switch Statement in Java",
               "🔁 Loops in Java (for, while, do-while)",
               "⛔ Break and Continue Statements",
               "🗂️ Working with Arrays in Java",
               "🏛️ Java Class Structure and Basics",
               "🧩 Defining and Using Functions in Java Classes",
               "🛡️ Static Keyword in Java",
               "🧰 Method Overloading in Java",
               "🔁 Recursion in Java",
               "🏗️ Constructors in Java Classes",
               "📥 Runtime Arguments in Java",
               "🫵 The 'this' Keyword in Java",
               "🚫 The 'final' Keyword in Java",
               "📦 User-Defined Data Types in Java",
               "🧬 Inheritance in Java",
               "🔝 Using the 'super' Keyword",
               "🏠 Inner Classes in Java",
               "🧩 Abstract Classes in Java",
               "🔗 Interfaces in Java",
               "💡 Interface Implementation Example",
               "🔢 Enums in Java",
               "🖊️ User Input in Java (Scanner and BufferedReader)",
               "⚠️ Exception Handling in Java",
               "📖 Reading Files in Java",
               "✍️ Writing Files in Java",
               "🗑️ Deleting Files in Java",
               "🔌 Java I/O Streams Overview",
               "🗃️ Collections Framework in Java",
               "📝 ArrayList in Java",
               "🔗 LinkedList in Java",
               "🔒 HashSet in Java",
               "🌳 TreeSet in Java",
               "🧩 LinkedHashSet in Java",
               "📚 Stack in Java",
               "🛒 Queue in Java",
               "🎯 PriorityQueue in Java",
               "🗝️ HashMap in Java",
               "🌲 TreeMap in Java",
               "🔗 LinkedHashMap in Java",
               "🎯 Generics in Java",
               "⚖️ Comparable and Comparator in Java",
               "🔁 Callbacks in Java",
               "⚙️ Processes vs Threads in Java",
               "⏱️ Thread Life Cycle in Java",
               "🧵 Creating and Managing Threads in Java",
               "🔄 Runnable Interface in Java",
               "🔒 Synchronized Methods and Locks in Java",
               "🧰 Java APIs Overview and Usage"
       );

       List<Map<String, String>> playlist = new ArrayList<>();
       int classNumber = 1;
       for (String topic : topics) {
           playlist.add(createLecture(String.valueOf(classNumber), topic, "lec" + classNumber + "/master.m3u8"));
           classNumber++;
       }
       return playlist;
   }

   // Public method to get a playlist by course ID
   public List<Map<String, String>> getPlaylist(String courseId) {
       if ("jpl3".equals(courseId)) {
           return javaPlayList();
       }
       return courses.get(courseId);
   }
}




// -----------------------------------------------
// package com.edFlix.EdFlix.Service;

// import com.azure.storage.blob.BlobContainerClient;
// import com.azure.storage.blob.BlobServiceClient;
// import com.azure.storage.blob.BlobServiceClientBuilder;
// import com.azure.storage.blob.sas.BlobSasPermission;
// import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
// import com.azure.storage.common.StorageSharedKeyCredential;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;
// import org.springframework.web.bind.annotation.CrossOrigin;

// import java.time.OffsetDateTime;
// import java.time.ZoneOffset;
// import java.util.*;

// @Service
// @CrossOrigin
// public class CoursePlaylist {

//     private final String accountKey;
//     private final String accountName;
//     private final String containerName;

//     private final StorageSharedKeyCredential credential;
//     private final BlobServiceClient blobServiceClient;

//     public CoursePlaylist(
//             @Value("${ACCOUNT_KEY}") String accountKey,
//             @Value("${ACCOUNT_NAME}") String accountName,
//             @Value("${CONTAINER_NAME}") String containerName) {

//         this.accountKey = Objects.requireNonNull(accountKey, "ACCOUNT_KEY cannot be null");
//         this.accountName = Objects.requireNonNull(accountName, "ACCOUNT_NAME cannot be null");
//         this.containerName = Objects.requireNonNull(containerName, "CONTAINER_NAME cannot be null");

//         this.credential = new StorageSharedKeyCredential(this.accountName, this.accountKey);
//         this.blobServiceClient = new BlobServiceClientBuilder()
//                 .endpoint("https://" + this.accountName + ".blob.core.windows.net")
//                 .credential(this.credential)
//                 .buildClient();

//     }

//     // Generate a SAS URL for a blob
//     public String sas_appended_url(String blobPath) {
//         int expiryMinutes = 60;
//         BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

//         OffsetDateTime expiryTime = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(expiryMinutes);

//         BlobSasPermission permissions = new BlobSasPermission()
//                 .setReadPermission(true)
//                 .setListPermission(true);

//         BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions);

//         String sasToken = containerClient.generateSas(sasValues);

//         return String.format(
//                 "https://%s.blob.core.windows.net/%s/%s?%s",
//                 accountName, containerName, blobPath, sasToken
//         );
//     }

//     // Helper method to create lecture entries
//     private Map<String, String> createLecture(String classNumber, String topic, String blobPath) {
//         Map<String, String> lecture = new HashMap<>();
//         lecture.put("class", classNumber);
//         lecture.put("topic", topic);
//         lecture.put("url", sas_appended_url(blobPath));
//         return lecture;
//     }

//     // Java playlist generator
//     private List<Map<String, String>> javaPlayList() {
//         List<String> topics = Arrays.asList(
//                 "💻 Introduction to Java and Setup",
//                 "🏗️ Structure of a Java Program",
//                 "👋 Hello World Program in Java",
//                 "🔢 Variables and Data Storage in Java",
//                 "📊 Java Data Types Explained",
//                 "🖋️ Programming with Java Data Types",
//                 "🔄 Type Casting and Conversion in Java",
//                 "➕➖ Operators in Java",
//                 "🛠️ Operator Usage in Java Programs",
//                 "📜 Working with the String Class",
//                 "📐 Java Math Class and Utility Methods",
//                 "❓ Conditional Statements in Java",
//                 "🔀 Switch Statement in Java",
//                 "🔁 Loops in Java (for, while, do-while)",
//                 "⛔ Break and Continue Statements",
//                 "🗂️ Working with Arrays in Java",
//                 "🏛️ Java Class Structure and Basics",
//                 "🧩 Defining and Using Functions in Java Classes",
//                 "🛡️ Static Keyword in Java",
//                 "🧰 Method Overloading in Java",
//                 "🔁 Recursion in Java",
//                 "🏗️ Constructors in Java Classes",
//                 "📥 Runtime Arguments in Java",
//                 "🫵 The 'this' Keyword in Java",
//                 "🚫 The 'final' Keyword in Java",
//                 "📦 User-Defined Data Types in Java",
//                 "🧬 Inheritance in Java",
//                 "🔝 Using the 'super' Keyword",
//                 "🏠 Inner Classes in Java",
//                 "🧩 Abstract Classes in Java",
//                 "🔗 Interfaces in Java",
//                 "💡 Interface Implementation Example",
//                 "🔢 Enums in Java",
//                 "🖊️ User Input in Java (Scanner and BufferedReader)",
//                 "⚠️ Exception Handling in Java",
//                 "📖 Reading Files in Java",
//                 "✍️ Writing Files in Java",
//                 "🗑️ Deleting Files in Java",
//                 "🔌 Java I/O Streams Overview",
//                 "🗃️ Collections Framework in Java",
//                 "📝 ArrayList in Java",
//                 "🔗 LinkedList in Java",
//                 "🔒 HashSet in Java",
//                 "🌳 TreeSet in Java",
//                 "🧩 LinkedHashSet in Java",
//                 "📚 Stack in Java",
//                 "🛒 Queue in Java",
//                 "🎯 PriorityQueue in Java",
//                 "🗝️ HashMap in Java",
//                 "🌲 TreeMap in Java",
//                 "🔗 LinkedHashMap in Java",
//                 "🎯 Generics in Java",
//                 "⚖️ Comparable and Comparator in Java",
//                 "🔁 Callbacks in Java",
//                 "⚙️ Processes vs Threads in Java",
//                 "⏱️ Thread Life Cycle in Java",
//                 "🧵 Creating and Managing Threads in Java",
//                 "🔄 Runnable Interface in Java",
//                 "🔒 Synchronized Methods and Locks in Java",
//                 "🧰 Java APIs Overview and Usage"
//         );

//         List<Map<String, String>> playlist = new ArrayList<>();
//         int classNumber = 1;
//         for (String topic : topics) {
//             playlist.add(createLecture(String.valueOf(classNumber), topic, "lec" + classNumber + "/master.m3u8"));
//             classNumber++;
//         }
//         return playlist;
//     }

//     // Public method to get a playlist by course ID
//     public Map<String, Object> getPlaylist(String courseId, String courseTimeOutAt, int load) {
//         /**
//          * {
//          *      notes_and_source_codes: URL,
//          *      playlist: [...]
//          * }
//          * */

//         Map<String, Object> payload = new HashMap<>();

//         if ("jpl3".equals(courseId)) {
//             payload.put("notes_and_source_codes", sas_appended_url("notes/notes-and-programs.tar.sq"));
//             int size = javaPlayList().size();
// //            System.out.println(Math.min(load - 15, size) + " , " + Math.min(load, size));
//             payload.put("playlist", javaPlayList().subList(Math.min(load - 15, size), Math.min(load, size)));
//             payload.put("courseTimeOutAt", courseTimeOutAt);
//             return payload;
//         }
//         return null;
//     }
// }
