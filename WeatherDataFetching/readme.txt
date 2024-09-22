hbase shell
 
# In HBase shell
create 'weather_data', 'data'
 
# Verify table creation
list
 
Download Kafka from the https://kafka.apache.org/downloads and extract it
tar -xzf kafka_2.13-3.4.0.tgz
cd kafka_2.13-3.4.0
 

Start Zookeeper:

 

bash
Copy code
bin/zookeeper-server-start.sh config/zookeeper.properties

In a new terminal, start Kafka broker:

 

bash
Copy code
bin/kafka-server-start.sh config/server.properties

Create Kafka Topic

Create a topic named weather-data:

bin/kafka-topics.sh --create --topic weather-data --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1 

 

Verify Kafka is Running

List the topics to confirm:

bin/kafka-topics.sh --list --bootstrap-server localhost:9092 

You should see weather-data listed.
 
flume-ng agent --conf ./conf --conf-file flume.conf --name agent1 -Dflume.root.logger=INFO,console