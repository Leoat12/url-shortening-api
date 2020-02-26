package com.leoat.urlshorteningapp.model;

import java.util.concurrent.atomic.AtomicInteger;

public class UrlIdRange {

    private Integer initialValue;
    private AtomicInteger currentValue;
    private Integer finalValue;
    private Boolean hasNext;

    public UrlIdRange(Integer counter) {
        this.calculateRange(counter);
        this.hasNext = true;
    }

    public void calculateRange(Integer counter) {
        this.initialValue = counter * 100_000;
        this.currentValue = new AtomicInteger(initialValue);
        this.finalValue = initialValue + 99_999;
        this.hasNext = true;
    }

    public Integer getCurrentValue() {
        if(!hasNext)
            throw new UnsupportedOperationException("No value available in the current range.");

        Integer value = currentValue.getAndIncrement();
        this.hasNext = value < finalValue;
        return value;
    }

    public boolean hasNext() {
        return this.hasNext;
    }

}
