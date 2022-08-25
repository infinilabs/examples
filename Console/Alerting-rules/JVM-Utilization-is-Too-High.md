# JVM utilization is Too High

## Rule Variables
| Field        | Descriction   |  eg  |
| --------   | -----  | ----  |
| ${RESOURCE_ID}        |   The Elasticsearch cluster_id   |   c6abfdovi074mgr185m2   |
| ${RESOURCE_NAME}        |    The Elasticsearch cluster_name    |  es-v710  |
| ${INFINI_CONSOLE_ENDPOINT}        |    The host address of the current Console UI    |  http://192.168.3.201:9000  |
| ${SLACK_WEBHOOK_ENDPOINT}        |    The webhook address of the notification channel    |  https://hooks.slack.com/services/xxx/xxx/xxx  |

## Rule Template
Note: The following rule template content (available only after replacing the placeholder variable) can be directly copied to Console Command for execution to quickly create an rule.

```sh
#The `id` value is consistent with the `_id` value
POST .infini_alert-rule/_doc/calaqnh7h710dpnp2bm8
{
    "id": "calaqnh7h710dpnp2bm8",
    "created": "2022-06-16T04:11:10.242061032Z",
    "updated": "2022-07-21T23:12:07.142532243Z",
    "name": "JVM utilization is Too High",
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
                        "term": {
                            "metadata.name": {
                                "value": "node_stats"
                            }
                        }
                    },
                    {
                        "term": {
                            "metadata.category": {
                                "value": "elasticsearch"
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
            },
            {
                "field": "metadata.labels.node_id",
                "limit": 300
            }
        ],
        "formula": "a",
        "items": [
            {
                "name": "a",
                "field": "payload.elasticsearch.node_stats.jvm.mem.heap_used_percent",
                "statistic": "p90"
            }
        ],
        "format_type": "ratio",
        "expression": "p90(payload.elasticsearch.node_stats.jvm.mem.heap_used_percent)",
        "title": "JVM Usage of Node[s] ({{.first_group_value}} ..., {{len .results}} nodes in total) >= {{.first_threshold}}%",
        "message": "Timestamp:{{.timestamp | datetime}}\nRuleID:{{.rule_id}}\nEventID:{{.event_id}}\n{{range .results}}\nClusterID:{{index .group_values 0}}; Node name:{{index .group_values 1}};  memory used percent：{{.result_value | to_fixed 2}}%;{{end}}"
    },
    "conditions": {
        "operator": "any",
        "items": [
            {
                "minimum_period_match": 1,
                "operator": "gte",
                "values": [
                    "80"
                ],
                "priority": "low"
            },
            {
                "minimum_period_match": 1,
                "operator": "gte",
                "values": [
                    "90"
                ],
                "priority": "medium"
            },
            {
                "minimum_period_match": 1,
                "operator": "gte",
                "values": [
                    "95"
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
                    "body": "{\n    \"blocks\": [\n        {\n            \"type\": \"section\",\n            \"text\": {\n                \"type\": \"mrkdwn\",\n                \"text\": \"Incident <${INFINI_CONSOLE_ENDPOINT}/#/alerting/alert/{{.event_id}}|#{{.event_id}}> is ongoing\\n{{.title}}\"\n            }\n        }\n    ],\n    \"attachments\": [\n        {{range .results}}\n        {\n            \"color\": {{if eq .priority \"critical\"}} \"#C91010\" {{else if eq .priority \"error\"}} \"#EB4C21\" {{else}} \"#FFB449\" {{end}},\n            \"blocks\": [\n                {\n                    \"type\": \"section\",\n                    \"fields\": [\n  {\n                            \"type\": \"mrkdwn\",\n                            \"text\": \"*Priority:* {{.priority}}\"\n                        },\n                        {\n                            \"type\": \"mrkdwn\",\n                            \"text\": \"*ClusterID:* {{index .group_values 0}}\"\n                        },\n                        {\n                            \"type\": \"mrkdwn\",\n                            \"text\": \"*NodeID:* {{index .group_values 1}}\"\n                        }\n                      ,\n                        {\n                            \"type\": \"mrkdwn\",\n                            \"text\": \"*Usage:* {{.result_value | to_fixed 2}}%\"\n                        },\n                        {\n                            \"type\": \"mrkdwn\",\n                            \"text\": \"*Link:* <${INFINI_CONSOLE_ENDPOINT}/#/cluster/overview/{{ index .group_values 0}}/nodes/{{ index .group_values 1}}|View Node Monitoring>\"\n                        }\n                    ]\n                }\n            ]\n        },\n        {{end}}\n    ]\n}"
                }
            }
        ],
        "throttle_period": "3h",
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