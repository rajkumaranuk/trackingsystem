package com.vodafone.tracking.service;

import com.vodafone.tracking.entity.AirplaneMode;
import com.vodafone.tracking.entity.TrackingDataEntity;
import com.vodafone.tracking.exception.DeviceNotLocatedException;
import com.vodafone.tracking.exception.ProductNotFoundException;
import com.vodafone.tracking.mapper.TrackingDataMapper;
import com.vodafone.tracking.model.TrackingData;
import com.vodafone.tracking.repository.TrackingDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Service
public class TrackingService {

    private final TrackingFileParser trackingFileParser;
    private final TrackingDataRepository trackingDataRepository;
    private final TrackingDataMapper trackingDataMapper;

    @Autowired
    public TrackingService(final TrackingFileParser trackingFileParser, final TrackingDataRepository trackingDataRepository, final TrackingDataMapper trackingDataMapper) {
        this.trackingFileParser = trackingFileParser;
        this.trackingDataRepository = trackingDataRepository;
        this.trackingDataMapper = trackingDataMapper;
    }

    public void updateTrackingData(final String filepath) {
        final List<TrackingDataEntity> dataEntities = trackingFileParser.parse(filepath);
        trackingDataRepository.save(dataEntities);
    }

    public TrackingData getTrackerData(final String productId, final Long timestamp, final Boolean cyclePlus) {
        final Supplier<Stream<TrackingDataEntity>> entitySupplier = trackingDataRepository
                .findByProductIdAndTimestamp(productId, timestamp);

        return entitySupplier
                .get()
                .findFirst()
                .map(trackingDataEntity -> {
                    if (trackingDataEntity.getAirplaneMode() == AirplaneMode.OFF &&
                            (trackingDataEntity.getLongitude() == null || trackingDataEntity.getLatitude() == null)) {
                        throw new DeviceNotLocatedException();
                    } else {
                        if (cyclePlus) {
                            return trackingDataMapper.toTrackingData(trackingDataEntity, getStatus(entitySupplier));
                        } else {
                            return trackingDataMapper.toTrackingData(trackingDataEntity, null);
                        }
                    }
                })
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private String getStatus(final Supplier<Stream<TrackingDataEntity>> entitySupplier) {
        String status;
        if (entitySupplier.get().count() == 3) {
            final long longitudeCount = entitySupplier.get().map(TrackingDataEntity::getLongitude).distinct().count();
            final long latitudeCount = entitySupplier.get().map(TrackingDataEntity::getLatitude).distinct().count();

            if (longitudeCount == 3 && latitudeCount == 3) {
                status = "Active";
            } else {
                status = "Inactive";
            }
        } else {
            status = "N/A";
        }
        return status;
    }
}
