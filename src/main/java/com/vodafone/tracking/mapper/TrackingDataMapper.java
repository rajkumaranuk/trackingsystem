package com.vodafone.tracking.mapper;

import com.vodafone.tracking.entity.AirplaneMode;
import com.vodafone.tracking.entity.TrackingDataEntity;
import com.vodafone.tracking.model.TrackingData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper
public interface TrackingDataMapper {

    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    default TrackingData toTrackingData(final TrackingDataEntity trackingDataEntity, final String status) {
        final TrackingData trackingData = toTrackingData(trackingDataEntity);

        if (trackingDataEntity.getAirplaneMode().equals(AirplaneMode.ON)) {
            trackingData.setLongitude(null);
            trackingData.setLatitude(null);
            trackingData.setDescription("SUCCESS: Location not available: Please turn off airplane mode");
        } else {
            trackingData.setDescription("SUCCESS: Location identified");
        }

        if (status == null) {
            if (trackingData.getLongitude() == null || trackingData.getLatitude() == null) {
                trackingData.setStatus("Inactive");
            } else {
                trackingData.setStatus("Active");
            }
        } else {
            trackingData.setStatus(status);
        }
        return trackingData;
    }

    @Mapping(source = "trackingDataEntity.productId", target = "id")
    @Mapping(source = "trackingDataEntity.productId", target = "name", qualifiedByName = "name")
    @Mapping(source = "trackingDataEntity.eventTime", target = "eventTime", qualifiedByName = "eventTime")
    @Mapping(source = "trackingDataEntity.battery", target = "battery", qualifiedByName = "battery")
    TrackingData toTrackingData(final TrackingDataEntity trackingDataEntity);

    @Named("name")
    default String productName(final String productId) {
        return productId.startsWith("WG") ? "CyclePlusTracker" : "GeneralTracker";
    }

    @Named("eventTime")
    default String eventTime(final LocalDateTime eventTime) {
        return eventTime.format(DATE_TIME_FORMATTER);
    }

    @Named("battery")
    default String battery(final Double battery) {
        if (battery >= 0.90) {
            return "Full";
        } else if (battery >= 0.60) {
            return "High";
        } else if (battery >= 0.40) {
            return "Medium";
        } else if (battery >= 0.10) {
            return "Low";
        } else {
            return "Critical";
        }
    }
}
