package org.example.Util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtils {
    @Value("${spring.security.jwt.key}")
    String key;

    @Value("${spring.security.jwt.expire}")
    int expire;

    @Resource
    StringRedisTemplate template;

    // 使一个给定的 JWT Token 失效
    public boolean invalidateJwt(String headerToken){
        DecodedJWT jwt = this.DecodedJWT(headerToken); // 使用DecodedJWT方法进行解码
        if (jwt == null) return false;
        String id = jwt.getId(); //
        deleteJwt(id,jwt.getExpiresAt()); // 调用这个方法将此JWT的id放入黑名单
        return true;  // 返回true就是jwt已成功失效
    }


    public boolean deleteJwt(String uuid, Date time) {
        if (this.isInvalidToken(uuid))
            return false;  // 如果已在黑名单中返回false
        Date now = new Date();
        long expire = Math.max(time.getTime()-now.getTime(),0); // 获取jwt剩余有效时间,如果过期时间早于现在时间则为0

        // 将jwt id 存入黑名单,设置其过期时间
        template.opsForValue().set(Const.JWT_BLACK_LIST + uuid,"",expire, TimeUnit.MILLISECONDS);

        return true;
    }

    // 检查jwt是否已经在黑名单中
    public boolean isInvalidToken(String uuid) {
       return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST + uuid));
    }


    public String createJwt(UserDetails details, int id, String username) {
        Algorithm algorithm = Algorithm.HMAC256(key);
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("id", id)
                .withClaim("name", username)
                // 这样处理后的结果是一个List<String>，包含用户的所有权限名称。然后，将这个权限列表作为authorities的值添加到JWT的claim中
                .withClaim("authorities", details.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(expiresTime())  //设置过期时间
                .withIssuedAt(new Date())   // 设置获取Token的时间
                .sign(algorithm);  // 签名
    }


    public Date expiresTime() {
        Calendar calendar = Calendar.getInstance(); //获取当前时间
        calendar.add(Calendar.HOUR, expire * 24); // 将当前时间加上有效期即为过期时间
        return calendar.getTime(); // 返回过期时间
    }


    public DecodedJWT DecodedJWT(String headerToken) {
       String token = this.convertToken(headerToken); // 获取去除前缀后端Token
       if (token == null) return null;
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build(); //创建一个 JWT 验证器，它使用前面定义的算法来验证 JWT 的签名和有效性。
        try {
            DecodedJWT verify = jwtVerifier.verify(token); //会验证传入的 token，如果签名无效或过期，抛出 JWTVerificationException 异常
           if(this.isInvalidToken(verify.getId())) return null; // 验证jwt是否为黑名单中的
            Date expiresAt = verify.getExpiresAt(); // 获取JWt的过期时间
            return new Date().after(expiresAt) ? null : verify; // 检查当前时间是否在过期时间之后。如果令牌已过期，返回 null；如果未过期，返回解码后的 DecodedJWT 对象。
        }catch (JWTVerificationException e) {
            return null;
        }

    }

    private String convertToken(String headerToken) {
        if (headerToken == null || !headerToken.startsWith("Bearer") || headerToken.length() <= 7)  { // 验证是否是正确格式的Token
            return null;
        }
        return headerToken.substring(7); // 去除Token的前缀
    }

    public UserDetails toUser(DecodedJWT jwt){
        Map<String, Claim> claims = jwt.getClaims(); // 从解码后的Jwt作为Map检索所有的信息

        // 将JWT中的信息转换为User对象
        return User
                .withUsername(claims.get("name").asString())
                .password("******")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
    }

    public Integer toId(DecodedJWT jwt) {
        Map<String, Claim> claims = jwt.getClaims();
       return claims.get("id").asInt();
    }
}
