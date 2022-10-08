package com.velb.shop.model.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class HashtagsToSetConverter implements Converter<String, Set<String>> {
    String SEPARATOR = "#";

    @Override
    public Set<String> convert(String source) {
        return Set.of(source.split(SEPARATOR));
    }

}
