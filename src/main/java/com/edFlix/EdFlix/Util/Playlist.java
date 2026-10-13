package com.edFlix.EdFlix.Util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;
import java.util.*;


@Component
public final class Playlist {


  @Autowired
  private S3Client s3Client;

  @Autowired
  private S3Presigner s3Presigner;

  private final static String bucket = "edflix-lectures";

  private static Map<String, String> createLecture(String classNumber, String topic, String url) {
    Map<String, String> lecture = new HashMap<>();
    lecture.put("class", classNumber);
    lecture.put("topic", topic);
    lecture.put("url", url);
    return lecture;
  }

  @Component
  public class Java {

    private final List<String> topics = Arrays.asList("💻 Introduction to Java and Setup", "🏗️ Structure of a Java Program", "👋 Hello World Program in Java", "🔢 Variables and Data Storage in Java", "📊 Java Data Types Explained", "🖋️ Programming with Java Data Types", "🔄 Type Casting and Conversion in Java", "➕➖ Operators in Java", "🛠️ Operator Usage in Java Programs", "📜 Working with the String Class", "📐 Java Math Class and Utility Methods", "❓ Conditional Statements in Java", "🔀 Switch Statement in Java", "🔁 Loops in Java (for, while, do-while)", "⛔ Break and Continue Statements", "🗂️ Working with Arrays in Java", "🏛️ Java Class Structure and Basics", "🧩 Defining and Using Functions in Java Classes", "🛡️ Static Keyword in Java", "🧰 Method Overloading in Java", "🔁 Recursion in Java", "🏗️ Constructors in Java Classes", "📥 Runtime Arguments in Java", "🫵 The 'this' Keyword in Java", "🚫 The 'final' Keyword in Java", "📦 User-Defined Data Types in Java", "🧬 Inheritance in Java", "🔝 Using the 'super' Keyword", "🏠 Inner Classes in Java", "🧩 Abstract Classes in Java", "🔗 Interfaces in Java", "💡 Interface Implementation Example", "🔢 Enums in Java", "🖊️ User Input in Java (Scanner and BufferedReader)", "⚠️ Exception Handling in Java", "📖 Reading Files in Java", "✍️ Writing Files in Java", "🗑️ Deleting Files in Java", "🔌 Java I/O Streams Overview", "🗃️ Collections Framework in Java", "📝 ArrayList in Java", "🔗 LinkedList in Java", "🔒 HashSet in Java", "🌳 TreeSet in Java", "🧩 LinkedHashSet in Java", "📚 Stack in Java", "🛒 Queue in Java", "🎯 PriorityQueue in Java", "🗝️ HashMap in Java", "🌲 TreeMap in Java", "🔗 LinkedHashMap in Java", "🎯 Generics in Java", "⚖️ Comparable and Comparator in Java", "🔁 Callbacks in Java", "⚙️ Processes vs Threads in Java", "⏱️ Thread Life Cycle in Java", "🧵 Creating and Managing Threads in Java", "🔄 Runnable Interface in Java", "🔒 Synchronized Methods and Locks in Java", "🧰 Java APIs Overview and Usage");

    public int playlistSize = topics.size();


    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Presigner s3Presigner;

    public List<Map<String, String>> generatePlaylist() {

      ListObjectsV2Request request = ListObjectsV2Request.builder()
          .bucket("edflix-lectures")
          .prefix("Advanced_Java/")
          .build();

      List<String> objects = new ArrayList<>();

      for (S3Object object : s3Client.listObjectsV2Paginator(request).contents()) {

        String key = object.key();

        if (key.endsWith("master.m3u8"))
          objects.add(key);

      }

      objects.sort((a, b) -> {

        String token_a = a.substring("Advanced_Java/".length());
        String token_b = b.substring("Advanced_Java/".length());

        return Integer.compare(
            Integer.parseInt(token_a.substring(0, token_a.indexOf("."))),
            Integer.parseInt(token_b.substring(0, token_b.indexOf(".")))
        );
      });

      List<Map<String, String>> playlist = new ArrayList<>();

      int classNumber = 1;

      for (String key : objects) {

        if (classNumber > topics.size())
          break;


        String url = UriComponentsBuilder
            .fromUriString("https://3-139-72-215.sslip.io/api/hls/playlist")
            .queryParam("key", key)
            .build()
            .encode()
            .toUriString();

        playlist.add(
            createLecture(
                String.valueOf(classNumber),
                topics.get(classNumber - 1),
                url
            )
        );

        classNumber++;
      }

      return playlist;
    }}
}
