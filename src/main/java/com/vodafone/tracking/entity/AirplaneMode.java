package com.vodafone.tracking.entity;

import com.vodafone.tracking.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum AirplaneMode {
    ON("ON"), OFF("OFF");

    private String value;

    public static AirplaneMode byValue(final String value) throws ValidationException {
        return Arrays.stream(values())
                .filter(e -> e.getValue().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(ValidationException::new);
    }
}
