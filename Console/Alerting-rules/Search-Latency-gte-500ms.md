# Search latency is great than 500ms

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
POST .infini_alert-rule/_doc/cbp2e4ianisjmu4giqs7
{
    "id": "cbp2e4ianisjmu4giqs7",
    "created": "2022-06-16T04:11:10.242061032Z",
    "updated": "2022-08-09T09:39:29.604751601Z",
    "name": "Search latency is great than 500ms",
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
                                "value": "index_stats"
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
                ],
                "must_not": [
                    {
                        "term": {
                            "metadata.labels.index_name": {
                                "value": "_all"
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
                "field": "metadata.labels.index_name",
                "limit": 500
            }
        ],
        "formula": "a/b",
        "items": [
            {
                "name": "a",
                "field": "payload.elasticsearch.index_stats.total.search.query_time_in_millis",
                "statistic": "rate"
            },
            {
                "name": "b",
                "field": "payload.elasticsearch.index_stats.primaries.search.query_total",
                "statistic": "rate"
            }
        ],
        "format_type": "num",
        "expression": "rate(payload.elasticsearch.index_stats.total.search.query_time_in_millis)/rate(payload.elasticsearch.index_stats.primaries.search.query_total)",
        "title": "Search latency is great than 500ms",
        "message": "Priority:{{.priority}}\nTimestamp:{{.timestamp | datetime_in_zone \"Asia/Shanghai\"}}\nRuleID:{{.rule_id}}\nEventID:{{.event_id}}\n{{range .results}}\nClusterID:{{index .group_values 0}}; \nIndex name:{{index .group_values 1}}; \nCurrent value:{{.result_value | to_fixed 2}}ms;\n{{end}}"
    },
    "conditions": {
        "operator": "any",
        "items": [
            {
                "minimum_period_match": 1,
                "operator": "gte",
                "values": [
                    "500"
                ],
                "priority": "medium"
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
                    "body": "{\n    \"blocks\": [\n        {\n            \"type\": \"section\",\n            \"text\": {\n                \"type\": \"mrkdwn\",\n                \"text\": \"Incident <${INFINI_CONSOLE_ENDPOINT}/#/alerting/alert/{{.event_id}}|#{{.event_id}}> is ongoing\\n{{.title}}\"\n            }\n        }\n    ],\n    \"attachments\": [\n        {{range .results}}\n        {\n            \"color\": {{if eq .priority \"critical\"}} \"#C91010\" {{else if eq .priority \"error\"}} \"#EB4C21\" {{else}} \"#FFB449\" {{end}},\n            \"blocks\": [\n                {\n                    \"type\": \"section\",\n                    \"fields\": [\n                        {\n                            \"type\": \"mrkdwn\",\n                            \"text\": \"*ClusterID:* {{index .group_values 0}}\"\n                        },\n  {\n                            \"type\": \"mrkdwn\",\n                            \"text\": \"*Index:* {{index .group_values 1}}\"\n                        },\n  {\n                            \"type\": \"mrkdwn\",\n                            \"text\": \"*Latency:* {{.result_value | to_fixed 2}}ms\"\n                        },\n                        {\n                            \"type\": \"mrkdwn\",\n                            \"text\": \"*Priority:* {{.priority}}\"\n                        },\n                        {\n                            \"type\": \"mrkdwn\",\n                            \"text\": \"*Link:* <${INFINI_CONSOLE_ENDPOINT}/#/cluster/overview/{{ index .group_values 0}}/indices/{{ index .group_values 1}}|View Index Monitoring>\"\n                        }\n                    ]\n                }\n            ]\n        },\n        {{end}}\n    ]\n}"
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