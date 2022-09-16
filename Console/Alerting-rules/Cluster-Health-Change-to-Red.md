# Cluster Health Change to Red

## Rule Variables
| Field        | Descriction   |  eg  |
| --------   | -----  | ----  |
| ${RESOURCE_ID}        |   The Elasticsearch (configured in console.yml) internal id（not cluster_uuid）  |   c6abfdovi074mgr185m2   |
| ${RESOURCE_NAME}        |    The Elasticsearch (configured in console.yml)  cluster_name    |  es-v710  |
| ${INFINI_CONSOLE_ENDPOINT}        |    The host address of the current Console UI    |  http://192.168.3.201:9000  |
| ${SLACK_WEBHOOK_ENDPOINT}        |    The webhook address of the notification channel    |  https://hooks.slack.com/services/xxx/xxx/xxx  |
| ${DINGTALK_WEBHOOK_ENDPOINT}        |    The  DingTalk webhook address of the notification channel    |  https://oapi.dingtalk.com/robot/send?access_token=xxx  |

## Rule Template
Note: The following rule template content (available only after replacing the placeholder variable) can be directly copied to Console Command for execution to quickly create an rule.

```sh
#The `id` value is consistent with the `_id` value
POST .infini_alert-rule/_doc/cal8n7p7h710dpnoaps0
{
    "id": "cal8n7p7h710dpnoaps0",
    "created": "2022-06-16T01:47:11.326727124Z",
    "updated": "2022-07-13T04:00:06.181994982Z",
    "name": "Cluster Health Change to Red",
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
            "bool": {
                "must": [
                    {
                        "match": {
                            "payload.elasticsearch.cluster_health.status": "red"
                        }
                    },
                    {
                        "term": {
                            "metadata.name": {
                                "value": "cluster_health"
                            }
                        }
                    }
                ]
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
            }
        ],
        "formula": "a",
        "items": [
            {
                "name": "a",
                "field": "payload.elasticsearch.cluster_health.status",
                "statistic": "count"
            }
        ],
        "format_type": "num",
        "expression": "count(payload.elasticsearch.cluster_health.status)",
        "title": "Health of Cluster[s] ({{.first_group_value}} ..., {{len .results}} clusters in total) Changed to Red",
        "message": "Priority:{{.priority}}\nTimestamp:{{.timestamp | datetime}}\nRuleID:{{.rule_id}}\nEventID:{{.event_id}}\n{{range .results}}\nClusterID:{{index .group_values 0}} is red now;\n{{end}}"
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
                "created": "2022-06-16T01:47:11.326727124Z",
                "updated": "2022-06-16T01:47:11.326727124Z",
                "name": "Slack webhook",
                "type": "webhook",
                "webhook": {
                    "header_params": {
                        "Content-Type": "application/json"
                    },
                    "method": "POST",
                    "url": "${SLACK_WEBHOOK_ENDPOINT}",
                    "body": "{\n    \"blocks\": [\n        {\n            \"type\": \"section\",\n            \"text\": {\n                \"type\": \"mrkdwn\",\n                \"text\": \"Incident <${INFINI_CONSOLE_ENDPOINT}/#/alerting/alert/{{.event_id}}|#{{.event_id}}> is ongoing\\n{{.title}}\"\n            }\n        }\n    ],\n    \"attachments\": [\n        {{range .results}}\n        {\n            \"color\": {{if eq .priority \"critical\"}} \"#C91010\" {{else if eq .priority \"high\"}} \"#EB4C21\" {{else if eq .priority \"medium\"}} \"#FFB449\" {{else if eq .priority \"low\"}} \"#87d068\" {{else}} \"#2db7f5\" {{end}},\n            \"blocks\": [\n                {\n                    \"type\": \"section\",\n                    \"fields\": [\n                        {\n                            \"type\": \"mrkdwn\",\n                            \"text\": \"*ClusterID:* {{index .group_values 0}}\"\n                        },\n                        {\n                            \"type\": \"mrkdwn\",\n                            \"text\": \"*Priority:* {{.priority}}\"\n                        },\n                        {\n                            \"type\": \"mrkdwn\",\n                            \"text\": \"*Link:* <${INFINI_CONSOLE_ENDPOINT}/#/cluster/monitor/elasticsearch/{{ index .group_values 0}}|View Cluster Monitoring>\"\n                        }\n                    ]\n                }\n            ]\n        },\n        {{end}}\n    ]\n}"
                }
            },
            {
                "created": "2022-06-16T01:47:11.326727124Z",
                "updated": "2022-06-16T01:47:11.326727124Z",
                "name": "DingTalk",
                "type": "webhook",
                "webhook": {
                    "header_params": {
                        "Content-type": "application/json"
                    },
                    "method": "POST",
                    "url": "${DINGTALK_WEBHOOK_ENDPOINT}",
                    "body": "{\"msgtype\": \"text\",\"text\": {\"content\":\"Alerting: \\n{{.title}}\\n\\n{{.message}}\\nLink:${INFINI_CONSOLE_ENDPOINT}/#/alerting/alert/{{.event_id}}\"}}"
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