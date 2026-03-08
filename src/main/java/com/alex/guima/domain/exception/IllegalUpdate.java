package com.alex.guima.domain.exception;

import jakarta.ws.rs.BadRequestException;

public class IllegalUpdate extends BadRequestException {
    public IllegalUpdate(String message) {
        super("Illegal update: " + message);
    }
}
