package org.example.service.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.entity.dto.Account;
import org.example.entity.dto.AccountDetails;
import org.example.entity.vo.request.DetailSaveVO;
import org.example.mappers.AccountDetailsMapper;
import org.example.service.AccountDetailsServer;
import org.example.service.AccountServer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountDetailsServiceImpl extends ServiceImpl<AccountDetailsMapper, AccountDetails> implements AccountDetailsServer {
    @Resource
    AccountServer server;

    @Override
    public AccountDetails findAccountDetailsById(int id) {
        return this.getById(id);
    }


    // 保存用户详细信息
    @Override
    @Transactional
    public boolean saveAccountDetails(int id, DetailSaveVO vo) {
        Account account = server.findAccountByUsernameOrEmail(vo.getUsername());
        if(account == null || account.getId() == id) {
            server.update()
                    .eq("id",id)
                    .set("username",vo.getUsername())
                    .update();
            this.saveOrUpdate(new AccountDetails(id, vo.getGender(), vo.getQq(), vo.getWx(), vo.getPhone(), vo.getDesc()));
            return true;
        }
        return false;
    }
}
