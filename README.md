# 雨花台区 · 技能互换 & 极简闲置平台 — 后端服务
> 项目仓库：私有仓库
> 开发人员：megatron
> 技术架构：SpringBoot + MySQL + MyBatis-Plus
> 适配终端：微信小程序端
> 项目定位：面向南京雨花台区本地社区，实现**技能互换、闲置物品流转、同城私信沟通**轻量化社区平台

---

## 一、项目简介
本项目为区域性社区服务后端系统，聚焦本地邻里轻量化需求：
1. 技能互换：用户发布自身擅长技能，实现互助交换
2. 闲置流转：极简二手闲置发布，减少资源浪费
3. 即时私信：用户一对一在线聊天沟通
4. 内容风控：内置敏感词过滤，保障社区内容合规
5. 内容审核：帖子多级审核机制，维护社区氛围
6. 操作留痕：全局操作日志AOP统一记录

整体采用前后端分离架构，后端提供标准化 RESTful 接口，
基于 JWT 实现无状态登录鉴权，适配微信小程序快速对接。

---

## 二、核心功能模块
- ✅ 微信小程序授权一键登录 / 注册
- ✅ 用户个人信息管理
- ✅ 技能帖子 & 闲置内容发布、查询、展示
- ✅ 内容敏感词实时拦截校验
- ✅ 帖子审核状态管控（待审核/通过/驳回）
- ✅ 用户一对一私信聊天
- ✅ 违规内容举报功能
- ✅ 图片本地上传与访问
- ✅ 全局统一异常处理 & 统一返回结果
- ✅ 接口登录拦截、权限校验
- ✅ 全局操作日志自动记录

---

## 三、技术栈
### 后端核心
- 核心框架：SpringBoot 3.x
- 数据库：MySQL 8.0
- ORM 框架：MyBatis-Plus
- 鉴权方案：JWT 令牌认证
- 内容风控：自定义敏感词工具类
- 日志处理：AOP 切面统一记录操作日志
- 统一响应：全局封装通用返回结果 R
- 异常处理：全局统一异常处理器

### 开发工具 & 环境
- IDE：IntelliJ IDEA
- 终端：Mac iTerm / 系统终端
- 版本控制：Git + GitHub 私有仓库
- 接口测试：Postman
- 前端适配：微信小程序

---

## 四、开发环境要求
- JDK：17+
- MySQL：8.0+
- Maven：3.6+
- 网络：本地开发无外网强制依赖
- 系统：MacOS / Windows 均可正常运行

---

## 五、数据库初始化
1. 本地 MySQL 新建数据库
```sql
CREATE DATABASE skill_exchange DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
2. 无需手动建表
   可配合 MyBatis-Plus 代码生成器 / 执行 SQL 脚本完成数据表初始化
3. 数据库配置文件位置：
   `src/main/resources/application.yml`

---

## 六、核心配置说明(application.yml)
### 1. 服务基础
- 服务运行端口：`8080`
- 全局编码：UTF-8 统一解决中文乱码

### 2. 数据库配置
```yaml
url: jdbc:mysql://localhost:3306/skill_exchange
username: [your_db_user]
password: [your_db_password]
```

### 3. JWT 配置
- 令牌有效期：7天
- 自定义加密密钥，保障接口安全

### 4. 文件上传
- 存储路径：项目根目录 `./uploads/`
- 访问前缀：`http://localhost:8080/uploads/`

### 5.微信小程序
预留 `appid / secret` 配置项，对接小程序登录授权

---

## 七、项目目录结构
```
com.example.skillexchange
├── aop          // 切面编程：操作日志记录
├── common       // 公共模块：统一返回、全局上下文、自定义异常
├── config       // 配置类：拦截器、MP配置、WebMvc配置
├── controller   // 控制层：所有接口请求入口
├── entity       // 数据库实体类
├── handler      // 全局异常统一处理
├── mapper       // 数据持久层
├── service      // 业务接口 & 业务实现类
└── utils        // 工具类：JWT、敏感词、微信API工具
```

---

## 八、权限与拦截机制
1. 所有需要登录的接口，统一通过 `LoginAuthInterceptor` 拦截
2. 基于请求头 `Authorization` 携带 JWT Token 完成鉴权
3. 未登录 / Token 过期 / 非法令牌 自动拦截并返回统一提示
4. 通过 `UserContext` 全局存储当前登录用户信息，解耦业务代码

---

## 九、接口规范
- 请求方式：RESTful 风格 GET / POST
- 统一返回格式：
    - `code`：业务状态码
    - `msg`：提示信息
    - `data`：业务数据
- 时间格式统一：`yyyy-MM-dd HH:mm:ss`

---

## 十、Git 版本管理（本地&GitHub）
> 本项目托管于 GitHub 私有仓库，代码全程云端备份

### 日常提交流程
```bash
# 1. 暂存所有修改
git add .

# 2. 本地提交（填写清晰备注）
git commit -m "描述本次修改内容"

# 3. 推送到远程私有仓库
git push
```

### 常用拓展命令
```bash
# 查看简洁提交记录
git log --oneline

# 安全回滚（保留本地代码）
git reset --soft 版本号
```

---

## 十一、项目启动步骤
1. 本地启动 MySQL，确保数据库 `skill_exchange` 存在
2. 检查 `application.yml` 数据库账号密码是否正确
3. IDEA 启动主类：`SkillExchangeApplication.java`
4. 访问地址：`http://localhost:8080`
5. 可通过 Postman 测试全部接口

---

## 十二、后续开发计划
- [ ] 完善小程序前端页面，完成前后端完整联调
- [ ] 完善后台管理简易功能
- [ ] 优化文件上传大小、格式限制
- [ ] 增加接口限流、简单安全防护
- [ ] 支持项目部署至云服务器，实现公网访问
- [ ] 上线微信小程序，提供社会使用

---

## 十三、版权说明
本项目为个人独立开发学习项目，
仓库为 GitHub 私有仓库，仅限个人学习与使用。
