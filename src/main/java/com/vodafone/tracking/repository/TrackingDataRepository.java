package com.vodafone.tracking.repository;

import com.vodafone.tracking.entity.TrackingDataEntity;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Repository
public class TrackingDataRepository {

    private List<TrackingDataEntity> trackingDataEntities = new ArrayList<>();

    public void save(List<TrackingDataEntity> trackingDataEntities) {
        this.trackingDataEntities = trackingDataEntities;
    }

    public Supplier<Stream<TrackingDataEntity>> findByProductIdAndTimestamp(final String productId, final Long timestamp) {
        return () -> trackingDataEntities.stream()
                .filter(e -> e.getProductId().equalsIgnoreCase(productId))
                .filter(e -> Timestamp.valueOf(e.getEventTime()).getTime() <= timestamp)
                .limit(3)
                .sorted(Comparator.comparing(TrackingDataEntity::getEventTime).reversed());
    }
}
