# Campus Forum

**Campus Forum** 是一个基于 Spring Boot 开发的校园论坛系统，旨在为用户提供一个在线社区平台，用于交流和分享校园生活中的点滴。

## 功能特点

- **用户管理**：支持用户注册、登录和个人信息管理。
- **发帖与评论**：用户可以创建帖子、评论他人帖子。
- **版块划分**：根据主题分类帖子，方便用户浏览和讨论。
- **搜索功能**：支持关键字搜索，快速找到感兴趣的内容。
- **通知系统**：用户可接收评论、点赞等动态通知。
- **权限管理**：管理员可管理用户和帖子，维护社区秩序。

## 技术栈

- **后端**：采用Mybatis-Plus作为持久层框架，使用更便捷
采用Redis存储注册/重置操作验证码，带过期时间控制
采用RabbitMQ积压短信发送任务，再由监听器统一处理
采用SpringSecurity作为权限校验框架，手动整合Jwt校验方案
采用Redis进行IP地址限流处理，防刷接口
视图层对象和数据层对象分离，编写工具方法利用反射快速互相转换
错误和异常页面统一采用JSON格式返回，前端处理响应更统一
手动处理跨域，采用过滤器实现
使用Swagger作为接口文档自动生成，已自动配置登录相关接口
采用过滤器实现对所有请求自动生成雪花ID方便线上定位问题
针对于多环境进行处理，开发环境和生产环境采用不同的配置
日志中包含单次请求完整信息以及对应的雪花ID，支持文件记录
项目整体结构清晰，职责明确，注释全面，开箱即用
- **数据库**：MySQL,redis
- **前端**：HTML, CSS, JavaScript, vue
- **工具**：Maven, Git


