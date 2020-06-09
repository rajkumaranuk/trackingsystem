package com.vodafone.tracking.api;

import com.vodafone.tracking.model.StandardResponse;
import com.vodafone.tracking.model.TrackingData;
import com.vodafone.tracking.model.UploadTrackingDataRequest;
import com.vodafone.tracking.service.TrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Optional;

@RestController
public class TrackingController {

    @Autowired
    private TrackingService trackingService;

    @PostMapping(path = "/event/v1/", consumes = "application/json", produces = "application/json")
    public @ResponseBody StandardResponse uploadTrackingData(@RequestBody final UploadTrackingDataRequest request) {
        trackingService.updateTrackingData(request.getFilepath());
        return new StandardResponse("data refreshed");
    }

    @GetMapping(path = "/event/v1", produces = "application/json")
    public @ResponseBody TrackingData getTrackerData(@RequestParam final String productId, @RequestParam final Optional<Long> tstmp) {
        return trackingService.getTrackerData(productId, tstmp.orElse(Instant.now().toEpochMilli()), false);
    }

    @GetMapping(path = "/event/v2", produces = "application/json")
    public @ResponseBody TrackingData getCyclePlusTrackerData(@RequestParam final String productId, @RequestParam final Optional<Long> tstmp) {
        return trackingService.getTrackerData(productId, tstmp.orElse(Instant.now().toEpochMilli()), true);
    }
}

