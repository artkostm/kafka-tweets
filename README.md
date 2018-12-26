# kafka-tweets

```bash
docker run --rm -p 2181:2181 -p 9092:9092 -p9999:9999 \
    --env ADVERTISED_HOST=192.168.99.100 \
    --env ADVERTISED_PORT=9092 \
    --env TOPICS=TestTopic \
    spotify/kafka
    ```
