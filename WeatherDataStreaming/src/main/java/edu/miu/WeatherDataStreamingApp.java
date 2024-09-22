package edu.miu;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.api.java.*;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.kafka010.*;

import java.text.SimpleDateFormat;
import java.util.*;
 
public class WeatherDataStreamingApp {
    private static final String KAFKA_TOPIC = "weather-data";
    private static final String KAFKA_BROKERS = "localhost:9092";
    private static final ObjectMapper objectMapper = new ObjectMapper();
 
    public static void main(String[] args) throws InterruptedException {
        // Initialize Spark Configuration
        SparkConf conf = new SparkConf().setAppName("WeatherStreamingApp").setMaster("local[*]");
        JavaStreamingContext streamingContext = new JavaStreamingContext(conf, Durations.minutes(1));
 
        // Kafka Parameters
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", KAFKA_BROKERS);
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "weather-streaming-group");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);
 
        // Subscribe to Kafka Topic
        Collection<String> topics = Arrays.asList(KAFKA_TOPIC);
        JavaInputDStream<ConsumerRecord<String, String>> stream =
            KafkaUtils.createDirectStream(
                streamingContext,
                LocationStrategies.PreferConsistent(),
                ConsumerStrategies.Subscribe(topics, kafkaParams)
            );
 
        // Process Each RDD
        stream.foreachRDD(rdd -> {
            rdd.foreach(record -> {
                String json = record.value();
                try {
                    JsonNode node = objectMapper.readTree(json);
                    String city = node.get("name").asText();
                    double temperature = node.get("main").get("temp").asDouble();
                    double humidity = node.get("main").get("humidity").asDouble();
                    long timestamp = node.get("dt").asLong() * 1000L; // Convert to milliseconds
 
                    String formattedTimestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(timestamp));
 
                    // Write to HBase
                    HBaseWriter.write(city, formattedTimestamp, temperature, humidity);
                } catch (Exception e) {
                    System.err.println("Failed to parse JSON: " + json);
                    e.printStackTrace();
                }
            });
        });
 
        // Start Streaming Context
        streamingContext.start();
        streamingContext.awaitTermination();
    }
}
