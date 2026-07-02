# Docker 本地部署目录

本目录提供项目开发、联调和测试所需的基础设施。每个子目录代表一种可以独立启动和停止的部署单元；单机、主从、哨兵和集群部署互不混用。

全部组件的单机与高可用覆盖情况、原生能力限制和推荐替代方案见 [TOPOLOGY.md](./TOPOLOGY.md)。

## 配置原则

- 每个子目录都是独立部署单元，单机、主从、副本集、哨兵和集群互不依赖。
- 环境变量、账号、密码、端口和外部地址全部直接写在各自的 `docker-compose.yml` 内，不读取 `.env`。
- 每个文件开头和关键配置旁均有中文说明。仓库中的密码仅适合本地开发，生产部署必须修改。
- 要求 Docker Engine 24+、Docker Compose 2.20+，统一使用 `docker compose` 命令。
- Linux 宿主机建议预先执行 `sudo sysctl -w vm.max_map_count=262144`，供 Elasticsearch 和 SonarQube 使用。

## 统一管理命令

Windows PowerShell：

```powershell
.\manage.ps1 list
.\manage.ps1 validate
.\manage.ps1 config mysql
.\manage.ps1 up mysql
.\manage.ps1 logs mysql
.\manage.ps1 down mysql
```

Linux/macOS：

```bash
./manage.sh list
./manage.sh validate
./manage.sh config mysql
./manage.sh up mysql
./manage.sh logs mysql
./manage.sh down mysql
```

`validate` 用于一次校验全部部署单元；`config` 用于查看单个部署单元的最终配置。部署前应先执行这两个命令。

## 部署单元

### 数据库与存储

| 目录 | 说明 | 默认宿主机端口 |
| --- | --- | --- |
| `mysql` | MySQL 单机 | 3306 |
| `mysql主从` | MySQL 1 主 1 从（GTID） | 3306、3307 |
| `mariaDB` | MariaDB 单机 | 3307 |
| `postgresSql` | PostgreSQL 单机 | 5432 |
| `postgresql主从` | PostgreSQL 1 主 1 热备 | 5432、5433 |
| `mongodb` | MongoDB 单机 | 27017 |
| `mongodb副本集` | MongoDB 三节点副本集 | 27017、27018、27019 |
| `sqlserver` | SQL Server 2022 开发版 | 1433 |
| `cockroachdb` | CockroachDB 单节点 | 26257、8082 |
| `cockroachdb集群` | CockroachDB 三节点无主集群 | 26257-26259、8082-8084 |
| `clickhouse` | ClickHouse 分析数据库 | 8123、9000 |
| `clickhouse集群` | ClickHouse 2 分片 2 副本 + 3 Keeper | 8123-8126、9000-9003 |
| `influxdb` | InfluxDB 时序数据库 | 8086 |
| `neo4j` | Neo4j 图数据库 | 7474、7687 |
| `qdrant` | Qdrant 向量数据库 | 6333、6334 |
| `chromadb` | Chroma 向量数据库 | 8000 |
| `weaviate` | Weaviate 向量数据库 | 8088、50051 |
| `couchdb` | CouchDB 文档数据库 | 5984 |
| `cassandra` | Cassandra 宽列数据库 | 9042 |
| `cassandra集群` | Cassandra 三节点无主集群 | 9042-9044 |
| `couchdb集群` | CouchDB 三节点集群 | 5984-5986 |
| `mariaDB主从` | MariaDB GTID 主从 | 3310、3311 |
| `elasticsearch集群` | Elasticsearch 三节点集群 | 9200-9202 |
| `opensearch集群` | OpenSearch 三节点集群 | 9201、9211、9221 |
| `qdrant集群` | Qdrant 三节点集群 | 6333-6354 |
| `weaviate集群` | Weaviate 三节点集群 | 8088-8090、50051-50053 |
| `h2` | H2 数据库 | 81、1521 |
| `minio` | MinIO 对象存储 | 9000、9001 |
| `minio分布式` | MinIO 四节点纠删码 | 9000-9031 |
| `rustfs` | RustFS 对象存储 | 9100、9101 |

### 缓存

| 目录 | 独立拓扑 | 说明 |
| --- | --- | --- |
| `redis` | 单机 | 单节点 Redis |
| `redis主从集群` | 主从 | 1 主 2 从 |
| `redis哨兵` | 哨兵 | 连接外部 Redis 主节点的 3 哨兵 |
| `redis哨兵主从集群` | 主从 + 哨兵 | 1 主 2 从 + 3 哨兵 |
| `redis集群` | Cluster | 3 主 3 从 |
| `dragonflyDB缓存` | 单机 | Redis 协议兼容缓存 |
| `memcached` | 单机 | Memcached 内存缓存 |

