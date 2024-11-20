package org.example.controller;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.example.entity.RestBean;
import org.example.entity.vo.request.ConfirmResetVO;
import org.example.entity.vo.request.EmailModifyVO;
import org.example.entity.vo.request.EmailRegistVO;
import org.example.entity.vo.request.EmailResetVO;
import org.example.service.AccountServer;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Supplier;

@Validated
@RestController
@RequestMapping("api/auth")
public class AuthorizationController {

    @Resource
    AccountServer server;

    @RequestMapping("/ask-code")
    public RestBean<Void> askVerifyCode(@RequestParam @Email String email,
                                        @RequestParam @Pattern(regexp = "(register|reset|modify)") String type,
                                        HttpServletRequest request) {
        return this.messageHander(() -> server.registEmailVerifyCode(type,email,request.getRemoteAddr()));
    }

    @PostMapping("/register")
    public RestBean<Void> register(@RequestBody @Validated EmailRegistVO vo){
      return this.messageHander(() -> server.registEmailAccount(vo));
    }

    @PostMapping("/reset-confirm")
    public RestBean<Void> resetConfirm(@RequestBody @Validated ConfirmResetVO vo) {
        return this.messageHander(() ->server.resetConfirm(vo));
    }

    @PostMapping("/reset-password")
    public RestBean<Void> resetPassword(@RequestBody @Valid EmailResetVO vo){
        return this.messageHander(() ->
                server.resetEmailAccountPassword(vo));
    }


    private <T> RestBean<T> messageHander(Supplier<String> action) {
        String message = action.get();
        if (message == null) {
            return RestBean.success();
        }else {
            return RestBean.failure(400,message);
        }
    }
}

