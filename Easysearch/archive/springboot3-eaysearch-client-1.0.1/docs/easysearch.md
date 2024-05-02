# Easysearch 部署与配置调整

> 建议采用 jdk 17 运行 Easysearch，[下载地址](https://release.infinilabs.com/easysearch/jdk/)

Easysearch [部署参考](https://infinilabs.com/docs/latest/easysearch/getting-started/install/)

## 配置调整

- 关闭 https，采用 http 及用户名和密码的方式
- 开启 Elasticsearch 兼容性参数 `elasticsearch.api_compatibility: true`

[配置文件示例](config/easysearch.yml)

> 其他参考：[备份还原](https://infinilabs.com/docs/latest/easysearch/references/management/snapshot-restore/)
