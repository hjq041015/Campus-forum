package org.example.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.RestBean;
import org.example.service.ImageService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/image")
public class ImageController {

    @Resource
    ImageService service;

    @PostMapping("/avatar")
    public RestBean<String> uploadAvatar(@RequestAttribute("id") int id,
                                         @RequestParam("file")MultipartFile file,
                                         HttpServletResponse response) throws Exception {
       if (file.getSize() > 1024* 100)
        return     RestBean.failure(400,"头像大小不能超过100kb");
       log.info("正在上传头像");
        String url = service.uploadAvatar(id, file);
        if (url != null) {
            log.info("头像上传成功,大小{}", file.getSize());
            return RestBean.success(url);

        }else {
            response.setStatus(400);
            return RestBean.failure(400,"上传失败,请联系管理员");
        }
    }

    @PostMapping("/cache")
    public RestBean<String> uploadImage(@RequestAttribute("id") int id,
                                         @RequestParam("file")MultipartFile file) throws Exception {
        if (file.getSize() > 1024 * 1024 * 5)
             return     RestBean.failure(400,"图片大小不能超过5MB");
       log.info("正在上传图片");
       String url = service.uploadImage(id, file);
       if (url != null) {
            log.info("图片上传成功,大小{}", file.getSize());
            return RestBean.success(url);

        }else {
            return RestBean.failure(400,"上传失败,请联系管理员");
        }
    }
}
