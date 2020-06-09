package com.vodafone.tracking.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingData {
    private String id;
    private String name;
    @JsonProperty("datetime")
    private String eventTime;
    @JsonProperty("long")
    private Double longitude;
    @JsonProperty("lat")
    private Double latitude;
    private String status;
    private String battery;
    private String description;
}