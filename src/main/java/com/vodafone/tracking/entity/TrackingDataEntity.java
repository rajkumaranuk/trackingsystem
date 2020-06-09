package com.vodafone.tracking.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingDataEntity {
    private LocalDateTime eventTime;
    private Integer eventId;
    private String productId;
    private Double longitude;
    private Double latitude;
    private Double battery;
    private LightStatus lightStatus;
    private AirplaneMode airplaneMode;
}