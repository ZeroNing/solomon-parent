# Docker 部署拓扑覆盖矩阵

本表说明每个组件的独立部署单元和可用的高可用形态。高可用严格按组件原生能力配置；不支持原生集群的组件不会用多个相互独立的容器冒充高可用。

| 组件 | 单机/基础部署 | 主从、哨兵或高可用部署 | 说明 |
| --- | --- | --- | --- |
| MySQL | `mysql` | `mysql主从` | GTID 主从复制 |
| MariaDB | `mariaDB` | `mariaDB主从` | GTID 主从复制 |
| PostgreSQL | `postgresSql` | `postgresql主从` | 流复制热备 |
| MongoDB | `mongodb` | `mongodb副本集` | 三节点副本集并自动初始化 |
| Redis | `redis` | `redis主从集群`、`redis哨兵`、`redis哨兵主从集群`、`redis集群` | 主从、Sentinel、3 主 3 从 Cluster 均为独立文件 |
| CouchDB | `couchdb` | `couchdb集群` | 三节点集群并自动建制 |
| Cassandra | `cassandra` | `cassandra集群` | 原生无主三节点集群 |
| ClickHouse | `clickhouse` | `clickhouse集群` | 2 分片 × 2 副本 + 3 Keeper |
| Elasticsearch | `elasticsearch单机版` | `elasticsearch集群` | 三节点集群 |
| OpenSearch | `opensearch` | `opensearch集群` | 三节点集群 |
| Qdrant | `qdrant` | `qdrant集群` | 三节点 P2P 集群 |
| Weaviate | `weaviate` | `weaviate集群` | 三节点 Raft 集群 |
| MinIO | `minio` | `minio分布式` | 四节点纠删码部署 |
| etcd | `etcd` | `etcd集群` | 三节点 Raft |
| Consul | `consul` | `consul集群` | 三服务端 Raft |
| ZooKeeper | `zookeeper` | `zookeeper集群主从同步` | 三节点选主集群 |
| Kafka | `kafka` | `kafka集群` | 三节点 KRaft |
| RabbitMQ | `rabbitmq`、三个延迟插件方案 | `rabbitmq集群` | 两节点集群；业务队列应声明为 quorum queue |
| NATS | `nats` | `nats集群` | 三节点 JetStream 集群 |
| EMQX | `emqx` | `emqx集群` | 两节点集群 |
| Nacos | `nacos` | `nacos集群` | 三节点集群，依赖外部 MySQL |
| RocketMQ | `rocketmq` | `rocketmq高可用` | 2 NameServer + 3 Broker DLedger 副本 + Dashboard |
| Pulsar | `pulsar` | `pulsar集群` | 3 ZooKeeper + 3 BookKeeper + 2 Broker + Proxy |
| SQL Server | `sqlserver` | 不适用于本地开发镜像 | Always On 需要域、证书和相应授权，推荐在真实服务器环境部署 |
| CockroachDB | `cockroachdb` | `cockroachdb集群` | 三节点无主 Raft；本地文件为无 TLS 联调模式 |
| InfluxDB OSS | `influxdb` | 无原生集群 | OSS 2.x 不提供集群；高可用需 InfluxDB Clustered/Cloud |
| Neo4j Community | `neo4j` | 社区版无集群 | 集群属于 Enterprise 能力 |
| H2 | `h2` | 不适用 | 嵌入式/开发数据库，不作为生产高可用数据库 |
| Chroma | `chromadb` | 无稳定的单机 Compose 集群形态 | 生产分布式部署推荐官方 Kubernetes 路径 |
| Dragonfly | `dragonflyDB缓存` | 当前为单节点 | 生产高可用推荐其官方 Kubernetes Operator |
| Memcached | `memcached` | 客户端分片 | Memcached 无服务端复制或哨兵，由客户端一致性哈希扩展 |
| Mosquitto | `mosquitto` | 无原生集群 | Bridge 不是一致性高可用；需要集群时使用 `emqx集群` |
| Keycloak | `keycloak` | 外部数据库 + 编排平台扩容 | 当前文件为开发模式；生产需 PostgreSQL、反向代理和多实例发现 |
| Seata | `seata` | 依赖注册中心和外部数据库 | 本地文件为单节点；生产通过 Nacos/Consul 注册后多实例部署 |
| Sentinel Dashboard | `sentinel` | 无状态控制台 | 控制台不是数据面，按需由编排平台多副本部署 |
| XXL-JOB | `xxl-job` | 外部数据库 + 多实例 | 调度中心共享数据库后可水平扩展 |
| PowerJob | 两种网络模式 | 外部数据库 + 多实例 | Server 共享数据库并通过自身集群机制工作 |
| Prometheus | `prometheus` | 单机采集 | 长期高可用推荐双 Prometheus + Thanos/Mimir |
| Grafana | `grafana` | 外部数据库 + 多实例 | SQLite 模式不能多实例 |
| Loki | `loki` | 单机模式 | 分布式模式推荐官方 Helm，依赖对象存储 |
| Tempo | `tempo` | 单机模式 | 分布式模式推荐官方 Helm，依赖对象存储 |
| Jaeger | `jaeger` | all-in-one 开发模式 | 生产推荐 Jaeger v2 多实例并使用外部存储 |
| Zipkin | `zipkin` | 单机模式 | 多实例需共享 Elasticsearch/Cassandra 存储 |
| SkyWalking | `skywalking` | OAP + UI | OAP 可在外部存储和服务发现支持下横向扩展 |
| Gitea | `gitea` | 外部数据库/对象存储 + 多实例 | 当前部署连接外部数据库 |
| Jenkins | `jenkins` | 控制器单实例 + Agents | Jenkins 控制器不支持主动-主动 |
| Nexus | `nexus` | 单实例社区版 | 高可用属于商业版本/外部存储架构 |
| SonarQube | `sonarqube` | Data Center Edition | 社区版不提供应用集群 |
| Vault | `vault` | 当前为开发模式 | 生产推荐 integrated storage Raft + TLS，不能使用 dev 模式 |
| Portainer | `portainer` | 单实例社区版 | 商业版提供更完整的高可用能力 |
| Nginx | `nginx` | 无状态多实例 | 复制同一配置后由外部负载均衡器分流 |
| ClamAV | `clamAV` | 无状态多实例 | 由调用端或负载均衡器分流 |
| Adminer | `adminer` | 无状态多实例 | 无持久状态，可由编排平台扩容 |
| pgAdmin | `pgadmin` | 单机管理工具 | 管理工具不属于业务数据面 |
| RedisInsight | `redisinsight` | 单机管理工具 | 管理工具不属于业务数据面 |
| Kafka UI | `kafka-ui` | 单机管理工具 | 管理工具不属于业务数据面 |
| Kibana/OpenSearch Dashboards | 各自独立目录 | 无状态多实例 | 连接高可用搜索集群后可水平扩容 |
| ActiveMQ Classic | `activemq` | 当前为单节点 | 原镜像已老旧；新项目优先使用 Artemis/RabbitMQ/Kafka |
| 禅道/InStock | 各自独立目录 | 由应用自身和外部数据库决定 | 本目录仅提供本地应用部署 |

## 判定原则

- “主从”只用于产品确有复制角色的组件。
- “哨兵”是 Redis Sentinel 的专有形态，其他组件使用 Raft、KRaft、副本集、无主集群或纠删码等原生高可用机制。
- 无状态组件的高可用由多个相同实例和外部负载均衡器实现，不需要另造一份内容相同的 Compose。
- 商业版、云版或 Kubernetes 才提供的能力会明确标注，不把无法在本地 Compose 中可靠复现的能力写成“可用”部署。
