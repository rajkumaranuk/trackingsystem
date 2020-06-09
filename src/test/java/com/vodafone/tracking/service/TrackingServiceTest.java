package com.vodafone.tracking.service;

import com.vodafone.tracking.entity.AirplaneMode;
import com.vodafone.tracking.entity.LightStatus;
import com.vodafone.tracking.entity.TrackingDataEntity;
import com.vodafone.tracking.exception.ValidationException;
import com.vodafone.tracking.mapper.TrackingDataMapper;
import com.vodafone.tracking.model.TrackingData;
import com.vodafone.tracking.repository.TrackingDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static com.vodafone.tracking.mapper.TrackingDataMapper.DATE_TIME_FORMATTER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrackingServiceTest {

    private TrackingService trackingService;

    @Mock
    private TrackingFileParser trackingFileParser;

    @Mock
    private TrackingDataMapper trackingDataMapper;

    @Mock
    private TrackingDataRepository trackingDataRepository;

    @BeforeEach
    public void init() {
        trackingService = new TrackingService(trackingFileParser, trackingDataRepository, trackingDataMapper);
    }

    @Test
    public void uploadTrackingDataSuccessfully() {
        final List<TrackingDataEntity> dataEntities = trackingDataEntities();
        when(trackingFileParser.parse("data.csv")).thenReturn(dataEntities);
        trackingService.updateTrackingData("data.csv");
        verify(trackingDataRepository).save(dataEntities);
    }

    @Test()
    public void uploadTrackingDataFailsDueToParserError() {
        when(trackingFileParser.parse("data.csv")).thenThrow(new ValidationException());
        assertThrows(ValidationException.class, () -> {
            trackingService.updateTrackingData("data.csv");
        });
    }

    @Test
    public void getTrackingDataSuccessfully() {
        final TrackingData data = trackingData("WG11155638", "CyclePlusTracker", "Inactive", "Full", "SUCCESS: Location identified", "25/02/2020 04:33:17", -0.1736, 51.5185);
        when(trackingDataRepository.findByProductIdAndTimestamp("WG11155638", 1582605077000l)).thenReturn(() -> trackingDataEntities().stream());
        when(trackingDataMapper.toTrackingData(any(TrackingDataEntity.class), eq(null))).thenReturn(data);
        final TrackingData trackingData = trackingService.getTrackerData("WG11155638", 1582605077000l, false);

        assertThat(trackingData)
                .extracting("id", "name", "status", "battery", "description", "eventTime", "longitude", "latitude")
                .contains("WG11155638", "CyclePlusTracker", "Inactive", "Full", "SUCCESS: Location identified", "25/02/2020 04:33:17", -0.1736, 51.5185);

    }

    private List<TrackingDataEntity> trackingDataEntities() {
        return Arrays.asList(
                trackingDataEntity(1582605075000l, 10001, "WG11155638", 51.5185, -0.1736, 0.99, LightStatus.OFF, AirplaneMode.OFF),
                trackingDataEntity(1582605076000l, 10002, "WG11155638", 51.5185, -0.1736, 0.99, LightStatus.OFF, AirplaneMode.OFF),
                trackingDataEntity(1582605077000l, 10003, "WG11155638", 51.5185, -0.1736, 0.99, LightStatus.OFF, AirplaneMode.OFF));
    }

    private TrackingDataEntity trackingDataEntity(final long eventTime, final int eventId, final String productId,
                                                  final double latitude, final double longitude, double battery,
                                                  final LightStatus lightStatus, final AirplaneMode airplaneMode) {
        return TrackingDataEntity.builder()
                .eventTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(eventTime), ZoneOffset.UTC))
                .eventId(eventId)
                .productId(productId)
                .latitude(latitude)
                .longitude(longitude)
                .battery(battery)
                .lightStatus(lightStatus)
                .airplaneMode(airplaneMode)
                .build();
    }

    private TrackingData trackingData(final String productId, final String name, final String status, final String battery,
                                      final String description, final String eventTime, final double latitude, final double longitude) {
        return TrackingData.builder()
                .eventTime(eventTime)
                .id(productId)
                .name(name)
                .latitude(latitude)
                .longitude(longitude)
                .battery(battery)
                .status(status)
                .description(description)
                .build();
    }


}
