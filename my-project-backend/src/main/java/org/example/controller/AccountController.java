package org.example.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.Util.ControllerUtil;
import org.example.entity.RestBean;
import org.example.entity.dto.Account;
import org.example.entity.dto.AccountDetails;
import org.example.entity.vo.request.ChangePasswordVO;
import org.example.entity.vo.request.DetailSaveVO;
import org.example.entity.vo.request.EmailModifyVO;
import org.example.entity.vo.request.SavePrivacyVO;
import org.example.entity.vo.response.AccountDetailsVO;
import org.example.entity.vo.response.AccountPrivacyVO;
import org.example.entity.vo.response.AccountVO;
import org.example.service.AccountDetailsServer;
import org.example.service.AccountPrivacyService;
import org.example.service.AccountServer;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@RestController
@RequestMapping("/api/user")
public class AccountController {
    @Resource
    AccountServer accountServer;

    @Resource
    AccountDetailsServer detailsServer;

    @Resource
    AccountPrivacyService privacyService;

    @Resource
    ControllerUtil util;

    @GetMapping("/info")
    RestBean<AccountVO> info(@RequestAttribute("id") String id){
        Account account = accountServer.findAccountById(id);
        return RestBean.success(account.asViewObject(AccountVO.class));
    }

    @GetMapping("/details")
    RestBean<AccountDetailsVO> details(@RequestAttribute("id") int id) {
        AccountDetails details = Optional
                .ofNullable(detailsServer.findAccountDetailsById(id))
                .orElseGet(AccountDetails::new);
        return RestBean.success(details.asViewObject(AccountDetailsVO.class));
    }

    @PostMapping("save-details")
    RestBean<Void> saveDetails(@RequestAttribute("id") int id,
                               @RequestBody @Valid DetailSaveVO vo) {
        boolean success = detailsServer.saveAccountDetails(id, vo);
        return success ? RestBean.success() : RestBean.failure(400,"此用户已被其他用户使用,请重新更换");
    }

    @PostMapping("/modify-email")
    public RestBean<Void> modifyEmail(@RequestBody @Valid EmailModifyVO vo,
                                      @RequestAttribute("id") int id) {
        return util.messageHander(()->
            accountServer.modifyEmail(vo,id));
    }

    @PostMapping("/change-password")
    public RestBean<Void> changePassword(@RequestAttribute("id") int id,
                                         @RequestBody @Valid ChangePasswordVO vo) {
        return util.messageHander(() ->
                accountServer.changePassword(id,vo));
    }

    @PostMapping("save-privacy")
    public RestBean<Void> savePrivacy(@RequestAttribute("id") int id,
                                      @RequestBody @Valid SavePrivacyVO vo) {
        privacyService.savePrivacy(id, vo);
        return RestBean.success();
    }

    @GetMapping("/privacy")
    public RestBean<AccountPrivacyVO> privacy(@RequestAttribute("id") int id) {
        return RestBean.success(privacyService.accountPrivacy(id).asViewObject(AccountPrivacyVO.class));
    }



}
