package org.example.service.serviceImpl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.entity.dto.Notification;
import org.example.entity.vo.response.NotificationVO;
import org.example.mappers.NotificationMapper;
import org.example.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {


    @Override
    public List<NotificationVO> findUserNotification(int uid) {
       return this.list(Wrappers.<Notification>query().eq("uid",uid))
               .stream()
               .map(notification -> notification.asViewObject(NotificationVO.class))
               .toList();
    }

    @Override
    public void deleteUserNotification(int id, int uid) {
        this.remove(Wrappers.<Notification>query().eq("id",id).eq("uid",uid));
    }

    @Override
    public void deleteUserAllNotification(int uid) {
        this.remove(Wrappers.<Notification>query().eq("uid",uid));
    }

    @Override
    public void addNotification(int uid, String title, String content, String type, String url) {
        Notification notification = new Notification();
        notification.setUid(uid);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setUrl(url);
        this.save(notification);
    }
}
