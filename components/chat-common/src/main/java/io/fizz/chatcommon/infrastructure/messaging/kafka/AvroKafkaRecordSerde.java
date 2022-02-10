package io.fizz.chatcommon.infrastructure.messaging.kafka;

import io.vertx.kafka.client.consumer.KafkaConsumerRecord;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Objects;

public class AvroKafkaRecordSerde {
    public static KafkaProducerRecord<byte[],byte[]> create(final String aTopic,
                                                     final byte[] aKey,
                                                     final byte[] aPayload) throws IOException {
        byte[] record = serialize(aPayload);

        if (Objects.nonNull(aKey)) {
            return KafkaProducerRecord.create(aTopic, aKey, record);
        }
        else {
            return KafkaProducerRecord.create(aTopic, record);
        }
    }

    public static byte[] payload(final KafkaConsumerRecord<byte[], byte[]> aRecord) throws IOException {
        if (Objects.isNull(aRecord)) {
            throw new IllegalArgumentException("invalid record");
        }

        return deserialize(aRecord.value());
    }

    public static byte[] payload(final ConsumerRecord<byte[], byte[]> aRecord) throws IOException {
        if (Objects.isNull(aRecord)) {
            throw new IllegalArgumentException("invalid record");
        }

        return deserialize(aRecord.value());
    }

    static byte[] serialize(final byte[] aValue) throws IOException {
        final HBaseKafkaEvent event = new HBaseKafkaEvent();
        event.setValue(ByteBuffer.wrap(aValue));
        event.setDelete(false);
        event.setKey(ByteBuffer.wrap(new byte[1]));
        event.setFamily(ByteBuffer.wrap(new byte[1]));
        event.setQualifier(ByteBuffer.wrap(new byte[1]));
        event.setTable(ByteBuffer.wrap(new byte[1]));
        event.setTimestamp(new Date().getTime());


        final DatumWriter<HBaseKafkaEvent> writer = new SpecificDatumWriter<>(HBaseKafkaEvent.getClassSchema());
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(stream, null);
        writer.write(event, encoder);
        encoder.flush();
        return stream.toByteArray();
    }

    static byte[] deserialize(final byte[] aRecord) throws IOException {
        final DatumReader<HBaseKafkaEvent> reader = new SpecificDatumReader<>(HBaseKafkaEvent.getClassSchema());
        Decoder decoder = DecoderFactory.get().binaryDecoder(aRecord, null);
        GenericRecord event = reader.read(null, decoder);
        if (Objects.isNull(event)) {
            return null;
        }

        return ((ByteBuffer)event.get("value")).array();
    }
}
