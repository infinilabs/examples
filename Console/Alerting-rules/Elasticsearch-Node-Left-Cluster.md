# Elasticsearch node left cluster

## Rule Variables
| Field        | Descriction   |  eg  |
| --------   | -----  | ----  |
| ${RESOURCE_ID}        |   The Elasticsearch cluster_id   |   c6abfdovi074mgr185m2   |
| ${RESOURCE_NAME}        |    The Elasticsearch cluster_name    |  es-v710  |
| ${INFINI_CONSOLE_ENDPOINT}        |    The host address of the current Console UI    |  http://192.168.3.201:9000  |
| ${WECHAT_WEBHOOK_ENDPOINT}        |    The webhook address of the notification channel    |  https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx  |

## Rule Template
Note: The following rule template content (available only after replacing the placeholder variable) can be directly copied to Console Command for execution to quickly create an rule.

```sh
#The `id` value is consistent with the `_id` value
POST .infini_alert-rule/_doc/cbp20n2anisjmu4gehc5
{
    "id": "cbp20n2anisjmu4gehc5",
    "created": "2022-08-09T08:52:44.63345561Z",
    "updated": "2022-08-09T08:52:44.633455664Z",
    "name": "Elasticsearch node left cluster",
    "enabled": false,
    "resource": {
        "resource_id": "${RESOURCE_ID}",
        "resource_name": "${RESOURCE_NAME}",
        "type": "elasticsearch",
        "objects": [
            ".infini_node"
        ],
        "filter": {},
        "raw_filter": {
            "match_phrase": {
                "metadata.labels.status": "unavailable"
            }
        },
        "time_field": "timestamp",
        "context": {
            "fields": null
        }
    },
    "metrics": {
        "bucket_size": "1m",
        "groups": [
            {
                "field": "metadata.cluster_id",
                "limit": 5
            },
            {
                "field": "metadata.node_id",
                "limit": 50
            }
        ],
        "formula": "a",
        "items": [
            {
                "name": "a",
                "field": "metadata.labels.status",
                "statistic": "count"
            }
        ],
        "format_type": "num",
        "expression": "count(metadata.labels.status)",
        "title": "Elasticsearch node left cluster",
        "message": "Priority:{{.priority}}\nTimestamp:{{.timestamp | datetime_in_zone \"Asia/Shanghai\"}}\nRuleID:{{.rule_id}}\nEventID:{{.event_id}}\n{{range .results}}\nClusterID:{{index .group_values 0}}; \nNodeID:{{index .group_values 1}}; \n{{end}}"
    },
    "conditions": {
        "operator": "any",
        "items": [
            {
                "minimum_period_match": 1,
                "operator": "gte",
                "values": [
                    "1"
                ],
                "priority": "critical"
            }
        ]
    },
    "channels": {
        "enabled": true,
        "normal": [
              {
                "created": "2022-08-09T08:52:44.63345561Z",
                "updated": "2022-08-09T08:52:44.63345561Z",
                "name": "Wechat",
                "type": "webhook",
                "webhook": {
                  "header_params": {
                    "Content-Type": "application/json"
                  },
                  "method": "POST",
                  "url": "${WECHAT_WEBHOOK_ENDPOINT}",
                  "body": "{\n    \"msgtype\": \"markdown\",\n    \"markdown\": {\n        \"content\": \"Incident [#{{.event_id}}](${INFINI_CONSOLE_ENDPOINT}/#/alerting/alert/{{.event_id}}) is ongoing\\n{{.title}}\\n\n         {{range .results}}\n         >ClusterID:<font color=\\\"comment\\\">{{index .group_values 0}}</font>\n        >NodeID:<font color=\\\"comment\\\">{{index .group_values 1}}</font>\n         >Priority:<font color=\\\"comment\\\">{{.priority}}</font>\n         >Link:[View Cluster Monitoring](${INFINI_CONSOLE_ENDPOINT}/#/cluster/overview/{{ index .group_values 0}}/nodes/{{ index .group_values 1}}) \n         {{end}}\"\n    }\n}\n"
                }
              }
            ],
        "throttle_period": "1h",
        "accept_time_range": {
            "start": "00:00",
            "end": "23:59"
        }
    },
    "schedule": {
        "interval": "1m"
    }
}
```