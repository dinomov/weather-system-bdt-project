package edu.miu;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

public class HBaseWriter {
    private static Connection connection = null;
    private static final String TABLE_NAME = "weather_data";

    static {
        try {
            Configuration config = HBaseConfiguration.create();
            config.set("hbase.zookeeper.quorum", "localhost"); // Update if needed
            config.set("hbase.zookeeper.property.clientPort", "2181"); // Default port
            connection = ConnectionFactory.createConnection(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void write(String city, String timestamp, double temperature, double humidity) {
        try (Table table = connection.getTable(TableName.valueOf(TABLE_NAME))) {
            Put put = new Put(Bytes.toBytes(city));
            put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("temperature"), Bytes.toBytes(String.valueOf(temperature)));
            put.addColumn(Bytes.toBytes("data"), Bytes.toBytes("humidity"), Bytes.toBytes(String.valueOf(humidity)));
            table.put(put);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}