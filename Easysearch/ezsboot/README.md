# 项目描述

SpringBoot 3.x 集成 Springboot-Data-Elasticsearch 访问 Easysearch 示例。

## 开发环境

- IntelliJ IDEA 2022.3.3 (Community Edition)
- SDKMAN (script: 5.18.2 native: 0.1.3)
- JDK 17.0.6-zulu
- Maven 3.9.0

> 在运行本项目前，请部署好 [Easysearch](docs/easysearch.md)  
> 本项目进行的具体调整请参考 [项目调整](docs/project.md)

## 配置修改

修改 src/main/resources/application.yml 中的用户名和密码

## 编译项目并运行

```bash
# 安装并检查 sdk (可选)
sdk version

# 安装jdk 并切换 (可选)
sdk install java 17.0.6-zulu
sdk use java 17.0.6-zulu

#检查 java 和 maven 版本信息
java -version
mvn -v

#编译项目
mvn clean install
```

## 测试项目

### 增加数据

> 通过 swagger 进行测试  
> 打开浏览器访问  
> http://localhost:8081/swagger-ui/index.html#/book-controller/addBook

请求示例数据如下

```json
{
  "title": "SpringBoot Easysearch 示例",
  "author": "极限科技",
  "county": "中国",
  "price": 666,
  "createTime": "2020-12-03T00:12:03.898Z"
}
```

### 搜索数据

> 打开浏览器访问  
> http://localhost:8081/book/search?key=Easysearch

## 其他用例

`使用 IntelliJ IDEA 直接运行相关测试用例即可`
