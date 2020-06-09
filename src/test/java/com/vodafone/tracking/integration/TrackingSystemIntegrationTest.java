package com.vodafone.tracking.integration;

import com.vodafone.tracking.model.StandardResponse;
import com.vodafone.tracking.model.UploadTrackingDataRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TrackingSystemIntegrationTest {

    private static final String URL = "/event/v1/";
    private static final String GET_URL_V1 = "/event/v1?productId={productId}&tstmp={tstmp}";
    private static final String GET_URL_V2 = "/event/v2?productId={productId}&tstmp={tstmp}";

    @Autowired
    protected TestRestTemplate testRestTemplate;

    @Test
    public void uploadTrackingDataSuccessfully() {
        final ResponseEntity<StandardResponse> response = testRestTemplate.postForEntity(URL,
                new HttpEntity<>(new UploadTrackingDataRequest("data.csv")), StandardResponse.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDescription()).isEqualTo("data refreshed");
    }

    @Test
    public void uploadInvalidFilePathReturns404Error() {
        final ResponseEntity<StandardResponse> response = testRestTemplate.postForEntity(URL,
                new HttpEntity<>(new UploadTrackingDataRequest("nofile.csv")), StandardResponse.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDescription()).isEqualTo("ERROR: No data file found");
    }

    @Test
    public void uploadInvalidFileContentReturns500Error() {
        final ResponseEntity<StandardResponse> response = testRestTemplate.postForEntity(URL,
                new HttpEntity<>(new UploadTrackingDataRequest("data_invalid.csv")), StandardResponse.class);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDescription()).isEqualTo("ERROR: A technical exception occurred");
    }

    @Test
    public void getTrackingDataByProductIdAndMatchingTimestamp() {
        uploadTrackingDataSuccessfully();

        final ResponseEntity<Map> response = testRestTemplate.getForEntity(GET_URL_V1, Map.class, "WG11155800", 1582605437000l);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .extracting("id", "name", "status", "battery", "description", "datetime", "long", "lat")
                .contains("WG11155800", "CyclePlusTracker", "Active", "Critical", "SUCCESS: Location identified", "25/02/2020 04:37:17", -12.52025, 45.5187);
    }

    @Test
    public void getTrackingDataByProductIdAndNonMatchingTimestamp() {
        uploadTrackingDataSuccessfully();

        final ResponseEntity<Map> response = testRestTemplate.getForEntity(GET_URL_V1, Map.class, "WG11155800", 1582605437001l);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .extracting("id", "name", "status", "battery", "description", "datetime", "long", "lat")
                .contains("WG11155800", "CyclePlusTracker", "Active", "Critical", "SUCCESS: Location identified", "25/02/2020 04:37:17", -12.52025, 45.5187);
    }

    @Test
    public void getTrackingDataByProductIdOnly() {
        uploadTrackingDataSuccessfully();

        final ResponseEntity<Map> response = testRestTemplate.getForEntity(GET_URL_V1, Map.class, "WG11155800", null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .extracting("id", "name", "status", "battery", "description", "datetime", "long", "lat")
                .contains("WG11155800", "CyclePlusTracker", "Active", "Critical", "SUCCESS: Location identified", "25/02/2020 04:37:17", -12.52025, 45.5187);
    }

    @Test
    public void getTrackingDataWithAirplaneModeOn() {
        uploadTrackingDataSuccessfully();

        final ResponseEntity<Map> response = testRestTemplate.getForEntity(GET_URL_V1, Map.class, "6900233111", 1582605615000l);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .extracting("id", "name", "status", "battery", "description", "datetime", "long", "lat")
                .contains("6900233111", "GeneralTracker", "Inactive", "Low", "SUCCESS: Location not available: Please turn off airplane mode", "25/02/2020 04:40:15", null, null);
    }

    @Test
    public void getTrackingDataWithAirplaneModeOnAndMissingGpsDataReturns400Error() {
        uploadTrackingDataSuccessfully();

        final ResponseEntity<StandardResponse> response = testRestTemplate.getForEntity(GET_URL_V1, StandardResponse.class, "6900233111", 1582612875000l);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDescription()).isEqualTo("ERROR: Device could not be located");
    }

    @Test
    public void getTrackingDataWithInvalidProductIdReturns404Error() {
        uploadTrackingDataSuccessfully();

        final ResponseEntity<StandardResponse> response = testRestTemplate.getForEntity(GET_URL_V1, StandardResponse.class, "1111111111", null);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDescription()).isEqualTo("ERROR: Id 1111111111 not found");
    }

    @Test
    public void getCyclePlusTrackingDataWhenNotMoving() {
        uploadTrackingDataSuccessfully();

        final ResponseEntity<Map> response = testRestTemplate.getForEntity(GET_URL_V2, Map.class, "WG11155638", 1582605257000l);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .extracting("id", "name", "status", "battery", "description", "datetime", "long", "lat")
                .contains("WG11155638", "CyclePlusTracker", "Inactive", "Full", "SUCCESS: Location identified", "25/02/2020 04:33:17", -0.1736, 51.5185);
    }

    @Test
    public void getCyclePlusTrackingDataWhenMoving() {
        uploadTrackingDataSuccessfully();

        final ResponseEntity<Map> response = testRestTemplate.getForEntity(GET_URL_V2, Map.class, "WG11155800", 1582605437000l);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .extracting("id", "name", "status", "battery", "description", "datetime", "long", "lat")
                .contains("WG11155800", "CyclePlusTracker", "Active", "Critical", "SUCCESS: Location identified", "25/02/2020 04:37:17", -12.52025, 45.5187);
    }

    @Test
    public void getCyclePlusTrackingDataWhenNotEnoughGpsData() {
        uploadTrackingDataSuccessfully();

        final ResponseEntity<Map> response = testRestTemplate.getForEntity(GET_URL_V2, Map.class, "WG11155630", 1582612876000l);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .extracting("id", "name", "status", "battery", "description", "datetime", "long", "lat")
                .contains("WG11155630", "CyclePlusTracker", "N/A", "Full", "SUCCESS: Location identified", "25/02/2020 06:41:16", -0.17538, 51.5185);
    }
}
