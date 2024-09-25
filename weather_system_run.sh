#!/bin/bash

# Set variables
KAFKA_VERSION="3.8.0"
KAFKA_TGZ="kafka_2.13-$KAFKA_VERSION.tgz"
KAFKA_DIR="kafka_2.13-$KAFKA_VERSION"
TOPIC_NAME="weather-data"
HBASE_TABLE_NAME="weather_data"
SPOOL_DIR="/home/cloudera/spooldir"

# Download Kafka
echo "Downloading Kafka..."
wget https://downloads.apache.org/kafka/$KAFKA_VERSION/$KAFKA_TGZ

# Unzip Kafka
echo "Unzipping Kafka..."
tar -xzf $KAFKA_TGZ

# Start Kafka server in the background
echo "Starting Kafka server..."
$KAFKA_DIR/bin/kafka-server-start.sh $KAFKA_DIR/config/server.properties &

# Wait for Kafka to start
sleep 10

# Create Kafka topic
echo "Creating Kafka topic..."
$KAFKA_DIR/bin/kafka-topics.sh --create --topic $TOPIC_NAME --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# Enter HBase shell and create table
echo "Creating HBase table..."
echo "create '$HBASE_TABLE_NAME', 'data'" | hbase shell

# Enter Hive and create external table
echo "Creating Hive external table..."
hive -e "
CREATE EXTERNAL TABLE impala_weather_data (
    city STRING,
    temperature STRING,
    humidity STRING,
    timestamp STRING
)
STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
WITH SERDEPROPERTIES (
    'hbase.columns.mapping' = ':key,data:temperature,data:humidity,data:timestamp'
)
TBLPROPERTIES ('hbase.table.name' = '$HBASE_TABLE_NAME');
"

# Invalidate Impala metadata
echo "Invalidating Impala metadata..."
impala-shell -c "INVALIDATE METADATA;"

# Check if spool directory exists; if not, create it
if [ ! -d "$SPOOL_DIR" ]; then
    echo "Creating spool directory..."
    mkdir -p $SPOOL_DIR
fi

# Open Flume in a new terminal
echo "Starting Flume agent..."
gnome-terminal -- flume-ng agent --conf ./conf --conf-file flume.conf --name agent1 -Dflume.root.logger=INFO,console &

# Start Spark application in a new terminal
echo "Starting Spark application..."
gnome-terminal -- spark-submit \
    --class edu.miu.WeatherDataStreamingApp \
    --master local[*] \
    target/WeatherStreaming.jar &

# Run WeatherDataFetcher in a new terminal
echo "Starting Weather Data Fetcher..."
gnome-terminal -- java -jar WeatherDataFetcher.jar &

# Run Impala shell and execute query every 3 minutes
echo "Entering Impala shell..."
while true; do
    impala-shell -c "SELECT * FROM impala_weather_data;"
    sleep 180 # Wait for 3 minutes
done
