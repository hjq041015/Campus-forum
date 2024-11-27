package org.example.service.serviceImpl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.minio.*;
import jakarta.annotation.Resource;
import org.apache.commons.compress.utils.IOUtils;
import org.example.Util.Const;
import org.example.Util.FlowUtil;
import org.example.entity.dto.Account;
import org.example.entity.dto.ImageStore;
import org.example.mappers.AccountMapper;
import org.example.mappers.ImageStoreMapper;
import org.example.service.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class ImageServiceImpl extends ServiceImpl<ImageStoreMapper, ImageStore> implements ImageService  {

    private static final Logger log = LoggerFactory.getLogger(ImageServiceImpl.class);
    private final SimpleDateFormat format = new SimpleDateFormat("yyyMMdd");
    @Resource
    MinioClient client;

    @Resource
    AccountMapper mapper;

    @Resource
    FlowUtil util;

    @Override
    public String uploadAvatar(int id, MultipartFile file) throws Exception {
        String imageName = UUID.randomUUID().toString().replace("-", "");
        imageName = "/avatar/" + imageName;
        PutObjectArgs args = PutObjectArgs.builder()
                .bucket("study")
                .stream(file.getInputStream(), file.getSize(), -1)
                .object(imageName)
                .build();
        try {
            client.putObject(args);
            String avatar = mapper.selectById(id).getAvatar();
            this.deleteAvatar(avatar);
            if(mapper.update(null, Wrappers.<Account>update()
                    .eq("id", id).set("avatar", imageName)) > 0) {
                return imageName;
            } else
                return null;
        } catch (Exception e) {
            log.error("图片上传出现问题: {}", e.getMessage(), e);
            return null;
        }
    }

      @Override
    public String uploadImage(int id, MultipartFile file) throws Exception {
       String key = Const.FORUM_IMAGE_COUNTER + id;
       if (!util.limitPeriodCounterCheck(key,20,3600)) {
           return null;
       }
       String imageName = UUID.randomUUID().toString().replace("-", "");
       Date date = new Date();
       imageName = "/cache/" + format.format(date) + "/" + imageName;
       PutObjectArgs args = PutObjectArgs.builder()
                .bucket("study")
                .stream(file.getInputStream(), file.getSize(), -1)
                .object(imageName)
                .build();
       try {
           client.putObject(args);
           if(this.save(new ImageStore(id,imageName,date))) {
               return imageName;
           }else return null;
       }catch (Exception e) {
           log.error("图片上传出现问题:{}", e.getMessage(), e);
           return null;
       }

    }

    @Override
    public void fetchImageFromMinio(OutputStream stream, String image) throws Exception {
        GetObjectArgs args = GetObjectArgs.builder()
                .bucket("study")
                .object(image)
                .build();
        GetObjectResponse response = client.getObject(args);
        IOUtils.copy(response,stream);
    }

    private void deleteAvatar(String avatar) throws Exception{
        if (avatar == null ||avatar.isEmpty()) return;
        RemoveObjectArgs args = RemoveObjectArgs.builder()
                .bucket("study")
                .object(avatar)
                .build();
        client.removeObject(args);
    }




}
