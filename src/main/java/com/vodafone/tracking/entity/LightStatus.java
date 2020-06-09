package com.vodafone.tracking.entity;

import com.vodafone.tracking.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum LightStatus {
    ON("ON"), OFF("OFF"), NA("N/A");

    private String value;

    public static LightStatus byValue(final String value) throws ValidationException {
        return Arrays.stream(values())
                .filter(e -> e.getValue().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(ValidationException::new);
    }
}
