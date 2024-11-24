package org.example.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;

public interface ImageService {
    String uploadAvatar(int id , MultipartFile file) throws Exception;
    void fetchImageFromMinio(OutputStream stream, String image) throws Exception;

}
