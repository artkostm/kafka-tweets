# kafka-tweets

Run Zookeeper and Kafka using the docker image:
```bash
docker run --rm -p 2181:2181 -p 9092:9092 -p9999:9999 \
    --env ADVERTISED_HOST=192.168.99.100 \
    --env ADVERTISED_PORT=9092 \
    --env TOPICS=TestTopic \
    spotify/kafka
```
Or use HDP's Kafka (broker address is 0.0.0.0:6667 by default).

Then create Kafka topic:
```bash
kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 10 --topic Tweets
```

Go to http://apps.twitter.com/, login with your twitter account and register your application to get a consumer key and a consumer secret.

Add your consumer and access token as either environment variables or as part of your configuration. The twitter client will look for the following environment variables:
```bash
export TWITTER_CONSUMER_TOKEN_KEY='consumer-key'
export TWITTER_CONSUMER_TOKEN_SECRET='consumer-secret'
export TWITTER_ACCESS_TOKEN_KEY='access-key'
export TWITTER_ACCESS_TOKEN_SECRET='access-secret'
```

You can also add them to your configuration file, usually called application.conf:
```bson
twitter {
  consumer {
    key = "consumer-key"
    secret = "consumer-secret"
  }
  access {
    key = "access-key"
    secret = "access-secret"
  }
}
```

Build the jar: ```sbt assembly```

