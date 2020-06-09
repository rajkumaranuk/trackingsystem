package com.vodafone.tracking.exception;

import lombok.Getter;

@Getter
public class ProductNotFoundException extends RuntimeException {
    final String productId;
    public ProductNotFoundException(final String productId) {
        this.productId = productId;
    }
}
