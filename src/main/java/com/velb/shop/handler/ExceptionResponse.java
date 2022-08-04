package com.velb.shop.handler;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ExceptionResponse {

    @JsonProperty(value = "exceptionMessage")
    private final String exceptionMessage;

}
