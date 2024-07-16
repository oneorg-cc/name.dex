package com.name.dex.utils.serialization;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Optional;

public class RecordSerializer<R extends Record> extends StdSerializer<R> {

    public RecordSerializer() {
        this(null);
    }

    public RecordSerializer(Class<R> t) {
        super(t);
    }

    @Override
    public void serialize(R r, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        for(RecordComponent component : r.getClass().getRecordComponents()) {
            try {
                JsonProperty propertyAnnotation = component.getAccessor().getAnnotation(JsonProperty.class);
                String name = propertyAnnotation == null ? component.getName() : propertyAnnotation.value();
                Object value = component.getAccessor().invoke(r);

                boolean valueWritable = true;
                if (value instanceof Optional<?> valueOptional) {
                    if ((valueWritable = valueOptional.isPresent())) {
                        value = valueOptional.get();
                    }
                }

                if (valueWritable) {
                    jsonGenerator.writeObjectField(name, value);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        jsonGenerator.writeEndObject();
    }

}