Run Tweets Producer (Kafka's key is a `username:hastag` pair and value is a Tweet in json format):
```bash
java -jar \
    -Dakka.kafka.producer.kafka-clients.bootstrap.servers=192.168.99.100:9092 \
    -Dtweets.topic=Tweets \
    -Dakka.kafka.producer.parallelism=10 \
    -Dgenerator.number-of-events=10000 \
    -Dtwitter.consumer.key=${TWITTER_CONSUMER_KEY} \
    -Dtwitter.consumer.secret=${TWITTER_CONSUMER_SECRET} \
    -Dtwitter.access.key=${TWITTER_ACCESS_KEY} \
    -Dtwitter.access.secret=${TWITTER_ACCESS_KEY} \
    -Dscala.time \
    kafka-tweets-0.1.jar
```
All of the params above can be configured by changing application.conf or setting in different file:
```bash
java -jar \
    -Dconfig.resource=/path/to/your/configuration.conf \
    kafka-tweets-0.1.jar
```

# Spark Job

To run the spark job on HDP use this (the job produces hashtags and their counts partitioned by date and hour):
```bash
spark-submit \
    --class by.artsiom.bigdata201.job.Main \
    --master yarn-client
    job.jar
```

Or with a custom configuration file:
```bash
spark-submit \
    --class by.artsiom.bigdata201.job.Main \
    --conf 'spark.driver.extraJavaOptions=-Dconfig.resource=/path/to/application.conf' \
    --master yarn-client
    job.jar
```

<details><summary>Local logs</summary>
<p>

```shell
Using Spark's default log4j profile: org/apache/spark/log4j-defaults.properties
19/01/02 15:31:33 INFO SparkContext: Running Spark version 2.3.0
19/01/02 15:31:33 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
19/01/02 15:31:33 INFO SparkContext: Submitted application: 2aec043e-8081-4647-985e-6ac8dbc904d7
19/01/02 15:31:33 INFO SecurityManager: Changing view acls to: Artsiom_Chuiko
19/01/02 15:31:33 INFO SecurityManager: Changing modify acls to: Artsiom_Chuiko
19/01/02 15:31:33 INFO SecurityManager: Changing view acls groups to:
19/01/02 15:31:33 INFO SecurityManager: Changing modify acls groups to:
19/01/02 15:31:33 INFO SecurityManager: SecurityManager: authentication disabled; ui acls disabled; users  with view permissions: Set(Artsiom_Chuiko); groups with view permissions: Set(); users  with modify permissions: Set(Artsiom_Chuiko); groups with modify permissions: Set()
19/01/02 15:31:34 INFO Utils: Successfully started service 'sparkDriver' on port 58897.
19/01/02 15:31:34 INFO SparkEnv: Registering MapOutputTracker
19/01/02 15:31:34 INFO SparkEnv: Registering BlockManagerMaster
19/01/02 15:31:34 INFO BlockManagerMasterEndpoint: Using org.apache.spark.storage.DefaultTopologyMapper for getting topology information
19/01/02 15:31:34 INFO BlockManagerMasterEndpoint: BlockManagerMasterEndpoint up
19/01/02 15:31:34 INFO DiskBlockManager: Created local directory at C:\Temp\blockmgr-94fbc450-7ad4-42a6-8f36-f2eb6ff11be2
19/01/02 15:31:34 INFO MemoryStore: MemoryStore started with capacity 1992.9 MB
19/01/02 15:31:34 INFO SparkEnv: Registering OutputCommitCoordinator
19/01/02 15:31:34 INFO Utils: Successfully started service 'SparkUI' on port 4040.
19/01/02 15:31:34 INFO SparkUI: Bound SparkUI to 0.0.0.0, and started at http://localhost:4040
19/01/02 15:31:34 INFO Executor: Starting executor ID driver on host localhost
19/01/02 15:31:34 INFO Utils: Successfully started service 'org.apache.spark.network.netty.NettyBlockTransferService' on port 58918.
19/01/02 15:31:34 INFO NettyBlockTransferService: Server created on localhost:58918
19/01/02 15:31:34 INFO BlockManager: Using org.apache.spark.storage.RandomBlockReplicationPolicy for block replication policy
19/01/02 15:31:34 INFO BlockManagerMaster: Registering BlockManager BlockManagerId(driver, localhost, 58918, None)
19/01/02 15:31:34 INFO BlockManagerMasterEndpoint: Registering block manager localhost:58918 with 1992.9 MB RAM, BlockManagerId(driver, localhost, 58918, None)
19/01/02 15:31:34 INFO BlockManagerMaster: Registered BlockManager BlockManagerId(driver, localhost, 58918, None)
19/01/02 15:31:34 INFO BlockManager: Initialized BlockManager: BlockManagerId(driver, localhost, 58918, None)
19/01/02 15:31:34 INFO SharedState: Setting hive.metastore.warehouse.dir ('null') to the value of spark.sql.warehouse.dir ('file:/C://kafka-tweets/spark-warehouse/').
19/01/02 15:31:34 INFO SharedState: Warehouse path is 'file:/C://kafka-tweets/spark-warehouse/'.
19/01/02 15:31:35 INFO StateStoreCoordinatorRef: Registered StateStoreCoordinator endpoint
19/01/02 15:31:36 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 1
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-driver-0
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = earliest

19/01/02 15:31:36 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-1
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 1
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-driver-0
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = earliest

19/01/02 15:31:36 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/02 15:31:36 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/02 15:31:36 INFO AbstractCoordinator: Discovered coordinator 192.168.99.100:9092 (id: 2147483647 rack: null) for group spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-driver-0.
19/01/02 15:31:36 INFO ConsumerCoordinator: Revoking previously assigned partitions [] for group spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-driver-0
19/01/02 15:31:36 INFO AbstractCoordinator: (Re-)joining group spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-driver-0
19/01/02 15:31:37 INFO AbstractCoordinator: Successfully joined group spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-driver-0 with generation 1
19/01/02 15:31:37 INFO ConsumerCoordinator: Setting newly assigned partitions [Tweets-0, Tweets-1, Tweets-2, Tweets-3, Tweets-8, Tweets-9, Tweets-4, Tweets-5, Tweets-6, Tweets-7] for group spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-driver-0
19/01/02 15:31:37 INFO KafkaRelation: GetBatch generating RDD of offset range: KafkaSourceRDDOffsetRange(Tweets-0,-2,-1,None), KafkaSourceRDDOffsetRange(Tweets-1,-2,-1,None), KafkaSourceRDDOffsetRange(Tweets-2,-2,-1,None), KafkaSourceRDDOffsetRange(Tweets-3,-2,-1,None), KafkaSourceRDDOffsetRange(Tweets-4,-2,-1,None), KafkaSourceRDDOffsetRange(Tweets-5,-2,-1,None), KafkaSourceRDDOffsetRange(Tweets-6,-2,-1,None), KafkaSourceRDDOffsetRange(Tweets-7,-2,-1,None), KafkaSourceRDDOffsetRange(Tweets-8,-2,-1,None), KafkaSourceRDDOffsetRange(Tweets-9,-2,-1,None)
19/01/02 15:31:37 INFO ParquetFileFormat: Using default output committer for Parquet: org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:37 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:37 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:38 INFO CodeGenerator: Code generated in 268.075453 ms
19/01/02 15:31:38 INFO SparkContext: Starting job: parquet at Main.scala:35
19/01/02 15:31:38 INFO DAGScheduler: Got job 0 (parquet at Main.scala:35) with 10 output partitions
19/01/02 15:31:38 INFO DAGScheduler: Final stage: ResultStage 0 (parquet at Main.scala:35)
19/01/02 15:31:38 INFO DAGScheduler: Parents of final stage: List()
19/01/02 15:31:38 INFO DAGScheduler: Missing parents: List()
19/01/02 15:31:38 INFO DAGScheduler: Submitting ResultStage 0 (MapPartitionsRDD[6] at parquet at Main.scala:35), which has no missing parents
19/01/02 15:31:38 INFO MemoryStore: Block broadcast_0 stored as values in memory (estimated size 138.9 KB, free 1992.8 MB)
19/01/02 15:31:38 INFO MemoryStore: Block broadcast_0_piece0 stored as bytes in memory (estimated size 50.3 KB, free 1992.7 MB)
19/01/02 15:31:38 INFO BlockManagerInfo: Added broadcast_0_piece0 in memory on localhost:58918 (size: 50.3 KB, free: 1992.9 MB)
19/01/02 15:31:38 INFO SparkContext: Created broadcast 0 from broadcast at DAGScheduler.scala:1039
19/01/02 15:31:38 INFO DAGScheduler: Submitting 10 missing tasks from ResultStage 0 (MapPartitionsRDD[6] at parquet at Main.scala:35) (first 15 tasks are for partitions Vector(0, 1, 2, 3, 4, 5, 6, 7, 8, 9))
19/01/02 15:31:38 INFO TaskSchedulerImpl: Adding task set 0.0 with 10 tasks
19/01/02 15:31:38 INFO TaskSetManager: Starting task 0.0 in stage 0.0 (TID 0, localhost, executor driver, partition 0, PROCESS_LOCAL, 8029 bytes)
19/01/02 15:31:38 INFO TaskSetManager: Starting task 1.0 in stage 0.0 (TID 1, localhost, executor driver, partition 1, PROCESS_LOCAL, 8029 bytes)
19/01/02 15:31:38 INFO TaskSetManager: Starting task 2.0 in stage 0.0 (TID 2, localhost, executor driver, partition 2, PROCESS_LOCAL, 8029 bytes)
19/01/02 15:31:38 INFO TaskSetManager: Starting task 3.0 in stage 0.0 (TID 3, localhost, executor driver, partition 3, PROCESS_LOCAL, 8029 bytes)
19/01/02 15:31:38 INFO Executor: Running task 2.0 in stage 0.0 (TID 2)
19/01/02 15:31:38 INFO Executor: Running task 0.0 in stage 0.0 (TID 0)
19/01/02 15:31:38 INFO Executor: Running task 1.0 in stage 0.0 (TID 1)
19/01/02 15:31:38 INFO Executor: Running task 3.0 in stage 0.0 (TID 3)
19/01/02 15:31:38 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/02 15:31:38 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-2
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/02 15:31:38 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/02 15:31:38 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/02 15:31:38 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/02 15:31:38 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/02 15:31:38 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/02 15:31:38 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-4
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/02 15:31:38 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-3
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/02 15:31:38 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/02 15:31:38 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/02 15:31:38 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/02 15:31:38 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/02 15:31:38 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-5
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/02 15:31:38 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/02 15:31:38 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/02 15:31:38 INFO AbstractCoordinator: Discovered coordinator 192.168.99.100:9092 (id: 2147483647 rack: null) for group spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor.
19/01/02 15:31:38 INFO AbstractCoordinator: Discovered coordinator 192.168.99.100:9092 (id: 2147483647 rack: null) for group spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor.
19/01/02 15:31:38 INFO AbstractCoordinator: Discovered coordinator 192.168.99.100:9092 (id: 2147483647 rack: null) for group spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor.
19/01/02 15:31:38 INFO AbstractCoordinator: Discovered coordinator 192.168.99.100:9092 (id: 2147483647 rack: null) for group spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor.
19/01/02 15:31:38 INFO CodeGenerator: Code generated in 29.768165 ms
19/01/02 15:31:38 INFO CodeGenerator: Code generated in 25.476441 ms
19/01/02 15:31:38 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:38 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:38 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:38 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:38 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:38 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:38 INFO CodecConfig: Compression: SNAPPY
19/01/02 15:31:38 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:38 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:38 INFO CodecConfig: Compression: SNAPPY
19/01/02 15:31:38 INFO ParquetOutputFormat: Parquet block size to 134217728
19/01/02 15:31:38 INFO ParquetOutputFormat: Parquet page size to 1048576
19/01/02 15:31:38 INFO ParquetOutputFormat: Parquet dictionary page size to 1048576
19/01/02 15:31:38 INFO ParquetOutputFormat: Dictionary is on
19/01/02 15:31:38 INFO ParquetOutputFormat: Validation is off
19/01/02 15:31:38 INFO ParquetOutputFormat: Writer version is: PARQUET_1_0
19/01/02 15:31:38 INFO ParquetOutputFormat: Maximum row group padding size is 0 bytes
19/01/02 15:31:38 INFO ParquetOutputFormat: Page size checking is: estimated
19/01/02 15:31:38 INFO ParquetOutputFormat: Min row count for page size check is: 100
19/01/02 15:31:38 INFO ParquetOutputFormat: Max row count for page size check is: 10000
19/01/02 15:31:38 INFO CodecConfig: Compression: SNAPPY
19/01/02 15:31:38 INFO CodecConfig: Compression: SNAPPY
19/01/02 15:31:38 INFO CodecConfig: Compression: SNAPPY
19/01/02 15:31:38 INFO CodecConfig: Compression: SNAPPY
19/01/02 15:31:38 INFO ParquetOutputFormat: Parquet block size to 134217728
19/01/02 15:31:38 INFO ParquetOutputFormat: Parquet page size to 1048576
19/01/02 15:31:38 INFO ParquetOutputFormat: Parquet dictionary page size to 1048576
19/01/02 15:31:38 INFO ParquetOutputFormat: Dictionary is on
19/01/02 15:31:38 INFO ParquetOutputFormat: Validation is off
19/01/02 15:31:38 INFO ParquetOutputFormat: Writer version is: PARQUET_1_0
19/01/02 15:31:38 INFO ParquetOutputFormat: Maximum row group padding size is 0 bytes
19/01/02 15:31:38 INFO ParquetOutputFormat: Page size checking is: estimated
19/01/02 15:31:38 INFO ParquetOutputFormat: Min row count for page size check is: 100
19/01/02 15:31:38 INFO ParquetOutputFormat: Max row count for page size check is: 10000
19/01/02 15:31:38 INFO ParquetOutputFormat: Parquet block size to 134217728
19/01/02 15:31:38 INFO ParquetOutputFormat: Parquet page size to 1048576
19/01/02 15:31:38 INFO ParquetOutputFormat: Parquet dictionary page size to 1048576
19/01/02 15:31:38 INFO ParquetOutputFormat: Dictionary is on
19/01/02 15:31:38 INFO ParquetOutputFormat: Validation is off
19/01/02 15:31:38 INFO ParquetOutputFormat: Writer version is: PARQUET_1_0
19/01/02 15:31:38 INFO ParquetOutputFormat: Maximum row group padding size is 0 bytes
19/01/02 15:31:38 INFO ParquetOutputFormat: Page size checking is: estimated
19/01/02 15:31:38 INFO ParquetOutputFormat: Min row count for page size check is: 100
19/01/02 15:31:38 INFO ParquetOutputFormat: Max row count for page size check is: 10000
19/01/02 15:31:38 INFO ParquetWriteSupport: Initialized Parquet WriteSupport with Catalyst schema:
{
  "type" : "struct",
  "fields" : [ {
    "name" : "key",
    "type" : "binary",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "value",
    "type" : "binary",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "topic",
    "type" : "string",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "partition",
    "type" : "integer",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "offset",
    "type" : "long",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "timestamp",
    "type" : "timestamp",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "timestampType",
    "type" : "integer",
    "nullable" : true,
    "metadata" : { }
  } ]
}
and corresponding Parquet message type:
message spark_schema {
  optional binary key;
  optional binary value;
  optional binary topic (UTF8);
  optional int32 partition;
  optional int64 offset;
  optional int96 timestamp;
  optional int32 timestampType;
}


19/01/02 15:31:38 INFO ParquetWriteSupport: Initialized Parquet WriteSupport with Catalyst schema:
{
  "type" : "struct",
  "fields" : [ {
    "name" : "key",
    "type" : "binary",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "value",
    "type" : "binary",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "topic",
    "type" : "string",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "partition",
    "type" : "integer",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "offset",
    "type" : "long",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "timestamp",
    "type" : "timestamp",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "timestampType",
    "type" : "integer",
    "nullable" : true,
    "metadata" : { }
  } ]
}
and corresponding Parquet message type:
message spark_schema {
  optional binary key;
  optional binary value;
  optional binary topic (UTF8);
  optional int32 partition;
  optional int64 offset;
  optional int96 timestamp;
  optional int32 timestampType;
}


19/01/02 15:31:38 INFO ParquetWriteSupport: Initialized Parquet WriteSupport with Catalyst schema:
{
  "type" : "struct",
  "fields" : [ {
    "name" : "key",
    "type" : "binary",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "value",
    "type" : "binary",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "topic",
    "type" : "string",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "partition",
    "type" : "integer",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "offset",
    "type" : "long",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "timestamp",
    "type" : "timestamp",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "timestampType",
    "type" : "integer",
    "nullable" : true,
    "metadata" : { }
  } ]
}
and corresponding Parquet message type:
message spark_schema {
  optional binary key;
  optional binary value;
  optional binary topic (UTF8);
  optional int32 partition;
  optional int64 offset;
  optional int96 timestamp;
  optional int32 timestampType;
}


19/01/02 15:31:38 INFO CodecConfig: Compression: SNAPPY
19/01/02 15:31:38 INFO CodecConfig: Compression: SNAPPY
19/01/02 15:31:38 INFO ParquetOutputFormat: Parquet block size to 134217728
19/01/02 15:31:38 INFO ParquetOutputFormat: Parquet page size to 1048576
19/01/02 15:31:38 INFO ParquetOutputFormat: Parquet dictionary page size to 1048576
19/01/02 15:31:38 INFO ParquetOutputFormat: Dictionary is on
19/01/02 15:31:38 INFO ParquetOutputFormat: Validation is off
19/01/02 15:31:38 INFO ParquetOutputFormat: Writer version is: PARQUET_1_0
19/01/02 15:31:38 INFO ParquetOutputFormat: Maximum row group padding size is 0 bytes
19/01/02 15:31:38 INFO ParquetOutputFormat: Page size checking is: estimated
19/01/02 15:31:38 INFO ParquetOutputFormat: Min row count for page size check is: 100
19/01/02 15:31:38 INFO ParquetOutputFormat: Max row count for page size check is: 10000
19/01/02 15:31:38 INFO ParquetWriteSupport: Initialized Parquet WriteSupport with Catalyst schema:
{
  "type" : "struct",
  "fields" : [ {
    "name" : "key",
    "type" : "binary",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "value",
    "type" : "binary",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "topic",
    "type" : "string",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "partition",
    "type" : "integer",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "offset",
    "type" : "long",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "timestamp",
    "type" : "timestamp",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "timestampType",
    "type" : "integer",
    "nullable" : true,
    "metadata" : { }
  } ]
}
and corresponding Parquet message type:
message spark_schema {
  optional binary key;
  optional binary value;
  optional binary topic (UTF8);
  optional int32 partition;
  optional int64 offset;
  optional int96 timestamp;
  optional int32 timestampType;
}


19/01/02 15:31:41 INFO CodecPool: Got brand-new compressor [.snappy]
19/01/02 15:31:41 INFO InternalParquetRecordWriter: Flushing mem columnStore to file. allocated memory: 38273
19/01/02 15:31:41 INFO CodecPool: Got brand-new compressor [.snappy]
19/01/02 15:31:41 INFO InternalParquetRecordWriter: Flushing mem columnStore to file. allocated memory: 114167
19/01/02 15:31:41 INFO FileOutputCommitter: Saved output of task 'attempt_20190102153138_0000_m_000002_0' to file:/C://kafka-tweets/testDir1/_temporary/0/task_20190102153138_0000_m_000002
19/01/02 15:31:41 INFO SparkHadoopMapRedUtil: attempt_20190102153138_0000_m_000002_0: Committed
19/01/02 15:31:41 INFO FileOutputCommitter: Saved output of task 'attempt_20190102153138_0000_m_000001_0' to file:/C://kafka-tweets/testDir1/_temporary/0/task_20190102153138_0000_m_000001
19/01/02 15:31:41 INFO SparkHadoopMapRedUtil: attempt_20190102153138_0000_m_000001_0: Committed
19/01/02 15:31:41 INFO Executor: Finished task 1.0 in stage 0.0 (TID 1). 2152 bytes result sent to driver
19/01/02 15:31:41 INFO Executor: Finished task 2.0 in stage 0.0 (TID 2). 2195 bytes result sent to driver
19/01/02 15:31:41 INFO TaskSetManager: Starting task 4.0 in stage 0.0 (TID 4, localhost, executor driver, partition 4, PROCESS_LOCAL, 8029 bytes)
19/01/02 15:31:41 INFO Executor: Running task 4.0 in stage 0.0 (TID 4)
19/01/02 15:31:41 INFO TaskSetManager: Starting task 5.0 in stage 0.0 (TID 5, localhost, executor driver, partition 5, PROCESS_LOCAL, 8029 bytes)
19/01/02 15:31:41 INFO Executor: Running task 5.0 in stage 0.0 (TID 5)
19/01/02 15:31:41 INFO InternalParquetRecordWriter: Flushing mem columnStore to file. allocated memory: 97180
19/01/02 15:31:41 INFO FileOutputCommitter: Saved output of task 'attempt_20190102153138_0000_m_000003_0' to file:/C://kafka-tweets/testDir1/_temporary/0/task_20190102153138_0000_m_000003
19/01/02 15:31:41 INFO SparkHadoopMapRedUtil: attempt_20190102153138_0000_m_000003_0: Committed
19/01/02 15:31:41 INFO Executor: Finished task 3.0 in stage 0.0 (TID 3). 2109 bytes result sent to driver
19/01/02 15:31:41 INFO TaskSetManager: Finished task 2.0 in stage 0.0 (TID 2) in 3478 ms on localhost (executor driver) (1/10)
19/01/02 15:31:41 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/02 15:31:41 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-6
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/02 15:31:41 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/02 15:31:41 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/02 15:31:41 INFO TaskSetManager: Finished task 1.0 in stage 0.0 (TID 1) in 3497 ms on localhost (executor driver) (2/10)
19/01/02 15:31:41 INFO TaskSetManager: Starting task 6.0 in stage 0.0 (TID 6, localhost, executor driver, partition 6, PROCESS_LOCAL, 8029 bytes)
19/01/02 15:31:41 INFO Executor: Running task 6.0 in stage 0.0 (TID 6)
19/01/02 15:31:41 INFO TaskSetManager: Finished task 3.0 in stage 0.0 (TID 3) in 3538 ms on localhost (executor driver) (3/10)
19/01/02 15:31:41 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/02 15:31:41 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-7
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/02 15:31:41 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/02 15:31:41 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/02 15:31:41 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/02 15:31:42 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-8
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/02 15:31:42 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/02 15:31:42 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/02 15:31:42 INFO AbstractCoordinator: Discovered coordinator 192.168.99.100:9092 (id: 2147483647 rack: null) for group spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor.
19/01/02 15:31:42 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:42 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:42 INFO CodecConfig: Compression: SNAPPY
19/01/02 15:31:42 INFO CodecConfig: Compression: SNAPPY
19/01/02 15:31:42 INFO ParquetOutputFormat: Parquet block size to 134217728
19/01/02 15:31:42 INFO ParquetOutputFormat: Parquet page size to 1048576
19/01/02 15:31:42 INFO ParquetOutputFormat: Parquet dictionary page size to 1048576
19/01/02 15:31:42 INFO ParquetOutputFormat: Dictionary is on
19/01/02 15:31:42 INFO ParquetOutputFormat: Validation is off
19/01/02 15:31:42 INFO ParquetOutputFormat: Writer version is: PARQUET_1_0
19/01/02 15:31:42 INFO ParquetOutputFormat: Maximum row group padding size is 0 bytes
19/01/02 15:31:42 INFO ParquetOutputFormat: Page size checking is: estimated
19/01/02 15:31:42 INFO ParquetOutputFormat: Min row count for page size check is: 100
19/01/02 15:31:42 INFO ParquetOutputFormat: Max row count for page size check is: 10000
19/01/02 15:31:42 INFO ParquetWriteSupport: Initialized Parquet WriteSupport with Catalyst schema:
{
  "type" : "struct",
  "fields" : [ {
    "name" : "key",
    "type" : "binary",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "value",
    "type" : "binary",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "topic",
    "type" : "string",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "partition",
    "type" : "integer",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "offset",
    "type" : "long",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "timestamp",
    "type" : "timestamp",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "timestampType",
    "type" : "integer",
    "nullable" : true,
    "metadata" : { }
  } ]
}
and corresponding Parquet message type:
message spark_schema {
  optional binary key;
  optional binary value;
  optional binary topic (UTF8);
  optional int32 partition;
  optional int64 offset;
  optional int96 timestamp;
  optional int32 timestampType;
}


19/01/02 15:31:42 INFO AbstractCoordinator: Discovered coordinator 192.168.99.100:9092 (id: 2147483647 rack: null) for group spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor.
19/01/02 15:31:42 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:42 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:42 INFO AbstractCoordinator: Discovered coordinator 192.168.99.100:9092 (id: 2147483647 rack: null) for group spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor.
19/01/02 15:31:42 INFO CodecConfig: Compression: SNAPPY
19/01/02 15:31:42 INFO CodecConfig: Compression: SNAPPY
19/01/02 15:31:42 INFO ParquetOutputFormat: Parquet block size to 134217728
19/01/02 15:31:42 INFO ParquetOutputFormat: Parquet page size to 1048576
19/01/02 15:31:42 INFO ParquetOutputFormat: Parquet dictionary page size to 1048576
19/01/02 15:31:42 INFO ParquetOutputFormat: Dictionary is on
19/01/02 15:31:42 INFO ParquetOutputFormat: Validation is off
19/01/02 15:31:42 INFO ParquetOutputFormat: Writer version is: PARQUET_1_0
19/01/02 15:31:42 INFO ParquetOutputFormat: Maximum row group padding size is 0 bytes
19/01/02 15:31:42 INFO ParquetOutputFormat: Page size checking is: estimated
19/01/02 15:31:42 INFO ParquetOutputFormat: Min row count for page size check is: 100
19/01/02 15:31:42 INFO ParquetOutputFormat: Max row count for page size check is: 10000
19/01/02 15:31:42 INFO ParquetWriteSupport: Initialized Parquet WriteSupport with Catalyst schema:
{
  "type" : "struct",
  "fields" : [ {
    "name" : "key",
    "type" : "binary",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "value",
    "type" : "binary",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "topic",
    "type" : "string",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "partition",
    "type" : "integer",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "offset",
    "type" : "long",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "timestamp",
    "type" : "timestamp",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "timestampType",
    "type" : "integer",
    "nullable" : true,
    "metadata" : { }
  } ]
}
and corresponding Parquet message type:
message spark_schema {
  optional binary key;
  optional binary value;
  optional binary topic (UTF8);
  optional int32 partition;
  optional int64 offset;
  optional int96 timestamp;
  optional int32 timestampType;
}


19/01/02 15:31:42 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:42 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:42 INFO CodecConfig: Compression: SNAPPY
19/01/02 15:31:42 INFO CodecConfig: Compression: SNAPPY
19/01/02 15:31:42 INFO ParquetOutputFormat: Parquet block size to 134217728
19/01/02 15:31:42 INFO ParquetOutputFormat: Parquet page size to 1048576
19/01/02 15:31:42 INFO ParquetOutputFormat: Parquet dictionary page size to 1048576
19/01/02 15:31:42 INFO ParquetOutputFormat: Dictionary is on
19/01/02 15:31:42 INFO ParquetOutputFormat: Validation is off
19/01/02 15:31:42 INFO ParquetOutputFormat: Writer version is: PARQUET_1_0
19/01/02 15:31:42 INFO ParquetOutputFormat: Maximum row group padding size is 0 bytes
19/01/02 15:31:42 INFO ParquetOutputFormat: Page size checking is: estimated
19/01/02 15:31:42 INFO ParquetOutputFormat: Min row count for page size check is: 100
19/01/02 15:31:42 INFO ParquetOutputFormat: Max row count for page size check is: 10000
19/01/02 15:31:42 INFO ParquetWriteSupport: Initialized Parquet WriteSupport with Catalyst schema:
{
  "type" : "struct",
  "fields" : [ {
    "name" : "key",
    "type" : "binary",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "value",
    "type" : "binary",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "topic",
    "type" : "string",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "partition",
    "type" : "integer",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "offset",
    "type" : "long",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "timestamp",
    "type" : "timestamp",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "timestampType",
    "type" : "integer",
    "nullable" : true,
    "metadata" : { }
  } ]
}
and corresponding Parquet message type:
message spark_schema {
  optional binary key;
  optional binary value;
  optional binary topic (UTF8);
  optional int32 partition;
  optional int64 offset;
  optional int96 timestamp;
  optional int32 timestampType;
}


19/01/02 15:31:42 INFO InternalParquetRecordWriter: Flushing mem columnStore to file. allocated memory: 56474
19/01/02 15:31:42 INFO FileOutputCommitter: Saved output of task 'attempt_20190102153138_0000_m_000000_0' to file:/C://kafka-tweets/testDir1/_temporary/0/task_20190102153138_0000_m_000000
19/01/02 15:31:42 INFO SparkHadoopMapRedUtil: attempt_20190102153138_0000_m_000000_0: Committed
19/01/02 15:31:42 INFO Executor: Finished task 0.0 in stage 0.0 (TID 0). 2195 bytes result sent to driver
19/01/02 15:31:42 INFO TaskSetManager: Starting task 7.0 in stage 0.0 (TID 7, localhost, executor driver, partition 7, PROCESS_LOCAL, 8029 bytes)
19/01/02 15:31:42 INFO TaskSetManager: Finished task 0.0 in stage 0.0 (TID 0) in 4149 ms on localhost (executor driver) (4/10)
19/01/02 15:31:42 INFO Executor: Running task 7.0 in stage 0.0 (TID 7)
19/01/02 15:31:42 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/02 15:31:42 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-9
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/02 15:31:42 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/02 15:31:42 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/02 15:31:42 INFO AbstractCoordinator: Discovered coordinator 192.168.99.100:9092 (id: 2147483647 rack: null) for group spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor.
19/01/02 15:31:42 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:42 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:42 INFO CodecConfig: Compression: SNAPPY
19/01/02 15:31:42 INFO CodecConfig: Compression: SNAPPY
19/01/02 15:31:42 INFO ParquetOutputFormat: Parquet block size to 134217728
19/01/02 15:31:42 INFO ParquetOutputFormat: Parquet page size to 1048576
19/01/02 15:31:42 INFO ParquetOutputFormat: Parquet dictionary page size to 1048576
19/01/02 15:31:42 INFO ParquetOutputFormat: Dictionary is on
19/01/02 15:31:42 INFO ParquetOutputFormat: Validation is off
19/01/02 15:31:42 INFO ParquetOutputFormat: Writer version is: PARQUET_1_0
19/01/02 15:31:42 INFO ParquetOutputFormat: Maximum row group padding size is 0 bytes
19/01/02 15:31:42 INFO ParquetOutputFormat: Page size checking is: estimated
19/01/02 15:31:42 INFO ParquetOutputFormat: Min row count for page size check is: 100
19/01/02 15:31:42 INFO ParquetOutputFormat: Max row count for page size check is: 10000
19/01/02 15:31:42 INFO ParquetWriteSupport: Initialized Parquet WriteSupport with Catalyst schema:
{
  "type" : "struct",
  "fields" : [ {
    "name" : "key",
    "type" : "binary",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "value",
    "type" : "binary",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "topic",
    "type" : "string",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "partition",
    "type" : "integer",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "offset",
    "type" : "long",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "timestamp",
    "type" : "timestamp",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "timestampType",
    "type" : "integer",
    "nullable" : true,
    "metadata" : { }
  } ]
}
and corresponding Parquet message type:
message spark_schema {
  optional binary key;
  optional binary value;
  optional binary topic (UTF8);
  optional int32 partition;
  optional int64 offset;
  optional int96 timestamp;
  optional int32 timestampType;
}


19/01/02 15:31:43 INFO InternalParquetRecordWriter: Flushing mem columnStore to file. allocated memory: 54777
19/01/02 15:31:43 INFO FileOutputCommitter: Saved output of task 'attempt_20190102153142_0000_m_000005_0' to file:/C://kafka-tweets/testDir1/_temporary/0/task_20190102153142_0000_m_000005
19/01/02 15:31:43 INFO SparkHadoopMapRedUtil: attempt_20190102153142_0000_m_000005_0: Committed
19/01/02 15:31:43 INFO Executor: Finished task 5.0 in stage 0.0 (TID 5). 2195 bytes result sent to driver
19/01/02 15:31:43 INFO TaskSetManager: Starting task 8.0 in stage 0.0 (TID 8, localhost, executor driver, partition 8, PROCESS_LOCAL, 8029 bytes)
19/01/02 15:31:43 INFO Executor: Running task 8.0 in stage 0.0 (TID 8)
19/01/02 15:31:43 INFO TaskSetManager: Finished task 5.0 in stage 0.0 (TID 5) in 1698 ms on localhost (executor driver) (5/10)
19/01/02 15:31:43 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/02 15:31:43 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-10
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/02 15:31:43 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/02 15:31:43 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/02 15:31:43 INFO AbstractCoordinator: Discovered coordinator 192.168.99.100:9092 (id: 2147483647 rack: null) for group spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor.
19/01/02 15:31:43 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:43 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:43 INFO CodecConfig: Compression: SNAPPY
19/01/02 15:31:43 INFO CodecConfig: Compression: SNAPPY
19/01/02 15:31:43 INFO ParquetOutputFormat: Parquet block size to 134217728
19/01/02 15:31:43 INFO ParquetOutputFormat: Parquet page size to 1048576
19/01/02 15:31:43 INFO ParquetOutputFormat: Parquet dictionary page size to 1048576
19/01/02 15:31:43 INFO ParquetOutputFormat: Dictionary is on
19/01/02 15:31:43 INFO ParquetOutputFormat: Validation is off
19/01/02 15:31:43 INFO ParquetOutputFormat: Writer version is: PARQUET_1_0
19/01/02 15:31:43 INFO ParquetOutputFormat: Maximum row group padding size is 0 bytes
19/01/02 15:31:43 INFO ParquetOutputFormat: Page size checking is: estimated
19/01/02 15:31:43 INFO ParquetOutputFormat: Min row count for page size check is: 100
19/01/02 15:31:43 INFO ParquetOutputFormat: Max row count for page size check is: 10000
19/01/02 15:31:43 INFO ParquetWriteSupport: Initialized Parquet WriteSupport with Catalyst schema:
{
  "type" : "struct",
  "fields" : [ {
    "name" : "key",
    "type" : "binary",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "value",
    "type" : "binary",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "topic",
    "type" : "string",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "partition",
    "type" : "integer",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "offset",
    "type" : "long",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "timestamp",
    "type" : "timestamp",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "timestampType",
    "type" : "integer",
    "nullable" : true,
    "metadata" : { }
  } ]
}
and corresponding Parquet message type:
message spark_schema {
  optional binary key;
  optional binary value;
  optional binary topic (UTF8);
  optional int32 partition;
  optional int64 offset;
  optional int96 timestamp;
  optional int32 timestampType;
}


19/01/02 15:31:44 INFO InternalParquetRecordWriter: Flushing mem columnStore to file. allocated memory: 63214
19/01/02 15:31:44 INFO FileOutputCommitter: Saved output of task 'attempt_20190102153142_0000_m_000006_0' to file:/C://kafka-tweets/testDir1/_temporary/0/task_20190102153142_0000_m_000006
19/01/02 15:31:44 INFO SparkHadoopMapRedUtil: attempt_20190102153142_0000_m_000006_0: Committed
19/01/02 15:31:44 INFO Executor: Finished task 6.0 in stage 0.0 (TID 6). 2152 bytes result sent to driver
19/01/02 15:31:44 INFO TaskSetManager: Starting task 9.0 in stage 0.0 (TID 9, localhost, executor driver, partition 9, PROCESS_LOCAL, 8029 bytes)
19/01/02 15:31:44 INFO Executor: Running task 9.0 in stage 0.0 (TID 9)
19/01/02 15:31:44 INFO TaskSetManager: Finished task 6.0 in stage 0.0 (TID 6) in 2269 ms on localhost (executor driver) (6/10)
19/01/02 15:31:44 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/02 15:31:44 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-11
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/02 15:31:44 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/02 15:31:44 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/02 15:31:44 INFO AbstractCoordinator: Discovered coordinator 192.168.99.100:9092 (id: 2147483647 rack: null) for group spark-kafka-relation-44e2ec42-9211-40fe-976b-0bd444689e34-executor.
19/01/02 15:31:44 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:44 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:44 INFO CodecConfig: Compression: SNAPPY
19/01/02 15:31:44 INFO CodecConfig: Compression: SNAPPY
19/01/02 15:31:44 INFO ParquetOutputFormat: Parquet block size to 134217728
19/01/02 15:31:44 INFO ParquetOutputFormat: Parquet page size to 1048576
19/01/02 15:31:44 INFO ParquetOutputFormat: Parquet dictionary page size to 1048576
19/01/02 15:31:44 INFO ParquetOutputFormat: Dictionary is on
19/01/02 15:31:44 INFO ParquetOutputFormat: Validation is off
19/01/02 15:31:44 INFO ParquetOutputFormat: Writer version is: PARQUET_1_0
19/01/02 15:31:44 INFO ParquetOutputFormat: Maximum row group padding size is 0 bytes
19/01/02 15:31:44 INFO ParquetOutputFormat: Page size checking is: estimated
19/01/02 15:31:44 INFO ParquetOutputFormat: Min row count for page size check is: 100
19/01/02 15:31:44 INFO ParquetOutputFormat: Max row count for page size check is: 10000
19/01/02 15:31:44 INFO ParquetWriteSupport: Initialized Parquet WriteSupport with Catalyst schema:
{
  "type" : "struct",
  "fields" : [ {
    "name" : "key",
    "type" : "binary",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "value",
    "type" : "binary",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "topic",
    "type" : "string",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "partition",
    "type" : "integer",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "offset",
    "type" : "long",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "timestamp",
    "type" : "timestamp",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "timestampType",
    "type" : "integer",
    "nullable" : true,
    "metadata" : { }
  } ]
}
and corresponding Parquet message type:
message spark_schema {
  optional binary key;
  optional binary value;
  optional binary topic (UTF8);
  optional int32 partition;
  optional int64 offset;
  optional int96 timestamp;
  optional int32 timestampType;
}


19/01/02 15:31:44 INFO InternalParquetRecordWriter: Flushing mem columnStore to file. allocated memory: 112169
19/01/02 15:31:44 INFO FileOutputCommitter: Saved output of task 'attempt_20190102153142_0000_m_000007_0' to file:/C://kafka-tweets/testDir1/_temporary/0/task_20190102153142_0000_m_000007
19/01/02 15:31:44 INFO SparkHadoopMapRedUtil: attempt_20190102153142_0000_m_000007_0: Committed
19/01/02 15:31:44 INFO Executor: Finished task 7.0 in stage 0.0 (TID 7). 2152 bytes result sent to driver
19/01/02 15:31:44 INFO TaskSetManager: Finished task 7.0 in stage 0.0 (TID 7) in 1971 ms on localhost (executor driver) (7/10)
19/01/02 15:31:45 INFO InternalParquetRecordWriter: Flushing mem columnStore to file. allocated memory: 102705
19/01/02 15:31:45 INFO FileOutputCommitter: Saved output of task 'attempt_20190102153142_0000_m_000004_0' to file:/C://kafka-tweets/testDir1/_temporary/0/task_20190102153142_0000_m_000004
19/01/02 15:31:45 INFO SparkHadoopMapRedUtil: attempt_20190102153142_0000_m_000004_0: Committed
19/01/02 15:31:45 INFO Executor: Finished task 4.0 in stage 0.0 (TID 4). 2152 bytes result sent to driver
19/01/02 15:31:45 INFO TaskSetManager: Finished task 4.0 in stage 0.0 (TID 4) in 3297 ms on localhost (executor driver) (8/10)
19/01/02 15:31:45 INFO InternalParquetRecordWriter: Flushing mem columnStore to file. allocated memory: 94308
19/01/02 15:31:45 INFO FileOutputCommitter: Saved output of task 'attempt_20190102153143_0000_m_000008_0' to file:/C://kafka-tweets/testDir1/_temporary/0/task_20190102153143_0000_m_000008
19/01/02 15:31:45 INFO SparkHadoopMapRedUtil: attempt_20190102153143_0000_m_000008_0: Committed
19/01/02 15:31:45 INFO Executor: Finished task 8.0 in stage 0.0 (TID 8). 2152 bytes result sent to driver
19/01/02 15:31:45 INFO TaskSetManager: Finished task 8.0 in stage 0.0 (TID 8) in 2172 ms on localhost (executor driver) (9/10)
19/01/02 15:31:46 INFO InternalParquetRecordWriter: Flushing mem columnStore to file. allocated memory: 85259
19/01/02 15:31:46 INFO FileOutputCommitter: Saved output of task 'attempt_20190102153144_0000_m_000009_0' to file:/C://kafka-tweets/testDir1/_temporary/0/task_20190102153144_0000_m_000009
19/01/02 15:31:46 INFO SparkHadoopMapRedUtil: attempt_20190102153144_0000_m_000009_0: Committed
19/01/02 15:31:46 INFO Executor: Finished task 9.0 in stage 0.0 (TID 9). 2109 bytes result sent to driver
19/01/02 15:31:46 INFO TaskSetManager: Finished task 9.0 in stage 0.0 (TID 9) in 2580 ms on localhost (executor driver) (10/10)
19/01/02 15:31:46 INFO TaskSchedulerImpl: Removed TaskSet 0.0, whose tasks have all completed, from pool
19/01/02 15:31:46 INFO DAGScheduler: ResultStage 0 (parquet at Main.scala:35) finished in 8.603 s
19/01/02 15:31:46 INFO DAGScheduler: Job 0 finished: parquet at Main.scala:35, took 8.647160 s
19/01/02 15:31:47 INFO FileFormatWriter: Job null committed.
19/01/02 15:31:47 INFO FileFormatWriter: Finished processing stats for job null.
19/01/02 15:31:47 INFO SparkContext: Starting job: parquet at Main.scala:38
19/01/02 15:31:47 INFO DAGScheduler: Got job 1 (parquet at Main.scala:38) with 1 output partitions
19/01/02 15:31:47 INFO DAGScheduler: Final stage: ResultStage 1 (parquet at Main.scala:38)
19/01/02 15:31:47 INFO DAGScheduler: Parents of final stage: List()
19/01/02 15:31:47 INFO DAGScheduler: Missing parents: List()
19/01/02 15:31:47 INFO DAGScheduler: Submitting ResultStage 1 (MapPartitionsRDD[9] at parquet at Main.scala:38), which has no missing parents
19/01/02 15:31:48 INFO MemoryStore: Block broadcast_1 stored as values in memory (estimated size 63.1 KB, free 1992.7 MB)
19/01/02 15:31:48 INFO MemoryStore: Block broadcast_1_piece0 stored as bytes in memory (estimated size 22.0 KB, free 1992.6 MB)
19/01/02 15:31:48 INFO BlockManagerInfo: Added broadcast_1_piece0 in memory on localhost:58918 (size: 22.0 KB, free: 1992.8 MB)
19/01/02 15:31:48 INFO SparkContext: Created broadcast 1 from broadcast at DAGScheduler.scala:1039
19/01/02 15:31:48 INFO DAGScheduler: Submitting 1 missing tasks from ResultStage 1 (MapPartitionsRDD[9] at parquet at Main.scala:38) (first 15 tasks are for partitions Vector(0))
19/01/02 15:31:48 INFO TaskSchedulerImpl: Adding task set 1.0 with 1 tasks
19/01/02 15:31:48 INFO TaskSetManager: Starting task 0.0 in stage 1.0 (TID 10, localhost, executor driver, partition 0, PROCESS_LOCAL, 8077 bytes)
19/01/02 15:31:48 INFO Executor: Running task 0.0 in stage 1.0 (TID 10)
19/01/02 15:31:48 INFO Executor: Finished task 0.0 in stage 1.0 (TID 10). 1937 bytes result sent to driver
19/01/02 15:31:48 INFO TaskSetManager: Finished task 0.0 in stage 1.0 (TID 10) in 203 ms on localhost (executor driver) (1/1)
19/01/02 15:31:48 INFO TaskSchedulerImpl: Removed TaskSet 1.0, whose tasks have all completed, from pool
19/01/02 15:31:48 INFO DAGScheduler: ResultStage 1 (parquet at Main.scala:38) finished in 0.275 s
19/01/02 15:31:48 INFO DAGScheduler: Job 1 finished: parquet at Main.scala:38, took 0.333404 s
19/01/02 15:31:48 WARN Utils: Truncated the string representation of a plan since it was too large. This behavior can be adjusted by setting 'spark.debug.maxToStringFields' in SparkEnv.conf.
19/01/02 15:31:48 INFO FileSourceStrategy: Pruning directories with:
19/01/02 15:31:48 INFO FileSourceStrategy: Post-Scan Filters:
19/01/02 15:31:48 INFO FileSourceStrategy: Output Data Schema: struct<key: binary, value: binary>
19/01/02 15:31:48 INFO FileSourceScanExec: Pushed Filters:
19/01/02 15:31:49 INFO ParquetFileFormat: Using default output committer for Parquet: org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:49 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:49 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:49 INFO CodeGenerator: Code generated in 28.336093 ms
19/01/02 15:31:49 INFO CodeGenerator: Code generated in 39.06332 ms
19/01/02 15:31:49 INFO CodeGenerator: Code generated in 14.83485 ms
19/01/02 15:31:49 INFO MemoryStore: Block broadcast_2 stored as values in memory (estimated size 223.0 KB, free 1992.4 MB)
19/01/02 15:31:49 INFO MemoryStore: Block broadcast_2_piece0 stored as bytes in memory (estimated size 21.1 KB, free 1992.4 MB)
19/01/02 15:31:49 INFO BlockManagerInfo: Added broadcast_2_piece0 in memory on localhost:58918 (size: 21.1 KB, free: 1992.8 MB)
19/01/02 15:31:49 INFO SparkContext: Created broadcast 2 from parquet at Main.scala:64
19/01/02 15:31:49 INFO FileSourceScanExec: Planning scan with bin packing, max size: 10539984 bytes, open cost is considered as scanning 4194304 bytes.
19/01/02 15:31:49 INFO SparkContext: Starting job: parquet at Main.scala:64
19/01/02 15:31:49 INFO DAGScheduler: Registering RDD 14 (parquet at Main.scala:64)
19/01/02 15:31:49 INFO DAGScheduler: Got job 2 (parquet at Main.scala:64) with 200 output partitions
19/01/02 15:31:49 INFO DAGScheduler: Final stage: ResultStage 3 (parquet at Main.scala:64)
19/01/02 15:31:49 INFO DAGScheduler: Parents of final stage: List(ShuffleMapStage 2)
19/01/02 15:31:49 INFO DAGScheduler: Missing parents: List(ShuffleMapStage 2)
19/01/02 15:31:49 INFO DAGScheduler: Submitting ShuffleMapStage 2 (MapPartitionsRDD[14] at parquet at Main.scala:64), which has no missing parents
19/01/02 15:31:49 INFO MemoryStore: Block broadcast_3 stored as values in memory (estimated size 36.5 KB, free 1992.4 MB)
19/01/02 15:31:49 INFO MemoryStore: Block broadcast_3_piece0 stored as bytes in memory (estimated size 15.8 KB, free 1992.3 MB)
19/01/02 15:31:49 INFO BlockManagerInfo: Added broadcast_3_piece0 in memory on localhost:58918 (size: 15.8 KB, free: 1992.8 MB)
19/01/02 15:31:49 INFO SparkContext: Created broadcast 3 from broadcast at DAGScheduler.scala:1039
19/01/02 15:31:49 INFO DAGScheduler: Submitting 4 missing tasks from ShuffleMapStage 2 (MapPartitionsRDD[14] at parquet at Main.scala:64) (first 15 tasks are for partitions Vector(0, 1, 2, 3))
19/01/02 15:31:49 INFO TaskSchedulerImpl: Adding task set 2.0 with 4 tasks
19/01/02 15:31:49 INFO TaskSetManager: Starting task 0.0 in stage 2.0 (TID 11, localhost, executor driver, partition 0, PROCESS_LOCAL, 8689 bytes)
19/01/02 15:31:49 INFO TaskSetManager: Starting task 1.0 in stage 2.0 (TID 12, localhost, executor driver, partition 1, PROCESS_LOCAL, 8689 bytes)
19/01/02 15:31:49 INFO TaskSetManager: Starting task 2.0 in stage 2.0 (TID 13, localhost, executor driver, partition 2, PROCESS_LOCAL, 8689 bytes)
19/01/02 15:31:49 INFO TaskSetManager: Starting task 3.0 in stage 2.0 (TID 14, localhost, executor driver, partition 3, PROCESS_LOCAL, 8379 bytes)
19/01/02 15:31:49 INFO Executor: Running task 1.0 in stage 2.0 (TID 12)
19/01/02 15:31:49 INFO Executor: Running task 2.0 in stage 2.0 (TID 13)
19/01/02 15:31:49 INFO Executor: Running task 0.0 in stage 2.0 (TID 11)
19/01/02 15:31:49 INFO Executor: Running task 3.0 in stage 2.0 (TID 14)
19/01/02 15:31:49 INFO CodeGenerator: Code generated in 31.496273 ms
19/01/02 15:31:49 INFO CodeGenerator: Code generated in 10.106609 ms
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 24
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 28
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 7
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 19
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 25
19/01/02 15:31:49 INFO CodeGenerator: Code generated in 23.570646 ms
19/01/02 15:31:49 INFO BlockManagerInfo: Removed broadcast_0_piece0 on localhost:58918 in memory (size: 50.3 KB, free: 1992.8 MB)
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 48
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 10
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 42
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 8
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 34
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 26
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 20
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 17
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 44
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 46
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 45
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 54
19/01/02 15:31:49 INFO BlockManagerInfo: Removed broadcast_1_piece0 on localhost:58918 in memory (size: 22.0 KB, free: 1992.9 MB)
19/01/02 15:31:49 INFO CodeGenerator: Code generated in 16.159476 ms
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 9
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 37
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 49
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 51
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 27
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 11
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 23
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 13
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 47
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 50
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 52
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 15
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 33
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 56
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 16
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 38
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 14
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 36
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 29
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 32
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 39
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 53
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 40
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 21
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 41
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 30
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 43
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 22
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 31
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 12
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 57
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 18
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 55
19/01/02 15:31:49 INFO ContextCleaner: Cleaned accumulator 35
19/01/02 15:31:49 INFO CodeGenerator: Code generated in 7.808558 ms
19/01/02 15:31:49 INFO FileScanRDD: Reading File path: file:///C://kafka-tweets/testDir1/part-00003-36ff5128-d7a3-426d-921b-bfa4b474925e-c000.snappy.parquet, range: 0-26613, partition values: [empty row]
19/01/02 15:31:49 INFO FileScanRDD: Reading File path: file:///C://kafka-tweets/testDir1/part-00002-36ff5128-d7a3-426d-921b-bfa4b474925e-c000.snappy.parquet, range: 0-12607, partition values: [empty row]
19/01/02 15:31:49 INFO FileScanRDD: Reading File path: file:///C://kafka-tweets/testDir1/part-00000-36ff5128-d7a3-426d-921b-bfa4b474925e-c000.snappy.parquet, range: 0-16195, partition values: [empty row]
19/01/02 15:31:49 INFO FileScanRDD: Reading File path: file:///C://kafka-tweets/testDir1/part-00001-36ff5128-d7a3-426d-921b-bfa4b474925e-c000.snappy.parquet, range: 0-29891, partition values: [empty row]
19/01/02 15:31:49 INFO CodecPool: Got brand-new decompressor [.snappy]
19/01/02 15:31:49 INFO CodecPool: Got brand-new decompressor [.snappy]
19/01/02 15:31:49 INFO CodecPool: Got brand-new decompressor [.snappy]
19/01/02 15:31:49 INFO CodecPool: Got brand-new decompressor [.snappy]
19/01/02 15:31:49 INFO FileScanRDD: Reading File path: file:///C://kafka-tweets/testDir1/part-00005-36ff5128-d7a3-426d-921b-bfa4b474925e-c000.snappy.parquet, range: 0-16002, partition values: [empty row]
19/01/02 15:31:49 INFO FileScanRDD: Reading File path: file:///C://kafka-tweets/testDir1/part-00006-36ff5128-d7a3-426d-921b-bfa4b474925e-c000.snappy.parquet, range: 0-15020, partition values: [empty row]
19/01/02 15:31:49 INFO FileScanRDD: Reading File path: file:///C://kafka-tweets/testDir1/part-00007-36ff5128-d7a3-426d-921b-bfa4b474925e-c000.snappy.parquet, range: 0-28176, partition values: [empty row]
19/01/02 15:31:49 INFO FileScanRDD: Reading File path: file:///C://kafka-tweets/testDir1/part-00008-36ff5128-d7a3-426d-921b-bfa4b474925e-c000.snappy.parquet, range: 0-24342, partition values: [empty row]
19/01/02 15:31:49 INFO FileScanRDD: Reading File path: file:///C://kafka-tweets/testDir1/part-00009-36ff5128-d7a3-426d-921b-bfa4b474925e-c000.snappy.parquet, range: 0-20848, partition values: [empty row]
19/01/02 15:31:49 INFO FileScanRDD: Reading File path: file:///C://kafka-tweets/testDir1/part-00004-36ff5128-d7a3-426d-921b-bfa4b474925e-c000.snappy.parquet, range: 0-27205, partition values: [empty row]
19/01/02 15:31:50 INFO Executor: Finished task 3.0 in stage 2.0 (TID 14). 2505 bytes result sent to driver
19/01/02 15:31:50 INFO TaskSetManager: Finished task 3.0 in stage 2.0 (TID 14) in 831 ms on localhost (executor driver) (1/4)
19/01/02 15:31:50 INFO Executor: Finished task 2.0 in stage 2.0 (TID 13). 2462 bytes result sent to driver
19/01/02 15:31:50 INFO TaskSetManager: Finished task 2.0 in stage 2.0 (TID 13) in 904 ms on localhost (executor driver) (2/4)
19/01/02 15:31:50 INFO Executor: Finished task 1.0 in stage 2.0 (TID 12). 2462 bytes result sent to driver
19/01/02 15:31:50 INFO TaskSetManager: Finished task 1.0 in stage 2.0 (TID 12) in 958 ms on localhost (executor driver) (3/4)
19/01/02 15:31:50 INFO Executor: Finished task 0.0 in stage 2.0 (TID 11). 2462 bytes result sent to driver
19/01/02 15:31:50 INFO TaskSetManager: Finished task 0.0 in stage 2.0 (TID 11) in 1050 ms on localhost (executor driver) (4/4)
19/01/02 15:31:50 INFO TaskSchedulerImpl: Removed TaskSet 2.0, whose tasks have all completed, from pool
19/01/02 15:31:50 INFO DAGScheduler: ShuffleMapStage 2 (parquet at Main.scala:64) finished in 1.061 s
19/01/02 15:31:50 INFO DAGScheduler: looking for newly runnable stages
19/01/02 15:31:50 INFO DAGScheduler: running: Set()
19/01/02 15:31:50 INFO DAGScheduler: waiting: Set(ResultStage 3)
19/01/02 15:31:50 INFO DAGScheduler: failed: Set()
19/01/02 15:31:50 INFO DAGScheduler: Submitting ResultStage 3 (MapPartitionsRDD[17] at parquet at Main.scala:64), which has no missing parents
19/01/02 15:31:50 INFO MemoryStore: Block broadcast_4 stored as values in memory (estimated size 155.4 KB, free 1992.5 MB)
19/01/02 15:31:50 INFO MemoryStore: Block broadcast_4_piece0 stored as bytes in memory (estimated size 56.8 KB, free 1992.4 MB)
19/01/02 15:31:50 INFO BlockManagerInfo: Added broadcast_4_piece0 in memory on localhost:58918 (size: 56.8 KB, free: 1992.8 MB)
19/01/02 15:31:50 INFO SparkContext: Created broadcast 4 from broadcast at DAGScheduler.scala:1039
19/01/02 15:31:50 INFO DAGScheduler: Submitting 200 missing tasks from ResultStage 3 (MapPartitionsRDD[17] at parquet at Main.scala:64) (first 15 tasks are for partitions Vector(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14))
19/01/02 15:31:50 INFO TaskSchedulerImpl: Adding task set 3.0 with 200 tasks
19/01/02 15:31:50 INFO TaskSetManager: Starting task 1.0 in stage 3.0 (TID 15, localhost, executor driver, partition 1, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 4.0 in stage 3.0 (TID 16, localhost, executor driver, partition 4, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 7.0 in stage 3.0 (TID 17, localhost, executor driver, partition 7, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 9.0 in stage 3.0 (TID 18, localhost, executor driver, partition 9, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO Executor: Running task 4.0 in stage 3.0 (TID 16)
19/01/02 15:31:50 INFO Executor: Running task 1.0 in stage 3.0 (TID 15)
19/01/02 15:31:50 INFO Executor: Running task 7.0 in stage 3.0 (TID 17)
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO Executor: Running task 9.0 in stage 3.0 (TID 18)
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 7 ms
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 5 ms
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 8 ms
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO CodeGenerator: Code generated in 11.796229 ms
19/01/02 15:31:50 INFO CodeGenerator: Code generated in 8.751192 ms
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000004_0
19/01/02 15:31:50 INFO Executor: Finished task 4.0 in stage 3.0 (TID 16). 4012 bytes result sent to driver
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000009_0
19/01/02 15:31:50 INFO Executor: Finished task 9.0 in stage 3.0 (TID 18). 3926 bytes result sent to driver
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000007_0
19/01/02 15:31:50 INFO Executor: Finished task 7.0 in stage 3.0 (TID 17). 4012 bytes result sent to driver
19/01/02 15:31:50 INFO TaskSetManager: Starting task 10.0 in stage 3.0 (TID 19, localhost, executor driver, partition 10, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 11.0 in stage 3.0 (TID 20, localhost, executor driver, partition 11, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 13.0 in stage 3.0 (TID 21, localhost, executor driver, partition 13, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO Executor: Running task 10.0 in stage 3.0 (TID 19)
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000001_0
19/01/02 15:31:50 INFO Executor: Finished task 1.0 in stage 3.0 (TID 15). 4012 bytes result sent to driver
19/01/02 15:31:50 INFO TaskSetManager: Finished task 4.0 in stage 3.0 (TID 16) in 90 ms on localhost (executor driver) (1/200)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 9.0 in stage 3.0 (TID 18) in 90 ms on localhost (executor driver) (2/200)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 7.0 in stage 3.0 (TID 17) in 90 ms on localhost (executor driver) (3/200)
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000010_0
19/01/02 15:31:50 INFO Executor: Finished task 10.0 in stage 3.0 (TID 19). 3926 bytes result sent to driver
19/01/02 15:31:50 INFO Executor: Running task 13.0 in stage 3.0 (TID 21)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 14.0 in stage 3.0 (TID 22, localhost, executor driver, partition 14, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 15.0 in stage 3.0 (TID 23, localhost, executor driver, partition 15, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO Executor: Running task 14.0 in stage 3.0 (TID 22)
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000014_0
19/01/02 15:31:50 INFO Executor: Finished task 14.0 in stage 3.0 (TID 22). 3969 bytes result sent to driver
19/01/02 15:31:50 INFO Executor: Running task 11.0 in stage 3.0 (TID 20)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 1.0 in stage 3.0 (TID 15) in 112 ms on localhost (executor driver) (4/200)
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO TaskSetManager: Finished task 10.0 in stage 3.0 (TID 19) in 29 ms on localhost (executor driver) (5/200)
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000013_0
19/01/02 15:31:50 INFO Executor: Finished task 13.0 in stage 3.0 (TID 21). 3969 bytes result sent to driver
19/01/02 15:31:50 INFO Executor: Running task 15.0 in stage 3.0 (TID 23)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 17.0 in stage 3.0 (TID 24, localhost, executor driver, partition 17, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 19.0 in stage 3.0 (TID 25, localhost, executor driver, partition 19, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO Executor: Running task 17.0 in stage 3.0 (TID 24)
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO TaskSetManager: Finished task 14.0 in stage 3.0 (TID 22) in 24 ms on localhost (executor driver) (6/200)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 13.0 in stage 3.0 (TID 21) in 38 ms on localhost (executor driver) (7/200)
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000017_0
19/01/02 15:31:50 INFO Executor: Finished task 17.0 in stage 3.0 (TID 24). 3969 bytes result sent to driver
19/01/02 15:31:50 INFO TaskSetManager: Starting task 20.0 in stage 3.0 (TID 26, localhost, executor driver, partition 20, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000015_0
19/01/02 15:31:50 INFO TaskSetManager: Finished task 17.0 in stage 3.0 (TID 24) in 12 ms on localhost (executor driver) (8/200)
19/01/02 15:31:50 INFO Executor: Running task 20.0 in stage 3.0 (TID 26)
19/01/02 15:31:50 INFO Executor: Finished task 15.0 in stage 3.0 (TID 23). 3969 bytes result sent to driver
19/01/02 15:31:50 INFO Executor: Running task 19.0 in stage 3.0 (TID 25)
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000020_0
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000019_0
19/01/02 15:31:50 INFO Executor: Finished task 20.0 in stage 3.0 (TID 26). 3969 bytes result sent to driver
19/01/02 15:31:50 INFO Executor: Finished task 19.0 in stage 3.0 (TID 25). 3969 bytes result sent to driver
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000011_0
19/01/02 15:31:50 INFO Executor: Finished task 11.0 in stage 3.0 (TID 20). 3926 bytes result sent to driver
19/01/02 15:31:50 INFO TaskSetManager: Starting task 22.0 in stage 3.0 (TID 27, localhost, executor driver, partition 22, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 23.0 in stage 3.0 (TID 28, localhost, executor driver, partition 23, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 24.0 in stage 3.0 (TID 29, localhost, executor driver, partition 24, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 26.0 in stage 3.0 (TID 30, localhost, executor driver, partition 26, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO Executor: Running task 23.0 in stage 3.0 (TID 28)
19/01/02 15:31:50 INFO Executor: Running task 24.0 in stage 3.0 (TID 29)
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000024_0
19/01/02 15:31:50 INFO Executor: Finished task 24.0 in stage 3.0 (TID 29). 3969 bytes result sent to driver
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000023_0
19/01/02 15:31:50 INFO Executor: Finished task 23.0 in stage 3.0 (TID 28). 3926 bytes result sent to driver
19/01/02 15:31:50 INFO Executor: Running task 22.0 in stage 3.0 (TID 27)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 27.0 in stage 3.0 (TID 31, localhost, executor driver, partition 27, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 30.0 in stage 3.0 (TID 32, localhost, executor driver, partition 30, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO Executor: Running task 26.0 in stage 3.0 (TID 30)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 11.0 in stage 3.0 (TID 20) in 94 ms on localhost (executor driver) (9/200)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 24.0 in stage 3.0 (TID 29) in 19 ms on localhost (executor driver) (10/200)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 23.0 in stage 3.0 (TID 28) in 19 ms on localhost (executor driver) (11/200)
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO Executor: Running task 30.0 in stage 3.0 (TID 32)
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000026_0
19/01/02 15:31:50 INFO Executor: Finished task 26.0 in stage 3.0 (TID 30). 3969 bytes result sent to driver
19/01/02 15:31:50 INFO Executor: Running task 27.0 in stage 3.0 (TID 31)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 31.0 in stage 3.0 (TID 33, localhost, executor driver, partition 31, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO Executor: Running task 31.0 in stage 3.0 (TID 33)
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000031_0
19/01/02 15:31:50 INFO Executor: Finished task 31.0 in stage 3.0 (TID 33). 3969 bytes result sent to driver
19/01/02 15:31:50 INFO TaskSetManager: Finished task 26.0 in stage 3.0 (TID 30) in 51 ms on localhost (executor driver) (12/200)
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000030_0
19/01/02 15:31:50 INFO Executor: Finished task 30.0 in stage 3.0 (TID 32). 4012 bytes result sent to driver
19/01/02 15:31:50 INFO TaskSetManager: Finished task 19.0 in stage 3.0 (TID 25) in 101 ms on localhost (executor driver) (13/200)
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO TaskSetManager: Finished task 20.0 in stage 3.0 (TID 26) in 92 ms on localhost (executor driver) (14/200)
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000027_0
19/01/02 15:31:50 INFO Executor: Finished task 27.0 in stage 3.0 (TID 31). 3969 bytes result sent to driver
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/02 15:31:50 INFO TaskSetManager: Finished task 15.0 in stage 3.0 (TID 23) in 131 ms on localhost (executor driver) (15/200)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 32.0 in stage 3.0 (TID 34, localhost, executor driver, partition 32, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 33.0 in stage 3.0 (TID 35, localhost, executor driver, partition 33, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 39.0 in stage 3.0 (TID 36, localhost, executor driver, partition 39, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO Executor: Running task 39.0 in stage 3.0 (TID 36)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 30.0 in stage 3.0 (TID 32) in 55 ms on localhost (executor driver) (16/200)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 27.0 in stage 3.0 (TID 31) in 56 ms on localhost (executor driver) (17/200)
19/01/02 15:31:50 INFO Executor: Running task 33.0 in stage 3.0 (TID 35)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 31.0 in stage 3.0 (TID 33) in 42 ms on localhost (executor driver) (18/200)
19/01/02 15:31:50 INFO Executor: Running task 32.0 in stage 3.0 (TID 34)
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000039_0
19/01/02 15:31:50 INFO Executor: Finished task 39.0 in stage 3.0 (TID 36). 3969 bytes result sent to driver
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000033_0
19/01/02 15:31:50 INFO Executor: Finished task 33.0 in stage 3.0 (TID 35). 4012 bytes result sent to driver
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000032_0
19/01/02 15:31:50 INFO Executor: Finished task 32.0 in stage 3.0 (TID 34). 3926 bytes result sent to driver
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000022_0
19/01/02 15:31:50 INFO Executor: Finished task 22.0 in stage 3.0 (TID 27). 3969 bytes result sent to driver
19/01/02 15:31:50 INFO TaskSetManager: Starting task 40.0 in stage 3.0 (TID 37, localhost, executor driver, partition 40, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 44.0 in stage 3.0 (TID 38, localhost, executor driver, partition 44, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 47.0 in stage 3.0 (TID 39, localhost, executor driver, partition 47, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 50.0 in stage 3.0 (TID 40, localhost, executor driver, partition 50, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO Executor: Running task 50.0 in stage 3.0 (TID 40)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 33.0 in stage 3.0 (TID 35) in 36 ms on localhost (executor driver) (19/200)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 32.0 in stage 3.0 (TID 34) in 36 ms on localhost (executor driver) (20/200)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 22.0 in stage 3.0 (TID 27) in 107 ms on localhost (executor driver) (21/200)
19/01/02 15:31:50 INFO Executor: Running task 47.0 in stage 3.0 (TID 39)
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000050_0
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO Executor: Finished task 50.0 in stage 3.0 (TID 40). 3969 bytes result sent to driver
19/01/02 15:31:50 INFO Executor: Running task 44.0 in stage 3.0 (TID 38)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 51.0 in stage 3.0 (TID 41, localhost, executor driver, partition 51, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 50.0 in stage 3.0 (TID 40) in 17 ms on localhost (executor driver) (22/200)
19/01/02 15:31:50 INFO Executor: Running task 51.0 in stage 3.0 (TID 41)
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000047_0
19/01/02 15:31:50 INFO Executor: Finished task 47.0 in stage 3.0 (TID 39). 4012 bytes result sent to driver
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO Executor: Running task 40.0 in stage 3.0 (TID 37)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 39.0 in stage 3.0 (TID 36) in 116 ms on localhost (executor driver) (23/200)
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000051_0
19/01/02 15:31:50 INFO Executor: Finished task 51.0 in stage 3.0 (TID 41). 4012 bytes result sent to driver
19/01/02 15:31:50 INFO TaskSetManager: Starting task 54.0 in stage 3.0 (TID 42, localhost, executor driver, partition 54, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO BlockManagerInfo: Removed broadcast_3_piece0 on localhost:58918 in memory (size: 15.8 KB, free: 1992.8 MB)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 47.0 in stage 3.0 (TID 39) in 88 ms on localhost (executor driver) (24/200)
19/01/02 15:31:50 INFO Executor: Running task 54.0 in stage 3.0 (TID 42)
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000044_0
19/01/02 15:31:50 INFO Executor: Finished task 44.0 in stage 3.0 (TID 38). 3969 bytes result sent to driver
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO TaskSetManager: Starting task 55.0 in stage 3.0 (TID 43, localhost, executor driver, partition 55, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 56.0 in stage 3.0 (TID 44, localhost, executor driver, partition 56, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO Executor: Running task 55.0 in stage 3.0 (TID 43)
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000054_0
19/01/02 15:31:50 INFO Executor: Finished task 54.0 in stage 3.0 (TID 42). 3926 bytes result sent to driver
19/01/02 15:31:50 INFO TaskSetManager: Starting task 57.0 in stage 3.0 (TID 45, localhost, executor driver, partition 57, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 44.0 in stage 3.0 (TID 38) in 108 ms on localhost (executor driver) (25/200)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 51.0 in stage 3.0 (TID 41) in 93 ms on localhost (executor driver) (26/200)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 54.0 in stage 3.0 (TID 42) in 21 ms on localhost (executor driver) (27/200)
19/01/02 15:31:50 INFO Executor: Running task 56.0 in stage 3.0 (TID 44)
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000055_0
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000040_0
19/01/02 15:31:50 INFO Executor: Finished task 55.0 in stage 3.0 (TID 43). 3969 bytes result sent to driver
19/01/02 15:31:50 INFO Executor: Finished task 40.0 in stage 3.0 (TID 37). 3926 bytes result sent to driver
19/01/02 15:31:50 INFO Executor: Running task 57.0 in stage 3.0 (TID 45)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 58.0 in stage 3.0 (TID 46, localhost, executor driver, partition 58, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 61.0 in stage 3.0 (TID 47, localhost, executor driver, partition 61, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO Executor: Running task 58.0 in stage 3.0 (TID 46)
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 6 ms
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000057_0
19/01/02 15:31:50 INFO Executor: Finished task 57.0 in stage 3.0 (TID 45). 3926 bytes result sent to driver
19/01/02 15:31:50 INFO TaskSetManager: Finished task 40.0 in stage 3.0 (TID 37) in 138 ms on localhost (executor driver) (28/200)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 55.0 in stage 3.0 (TID 43) in 36 ms on localhost (executor driver) (29/200)
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000058_0
19/01/02 15:31:50 INFO Executor: Finished task 58.0 in stage 3.0 (TID 46). 3969 bytes result sent to driver
19/01/02 15:31:50 INFO Executor: Running task 61.0 in stage 3.0 (TID 47)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 65.0 in stage 3.0 (TID 48, localhost, executor driver, partition 65, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 66.0 in stage 3.0 (TID 49, localhost, executor driver, partition 66, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 58.0 in stage 3.0 (TID 46) in 21 ms on localhost (executor driver) (30/200)
19/01/02 15:31:50 INFO Executor: Running task 66.0 in stage 3.0 (TID 49)
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000061_0
19/01/02 15:31:50 INFO Executor: Finished task 61.0 in stage 3.0 (TID 47). 3969 bytes result sent to driver
19/01/02 15:31:50 INFO TaskSetManager: Starting task 67.0 in stage 3.0 (TID 50, localhost, executor driver, partition 67, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 57.0 in stage 3.0 (TID 45) in 42 ms on localhost (executor driver) (31/200)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 61.0 in stage 3.0 (TID 47) in 33 ms on localhost (executor driver) (32/200)
19/01/02 15:31:50 INFO Executor: Running task 65.0 in stage 3.0 (TID 48)
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO Executor: Running task 67.0 in stage 3.0 (TID 50)
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000066_0
19/01/02 15:31:50 INFO Executor: Finished task 66.0 in stage 3.0 (TID 49). 3969 bytes result sent to driver
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000065_0
19/01/02 15:31:50 INFO Executor: Finished task 65.0 in stage 3.0 (TID 48). 3926 bytes result sent to driver
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000056_0
19/01/02 15:31:50 INFO Executor: Finished task 56.0 in stage 3.0 (TID 44). 3969 bytes result sent to driver
19/01/02 15:31:50 INFO TaskSetManager: Starting task 68.0 in stage 3.0 (TID 51, localhost, executor driver, partition 68, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO Executor: Running task 68.0 in stage 3.0 (TID 51)
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO TaskSetManager: Starting task 71.0 in stage 3.0 (TID 52, localhost, executor driver, partition 71, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO TaskSetManager: Starting task 73.0 in stage 3.0 (TID 53, localhost, executor driver, partition 73, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:50 INFO Executor: Running task 71.0 in stage 3.0 (TID 52)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 65.0 in stage 3.0 (TID 48) in 39 ms on localhost (executor driver) (33/200)
19/01/02 15:31:50 INFO TaskSetManager: Finished task 56.0 in stage 3.0 (TID 44) in 76 ms on localhost (executor driver) (34/200)
19/01/02 15:31:50 INFO Executor: Running task 73.0 in stage 3.0 (TID 53)
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:50 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153150_0003_m_000071_0
19/01/02 15:31:50 INFO TaskSetManager: Finished task 66.0 in stage 3.0 (TID 49) in 51 ms on localhost (executor driver) (35/200)
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000073_0
19/01/02 15:31:51 INFO Executor: Finished task 73.0 in stage 3.0 (TID 53). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000068_0
19/01/02 15:31:51 INFO Executor: Finished task 68.0 in stage 3.0 (TID 51). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO Executor: Finished task 71.0 in stage 3.0 (TID 52). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Starting task 74.0 in stage 3.0 (TID 54, localhost, executor driver, partition 74, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 75.0 in stage 3.0 (TID 55, localhost, executor driver, partition 75, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 76.0 in stage 3.0 (TID 56, localhost, executor driver, partition 76, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 75.0 in stage 3.0 (TID 55)
19/01/02 15:31:51 INFO Executor: Running task 74.0 in stage 3.0 (TID 54)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 73.0 in stage 3.0 (TID 53) in 27 ms on localhost (executor driver) (36/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 68.0 in stage 3.0 (TID 51) in 38 ms on localhost (executor driver) (37/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 71.0 in stage 3.0 (TID 52) in 37 ms on localhost (executor driver) (38/200)
19/01/02 15:31:51 INFO Executor: Running task 76.0 in stage 3.0 (TID 56)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000074_0
19/01/02 15:31:51 INFO Executor: Finished task 74.0 in stage 3.0 (TID 54). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Starting task 82.0 in stage 3.0 (TID 57, localhost, executor driver, partition 82, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 82.0 in stage 3.0 (TID 57)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000082_0
19/01/02 15:31:51 INFO Executor: Finished task 82.0 in stage 3.0 (TID 57). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO TaskSetManager: Starting task 83.0 in stage 3.0 (TID 58, localhost, executor driver, partition 83, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 83.0 in stage 3.0 (TID 58)
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000076_0
19/01/02 15:31:51 INFO Executor: Finished task 76.0 in stage 3.0 (TID 56). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Finished task 74.0 in stage 3.0 (TID 54) in 39 ms on localhost (executor driver) (39/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 82.0 in stage 3.0 (TID 57) in 25 ms on localhost (executor driver) (40/200)
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000067_0
19/01/02 15:31:51 INFO Executor: Finished task 67.0 in stage 3.0 (TID 50). 4012 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Starting task 85.0 in stage 3.0 (TID 59, localhost, executor driver, partition 85, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000083_0
19/01/02 15:31:51 INFO Executor: Finished task 83.0 in stage 3.0 (TID 58). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO Executor: Running task 85.0 in stage 3.0 (TID 59)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 89.0 in stage 3.0 (TID 60, localhost, executor driver, partition 89, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 91.0 in stage 3.0 (TID 61, localhost, executor driver, partition 91, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 76.0 in stage 3.0 (TID 56) in 50 ms on localhost (executor driver) (41/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 67.0 in stage 3.0 (TID 50) in 99 ms on localhost (executor driver) (42/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 83.0 in stage 3.0 (TID 58) in 16 ms on localhost (executor driver) (43/200)
19/01/02 15:31:51 INFO Executor: Running task 89.0 in stage 3.0 (TID 60)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000089_0
19/01/02 15:31:51 INFO Executor: Finished task 89.0 in stage 3.0 (TID 60). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 29 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000085_0
19/01/02 15:31:51 INFO Executor: Finished task 85.0 in stage 3.0 (TID 59). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Starting task 92.0 in stage 3.0 (TID 62, localhost, executor driver, partition 92, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 93.0 in stage 3.0 (TID 63, localhost, executor driver, partition 93, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 89.0 in stage 3.0 (TID 60) in 21 ms on localhost (executor driver) (44/200)
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000075_0
19/01/02 15:31:51 INFO TaskSetManager: Finished task 85.0 in stage 3.0 (TID 59) in 23 ms on localhost (executor driver) (45/200)
19/01/02 15:31:51 INFO Executor: Finished task 75.0 in stage 3.0 (TID 55). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO Executor: Running task 91.0 in stage 3.0 (TID 61)
19/01/02 15:31:51 INFO Executor: Running task 92.0 in stage 3.0 (TID 62)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000092_0
19/01/02 15:31:51 INFO Executor: Finished task 92.0 in stage 3.0 (TID 62). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO TaskSetManager: Starting task 95.0 in stage 3.0 (TID 64, localhost, executor driver, partition 95, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 95.0 in stage 3.0 (TID 64)
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000091_0
19/01/02 15:31:51 INFO Executor: Finished task 91.0 in stage 3.0 (TID 61). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO Executor: Running task 93.0 in stage 3.0 (TID 63)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000093_0
19/01/02 15:31:51 INFO Executor: Finished task 93.0 in stage 3.0 (TID 63). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000095_0
19/01/02 15:31:51 INFO Executor: Finished task 95.0 in stage 3.0 (TID 64). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Finished task 75.0 in stage 3.0 (TID 55) in 104 ms on localhost (executor driver) (46/200)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 97.0 in stage 3.0 (TID 65, localhost, executor driver, partition 97, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 97.0 in stage 3.0 (TID 65)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 99.0 in stage 3.0 (TID 66, localhost, executor driver, partition 99, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 103.0 in stage 3.0 (TID 67, localhost, executor driver, partition 103, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 104.0 in stage 3.0 (TID 68, localhost, executor driver, partition 104, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 92.0 in stage 3.0 (TID 62) in 46 ms on localhost (executor driver) (47/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 91.0 in stage 3.0 (TID 61) in 62 ms on localhost (executor driver) (48/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 93.0 in stage 3.0 (TID 63) in 46 ms on localhost (executor driver) (49/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 95.0 in stage 3.0 (TID 64) in 28 ms on localhost (executor driver) (50/200)
19/01/02 15:31:51 INFO Executor: Running task 103.0 in stage 3.0 (TID 67)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO Executor: Running task 99.0 in stage 3.0 (TID 66)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000099_0
19/01/02 15:31:51 INFO Executor: Finished task 99.0 in stage 3.0 (TID 66). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Starting task 105.0 in stage 3.0 (TID 69, localhost, executor driver, partition 105, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 105.0 in stage 3.0 (TID 69)
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000103_0
19/01/02 15:31:51 INFO Executor: Finished task 103.0 in stage 3.0 (TID 67). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000097_0
19/01/02 15:31:51 INFO Executor: Finished task 97.0 in stage 3.0 (TID 65). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO Executor: Running task 104.0 in stage 3.0 (TID 68)
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000105_0
19/01/02 15:31:51 INFO Executor: Finished task 105.0 in stage 3.0 (TID 69). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Finished task 99.0 in stage 3.0 (TID 66) in 35 ms on localhost (executor driver) (51/200)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO TaskSetManager: Starting task 107.0 in stage 3.0 (TID 70, localhost, executor driver, partition 107, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000104_0
19/01/02 15:31:51 INFO TaskSetManager: Starting task 108.0 in stage 3.0 (TID 71, localhost, executor driver, partition 108, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 109.0 in stage 3.0 (TID 72, localhost, executor driver, partition 109, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Finished task 104.0 in stage 3.0 (TID 68). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO Executor: Running task 108.0 in stage 3.0 (TID 71)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 110.0 in stage 3.0 (TID 73, localhost, executor driver, partition 110, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 97.0 in stage 3.0 (TID 65) in 45 ms on localhost (executor driver) (52/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 105.0 in stage 3.0 (TID 69) in 21 ms on localhost (executor driver) (53/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 104.0 in stage 3.0 (TID 68) in 43 ms on localhost (executor driver) (54/200)
19/01/02 15:31:51 INFO Executor: Running task 107.0 in stage 3.0 (TID 70)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 103.0 in stage 3.0 (TID 67) in 46 ms on localhost (executor driver) (55/200)
19/01/02 15:31:51 INFO Executor: Running task 109.0 in stage 3.0 (TID 72)
19/01/02 15:31:51 INFO Executor: Running task 110.0 in stage 3.0 (TID 73)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000107_0
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000110_0
19/01/02 15:31:51 INFO Executor: Finished task 107.0 in stage 3.0 (TID 70). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO Executor: Finished task 110.0 in stage 3.0 (TID 73). 4012 bytes result sent to driver
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO TaskSetManager: Starting task 111.0 in stage 3.0 (TID 74, localhost, executor driver, partition 111, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 113.0 in stage 3.0 (TID 75, localhost, executor driver, partition 113, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 111.0 in stage 3.0 (TID 74)
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000109_0
19/01/02 15:31:51 INFO Executor: Finished task 109.0 in stage 3.0 (TID 72). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Finished task 107.0 in stage 3.0 (TID 70) in 30 ms on localhost (executor driver) (56/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 110.0 in stage 3.0 (TID 73) in 23 ms on localhost (executor driver) (57/200)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000111_0
19/01/02 15:31:51 INFO Executor: Finished task 111.0 in stage 3.0 (TID 74). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000108_0
19/01/02 15:31:51 INFO Executor: Running task 113.0 in stage 3.0 (TID 75)
19/01/02 15:31:51 INFO Executor: Finished task 108.0 in stage 3.0 (TID 71). 4012 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Starting task 114.0 in stage 3.0 (TID 76, localhost, executor driver, partition 114, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 116.0 in stage 3.0 (TID 77, localhost, executor driver, partition 116, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 119.0 in stage 3.0 (TID 78, localhost, executor driver, partition 119, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 114.0 in stage 3.0 (TID 76)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000113_0
19/01/02 15:31:51 INFO Executor: Finished task 113.0 in stage 3.0 (TID 75). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000114_0
19/01/02 15:31:51 INFO Executor: Running task 119.0 in stage 3.0 (TID 78)
19/01/02 15:31:51 INFO Executor: Finished task 114.0 in stage 3.0 (TID 76). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO Executor: Running task 116.0 in stage 3.0 (TID 77)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 126.0 in stage 3.0 (TID 79, localhost, executor driver, partition 126, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 127.0 in stage 3.0 (TID 80, localhost, executor driver, partition 127, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 126.0 in stage 3.0 (TID 79)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000126_0
19/01/02 15:31:51 INFO Executor: Finished task 126.0 in stage 3.0 (TID 79). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000119_0
19/01/02 15:31:51 INFO Executor: Finished task 119.0 in stage 3.0 (TID 78). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000116_0
19/01/02 15:31:51 INFO Executor: Finished task 116.0 in stage 3.0 (TID 77). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Finished task 109.0 in stage 3.0 (TID 72) in 74 ms on localhost (executor driver) (58/200)
19/01/02 15:31:51 INFO Executor: Running task 127.0 in stage 3.0 (TID 80)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 111.0 in stage 3.0 (TID 74) in 58 ms on localhost (executor driver) (59/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 108.0 in stage 3.0 (TID 71) in 75 ms on localhost (executor driver) (60/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 113.0 in stage 3.0 (TID 75) in 58 ms on localhost (executor driver) (61/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 114.0 in stage 3.0 (TID 76) in 38 ms on localhost (executor driver) (62/200)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 129.0 in stage 3.0 (TID 81, localhost, executor driver, partition 129, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 130.0 in stage 3.0 (TID 82, localhost, executor driver, partition 130, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 131.0 in stage 3.0 (TID 83, localhost, executor driver, partition 131, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 131.0 in stage 3.0 (TID 83)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO Executor: Running task 129.0 in stage 3.0 (TID 81)
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000131_0
19/01/02 15:31:51 INFO Executor: Finished task 131.0 in stage 3.0 (TID 83). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Starting task 132.0 in stage 3.0 (TID 84, localhost, executor driver, partition 132, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 132.0 in stage 3.0 (TID 84)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000129_0
19/01/02 15:31:51 INFO Executor: Finished task 129.0 in stage 3.0 (TID 81). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000127_0
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000132_0
19/01/02 15:31:51 INFO Executor: Finished task 132.0 in stage 3.0 (TID 84). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO Executor: Finished task 127.0 in stage 3.0 (TID 80). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Starting task 133.0 in stage 3.0 (TID 85, localhost, executor driver, partition 133, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 135.0 in stage 3.0 (TID 86, localhost, executor driver, partition 135, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 136.0 in stage 3.0 (TID 87, localhost, executor driver, partition 136, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 136.0 in stage 3.0 (TID 87)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 126.0 in stage 3.0 (TID 79) in 45 ms on localhost (executor driver) (63/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 119.0 in stage 3.0 (TID 78) in 61 ms on localhost (executor driver) (64/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 116.0 in stage 3.0 (TID 77) in 61 ms on localhost (executor driver) (65/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 131.0 in stage 3.0 (TID 83) in 23 ms on localhost (executor driver) (66/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 129.0 in stage 3.0 (TID 81) in 25 ms on localhost (executor driver) (67/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 132.0 in stage 3.0 (TID 84) in 14 ms on localhost (executor driver) (68/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 127.0 in stage 3.0 (TID 80) in 46 ms on localhost (executor driver) (69/200)
19/01/02 15:31:51 INFO Executor: Running task 130.0 in stage 3.0 (TID 82)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000136_0
19/01/02 15:31:51 INFO Executor: Finished task 136.0 in stage 3.0 (TID 87). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO Executor: Running task 135.0 in stage 3.0 (TID 86)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000130_0
19/01/02 15:31:51 INFO Executor: Finished task 130.0 in stage 3.0 (TID 82). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO TaskSetManager: Starting task 137.0 in stage 3.0 (TID 88, localhost, executor driver, partition 137, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 138.0 in stage 3.0 (TID 89, localhost, executor driver, partition 138, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 137.0 in stage 3.0 (TID 88)
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000135_0
19/01/02 15:31:51 INFO Executor: Finished task 135.0 in stage 3.0 (TID 86). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO Executor: Running task 138.0 in stage 3.0 (TID 89)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000138_0
19/01/02 15:31:51 INFO Executor: Finished task 138.0 in stage 3.0 (TID 89). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO Executor: Running task 133.0 in stage 3.0 (TID 85)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 136.0 in stage 3.0 (TID 87) in 33 ms on localhost (executor driver) (70/200)
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO TaskSetManager: Finished task 130.0 in stage 3.0 (TID 82) in 54 ms on localhost (executor driver) (71/200)
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000137_0
19/01/02 15:31:51 INFO TaskSetManager: Starting task 139.0 in stage 3.0 (TID 90, localhost, executor driver, partition 139, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 140.0 in stage 3.0 (TID 91, localhost, executor driver, partition 140, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 140.0 in stage 3.0 (TID 91)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 135.0 in stage 3.0 (TID 86) in 34 ms on localhost (executor driver) (72/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 138.0 in stage 3.0 (TID 89) in 17 ms on localhost (executor driver) (73/200)
19/01/02 15:31:51 INFO Executor: Finished task 137.0 in stage 3.0 (TID 88). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Starting task 141.0 in stage 3.0 (TID 92, localhost, executor driver, partition 141, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 141.0 in stage 3.0 (TID 92)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO Executor: Running task 139.0 in stage 3.0 (TID 90)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO TaskSetManager: Finished task 137.0 in stage 3.0 (TID 88) in 27 ms on localhost (executor driver) (74/200)
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000140_0
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO Executor: Finished task 140.0 in stage 3.0 (TID 91). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000141_0
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO TaskSetManager: Starting task 143.0 in stage 3.0 (TID 93, localhost, executor driver, partition 143, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 140.0 in stage 3.0 (TID 91) in 18 ms on localhost (executor driver) (75/200)
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000133_0
19/01/02 15:31:51 INFO Executor: Running task 143.0 in stage 3.0 (TID 93)
19/01/02 15:31:51 INFO Executor: Finished task 141.0 in stage 3.0 (TID 92). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Starting task 144.0 in stage 3.0 (TID 94, localhost, executor driver, partition 144, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 144.0 in stage 3.0 (TID 94)
19/01/02 15:31:51 INFO Executor: Finished task 133.0 in stage 3.0 (TID 85). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000143_0
19/01/02 15:31:51 INFO Executor: Finished task 143.0 in stage 3.0 (TID 93). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000144_0
19/01/02 15:31:51 INFO Executor: Finished task 144.0 in stage 3.0 (TID 94). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000139_0
19/01/02 15:31:51 INFO Executor: Finished task 139.0 in stage 3.0 (TID 90). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Finished task 141.0 in stage 3.0 (TID 92) in 39 ms on localhost (executor driver) (76/200)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 150.0 in stage 3.0 (TID 95, localhost, executor driver, partition 150, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 151.0 in stage 3.0 (TID 96, localhost, executor driver, partition 151, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 151.0 in stage 3.0 (TID 96)
19/01/02 15:31:51 INFO Executor: Running task 150.0 in stage 3.0 (TID 95)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/02 15:31:51 INFO TaskSetManager: Finished task 133.0 in stage 3.0 (TID 85) in 88 ms on localhost (executor driver) (77/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 143.0 in stage 3.0 (TID 93) in 38 ms on localhost (executor driver) (78/200)
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000150_0
19/01/02 15:31:51 INFO Executor: Finished task 150.0 in stage 3.0 (TID 95). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000151_0
19/01/02 15:31:51 INFO Executor: Finished task 151.0 in stage 3.0 (TID 96). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Finished task 144.0 in stage 3.0 (TID 94) in 42 ms on localhost (executor driver) (79/200)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 154.0 in stage 3.0 (TID 97, localhost, executor driver, partition 154, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 154.0 in stage 3.0 (TID 97)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 155.0 in stage 3.0 (TID 98, localhost, executor driver, partition 155, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 158.0 in stage 3.0 (TID 99, localhost, executor driver, partition 158, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 158.0 in stage 3.0 (TID 99)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 139.0 in stage 3.0 (TID 90) in 72 ms on localhost (executor driver) (80/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 150.0 in stage 3.0 (TID 95) in 29 ms on localhost (executor driver) (81/200)
19/01/02 15:31:51 INFO Executor: Running task 155.0 in stage 3.0 (TID 98)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 160.0 in stage 3.0 (TID 100, localhost, executor driver, partition 160, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 151.0 in stage 3.0 (TID 96) in 33 ms on localhost (executor driver) (82/200)
19/01/02 15:31:51 INFO Executor: Running task 160.0 in stage 3.0 (TID 100)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000160_0
19/01/02 15:31:51 INFO Executor: Finished task 160.0 in stage 3.0 (TID 100). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 17 ms
19/01/02 15:31:51 INFO TaskSetManager: Starting task 162.0 in stage 3.0 (TID 101, localhost, executor driver, partition 162, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 162.0 in stage 3.0 (TID 101)
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000155_0
19/01/02 15:31:51 INFO Executor: Finished task 155.0 in stage 3.0 (TID 98). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000158_0
19/01/02 15:31:51 INFO TaskSetManager: Starting task 163.0 in stage 3.0 (TID 102, localhost, executor driver, partition 163, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 163.0 in stage 3.0 (TID 102)
19/01/02 15:31:51 INFO Executor: Finished task 158.0 in stage 3.0 (TID 99). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000162_0
19/01/02 15:31:51 INFO Executor: Finished task 162.0 in stage 3.0 (TID 101). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Finished task 160.0 in stage 3.0 (TID 100) in 49 ms on localhost (executor driver) (83/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 155.0 in stage 3.0 (TID 98) in 55 ms on localhost (executor driver) (84/200)
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000154_0
19/01/02 15:31:51 INFO Executor: Finished task 154.0 in stage 3.0 (TID 97). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000163_0
19/01/02 15:31:51 INFO TaskSetManager: Starting task 164.0 in stage 3.0 (TID 103, localhost, executor driver, partition 164, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Finished task 163.0 in stage 3.0 (TID 102). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Starting task 165.0 in stage 3.0 (TID 104, localhost, executor driver, partition 165, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 168.0 in stage 3.0 (TID 105, localhost, executor driver, partition 168, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 170.0 in stage 3.0 (TID 106, localhost, executor driver, partition 170, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 170.0 in stage 3.0 (TID 106)
19/01/02 15:31:51 INFO Executor: Running task 164.0 in stage 3.0 (TID 103)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 158.0 in stage 3.0 (TID 99) in 68 ms on localhost (executor driver) (85/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 162.0 in stage 3.0 (TID 101) in 42 ms on localhost (executor driver) (86/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 154.0 in stage 3.0 (TID 97) in 70 ms on localhost (executor driver) (87/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 163.0 in stage 3.0 (TID 102) in 40 ms on localhost (executor driver) (88/200)
19/01/02 15:31:51 INFO Executor: Running task 168.0 in stage 3.0 (TID 105)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000164_0
19/01/02 15:31:51 INFO Executor: Finished task 164.0 in stage 3.0 (TID 103). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000168_0
19/01/02 15:31:51 INFO TaskSetManager: Starting task 171.0 in stage 3.0 (TID 107, localhost, executor driver, partition 171, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Finished task 168.0 in stage 3.0 (TID 105). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO Executor: Running task 171.0 in stage 3.0 (TID 107)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO Executor: Running task 165.0 in stage 3.0 (TID 104)
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000170_0
19/01/02 15:31:51 INFO Executor: Finished task 170.0 in stage 3.0 (TID 106). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Starting task 173.0 in stage 3.0 (TID 108, localhost, executor driver, partition 173, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 174.0 in stage 3.0 (TID 109, localhost, executor driver, partition 174, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 174.0 in stage 3.0 (TID 109)
19/01/02 15:31:51 INFO Executor: Running task 173.0 in stage 3.0 (TID 108)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000171_0
19/01/02 15:31:51 INFO Executor: Finished task 171.0 in stage 3.0 (TID 107). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Finished task 164.0 in stage 3.0 (TID 103) in 42 ms on localhost (executor driver) (89/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 168.0 in stage 3.0 (TID 105) in 41 ms on localhost (executor driver) (90/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 170.0 in stage 3.0 (TID 106) in 41 ms on localhost (executor driver) (91/200)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 176.0 in stage 3.0 (TID 110, localhost, executor driver, partition 176, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000165_0
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/02 15:31:51 INFO Executor: Finished task 165.0 in stage 3.0 (TID 104). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000173_0
19/01/02 15:31:51 INFO Executor: Finished task 173.0 in stage 3.0 (TID 108). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Finished task 171.0 in stage 3.0 (TID 107) in 36 ms on localhost (executor driver) (92/200)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000174_0
19/01/02 15:31:51 INFO Executor: Finished task 174.0 in stage 3.0 (TID 109). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Starting task 177.0 in stage 3.0 (TID 111, localhost, executor driver, partition 177, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 178.0 in stage 3.0 (TID 112, localhost, executor driver, partition 178, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 181.0 in stage 3.0 (TID 113, localhost, executor driver, partition 181, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 176.0 in stage 3.0 (TID 110)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 165.0 in stage 3.0 (TID 104) in 69 ms on localhost (executor driver) (93/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 173.0 in stage 3.0 (TID 108) in 45 ms on localhost (executor driver) (94/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 174.0 in stage 3.0 (TID 109) in 44 ms on localhost (executor driver) (95/200)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/02 15:31:51 INFO Executor: Running task 177.0 in stage 3.0 (TID 111)
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000176_0
19/01/02 15:31:51 INFO Executor: Finished task 176.0 in stage 3.0 (TID 110). 4012 bytes result sent to driver
19/01/02 15:31:51 INFO Executor: Running task 178.0 in stage 3.0 (TID 112)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000177_0
19/01/02 15:31:51 INFO Executor: Finished task 177.0 in stage 3.0 (TID 111). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO Executor: Running task 181.0 in stage 3.0 (TID 113)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000178_0
19/01/02 15:31:51 INFO Executor: Finished task 178.0 in stage 3.0 (TID 112). 4012 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Starting task 182.0 in stage 3.0 (TID 114, localhost, executor driver, partition 182, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 183.0 in stage 3.0 (TID 115, localhost, executor driver, partition 183, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 184.0 in stage 3.0 (TID 116, localhost, executor driver, partition 184, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 183.0 in stage 3.0 (TID 115)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 176.0 in stage 3.0 (TID 110) in 128 ms on localhost (executor driver) (96/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 178.0 in stage 3.0 (TID 112) in 107 ms on localhost (executor driver) (97/200)
19/01/02 15:31:51 INFO Executor: Running task 184.0 in stage 3.0 (TID 116)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000183_0
19/01/02 15:31:51 INFO Executor: Finished task 183.0 in stage 3.0 (TID 115). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO Executor: Running task 182.0 in stage 3.0 (TID 114)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 177.0 in stage 3.0 (TID 111) in 117 ms on localhost (executor driver) (98/200)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000184_0
19/01/02 15:31:51 INFO Executor: Finished task 184.0 in stage 3.0 (TID 116). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000181_0
19/01/02 15:31:51 INFO Executor: Finished task 181.0 in stage 3.0 (TID 113). 4012 bytes result sent to driver
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000182_0
19/01/02 15:31:51 INFO TaskSetManager: Starting task 185.0 in stage 3.0 (TID 117, localhost, executor driver, partition 185, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 187.0 in stage 3.0 (TID 118, localhost, executor driver, partition 187, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 188.0 in stage 3.0 (TID 119, localhost, executor driver, partition 188, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 183.0 in stage 3.0 (TID 115) in 30 ms on localhost (executor driver) (99/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 184.0 in stage 3.0 (TID 116) in 30 ms on localhost (executor driver) (100/200)
19/01/02 15:31:51 INFO Executor: Finished task 182.0 in stage 3.0 (TID 114). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO Executor: Running task 188.0 in stage 3.0 (TID 119)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 192.0 in stage 3.0 (TID 120, localhost, executor driver, partition 192, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 181.0 in stage 3.0 (TID 113) in 135 ms on localhost (executor driver) (101/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 182.0 in stage 3.0 (TID 114) in 31 ms on localhost (executor driver) (102/200)
19/01/02 15:31:51 INFO Executor: Running task 192.0 in stage 3.0 (TID 120)
19/01/02 15:31:51 INFO Executor: Running task 187.0 in stage 3.0 (TID 118)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/02 15:31:51 INFO Executor: Running task 185.0 in stage 3.0 (TID 117)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000188_0
19/01/02 15:31:51 INFO Executor: Finished task 188.0 in stage 3.0 (TID 119). 4012 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Starting task 196.0 in stage 3.0 (TID 121, localhost, executor driver, partition 196, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 196.0 in stage 3.0 (TID 121)
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000187_0
19/01/02 15:31:51 INFO Executor: Finished task 187.0 in stage 3.0 (TID 118). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Finished task 188.0 in stage 3.0 (TID 119) in 15 ms on localhost (executor driver) (103/200)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000196_0
19/01/02 15:31:51 INFO Executor: Finished task 196.0 in stage 3.0 (TID 121). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000185_0
19/01/02 15:31:51 INFO Executor: Finished task 185.0 in stage 3.0 (TID 117). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Starting task 197.0 in stage 3.0 (TID 122, localhost, executor driver, partition 197, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 198.0 in stage 3.0 (TID 123, localhost, executor driver, partition 198, PROCESS_LOCAL, 7754 bytes)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 0.0 in stage 3.0 (TID 124, localhost, executor driver, partition 0, ANY, 7754 bytes)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/02 15:31:51 INFO Executor: Running task 198.0 in stage 3.0 (TID 123)
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000192_0
19/01/02 15:31:51 INFO Executor: Finished task 192.0 in stage 3.0 (TID 120). 3969 bytes result sent to driver
19/01/02 15:31:51 INFO TaskSetManager: Finished task 187.0 in stage 3.0 (TID 118) in 32 ms on localhost (executor driver) (104/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 196.0 in stage 3.0 (TID 121) in 20 ms on localhost (executor driver) (105/200)
19/01/02 15:31:51 INFO TaskSetManager: Finished task 185.0 in stage 3.0 (TID 117) in 33 ms on localhost (executor driver) (106/200)
19/01/02 15:31:51 INFO TaskSetManager: Starting task 2.0 in stage 3.0 (TID 125, localhost, executor driver, partition 2, ANY, 7754 bytes)
19/01/02 15:31:51 INFO Executor: Running task 2.0 in stage 3.0 (TID 125)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000198_0
19/01/02 15:31:51 INFO Executor: Finished task 198.0 in stage 3.0 (TID 123). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO Executor: Running task 197.0 in stage 3.0 (TID 122)
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 4 blocks
19/01/02 15:31:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SparkHadoopMapRedUtil: No need to commit output of task because needsTaskCommit=false: attempt_20190102153151_0003_m_000197_0
19/01/02 15:31:51 INFO Executor: Finished task 197.0 in stage 3.0 (TID 122). 3926 bytes result sent to driver
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using user defined output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO SQLHadoopMapReduceCommitProtocol: Using output committer class org.apache.parquet.hadoop.ParquetOutputCommitter
19/01/02 15:31:51 INFO CodeGenerator: Code generated in 5.345973 ms
19/01/02 15:31:51 INFO CodeGenerator: Code generated in 5.500886 ms
19/01/02 15:31:51 INFO CodeGenerator: Code generated in 11.015885 ms
19/01/02 15:31:51 INFO Executor: Running task 0.0 in stage 3.0 (TID 124)
19/01/02 15:31:51 INFO CodecConfig: Compression: SNAPPY
19/01/02 15:31:51 INFO CodecConfig: Compression: SNAPPY
19/01/02 15:31:51 INFO ParquetOutputFormat: Parquet block size to 134217728
19/01/02 15:31:51 INFO ParquetOutputFormat: Parquet page size to 1048576
19/01/02 15:31:51 INFO ParquetOutputFormat: Parquet dictionary page size to 1048576
19/01/02 15:31:51 INFO ParquetOutputFormat: Dictionary is on
19/01/02 15:31:51 INFO ParquetOutputFormat: Validation is off
19/01/02 15:31:51 INFO ParquetOutputFormat: Writer version is: PARQUET_1_0
19/01/02 15:31:51 INFO ParquetOutputFormat: Maximum row group padding size is 0 bytes
19/01/02 15:31:51 INFO ParquetOutputFormat: Page size checking is: estimated
19/01/02 15:31:51 INFO ParquetOutputFormat: Min row count for page size check is: 100
19/01/02 15:31:51 INFO ParquetOutputFormat: Max row count for page size check is: 10000
19/01/02 15:31:51 INFO ParquetWriteSupport: Initialized Parquet WriteSupport with Catalyst schema:
{
  "type" : "struct",
  "fields" : [ {
    "name" : "tag",
    "type" : "string",
    "nullable" : true,
    "metadata" : { }
  }, {
    "name" : "count",
    "type" : "long",
    "nullable" : false,
    "metadata" : { }
  } ]
}
and corresponding Parquet message type:
message spark_schema {
  optional binary tag (UTF8);
  required int64 count;
}

...
19/01/02 15:32:52 INFO InternalParquetRecordWriter: Flushing mem columnStore to file. allocated memory: 29
19/01/02 15:32:52 INFO FileOutputCommitter: Saved output of task 'attempt_20190102153248_0003_m_000190_0' to file:/C://kafka-tweets/testDir/_temporary/0/task_20190102153248_0003_m_000190
19/01/02 15:32:52 INFO SparkHadoopMapRedUtil: attempt_20190102153248_0003_m_000190_0: Committed
19/01/02 15:32:52 INFO Executor: Finished task 190.0 in stage 3.0 (TID 209). 4143 bytes result sent to driver
19/01/02 15:32:52 INFO TaskSetManager: Finished task 190.0 in stage 3.0 (TID 209) in 4425 ms on localhost (executor driver) (197/200)
19/01/02 15:32:52 INFO InternalParquetRecordWriter: Flushing mem columnStore to file. allocated memory: 32
19/01/02 15:32:52 INFO FileOutputCommitter: Saved output of task 'attempt_20190102153250_0003_m_000194_0' to file:/C://kafka-tweets/testDir/_temporary/0/task_20190102153250_0003_m_000194
19/01/02 15:32:52 INFO SparkHadoopMapRedUtil: attempt_20190102153250_0003_m_000194_0: Committed
19/01/02 15:32:52 INFO Executor: Finished task 194.0 in stage 3.0 (TID 212). 4143 bytes result sent to driver
19/01/02 15:32:52 INFO TaskSetManager: Finished task 194.0 in stage 3.0 (TID 212) in 2335 ms on localhost (executor driver) (198/200)
19/01/02 15:32:54 INFO InternalParquetRecordWriter: Flushing mem columnStore to file. allocated memory: 34
19/01/02 15:32:54 INFO FileOutputCommitter: Saved output of task 'attempt_20190102153251_0003_m_000199_0' to file:/C://kafka-tweets/testDir/_temporary/0/task_20190102153251_0003_m_000199
19/01/02 15:32:54 INFO SparkHadoopMapRedUtil: attempt_20190102153251_0003_m_000199_0: Committed
19/01/02 15:32:54 INFO Executor: Finished task 199.0 in stage 3.0 (TID 214). 4143 bytes result sent to driver
19/01/02 15:32:54 INFO TaskSetManager: Finished task 199.0 in stage 3.0 (TID 214) in 2044 ms on localhost (executor driver) (199/200)
19/01/02 15:32:54 INFO InternalParquetRecordWriter: Flushing mem columnStore to file. allocated memory: 27
19/01/02 15:32:54 INFO FileOutputCommitter: Saved output of task 'attempt_20190102153251_0003_m_000195_0' to file:/C://kafka-tweets/testDir/_temporary/0/task_20190102153251_0003_m_000195
19/01/02 15:32:54 INFO SparkHadoopMapRedUtil: attempt_20190102153251_0003_m_000195_0: Committed
19/01/02 15:32:54 INFO Executor: Finished task 195.0 in stage 3.0 (TID 213). 4186 bytes result sent to driver
19/01/02 15:32:54 INFO TaskSetManager: Finished task 195.0 in stage 3.0 (TID 213) in 2519 ms on localhost (executor driver) (200/200)
19/01/02 15:32:54 INFO TaskSchedulerImpl: Removed TaskSet 3.0, whose tasks have all completed, from pool
19/01/02 15:32:54 INFO DAGScheduler: ResultStage 3 (parquet at Main.scala:64) finished in 63.754 s
19/01/02 15:32:54 INFO DAGScheduler: Job 2 finished: parquet at Main.scala:64, took 64.850589 s
19/01/02 15:32:56 INFO FileFormatWriter: Job null committed.
19/01/02 15:32:56 INFO FileFormatWriter: Finished processing stats for job null.
19/01/02 15:37:56 INFO SparkUI: Stopped Spark web UI at http://localhost:4040
19/01/02 15:37:56 INFO MapOutputTrackerMasterEndpoint: MapOutputTrackerMasterEndpoint stopped!
19/01/02 15:37:56 ERROR DiskBlockManager: Exception while deleting local spark dir: C:\Users\Artsiom_Chuiko\AppData\Local\Temp\blockmgr-94fbc450-7ad4-42a6-8f36-f2eb6ff11be2
19/01/02 15:37:56 INFO MemoryStore: MemoryStore cleared
19/01/02 15:37:56 INFO BlockManager: BlockManager stopped
19/01/02 15:37:56 INFO BlockManagerMaster: BlockManagerMaster stopped
19/01/02 15:37:56 INFO OutputCommitCoordinator$OutputCommitCoordinatorEndpoint: OutputCommitCoordinator stopped!
19/01/02 15:37:56 INFO SparkContext: Successfully stopped SparkContext
19/01/02 15:37:56 INFO ShutdownHookManager: Shutdown hook called
19/01/02 15:37:56 INFO ShutdownHookManager: Deleting directory C:\Users\Artsiom_Chuiko\AppData\Local\Temp\spark-0add60bc-4630-4781-a137-121fcb14bfbe
```

</p>
</details>

Here are some screenshots of Spark UI, DAG Stages and results in HDFS

![Spark UI](./screenshots/sparkui.png "Spark UI")

![Job 0 Stage 0](./screenshots/Job0Stage0.png "Job 0 Stage 0")

![Job 1 Stage 1](./screenshots/Job1Stage1.png "Job 1 Stage 1")

![Job 2 Stages 2 and 3](./screenshots/Job2Stage2-3.png "Job 2 Stages 2 and 3")

Kafka's Topic data:
![Kafka's Topic data](./screenshots/topicData.png "Kafka's Topic data")

Counts partitioned by date and hour:
![Сounts](./screenshots/countsDate.png "Сounts")

![Сounts](./screenshots/countsHour.png "Сounts")

![Сounts](./screenshots/counts15.png "Сounts")

# Spark streaming

Create a topic in Kafka to store hashtag counts:
```shell
../kafka_2.12-1.0.0/bin/kafka-topics.sh --create --zookeeper 192.168.99.100:2181 --replication-factor 1 --partitions 1 --topic HashCounts
Created topic "HashCounts".
```

Run the streaming job:
```shell
spark-submit \
    --class by.artsiom.bigdata201.streaming.Main \
    --master yarn-client
    streaming.jar
```

<details><summary>Spark local logs</summary>
<p>

```shell
...
19/01/03 14:16:46 INFO BlockManagerMasterEndpoint: Using org.apache.spark.storage.DefaultTopologyMapper for getting topology information
19/01/03 14:16:46 INFO BlockManagerMasterEndpoint: BlockManagerMasterEndpoint up
19/01/03 14:16:46 INFO DiskBlockManager: Created local directory at C:\Logs\Temp\blockmgr-8fa108a6-d1fd-4fa7-9118-3b5815012ab2
19/01/03 14:16:46 INFO MemoryStore: MemoryStore started with capacity 1992.9 MB
19/01/03 14:16:46 INFO SparkEnv: Registering OutputCommitCoordinator
19/01/03 14:16:46 INFO Utils: Successfully started service 'SparkUI' on port 4040.
19/01/03 14:16:46 INFO SparkUI: Bound SparkUI to 0.0.0.0, and started at http://localhost:4040
19/01/03 14:16:47 INFO Executor: Starting executor ID driver on host localhost
19/01/03 14:16:47 INFO Utils: Successfully started service 'org.apache.spark.network.netty.NettyBlockTransferService' on port 50631.
19/01/03 14:16:47 INFO NettyBlockTransferService: Server created on localhost:50631
19/01/03 14:16:47 INFO BlockManager: Using org.apache.spark.storage.RandomBlockReplicationPolicy for block replication policy
19/01/03 14:16:47 INFO BlockManagerMaster: Registering BlockManager BlockManagerId(driver, localhost, 50631, None)
19/01/03 14:16:47 INFO BlockManagerMasterEndpoint: Registering block manager localhost:50631 with 1992.9 MB RAM, BlockManagerId(driver, localhost, 50631, None)
19/01/03 14:16:47 INFO BlockManagerMaster: Registered BlockManager BlockManagerId(driver, localhost, 50631, None)
19/01/03 14:16:47 INFO BlockManager: Initialized BlockManager: BlockManagerId(driver, localhost, 50631, None)
19/01/03 14:16:47 INFO SharedState: Setting hive.metastore.warehouse.dir ('null') to the value of spark.sql.warehouse.dir ('file:/User/artsiom/git/kafka-tweets/spark-warehouse/').
19/01/03 14:16:47 INFO SharedState: Warehouse path is 'file:/User/artsiom/git/kafka-tweets/spark-warehouse/'.
19/01/03 14:16:48 INFO StateStoreCoordinatorRef: Registered StateStoreCoordinator endpoint
19/01/03 14:16:48 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 1
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-577e71c5-cd04-48fc-8856-702361427721-1106824333-driver-0
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = earliest

19/01/03 14:16:48 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-1
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 1
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-577e71c5-cd04-48fc-8856-702361427721-1106824333-driver-0
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = earliest

19/01/03 14:16:48 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/03 14:16:48 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/03 14:16:51 WARN Utils: Truncated the string representation of a plan since it was too large. This behavior can be adjusted by setting 'spark.debug.maxToStringFields' in SparkEnv.conf.
19/01/03 14:16:57 INFO MicroBatchExecution: Starting hashtag counts [id = add09302-8d5c-43fd-b5df-159c03824a01, runId = ede4716d-6739-4d71-9302-20f0610a8086]. Use file:///tmp/ts-sink to store the query checkpoint.
19/01/03 14:16:57 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 1
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-driver-0
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = earliest

19/01/03 14:16:57 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-2
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 1
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-driver-0
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = earliest

19/01/03 14:16:57 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/03 14:16:57 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/03 14:16:57 INFO MicroBatchExecution: Starting new streaming query.
19/01/03 14:16:58 INFO AbstractCoordinator: Discovered coordinator 192.168.99.100:9092 (id: 2147483647 rack: null) for group spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-driver-0.
19/01/03 14:16:58 INFO ConsumerCoordinator: Revoking previously assigned partitions [] for group spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-driver-0
19/01/03 14:16:58 INFO AbstractCoordinator: (Re-)joining group spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-driver-0
19/01/03 14:16:58 INFO AbstractCoordinator: Successfully joined group spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-driver-0 with generation 1
19/01/03 14:16:58 INFO ConsumerCoordinator: Setting newly assigned partitions [Tweets-0, Tweets-1, Tweets-2, Tweets-3, Tweets-8, Tweets-9, Tweets-4, Tweets-5, Tweets-6, Tweets-7] for group spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-driver-0
19/01/03 14:17:02 INFO KafkaSource: Initial offsets: {"Tweets":{"8":0,"2":0,"5":0,"4":0,"7":0,"1":0,"9":0,"3":0,"6":0,"0":0}}
19/01/03 14:17:05 INFO MicroBatchExecution: Committed offsets for batch 0. Metadata OffsetSeqMetadata(0,1546514222326,Map(spark.sql.shuffle.partitions -> 200, spark.sql.streaming.stateStore.providerClass -> org.apache.spark.sql.execution.streaming.state.HDFSBackedStateStoreProvider))
19/01/03 14:17:05 INFO KafkaSource: GetBatch called with start = None, end = {"Tweets":{"8":14,"2":13,"5":12,"4":11,"7":20,"1":9,"9":12,"3":17,"6":20,"0":18}}
19/01/03 14:17:05 INFO KafkaSource: Partitions added: Map()
19/01/03 14:17:06 INFO KafkaSource: GetBatch generating RDD of offset range: KafkaSourceRDDOffsetRange(Tweets-0,0,18,None), KafkaSourceRDDOffsetRange(Tweets-1,0,9,None), KafkaSourceRDDOffsetRange(Tweets-2,0,13,None), KafkaSourceRDDOffsetRange(Tweets-3,0,17,None), KafkaSourceRDDOffsetRange(Tweets-4,0,11,None), KafkaSourceRDDOffsetRange(Tweets-5,0,12,None), KafkaSourceRDDOffsetRange(Tweets-6,0,20,None), KafkaSourceRDDOffsetRange(Tweets-7,0,20,None), KafkaSourceRDDOffsetRange(Tweets-8,0,14,None), KafkaSourceRDDOffsetRange(Tweets-9,0,12,None)
19/01/03 14:17:08 INFO CodeGenerator: Code generated in 238.61006 ms
19/01/03 14:17:08 INFO CodeGenerator: Code generated in 83.44567 ms
19/01/03 14:17:08 INFO HashAggregateExec: spark.sql.codegen.aggregate.map.twolevel.enabled is set to true, but current version of codegened fast hashmap does not support this aggregate.
19/01/03 14:17:08 INFO CodeGenerator: Code generated in 28.967937 ms
19/01/03 14:17:08 INFO HashAggregateExec: spark.sql.codegen.aggregate.map.twolevel.enabled is set to true, but current version of codegened fast hashmap does not support this aggregate.
19/01/03 14:17:08 INFO CodeGenerator: Code generated in 96.47672 ms
19/01/03 14:17:08 INFO HashAggregateExec: spark.sql.codegen.aggregate.map.twolevel.enabled is set to true, but current version of codegened fast hashmap does not support this aggregate.
19/01/03 14:17:08 INFO CodeGenerator: Code generated in 67.813799 ms
19/01/03 14:17:09 INFO MemoryStore: Block broadcast_0 stored as values in memory (estimated size 220.8 KB, free 1992.7 MB)
19/01/03 14:17:09 INFO MemoryStore: Block broadcast_0_piece0 stored as bytes in memory (estimated size 20.6 KB, free 1992.7 MB)
19/01/03 14:17:09 INFO BlockManagerInfo: Added broadcast_0_piece0 in memory on localhost:50631 (size: 20.6 KB, free: 1992.9 MB)
19/01/03 14:17:09 INFO SparkContext: Created broadcast 0 from start at Main.scala:54
19/01/03 14:17:09 INFO MemoryStore: Block broadcast_1 stored as values in memory (estimated size 220.8 KB, free 1992.4 MB)
19/01/03 14:17:09 INFO MemoryStore: Block broadcast_1_piece0 stored as bytes in memory (estimated size 20.6 KB, free 1992.4 MB)
19/01/03 14:17:09 INFO BlockManagerInfo: Added broadcast_1_piece0 in memory on localhost:50631 (size: 20.6 KB, free: 1992.9 MB)
19/01/03 14:17:09 INFO SparkContext: Created broadcast 1 from start at Main.scala:54
19/01/03 14:17:09 INFO WriteToDataSourceV2Exec: Start processing data source writer: org.apache.spark.sql.execution.streaming.sources.InternalRowMicroBatchWriter@28f30d36. The input RDD has 200 partitions.
19/01/03 14:17:09 INFO SparkContext: Starting job: start at Main.scala:54
19/01/03 14:17:09 INFO DAGScheduler: Registering RDD 6 (start at Main.scala:54)
19/01/03 14:17:09 INFO DAGScheduler: Got job 0 (start at Main.scala:54) with 200 output partitions
19/01/03 14:17:09 INFO DAGScheduler: Final stage: ResultStage 1 (start at Main.scala:54)
19/01/03 14:17:09 INFO DAGScheduler: Parents of final stage: List(ShuffleMapStage 0)
19/01/03 14:17:09 INFO DAGScheduler: Missing parents: List(ShuffleMapStage 0)
19/01/03 14:17:09 INFO DAGScheduler: Submitting ShuffleMapStage 0 (MapPartitionsRDD[6] at start at Main.scala:54), which has no missing parents
19/01/03 14:17:09 INFO MemoryStore: Block broadcast_2 stored as values in memory (estimated size 42.2 KB, free 1992.4 MB)
19/01/03 14:17:09 INFO MemoryStore: Block broadcast_2_piece0 stored as bytes in memory (estimated size 17.4 KB, free 1992.4 MB)
19/01/03 14:17:09 INFO BlockManagerInfo: Added broadcast_2_piece0 in memory on localhost:50631 (size: 17.4 KB, free: 1992.8 MB)
19/01/03 14:17:09 INFO SparkContext: Created broadcast 2 from broadcast at DAGScheduler.scala:1039
19/01/03 14:17:09 INFO DAGScheduler: Submitting 10 missing tasks from ShuffleMapStage 0 (MapPartitionsRDD[6] at start at Main.scala:54) (first 15 tasks are for partitions Vector(0, 1, 2, 3, 4, 5, 6, 7, 8, 9))
19/01/03 14:17:09 INFO TaskSchedulerImpl: Adding task set 0.0 with 10 tasks
19/01/03 14:17:09 INFO TaskSetManager: Starting task 0.0 in stage 0.0 (TID 0, localhost, executor driver, partition 0, PROCESS_LOCAL, 8018 bytes)
19/01/03 14:17:09 INFO TaskSetManager: Starting task 1.0 in stage 0.0 (TID 1, localhost, executor driver, partition 1, PROCESS_LOCAL, 8018 bytes)
19/01/03 14:17:09 INFO TaskSetManager: Starting task 2.0 in stage 0.0 (TID 2, localhost, executor driver, partition 2, PROCESS_LOCAL, 8018 bytes)
19/01/03 14:17:09 INFO TaskSetManager: Starting task 3.0 in stage 0.0 (TID 3, localhost, executor driver, partition 3, PROCESS_LOCAL, 8018 bytes)
19/01/03 14:17:09 INFO Executor: Running task 0.0 in stage 0.0 (TID 0)
19/01/03 14:17:09 INFO Executor: Running task 3.0 in stage 0.0 (TID 3)
19/01/03 14:17:09 INFO Executor: Running task 2.0 in stage 0.0 (TID 2)
19/01/03 14:17:09 INFO Executor: Running task 1.0 in stage 0.0 (TID 1)
19/01/03 14:17:10 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/03 14:17:10 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-3
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/03 14:17:10 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/03 14:17:10 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/03 14:17:10 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/03 14:17:10 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-4
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/03 14:17:10 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/03 14:17:10 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/03 14:17:10 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/03 14:17:10 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-5
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/03 14:17:10 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/03 14:17:10 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/03 14:17:10 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/03 14:17:10 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-6
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/03 14:17:10 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/03 14:17:10 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/03 14:17:10 INFO CodeGenerator: Code generated in 20.994843 ms
19/01/03 14:17:10 INFO CodeGenerator: Code generated in 65.602668 ms
19/01/03 14:17:10 INFO CodeGenerator: Code generated in 6.770347 ms
19/01/03 14:17:10 INFO CodeGenerator: Code generated in 8.165534 ms
19/01/03 14:17:10 INFO CodeGenerator: Code generated in 11.515587 ms
19/01/03 14:17:10 INFO CodeGenerator: Code generated in 5.897632 ms
19/01/03 14:17:10 INFO CodeGenerator: Code generated in 13.082687 ms
19/01/03 14:17:10 INFO AbstractCoordinator: Discovered coordinator 192.168.99.100:9092 (id: 2147483647 rack: null) for group spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor.
19/01/03 14:17:10 INFO AbstractCoordinator: Discovered coordinator 192.168.99.100:9092 (id: 2147483647 rack: null) for group spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor.
19/01/03 14:17:10 INFO AbstractCoordinator: Discovered coordinator 192.168.99.100:9092 (id: 2147483647 rack: null) for group spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor.
19/01/03 14:17:10 INFO AbstractCoordinator: Discovered coordinator 192.168.99.100:9092 (id: 2147483647 rack: null) for group spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor.
19/01/03 14:17:11 INFO Executor: Finished task 0.0 in stage 0.0 (TID 0). 2497 bytes result sent to driver
19/01/03 14:17:11 INFO Executor: Finished task 3.0 in stage 0.0 (TID 3). 2454 bytes result sent to driver
19/01/03 14:17:11 INFO Executor: Finished task 1.0 in stage 0.0 (TID 1). 2497 bytes result sent to driver
19/01/03 14:17:11 INFO Executor: Finished task 2.0 in stage 0.0 (TID 2). 2454 bytes result sent to driver
19/01/03 14:17:11 INFO TaskSetManager: Starting task 4.0 in stage 0.0 (TID 4, localhost, executor driver, partition 4, PROCESS_LOCAL, 8018 bytes)
19/01/03 14:17:11 INFO Executor: Running task 4.0 in stage 0.0 (TID 4)
19/01/03 14:17:11 INFO TaskSetManager: Starting task 5.0 in stage 0.0 (TID 5, localhost, executor driver, partition 5, PROCESS_LOCAL, 8018 bytes)
19/01/03 14:17:11 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/03 14:17:11 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-7
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/03 14:17:11 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/03 14:17:11 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/03 14:17:11 INFO Executor: Running task 5.0 in stage 0.0 (TID 5)
19/01/03 14:17:11 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/03 14:17:11 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-8
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/03 14:17:11 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/03 14:17:11 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/03 14:17:11 INFO TaskSetManager: Finished task 0.0 in stage 0.0 (TID 0) in 1697 ms on localhost (executor driver) (1/10)
19/01/03 14:17:11 INFO TaskSetManager: Starting task 6.0 in stage 0.0 (TID 6, localhost, executor driver, partition 6, PROCESS_LOCAL, 8018 bytes)
19/01/03 14:17:11 INFO TaskSetManager: Starting task 7.0 in stage 0.0 (TID 7, localhost, executor driver, partition 7, PROCESS_LOCAL, 8018 bytes)
19/01/03 14:17:11 INFO TaskSetManager: Finished task 3.0 in stage 0.0 (TID 3) in 1681 ms on localhost (executor driver) (2/10)
19/01/03 14:17:11 INFO Executor: Running task 7.0 in stage 0.0 (TID 7)
19/01/03 14:17:11 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/03 14:17:11 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-9
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/03 14:17:11 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/03 14:17:11 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/03 14:17:11 INFO Executor: Running task 6.0 in stage 0.0 (TID 6)
19/01/03 14:17:11 INFO TaskSetManager: Finished task 2.0 in stage 0.0 (TID 2) in 1690 ms on localhost (executor driver) (3/10)
19/01/03 14:17:11 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/03 14:17:11 INFO TaskSetManager: Finished task 1.0 in stage 0.0 (TID 1) in 1706 ms on localhost (executor driver) (4/10)
19/01/03 14:17:11 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-10
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/03 14:17:11 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/03 14:17:11 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/03 14:17:11 INFO AbstractCoordinator: Discovered coordinator 192.168.99.100:9092 (id: 2147483647 rack: null) for group spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor.
19/01/03 14:17:11 INFO AbstractCoordinator: Discovered coordinator 192.168.99.100:9092 (id: 2147483647 rack: null) for group spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor.
19/01/03 14:17:11 INFO AbstractCoordinator: Discovered coordinator 192.168.99.100:9092 (id: 2147483647 rack: null) for group spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor.
19/01/03 14:17:11 INFO AbstractCoordinator: Discovered coordinator 192.168.99.100:9092 (id: 2147483647 rack: null) for group spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor.
19/01/03 14:17:12 INFO Executor: Finished task 7.0 in stage 0.0 (TID 7). 2368 bytes result sent to driver
19/01/03 14:17:12 INFO TaskSetManager: Starting task 8.0 in stage 0.0 (TID 8, localhost, executor driver, partition 8, PROCESS_LOCAL, 8018 bytes)
19/01/03 14:17:12 INFO Executor: Running task 8.0 in stage 0.0 (TID 8)
19/01/03 14:17:12 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/03 14:17:12 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-11
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/03 14:17:12 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/03 14:17:12 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/03 14:17:12 INFO TaskSetManager: Finished task 7.0 in stage 0.0 (TID 7) in 522 ms on localhost (executor driver) (5/10)
19/01/03 14:17:12 INFO Executor: Finished task 5.0 in stage 0.0 (TID 5). 2368 bytes result sent to driver
19/01/03 14:17:12 INFO TaskSetManager: Starting task 9.0 in stage 0.0 (TID 9, localhost, executor driver, partition 9, PROCESS_LOCAL, 8018 bytes)
19/01/03 14:17:12 INFO TaskSetManager: Finished task 5.0 in stage 0.0 (TID 5) in 624 ms on localhost (executor driver) (6/10)
19/01/03 14:17:12 INFO Executor: Running task 9.0 in stage 0.0 (TID 9)
19/01/03 14:17:12 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/03 14:17:12 INFO ConsumerConfig: ConsumerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	partition.assignment.strategy = [org.apache.kafka.clients.consumer.RangeAssignor]
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	max.partition.fetch.bytes = 1048576
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	enable.auto.commit = false
	sasl.mechanism = GSSAPI
	interceptor.classes = null
	exclude.internal.topics = true
	ssl.truststore.password = null
	client.id = consumer-12
	ssl.endpoint.identification.algorithm = null
	max.poll.records = 2147483647
	check.crcs = true
	request.timeout.ms = 40000
	heartbeat.interval.ms = 3000
	auto.commit.interval.ms = 5000
	receive.buffer.bytes = 65536
	ssl.truststore.type = JKS
	ssl.truststore.location = null
	ssl.keystore.password = null
	fetch.min.bytes = 1
	send.buffer.bytes = 131072
	value.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	group.id = spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	ssl.key.password = null
	fetch.max.wait.ms = 500
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	session.timeout.ms = 30000
	metrics.num.samples = 2
	key.deserializer = class org.apache.kafka.common.serialization.ByteArrayDeserializer
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	auto.offset.reset = none

19/01/03 14:17:12 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/03 14:17:12 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/03 14:17:12 INFO Executor: Finished task 4.0 in stage 0.0 (TID 4). 2368 bytes result sent to driver
19/01/03 14:17:12 INFO TaskSetManager: Finished task 4.0 in stage 0.0 (TID 4) in 673 ms on localhost (executor driver) (7/10)
19/01/03 14:17:12 INFO Executor: Finished task 6.0 in stage 0.0 (TID 6). 2368 bytes result sent to driver
19/01/03 14:17:12 INFO TaskSetManager: Finished task 6.0 in stage 0.0 (TID 6) in 638 ms on localhost (executor driver) (8/10)
19/01/03 14:17:12 INFO AbstractCoordinator: Discovered coordinator 192.168.99.100:9092 (id: 2147483647 rack: null) for group spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor.
19/01/03 14:17:12 INFO Executor: Finished task 8.0 in stage 0.0 (TID 8). 2411 bytes result sent to driver
19/01/03 14:17:12 INFO TaskSetManager: Finished task 8.0 in stage 0.0 (TID 8) in 206 ms on localhost (executor driver) (9/10)
19/01/03 14:17:12 INFO AbstractCoordinator: Discovered coordinator 192.168.99.100:9092 (id: 2147483647 rack: null) for group spark-kafka-source-b5dd24fd-c9cd-4cf4-a207-e88c62fc8b73-160176770-executor.
19/01/03 14:17:12 INFO Executor: Finished task 9.0 in stage 0.0 (TID 9). 2411 bytes result sent to driver
19/01/03 14:17:12 INFO TaskSetManager: Finished task 9.0 in stage 0.0 (TID 9) in 388 ms on localhost (executor driver) (10/10)
19/01/03 14:17:12 INFO TaskSchedulerImpl: Removed TaskSet 0.0, whose tasks have all completed, from pool
19/01/03 14:17:12 INFO DAGScheduler: ShuffleMapStage 0 (start at Main.scala:54) finished in 2.988 s
19/01/03 14:17:12 INFO DAGScheduler: looking for newly runnable stages
19/01/03 14:17:12 INFO DAGScheduler: running: Set()
19/01/03 14:17:12 INFO DAGScheduler: waiting: Set(ResultStage 1)
19/01/03 14:17:12 INFO DAGScheduler: failed: Set()
19/01/03 14:17:12 INFO DAGScheduler: Submitting ResultStage 1 (MapPartitionsRDD[12] at start at Main.scala:54), which has no missing parents
19/01/03 14:17:12 INFO MemoryStore: Block broadcast_3 stored as values in memory (estimated size 56.6 KB, free 1992.3 MB)
19/01/03 14:17:12 INFO MemoryStore: Block broadcast_3_piece0 stored as bytes in memory (estimated size 21.3 KB, free 1992.3 MB)
19/01/03 14:17:12 INFO BlockManagerInfo: Added broadcast_3_piece0 in memory on localhost:50631 (size: 21.3 KB, free: 1992.8 MB)
19/01/03 14:17:12 INFO SparkContext: Created broadcast 3 from broadcast at DAGScheduler.scala:1039
19/01/03 14:17:12 INFO DAGScheduler: Submitting 200 missing tasks from ResultStage 1 (MapPartitionsRDD[12] at start at Main.scala:54) (first 15 tasks are for partitions Vector(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14))
19/01/03 14:17:12 INFO TaskSchedulerImpl: Adding task set 1.0 with 200 tasks
19/01/03 14:17:12 INFO TaskSetManager: Starting task 1.0 in stage 1.0 (TID 10, localhost, executor driver, partition 1, PROCESS_LOCAL, 7754 bytes)
19/01/03 14:17:12 INFO TaskSetManager: Starting task 2.0 in stage 1.0 (TID 11, localhost, executor driver, partition 2, PROCESS_LOCAL, 7754 bytes)
19/01/03 14:17:12 INFO TaskSetManager: Starting task 3.0 in stage 1.0 (TID 12, localhost, executor driver, partition 3, PROCESS_LOCAL, 7754 bytes)
19/01/03 14:17:12 INFO TaskSetManager: Starting task 6.0 in stage 1.0 (TID 13, localhost, executor driver, partition 6, PROCESS_LOCAL, 7754 bytes)
19/01/03 14:17:12 INFO Executor: Running task 1.0 in stage 1.0 (TID 10)
19/01/03 14:17:12 INFO Executor: Running task 3.0 in stage 1.0 (TID 12)
19/01/03 14:17:12 INFO Executor: Running task 2.0 in stage 1.0 (TID 11)
19/01/03 14:17:12 INFO Executor: Running task 6.0 in stage 1.0 (TID 13)
19/01/03 14:17:12 INFO StateStore: State Store maintenance task started
19/01/03 14:17:12 INFO StateStore: Env is not null
19/01/03 14:17:12 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:17:12 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@6e99bf04
19/01/03 14:17:12 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,1,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:17:12 INFO StateStore: Env is not null
19/01/03 14:17:12 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:17:12 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@2f8b5738
19/01/03 14:17:12 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,2,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:17:12 INFO StateStore: Env is not null
19/01/03 14:17:12 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:17:12 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@290a324c
19/01/03 14:17:12 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,3,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:17:12 INFO StateStore: Env is not null
19/01/03 14:17:12 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:17:12 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@7d23ac18
19/01/03 14:17:12 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,6,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:17:12 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=6),dir = file:/tmp/ts-sink/state/0/6] for update
19/01/03 14:17:12 INFO StateStore: Env is not null
19/01/03 14:17:12 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:17:12 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=1),dir = file:/tmp/ts-sink/state/0/1] for update
19/01/03 14:17:12 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=2),dir = file:/tmp/ts-sink/state/0/2] for update
19/01/03 14:17:12 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=3),dir = file:/tmp/ts-sink/state/0/3] for update
19/01/03 14:17:12 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@142dd85b
19/01/03 14:17:12 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,6,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:17:12 INFO StateStore: Env is not null
19/01/03 14:17:12 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:17:12 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=6),dir = file:/tmp/ts-sink/state/0/6] for update
19/01/03 14:17:12 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@1a64f732
19/01/03 14:17:12 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,3,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:17:12 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=3),dir = file:/tmp/ts-sink/state/0/3] for update
19/01/03 14:17:12 INFO StateStore: Env is not null
19/01/03 14:17:12 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:17:12 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@32f6de5d
19/01/03 14:17:12 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,2,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:17:12 INFO StateStore: Env is not null
19/01/03 14:17:12 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:17:12 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=2),dir = file:/tmp/ts-sink/state/0/2] for update
19/01/03 14:17:12 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@2dfe1c13
19/01/03 14:17:12 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,1,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:17:12 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=1),dir = file:/tmp/ts-sink/state/0/1] for update
19/01/03 14:17:12 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 10 blocks
19/01/03 14:17:12 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 10 blocks
19/01/03 14:17:12 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 10 blocks
19/01/03 14:17:12 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 10 blocks
19/01/03 14:17:12 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 6 ms
19/01/03 14:17:12 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 7 ms
19/01/03 14:17:12 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 7 ms
19/01/03 14:17:12 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 7 ms
19/01/03 14:17:12 INFO CodeGenerator: Code generated in 10.570068 ms
19/01/03 14:17:13 INFO CodeGenerator: Code generated in 8.57543 ms
19/01/03 14:17:13 INFO CodeGenerator: Code generated in 7.573461 ms
19/01/03 14:17:14 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=2),dir=file:/tmp/ts-sink/state/0/2] to file file:/tmp/ts-sink/state/0/2/1.delta
19/01/03 14:17:14 INFO DataWritingSparkTask: Writer for partition 2 is committing.
19/01/03 14:17:14 INFO ProducerConfig: ProducerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	sasl.mechanism = GSSAPI
	max.block.ms = 60000
	interceptor.classes = null
	ssl.truststore.password = null
	client.id =
	ssl.endpoint.identification.algorithm = null
	request.timeout.ms = 30000
	acks = 1
	receive.buffer.bytes = 32768
	ssl.truststore.type = JKS
	retries = 0
	ssl.truststore.location = null
	ssl.keystore.password = null
	send.buffer.bytes = 131072
	compression.type = none
	metadata.fetch.timeout.ms = 60000
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	buffer.memory = 33554432
	timeout.ms = 30000
	key.serializer = class org.apache.kafka.common.serialization.ByteArraySerializer
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	block.on.buffer.full = false
	ssl.key.password = null
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	max.in.flight.requests.per.connection = 5
	metrics.num.samples = 2
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	batch.size = 16384
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	max.request.size = 1048576
	value.serializer = class org.apache.kafka.common.serialization.ByteArraySerializer
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	partitioner.class = class org.apache.kafka.clients.producer.internals.DefaultPartitioner
	linger.ms = 0

19/01/03 14:17:14 INFO ProducerConfig: ProducerConfig values:
	metric.reporters = []
	metadata.max.age.ms = 300000
	reconnect.backoff.ms = 50
	sasl.kerberos.ticket.renew.window.factor = 0.8
	bootstrap.servers = [192.168.99.100:9092]
	ssl.keystore.type = JKS
	sasl.mechanism = GSSAPI
	max.block.ms = 60000
	interceptor.classes = null
	ssl.truststore.password = null
	client.id = producer-1
	ssl.endpoint.identification.algorithm = null
	request.timeout.ms = 30000
	acks = 1
	receive.buffer.bytes = 32768
	ssl.truststore.type = JKS
	retries = 0
	ssl.truststore.location = null
	ssl.keystore.password = null
	send.buffer.bytes = 131072
	compression.type = none
	metadata.fetch.timeout.ms = 60000
	retry.backoff.ms = 100
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	buffer.memory = 33554432
	timeout.ms = 30000
	key.serializer = class org.apache.kafka.common.serialization.ByteArraySerializer
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	ssl.trustmanager.algorithm = PKIX
	block.on.buffer.full = false
	ssl.key.password = null
	sasl.kerberos.min.time.before.relogin = 60000
	connections.max.idle.ms = 540000
	max.in.flight.requests.per.connection = 5
	metrics.num.samples = 2
	ssl.protocol = TLS
	ssl.provider = null
	ssl.enabled.protocols = [TLSv1.2, TLSv1.1, TLSv1]
	batch.size = 16384
	ssl.keystore.location = null
	ssl.cipher.suites = null
	security.protocol = PLAINTEXT
	max.request.size = 1048576
	value.serializer = class org.apache.kafka.common.serialization.ByteArraySerializer
	ssl.keymanager.algorithm = SunX509
	metrics.sample.window.ms = 30000
	partitioner.class = class org.apache.kafka.clients.producer.internals.DefaultPartitioner
	linger.ms = 0

19/01/03 14:17:14 INFO AppInfoParser: Kafka version : 0.10.0.1
19/01/03 14:17:14 INFO AppInfoParser: Kafka commitId : a7a17cdec9eaa6c5
19/01/03 14:17:14 INFO DataWritingSparkTask: Writer for partition 2 committed.
19/01/03 14:17:15 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=1),dir=file:/tmp/ts-sink/state/0/1] to file file:/tmp/ts-sink/state/0/1/1.delta
19/01/03 14:17:15 INFO DataWritingSparkTask: Writer for partition 1 is committing.
19/01/03 14:17:15 INFO DataWritingSparkTask: Writer for partition 1 committed.
19/01/03 14:17:15 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=3),dir=file:/tmp/ts-sink/state/0/3] to file file:/tmp/ts-sink/state/0/3/1.delta
19/01/03 14:17:15 INFO DataWritingSparkTask: Writer for partition 3 is committing.
19/01/03 14:17:15 INFO DataWritingSparkTask: Writer for partition 3 committed.
19/01/03 14:17:16 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=6),dir=file:/tmp/ts-sink/state/0/6] to file file:/tmp/ts-sink/state/0/6/1.delta
19/01/03 14:17:16 INFO DataWritingSparkTask: Writer for partition 6 is committing.
19/01/03 14:17:16 INFO DataWritingSparkTask: Writer for partition 6 committed.
19/01/03 14:17:16 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=2),dir=file:/tmp/ts-sink/state/0/2]
19/01/03 14:17:16 INFO Executor: Finished task 2.0 in stage 1.0 (TID 11). 4627 bytes result sent to driver
19/01/03 14:17:16 INFO TaskSetManager: Starting task 7.0 in stage 1.0 (TID 14, localhost, executor driver, partition 7, PROCESS_LOCAL, 7754 bytes)
19/01/03 14:17:16 INFO Executor: Running task 7.0 in stage 1.0 (TID 14)
19/01/03 14:17:16 INFO TaskSetManager: Finished task 2.0 in stage 1.0 (TID 11) in 3641 ms on localhost (executor driver) (1/200)
..
19/01/03 14:17:52 INFO StateStore: Env is not null
19/01/03 14:17:52 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:17:52 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@6fd42484
19/01/03 14:17:52 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,62,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:17:52 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=62),dir = file:/tmp/ts-sink/state/0/62] for update
19/01/03 14:17:52 INFO StateStore: Env is not null
19/01/03 14:17:52 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:17:52 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@346334f3
19/01/03 14:17:52 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,62,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:17:52 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=62),dir = file:/tmp/ts-sink/state/0/62] for update
19/01/03 14:17:52 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 10 blocks
19/01/03 14:17:52 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:17:54 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=56),dir=file:/tmp/ts-sink/state/0/56] to file file:/tmp/ts-sink/state/0/56/1.delta
19/01/03 14:17:54 INFO DataWritingSparkTask: Writer for partition 56 is committing.
19/01/03 14:17:54 INFO DataWritingSparkTask: Writer for partition 56 committed.
19/01/03 14:17:54 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=62),dir=file:/tmp/ts-sink/state/0/62] to file file:/tmp/ts-sink/state/0/62/1.delta
19/01/03 14:17:54 INFO DataWritingSparkTask: Writer for partition 62 is committing.
19/01/03 14:17:54 INFO DataWritingSparkTask: Writer for partition 62 committed.
19/01/03 14:17:54 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=53),dir=file:/tmp/ts-sink/state/0/53] to file file:/tmp/ts-sink/state/0/53/1.delta
19/01/03 14:17:54 INFO DataWritingSparkTask: Writer for partition 53 is committing.
19/01/03 14:17:54 INFO DataWritingSparkTask: Writer for partition 53 committed.
19/01/03 14:17:55 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=55),dir=file:/tmp/ts-sink/state/0/55] to file file:/tmp/ts-sink/state/0/55/1.delta
19/01/03 14:17:55 INFO DataWritingSparkTask: Writer for partition 55 is committing.
19/01/03 14:17:55 INFO DataWritingSparkTask: Writer for partition 55 committed.
19/01/03 14:17:56 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=56),dir=file:/tmp/ts-sink/state/0/56]
19/01/03 14:17:56 INFO Executor: Finished task 56.0 in stage 1.0 (TID 42). 4584 bytes result sent to driver
19/01/03 14:17:56 INFO TaskSetManager: Starting task 64.0 in stage 1.0 (TID 44, localhost, executor driver, partition 64, PROCESS_LOCAL, 7754 bytes)
19/01/03 14:17:56 INFO Executor: Running task 64.0 in stage 1.0 (TID 44)
19/01/03 14:17:56 INFO TaskSetManager: Finished task 56.0 in stage 1.0 (TID 42) in 3718 ms on localhost (executor driver) (31/200)
19/01/03 14:17:56 INFO StateStore: Env is not null
19/01/03 14:17:56 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:17:56 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@421a8e4e
19/01/03 14:17:56 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,64,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:17:56 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=64),dir = file:/tmp/ts-sink/state/0/64] for update
19/01/03 14:17:56 INFO StateStore: Env is not null
19/01/03 14:17:56 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:17:56 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@5c6fa74b
19/01/03 14:17:56 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,64,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:17:56 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=64),dir = file:/tmp/ts-sink/state/0/64] for update
19/01/03 14:17:56 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 10 blocks
19/01/03 14:17:56 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/03 14:17:56 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=62),dir=file:/tmp/ts-sink/state/0/62]
19/01/03 14:17:56 INFO Executor: Finished task 62.0 in stage 1.0 (TID 43). 4584 bytes result sent to driver
19/01/03 14:17:56 INFO TaskSetManager: Starting task 65.0 in stage 1.0 (TID 45, localhost, executor driver, partition 65, PROCESS_LOCAL, 7754 bytes)
19/01/03 14:17:56 INFO Executor: Running task 65.0 in stage 1.0 (TID 45)
19/01/03 14:17:56 INFO TaskSetManager: Finished task 62.0 in stage 1.0 (TID 43) in 3719 ms on localhost (executor driver) (32/200)
19/01/03 14:17:56 INFO StateStore: Env is not null
19/01/03 14:17:56 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:17:56 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@540fac66
19/01/03 14:17:56 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,65,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:17:56 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=65),dir = file:/tmp/ts-sink/state/0/65] for update
19/01/03 14:17:56 INFO StateStore: Env is not null
19/01/03 14:17:56 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:17:56 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@5645edc5
19/01/03 14:17:56 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,65,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:17:56 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=65),dir = file:/tmp/ts-sink/state/0/65] for update
19/01/03 14:17:56 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 10 blocks
19/01/03 14:17:56 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:17:57 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=64),dir=file:/tmp/ts-sink/state/0/64] to file file:/tmp/ts-sink/state/0/64/1.delta
19/01/03 14:17:57 INFO DataWritingSparkTask: Writer for partition 64 is committing.
19/01/03 14:17:57 INFO DataWritingSparkTask: Writer for partition 64 committed.
19/01/03 14:17:58 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=65),dir=file:/tmp/ts-sink/state/0/65] to file file:/tmp/ts-sink/state/0/65/1.delta
19/01/03 14:17:58 INFO DataWritingSparkTask: Writer for partition 65 is committing.
19/01/03 14:17:58 INFO DataWritingSparkTask: Writer for partition 65 committed.
19/01/03 14:17:58 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=53),dir=file:/tmp/ts-sink/state/0/53]
19/01/03 14:17:58 INFO Executor: Finished task 53.0 in stage 1.0 (TID 40). 4584 bytes result sent to driver
19/01/03 14:17:58 INFO TaskSetManager: Starting task 66.0 in stage 1.0 (TID 46, localhost, executor driver, partition 66, PROCESS_LOCAL, 7754 bytes)
19/01/03 14:17:58 INFO Executor: Running task 66.0 in stage 1.0 (TID 46)
19/01/03 14:17:58 INFO TaskSetManager: Finished task 53.0 in stage 1.0 (TID 40) in 7434 ms on localhost (executor driver) (33/200)
19/01/03 14:17:58 INFO StateStore: Env is not null
19/01/03 14:17:58 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:17:58 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@9e8c1d7
19/01/03 14:17:58 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,66,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:17:58 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=66),dir = file:/tmp/ts-sink/state/0/66] for update
19/01/03 14:17:58 INFO StateStore: Env is not null
19/01/03 14:17:58 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:17:58 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@5b60586e
19/01/03 14:17:58 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,66,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:17:58 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=66),dir = file:/tmp/ts-sink/state/0/66] for update
19/01/03 14:17:58 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 10 blocks
19/01/03 14:17:58 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/03 14:17:59 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=55),dir=file:/tmp/ts-sink/state/0/55]
19/01/03 14:17:59 INFO Executor: Finished task 55.0 in stage 1.0 (TID 41). 4584 bytes result sent to driver
19/01/03 14:17:59 INFO TaskSetManager: Starting task 67.0 in stage 1.0 (TID 47, localhost, executor driver, partition 67, PROCESS_LOCAL, 7754 bytes)
19/01/03 14:17:59 INFO TaskSetManager: Finished task 55.0 in stage 1.0 (TID 41) in 7435 ms on localhost (executor driver) (34/200)
19/01/03 14:17:59 INFO Executor: Running task 67.0 in stage 1.0 (TID 47)
19/01/03 14:17:59 INFO StateStore: Env is not null
19/01/03 14:17:59 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:17:59 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@2473aa1a
19/01/03 14:17:59 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,67,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:17:59 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=67),dir = file:/tmp/ts-sink/state/0/67] for update
19/01/03 14:17:59 INFO StateStore: Env is not null
19/01/03 14:17:59 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:17:59 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@6a0be50
19/01/03 14:17:59 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,67,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:17:59 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=67),dir = file:/tmp/ts-sink/state/0/67] for update
19/01/03 14:17:59 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 10 blocks
19/01/03 14:17:59 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:17:59 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=64),dir=file:/tmp/ts-sink/state/0/64]
19/01/03 14:17:59 INFO Executor: Finished task 64.0 in stage 1.0 (TID 44). 4584 bytes result sent to driver
19/01/03 14:17:59 INFO TaskSetManager: Starting task 68.0 in stage 1.0 (TID 48, localhost, executor driver, partition 68, PROCESS_LOCAL, 7754 bytes)
19/01/03 14:17:59 INFO Executor: Running task 68.0 in stage 1.0 (TID 48)
19/01/03 14:17:59 INFO TaskSetManager: Finished task 64.0 in stage 1.0 (TID 44) in 3736 ms on localhost (executor driver) (35/200)
19/01/03 14:17:59 INFO StateStore: Env is not null
19/01/03 14:17:59 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:17:59 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@43079a2d
19/01/03 14:17:59 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,68,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:17:59 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=68),dir = file:/tmp/ts-sink/state/0/68] for update
19/01/03 14:17:59 INFO StateStore: Env is not null
19/01/03 14:17:59 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:17:59 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@37a064e1
19/01/03 14:17:59 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,68,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:17:59 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=68),dir = file:/tmp/ts-sink/state/0/68] for update
19/01/03 14:17:59 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 10 blocks
19/01/03 14:17:59 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:18:00 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=65),dir=file:/tmp/ts-sink/state/0/65]
19/01/03 14:18:00 INFO Executor: Finished task 65.0 in stage 1.0 (TID 45). 4584 bytes result sent to driver
19/01/03 14:18:00 INFO TaskSetManager: Starting task 70.0 in stage 1.0 (TID 49, localhost, executor driver, partition 70, PROCESS_LOCAL, 7754 bytes)
19/01/03 14:18:00 INFO Executor: Running task 70.0 in stage 1.0 (TID 49)
19/01/03 14:18:00 INFO TaskSetManager: Finished task 65.0 in stage 1.0 (TID 45) in 3720 ms on localhost (executor driver) (36/200)
19/01/03 14:18:00 INFO StateStore: Env is not null
19/01/03 14:18:00 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:18:00 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@5eac28e9
19/01/03 14:18:00 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,70,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:18:00 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=70),dir = file:/tmp/ts-sink/state/0/70] for update
19/01/03 14:18:00 INFO StateStore: Env is not null
19/01/03 14:18:00 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:18:00 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@5454c2cd
19/01/03 14:18:00 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,70,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:18:00 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=70),dir = file:/tmp/ts-sink/state/0/70] for update
19/01/03 14:18:00 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 10 blocks
19/01/03 14:18:00 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:18:01 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=68),dir=file:/tmp/ts-sink/state/0/68] to file file:/tmp/ts-sink/state/0/68/1.delta
19/01/03 14:18:01 INFO DataWritingSparkTask: Writer for partition 68 is committing.
19/01/03 14:18:01 INFO DataWritingSparkTask: Writer for partition 68 committed.
19/01/03 14:18:01 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=70),dir=file:/tmp/ts-sink/state/0/70] to file file:/tmp/ts-sink/state/0/70/1.delta
19/01/03 14:18:01 INFO DataWritingSparkTask: Writer for partition 70 is committing.
19/01/03 14:18:01 INFO DataWritingSparkTask: Writer for partition 70 committed.
19/01/03 14:18:02 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=66),dir=file:/tmp/ts-sink/state/0/66] to file file:/tmp/ts-sink/state/0/66/1.delta
19/01/03 14:18:02 INFO DataWritingSparkTask: Writer for partition 66 is committing.
19/01/03 14:18:02 INFO DataWritingSparkTask: Writer for partition 66 committed.
19/01/03 14:18:03 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=67),dir=file:/tmp/ts-sink/state/0/67] to file file:/tmp/ts-sink/state/0/67/1.delta
19/01/03 14:18:03 INFO DataWritingSparkTask: Writer for partition 67 is committing.
19/01/03 14:18:03 INFO DataWritingSparkTask: Writer for partition 67 committed.
19/01/03 14:18:03 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=68),dir=file:/tmp/ts-sink/state/0/68]
19/01/03 14:18:03 INFO Executor: Finished task 68.0 in stage 1.0 (TID 48). 4584 bytes result sent to driver
19/01/03 14:18:03 INFO TaskSetManager: Starting task 71.0 in stage 1.0 (TID 50, localhost, executor driver, partition 71, PROCESS_LOCAL, 7754 bytes)
19/01/03 14:18:03 INFO Executor: Running task 71.0 in stage 1.0 (TID 50)
19/01/03 14:18:03 INFO TaskSetManager: Finished task 68.0 in stage 1.0 (TID 48) in 3728 ms on localhost (executor driver) (37/200)
19/01/03 14:18:03 INFO StateStore: Env is not null
19/01/03 14:18:03 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:18:03 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@41f49e7e
19/01/03 14:18:03 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,71,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:18:03 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=71),dir = file:/tmp/ts-sink/state/0/71] for update
19/01/03 14:18:03 INFO StateStore: Env is not null
19/01/03 14:18:03 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:18:03 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@6e0c9345
19/01/03 14:18:03 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,71,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:18:03 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=71),dir = file:/tmp/ts-sink/state/0/71] for update
19/01/03 14:18:03 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 10 blocks
19/01/03 14:18:03 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:18:04 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=70),dir=file:/tmp/ts-sink/state/0/70]
19/01/03 14:18:04 INFO Executor: Finished task 70.0 in stage 1.0 (TID 49). 4584 bytes result sent to driver
19/01/03 14:18:04 INFO TaskSetManager: Starting task 72.0 in stage 1.0 (TID 51, localhost, executor driver, partition 72, PROCESS_LOCAL, 7754 bytes)
19/01/03 14:18:04 INFO Executor: Running task 72.0 in stage 1.0 (TID 51)
19/01/03 14:18:04 INFO TaskSetManager: Finished task 70.0 in stage 1.0 (TID 49) in 3886 ms on localhost (executor driver) (38/200)
19/01/03 14:18:04 INFO StateStore: Env is not null
19/01/03 14:18:04 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:18:04 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@63e3d72e
19/01/03 14:18:04 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,72,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:18:04 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=72),dir = file:/tmp/ts-sink/state/0/72] for update
19/01/03 14:18:04 INFO StateStore: Env is not null
19/01/03 14:18:04 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:18:04 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@c64775d
19/01/03 14:18:04 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,72,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:18:04 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=72),dir = file:/tmp/ts-sink/state/0/72] for update
19/01/03 14:18:04 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 10 blocks
19/01/03 14:18:04 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:18:05 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=71),dir=file:/tmp/ts-sink/state/0/71] to file file:/tmp/ts-sink/state/0/71/1.delta
19/01/03 14:18:05 INFO DataWritingSparkTask: Writer for partition 71 is committing.
19/01/03 14:18:05 INFO DataWritingSparkTask: Writer for partition 71 committed.
19/01/03 14:18:05 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=72),dir=file:/tmp/ts-sink/state/0/72] to file file:/tmp/ts-sink/state/0/72/1.delta
19/01/03 14:18:05 INFO DataWritingSparkTask: Writer for partition 72 is committing.
19/01/03 14:18:05 INFO DataWritingSparkTask: Writer for partition 72 committed.
19/01/03 14:18:06 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=66),dir=file:/tmp/ts-sink/state/0/66]
19/01/03 14:18:06 INFO Executor: Finished task 66.0 in stage 1.0 (TID 46). 4627 bytes result sent to driver
19/01/03 14:18:06 INFO TaskSetManager: Starting task 75.0 in stage 1.0 (TID 52, localhost, executor driver, partition 75, PROCESS_LOCAL, 7754 bytes)
19/01/03 14:18:06 INFO Executor: Running task 75.0 in stage 1.0 (TID 52)
19/01/03 14:18:06 INFO TaskSetManager: Finished task 66.0 in stage 1.0 (TID 46) in 7546 ms on localhost (executor driver) (39/200)
19/01/03 14:18:06 INFO StateStore: Env is not null
19/01/03 14:18:06 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:18:06 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@58cd5ab0
19/01/03 14:18:06 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,75,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:18:06 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=75),dir = file:/tmp/ts-sink/state/0/75] for update
19/01/03 14:18:06 INFO StateStore: Env is not null
19/01/03 14:18:06 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:18:06 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@77489826
19/01/03 14:18:06 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,75,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:18:06 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=75),dir = file:/tmp/ts-sink/state/0/75] for update
19/01/03 14:18:06 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 10 blocks
19/01/03 14:18:06 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:18:07 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=67),dir=file:/tmp/ts-sink/state/0/67]
19/01/03 14:18:07 INFO Executor: Finished task 67.0 in stage 1.0 (TID 47). 4584 bytes result sent to driver
19/01/03 14:18:07 INFO TaskSetManager: Starting task 76.0 in stage 1.0 (TID 53, localhost, executor driver, partition 76, PROCESS_LOCAL, 7754 bytes)
19/01/03 14:18:07 INFO Executor: Running task 76.0 in stage 1.0 (TID 53)
19/01/03 14:18:07 INFO TaskSetManager: Finished task 67.0 in stage 1.0 (TID 47) in 7542 ms on localhost (executor driver) (40/200)
19/01/03 14:18:07 INFO StateStore: Env is not null
19/01/03 14:18:07 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:18:07 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@360424b8
19/01/03 14:18:07 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,76,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:18:07 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=76),dir = file:/tmp/ts-sink/state/0/76] for update
19/01/03 14:18:07 INFO StateStore: Env is not null
19/01/03 14:18:07 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:18:07 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@31bf165c
19/01/03 14:18:07 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,76,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:18:07 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=76),dir = file:/tmp/ts-sink/state/0/76] for update
19/01/03 14:18:07 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 10 blocks
19/01/03 14:18:07 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:18:07 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=71),dir=file:/tmp/ts-sink/state/0/71]
19/01/03 14:18:07 INFO Executor: Finished task 71.0 in stage 1.0 (TID 50). 4627 bytes result sent to driver
19/01/03 14:18:07 INFO TaskSetManager: Starting task 78.0 in stage 1.0 (TID 54, localhost, executor driver, partition 78, PROCESS_LOCAL, 7754 bytes)
19/01/03 14:18:07 INFO Executor: Running task 78.0 in stage 1.0 (TID 54)
19/01/03 14:18:07 INFO TaskSetManager: Finished task 71.0 in stage 1.0 (TID 50) in 3810 ms on localhost (executor driver) (41/200)
19/01/03 14:18:07 INFO StateStore: Env is not null
19/01/03 14:18:07 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:18:07 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@4271994f
19/01/03 14:18:07 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,78,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:18:07 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=78),dir = file:/tmp/ts-sink/state/0/78] for update
19/01/03 14:18:07 INFO StateStore: Env is not null
19/01/03 14:18:07 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:18:07 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@3b1bf9bc
19/01/03 14:18:07 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,78,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:18:07 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=78),dir = file:/tmp/ts-sink/state/0/78] for update
19/01/03 14:18:07 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 10 blocks
19/01/03 14:18:07 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:18:07 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=72),dir=file:/tmp/ts-sink/state/0/72]
19/01/03 14:18:07 INFO Executor: Finished task 72.0 in stage 1.0 (TID 51). 4584 bytes result sent to driver
19/01/03 14:18:07 INFO TaskSetManager: Starting task 79.0 in stage 1.0 (TID 55, localhost, executor driver, partition 79, PROCESS_LOCAL, 7754 bytes)
19/01/03 14:18:07 INFO Executor: Running task 79.0 in stage 1.0 (TID 55)
19/01/03 14:18:07 INFO TaskSetManager: Finished task 72.0 in stage 1.0 (TID 51) in 3686 ms on localhost (executor driver) (42/200)
19/01/03 14:18:07 INFO StateStore: Env is not null
19/01/03 14:18:07 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:18:07 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@4bba6262
19/01/03 14:18:07 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,79,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:18:07 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=79),dir = file:/tmp/ts-sink/state/0/79] for update
19/01/03 14:18:07 INFO StateStore: Env is not null
19/01/03 14:18:07 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:18:07 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@3db9c010
19/01/03 14:18:07 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,79,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:18:07 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=79),dir = file:/tmp/ts-sink/state/0/79] for update
19/01/03 14:18:07 INFO ShuffleBlockFetcherIterator: Getting 0 non-empty blocks out of 10 blocks
19/01/03 14:18:07 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:18:09 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=78),dir=file:/tmp/ts-sink/state/0/78] to file file:/tmp/ts-sink/state/0/78/1.delta
19/01/03 14:18:09 INFO DataWritingSparkTask: Writer for partition 78 is committing.
19/01/03 14:18:09 INFO DataWritingSparkTask: Writer for partition 78 committed.
19/01/03 14:18:09 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=79),dir=file:/tmp/ts-sink/state/0/79] to file file:/tmp/ts-sink/state/0/79/1.delta
19/01/03 14:18:09 INFO DataWritingSparkTask: Writer for partition 79 is committing.
19/01/03 14:18:09 INFO DataWritingSparkTask: Writer for partition 79 committed.
19/01/03 14:18:09 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=75),dir=file:/tmp/ts-sink/state/0/75] to file file:/tmp/ts-sink/state/0/75/1.delta
19/01/03 14:18:09 INFO DataWritingSparkTask: Writer for partition 75 is committing.
19/01/03 14:18:09 INFO DataWritingSparkTask: Writer for partition 75 committed.
19/01/03 14:18:10 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=76),dir=file:/tmp/ts-sink/state/0/76] to file file:/tmp/ts-sink/state/0/76/1.delta
19/01/03 14:18:10 INFO DataWritingSparkTask: Writer for partition 76 is committing.
19/01/03 14:18:10 INFO DataWritingSparkTask: Writer for partition 76 committed.
19/01/03 14:18:11 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=78),dir=file:/tmp/ts-sink/state/0/78]
19/01/03 14:18:11 INFO Executor: Finished task 78.0 in stage 1.0 (TID 54). 4584 bytes result sent to driver
19/01/03 14:18:11 INFO TaskSetManager: Starting task 80.0 in stage 1.0 (TID 56, localhost, executor driver, partition 80, PROCESS_LOCAL, 7754 bytes)
19/01/03 14:18:11 INFO TaskSetManager: Finished task 78.0 in stage 1.0 (TID 54) in 3903 ms on localhost (executor driver) (43/200)
19/01/03 14:18:11 INFO Executor: Running task 80.0 in stage 1.0 (TID 56)
19/01/03 14:18:11 INFO StateStore: Env is not null
19/01/03 14:18:11 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:18:11 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@44879654
19/01/03 14:18:11 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,80,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:18:11 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=80),dir = file:/tmp/ts-sink/state/0/80] for update
19/01/03 14:20:09 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,74,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:09 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=74),dir = file:/tmp/ts-sink/state/0/74] for update
19/01/03 14:20:09 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:09 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:10 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=63),dir=file:/tmp/ts-sink/state/0/63] to file file:/tmp/ts-sink/state/0/63/1.delta
19/01/03 14:20:10 INFO DataWritingSparkTask: Writer for partition 63 is committing.
19/01/03 14:20:10 INFO DataWritingSparkTask: Writer for partition 63 committed.
19/01/03 14:20:10 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=69),dir=file:/tmp/ts-sink/state/0/69] to file file:/tmp/ts-sink/state/0/69/1.delta
19/01/03 14:20:10 INFO DataWritingSparkTask: Writer for partition 69 is committing.
19/01/03 14:20:10 INFO DataWritingSparkTask: Writer for partition 69 committed.
19/01/03 14:20:11 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=74),dir=file:/tmp/ts-sink/state/0/74] to file file:/tmp/ts-sink/state/0/74/1.delta
19/01/03 14:20:11 INFO DataWritingSparkTask: Writer for partition 74 is committing.
19/01/03 14:20:11 INFO DataWritingSparkTask: Writer for partition 74 committed.
19/01/03 14:20:12 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=69),dir=file:/tmp/ts-sink/state/0/69]
19/01/03 14:20:12 INFO Executor: Finished task 69.0 in stage 1.0 (TID 142). 4584 bytes result sent to driver
19/01/03 14:20:12 INFO TaskSetManager: Starting task 77.0 in stage 1.0 (TID 145, localhost, executor driver, partition 77, ANY, 7754 bytes)
19/01/03 14:20:12 INFO Executor: Running task 77.0 in stage 1.0 (TID 145)
19/01/03 14:20:12 INFO TaskSetManager: Finished task 69.0 in stage 1.0 (TID 142) in 3703 ms on localhost (executor driver) (132/200)
19/01/03 14:20:16 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,83,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:16 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=83),dir = file:/tmp/ts-sink/state/0/83] for update
19/01/03 14:20:16 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:16 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:16 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=73),dir=file:/tmp/ts-sink/state/0/73]
19/01/03 14:20:16 INFO Executor: Finished task 73.0 in stage 1.0 (TID 143). 4584 bytes result sent to driver
19/01/03 14:20:16 INFO TaskSetManager: Starting task 84.0 in stage 1.0 (TID 149, localhost, executor driver, partition 84, ANY, 7754 bytes)
19/01/03 14:20:16 INFO Executor: Running task 84.0 in stage 1.0 (TID 149)
19/01/03 14:20:16 INFO TaskSetManager: Finished task 73.0 in stage 1.0 (TID 143) in 7567 ms on localhost (executor driver) (136/200)
19/01/03 14:20:16 INFO StateStore: Env is not null
19/01/03 14:20:16 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:16 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@56ff65f4
19/01/03 14:20:16 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,84,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:16 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=84),dir = file:/tmp/ts-sink/state/0/84] for update
19/01/03 14:20:16 INFO StateStore: Env is not null
19/01/03 14:20:16 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:16 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@7ab2c5ca
19/01/03 14:20:16 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,84,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:16 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=84),dir = file:/tmp/ts-sink/state/0/84] for update
19/01/03 14:20:16 INFO ShuffleBlockFetcherIterator: Getting 2 non-empty blocks out of 10 blocks
19/01/03 14:20:16 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:17 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=81),dir=file:/tmp/ts-sink/state/0/81]
19/01/03 14:20:17 INFO Executor: Finished task 81.0 in stage 1.0 (TID 146). 4584 bytes result sent to driver
19/01/03 14:20:17 INFO TaskSetManager: Starting task 87.0 in stage 1.0 (TID 150, localhost, executor driver, partition 87, ANY, 7754 bytes)
19/01/03 14:20:17 INFO Executor: Running task 87.0 in stage 1.0 (TID 150)
19/01/03 14:20:17 INFO TaskSetManager: Finished task 81.0 in stage 1.0 (TID 146) in 3821 ms on localhost (executor driver) (137/200)
19/01/03 14:20:17 INFO StateStore: Env is not null
19/01/03 14:20:17 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:17 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@67fc4df0
19/01/03 14:20:17 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,87,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:17 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=87),dir = file:/tmp/ts-sink/state/0/87] for update
19/01/03 14:20:17 INFO StateStore: Env is not null
19/01/03 14:20:17 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:17 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@207da08c
19/01/03 14:20:17 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,87,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:17 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=87),dir = file:/tmp/ts-sink/state/0/87] for update
19/01/03 14:20:17 INFO ShuffleBlockFetcherIterator: Getting 2 non-empty blocks out of 10 blocks
19/01/03 14:20:17 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:17 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=82),dir=file:/tmp/ts-sink/state/0/82] to file file:/tmp/ts-sink/state/0/82/1.delta
19/01/03 14:20:17 INFO DataWritingSparkTask: Writer for partition 82 is committing.
19/01/03 14:20:17 INFO DataWritingSparkTask: Writer for partition 82 committed.
19/01/03 14:20:18 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=83),dir=file:/tmp/ts-sink/state/0/83] to file file:/tmp/ts-sink/state/0/83/1.delta
19/01/03 14:20:18 INFO DataWritingSparkTask: Writer for partition 83 is committing.
19/01/03 14:20:18 INFO DataWritingSparkTask: Writer for partition 83 committed.
19/01/03 14:20:18 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=87),dir=file:/tmp/ts-sink/state/0/87] to file file:/tmp/ts-sink/state/0/87/1.delta
19/01/03 14:20:18 INFO DataWritingSparkTask: Writer for partition 87 is committing.
19/01/03 14:20:18 INFO DataWritingSparkTask: Writer for partition 87 committed.
19/01/03 14:20:19 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=83),dir=file:/tmp/ts-sink/state/0/83]
19/01/03 14:20:19 INFO Executor: Finished task 83.0 in stage 1.0 (TID 148). 4627 bytes result sent to driver
19/01/03 14:20:19 INFO TaskSetManager: Starting task 89.0 in stage 1.0 (TID 151, localhost, executor driver, partition 89, ANY, 7754 bytes)
19/01/03 14:20:19 INFO Executor: Running task 89.0 in stage 1.0 (TID 151)
19/01/03 14:20:19 INFO TaskSetManager: Finished task 83.0 in stage 1.0 (TID 148) in 3705 ms on localhost (executor driver) (138/200)
19/01/03 14:20:19 INFO StateStore: Env is not null
19/01/03 14:20:19 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:19 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@42fa1a8e
19/01/03 14:20:19 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,89,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:19 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=89),dir = file:/tmp/ts-sink/state/0/89] for update
19/01/03 14:20:19 INFO StateStore: Env is not null
19/01/03 14:20:19 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:19 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@bd8fa5d
19/01/03 14:20:19 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,89,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:19 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=89),dir = file:/tmp/ts-sink/state/0/89] for update
19/01/03 14:20:19 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:19 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:20 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=84),dir=file:/tmp/ts-sink/state/0/84] to file file:/tmp/ts-sink/state/0/84/1.delta
19/01/03 14:20:20 INFO DataWritingSparkTask: Writer for partition 84 is committing.
19/01/03 14:20:20 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=87),dir=file:/tmp/ts-sink/state/0/87]
19/01/03 14:20:20 INFO Executor: Finished task 87.0 in stage 1.0 (TID 150). 4584 bytes result sent to driver
19/01/03 14:20:20 INFO TaskSetManager: Starting task 90.0 in stage 1.0 (TID 152, localhost, executor driver, partition 90, ANY, 7754 bytes)
19/01/03 14:20:20 INFO Executor: Running task 90.0 in stage 1.0 (TID 152)
19/01/03 14:20:20 INFO StateStore: Env is not null
19/01/03 14:20:20 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:20 INFO TaskSetManager: Finished task 87.0 in stage 1.0 (TID 150) in 3720 ms on localhost (executor driver) (139/200)
19/01/03 14:20:20 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@2372efa
19/01/03 14:20:20 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,90,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:20 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=90),dir = file:/tmp/ts-sink/state/0/90] for update
19/01/03 14:20:20 INFO StateStore: Env is not null
19/01/03 14:20:20 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:20 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@3865afa0
19/01/03 14:20:20 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,90,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:20 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=90),dir = file:/tmp/ts-sink/state/0/90] for update
19/01/03 14:20:20 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:20 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:20 INFO DataWritingSparkTask: Writer for partition 84 committed.
19/01/03 14:20:21 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=82),dir=file:/tmp/ts-sink/state/0/82]
19/01/03 14:20:21 INFO Executor: Finished task 82.0 in stage 1.0 (TID 147). 4627 bytes result sent to driver
19/01/03 14:20:21 INFO TaskSetManager: Starting task 95.0 in stage 1.0 (TID 153, localhost, executor driver, partition 95, ANY, 7754 bytes)
19/01/03 14:20:21 INFO TaskSetManager: Finished task 82.0 in stage 1.0 (TID 147) in 7819 ms on localhost (executor driver) (140/200)
19/01/03 14:20:21 INFO Executor: Running task 95.0 in stage 1.0 (TID 153)
19/01/03 14:20:21 INFO StateStore: Env is not null
19/01/03 14:20:21 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:21 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@145dbe84
19/01/03 14:20:21 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,95,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:21 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=95),dir = file:/tmp/ts-sink/state/0/95] for update
19/01/03 14:20:21 INFO StateStore: Env is not null
19/01/03 14:20:21 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:21 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@33754da7
19/01/03 14:20:21 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,95,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:21 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=95),dir = file:/tmp/ts-sink/state/0/95] for update
19/01/03 14:20:21 INFO ShuffleBlockFetcherIterator: Getting 2 non-empty blocks out of 10 blocks
19/01/03 14:20:21 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:22 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=89),dir=file:/tmp/ts-sink/state/0/89] to file file:/tmp/ts-sink/state/0/89/1.delta
19/01/03 14:20:22 INFO DataWritingSparkTask: Writer for partition 89 is committing.
19/01/03 14:20:22 INFO DataWritingSparkTask: Writer for partition 89 committed.
19/01/03 14:20:23 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=84),dir=file:/tmp/ts-sink/state/0/84]
19/01/03 14:20:23 INFO Executor: Finished task 84.0 in stage 1.0 (TID 149). 4584 bytes result sent to driver
19/01/03 14:20:23 INFO TaskSetManager: Starting task 96.0 in stage 1.0 (TID 154, localhost, executor driver, partition 96, ANY, 7754 bytes)
19/01/03 14:20:23 INFO Executor: Running task 96.0 in stage 1.0 (TID 154)
19/01/03 14:20:23 INFO TaskSetManager: Finished task 84.0 in stage 1.0 (TID 149) in 6216 ms on localhost (executor driver) (141/200)
19/01/03 14:20:23 INFO StateStore: Env is not null
19/01/03 14:20:23 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:23 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@291195af
19/01/03 14:20:23 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,96,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:23 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=96),dir = file:/tmp/ts-sink/state/0/96] for update
19/01/03 14:20:23 INFO StateStore: Env is not null
19/01/03 14:20:23 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:23 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@460f4969
19/01/03 14:20:23 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,96,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:23 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=96),dir = file:/tmp/ts-sink/state/0/96] for update
19/01/03 14:20:23 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:23 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:23 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=89),dir=file:/tmp/ts-sink/state/0/89]
19/01/03 14:20:23 INFO Executor: Finished task 89.0 in stage 1.0 (TID 151). 4584 bytes result sent to driver
19/01/03 14:20:23 INFO TaskSetManager: Starting task 97.0 in stage 1.0 (TID 155, localhost, executor driver, partition 97, ANY, 7754 bytes)
19/01/03 14:20:23 INFO Executor: Running task 97.0 in stage 1.0 (TID 155)
19/01/03 14:20:23 INFO TaskSetManager: Finished task 89.0 in stage 1.0 (TID 151) in 4100 ms on localhost (executor driver) (142/200)
19/01/03 14:20:23 INFO StateStore: Env is not null
19/01/03 14:20:23 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:23 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@1901642f
19/01/03 14:20:23 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,97,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:23 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=97),dir = file:/tmp/ts-sink/state/0/97] for update
19/01/03 14:20:23 INFO StateStore: Env is not null
19/01/03 14:20:23 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:23 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@3086374c
19/01/03 14:20:23 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,97,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:23 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=97),dir = file:/tmp/ts-sink/state/0/97] for update
19/01/03 14:20:23 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:23 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:24 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=90),dir=file:/tmp/ts-sink/state/0/90] to file file:/tmp/ts-sink/state/0/90/1.delta
19/01/03 14:20:24 INFO DataWritingSparkTask: Writer for partition 90 is committing.
19/01/03 14:20:24 INFO DataWritingSparkTask: Writer for partition 90 committed.
19/01/03 14:20:24 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=96),dir=file:/tmp/ts-sink/state/0/96] to file file:/tmp/ts-sink/state/0/96/1.delta
19/01/03 14:20:24 INFO DataWritingSparkTask: Writer for partition 96 is committing.
19/01/03 14:20:24 INFO DataWritingSparkTask: Writer for partition 96 committed.
19/01/03 14:20:25 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=95),dir=file:/tmp/ts-sink/state/0/95] to file file:/tmp/ts-sink/state/0/95/1.delta
19/01/03 14:20:25 INFO DataWritingSparkTask: Writer for partition 95 is committing.
19/01/03 14:20:25 INFO DataWritingSparkTask: Writer for partition 95 committed.
19/01/03 14:20:25 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=97),dir=file:/tmp/ts-sink/state/0/97] to file file:/tmp/ts-sink/state/0/97/1.delta
19/01/03 14:20:25 INFO DataWritingSparkTask: Writer for partition 97 is committing.
19/01/03 14:20:25 INFO DataWritingSparkTask: Writer for partition 97 committed.
19/01/03 14:20:26 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=96),dir=file:/tmp/ts-sink/state/0/96]
19/01/03 14:20:26 INFO Executor: Finished task 96.0 in stage 1.0 (TID 154). 4584 bytes result sent to driver
19/01/03 14:20:26 INFO TaskSetManager: Starting task 102.0 in stage 1.0 (TID 156, localhost, executor driver, partition 102, ANY, 7754 bytes)
19/01/03 14:20:26 INFO Executor: Running task 102.0 in stage 1.0 (TID 156)
19/01/03 14:20:26 INFO TaskSetManager: Finished task 96.0 in stage 1.0 (TID 154) in 3740 ms on localhost (executor driver) (143/200)
19/01/03 14:20:26 INFO StateStore: Env is not null
19/01/03 14:20:26 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:26 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@cbe00c8
19/01/03 14:20:26 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,102,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:26 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=102),dir = file:/tmp/ts-sink/state/0/102] for update
19/01/03 14:20:26 INFO StateStore: Env is not null
19/01/03 14:20:26 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:26 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@4aadf759
19/01/03 14:20:26 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,102,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:26 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=102),dir = file:/tmp/ts-sink/state/0/102] for update
19/01/03 14:20:26 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:26 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:27 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=97),dir=file:/tmp/ts-sink/state/0/97]
19/01/03 14:20:27 INFO Executor: Finished task 97.0 in stage 1.0 (TID 155). 4584 bytes result sent to driver
19/01/03 14:20:27 INFO TaskSetManager: Starting task 106.0 in stage 1.0 (TID 157, localhost, executor driver, partition 106, ANY, 7754 bytes)
19/01/03 14:20:27 INFO Executor: Running task 106.0 in stage 1.0 (TID 157)
19/01/03 14:20:27 INFO TaskSetManager: Finished task 97.0 in stage 1.0 (TID 155) in 3703 ms on localhost (executor driver) (144/200)
19/01/03 14:20:27 INFO StateStore: Env is not null
19/01/03 14:20:27 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:27 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@72bbf9f5
19/01/03 14:20:27 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,106,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:27 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=106),dir = file:/tmp/ts-sink/state/0/106] for update
19/01/03 14:20:27 INFO StateStore: Env is not null
19/01/03 14:20:27 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:27 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@1798ec99
19/01/03 14:20:27 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,106,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:27 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=106),dir = file:/tmp/ts-sink/state/0/106] for update
19/01/03 14:20:27 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:27 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:28 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=90),dir=file:/tmp/ts-sink/state/0/90]
19/01/03 14:20:28 INFO Executor: Finished task 90.0 in stage 1.0 (TID 152). 4584 bytes result sent to driver
19/01/03 14:20:28 INFO TaskSetManager: Starting task 109.0 in stage 1.0 (TID 158, localhost, executor driver, partition 109, ANY, 7754 bytes)
19/01/03 14:20:28 INFO Executor: Running task 109.0 in stage 1.0 (TID 158)
19/01/03 14:20:28 INFO TaskSetManager: Finished task 90.0 in stage 1.0 (TID 152) in 7548 ms on localhost (executor driver) (145/200)
19/01/03 14:20:28 INFO StateStore: Env is not null
19/01/03 14:20:28 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:28 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@7572168
19/01/03 14:20:28 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,109,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:28 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=109),dir = file:/tmp/ts-sink/state/0/109] for update
19/01/03 14:20:28 INFO StateStore: Env is not null
19/01/03 14:20:28 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:28 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@785decc7
19/01/03 14:20:28 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,109,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:28 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=109),dir = file:/tmp/ts-sink/state/0/109] for update
19/01/03 14:20:28 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:28 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:28 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=102),dir=file:/tmp/ts-sink/state/0/102] to file file:/tmp/ts-sink/state/0/102/1.delta
19/01/03 14:20:28 INFO DataWritingSparkTask: Writer for partition 102 is committing.
19/01/03 14:20:28 INFO DataWritingSparkTask: Writer for partition 102 committed.
19/01/03 14:20:29 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=95),dir=file:/tmp/ts-sink/state/0/95]
19/01/03 14:20:29 INFO Executor: Finished task 95.0 in stage 1.0 (TID 153). 4584 bytes result sent to driver
19/01/03 14:20:29 INFO TaskSetManager: Starting task 111.0 in stage 1.0 (TID 159, localhost, executor driver, partition 111, ANY, 7754 bytes)
19/01/03 14:20:29 INFO Executor: Running task 111.0 in stage 1.0 (TID 159)
19/01/03 14:20:29 INFO TaskSetManager: Finished task 95.0 in stage 1.0 (TID 153) in 7560 ms on localhost (executor driver) (146/200)
19/01/03 14:20:29 INFO StateStore: Env is not null
19/01/03 14:20:29 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:29 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@32b44e6e
19/01/03 14:20:29 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,111,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:29 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=111),dir = file:/tmp/ts-sink/state/0/111] for update
19/01/03 14:20:29 INFO StateStore: Env is not null
19/01/03 14:20:29 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:29 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@669ae82
19/01/03 14:20:29 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,111,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:29 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=111),dir = file:/tmp/ts-sink/state/0/111] for update
19/01/03 14:20:29 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:29 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:29 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=106),dir=file:/tmp/ts-sink/state/0/106] to file file:/tmp/ts-sink/state/0/106/1.delta
19/01/03 14:20:29 INFO DataWritingSparkTask: Writer for partition 106 is committing.
19/01/03 14:20:29 INFO DataWritingSparkTask: Writer for partition 106 committed.
19/01/03 14:20:30 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=102),dir=file:/tmp/ts-sink/state/0/102]
19/01/03 14:20:30 INFO Executor: Finished task 102.0 in stage 1.0 (TID 156). 4584 bytes result sent to driver
19/01/03 14:20:30 INFO TaskSetManager: Starting task 113.0 in stage 1.0 (TID 160, localhost, executor driver, partition 113, ANY, 7754 bytes)
19/01/03 14:20:30 INFO Executor: Running task 113.0 in stage 1.0 (TID 160)
19/01/03 14:20:30 INFO TaskSetManager: Finished task 102.0 in stage 1.0 (TID 156) in 3761 ms on localhost (executor driver) (147/200)
19/01/03 14:20:30 INFO StateStore: Env is not null
19/01/03 14:20:30 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:30 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@7e77d586
19/01/03 14:20:30 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,113,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:30 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=113),dir = file:/tmp/ts-sink/state/0/113] for update
19/01/03 14:20:30 INFO StateStore: Env is not null
19/01/03 14:20:30 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:30 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@15ad9408
19/01/03 14:20:30 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,113,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:30 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=113),dir = file:/tmp/ts-sink/state/0/113] for update
19/01/03 14:20:30 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:30 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:31 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=106),dir=file:/tmp/ts-sink/state/0/106]
19/01/03 14:20:31 INFO Executor: Finished task 106.0 in stage 1.0 (TID 157). 4584 bytes result sent to driver
19/01/03 14:20:31 INFO TaskSetManager: Starting task 114.0 in stage 1.0 (TID 161, localhost, executor driver, partition 114, ANY, 7754 bytes)
19/01/03 14:20:31 INFO TaskSetManager: Finished task 106.0 in stage 1.0 (TID 157) in 3775 ms on localhost (executor driver) (148/200)
19/01/03 14:20:31 INFO Executor: Running task 114.0 in stage 1.0 (TID 161)
19/01/03 14:20:31 INFO StateStore: Env is not null
19/01/03 14:20:31 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:31 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@753d730d
19/01/03 14:20:31 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,114,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:31 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=114),dir = file:/tmp/ts-sink/state/0/114] for update
19/01/03 14:20:31 INFO StateStore: Env is not null
19/01/03 14:20:31 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:31 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@d6f6931
19/01/03 14:20:31 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,114,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:31 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=114),dir = file:/tmp/ts-sink/state/0/114] for update
19/01/03 14:20:31 INFO ShuffleBlockFetcherIterator: Getting 2 non-empty blocks out of 10 blocks
19/01/03 14:20:31 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:32 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=109),dir=file:/tmp/ts-sink/state/0/109] to file file:/tmp/ts-sink/state/0/109/1.delta
19/01/03 14:20:32 INFO DataWritingSparkTask: Writer for partition 109 is committing.
19/01/03 14:20:32 INFO DataWritingSparkTask: Writer for partition 109 committed.
19/01/03 14:20:32 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=113),dir=file:/tmp/ts-sink/state/0/113] to file file:/tmp/ts-sink/state/0/113/1.delta
19/01/03 14:20:32 INFO DataWritingSparkTask: Writer for partition 113 is committing.
19/01/03 14:20:32 INFO DataWritingSparkTask: Writer for partition 113 committed.
19/01/03 14:20:32 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=111),dir=file:/tmp/ts-sink/state/0/111] to file file:/tmp/ts-sink/state/0/111/1.delta
19/01/03 14:20:32 INFO DataWritingSparkTask: Writer for partition 111 is committing.
19/01/03 14:20:32 INFO DataWritingSparkTask: Writer for partition 111 committed.
19/01/03 14:20:33 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=114),dir=file:/tmp/ts-sink/state/0/114] to file file:/tmp/ts-sink/state/0/114/1.delta
19/01/03 14:20:33 INFO DataWritingSparkTask: Writer for partition 114 is committing.
19/01/03 14:20:33 INFO DataWritingSparkTask: Writer for partition 114 committed.
19/01/03 14:20:34 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=113),dir=file:/tmp/ts-sink/state/0/113]
19/01/03 14:20:34 INFO Executor: Finished task 113.0 in stage 1.0 (TID 160). 4584 bytes result sent to driver
19/01/03 14:20:34 INFO TaskSetManager: Starting task 116.0 in stage 1.0 (TID 162, localhost, executor driver, partition 116, ANY, 7754 bytes)
19/01/03 14:20:34 INFO Executor: Running task 116.0 in stage 1.0 (TID 162)
19/01/03 14:20:34 INFO TaskSetManager: Finished task 113.0 in stage 1.0 (TID 160) in 3736 ms on localhost (executor driver) (149/200)
19/01/03 14:20:34 INFO StateStore: Env is not null
19/01/03 14:20:34 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:34 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@2ad02675
19/01/03 14:20:34 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,116,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:34 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=116),dir = file:/tmp/ts-sink/state/0/116] for update
19/01/03 14:20:34 INFO StateStore: Env is not null
19/01/03 14:20:34 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:34 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@6e692c5d
19/01/03 14:20:34 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,116,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:34 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=116),dir = file:/tmp/ts-sink/state/0/116] for update
19/01/03 14:20:34 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:34 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:35 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=114),dir=file:/tmp/ts-sink/state/0/114]
19/01/03 14:20:35 INFO Executor: Finished task 114.0 in stage 1.0 (TID 161). 4584 bytes result sent to driver
19/01/03 14:20:35 INFO TaskSetManager: Starting task 117.0 in stage 1.0 (TID 163, localhost, executor driver, partition 117, ANY, 7754 bytes)
19/01/03 14:20:35 INFO Executor: Running task 117.0 in stage 1.0 (TID 163)
19/01/03 14:20:35 INFO StateStore: Env is not null
19/01/03 14:20:35 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:35 INFO TaskSetManager: Finished task 114.0 in stage 1.0 (TID 161) in 3737 ms on localhost (executor driver) (150/200)
19/01/03 14:20:35 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@4214271e
19/01/03 14:20:35 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,117,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:35 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=117),dir = file:/tmp/ts-sink/state/0/117] for update
19/01/03 14:20:35 INFO StateStore: Env is not null
19/01/03 14:20:35 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:35 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@1769fcf
19/01/03 14:20:35 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,117,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:35 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=117),dir = file:/tmp/ts-sink/state/0/117] for update
19/01/03 14:20:35 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:35 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:35 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=109),dir=file:/tmp/ts-sink/state/0/109]
19/01/03 14:20:35 INFO Executor: Finished task 109.0 in stage 1.0 (TID 158). 4584 bytes result sent to driver
19/01/03 14:20:35 INFO TaskSetManager: Starting task 119.0 in stage 1.0 (TID 164, localhost, executor driver, partition 119, ANY, 7754 bytes)
19/01/03 14:20:35 INFO Executor: Running task 119.0 in stage 1.0 (TID 164)
19/01/03 14:20:35 INFO TaskSetManager: Finished task 109.0 in stage 1.0 (TID 158) in 7421 ms on localhost (executor driver) (151/200)
19/01/03 14:20:35 INFO StateStore: Env is not null
19/01/03 14:20:35 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:35 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@1b410299
19/01/03 14:20:35 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,119,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:35 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=119),dir = file:/tmp/ts-sink/state/0/119] for update
19/01/03 14:20:35 INFO StateStore: Env is not null
19/01/03 14:20:35 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:35 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@335fb71e
19/01/03 14:20:35 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,119,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:35 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=119),dir = file:/tmp/ts-sink/state/0/119] for update
19/01/03 14:20:35 INFO ShuffleBlockFetcherIterator: Getting 3 non-empty blocks out of 10 blocks
19/01/03 14:20:35 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:36 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=116),dir=file:/tmp/ts-sink/state/0/116] to file file:/tmp/ts-sink/state/0/116/1.delta
19/01/03 14:20:36 INFO DataWritingSparkTask: Writer for partition 116 is committing.
19/01/03 14:20:36 INFO DataWritingSparkTask: Writer for partition 116 committed.
19/01/03 14:20:36 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=111),dir=file:/tmp/ts-sink/state/0/111]
19/01/03 14:20:36 INFO Executor: Finished task 111.0 in stage 1.0 (TID 159). 4584 bytes result sent to driver
19/01/03 14:20:36 INFO TaskSetManager: Starting task 121.0 in stage 1.0 (TID 165, localhost, executor driver, partition 121, ANY, 7754 bytes)
19/01/03 14:20:36 INFO Executor: Running task 121.0 in stage 1.0 (TID 165)
19/01/03 14:20:36 INFO TaskSetManager: Finished task 111.0 in stage 1.0 (TID 159) in 7437 ms on localhost (executor driver) (152/200)
19/01/03 14:20:36 INFO StateStore: Env is not null
19/01/03 14:20:36 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:36 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@4d27c578
19/01/03 14:20:36 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,121,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:36 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=121),dir = file:/tmp/ts-sink/state/0/121] for update
19/01/03 14:20:36 INFO StateStore: Env is not null
19/01/03 14:20:36 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:36 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@4d8c73c9
19/01/03 14:20:36 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,121,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:36 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=121),dir = file:/tmp/ts-sink/state/0/121] for update
19/01/03 14:20:36 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:36 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:37 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=117),dir=file:/tmp/ts-sink/state/0/117] to file file:/tmp/ts-sink/state/0/117/1.delta
19/01/03 14:20:37 INFO DataWritingSparkTask: Writer for partition 117 is committing.
19/01/03 14:20:37 INFO DataWritingSparkTask: Writer for partition 117 committed.
19/01/03 14:20:37 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=116),dir=file:/tmp/ts-sink/state/0/116]
19/01/03 14:20:37 INFO Executor: Finished task 116.0 in stage 1.0 (TID 162). 4584 bytes result sent to driver
19/01/03 14:20:37 INFO TaskSetManager: Starting task 122.0 in stage 1.0 (TID 166, localhost, executor driver, partition 122, ANY, 7754 bytes)
19/01/03 14:20:37 INFO Executor: Running task 122.0 in stage 1.0 (TID 166)
19/01/03 14:20:37 INFO TaskSetManager: Finished task 116.0 in stage 1.0 (TID 162) in 3694 ms on localhost (executor driver) (153/200)
19/01/03 14:20:37 INFO StateStore: Env is not null
19/01/03 14:20:37 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:37 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@52a16e98
19/01/03 14:20:37 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,122,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:37 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=122),dir = file:/tmp/ts-sink/state/0/122] for update
19/01/03 14:20:37 INFO StateStore: Env is not null
19/01/03 14:20:37 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:37 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@5d70f815
19/01/03 14:20:37 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,122,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:37 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=122),dir = file:/tmp/ts-sink/state/0/122] for update
19/01/03 14:20:37 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:37 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:38 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=117),dir=file:/tmp/ts-sink/state/0/117]
19/01/03 14:20:38 INFO Executor: Finished task 117.0 in stage 1.0 (TID 163). 4584 bytes result sent to driver
19/01/03 14:20:38 INFO TaskSetManager: Starting task 125.0 in stage 1.0 (TID 167, localhost, executor driver, partition 125, ANY, 7754 bytes)
19/01/03 14:20:38 INFO Executor: Running task 125.0 in stage 1.0 (TID 167)
19/01/03 14:20:38 INFO StateStore: Env is not null
19/01/03 14:20:38 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:38 INFO TaskSetManager: Finished task 117.0 in stage 1.0 (TID 163) in 3709 ms on localhost (executor driver) (154/200)
19/01/03 14:20:38 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@162ab969
19/01/03 14:20:38 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,125,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:38 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=125),dir = file:/tmp/ts-sink/state/0/125] for update
19/01/03 14:20:38 INFO StateStore: Env is not null
19/01/03 14:20:38 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:38 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@242a329d
19/01/03 14:20:38 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,125,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:38 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=125),dir = file:/tmp/ts-sink/state/0/125] for update
19/01/03 14:20:38 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:38 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:39 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=119),dir=file:/tmp/ts-sink/state/0/119] to file file:/tmp/ts-sink/state/0/119/1.delta
19/01/03 14:20:39 INFO DataWritingSparkTask: Writer for partition 119 is committing.
19/01/03 14:20:39 INFO DataWritingSparkTask: Writer for partition 119 committed.
19/01/03 14:20:39 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=122),dir=file:/tmp/ts-sink/state/0/122] to file file:/tmp/ts-sink/state/0/122/1.delta
19/01/03 14:20:39 INFO DataWritingSparkTask: Writer for partition 122 is committing.
19/01/03 14:20:39 INFO DataWritingSparkTask: Writer for partition 122 committed.
19/01/03 14:20:40 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=121),dir=file:/tmp/ts-sink/state/0/121] to file file:/tmp/ts-sink/state/0/121/1.delta
19/01/03 14:20:40 INFO DataWritingSparkTask: Writer for partition 121 is committing.
19/01/03 14:20:40 INFO DataWritingSparkTask: Writer for partition 121 committed.
19/01/03 14:20:40 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=125),dir=file:/tmp/ts-sink/state/0/125] to file file:/tmp/ts-sink/state/0/125/1.delta
19/01/03 14:20:40 INFO DataWritingSparkTask: Writer for partition 125 is committing.
19/01/03 14:20:40 INFO DataWritingSparkTask: Writer for partition 125 committed.
19/01/03 14:20:41 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=122),dir=file:/tmp/ts-sink/state/0/122]
19/01/03 14:20:41 INFO Executor: Finished task 122.0 in stage 1.0 (TID 166). 4584 bytes result sent to driver
19/01/03 14:20:41 INFO TaskSetManager: Starting task 126.0 in stage 1.0 (TID 168, localhost, executor driver, partition 126, ANY, 7754 bytes)
19/01/03 14:20:41 INFO Executor: Running task 126.0 in stage 1.0 (TID 168)
19/01/03 14:20:41 INFO TaskSetManager: Finished task 122.0 in stage 1.0 (TID 166) in 3780 ms on localhost (executor driver) (155/200)
19/01/03 14:20:41 INFO StateStore: Env is not null
19/01/03 14:20:41 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:41 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@40dbef6a
19/01/03 14:20:41 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,126,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:41 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=126),dir = file:/tmp/ts-sink/state/0/126] for update
19/01/03 14:20:41 INFO StateStore: Env is not null
19/01/03 14:20:41 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:41 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@728855bc
19/01/03 14:20:41 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,126,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:41 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=126),dir = file:/tmp/ts-sink/state/0/126] for update
19/01/03 14:20:41 INFO ShuffleBlockFetcherIterator: Getting 2 non-empty blocks out of 10 blocks
19/01/03 14:20:41 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:42 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=125),dir=file:/tmp/ts-sink/state/0/125]
19/01/03 14:20:42 INFO Executor: Finished task 125.0 in stage 1.0 (TID 167). 4584 bytes result sent to driver
19/01/03 14:20:42 INFO TaskSetManager: Starting task 127.0 in stage 1.0 (TID 169, localhost, executor driver, partition 127, ANY, 7754 bytes)
19/01/03 14:20:42 INFO TaskSetManager: Finished task 125.0 in stage 1.0 (TID 167) in 3768 ms on localhost (executor driver) (156/200)
19/01/03 14:20:42 INFO Executor: Running task 127.0 in stage 1.0 (TID 169)
19/01/03 14:20:42 INFO StateStore: Env is not null
19/01/03 14:20:42 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:42 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@e2738b7
19/01/03 14:20:42 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,127,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:42 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=127),dir = file:/tmp/ts-sink/state/0/127] for update
19/01/03 14:20:42 INFO StateStore: Env is not null
19/01/03 14:20:42 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:42 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@dc8ad12
19/01/03 14:20:42 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,127,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:42 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=127),dir = file:/tmp/ts-sink/state/0/127] for update
19/01/03 14:20:42 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:42 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:43 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=119),dir=file:/tmp/ts-sink/state/0/119]
19/01/03 14:20:43 INFO Executor: Finished task 119.0 in stage 1.0 (TID 164). 4584 bytes result sent to driver
19/01/03 14:20:43 INFO TaskSetManager: Starting task 129.0 in stage 1.0 (TID 170, localhost, executor driver, partition 129, ANY, 7754 bytes)
19/01/03 14:20:43 INFO Executor: Running task 129.0 in stage 1.0 (TID 170)
19/01/03 14:20:43 INFO TaskSetManager: Finished task 119.0 in stage 1.0 (TID 164) in 7467 ms on localhost (executor driver) (157/200)
19/01/03 14:20:43 INFO StateStore: Env is not null
19/01/03 14:20:43 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:43 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@1b538307
19/01/03 14:20:43 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,129,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:43 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=129),dir = file:/tmp/ts-sink/state/0/129] for update
19/01/03 14:20:43 INFO StateStore: Env is not null
19/01/03 14:20:43 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:43 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@2aed55bf
19/01/03 14:20:43 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,129,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:43 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=129),dir = file:/tmp/ts-sink/state/0/129] for update
19/01/03 14:20:43 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:43 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:43 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=126),dir=file:/tmp/ts-sink/state/0/126] to file file:/tmp/ts-sink/state/0/126/1.delta
19/01/03 14:20:43 INFO DataWritingSparkTask: Writer for partition 126 is committing.
19/01/03 14:20:43 INFO DataWritingSparkTask: Writer for partition 126 committed.
19/01/03 14:20:44 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=121),dir=file:/tmp/ts-sink/state/0/121]
19/01/03 14:20:44 INFO Executor: Finished task 121.0 in stage 1.0 (TID 165). 4584 bytes result sent to driver
19/01/03 14:20:44 INFO TaskSetManager: Starting task 132.0 in stage 1.0 (TID 171, localhost, executor driver, partition 132, ANY, 7754 bytes)
19/01/03 14:20:44 INFO Executor: Running task 132.0 in stage 1.0 (TID 171)
19/01/03 14:20:44 INFO TaskSetManager: Finished task 121.0 in stage 1.0 (TID 165) in 7468 ms on localhost (executor driver) (158/200)
19/01/03 14:20:44 INFO StateStore: Env is not null
19/01/03 14:20:44 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:44 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@15869473
19/01/03 14:20:44 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,132,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:44 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=132),dir = file:/tmp/ts-sink/state/0/132] for update
19/01/03 14:20:44 INFO StateStore: Env is not null
19/01/03 14:20:44 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:44 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@314d1f18
19/01/03 14:20:44 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,132,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:44 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=132),dir = file:/tmp/ts-sink/state/0/132] for update
19/01/03 14:20:44 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:44 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:44 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=127),dir=file:/tmp/ts-sink/state/0/127] to file file:/tmp/ts-sink/state/0/127/1.delta
19/01/03 14:20:44 INFO DataWritingSparkTask: Writer for partition 127 is committing.
19/01/03 14:20:44 INFO DataWritingSparkTask: Writer for partition 127 committed.
19/01/03 14:20:45 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=126),dir=file:/tmp/ts-sink/state/0/126]
19/01/03 14:20:45 INFO Executor: Finished task 126.0 in stage 1.0 (TID 168). 4584 bytes result sent to driver
19/01/03 14:20:45 INFO TaskSetManager: Starting task 134.0 in stage 1.0 (TID 172, localhost, executor driver, partition 134, ANY, 7754 bytes)
19/01/03 14:20:45 INFO Executor: Running task 134.0 in stage 1.0 (TID 172)
19/01/03 14:20:45 INFO TaskSetManager: Finished task 126.0 in stage 1.0 (TID 168) in 3718 ms on localhost (executor driver) (159/200)
19/01/03 14:20:45 INFO StateStore: Env is not null
19/01/03 14:20:45 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:45 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@51570054
19/01/03 14:20:45 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,134,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:45 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=134),dir = file:/tmp/ts-sink/state/0/134] for update
19/01/03 14:20:45 INFO StateStore: Env is not null
19/01/03 14:20:45 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:45 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@abe840
19/01/03 14:20:45 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,134,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:45 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=134),dir = file:/tmp/ts-sink/state/0/134] for update
19/01/03 14:20:45 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:45 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:46 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=127),dir=file:/tmp/ts-sink/state/0/127]
19/01/03 14:20:46 INFO Executor: Finished task 127.0 in stage 1.0 (TID 169). 4584 bytes result sent to driver
19/01/03 14:20:46 INFO TaskSetManager: Starting task 139.0 in stage 1.0 (TID 173, localhost, executor driver, partition 139, ANY, 7754 bytes)
19/01/03 14:20:46 INFO Executor: Running task 139.0 in stage 1.0 (TID 173)
19/01/03 14:20:46 INFO TaskSetManager: Finished task 127.0 in stage 1.0 (TID 169) in 3857 ms on localhost (executor driver) (160/200)
19/01/03 14:20:46 INFO StateStore: Env is not null
19/01/03 14:20:46 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:46 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@71af8b66
19/01/03 14:20:46 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,139,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:46 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=139),dir = file:/tmp/ts-sink/state/0/139] for update
19/01/03 14:20:46 INFO StateStore: Env is not null
19/01/03 14:20:46 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:46 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@3d6b2bb
19/01/03 14:20:46 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,139,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:46 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=139),dir = file:/tmp/ts-sink/state/0/139] for update
19/01/03 14:20:46 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:46 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:47 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=129),dir=file:/tmp/ts-sink/state/0/129] to file file:/tmp/ts-sink/state/0/129/1.delta
19/01/03 14:20:47 INFO DataWritingSparkTask: Writer for partition 129 is committing.
19/01/03 14:20:47 INFO DataWritingSparkTask: Writer for partition 129 committed.
19/01/03 14:20:47 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=134),dir=file:/tmp/ts-sink/state/0/134] to file file:/tmp/ts-sink/state/0/134/1.delta
19/01/03 14:20:47 INFO DataWritingSparkTask: Writer for partition 134 is committing.
19/01/03 14:20:47 INFO DataWritingSparkTask: Writer for partition 134 committed.
19/01/03 14:20:48 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=132),dir=file:/tmp/ts-sink/state/0/132] to file file:/tmp/ts-sink/state/0/132/1.delta
19/01/03 14:20:48 INFO DataWritingSparkTask: Writer for partition 132 is committing.
19/01/03 14:20:48 INFO DataWritingSparkTask: Writer for partition 132 committed.
19/01/03 14:20:48 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=139),dir=file:/tmp/ts-sink/state/0/139] to file file:/tmp/ts-sink/state/0/139/1.delta
19/01/03 14:20:48 INFO DataWritingSparkTask: Writer for partition 139 is committing.
19/01/03 14:20:48 INFO DataWritingSparkTask: Writer for partition 139 committed.
19/01/03 14:20:49 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=134),dir=file:/tmp/ts-sink/state/0/134]
19/01/03 14:20:49 INFO Executor: Finished task 134.0 in stage 1.0 (TID 172). 4584 bytes result sent to driver
19/01/03 14:20:49 INFO TaskSetManager: Starting task 142.0 in stage 1.0 (TID 174, localhost, executor driver, partition 142, ANY, 7754 bytes)
19/01/03 14:20:49 INFO Executor: Running task 142.0 in stage 1.0 (TID 174)
19/01/03 14:20:49 INFO TaskSetManager: Finished task 134.0 in stage 1.0 (TID 172) in 3853 ms on localhost (executor driver) (161/200)
19/01/03 14:20:49 INFO StateStore: Env is not null
19/01/03 14:20:49 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:49 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@139d7419
19/01/03 14:20:49 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,142,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:49 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=142),dir = file:/tmp/ts-sink/state/0/142] for update
19/01/03 14:20:49 INFO StateStore: Env is not null
19/01/03 14:20:49 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:49 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@608e4d1e
19/01/03 14:20:49 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,142,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:49 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=142),dir = file:/tmp/ts-sink/state/0/142] for update
19/01/03 14:20:49 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:49 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:50 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=139),dir=file:/tmp/ts-sink/state/0/139]
19/01/03 14:20:50 INFO Executor: Finished task 139.0 in stage 1.0 (TID 173). 4627 bytes result sent to driver
19/01/03 14:20:50 INFO TaskSetManager: Starting task 144.0 in stage 1.0 (TID 175, localhost, executor driver, partition 144, ANY, 7754 bytes)
19/01/03 14:20:50 INFO Executor: Running task 144.0 in stage 1.0 (TID 175)
19/01/03 14:20:50 INFO TaskSetManager: Finished task 139.0 in stage 1.0 (TID 173) in 3741 ms on localhost (executor driver) (162/200)
19/01/03 14:20:50 INFO StateStore: Env is not null
19/01/03 14:20:50 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:50 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@14d7b513
19/01/03 14:20:50 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,144,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:50 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=144),dir = file:/tmp/ts-sink/state/0/144] for update
19/01/03 14:20:50 INFO StateStore: Env is not null
19/01/03 14:20:50 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:50 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@6021da59
19/01/03 14:20:50 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,144,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:50 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=144),dir = file:/tmp/ts-sink/state/0/144] for update
19/01/03 14:20:50 INFO ShuffleBlockFetcherIterator: Getting 2 non-empty blocks out of 10 blocks
19/01/03 14:20:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:50 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=129),dir=file:/tmp/ts-sink/state/0/129]
19/01/03 14:20:50 INFO Executor: Finished task 129.0 in stage 1.0 (TID 170). 4584 bytes result sent to driver
19/01/03 14:20:50 INFO TaskSetManager: Starting task 145.0 in stage 1.0 (TID 176, localhost, executor driver, partition 145, ANY, 7754 bytes)
19/01/03 14:20:50 INFO Executor: Running task 145.0 in stage 1.0 (TID 176)
19/01/03 14:20:50 INFO TaskSetManager: Finished task 129.0 in stage 1.0 (TID 170) in 7614 ms on localhost (executor driver) (163/200)
19/01/03 14:20:50 INFO StateStore: Env is not null
19/01/03 14:20:50 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:50 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@7c0df0ff
19/01/03 14:20:50 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,145,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:50 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=145),dir = file:/tmp/ts-sink/state/0/145] for update
19/01/03 14:20:50 INFO StateStore: Env is not null
19/01/03 14:20:50 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:50 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@468eeb98
19/01/03 14:20:50 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,145,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:50 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=145),dir = file:/tmp/ts-sink/state/0/145] for update
19/01/03 14:20:50 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:50 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:51 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=142),dir=file:/tmp/ts-sink/state/0/142] to file file:/tmp/ts-sink/state/0/142/1.delta
19/01/03 14:20:51 INFO DataWritingSparkTask: Writer for partition 142 is committing.
19/01/03 14:20:51 INFO DataWritingSparkTask: Writer for partition 142 committed.
19/01/03 14:20:51 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=132),dir=file:/tmp/ts-sink/state/0/132]
19/01/03 14:20:51 INFO Executor: Finished task 132.0 in stage 1.0 (TID 171). 4584 bytes result sent to driver
19/01/03 14:20:51 INFO TaskSetManager: Starting task 146.0 in stage 1.0 (TID 177, localhost, executor driver, partition 146, ANY, 7754 bytes)
19/01/03 14:20:51 INFO Executor: Running task 146.0 in stage 1.0 (TID 177)
19/01/03 14:20:51 INFO TaskSetManager: Finished task 132.0 in stage 1.0 (TID 171) in 7764 ms on localhost (executor driver) (164/200)
19/01/03 14:20:51 INFO StateStore: Env is not null
19/01/03 14:20:51 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:51 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@24c6077
19/01/03 14:20:51 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,146,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:51 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=146),dir = file:/tmp/ts-sink/state/0/146] for update
19/01/03 14:20:51 INFO StateStore: Env is not null
19/01/03 14:20:51 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:51 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@5eec056a
19/01/03 14:20:51 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,146,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:51 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=146),dir = file:/tmp/ts-sink/state/0/146] for update
19/01/03 14:20:51 INFO ShuffleBlockFetcherIterator: Getting 3 non-empty blocks out of 10 blocks
19/01/03 14:20:51 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:52 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=144),dir=file:/tmp/ts-sink/state/0/144] to file file:/tmp/ts-sink/state/0/144/1.delta
19/01/03 14:20:52 INFO DataWritingSparkTask: Writer for partition 144 is committing.
19/01/03 14:20:52 INFO DataWritingSparkTask: Writer for partition 144 committed.
19/01/03 14:20:53 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=142),dir=file:/tmp/ts-sink/state/0/142]
19/01/03 14:20:53 INFO Executor: Finished task 142.0 in stage 1.0 (TID 174). 4584 bytes result sent to driver
19/01/03 14:20:53 INFO TaskSetManager: Starting task 147.0 in stage 1.0 (TID 178, localhost, executor driver, partition 147, ANY, 7754 bytes)
19/01/03 14:20:53 INFO Executor: Running task 147.0 in stage 1.0 (TID 178)
19/01/03 14:20:53 INFO TaskSetManager: Finished task 142.0 in stage 1.0 (TID 174) in 3925 ms on localhost (executor driver) (165/200)
19/01/03 14:20:53 INFO StateStore: Env is not null
19/01/03 14:20:53 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:53 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@270bb30a
19/01/03 14:20:53 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,147,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:53 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=147),dir = file:/tmp/ts-sink/state/0/147] for update
19/01/03 14:20:53 INFO StateStore: Env is not null
19/01/03 14:20:53 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:53 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@858352c
19/01/03 14:20:53 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,147,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:53 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=147),dir = file:/tmp/ts-sink/state/0/147] for update
19/01/03 14:20:53 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:53 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:54 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=144),dir=file:/tmp/ts-sink/state/0/144]
19/01/03 14:20:54 INFO Executor: Finished task 144.0 in stage 1.0 (TID 175). 4584 bytes result sent to driver
19/01/03 14:20:54 INFO TaskSetManager: Starting task 149.0 in stage 1.0 (TID 179, localhost, executor driver, partition 149, ANY, 7754 bytes)
19/01/03 14:20:54 INFO Executor: Running task 149.0 in stage 1.0 (TID 179)
19/01/03 14:20:54 INFO StateStore: Env is not null
19/01/03 14:20:54 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:54 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@34fbf426
19/01/03 14:20:54 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,149,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:54 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=149),dir = file:/tmp/ts-sink/state/0/149] for update
19/01/03 14:20:54 INFO StateStore: Env is not null
19/01/03 14:20:54 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:54 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@5883552b
19/01/03 14:20:54 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,149,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:54 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=149),dir = file:/tmp/ts-sink/state/0/149] for update
19/01/03 14:20:54 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:54 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:54 INFO TaskSetManager: Finished task 144.0 in stage 1.0 (TID 175) in 3982 ms on localhost (executor driver) (166/200)
19/01/03 14:20:54 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=145),dir=file:/tmp/ts-sink/state/0/145] to file file:/tmp/ts-sink/state/0/145/1.delta
19/01/03 14:20:54 INFO DataWritingSparkTask: Writer for partition 145 is committing.
19/01/03 14:20:54 INFO DataWritingSparkTask: Writer for partition 145 committed.
19/01/03 14:20:55 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=147),dir=file:/tmp/ts-sink/state/0/147] to file file:/tmp/ts-sink/state/0/147/1.delta
19/01/03 14:20:55 INFO DataWritingSparkTask: Writer for partition 147 is committing.
19/01/03 14:20:55 INFO DataWritingSparkTask: Writer for partition 147 committed.
19/01/03 14:20:55 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=146),dir=file:/tmp/ts-sink/state/0/146] to file file:/tmp/ts-sink/state/0/146/1.delta
19/01/03 14:20:55 INFO DataWritingSparkTask: Writer for partition 146 is committing.
19/01/03 14:20:55 INFO DataWritingSparkTask: Writer for partition 146 committed.
19/01/03 14:20:56 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=149),dir=file:/tmp/ts-sink/state/0/149] to file file:/tmp/ts-sink/state/0/149/1.delta
19/01/03 14:20:56 INFO DataWritingSparkTask: Writer for partition 149 is committing.
19/01/03 14:20:56 INFO DataWritingSparkTask: Writer for partition 149 committed.
19/01/03 14:20:57 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=147),dir=file:/tmp/ts-sink/state/0/147]
19/01/03 14:20:57 INFO Executor: Finished task 147.0 in stage 1.0 (TID 178). 4584 bytes result sent to driver
19/01/03 14:20:57 INFO TaskSetManager: Starting task 150.0 in stage 1.0 (TID 180, localhost, executor driver, partition 150, ANY, 7754 bytes)
19/01/03 14:20:57 INFO Executor: Running task 150.0 in stage 1.0 (TID 180)
19/01/03 14:20:57 INFO TaskSetManager: Finished task 147.0 in stage 1.0 (TID 178) in 4268 ms on localhost (executor driver) (167/200)
19/01/03 14:20:57 INFO StateStore: Env is not null
19/01/03 14:20:57 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:57 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@7613028d
19/01/03 14:20:57 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,150,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:57 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=150),dir = file:/tmp/ts-sink/state/0/150] for update
19/01/03 14:20:57 INFO StateStore: Env is not null
19/01/03 14:20:57 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:57 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@6b58de89
19/01/03 14:20:57 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,150,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:57 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=150),dir = file:/tmp/ts-sink/state/0/150] for update
19/01/03 14:20:57 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:57 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:58 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=149),dir=file:/tmp/ts-sink/state/0/149]
19/01/03 14:20:58 INFO Executor: Finished task 149.0 in stage 1.0 (TID 179). 4584 bytes result sent to driver
19/01/03 14:20:58 INFO TaskSetManager: Starting task 151.0 in stage 1.0 (TID 181, localhost, executor driver, partition 151, ANY, 7754 bytes)
19/01/03 14:20:58 INFO Executor: Running task 151.0 in stage 1.0 (TID 181)
19/01/03 14:20:58 INFO TaskSetManager: Finished task 149.0 in stage 1.0 (TID 179) in 4216 ms on localhost (executor driver) (168/200)
19/01/03 14:20:58 INFO StateStore: Env is not null
19/01/03 14:20:58 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:58 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@4db6ed76
19/01/03 14:20:58 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,151,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:58 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=151),dir = file:/tmp/ts-sink/state/0/151] for update
19/01/03 14:20:58 INFO StateStore: Env is not null
19/01/03 14:20:58 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:58 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@e9ccb89
19/01/03 14:20:58 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,151,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:58 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=151),dir = file:/tmp/ts-sink/state/0/151] for update
19/01/03 14:20:58 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:58 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:58 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=145),dir=file:/tmp/ts-sink/state/0/145]
19/01/03 14:20:58 INFO Executor: Finished task 145.0 in stage 1.0 (TID 176). 4584 bytes result sent to driver
19/01/03 14:20:58 INFO TaskSetManager: Starting task 153.0 in stage 1.0 (TID 182, localhost, executor driver, partition 153, ANY, 7754 bytes)
19/01/03 14:20:58 INFO Executor: Running task 153.0 in stage 1.0 (TID 182)
19/01/03 14:20:58 INFO TaskSetManager: Finished task 145.0 in stage 1.0 (TID 176) in 7879 ms on localhost (executor driver) (169/200)
19/01/03 14:20:58 INFO StateStore: Env is not null
19/01/03 14:20:58 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:58 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@12017185
19/01/03 14:20:58 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,153,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:58 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=153),dir = file:/tmp/ts-sink/state/0/153] for update
19/01/03 14:20:58 INFO StateStore: Env is not null
19/01/03 14:20:58 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:58 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@12583315
19/01/03 14:20:58 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,153,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:58 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=153),dir = file:/tmp/ts-sink/state/0/153] for update
19/01/03 14:20:58 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:58 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:20:59 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=150),dir=file:/tmp/ts-sink/state/0/150] to file file:/tmp/ts-sink/state/0/150/1.delta
19/01/03 14:20:59 INFO DataWritingSparkTask: Writer for partition 150 is committing.
19/01/03 14:20:59 INFO DataWritingSparkTask: Writer for partition 150 committed.
19/01/03 14:20:59 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=146),dir=file:/tmp/ts-sink/state/0/146]
19/01/03 14:20:59 INFO Executor: Finished task 146.0 in stage 1.0 (TID 177). 4584 bytes result sent to driver
19/01/03 14:20:59 INFO TaskSetManager: Starting task 154.0 in stage 1.0 (TID 183, localhost, executor driver, partition 154, ANY, 7754 bytes)
19/01/03 14:20:59 INFO Executor: Running task 154.0 in stage 1.0 (TID 183)
19/01/03 14:20:59 INFO TaskSetManager: Finished task 146.0 in stage 1.0 (TID 177) in 7751 ms on localhost (executor driver) (170/200)
19/01/03 14:20:59 INFO StateStore: Env is not null
19/01/03 14:20:59 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:59 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@350c0d41
19/01/03 14:20:59 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,154,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:59 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=154),dir = file:/tmp/ts-sink/state/0/154] for update
19/01/03 14:20:59 INFO StateStore: Env is not null
19/01/03 14:20:59 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:20:59 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@687d71d0
19/01/03 14:20:59 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,154,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:20:59 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=154),dir = file:/tmp/ts-sink/state/0/154] for update
19/01/03 14:20:59 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:20:59 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:00 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=151),dir=file:/tmp/ts-sink/state/0/151] to file file:/tmp/ts-sink/state/0/151/1.delta
19/01/03 14:21:00 INFO DataWritingSparkTask: Writer for partition 151 is committing.
19/01/03 14:21:00 INFO DataWritingSparkTask: Writer for partition 151 committed.
19/01/03 14:21:01 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=150),dir=file:/tmp/ts-sink/state/0/150]
19/01/03 14:21:01 INFO Executor: Finished task 150.0 in stage 1.0 (TID 180). 4627 bytes result sent to driver
19/01/03 14:21:01 INFO TaskSetManager: Starting task 155.0 in stage 1.0 (TID 184, localhost, executor driver, partition 155, ANY, 7754 bytes)
19/01/03 14:21:01 INFO Executor: Running task 155.0 in stage 1.0 (TID 184)
19/01/03 14:21:01 INFO TaskSetManager: Finished task 150.0 in stage 1.0 (TID 180) in 3777 ms on localhost (executor driver) (171/200)
19/01/03 14:21:01 INFO StateStore: Env is not null
19/01/03 14:21:01 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:01 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@2add205c
19/01/03 14:21:01 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,155,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:01 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=155),dir = file:/tmp/ts-sink/state/0/155] for update
19/01/03 14:21:01 INFO StateStore: Env is not null
19/01/03 14:21:01 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:01 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@62be456c
19/01/03 14:21:01 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,155,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:01 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=155),dir = file:/tmp/ts-sink/state/0/155] for update
19/01/03 14:21:01 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:21:01 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:02 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=151),dir=file:/tmp/ts-sink/state/0/151]
19/01/03 14:21:02 INFO Executor: Finished task 151.0 in stage 1.0 (TID 181). 4584 bytes result sent to driver
19/01/03 14:21:02 INFO TaskSetManager: Starting task 158.0 in stage 1.0 (TID 185, localhost, executor driver, partition 158, ANY, 7754 bytes)
19/01/03 14:21:02 INFO Executor: Running task 158.0 in stage 1.0 (TID 185)
19/01/03 14:21:02 INFO TaskSetManager: Finished task 151.0 in stage 1.0 (TID 181) in 3770 ms on localhost (executor driver) (172/200)
19/01/03 14:21:02 INFO StateStore: Env is not null
19/01/03 14:21:02 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:02 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@352523bb
19/01/03 14:21:02 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,158,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:02 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=158),dir = file:/tmp/ts-sink/state/0/158] for update
19/01/03 14:21:02 INFO StateStore: Env is not null
19/01/03 14:21:02 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:02 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@62976949
19/01/03 14:21:02 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,158,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:02 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=158),dir = file:/tmp/ts-sink/state/0/158] for update
19/01/03 14:21:02 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:21:02 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:02 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=153),dir=file:/tmp/ts-sink/state/0/153] to file file:/tmp/ts-sink/state/0/153/1.delta
19/01/03 14:21:02 INFO DataWritingSparkTask: Writer for partition 153 is committing.
19/01/03 14:21:02 INFO DataWritingSparkTask: Writer for partition 153 committed.
19/01/03 14:21:03 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=155),dir=file:/tmp/ts-sink/state/0/155] to file file:/tmp/ts-sink/state/0/155/1.delta
19/01/03 14:21:03 INFO DataWritingSparkTask: Writer for partition 155 is committing.
19/01/03 14:21:03 INFO DataWritingSparkTask: Writer for partition 155 committed.
19/01/03 14:21:03 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=154),dir=file:/tmp/ts-sink/state/0/154] to file file:/tmp/ts-sink/state/0/154/1.delta
19/01/03 14:21:03 INFO DataWritingSparkTask: Writer for partition 154 is committing.
19/01/03 14:21:03 INFO DataWritingSparkTask: Writer for partition 154 committed.
19/01/03 14:21:04 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=158),dir=file:/tmp/ts-sink/state/0/158] to file file:/tmp/ts-sink/state/0/158/1.delta
19/01/03 14:21:04 INFO DataWritingSparkTask: Writer for partition 158 is committing.
19/01/03 14:21:04 INFO DataWritingSparkTask: Writer for partition 158 committed.
19/01/03 14:21:05 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=155),dir=file:/tmp/ts-sink/state/0/155]
19/01/03 14:21:05 INFO Executor: Finished task 155.0 in stage 1.0 (TID 184). 4584 bytes result sent to driver
19/01/03 14:21:05 INFO TaskSetManager: Starting task 160.0 in stage 1.0 (TID 186, localhost, executor driver, partition 160, ANY, 7754 bytes)
19/01/03 14:21:05 INFO Executor: Running task 160.0 in stage 1.0 (TID 186)
19/01/03 14:21:05 INFO TaskSetManager: Finished task 155.0 in stage 1.0 (TID 184) in 3749 ms on localhost (executor driver) (173/200)
19/01/03 14:21:05 INFO StateStore: Env is not null
19/01/03 14:21:05 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:05 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@45d7180a
19/01/03 14:21:05 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,160,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:05 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=160),dir = file:/tmp/ts-sink/state/0/160] for update
19/01/03 14:21:05 INFO StateStore: Env is not null
19/01/03 14:21:05 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:05 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@24bfd1a4
19/01/03 14:21:05 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,160,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:05 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=160),dir = file:/tmp/ts-sink/state/0/160] for update
19/01/03 14:21:05 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:21:05 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:05 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=158),dir=file:/tmp/ts-sink/state/0/158]
19/01/03 14:21:05 INFO Executor: Finished task 158.0 in stage 1.0 (TID 185). 4584 bytes result sent to driver
19/01/03 14:21:05 INFO TaskSetManager: Starting task 162.0 in stage 1.0 (TID 187, localhost, executor driver, partition 162, ANY, 7754 bytes)
19/01/03 14:21:05 INFO Executor: Running task 162.0 in stage 1.0 (TID 187)
19/01/03 14:21:05 INFO TaskSetManager: Finished task 158.0 in stage 1.0 (TID 185) in 3776 ms on localhost (executor driver) (174/200)
19/01/03 14:21:05 INFO StateStore: Env is not null
19/01/03 14:21:05 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:05 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@f27cb20
19/01/03 14:21:05 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,162,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:05 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=162),dir = file:/tmp/ts-sink/state/0/162] for update
19/01/03 14:21:05 INFO StateStore: Env is not null
19/01/03 14:21:05 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:05 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@4aaf6034
19/01/03 14:21:05 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,162,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:05 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=162),dir = file:/tmp/ts-sink/state/0/162] for update
19/01/03 14:21:05 INFO ShuffleBlockFetcherIterator: Getting 2 non-empty blocks out of 10 blocks
19/01/03 14:21:05 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:06 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=153),dir=file:/tmp/ts-sink/state/0/153]
19/01/03 14:21:06 INFO Executor: Finished task 153.0 in stage 1.0 (TID 182). 4584 bytes result sent to driver
19/01/03 14:21:06 INFO TaskSetManager: Starting task 163.0 in stage 1.0 (TID 188, localhost, executor driver, partition 163, ANY, 7754 bytes)
19/01/03 14:21:06 INFO Executor: Running task 163.0 in stage 1.0 (TID 188)
19/01/03 14:21:06 INFO TaskSetManager: Finished task 153.0 in stage 1.0 (TID 182) in 7537 ms on localhost (executor driver) (175/200)
19/01/03 14:21:06 INFO StateStore: Env is not null
19/01/03 14:21:06 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:06 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@18191a6
19/01/03 14:21:06 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,163,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:06 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=163),dir = file:/tmp/ts-sink/state/0/163] for update
19/01/03 14:21:06 INFO StateStore: Env is not null
19/01/03 14:21:06 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:06 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@5ddd9349
19/01/03 14:21:06 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,163,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:06 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=163),dir = file:/tmp/ts-sink/state/0/163] for update
19/01/03 14:21:06 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:21:06 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:06 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=160),dir=file:/tmp/ts-sink/state/0/160] to file file:/tmp/ts-sink/state/0/160/1.delta
19/01/03 14:21:06 INFO DataWritingSparkTask: Writer for partition 160 is committing.
19/01/03 14:21:06 INFO DataWritingSparkTask: Writer for partition 160 committed.
19/01/03 14:21:07 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=154),dir=file:/tmp/ts-sink/state/0/154]
19/01/03 14:21:07 INFO Executor: Finished task 154.0 in stage 1.0 (TID 183). 4584 bytes result sent to driver
19/01/03 14:21:07 INFO TaskSetManager: Starting task 164.0 in stage 1.0 (TID 189, localhost, executor driver, partition 164, ANY, 7754 bytes)
19/01/03 14:21:07 INFO TaskSetManager: Finished task 154.0 in stage 1.0 (TID 183) in 7545 ms on localhost (executor driver) (176/200)
19/01/03 14:21:07 INFO Executor: Running task 164.0 in stage 1.0 (TID 189)
19/01/03 14:21:07 INFO StateStore: Env is not null
19/01/03 14:21:07 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:07 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@713d4443
19/01/03 14:21:07 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,164,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:07 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=164),dir = file:/tmp/ts-sink/state/0/164] for update
19/01/03 14:21:07 INFO StateStore: Env is not null
19/01/03 14:21:07 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:07 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@2a8b81b4
19/01/03 14:21:07 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,164,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:07 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=164),dir = file:/tmp/ts-sink/state/0/164] for update
19/01/03 14:21:07 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:21:07 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:07 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=162),dir=file:/tmp/ts-sink/state/0/162] to file file:/tmp/ts-sink/state/0/162/1.delta
19/01/03 14:21:07 INFO DataWritingSparkTask: Writer for partition 162 is committing.
19/01/03 14:21:07 INFO DataWritingSparkTask: Writer for partition 162 committed.
19/01/03 14:21:08 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=160),dir=file:/tmp/ts-sink/state/0/160]
19/01/03 14:21:08 INFO Executor: Finished task 160.0 in stage 1.0 (TID 186). 4584 bytes result sent to driver
19/01/03 14:21:08 INFO TaskSetManager: Starting task 167.0 in stage 1.0 (TID 190, localhost, executor driver, partition 167, ANY, 7754 bytes)
19/01/03 14:21:08 INFO Executor: Running task 167.0 in stage 1.0 (TID 190)
19/01/03 14:21:08 INFO TaskSetManager: Finished task 160.0 in stage 1.0 (TID 186) in 3984 ms on localhost (executor driver) (177/200)
19/01/03 14:21:08 INFO StateStore: Env is not null
19/01/03 14:21:08 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:08 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@58bce34e
19/01/03 14:21:08 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,167,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:08 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=167),dir = file:/tmp/ts-sink/state/0/167] for update
19/01/03 14:21:08 INFO StateStore: Env is not null
19/01/03 14:21:08 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:08 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@71e0e528
19/01/03 14:21:08 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,167,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:08 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=167),dir = file:/tmp/ts-sink/state/0/167] for update
19/01/03 14:21:08 INFO ShuffleBlockFetcherIterator: Getting 2 non-empty blocks out of 10 blocks
19/01/03 14:21:08 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:09 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=162),dir=file:/tmp/ts-sink/state/0/162]
19/01/03 14:21:09 INFO Executor: Finished task 162.0 in stage 1.0 (TID 187). 4584 bytes result sent to driver
19/01/03 14:21:09 INFO TaskSetManager: Starting task 169.0 in stage 1.0 (TID 191, localhost, executor driver, partition 169, ANY, 7754 bytes)
19/01/03 14:21:09 INFO Executor: Running task 169.0 in stage 1.0 (TID 191)
19/01/03 14:21:09 INFO TaskSetManager: Finished task 162.0 in stage 1.0 (TID 187) in 3989 ms on localhost (executor driver) (178/200)
19/01/03 14:21:09 INFO StateStore: Env is not null
19/01/03 14:21:09 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:09 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@7b844885
19/01/03 14:21:09 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,169,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:09 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=169),dir = file:/tmp/ts-sink/state/0/169] for update
19/01/03 14:21:09 INFO StateStore: Env is not null
19/01/03 14:21:09 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:09 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@342c6f42
19/01/03 14:21:09 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,169,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:09 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=169),dir = file:/tmp/ts-sink/state/0/169] for update
19/01/03 14:21:09 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:21:09 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/03 14:21:10 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=163),dir=file:/tmp/ts-sink/state/0/163] to file file:/tmp/ts-sink/state/0/163/1.delta
19/01/03 14:21:10 INFO DataWritingSparkTask: Writer for partition 163 is committing.
19/01/03 14:21:10 INFO DataWritingSparkTask: Writer for partition 163 committed.
19/01/03 14:21:10 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=167),dir=file:/tmp/ts-sink/state/0/167] to file file:/tmp/ts-sink/state/0/167/1.delta
19/01/03 14:21:10 INFO DataWritingSparkTask: Writer for partition 167 is committing.
19/01/03 14:21:10 INFO DataWritingSparkTask: Writer for partition 167 committed.
19/01/03 14:21:11 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=164),dir=file:/tmp/ts-sink/state/0/164] to file file:/tmp/ts-sink/state/0/164/1.delta
19/01/03 14:21:11 INFO DataWritingSparkTask: Writer for partition 164 is committing.
19/01/03 14:21:11 INFO DataWritingSparkTask: Writer for partition 164 committed.
19/01/03 14:21:11 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=169),dir=file:/tmp/ts-sink/state/0/169] to file file:/tmp/ts-sink/state/0/169/1.delta
19/01/03 14:21:11 INFO DataWritingSparkTask: Writer for partition 169 is committing.
19/01/03 14:21:11 INFO DataWritingSparkTask: Writer for partition 169 committed.
19/01/03 14:21:12 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=167),dir=file:/tmp/ts-sink/state/0/167]
19/01/03 14:21:12 INFO Executor: Finished task 167.0 in stage 1.0 (TID 190). 4584 bytes result sent to driver
19/01/03 14:21:12 INFO TaskSetManager: Starting task 170.0 in stage 1.0 (TID 192, localhost, executor driver, partition 170, ANY, 7754 bytes)
19/01/03 14:21:12 INFO Executor: Running task 170.0 in stage 1.0 (TID 192)
19/01/03 14:21:12 INFO TaskSetManager: Finished task 167.0 in stage 1.0 (TID 190) in 3818 ms on localhost (executor driver) (179/200)
19/01/03 14:21:12 INFO StateStore: Env is not null
19/01/03 14:21:12 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:12 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@1267bd22
19/01/03 14:21:12 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,170,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:12 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=170),dir = file:/tmp/ts-sink/state/0/170] for update
19/01/03 14:21:12 INFO StateStore: Env is not null
19/01/03 14:21:12 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:12 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@b5da26b
19/01/03 14:21:12 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,170,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:12 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=170),dir = file:/tmp/ts-sink/state/0/170] for update
19/01/03 14:21:12 INFO ShuffleBlockFetcherIterator: Getting 2 non-empty blocks out of 10 blocks
19/01/03 14:21:12 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:13 INFO StateStore: Env is not null
19/01/03 14:21:13 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:13 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@402262cb
19/01/03 14:21:13 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=169),dir=file:/tmp/ts-sink/state/0/169]
19/01/03 14:21:13 INFO Executor: Finished task 169.0 in stage 1.0 (TID 191). 4627 bytes result sent to driver
19/01/03 14:21:13 INFO TaskSetManager: Starting task 171.0 in stage 1.0 (TID 193, localhost, executor driver, partition 171, ANY, 7754 bytes)
19/01/03 14:21:13 INFO Executor: Running task 171.0 in stage 1.0 (TID 193)
19/01/03 14:21:13 INFO TaskSetManager: Finished task 169.0 in stage 1.0 (TID 191) in 3844 ms on localhost (executor driver) (180/200)
19/01/03 14:21:13 INFO StateStore: Env is not null
19/01/03 14:21:13 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:13 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@1ea24675
19/01/03 14:21:13 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,171,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:13 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=171),dir = file:/tmp/ts-sink/state/0/171] for update
19/01/03 14:21:13 INFO StateStore: Env is not null
19/01/03 14:21:13 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:13 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@a1939d4
19/01/03 14:21:13 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,171,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:13 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=171),dir = file:/tmp/ts-sink/state/0/171] for update
19/01/03 14:21:13 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:21:13 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/03 14:21:14 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=163),dir=file:/tmp/ts-sink/state/0/163]
19/01/03 14:21:14 INFO Executor: Finished task 163.0 in stage 1.0 (TID 188). 4584 bytes result sent to driver
19/01/03 14:21:14 INFO TaskSetManager: Starting task 173.0 in stage 1.0 (TID 194, localhost, executor driver, partition 173, ANY, 7754 bytes)
19/01/03 14:21:14 INFO Executor: Running task 173.0 in stage 1.0 (TID 194)
19/01/03 14:21:14 INFO TaskSetManager: Finished task 163.0 in stage 1.0 (TID 188) in 7832 ms on localhost (executor driver) (181/200)
19/01/03 14:21:14 INFO StateStore: Env is not null
19/01/03 14:21:14 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:14 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@49d5513f
19/01/03 14:21:14 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,173,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:14 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=173),dir = file:/tmp/ts-sink/state/0/173] for update
19/01/03 14:21:14 INFO StateStore: Env is not null
19/01/03 14:21:14 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:14 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@30c80c39
19/01/03 14:21:14 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,173,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:14 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=173),dir = file:/tmp/ts-sink/state/0/173] for update
19/01/03 14:21:14 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:21:14 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:14 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=170),dir=file:/tmp/ts-sink/state/0/170] to file file:/tmp/ts-sink/state/0/170/1.delta
19/01/03 14:21:14 INFO DataWritingSparkTask: Writer for partition 170 is committing.
19/01/03 14:21:14 INFO DataWritingSparkTask: Writer for partition 170 committed.
19/01/03 14:21:15 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=164),dir=file:/tmp/ts-sink/state/0/164]
19/01/03 14:21:15 INFO Executor: Finished task 164.0 in stage 1.0 (TID 189). 4584 bytes result sent to driver
19/01/03 14:21:15 INFO TaskSetManager: Starting task 175.0 in stage 1.0 (TID 195, localhost, executor driver, partition 175, ANY, 7754 bytes)
19/01/03 14:21:15 INFO Executor: Running task 175.0 in stage 1.0 (TID 195)
19/01/03 14:21:15 INFO TaskSetManager: Finished task 164.0 in stage 1.0 (TID 189) in 7822 ms on localhost (executor driver) (182/200)
19/01/03 14:21:15 INFO StateStore: Env is not null
19/01/03 14:21:15 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:15 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@24175a9
19/01/03 14:21:15 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,175,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:15 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=175),dir = file:/tmp/ts-sink/state/0/175] for update
19/01/03 14:21:15 INFO StateStore: Env is not null
19/01/03 14:21:15 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:15 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@1e7a435
19/01/03 14:21:15 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,175,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:15 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=175),dir = file:/tmp/ts-sink/state/0/175] for update
19/01/03 14:21:15 INFO ShuffleBlockFetcherIterator: Getting 2 non-empty blocks out of 10 blocks
19/01/03 14:21:15 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:15 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=171),dir=file:/tmp/ts-sink/state/0/171] to file file:/tmp/ts-sink/state/0/171/1.delta
19/01/03 14:21:15 INFO DataWritingSparkTask: Writer for partition 171 is committing.
19/01/03 14:21:15 INFO DataWritingSparkTask: Writer for partition 171 committed.
19/01/03 14:21:16 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=170),dir=file:/tmp/ts-sink/state/0/170]
19/01/03 14:21:16 INFO Executor: Finished task 170.0 in stage 1.0 (TID 192). 4584 bytes result sent to driver
19/01/03 14:21:16 INFO TaskSetManager: Starting task 176.0 in stage 1.0 (TID 196, localhost, executor driver, partition 176, ANY, 7754 bytes)
19/01/03 14:21:16 INFO Executor: Running task 176.0 in stage 1.0 (TID 196)
19/01/03 14:21:16 INFO TaskSetManager: Finished task 170.0 in stage 1.0 (TID 192) in 3932 ms on localhost (executor driver) (183/200)
19/01/03 14:21:16 INFO StateStore: Env is not null
19/01/03 14:21:16 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:16 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@60ea1920
19/01/03 14:21:16 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,176,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:16 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=176),dir = file:/tmp/ts-sink/state/0/176] for update
19/01/03 14:21:16 INFO StateStore: Env is not null
19/01/03 14:21:16 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:16 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@27c36f2a
19/01/03 14:21:16 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,176,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:16 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=176),dir = file:/tmp/ts-sink/state/0/176] for update
19/01/03 14:21:16 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:21:16 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:17 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=171),dir=file:/tmp/ts-sink/state/0/171]
19/01/03 14:21:17 INFO Executor: Finished task 171.0 in stage 1.0 (TID 193). 4584 bytes result sent to driver
19/01/03 14:21:17 INFO TaskSetManager: Starting task 177.0 in stage 1.0 (TID 197, localhost, executor driver, partition 177, ANY, 7754 bytes)
19/01/03 14:21:17 INFO Executor: Running task 177.0 in stage 1.0 (TID 197)
19/01/03 14:21:17 INFO TaskSetManager: Finished task 171.0 in stage 1.0 (TID 193) in 3972 ms on localhost (executor driver) (184/200)
19/01/03 14:21:17 INFO StateStore: Env is not null
19/01/03 14:21:17 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:17 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@1de6f0a1
19/01/03 14:21:17 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,177,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:17 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=177),dir = file:/tmp/ts-sink/state/0/177] for update
19/01/03 14:21:17 INFO StateStore: Env is not null
19/01/03 14:21:17 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:17 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@334fceb
19/01/03 14:21:17 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,177,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:17 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=177),dir = file:/tmp/ts-sink/state/0/177] for update
19/01/03 14:21:17 INFO ShuffleBlockFetcherIterator: Getting 3 non-empty blocks out of 10 blocks
19/01/03 14:21:17 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:18 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=173),dir=file:/tmp/ts-sink/state/0/173] to file file:/tmp/ts-sink/state/0/173/1.delta
19/01/03 14:21:18 INFO DataWritingSparkTask: Writer for partition 173 is committing.
19/01/03 14:21:18 INFO DataWritingSparkTask: Writer for partition 173 committed.
19/01/03 14:21:18 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=176),dir=file:/tmp/ts-sink/state/0/176] to file file:/tmp/ts-sink/state/0/176/1.delta
19/01/03 14:21:18 INFO DataWritingSparkTask: Writer for partition 176 is committing.
19/01/03 14:21:18 INFO DataWritingSparkTask: Writer for partition 176 committed.
19/01/03 14:21:19 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=175),dir=file:/tmp/ts-sink/state/0/175] to file file:/tmp/ts-sink/state/0/175/1.delta
19/01/03 14:21:19 INFO DataWritingSparkTask: Writer for partition 175 is committing.
19/01/03 14:21:19 INFO DataWritingSparkTask: Writer for partition 175 committed.
19/01/03 14:21:19 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=177),dir=file:/tmp/ts-sink/state/0/177] to file file:/tmp/ts-sink/state/0/177/1.delta
19/01/03 14:21:19 INFO DataWritingSparkTask: Writer for partition 177 is committing.
19/01/03 14:21:19 INFO DataWritingSparkTask: Writer for partition 177 committed.
19/01/03 14:21:20 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=176),dir=file:/tmp/ts-sink/state/0/176]
19/01/03 14:21:20 INFO Executor: Finished task 176.0 in stage 1.0 (TID 196). 4584 bytes result sent to driver
19/01/03 14:21:20 INFO TaskSetManager: Starting task 179.0 in stage 1.0 (TID 198, localhost, executor driver, partition 179, ANY, 7754 bytes)
19/01/03 14:21:20 INFO Executor: Running task 179.0 in stage 1.0 (TID 198)
19/01/03 14:21:20 INFO TaskSetManager: Finished task 176.0 in stage 1.0 (TID 196) in 3861 ms on localhost (executor driver) (185/200)
19/01/03 14:21:20 INFO StateStore: Env is not null
19/01/03 14:21:20 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:20 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@356b8e8c
19/01/03 14:21:20 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,179,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:20 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=179),dir = file:/tmp/ts-sink/state/0/179] for update
19/01/03 14:21:20 INFO StateStore: Env is not null
19/01/03 14:21:20 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:20 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@3660af41
19/01/03 14:21:20 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,179,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:20 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=179),dir = file:/tmp/ts-sink/state/0/179] for update
19/01/03 14:21:20 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:21:20 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:21 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=177),dir=file:/tmp/ts-sink/state/0/177]
19/01/03 14:21:21 INFO Executor: Finished task 177.0 in stage 1.0 (TID 197). 4584 bytes result sent to driver
19/01/03 14:21:21 INFO TaskSetManager: Starting task 180.0 in stage 1.0 (TID 199, localhost, executor driver, partition 180, ANY, 7754 bytes)
19/01/03 14:21:21 INFO Executor: Running task 180.0 in stage 1.0 (TID 199)
19/01/03 14:21:21 INFO TaskSetManager: Finished task 177.0 in stage 1.0 (TID 197) in 3760 ms on localhost (executor driver) (186/200)
19/01/03 14:21:21 INFO StateStore: Env is not null
19/01/03 14:21:21 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:21 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@659b069d
19/01/03 14:21:21 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,180,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:21 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=180),dir = file:/tmp/ts-sink/state/0/180] for update
19/01/03 14:21:21 INFO StateStore: Env is not null
19/01/03 14:21:21 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:21 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@2f2c1ab0
19/01/03 14:21:21 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,180,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:21 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=180),dir = file:/tmp/ts-sink/state/0/180] for update
19/01/03 14:21:21 INFO ShuffleBlockFetcherIterator: Getting 2 non-empty blocks out of 10 blocks
19/01/03 14:21:21 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:21 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=173),dir=file:/tmp/ts-sink/state/0/173]
19/01/03 14:21:21 INFO Executor: Finished task 173.0 in stage 1.0 (TID 194). 4584 bytes result sent to driver
19/01/03 14:21:21 INFO TaskSetManager: Starting task 184.0 in stage 1.0 (TID 200, localhost, executor driver, partition 184, ANY, 7754 bytes)
19/01/03 14:21:21 INFO Executor: Running task 184.0 in stage 1.0 (TID 200)
19/01/03 14:21:21 INFO TaskSetManager: Finished task 173.0 in stage 1.0 (TID 194) in 7722 ms on localhost (executor driver) (187/200)
19/01/03 14:21:21 INFO StateStore: Env is not null
19/01/03 14:21:21 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:21 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@3231fd9f
19/01/03 14:21:21 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,184,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:21 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=184),dir = file:/tmp/ts-sink/state/0/184] for update
19/01/03 14:21:21 INFO StateStore: Env is not null
19/01/03 14:21:21 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:21 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@75b88e96
19/01/03 14:21:21 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,184,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:21 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=184),dir = file:/tmp/ts-sink/state/0/184] for update
19/01/03 14:21:21 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:21:21 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:22 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=179),dir=file:/tmp/ts-sink/state/0/179] to file file:/tmp/ts-sink/state/0/179/1.delta
19/01/03 14:21:22 INFO DataWritingSparkTask: Writer for partition 179 is committing.
19/01/03 14:21:22 INFO DataWritingSparkTask: Writer for partition 179 committed.
19/01/03 14:21:22 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=175),dir=file:/tmp/ts-sink/state/0/175]
19/01/03 14:21:22 INFO Executor: Finished task 175.0 in stage 1.0 (TID 195). 4584 bytes result sent to driver
19/01/03 14:21:22 INFO TaskSetManager: Starting task 185.0 in stage 1.0 (TID 201, localhost, executor driver, partition 185, ANY, 7754 bytes)
19/01/03 14:21:22 INFO Executor: Running task 185.0 in stage 1.0 (TID 201)
19/01/03 14:21:22 INFO TaskSetManager: Finished task 175.0 in stage 1.0 (TID 195) in 7717 ms on localhost (executor driver) (188/200)
19/01/03 14:21:22 INFO StateStore: Env is not null
19/01/03 14:21:22 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:22 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@6b362452
19/01/03 14:21:22 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,185,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:22 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=185),dir = file:/tmp/ts-sink/state/0/185] for update
19/01/03 14:21:22 INFO StateStore: Env is not null
19/01/03 14:21:22 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:22 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@4ef60346
19/01/03 14:21:22 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,185,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:22 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=185),dir = file:/tmp/ts-sink/state/0/185] for update
19/01/03 14:21:22 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:21:22 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/03 14:21:23 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=180),dir=file:/tmp/ts-sink/state/0/180] to file file:/tmp/ts-sink/state/0/180/1.delta
19/01/03 14:21:23 INFO DataWritingSparkTask: Writer for partition 180 is committing.
19/01/03 14:21:23 INFO DataWritingSparkTask: Writer for partition 180 committed.
19/01/03 14:21:24 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=179),dir=file:/tmp/ts-sink/state/0/179]
19/01/03 14:21:24 INFO Executor: Finished task 179.0 in stage 1.0 (TID 198). 4584 bytes result sent to driver
19/01/03 14:21:24 INFO TaskSetManager: Starting task 186.0 in stage 1.0 (TID 202, localhost, executor driver, partition 186, ANY, 7754 bytes)
19/01/03 14:21:24 INFO Executor: Running task 186.0 in stage 1.0 (TID 202)
19/01/03 14:21:24 INFO TaskSetManager: Finished task 179.0 in stage 1.0 (TID 198) in 3745 ms on localhost (executor driver) (189/200)
19/01/03 14:21:24 INFO StateStore: Env is not null
19/01/03 14:21:24 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:24 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@197098d
19/01/03 14:21:24 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,186,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:24 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=186),dir = file:/tmp/ts-sink/state/0/186] for update
19/01/03 14:21:24 INFO StateStore: Env is not null
19/01/03 14:21:24 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:24 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@54370827
19/01/03 14:21:24 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,186,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:24 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=186),dir = file:/tmp/ts-sink/state/0/186] for update
19/01/03 14:21:24 INFO ShuffleBlockFetcherIterator: Getting 2 non-empty blocks out of 10 blocks
19/01/03 14:21:24 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:25 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=180),dir=file:/tmp/ts-sink/state/0/180]
19/01/03 14:21:25 INFO Executor: Finished task 180.0 in stage 1.0 (TID 199). 4627 bytes result sent to driver
19/01/03 14:21:25 INFO TaskSetManager: Starting task 191.0 in stage 1.0 (TID 203, localhost, executor driver, partition 191, ANY, 7754 bytes)
19/01/03 14:21:25 INFO Executor: Running task 191.0 in stage 1.0 (TID 203)
19/01/03 14:21:25 INFO TaskSetManager: Finished task 180.0 in stage 1.0 (TID 199) in 3775 ms on localhost (executor driver) (190/200)
19/01/03 14:21:25 INFO StateStore: Env is not null
19/01/03 14:21:25 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:25 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@60816f6a
19/01/03 14:21:25 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,191,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:25 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=191),dir = file:/tmp/ts-sink/state/0/191] for update
19/01/03 14:21:25 INFO StateStore: Env is not null
19/01/03 14:21:25 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:25 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@141745b1
19/01/03 14:21:25 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,191,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:25 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=191),dir = file:/tmp/ts-sink/state/0/191] for update
19/01/03 14:21:25 INFO ShuffleBlockFetcherIterator: Getting 5 non-empty blocks out of 10 blocks
19/01/03 14:21:25 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:25 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=184),dir=file:/tmp/ts-sink/state/0/184] to file file:/tmp/ts-sink/state/0/184/1.delta
19/01/03 14:21:25 INFO DataWritingSparkTask: Writer for partition 184 is committing.
19/01/03 14:21:25 INFO DataWritingSparkTask: Writer for partition 184 committed.
19/01/03 14:21:26 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=186),dir=file:/tmp/ts-sink/state/0/186] to file file:/tmp/ts-sink/state/0/186/1.delta
19/01/03 14:21:26 INFO DataWritingSparkTask: Writer for partition 186 is committing.
19/01/03 14:21:26 INFO DataWritingSparkTask: Writer for partition 186 committed.
19/01/03 14:21:26 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=185),dir=file:/tmp/ts-sink/state/0/185] to file file:/tmp/ts-sink/state/0/185/1.delta
19/01/03 14:21:26 INFO DataWritingSparkTask: Writer for partition 185 is committing.
19/01/03 14:21:26 INFO DataWritingSparkTask: Writer for partition 185 committed.
19/01/03 14:21:27 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=191),dir=file:/tmp/ts-sink/state/0/191] to file file:/tmp/ts-sink/state/0/191/1.delta
19/01/03 14:21:27 INFO DataWritingSparkTask: Writer for partition 191 is committing.
19/01/03 14:21:27 INFO DataWritingSparkTask: Writer for partition 191 committed.
19/01/03 14:21:28 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=186),dir=file:/tmp/ts-sink/state/0/186]
19/01/03 14:21:28 INFO Executor: Finished task 186.0 in stage 1.0 (TID 202). 4584 bytes result sent to driver
19/01/03 14:21:28 INFO TaskSetManager: Starting task 192.0 in stage 1.0 (TID 204, localhost, executor driver, partition 192, ANY, 7754 bytes)
19/01/03 14:21:28 INFO TaskSetManager: Finished task 186.0 in stage 1.0 (TID 202) in 3765 ms on localhost (executor driver) (191/200)
19/01/03 14:21:28 INFO Executor: Running task 192.0 in stage 1.0 (TID 204)
19/01/03 14:21:28 INFO StateStore: Env is not null
19/01/03 14:21:28 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:28 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@c88af13
19/01/03 14:21:28 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,192,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:28 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=192),dir = file:/tmp/ts-sink/state/0/192] for update
19/01/03 14:21:28 INFO StateStore: Env is not null
19/01/03 14:21:28 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:28 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@d7ac738
19/01/03 14:21:28 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,192,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:28 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=192),dir = file:/tmp/ts-sink/state/0/192] for update
19/01/03 14:21:28 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:21:28 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:29 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=191),dir=file:/tmp/ts-sink/state/0/191]
19/01/03 14:21:29 INFO Executor: Finished task 191.0 in stage 1.0 (TID 203). 4627 bytes result sent to driver
19/01/03 14:21:29 INFO TaskSetManager: Starting task 193.0 in stage 1.0 (TID 205, localhost, executor driver, partition 193, ANY, 7754 bytes)
19/01/03 14:21:29 INFO Executor: Running task 193.0 in stage 1.0 (TID 205)
19/01/03 14:21:29 INFO TaskSetManager: Finished task 191.0 in stage 1.0 (TID 203) in 3742 ms on localhost (executor driver) (192/200)
19/01/03 14:21:29 INFO StateStore: Env is not null
19/01/03 14:21:29 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:29 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@3eae02a
19/01/03 14:21:29 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,193,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:29 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=193),dir = file:/tmp/ts-sink/state/0/193] for update
19/01/03 14:21:29 INFO StateStore: Env is not null
19/01/03 14:21:29 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:29 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@6aad67a9
19/01/03 14:21:29 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,193,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:29 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=193),dir = file:/tmp/ts-sink/state/0/193] for update
19/01/03 14:21:29 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:21:29 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:29 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=184),dir=file:/tmp/ts-sink/state/0/184]
19/01/03 14:21:29 INFO Executor: Finished task 184.0 in stage 1.0 (TID 200). 4584 bytes result sent to driver
19/01/03 14:21:29 INFO TaskSetManager: Starting task 194.0 in stage 1.0 (TID 206, localhost, executor driver, partition 194, ANY, 7754 bytes)
19/01/03 14:21:29 INFO Executor: Running task 194.0 in stage 1.0 (TID 206)
19/01/03 14:21:29 INFO TaskSetManager: Finished task 184.0 in stage 1.0 (TID 200) in 7522 ms on localhost (executor driver) (193/200)
19/01/03 14:21:29 INFO StateStore: Env is not null
19/01/03 14:21:29 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:29 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@502951ae
19/01/03 14:21:29 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,194,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:29 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=194),dir = file:/tmp/ts-sink/state/0/194] for update
19/01/03 14:21:29 INFO StateStore: Env is not null
19/01/03 14:21:29 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:29 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@714c7bfc
19/01/03 14:21:29 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,194,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:29 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=194),dir = file:/tmp/ts-sink/state/0/194] for update
19/01/03 14:21:29 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:21:29 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:29 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=192),dir=file:/tmp/ts-sink/state/0/192] to file file:/tmp/ts-sink/state/0/192/1.delta
19/01/03 14:21:29 INFO DataWritingSparkTask: Writer for partition 192 is committing.
19/01/03 14:21:29 INFO DataWritingSparkTask: Writer for partition 192 committed.
19/01/03 14:21:30 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=185),dir=file:/tmp/ts-sink/state/0/185]
19/01/03 14:21:30 INFO Executor: Finished task 185.0 in stage 1.0 (TID 201). 4584 bytes result sent to driver
19/01/03 14:21:30 INFO TaskSetManager: Starting task 195.0 in stage 1.0 (TID 207, localhost, executor driver, partition 195, ANY, 7754 bytes)
19/01/03 14:21:30 INFO Executor: Running task 195.0 in stage 1.0 (TID 207)
19/01/03 14:21:30 INFO TaskSetManager: Finished task 185.0 in stage 1.0 (TID 201) in 7695 ms on localhost (executor driver) (194/200)
19/01/03 14:21:30 INFO StateStore: Env is not null
19/01/03 14:21:30 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:30 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@716c92e0
19/01/03 14:21:30 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,195,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:30 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=195),dir = file:/tmp/ts-sink/state/0/195] for update
19/01/03 14:21:30 INFO StateStore: Env is not null
19/01/03 14:21:30 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:30 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@3dd06bc4
19/01/03 14:21:30 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,195,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:30 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=195),dir = file:/tmp/ts-sink/state/0/195] for update
19/01/03 14:21:30 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:21:30 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 1 ms
19/01/03 14:21:31 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=193),dir=file:/tmp/ts-sink/state/0/193] to file file:/tmp/ts-sink/state/0/193/1.delta
19/01/03 14:21:31 INFO DataWritingSparkTask: Writer for partition 193 is committing.
19/01/03 14:21:31 INFO DataWritingSparkTask: Writer for partition 193 committed.
19/01/03 14:21:32 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=192),dir=file:/tmp/ts-sink/state/0/192]
19/01/03 14:21:32 INFO Executor: Finished task 192.0 in stage 1.0 (TID 204). 4584 bytes result sent to driver
19/01/03 14:21:32 INFO TaskSetManager: Starting task 196.0 in stage 1.0 (TID 208, localhost, executor driver, partition 196, ANY, 7754 bytes)
19/01/03 14:21:32 INFO TaskSetManager: Finished task 192.0 in stage 1.0 (TID 204) in 3925 ms on localhost (executor driver) (195/200)
19/01/03 14:21:32 INFO Executor: Running task 196.0 in stage 1.0 (TID 208)
19/01/03 14:21:32 INFO StateStore: Env is not null
19/01/03 14:21:32 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:32 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@78d419bf
19/01/03 14:21:32 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,196,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:32 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=196),dir = file:/tmp/ts-sink/state/0/196] for update
19/01/03 14:21:32 INFO StateStore: Env is not null
19/01/03 14:21:32 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:32 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@66ff7000
19/01/03 14:21:32 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,196,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:32 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=196),dir = file:/tmp/ts-sink/state/0/196] for update
19/01/03 14:21:32 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:21:32 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:32 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=193),dir=file:/tmp/ts-sink/state/0/193]
19/01/03 14:21:32 INFO Executor: Finished task 193.0 in stage 1.0 (TID 205). 4584 bytes result sent to driver
19/01/03 14:21:32 INFO TaskSetManager: Starting task 198.0 in stage 1.0 (TID 209, localhost, executor driver, partition 198, ANY, 7754 bytes)
19/01/03 14:21:32 INFO Executor: Running task 198.0 in stage 1.0 (TID 209)
19/01/03 14:21:32 INFO TaskSetManager: Finished task 193.0 in stage 1.0 (TID 205) in 3937 ms on localhost (executor driver) (196/200)
19/01/03 14:21:32 INFO StateStore: Env is not null
19/01/03 14:21:32 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:32 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@4edeeceb
19/01/03 14:21:32 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,198,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:32 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=198),dir = file:/tmp/ts-sink/state/0/198] for update
19/01/03 14:21:32 INFO StateStore: Env is not null
19/01/03 14:21:32 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:21:32 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@3235b88f
19/01/03 14:21:32 INFO StateStore: Reported that the loaded instance StateStoreProviderId(StateStoreId(file:/tmp/ts-sink/state,0,198,default),ede4716d-6739-4d71-9302-20f0610a8086) is active
19/01/03 14:21:32 INFO HDFSBackedStateStoreProvider: Retrieved version 0 of HDFSStateStoreProvider[id = (op=0,part=198),dir = file:/tmp/ts-sink/state/0/198] for update
19/01/03 14:21:32 INFO ShuffleBlockFetcherIterator: Getting 1 non-empty blocks out of 10 blocks
19/01/03 14:21:32 INFO ShuffleBlockFetcherIterator: Started 0 remote fetches in 0 ms
19/01/03 14:21:33 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=194),dir=file:/tmp/ts-sink/state/0/194] to file file:/tmp/ts-sink/state/0/194/1.delta
19/01/03 14:21:33 INFO DataWritingSparkTask: Writer for partition 194 is committing.
19/01/03 14:21:33 INFO DataWritingSparkTask: Writer for partition 194 committed.
19/01/03 14:21:33 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=196),dir=file:/tmp/ts-sink/state/0/196] to file file:/tmp/ts-sink/state/0/196/1.delta
19/01/03 14:21:33 INFO DataWritingSparkTask: Writer for partition 196 is committing.
19/01/03 14:21:33 INFO DataWritingSparkTask: Writer for partition 196 committed.
19/01/03 14:21:34 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=195),dir=file:/tmp/ts-sink/state/0/195] to file file:/tmp/ts-sink/state/0/195/1.delta
19/01/03 14:21:34 INFO DataWritingSparkTask: Writer for partition 195 is committing.
19/01/03 14:21:34 INFO DataWritingSparkTask: Writer for partition 195 committed.
19/01/03 14:21:34 INFO HDFSBackedStateStoreProvider: Committed version 1 for HDFSStateStore[id=(op=0,part=198),dir=file:/tmp/ts-sink/state/0/198] to file file:/tmp/ts-sink/state/0/198/1.delta
19/01/03 14:21:34 INFO DataWritingSparkTask: Writer for partition 198 is committing.
19/01/03 14:21:34 INFO DataWritingSparkTask: Writer for partition 198 committed.
19/01/03 14:21:35 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=196),dir=file:/tmp/ts-sink/state/0/196]
19/01/03 14:21:35 INFO Executor: Finished task 196.0 in stage 1.0 (TID 208). 4584 bytes result sent to driver
19/01/03 14:21:35 INFO TaskSetManager: Finished task 196.0 in stage 1.0 (TID 208) in 3734 ms on localhost (executor driver) (197/200)
19/01/03 14:21:36 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=198),dir=file:/tmp/ts-sink/state/0/198]
19/01/03 14:21:36 INFO Executor: Finished task 198.0 in stage 1.0 (TID 209). 4584 bytes result sent to driver
19/01/03 14:21:36 INFO TaskSetManager: Finished task 198.0 in stage 1.0 (TID 209) in 3409 ms on localhost (executor driver) (198/200)
19/01/03 14:21:36 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=194),dir=file:/tmp/ts-sink/state/0/194]
19/01/03 14:21:36 INFO Executor: Finished task 194.0 in stage 1.0 (TID 206). 4627 bytes result sent to driver
19/01/03 14:21:36 INFO TaskSetManager: Finished task 194.0 in stage 1.0 (TID 206) in 7448 ms on localhost (executor driver) (199/200)
19/01/03 14:21:37 INFO HDFSBackedStateStoreProvider: Aborted version 1 for HDFSStateStore[id=(op=0,part=195),dir=file:/tmp/ts-sink/state/0/195]
19/01/03 14:21:37 INFO Executor: Finished task 195.0 in stage 1.0 (TID 207). 4584 bytes result sent to driver
19/01/03 14:21:37 INFO TaskSetManager: Finished task 195.0 in stage 1.0 (TID 207) in 6645 ms on localhost (executor driver) (200/200)
19/01/03 14:21:37 INFO TaskSchedulerImpl: Removed TaskSet 1.0, whose tasks have all completed, from pool
19/01/03 14:21:37 INFO DAGScheduler: ResultStage 1 (start at Main.scala:54) finished in 264.214 s
19/01/03 14:21:37 INFO DAGScheduler: Job 0 finished: start at Main.scala:54, took 267.709168 s
19/01/03 14:21:37 INFO WriteToDataSourceV2Exec: Data source writer org.apache.spark.sql.execution.streaming.sources.InternalRowMicroBatchWriter@28f30d36 is committing.
19/01/03 14:21:37 INFO WriteToDataSourceV2Exec: Data source writer org.apache.spark.sql.execution.streaming.sources.InternalRowMicroBatchWriter@28f30d36 committed.
19/01/03 14:21:37 INFO SparkContext: Starting job: start at Main.scala:54
19/01/03 14:21:37 INFO DAGScheduler: Job 1 finished: start at Main.scala:54, took 0.000039 s
19/01/03 14:21:37 INFO MicroBatchExecution: Streaming query made progress: {
  "id" : "add09302-8d5c-43fd-b5df-159c03824a01",
  "runId" : "ede4716d-6739-4d71-9302-20f0610a8086",
  "name" : "hashtag counts",
  "timestamp" : "2019-01-03T11:16:57.453Z",
  "batchId" : 0,
  "numInputRows" : 146,
  "processedRowsPerSecond" : 0.5218814900073993,
  "durationMs" : {
    "addBatch" : 269527,
    "getBatch" : 752,
    "getOffset" : 4834,
    "queryPlanning" : 1195,
    "triggerExecution" : 279756,
    "walCommit" : 3371
  },
  "eventTime" : {
    "avg" : "2018-12-27T12:14:38.698Z",
    "max" : "2018-12-27T12:39:35.000Z",
    "min" : "2018-12-27T12:10:43.000Z",
    "watermark" : "1970-01-01T00:00:00.000Z"
  },
  "stateOperators" : [ {
    "numRowsTotal" : 134,
    "numRowsUpdated" : 134,
    "memoryUsedBytes" : 59815
  } ],
  "sources" : [ {
    "description" : "KafkaSource[Subscribe[Tweets]]",
    "startOffset" : null,
    "endOffset" : {
      "Tweets" : {
        "8" : 14,
        "2" : 13,
        "5" : 12,
        "4" : 11,
        "7" : 20,
        "1" : 9,
        "9" : 12,
        "3" : 17,
        "6" : 20,
        "0" : 18
      }
    },
    "numInputRows" : 146,
    "processedRowsPerSecond" : 0.5218814900073993
  } ],
  "sink" : {
    "description" : "org.apache.spark.sql.kafka010.KafkaSourceProvider@7e23d84f"
  }
}
19/01/03 14:24:12 INFO StateStore: Env is not null
19/01/03 14:24:12 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:24:12 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@21d48b8
19/01/03 14:25:10 INFO MicroBatchExecution: Streaming query made progress: {
  "id" : "add09302-8d5c-43fd-b5df-159c03824a01",
  "runId" : "ede4716d-6739-4d71-9302-20f0610a8086",
  "name" : "hashtag counts",
  "timestamp" : "2019-01-03T11:25:10.000Z",
  "batchId" : 1,
  "numInputRows" : 0,
  "inputRowsPerSecond" : 0.0,
  "processedRowsPerSecond" : 0.0,
  "durationMs" : {
    "getOffset" : 43,
    "triggerExecution" : 43
  },
  "eventTime" : {
    "watermark" : "1970-01-01T00:00:00.000Z"
  },
  "stateOperators" : [ {
    "numRowsTotal" : 134,
    "numRowsUpdated" : 0,
    "memoryUsedBytes" : 59815
  } ],
  "sources" : [ {
    "description" : "KafkaSource[Subscribe[Tweets]]",
    "startOffset" : {
      "Tweets" : {
        "8" : 14,
        "2" : 13,
        "5" : 12,
        "4" : 11,
        "7" : 20,
        "1" : 9,
        "9" : 12,
        "3" : 17,
        "6" : 20,
        "0" : 18
      }
    },
    "endOffset" : {
      "Tweets" : {
        "8" : 14,
        "2" : 13,
        "5" : 12,
        "4" : 11,
        "7" : 20,
        "1" : 9,
        "9" : 12,
        "3" : 17,
        "6" : 20,
        "0" : 18
      }
    },
    "numInputRows" : 0,
    "inputRowsPerSecond" : 0.0,
    "processedRowsPerSecond" : 0.0
  } ],
  "sink" : {
    "description" : "org.apache.spark.sql.kafka010.KafkaSourceProvider@7e23d84f"
  }
}
19/01/03 14:25:12 INFO StateStore: Env is not null
19/01/03 14:25:12 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:25:12 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@25c2019
19/01/03 14:26:00 INFO MicroBatchExecution: Streaming query made progress: {
  "id" : "add09302-8d5c-43fd-b5df-159c03824a01",
  "runId" : "ede4716d-6739-4d71-9302-20f0610a8086",
  "name" : "hashtag counts",
  "timestamp" : "2019-01-03T11:26:00.000Z",
  "batchId" : 1,
  "numInputRows" : 0,
  "inputRowsPerSecond" : 0.0,
  "processedRowsPerSecond" : 0.0,
  "durationMs" : {
    "getOffset" : 271,
    "triggerExecution" : 271
  },
  "eventTime" : {
    "watermark" : "1970-01-01T00:00:00.000Z"
  },
  "stateOperators" : [ {
    "numRowsTotal" : 134,
    "numRowsUpdated" : 0,
    "memoryUsedBytes" : 59815
  } ],
  "sources" : [ {
    "description" : "KafkaSource[Subscribe[Tweets]]",
    "startOffset" : {
      "Tweets" : {
        "8" : 14,
        "2" : 13,
        "5" : 12,
        "4" : 11,
        "7" : 20,
        "1" : 9,
        "9" : 12,
        "3" : 17,
        "6" : 20,
        "0" : 18
      }
    },
    "endOffset" : {
      "Tweets" : {
        "8" : 14,
        "2" : 13,
        "5" : 12,
        "4" : 11,
        "7" : 20,
        "1" : 9,
        "9" : 12,
        "3" : 17,
        "6" : 20,
        "0" : 18
      }
    },
    "numInputRows" : 0,
    "inputRowsPerSecond" : 0.0,
    "processedRowsPerSecond" : 0.0
  } ],
  "sink" : {
    "description" : "org.apache.spark.sql.kafka010.KafkaSourceProvider@7e23d84f"
  }
}
19/01/03 14:26:12 INFO StateStore: Env is not null
19/01/03 14:26:12 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:26:12 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@865f33f
19/01/03 14:27:10 INFO MicroBatchExecution: Streaming query made progress: {
  "id" : "add09302-8d5c-43fd-b5df-159c03824a01",
  "runId" : "ede4716d-6739-4d71-9302-20f0610a8086",
  "name" : "hashtag counts",
  "timestamp" : "2019-01-03T11:27:10.000Z",
  "batchId" : 1,
  "numInputRows" : 0,
  "inputRowsPerSecond" : 0.0,
  "processedRowsPerSecond" : 0.0,
  "durationMs" : {
    "getOffset" : 42,
    "triggerExecution" : 42
  },
  "eventTime" : {
    "watermark" : "1970-01-01T00:00:00.000Z"
  },
  "stateOperators" : [ {
    "numRowsTotal" : 134,
    "numRowsUpdated" : 0,
    "memoryUsedBytes" : 59815
  } ],
  "sources" : [ {
    "description" : "KafkaSource[Subscribe[Tweets]]",
    "startOffset" : {
      "Tweets" : {
        "8" : 14,
        "2" : 13,
        "5" : 12,
        "4" : 11,
        "7" : 20,
        "1" : 9,
        "9" : 12,
        "3" : 17,
        "6" : 20,
        "0" : 18
      }
    },
    "endOffset" : {
      "Tweets" : {
        "8" : 14,
        "2" : 13,
        "5" : 12,
        "4" : 11,
        "7" : 20,
        "1" : 9,
        "9" : 12,
        "3" : 17,
        "6" : 20,
        "0" : 18
      }
    },
    "numInputRows" : 0,
    "inputRowsPerSecond" : 0.0,
    "processedRowsPerSecond" : 0.0
  } ],
  "sink" : {
    "description" : "org.apache.spark.sql.kafka010.KafkaSourceProvider@7e23d84f"
  }
}
19/01/03 14:27:13 INFO StateStore: Env is not null
19/01/03 14:27:13 INFO StateStore: Getting StateStoreCoordinatorRef
19/01/03 14:27:13 INFO StateStore: Retrieved reference to StateStoreCoordinator: org.apache.spark.sql.execution.streaming.state.StateStoreCoordinatorRef@39db736b
19/01/03 14:27:20 INFO MicroBatchExecution: Streaming query made progress: {
  "id" : "add09302-8d5c-43fd-b5df-159c03824a01",
  "runId" : "ede4716d-6739-4d71-9302-20f0610a8086",
  "name" : "hashtag counts",
  "timestamp" : "2019-01-03T11:27:20.000Z",
  "batchId" : 1,
  "numInputRows" : 0,
  "inputRowsPerSecond" : 0.0,
  "processedRowsPerSecond" : 0.0,
  "durationMs" : {
    "getOffset" : 53,
    "triggerExecution" : 53
  },
  "eventTime" : {
    "watermark" : "1970-01-01T00:00:00.000Z"
  },
  "stateOperators" : [ {
    "numRowsTotal" : 134,
    "numRowsUpdated" : 0,
    "memoryUsedBytes" : 59815
  } ],
  "sources" : [ {
    "description" : "KafkaSource[Subscribe[Tweets]]",
    "startOffset" : {
      "Tweets" : {
        "8" : 14,
        "2" : 13,
        "5" : 12,
        "4" : 11,
        "7" : 20,
        "1" : 9,
        "9" : 12,
        "3" : 17,
        "6" : 20,
        "0" : 18
      }
    },
    "endOffset" : {
      "Tweets" : {
        "8" : 14,
        "2" : 13,
        "5" : 12,
        "4" : 11,
        "7" : 20,
        "1" : 9,
        "9" : 12,
        "3" : 17,
        "6" : 20,
        "0" : 18
      }
    },
    "numInputRows" : 0,
    "inputRowsPerSecond" : 0.0,
    "processedRowsPerSecond" : 0.0
  } ],
  "sink" : {
    "description" : "org.apache.spark.sql.kafka010.KafkaSourceProvider@7e23d84f"
  }
}
```

</p>
</details>

To check that new data successfully committed to the kafka's topic, use the following:
```shell
kafka-console-consumer.sh --bootstrap-server 192.168.99.100:9092 --topic HashCounts --from-beginning
```

<details><summary>HashCounts topic content</summary>
<p>

```shell
.../kafka_2.12-1.0.0/bin/kafka-console-consumer.sh --bootstrap-server 192.168.99.100:9092 --topic HashCounts --from-beginning
1
1
1
1
1
3
1
1
1
1
1
4
1
1
1
3
1
1
1
1
1
6
1
1
1
1
1
1
1
2
1
1
1
1
1
1
1
1
1
1
6
1
1
2
1
1
1
1
1
1
1
1
1
1
1
1
1
1
1
1
1
1
1
2
1
1
1
1
3
1
1
1
1
1
1
1
1
2
1
1
1
1
1
1
1
1
1
1
1
1
1
2
1
1
1
1
1
1
1
4
1
1
1
1
1
1
1
2
2
1
3
1
1
1
1
1
1
1
1
3
1
1
1
1
2
1
1
1
1
1
1
1
1
1
Processed a total of 134 messages
```

</p>
</details>

Here are some screenshots:

![Spark UI](./screenshots/Ssparkui.png "Spark UI")

![Job 0 DAG](./screenshots/SJob0Stages0-1.png "Job 0 DAG")

