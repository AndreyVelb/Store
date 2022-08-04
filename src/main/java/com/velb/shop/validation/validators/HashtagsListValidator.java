package com.velb.shop.validation.validators;

import com.velb.shop.validation.HashtagsList;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class HashtagsListValidator implements ConstraintValidator<HashtagsList, List<String>> {
    @Override
    public boolean isValid(List<String> hashtags, ConstraintValidatorContext context) {
        for (String hashtag : hashtags) {
            if (!StringUtils.hasText(hashtag) || !(hashtag.length() <= 30)) {
                return false;
            }
        }
        return true;
    }
}
