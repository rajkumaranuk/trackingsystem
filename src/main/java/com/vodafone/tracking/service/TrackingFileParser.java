package com.vodafone.tracking.service;

import com.vodafone.tracking.entity.AirplaneMode;
import com.vodafone.tracking.entity.LightStatus;
import com.vodafone.tracking.entity.TrackingDataEntity;
import com.vodafone.tracking.exception.FileNotFoundException;
import com.vodafone.tracking.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TrackingFileParser {

    private static final String[] HEADER = {"DateTime", "EventId", "ProductId", "Latitude", "Longitude", "Battery", "Light", "AirplaneMode"};

    public List<TrackingDataEntity> parse(final String filename) {
        try (final BufferedReader bufferedReader = new BufferedReader(new FileReader(toFile(filename)))) {
            skipLine(bufferedReader);
            final CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withHeader(HEADER).withFirstRecordAsHeader();
            final CSVParser parser = new CSVParser(bufferedReader, format);
            final List<CSVRecord> records = parser.getRecords();

            return records.stream()
                    .map(this::parse)
                    .collect(Collectors.toList());
        } catch (final IOException e) {
            throw new ValidationException();
        }
    }

    private void skipLine(final BufferedReader bufferedReader) throws IOException {
        final String line = bufferedReader.readLine();
        log.info("Read initial line: " + line);
    }

    private File toFile(final String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            final URL fileURI = TrackingFileParser.class.getClassLoader().getResource(filename);
            if (fileURI != null) {
                file = new File(fileURI.getFile());
            }
        }
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        return file;
    }

    private TrackingDataEntity parse(CSVRecord record) {
        final LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(record.get("DateTime"))), ZoneOffset.UTC);
        final int eventId = Integer.parseInt(record.get("EventId"));
        final String productId = record.get("ProductId");
        final Double latitude = getDoubleValue(record.get("Latitude"));
        final Double longitude = getDoubleValue(record.get("Longitude"));
        final Double battery = getDoubleValue(record.get("Battery"));
        final LightStatus light = LightStatus.byValue(record.get("Light"));
        final AirplaneMode airplaneMode = AirplaneMode.byValue(record.get("AirplaneMode"));

        return TrackingDataEntity.builder()
                .eventTime(dateTime)
                .eventId(eventId)
                .productId(productId)
                .latitude(latitude)
                .longitude(longitude)
                .battery(battery)
                .lightStatus(light)
                .airplaneMode(airplaneMode)
                .build();
    }

    private Double getDoubleValue(final String value) {
        if (value == null || value.length() == 0) {
            return null;
        } else {
            return Double.parseDouble(value);
        }
    }
}
