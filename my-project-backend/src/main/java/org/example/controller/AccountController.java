package org.example.controller;

import jakarta.annotation.Resource;
import org.example.entity.RestBean;
import org.example.entity.dto.Account;
import org.example.entity.vo.response.AccountVO;
import org.example.server.AccountServer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class AccountController {
    @Resource
    AccountServer server;

    @GetMapping("/info")
    RestBean<AccountVO> info(@RequestAttribute("id") String id){
        Account account = server.findAccountById(id);
        return RestBean.success(account.asViewObject(AccountVO.class));
    }
}
