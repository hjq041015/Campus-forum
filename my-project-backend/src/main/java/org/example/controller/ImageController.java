package org.example.controller;

import jakarta.annotation.Resource;
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
                                         @RequestParam("file")MultipartFile file) throws Exception {
       if (file.getSize() > 1024* 100)
        return     RestBean.failure(400,"头像大小不能超过100k");
       log.info("正在上传头像");
        String url = service.uploadAvatar(id, file);
        if (url != null) {
            log.info("图片上传成功,大小{}", file.getSize());
            return RestBean.success(url);

        }else {
            return RestBean.failure(400,"上传失败,请联系管理员");
        }
    }
}
