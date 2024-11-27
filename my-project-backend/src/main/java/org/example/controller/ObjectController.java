package org.example.controller;

import io.minio.errors.ErrorResponseException;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.entity.RestBean;
import org.example.service.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ObjectController {

    private static final Logger log = LoggerFactory.getLogger(ObjectController.class);
    @Resource
    ImageService service;

    @GetMapping("/images/**")
    public void imageFetch(HttpServletResponse response , HttpServletRequest request) throws Exception {
        response.setHeader("Content-Type","image/jpg");
         this.fetchImage(response,request);
    }


    private void fetchImage(HttpServletResponse response , HttpServletRequest request) throws Exception {
        String imagePath = request.getServletPath().substring(7);
        ServletOutputStream stream = response.getOutputStream();
        if (imagePath.length() < 13 ) {
            response.setStatus(404);
            stream.print(RestBean.failure(404,"NOt found").toString());
        }else {
            try {
                service.fetchImageFromMinio(stream,imagePath);
                // 设置缓存过期时间,加上这个标头,浏览器会自动开启缓存
                response.setHeader("Cache-Control", "max-age=2592000");
                response.setHeader("Content-Type","image/jpg");
            }catch (ErrorResponseException e) {
                if (e.response().code() == 404) {
                    response.setStatus(404);
                    stream.print(RestBean.failure(404,"NOt found").toString());
                }else {
                    log.error("从Minio获取图片异常:{}", e.getMessage(), e);
                }
            }
        }
    }
}


