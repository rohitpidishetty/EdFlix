package com.edFlix.EdFlix.Service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.edFlix.EdFlix.Util.Playlist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@CrossOrigin
public class CoursePlaylist {


  @Autowired
  private Playlist.Java java;


//    Migration from azure to aws s3 buckets

  // Public method to get a playlist by course ID
  public Map<String, Object> getPlaylist(String courseId, String courseTimeOutAt, int load) {
    /**
     * {
     *      notes_and_source_codes: NOTES-URL,
     *      playlist: [...]
     *      courseTimeOutAt: futureTimeStamp
     * }
     * */

    Map<String, Object> payload = new HashMap<>();


    switch (courseId) {
      case "jpl3":

        int size = java.playlistSize;

        payload.put("notes_and_source_codes", "notes.sq");
        payload.put("playlist", java.generatePlaylist().subList(Math.min(load - 15, size), Math.min(load, size)));
        payload.put("courseTimeOutAt", courseTimeOutAt);

        return payload;

      case "pyl4":
        return null;
    }

    return null;
  }
}
