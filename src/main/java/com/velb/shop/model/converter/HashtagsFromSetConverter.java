package com.velb.shop.model.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import javax.persistence.Column;
import java.util.Set;

@Component
public class HashtagsFromSetConverter implements Converter<Set<String>, String> {

    @Override
    public String convert(Set<String> source) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean isFirstElement = true;
        for (String hashtag : source) {
            if (isFirstElement) {
                stringBuilder.append(hashtag);
                isFirstElement = false;
            } else {
                stringBuilder.append("#").append(hashtag);
            }
        }
        return stringBuilder.toString();
    }

}
