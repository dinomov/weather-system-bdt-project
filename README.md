# Weather System

## Description

Weather System is integrating OpenWeatherMap API, Apache Flume, Kafka, Spark streaming, HBase, Hive, Impala (Initially we wanted to use Apache Phoenix and Grafana for nice dashboard to demo weather data but Cloudera VM has very old browser and it was not possible to update it to make work Grafana, so we gave up Phoenix and Grafana, and used Hive and Impala to show weather data)

-  20 large cities in the US were given as source and temperature with humidity for these cities will be fetched every 3 mintues from OpenWeatherMap server.

- Weather data will be stored into file

- Flume reads this file and send to Kafka topic

- Spark streaming consumes weather data from Kafka and stores into HBase

- Using Hive we create table on base HBase table

- In Impala shell we show the human readable table for each city temperature and humidity


## Used technologies
- Java Development Kit (JDK) 8 or higher
- Apache Maven (for building Java projects)
- Apache Kafka
- Apache Flume
- Apache HBase
- Apache Spark (with Spark Streaming)
- Apache Hive
- Impala
- Git (for version control)
- Integrated Development Environment Eclipse

# For automatic running all components using shell script do these instructions
- Download the project https://github.com/dinomov/weather-system-bdt-project/archive/refs/heads/main.zip
  unzip it
- Download kafka from here https://kafka.apache.org/downloads
  make sure it is in this address KAFKA_DIR="/home/cloudera/Downloads/kafka_2.13-3.8.0",
  or change KAFKA_DIR in weather_system_run.sh file in the project folder.
- enter into project folder and run:
  ```sh
  chmod +x weather_system_run.sh 
  
  # then run shell script
  ./weather_system_run.sh
  ```


# For manual running the system components follow below steps
# Setup Environment in Cloudera VM
- # Install Apache Kafka
  Download from here https://kafka.apache.org/downloads
  run following commands
    ```sh
    tar -xzf kafka_2.13-3.4.0.tgz
    cd kafka_2.13-3.4.0
    ```

  You can use zookeeper which is already running in Cloudera VM or run another one which comes with kafka
  Start zookeeper, before change port, set 12181 in file config/zookeeper.properties, HBase uses its default port
    ```sh
    bin/zookeeper-server-start.sh config/zookeeper.properties
    ```

  start Kafka broker
    ```sh
    bin/kafka-server-start.sh config/server.properties
    ```

  Create a Kafka topic named weather-data:
    ```sh
    bin/kafka-topics.sh --create --topic weather-data --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
    ```

  List the topics to confirm:
    ```sh
    bin/kafka-topics.sh --list --bootstrap-server localhost:9092
    ```
  You should see weather-data listed.

- # HBase table creation
    ```sh
    hbase shell
    # In HBase shell
    create 'weather_data', 'data'
    # Verify table creation
    list
    ```
- # Hive and Impala
    ```sh
    hive
    
    # run this command
    CREATE EXTERNAL TABLE impala_weather_data (
        city STRING,          -- The row key in HBase
        temperature STRING,   -- Temperature from 'data:temperature'
        humidity STRING       -- Humidity from 'data:humidity'
    )
    STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
    WITH SERDEPROPERTIES (
        "hbase.columns.mapping" = ":key,data:temperature,data:humidity"
    )
    TBLPROPERTIES ("hbase.table.name" = "weather_data");
    ```

  exit and enter impala shell
    ```sh
    impala-shell
    
    # after creating the table in Hive, you need to invalidate the metadata so Impala recognizes the new table
    INVALIDATE METADATA;
    
    # we can run this command once we start all other components of our Weather System
    SELECT * FROM impala_weather_data
    ```

- # RUN the Weather System
  once we run kafka already above now run following components step by step

  - clone repository from here https://github.com/dinomov/weather-system-bdt-project
  - enter weather-system-bdt-project folder
    ```sh
    cd weather-system-bdt-project folder
    
    # there we have flume config file already, run flume
    flume-ng agent --conf ./conf --conf-file flume.conf --name agent1 -Dflume.root.logger=INFO,console
    ```

  in another terminal enter folder WeatherDataStreamingApp, build then run spark streaming app
    ```sh
    mvn clean compile
    
    # then run using maven 
    mvn exec:java -Dexec.mainClass="edu.miu.WeatherDataStreamingApp"
    
    # or run using this
    spark-submit \
  --class edu.miu.WeatherDataStreamingApp \
  --master local[*] \
  target/WeatherStreaming-1.0-SNAPSHOT.jar
  ```

  in another terminal run the app which connects OpenWeatherMap
  ```sh
  # enter following folder
  cd WeatherDataFetching/src/edu/miu/bdt
  
  # compile 
  javac WeatherDataFetcherScheduled.java
  
  # run it
  java WeatherDataFetcherScheduled
  ```

- # Time to see the result in Impala
[![N|Solid](https://github.com/dinomov/weather-system-bdt-project/blob/main/output-impala-shell.jpg)]    
    

