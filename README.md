# 养老服务平台 Elderly‑Platform
## 📖项目简介
本项目是一套养老服务管理平台，采用前后端分离架构。
包含管理后台与用户前端，实现老人健康档案维护、养老活动预约、健康评估、信息公告等业务模块。

## 🛠技术栈
### 后端
- SpringBoot2
- MyBatis
- MySQL

### 前端
- Vue2
- Element‑UI

## 🚀部署运行
1. 导入根目录下数据库脚本 `eldercare_schema.sql`，初始化MySQL数据库
2. 修改后端yml配置文件，填写本机数据库账号密码
3. 启动后端SpringBoot服务
4. frontend‑admin（管理端）、frontend‑member（用户端）分别安装依赖启动

## 📂目录说明
- src：后端服务源码
- frontend‑admin：管理员后台前端
- frontend‑member：普通用户前端
- *.sql：数据库初始化脚本
