package dev.m1stwng.polaris.common.normalization;

import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class EmailNormalizer {

    public String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
