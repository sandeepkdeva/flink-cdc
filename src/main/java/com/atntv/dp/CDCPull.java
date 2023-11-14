package com.atntv.dp;

import com.ververica.cdc.connectors.postgres.PostgreSQLSource;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Properties;

public class CDCPull {

    public static void main(String[] args) throws Exception {

        Configuration flinkConfig = new Configuration();
        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(flinkConfig);
        env.enableCheckpointing(30000);

        Properties debeziumProps = new Properties();
        debeziumProps.setProperty("snapshot.mode", "never");

        SourceFunction<String> sourceFunction = PostgreSQLSource.<String>builder()
                .hostname("localhost")
                .port(5432)
                .database("postgres")
                .schemaList("public")
                .username("postgres")
                .password("postgres")
                .decodingPluginName("pgoutput")
                .deserializer(new JsonDebeziumDeserializationSchema())
                .debeziumProperties(debeziumProps)
                .build();

        FileSink<String> fileSink =
                FileSink.<String>forRowFormat(
                                new Path("cdc-sink"),
                                new SimpleStringEncoder<>())
                        .build();

        //env.addSource(sourceFunction).sinkTo(fileSink).setParallelism(2);
        env.addSource(sourceFunction).print();

        env.execute();
    }
}