package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.dto.ImageStore;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;

public interface ImageService  extends IService<ImageStore> {
    String uploadAvatar(int id , MultipartFile file) throws Exception;
    void fetchImageFromMinio(OutputStream stream, String image) throws Exception;
    String uploadImage(int id , MultipartFile file) throws Exception;
}
