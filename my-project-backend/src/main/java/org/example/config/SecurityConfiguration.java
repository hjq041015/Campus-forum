package org.example.config;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.Util.JwtUtils;
import org.example.entity.RestBean;
import org.example.entity.vo.response.AuthorizeVO;
import org.example.filter.JwtAuthorizeFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfiguration {
    @Resource
    JwtUtils utils;

    @Resource
    JwtAuthorizeFilter jwtAuthorizeFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(conf ->{
                    conf.requestMatchers("/api/auth/**").permitAll()
                            .anyRequest().authenticated();
                })
                .formLogin(conf -> {
                    conf.loginProcessingUrl("/api/auth/login")
                            .successHandler(this::onAuthenticationSuccess)
                            .failureHandler(this::onAuthenticationFailure);
                })
                .logout(conf -> {
                    conf.logoutUrl("/api/auth/logout")
                            .logoutSuccessHandler(this::onLogoutSuccess);
                })
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(conf -> {
                    conf.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .addFilterBefore(jwtAuthorizeFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(conf -> {
                    conf.authenticationEntryPoint(this::UnAuthorized)
                            .accessDeniedHandler(this::onAccessDeny);
                })
                .build();
                }


    // 此方法在身份验证成功后调用
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        response.setContentType("application/json");
        request.setCharacterEncoding("utf-8");
        User user =(User) authentication.getPrincipal(); // 获取经过验证的user对象
        String token = utils.createJwt(user, 1, "Tom");    // 为user对象创建JWT
        AuthorizeVO vo = new AuthorizeVO();  // 创建一个响应对象(响应给前端),用于保存授权信息
        vo.setExpire(utils.expiresTime()); // 设置过期时间
        vo.setRole("");                     // 设置用户角色
        vo.setToken(token);                 // 添加JWt令牌
        vo.setUsername("Tom");              // 设置用户名
        response.getWriter().write(RestBean.success(vo).asJsonString());  // 将响应内容设为JSON格式表示响应成功
    }

    // 此方法在因权限不足或其他授权问题而拒绝访问时触发。
    public void onAccessDeny(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType("application/json");
        request.setCharacterEncoding("utf-8");
        // 发送一个响应，指示访问被禁止，并附上异常消息。
        response.getWriter().write(RestBean.forbidden(accessDeniedException.getMessage()).asJsonString());
    }

    // 当请求未通过正确的授权进行访问时调用该方法，例如用户未登录。
    public void UnAuthorized(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.setContentType("application/json");
        request.setCharacterEncoding("utf-8");
         // 发送一个未授权的响应，并附上异常消息。
        response.getWriter().write(RestBean.UnAuthorized(exception.getMessage()).asJsonString());
    }

    // 此方法在身份验证失败时触发，例如登录尝试无效时。
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException{
        response.setContentType("application/json");
        request.setCharacterEncoding("utf-8");
        response.getWriter().write(RestBean.failure(401,exception.getMessage()).asJsonString());
    }

    // 此方法在用户成功注销后调用。
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {
        response.setContentType("application/json");
        request.setCharacterEncoding("utf-8");
        PrintWriter writer = response.getWriter();
        String authorization = request.getHeader("Authorization");
        if(utils.invalidateJwt(authorization)) { // 验证jwt是否加入黑名单
            writer.write(RestBean.success().asJsonString());
        }else
            writer.write(RestBean.failure(400,"退出登录失败").asJsonString());
    }
}

