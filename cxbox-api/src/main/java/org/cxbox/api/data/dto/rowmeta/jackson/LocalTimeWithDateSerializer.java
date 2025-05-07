package org.cxbox.api.data.dto.rowmeta.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.cxbox.api.config.CxboxBeanProperties;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocalTimeWithDateSerializer extends JsonSerializer<LocalTime> {

	private final CxboxBeanProperties properties;

	@Override
	public void serialize(LocalTime localTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
			throws IOException {
		try {
			jsonGenerator.writeObject(localTime.atDate(properties.getDefaultDate()));
		} catch (BeansException e) {
			jsonGenerator.writeObject(localTime);
		}
	}

}
