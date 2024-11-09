package org.example.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.Util.JwtUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthorizeFilter extends OncePerRequestFilter {
    @Resource
    JwtUtils utils;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization"); // 从http请求头中检索Authorization标头,此标头通常包含用于身份验证的JWT。令牌通常以Bearer <token>格式传递。
        DecodedJWT jwt = utils.DecodedJWT(authorization); // 将jwt解码
        if (jwt != null) {
            UserDetails user = utils.toUser(jwt); // 如果JWT存在,将JWT转化为UserDetails对象


             // 这是 Spring Security 使用的Authentication的实现。它包括UserDetails对象、密码为null （因为这里不需要）以及从user.getAuthorities()获取的用户权限（权限/角色）。
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities());

            // 这里是根据传入的验证对象来设置authentication对象的详细信息
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            //此行将authentication对象放入SecurityContextHolder中，这是 Spring Security 中安全相关数据的中央存储。通过设置此身份验证，当前请求被视为已通过身份验证
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 将JWT的id添加到request中
            request.setAttribute("id",utils.toId(jwt));
        }

        //最后，这会将请求和响应对象传递到链中的下一个过滤器。这在基于过滤器的处理中至关重要，因为它允许请求在过滤器完成处理后继续进行。
        filterChain.doFilter(request,response);

    }

}
