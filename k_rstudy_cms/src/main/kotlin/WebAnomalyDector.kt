package com.krstudy.kapi


import com.fasterxml.jackson.databind.ObjectMapper
import com.krstudy.kapi.domain.kafkaproducer.entity.WebLog
import org.apache.flink.api.common.eventtime.WatermarkStrategy
import org.apache.flink.api.common.typeinfo.Types
import org.apache.flink.api.java.tuple.Tuple2
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema
import org.apache.flink.connector.kafka.sink.KafkaSink
import org.apache.flink.connector.kafka.source.KafkaSource
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer
import org.apache.flink.streaming.api.datastream.DataStream
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.api.common.serialization.SimpleStringSchema
class WebAnomalyDector {
    fun main(args: Array<String>) {
        // 1. Flink 환경 설정
        val env = StreamExecutionEnvironment.getExecutionEnvironment()

        // 2. Kafka 소스 설정
        val kafkaSource = KafkaSource.builder<String>()
            .setBootstrapServers("localhost:9092")
            .setTopics("weblog_for_ad")
            .setGroupId("anomaly_detector")
            .setValueOnlyDeserializer(SimpleStringSchema())
            .setStartingOffsets(OffsetsInitializer.earliest())
            .build()

        // 3. Kafka에서 WebLog 객체로 변환
        val webLogDataStream: DataStream<WebLog> = env.fromSource(
            kafkaSource,
            WatermarkStrategy.forMonotonousTimestamps(),
            "kafkaSource"
        ).map { jsonString ->
            val objectMapper = ObjectMapper()
            objectMapper.readValue(jsonString, WebLog::class.java)
        }

        // 4. Window Count 10초(Term), 1분(Window)
        val windowAnomalies: DataStream<String> = webLogDataStream
            .map { log -> Tuple2(log.ipAddress, 1) } // IP 카운트
            .returns(Types.TUPLE(Types.STRING, Types.INT))
            .keyBy { value -> value.f0 } // IP 기준 그룹화
            .window(SlidingProcessingTimeWindows.of(Time.minutes(1), Time.seconds(10))) // 1분 윈도우, 10초 슬라이딩
            .sum(1) // 카운트 합산
            .filter { ipCount -> ipCount.f1 > 20 } // 20번 이상 접속 감지
            .map { ipCount -> "Detected Anomaly Access from IP : ${ipCount.f0}, tries ${ipCount.f1} access." }

        // 5. Kafka Sink 설정 (Alert 전송)
        val kafkaSink = KafkaSink.builder<String>()
            .setBootstrapServers("localhost:9092")
            .setRecordSerializer(
                KafkaRecordSerializationSchema.builder<String>()
                    .setTopic("alert")
                    .setValueSerializationSchema(SimpleStringSchema())
                    .build()
            )
            .build()

        // 6. Anomalies를 Kafka에 전송
        windowAnomalies.sinkTo(kafkaSink)

        // Flink Job 실행
        env.execute("batch")
    }
}