package org.example.service.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.entity.dto.AccountPrivacy;
import org.example.entity.vo.request.SavePrivacyVO;
import org.example.mappers.AccountPrivacyMapper;
import org.example.service.AccountPrivacyService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountPrivacyServiceImpl extends ServiceImpl<AccountPrivacyMapper, AccountPrivacy> implements AccountPrivacyService {
    @Override
    public void savePrivacy(int id, SavePrivacyVO vo) {
        // 先判断是否已经进行了隐私设置,如果没有则创建隐私设置对象
        AccountPrivacy privacy = Optional.ofNullable(this.getById(id)).orElse(new AccountPrivacy(id));
        boolean status = vo.isStatus();
        switch (vo.getType()) {
            case "phone" ->privacy.setPhone(status);
            case "qq" ->privacy.setQq(status);
            case "wx" ->privacy.setWx(status);
            case "email" ->privacy.setEmail(status);
            case "gender" ->privacy.setGender(status);
        }
        this.saveOrUpdate(privacy);
    }

    @Override
    public AccountPrivacy accountPrivacy(int id) {
        return Optional.ofNullable(this.getById(id)).orElse(new AccountPrivacy(id));
    }
}
