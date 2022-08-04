package com.velb.shop.exception;

import java.util.List;

public class TotalRuntimeException extends RuntimeException {
    private final List<RuntimeException> exceptionList;

    public TotalRuntimeException(List<RuntimeException> exceptionList) {
        this.exceptionList = exceptionList;
    }


    public void addExceptionToList(RuntimeException ex) {
        exceptionList.add(ex);
    }

    public List<RuntimeException> getExceptionList() {
        return exceptionList;
    }

    @Override
    public String getMessage() {
        StringBuilder messageBuilder = new StringBuilder();
        exceptionList.forEach(ex -> messageBuilder.append(ex.getMessage()));
        return messageBuilder.toString();
    }
}
