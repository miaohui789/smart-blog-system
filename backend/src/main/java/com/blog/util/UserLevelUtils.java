package com.blog.util;

public final class UserLevelUtils {

    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 100;

    private UserLevelUtils() {
    }

    public static int normalize(Integer level) {
        if (level == null) {
            return MIN_LEVEL;
        }
        if (level < MIN_LEVEL) {
            return MIN_LEVEL;
        }
        return Math.min(level, MAX_LEVEL);
    }

    public static int getTier(Integer level) {
        return (normalize(level) - 1) / 10 + 1;
    }
}
