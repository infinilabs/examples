# 项目描述

SpringBoot 2.x 访问 Easysearch 示例。

## 开发环境

- IntelliJ IDEA 2022.3.3 (Community Edition)
- SDKMAN (script: 5.18.2 native: 0.1.3)
- JDK 11.0.21-kona
- Maven 3.9.0

> 在运行本项目前，请部署好 [Easysearch](../docs/easysearch.md)

## 配置修改

修改 src/main/resources/application.yml 中的用户名和密码

## 编译项目并运行

```bash
# 安装并检查 sdk (可选)
sdk version

# 安装jdk 并切换 (可选),其他发行版的 JDK 11 也可以
sdk install java 11.0.21-kona
sdk use java 11.0.21-kona

#检查 java 和 maven 版本信息
java -version
mvn -v

#编译项目
mvn clean install
```

## 测试项目

`使用 IntelliJ IDEA 直接运行相关测试用例即可`
