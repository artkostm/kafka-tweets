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

Run Tweets Producer:
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