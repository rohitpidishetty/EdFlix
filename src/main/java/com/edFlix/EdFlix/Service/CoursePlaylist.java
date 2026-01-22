package com.edFlix.EdFlix.Service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.google.api.client.util.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@CrossOrigin
public class CoursePlaylist {

    @Value("${ACCOUNT_KEY}")
    private String accountKey;


    @Value("${ACCOUNT_NAME}")
    private String accountName;


    @Value("${CONTAINER_NAME}")
    private String containerName;

    public String video_url(String blobPath) {

        int limit = 60;

        BlobServiceClient blobServiceClient =
                new BlobServiceClientBuilder()
                        .endpoint("https://" + accountName + ".blob.core.windows.net")
                        .credential(
                                new com.azure.storage.common.StorageSharedKeyCredential(
                                        accountName, accountKey))
                        .buildClient();

        BlobContainerClient containerClient =
                blobServiceClient.getBlobContainerClient(containerName);

        OffsetDateTime expiryTime =
                OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(limit);

        BlobSasPermission permissions = new BlobSasPermission()
                .setReadPermission(true)
                .setListPermission(true);

        BlobServiceSasSignatureValues sasValues =
                new BlobServiceSasSignatureValues(expiryTime, permissions);

        String sasToken = containerClient.generateSas(sasValues);

        return new StringBuilder()
                .append("https://")
                .append(accountName)
                .append(".blob.core.windows.net/")
                .append(containerName)
                .append("/")
                .append(blobPath)
                .append("?")
                .append(sasToken)
                .toString();
    }

    private Map<String, List<Map<String, String>>> courses = new HashMap<>() {
        {
            put("cpl8", Arrays.asList(
                    new HashMap<>() {
                        {
                            put("class", "1");
                            put("topic", "What is programming language ?");
                            put("url", video_url("sample/master.m3u8"));
                        }
                    },
                    new HashMap<>() {
                        {
                            put("class", "2");
                            put("topic", "Why C programming language ?");
                            put("url", video_url("sample/master.m3u8"));
                        }
                    },
                    new HashMap<>() {
                        {
                            put("class", "3");
                            put("topic", "How to declare variables ?");
                            put("url", video_url("sample/master.m3u8"));
                        }
                    },
                    new HashMap<>() {
                        {
                            put("class", "4");
                            put("topic", "How to debug your code ?");
                            put("url", video_url("sample/master.m3u8"));
                        }
                    }
            ));
        }
    };

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

        List<Map<String, String>> payload = new ArrayList<>();
        int classNumber = 1;
        for (String lecture : topics) {
            HashMap<String, String> url = new HashMap<>();
            url.put("class", String.valueOf(classNumber));
            url.put("topic", lecture);
            url.put("url", video_url("lec" + String.valueOf(classNumber) + "/master.m3u8"));
            payload.add(url);
            classNumber++;
        }
        return payload;
    }

    public List<Map<String, String>> getPlaylist(String courseId) {

        if (courseId.equals("jpl3")) {
            return javaPlayList();
        }

        return this.courses.get(courseId);
    }
}
