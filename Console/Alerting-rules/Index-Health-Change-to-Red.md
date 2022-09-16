# Index Health Change to Red

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
POST .infini_alert-rule/_doc/calavvp7h710dpnp32r3
{
    "id": "calavvp7h710dpnp32r3",
    "created": "2022-06-16T04:22:23.001354546Z",
    "updated": "2022-07-21T23:10:36.70696738Z",
    "name": "Index Health Change to Red",
    "enabled": false,
    "resource": {
        "resource_id": "${RESOURCE_ID}",
        "resource_name": "${RESOURCE_NAME}",
        "type": "elasticsearch",
        "objects": [
            ".infini_index"
        ],
        "filter": {},
        "raw_filter": {
            "match_phrase": {
                "metadata.labels.health_status": "red"
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
                "field": "metadata.index_name",
                "limit": 5
            }
        ],
        "formula": "a",
        "items": [
            {
                "name": "a",
                "field": "metadata.index_name",
                "statistic": "count"
            }
        ],
        "format_type": "num",
        "expression": "count(metadata.index_name)",
        "title": "Health of Indices ({{.first_group_value}} ..., {{len .results}} indices in total) Changed to Red",
        "message": "Timestamp:{{.timestamp | datetime}}\nRuleID:{{.rule_id}}\nEventID:{{.event_id}}\n{{range .results}}\nClusterID:{{index .group_values 0}}; Index name:{{index .group_values 1}}; {{end}}"
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
                    "body": "{\n    \"blocks\": [\n        {\n            \"type\": \"section\",\n            \"text\": {\n                \"type\": \"mrkdwn\",\n                \"text\": \"Incident <${INFINI_CONSOLE_ENDPOINT}/#/alerting/alert/{{.event_id}}|#{{.event_id}}> is ongoing\\n{{.title}}\"\n            }\n        }\n    ],\n    \"attachments\": [\n        {{range .results}}\n        {\n            \"color\": {{if eq .priority \"critical\"}} \"#C91010\" {{else if eq .priority \"error\"}} \"#EB4C21\" {{else}} \"#FFB449\" {{end}},\n            \"blocks\": [\n                {\n                    \"type\": \"section\",\n                    \"fields\": [\n                        {\n                            \"type\": \"mrkdwn\",\n                            \"text\": \"*ClusterID:* {{index .group_values 0}}\"\n                        },\n   {\n                            \"type\": \"mrkdwn\",\n                            \"text\": \"*Index:* {{index .group_values 1}}\"\n                        },\n                        {\n                            \"type\": \"mrkdwn\",\n                            \"text\": \"*Priority:* {{.priority}}\"\n                        },\n                        {\n                            \"type\": \"mrkdwn\",\n                            \"text\": \"*Link:* <${INFINI_CONSOLE_ENDPOINT}/#/cluster/monitor/elasticsearch/{{ index .group_values 0}}?_g=%7B%22tab%22%3A%22indices%22%7D|View Index Monitoring>\"\n                        }\n                    ]\n                }\n            ]\n        },\n        {{end}}\n    ]\n}"
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