package com.atntv.dp;

import com.ververica.cdc.connectors.postgres.PostgreSQLSource;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Properties;

public class CDCPull {

    public static void main(String[] args) throws Exception {

//        Configuration flinkConfig = new Configuration();
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(flinkConfig);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(30000);

        ParameterTool parameterTool = ParameterTool.fromArgs(args);
        boolean snapshot = parameterTool.getBoolean("snapshot", false);
        String tblList = parameterTool.get("tbls", "");


        Properties debeziumProps = new Properties();
        if (!snapshot) {
            debeziumProps.setProperty("snapshot.mode", "never");
        }
        debeziumProps.setProperty("publication.name", "flink_cdc");

        SourceFunction<String> sourceFunction = PostgreSQLSource.<String>builder()
                .hostname("identity-dev-upgrade.cbq3e3lxsohx.us-east-1.rds.amazonaws.com")
                .port(5432)
                .database("postgres")
                .schemaList("public")
                .tableList(tblList)
                //.tableList("public.identity_partition_1237, public.merged_user_relationships")
                .username("data_extractor")
                .password("******")
                .decodingPluginName("pgoutput")
                .deserializer(new JsonDebeziumDeserializationSchema())
                .slotName("flink_cdc_slot")

                .debeziumProperties(debeziumProps)

                .build();

        FileSink<String> fileSink =
                FileSink.<String>forRowFormat(
                                new Path("s3://attentive-datalake-dev/sdeva-ftl/cdc/"),
                                //new Path("cdc-sink"),
                                new SimpleStringEncoder<>())
                        .build();

        env.addSource(sourceFunction).sinkTo(fileSink).setParallelism(2);
        //env.addSource(sourceFunction).print();

        env.execute();
    }
}