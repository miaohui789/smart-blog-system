package com.blog.common.constant;

import com.blog.util.UserLevelUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ExpConstants {

    private ExpConstants() {
    }

    public static final String BIZ_STUDY_RECORD = "STUDY_RECORD";
    public static final String BIZ_CHECK_FINISH = "CHECK_FINISH";
    public static final String BIZ_CHECK_EXCELLENT = "CHECK_EXCELLENT";
    public static final String BIZ_ARTICLE_PUBLISH = "ARTICLE_PUBLISH";
    public static final String BIZ_COMMENT_CREATE = "COMMENT_CREATE";
    public static final String BIZ_USER_FOLLOW = "USER_FOLLOW";
    public static final String BIZ_LOGIN_DAILY = "LOGIN_DAILY";
    public static final String BIZ_ARTICLE_LIKE = "ARTICLE_LIKE";
    public static final String BIZ_ARTICLE_FAVORITE = "ARTICLE_FAVORITE";
    public static final String BIZ_SIGN_DAILY = "SIGN_DAILY";
    public static final String BIZ_SIGN_STREAK_BONUS = "SIGN_STREAK_BONUS";

    public static final int EXP_STUDY_RECORD = 2;
    public static final int EXP_CHECK_FINISH = 10;
    public static final int EXP_CHECK_EXCELLENT = 5;
    public static final int EXP_ARTICLE_PUBLISH = 15;
    public static final int EXP_COMMENT_CREATE = 2;
    public static final int EXP_USER_FOLLOW = 1;
    public static final int EXP_LOGIN_DAILY = 3;
    public static final int EXP_ARTICLE_LIKE = 1;
    public static final int EXP_ARTICLE_FAVORITE = 1;
    public static final int EXP_SIGN_DAILY = 50;

    public static final int MAX_LEVEL = UserLevelUtils.MAX_LEVEL;
    public static final String REDIS_EXP_RANK_TOTAL = "blog:exp:rank:total";

    public static final Map<String, Integer> BIZ_DAILY_CAP;
    public static final Map<Integer, Integer> SIGN_STREAK_REWARD;

    static {
        Map<String, Integer> caps = new HashMap<>();
        caps.put(BIZ_STUDY_RECORD, 20);
        caps.put(BIZ_CHECK_FINISH, 50);
        caps.put(BIZ_CHECK_EXCELLENT, 20);
        caps.put(BIZ_ARTICLE_PUBLISH, 30);
        caps.put(BIZ_COMMENT_CREATE, 20);
        caps.put(BIZ_USER_FOLLOW, 10);
        caps.put(BIZ_LOGIN_DAILY, 3);
        caps.put(BIZ_ARTICLE_LIKE, 20);
        caps.put(BIZ_ARTICLE_FAVORITE, 20);
        caps.put(BIZ_SIGN_DAILY, 50);
        caps.put(BIZ_SIGN_STREAK_BONUS, 200);
        BIZ_DAILY_CAP = Collections.unmodifiableMap(caps);

        Map<Integer, Integer> streakReward = new LinkedHashMap<>();
        streakReward.put(3, 20);
        streakReward.put(7, 50);
        streakReward.put(15, 100);
        streakReward.put(30, 200);
        SIGN_STREAK_REWARD = Collections.unmodifiableMap(streakReward);
    }
}
