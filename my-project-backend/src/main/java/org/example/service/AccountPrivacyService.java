package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.dto.AccountPrivacy;
import org.example.entity.vo.request.SavePrivacyVO;

public interface AccountPrivacyService extends IService<AccountPrivacy> {
    void savePrivacy(int id, SavePrivacyVO vo);
    AccountPrivacy accountPrivacy(int id);
}
