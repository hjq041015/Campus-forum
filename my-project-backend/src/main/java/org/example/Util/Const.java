package org.example.Util;

public class Const {
    //JWT令牌
    public static final String JWT_BLACK_LIST = "jwt:blacklist:";

    //过滤器优先级
    public static final int FLOW_ORDER = -101;
    public static final int CORS_ORDER = -102;


    // 邮箱验证相关
    public static final String VERIFY_EMAIL_LIMIT = "verify:email:limit";
    public static final String VERIFY_EMAIL_DATA = "verify:email:data";

    // 请求频率限制
    public static final String FLOW_LIMIT_COUNT = "limit_count";
    public static final String FLOW_LIMIT_BLOCK = "limit_block";

    // 论坛相关
     public final static String FORUM_WEATHER_CACHE = "weather:cache:";
     public final static String FORUM_IMAGE_COUNTER = "forum:image:";
     public final static String FORUM_CREATE_COUNTER = "forum:create:";
     public final static String FORUM_TOPIC_PREVIEW_CACHE = "topic:preview:";
     public final static String FORUM_TOPIC_COMMENT = "topic:comment:";

     //用户角色
    public final static String ROLE_DEFAULT = "user";
    public final static String ROLE_ADMIN = "admin";
}
