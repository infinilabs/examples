# Too Many Deleted Documents

## Rule Variables
| Field        | Descriction   |  eg  |
| --------   | -----  | ----  |
| ${RESOURCE_ID}        |   The Elasticsearch (configured in console.yml) internal id（not cluster_uuid）   |   c6abfdovi074mgr185m2   |
| ${RESOURCE_NAME}        |    The Elasticsearch (configured in console.yml) cluster_name    |  es-v710  |
| ${INFINI_CONSOLE_ENDPOINT}        |    The host address of the current Console UI    |  http://192.168.3.201:9000  |
| ${SLACK_WEBHOOK_ENDPOINT}        |    The webhook address of the notification channel    |  https://hooks.slack.com/services/xxx/xxx/xxx  |

## Rule Template
Note: The following rule template content (available only after replacing the placeholder variable) can be directly copied to Console Command for execution to quickly create an rule.

```sh
#The `id` value is consistent with the `_id` value
POST .infini_alert-rule/_doc/cb34sfl6psfiqtovhpt4
{
    "id": "cb34sfl6psfiqtovhpt4",
    "created": "2022-07-07T03:08:46.297166036Z",
    "updated": "2022-08-09T08:40:05.323148338Z",
    "name": "Too Many Deleted Documents",
    "enabled": false,
    "resource": {
        "resource_id": "${RESOURCE_ID}",
        "resource_name": "${RESOURCE_NAME}",
        "type": "elasticsearch",
        "objects": [
            ".infini_metrics*"
        ],
        "filter": {},
        "raw_filter": {
            "range": {
                "payload.elasticsearch.cluster_stats.indices.store.size_in_bytes": {
                    "gte": 32212254720
                }
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
                "field": "metadata.labels.cluster_id",
                "limit": 5
            },
            {
                "field": "metadata.labels.index_name",
                "limit": 300
            }
        ],
        "formula": "(a/(a+b))*100",
        "items": [
            {
                "name": "a",
                "field": "payload.elasticsearch.index_stats.primaries.docs.deleted",
                "statistic": "max"
            },
            {
                "name": "b",
                "field": "payload.elasticsearch.index_stats.primaries.docs.count",
                "statistic": "max"
            }
        ],
        "format_type": "ratio",
        "expression": "(max(payload.elasticsearch.index_stats.primaries.docs.deleted)/(max(payload.elasticsearch.index_stats.primaries.docs.deleted)+max(payload.elasticsearch.index_stats.primaries.docs.count)))*100",
        "title": "Too Many Deleted Documents (>30%)",
        "message": "Priority:{{.priority}}\nTimestamp:{{.timestamp | datetime_in_zone \"Asia/Shanghai\"}}\nRuleID:{{.rule_id}}\nEventID:{{.event_id}}\n{{range .results}}\nClusterID:{{index .group_values 0}}; \nIndex:{{index .group_values 0}}; \nRatio of Deleted Documents:{{.result_value}};\n{{end}}"
    },
    "conditions": {
        "operator": "any",
        "items": [
            {
                "minimum_period_match": 1,
                "operator": "gte",
                "values": [
                    "30"
                ],
                "priority": "medium"
            },
            {
                "minimum_period_match": 1,
                "operator": "gte",
                "values": [
                    "40"
                ],
                "priority": "high"
            }
        ]
    },
    "channels": {
        "enabled": true,
        "normal": [
            {
                "created": "2022-06-16T04:11:10.242061032Z",
                "updated": "2022-06-16T04:11:10.242061032Z",
                "name": "Slack",
                "type": "webhook",
                "webhook": {
                    "header_params": {
                        "Content-Type": "application/json"
                    },
                    "method": "POST",
                    "url": "${SLACK_WEBHOOK_ENDPOINT}",
                    "body": "{\n    \"blocks\": [\n        {\n            \"type\": \"section\",\n            \"text\": {\n                \"type\": \"mrkdwn\",\n                \"text\": \"Incident <${INFINI_CONSOLE_ENDPOINT}/#/alerting/alert/{{.event_id}}|#{{.event_id}}> is ongoing\\n{{.title}}\"\n            }\n        }\n    ],\n    \"attachments\": [\n        {{range .results}}\n        {\n            \"color\": {{if eq .priority \"critical\"}} \"#C91010\" {{else if eq .priority \"error\"}} \"#EB4C21\" {{else}} \"#FFB449\" {{end}},\n            \"blocks\": [\n                {\n                    \"type\": \"section\",\n                    \"fields\": [\n  {\n                            \"type\": \"mrkdwn\",\n                             \"text\": \"*Priority:* {{.priority}}\"\n                        },\n                        {\n                            \"type\": \"mrkdwn\",\n                            \"text\": \"*ClusterID:* {{index .group_values 0}}\"\n                        },\n   {\n                            \"type\": \"mrkdwn\",\n                            \"text\": \"*Index:* {{index .group_values 1}}\"\n                        },\n     {\n                            \"type\": \"mrkdwn\",\n                            \"text\": \"*Deleted:* {{.result_value | to_fixed 2}}%\"\n                        },\n                      \n                        {\n                            \"type\": \"mrkdwn\",\n                            \"text\": \"*Link:* <${INFINI_CONSOLE_ENDPOINT}/#/cluster/overview/{{ index .group_values 0}}/indices/{{ index .group_values 1}}?_g={%22cluster_name%22:%22{{ index .group_values 0}}%22}|View Index Monitoring>\"\n                        }\n                    ]\n                }\n            ]\n        },\n        {{end}}\n    ]\n}"
                }
            }
        ],
        "throttle_period": "24h",
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