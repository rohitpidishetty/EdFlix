package com.edFlix.EdFlix.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/hls")
public class HlsController {

  @Autowired
  private S3Client s3Client;

  @Autowired
  private S3Presigner s3Presigner;

  private static final String BUCKET = "edflix-lectures";

  private static final Pattern URI_ATTRIBUTE =
      Pattern.compile("URI=\"([^\"]+)\"");

  @GetMapping(
      value = "/playlist",
      produces = "application/vnd.apple.mpegurl"
  )
  public ResponseEntity<String> getPlaylist(
      @RequestParam String key) {

    if (!key.startsWith("Advanced_Java/")
        || !key.endsWith(".m3u8")
        || key.contains("\\")
        || key.contains("..")) {
      return ResponseEntity.badRequest().build();
    }



    try {
      GetObjectRequest request = GetObjectRequest.builder()
          .bucket(BUCKET)
          .key(key)
          .build();

      String content = s3Client.getObjectAsBytes(request)
          .asUtf8String();

      StringBuilder rewritten = new StringBuilder();

      for (String line : content.split("\\R")) {

        String trimmed = line.trim();

        if (trimmed.isEmpty()) {
          rewritten.append("\n");
          continue;
        }

        if (trimmed.startsWith("#")) {
          Matcher matcher = URI_ATTRIBUTE.matcher(line);
          StringBuffer result = new StringBuffer();

          while (matcher.find()) {

            String uri = matcher.group(1);

            String url = resolveUrl(key, uri);

            matcher.appendReplacement(
                result,
                Matcher.quoteReplacement(
                    "URI=\"" + url + "\""
                )
            );
          }

          matcher.appendTail(result);
          rewritten.append(result).append("\n");

        } else {

          rewritten.append(resolveUrl(key, trimmed))
              .append("\n");
        }
      }

      return ResponseEntity.ok()
          .contentType(MediaType.parseMediaType(
              "application/vnd.apple.mpegurl"
          ))
          .body(rewritten.toString());

    } catch (NoSuchKeyException e) {
      return ResponseEntity.notFound().build();
    }
  }

  private String resolveUrl(String parentKey, String reference) {

    reference = reference.replace("\\", "/");

    if (reference.startsWith("http://")
        || reference.startsWith("https://")
        || reference.startsWith("//")
        || reference.startsWith("/")) {
      throw new IllegalArgumentException(
          "Unsupported HLS reference: " + reference
      );
    }

    String key = Paths.get(parentKey)
        .getParent()
        .resolve(reference)
        .normalize()
        .toString()
        .replace("\\", "/");

    if (!key.startsWith("Advanced_Java/") || key.contains("..")) {
      throw new IllegalArgumentException("Invalid S3 path");
    }


    if (key.endsWith(".m3u8")) {

      return UriComponentsBuilder
          .fromUriString("http://localhost:8080/api/hls/playlist")
          .queryParam("key", key)
          .build()
          .encode()
          .toUriString();
    }


    GetObjectRequest request = GetObjectRequest.builder()
        .bucket(BUCKET)
        .key(key)
        .build();

    GetObjectPresignRequest presignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofHours(1))
            .getObjectRequest(request)
            .build();

    return s3Presigner
        .presignGetObject(presignRequest)
        .url()
        .toString();
  }
}