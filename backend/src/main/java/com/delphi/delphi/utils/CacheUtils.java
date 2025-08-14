package com.delphi.delphi.utils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class CacheUtils {
    public static String normalizeDateTime(LocalDateTime dt) {
        if (dt == null) {
            return "null";
        }
        return dt.truncatedTo(ChronoUnit.DAYS).toString();
    }

    public static String normalizeStringList(List<String> skills) {
        if (skills == null) {
            return "null";
        }
        return skills.stream().sorted().collect(Collectors.joining(","));
    }
}
