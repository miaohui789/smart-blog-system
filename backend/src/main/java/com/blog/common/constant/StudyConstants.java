package com.blog.common.constant;

import java.math.BigDecimal;

public final class StudyConstants {

    private StudyConstants() {
    }

    public static final Integer YES = 1;
    public static final Integer NO = 0;

    public static final Integer STATUS_DISABLED = 0;
    public static final Integer STATUS_ENABLED = 1;

    public static final Integer REVIEW_DRAFT = 0;
    public static final Integer REVIEW_PENDING = 1;
    public static final Integer REVIEW_PUBLISHED = 2;
    public static final Integer REVIEW_OFFLINE = 3;

    public static final Integer STUDY_STATUS_NOT_STARTED = 0;
    public static final Integer STUDY_STATUS_LEARNING = 1;
    public static final Integer STUDY_STATUS_MASTERED = 2;
    public static final Integer STUDY_STATUS_REVIEW = 3;

    public static final Integer SELF_RESULT_REMEMBER = 1;
    public static final Integer SELF_RESULT_FUZZY = 2;
    public static final Integer SELF_RESULT_FORGET = 3;

    public static final Integer TASK_SOURCE_MANUAL = 1;
    public static final Integer TASK_SOURCE_RECOMMEND = 2;
    public static final Integer TASK_SOURCE_ADMIN = 3;

    public static final Integer CHECK_MODE_RANDOM = 1;
    public static final Integer CHECK_MODE_CATEGORY = 2;
    public static final Integer CHECK_MODE_FAVORITE = 3;
    public static final Integer CHECK_MODE_WEAK = 4;
    public static final Integer CHECK_MODE_WRONG = 5;
    public static final Integer CHECK_MODE_MIXED = 6;

    public static final Integer SCORING_MODE_SELF_ONLY = 1;
    public static final Integer SCORING_MODE_AI_ONLY = 2;
    public static final Integer SCORING_MODE_MIXED = 3;

    public static final Integer TASK_STATUS_PENDING = 0;
    public static final Integer TASK_STATUS_RUNNING = 1;
    public static final Integer TASK_STATUS_COMPLETED = 2;
    public static final Integer TASK_STATUS_TERMINATED = 3;
    public static final Integer TASK_STATUS_EXPIRED = 4;

    public static final Integer TASK_ITEM_STATUS_PENDING = 0;
    public static final Integer TASK_ITEM_STATUS_ANSWERING = 1;
    public static final Integer TASK_ITEM_STATUS_SUBMITTED = 2;
    public static final Integer TASK_ITEM_STATUS_SCORED = 3;
    public static final Integer TASK_ITEM_STATUS_SKIPPED = 4;

    public static final Integer ANSWER_STATUS_DRAFT = 0;
    public static final Integer ANSWER_STATUS_SUBMITTED = 1;
    public static final Integer ANSWER_STATUS_SCORED = 2;
    public static final Integer ANSWER_STATUS_ABANDONED = 3;

    public static final Integer AI_SCORE_STATUS_NONE = 0;
    public static final Integer AI_SCORE_STATUS_RUNNING = 1;
    public static final Integer AI_SCORE_STATUS_SUCCESS = 2;
    public static final Integer AI_SCORE_STATUS_FAILED = 3;

    public static final Integer AI_RESULT_EXCELLENT = 1;
    public static final Integer AI_RESULT_GOOD = 2;
    public static final Integer AI_RESULT_PASS = 3;
    public static final Integer AI_RESULT_WEAK = 4;

    public static final Integer NOTE_TYPE_NORMAL = 1;
    public static final Integer NOTE_TYPE_REVIEW = 2;
    public static final Integer NOTE_TYPE_WRONG = 3;
    public static final Integer NOTE_TYPE_TEMPLATE = 4;

    public static final BigDecimal SELF_SCORE_REMEMBER = new BigDecimal("100");
    public static final BigDecimal SELF_SCORE_FUZZY = new BigDecimal("60");
    public static final BigDecimal SELF_SCORE_FORGET = BigDecimal.ZERO;
}