### 消息与协调服务

| 目录 | 独立拓扑 | 说明 |
| --- | --- | --- |
| `rabbitmq` | 单机 | RabbitMQ 管理版 |
| `rabbitmq集群` | 集群 | 两节点 RabbitMQ 集群 |
| `rabbitmq延迟队列` | 单机 | 通过 Dockerfile 安装延迟消息插件 |
| `rabbitmq延迟队列（插件已下载）` | 单机 | 使用仓库内插件文件 |
| `rabbitmq延迟队列（脚本下载）` | 单机 | 启动时下载插件 |
| `kafka` | 单机 | KRaft 模式 Kafka |
| `kafka集群` | 集群 | 三节点 KRaft Kafka |
| `rocketmq` | 集群组件 | NameServer、Broker、Dashboard |
| `rocketmq高可用` | 高可用 | 2 NameServer、3 Broker DLedger、Dashboard |
| `pulsar` | 单机 | Pulsar Standalone |
| `pulsar集群` | 集群 | 3 ZooKeeper、3 BookKeeper、2 Broker、1 Proxy |
| `nats` | 单机 | NATS + JetStream |
| `nats集群` | 集群 | 三节点 NATS + JetStream |
| `zookeeper` | 单机 | ZooKeeper 单节点 |
| `zookeeper集群主从同步` | 集群 | 三节点 ZooKeeper |
| `etcd` | 单机 | etcd 分布式键值存储 |
| `etcd集群` | 集群 | 三节点 etcd Raft |
| `consul` | 单机 | Consul 服务发现与 KV |
| `consul集群` | 集群 | 三节点 Consul Raft |

### 注册、任务与物联网

| 目录 | 独立拓扑 | 说明 |
| --- | --- | --- |
| `nacos` | 单机 | Nacos 单节点 |
| `nacos集群` | 集群 | 三节点 Nacos，依赖外部 MySQL |
| `emqx` | 单机 | EMQX 单节点 |
| `emqx集群` | 集群 | 两节点 EMQX |
| `mosquitto` | 单机 | Eclipse Mosquitto MQTT Broker |
| `xxl-job` | 单机 | XXL-JOB 管理端 |
| `powerjob(局域网模式)` | 单机 | 局域网地址广播模式 |
| `powerjob(非局域网模式)` | 单机 | 公网地址广播模式 |
| `sentinel` | 单机 | Sentinel Dashboard |
| `seata` | 单机 | Seata 分布式事务 |

### 搜索与可观测性

| 目录 | 说明 | 默认宿主机端口 |
| --- | --- | --- |
| `elasticsearch单机版` | Elasticsearch 单节点 | 9200、9300 |
| `kibana` | Elasticsearch 可视化界面 | 5601 |
| `opensearch` | OpenSearch 单节点 | 9201、9600 |
| `opensearch-dashboards` | OpenSearch 可视化界面 | 5602 |
| `prometheus` | 指标采集与存储 | 9090 |
| `grafana` | 指标与日志可视化 | 3000 |
| `loki` | 日志聚合存储 | 3100 |
| `tempo` | 分布式链路存储 | 3200、4317、4318 |
| `jaeger` | 分布式链路追踪 | 16686、4317、4318 |
| `zipkin` | 分布式链路追踪 | 9411 |
| `skywalking` | APM、OAP 与 UI | 11800、12800、8089 |

### 工具与业务应用

| 目录 | 说明 |
| --- | --- |
| `activemq` | ActiveMQ |
| `clamAV` | ClamAV 病毒扫描 |
| `gitea` | Gitea 代码托管 |
| `jenkins` | Jenkins |
| `nexus` | Nexus Repository |
| `nginx` | Nginx Web 服务 |
| `portainer` | Portainer |
| `sonarqube` | SonarQube |
| `keycloak` | OAuth 2.0 / OpenID Connect 身份认证 |
| `vault` | Vault 本地开发密钥管理 |
| `zentao(禅道)` | 禅道 |
| `InStock股票系统` | InStock 股票系统 |
| `adminer` | 通用数据库 Web 管理工具 |
| `pgadmin` | PostgreSQL Web 管理工具 |
| `redisinsight` | Redis 官方管理工具 |
| `kafka-ui` | Kafka Web 管理工具 |

## 注意事项

- 多个部署单元可能使用相同端口，不建议一次性全部启动；需要并行运行时请直接修改对应 `docker-compose.yml` 中的宿主机端口。
- `data`、`logs`、`home` 等目录用于持久化。删除这些目录会造成数据丢失。
- `latest` 镜像保留了原有行为。正式环境应在验证后改为明确版本，避免无感升级。
- Compose 中的 `container_name` 保留是为了兼容现有使用方式，因此同一宿主机不能重复启动同名部署。
