#!/usr/bin/env python3
"""
Import interview question markdown files into the enterprise study module tables.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import subprocess
import sys
import textwrap
from dataclasses import dataclass
from pathlib import Path
from typing import Any

try:
    import pymysql  # type: ignore
except ImportError:
    pymysql = None


ROOT_DIR = Path(__file__).resolve().parents[1]
DEFAULT_SOURCE_DIR = ROOT_DIR / "面试题大全md"
DEFAULT_BOOTSTRAP_SQL = ROOT_DIR / "docs" / "database" / "study_module_enterprise.sql"
DEFAULT_CONFIG_FILE = ROOT_DIR / "backend" / "src" / "main" / "resources" / "application-dev.yml"

ROOT_CATEGORY_CODE = "interview-root"
ROOT_CATEGORY_NAME = "面试题题库"
DEFAULT_RUBRIC = {
    "dimensions": [
        {"code": "accuracy", "name": "准确性", "score": 40},
        {"code": "completeness", "name": "完整性", "score": 30},
        {"code": "clarity", "name": "表达清晰度", "score": 20},
        {"code": "practicality", "name": "实战性", "score": 10},
    ],
    "levels": {
        "excellent": {"min": 90, "label": "优秀"},
        "good": {"min": 75, "label": "良好"},
        "pass": {"min": 60, "label": "及格"},
        "weak": {"min": 0, "label": "待提升"},
    },
}

FILE_CATEGORY_MAPPING = {
    "Java八股文200道-Java基础篇.md": ("java-basic", "Java基础篇"),
    "Java八股文200道-框架篇.md": ("java-framework", "框架篇"),
    "Java八股文200道-数据库篇.md": ("java-database", "数据库篇"),
    "Java八股文200道-微服务篇.md": ("java-microservice", "微服务篇"),
    "Java八股文200道-MQ和ES篇.md": ("java-mq-es", "MQ和ES篇"),
    "Java八股文200道-Linux服务器篇.md": ("java-linux", "Linux服务器篇"),
    "Java八股文200道-补充篇.md": ("java-extra", "补充篇"),
}


@dataclass
class DatabaseConfig:
    host: str
    port: int
    database: str
    username: str
    password: str
    mysql_bin: str = "mysql"


@dataclass
class ParsedQuestion:
    file_name: str
    category_code: str
    category_name: str
    source_section: str
    question_no: int | None
    source_ref_no: str | None
    title: str
    standard_answer: str
    answer_summary: str
    keywords: str
    difficulty: int
    answer_word_count: int
    estimated_minutes: int
    question_code: str
    raw_payload: str


class BaseAdapter:
    def execute(self, sql: str) -> None:
        raise NotImplementedError

    def query(self, sql: str) -> list[list[str]]:
        raise NotImplementedError

    def insert_and_get_id(self, sql: str) -> int:
        rows = self.query(f"{sql.rstrip().rstrip(';')};\nSELECT LAST_INSERT_ID();")
        if not rows:
            raise RuntimeError("LAST_INSERT_ID() returned empty result")
        return int(rows[-1][0])

    def close(self) -> None:
        return None


class MySQLCliAdapter(BaseAdapter):
    def __init__(self, config: DatabaseConfig):
        self.config = config

    def _run(self, sql: str) -> subprocess.CompletedProcess[str]:
        cmd = [
            self.config.mysql_bin,
            "--default-character-set=utf8mb4",
            "--batch",
            "--raw",
            "--skip-column-names",
            "-h",
            self.config.host,
            "-P",
            str(self.config.port),
            "-u",
            self.config.username,
            self.config.database,
        ]
        if self.config.password:
            cmd.insert(-1, f"-p{self.config.password}")
        result = subprocess.run(
            cmd,
            input=sql,
            text=True,
            capture_output=True,
            encoding="utf-8",
        )
        if result.returncode != 0:
            raise RuntimeError(
                f"MySQL CLI execution failed.\nSTDOUT:\n{result.stdout}\nSTDERR:\n{result.stderr}\nSQL:\n{sql[:2000]}"
            )
        return result

    def execute(self, sql: str) -> None:
        self._run(f"SET NAMES utf8mb4;\n{sql}")

    def query(self, sql: str) -> list[list[str]]:
        result = self._run(f"SET NAMES utf8mb4;\n{sql}")
        lines = [line for line in result.stdout.splitlines() if line.strip()]
        return [line.split("\t") for line in lines]


class PyMySQLAdapter(BaseAdapter):
    def __init__(self, config: DatabaseConfig):
        if pymysql is None:
            raise RuntimeError("PyMySQL is not installed")
        self.connection = pymysql.connect(
            host=config.host,
            port=config.port,
            user=config.username,
            password=config.password,
            database=config.database,
            charset="utf8mb4",
            autocommit=True,
        )

    def execute(self, sql: str) -> None:
        with self.connection.cursor() as cursor:
            for statement in split_sql(sql):
                if statement.strip():
                    cursor.execute(statement)

    def query(self, sql: str) -> list[list[str]]:
        rows: list[list[str]] = []
        with self.connection.cursor() as cursor:
            for statement in split_sql(sql):
                if not statement.strip():
                    continue
                cursor.execute(statement)
                if cursor.description:
                    for row in cursor.fetchall():
                        rows.append(["" if value is None else str(value) for value in row])
        return rows

    def insert_and_get_id(self, sql: str) -> int:
        with self.connection.cursor() as cursor:
            cursor.execute(sql)
            return int(cursor.lastrowid)

    def close(self) -> None:
        self.connection.close()


def split_sql(sql: str) -> list[str]:
    return [part for part in sql.split(";") if part.strip()]


def sql_quote(value: Any) -> str:
    if value is None:
        return "NULL"
    if isinstance(value, bool):
        return "1" if value else "0"
    if isinstance(value, (int, float)):
        return str(value)
    text = str(value)
    text = (
        text.replace("\\", "\\\\")
        .replace("\0", "\\0")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
        .replace("'", "\\'")
    )
    return f"'{text}'"


def load_database_config(args: argparse.Namespace) -> DatabaseConfig:
    defaults = {
        "host": "localhost",
        "port": 3306,
        "database": "blog_db",
        "username": "root",
        "password": "123456",
    }
    if DEFAULT_CONFIG_FILE.exists():
        content = DEFAULT_CONFIG_FILE.read_text(encoding="utf-8")
        url_match = re.search(r"url:\s*jdbc:mysql://([^:/]+):(\d+)/([^? \n]+)", content)
        if url_match:
            defaults["host"] = url_match.group(1)
            defaults["port"] = int(url_match.group(2))
            defaults["database"] = url_match.group(3)
        user_match = re.search(r"username:\s*(.+)", content)
        pwd_match = re.search(r"password:\s*(.*)", content)
        if user_match:
            defaults["username"] = user_match.group(1).strip()
        if pwd_match:
            defaults["password"] = pwd_match.group(1).strip()

    return DatabaseConfig(
        host=args.host or defaults["host"],
        port=int(args.port or defaults["port"]),
        database=args.database or defaults["database"],
        username=args.username or defaults["username"],
        password=args.password if args.password is not None else defaults["password"],
        mysql_bin=args.mysql_bin or "mysql",
    )


def build_adapter(config: DatabaseConfig, force_cli: bool) -> BaseAdapter:
    if pymysql is not None and not force_cli:
        return PyMySQLAdapter(config)
    return MySQLCliAdapter(config)


def clean_markdown_emphasis(text: str) -> str:
    cleaned = text.strip()
    cleaned = cleaned.replace("**", "").replace("__", "")
    cleaned = cleaned.replace("`", "").replace("*", "")
    cleaned = re.sub(r"\s+", " ", cleaned)
    return cleaned.strip()


def strip_heading_prefix(title: str) -> tuple[int | None, str | None, str]:
    raw = clean_markdown_emphasis(title)
    match = re.match(r"^(\d+)\s*[、.．]\s*(.+)$", raw)
    if match:
        return int(match.group(1)), match.group(1), match.group(2).strip()
    return None, None, raw


def normalize_section(section: str) -> str:
    cleaned = clean_markdown_emphasis(section)
    cleaned = re.sub(r"^[一二三四五六七八九十百千万0-9]+[、.．]\s*", "", cleaned)
    return cleaned.strip() or cleaned


def summary_from_answer(answer: str, max_len: int = 180) -> str:
    plain = answer
    plain = plain.replace("```", "\n")
    plain = re.sub(r"\!\[[^\]]*]\([^)]+\)", "", plain)
    plain = re.sub(r"\[([^\]]+)\]\([^)]+\)", r"\1", plain)
    plain = re.sub(r"`([^`]+)`", r"\1", plain)
    plain = re.sub(r"\s+", " ", plain).strip()
    if len(plain) <= max_len:
        return plain
    return plain[: max_len - 3].rstrip() + "..."


def extract_keywords(category_name: str, section_name: str, title: str) -> str:
    candidates = [category_name, section_name, title]
    tokens: list[str] = []
    for candidate in candidates:
        normalized = clean_markdown_emphasis(candidate)
        parts = re.split(r"[，,、：:（）()？?/\-\s]+", normalized)
        for part in parts:
            part = part.strip()
            if len(part) < 2:
                continue
            if part not in tokens:
                tokens.append(part)
    return ",".join(tokens[:12])


def estimate_minutes(answer: str) -> int:
    length = len(answer.strip())
    if length >= 800:
        return 10
    if length >= 400:
        return 8
    if length >= 180:
        return 6
    return 5


def guess_difficulty(title: str, answer: str) -> int:
    text = f"{title} {answer}"
    hard_keywords = ["底层", "原理", "优化", "调优", "锁", "事务", "分布式", "一致性", "源码", "垃圾回收", "MVCC", "链路追踪"]
    medium_keywords = ["区别", "流程", "实现", "机制", "生命周期", "方案", "模式", "如何"]
    if any(keyword in text for keyword in hard_keywords):
        return 4 if len(answer) > 900 else 3
    if any(keyword in text for keyword in medium_keywords):
        return 2 if len(answer) < 500 else 3
    return 2


def generate_question_code(category_code: str, question_no: int | None, title: str) -> str:
    if question_no is not None:
        return f"{category_code}-{question_no:03d}"
    digest = hashlib.md5(title.encode("utf-8")).hexdigest()[:10]
    return f"{category_code}-{digest}"


def parse_markdown_file(file_path: Path) -> list[ParsedQuestion]:
    if file_path.name not in FILE_CATEGORY_MAPPING:
        derived_name = file_path.stem.split("-")[-1]
        category_code = re.sub(r"[^a-z0-9]+", "-", derived_name.lower()).strip("-") or "study-extra"
        category_name = derived_name
    else:
        category_code, category_name = FILE_CATEGORY_MAPPING[file_path.name]

    content = file_path.read_text(encoding="utf-8")
    lines = content.splitlines()
    questions: list[ParsedQuestion] = []
    current_section = category_name
    current_heading: str | None = None
    buffer: list[str] = []

    def flush_question() -> None:
        nonlocal current_heading, buffer
        if not current_heading:
            buffer = []
            return
        answer = "\n".join(buffer).strip()
        if not answer:
            buffer = []
            current_heading = None
            return
        question_no, source_ref_no, title = strip_heading_prefix(current_heading)
        source_section = current_section or category_name
        questions.append(
            ParsedQuestion(
                file_name=file_path.name,
                category_code=category_code,
                category_name=category_name,
                source_section=source_section,
                question_no=question_no,
                source_ref_no=source_ref_no,
                title=title,
                standard_answer=answer,
                answer_summary=summary_from_answer(answer),
                keywords=extract_keywords(category_name, source_section, title),
                difficulty=guess_difficulty(title, answer),
                answer_word_count=len(re.sub(r"\s+", "", answer)),
                estimated_minutes=estimate_minutes(answer),
                question_code=generate_question_code(category_code, question_no, title),
                raw_payload="\n".join([current_heading, answer]).strip(),
            )
        )
        buffer = []
        current_heading = None

    for line in lines:
        if line.startswith("## "):
            flush_question()
            current_section = normalize_section(line[3:])
            continue
        if line.startswith("### "):
            flush_question()
            current_heading = line[4:].strip()
            continue
        if current_heading is not None:
            buffer.append(line)

    flush_question()
    return questions


def parse_source_dir(source_dir: Path, limit: int | None = None, only_file: str | None = None) -> list[ParsedQuestion]:
    files = sorted(source_dir.glob("*.md"))
    if only_file:
        files = [file for file in files if file.name == only_file]
    all_questions: list[ParsedQuestion] = []
    for file_path in files:
        all_questions.extend(parse_markdown_file(file_path))
    if limit is not None:
        return all_questions[:limit]
    return all_questions


def ensure_tables_exist(adapter: BaseAdapter) -> None:
    rows = adapter.query(
        """
        SELECT COUNT(*)
        FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name IN (
            'study_category',
            'study_import_batch',
            'study_import_batch_detail',
            'study_question',
            'study_question_version'
          );
        """
    )
    count = int(rows[0][0]) if rows else 0
    if count < 5:
        raise RuntimeError(
            "学习模块企业版数据表不存在，请先执行建表 SQL，或在脚本中增加 --bootstrap 参数。"
        )


def bootstrap_tables(adapter: BaseAdapter, bootstrap_sql_path: Path) -> None:
    if not bootstrap_sql_path.exists():
        raise FileNotFoundError(f"找不到建表 SQL 文件: {bootstrap_sql_path}")
    adapter.execute(bootstrap_sql_path.read_text(encoding="utf-8"))


def get_or_create_root_category(adapter: BaseAdapter, operator_id: int) -> int:
    rows = adapter.query(
        f"SELECT id FROM study_category WHERE category_code = {sql_quote(ROOT_CATEGORY_CODE)} AND is_deleted = 0 LIMIT 1;"
    )
    if rows:
        return int(rows[0][0])
    insert_sql = f"""
    INSERT INTO study_category (
        parent_id, category_name, category_code, category_level, category_path,
        source_type, description, question_count, sort_order, status, is_deleted,
        create_by, update_by, create_time, update_time
    ) VALUES (
        0, {sql_quote(ROOT_CATEGORY_NAME)}, {sql_quote(ROOT_CATEGORY_CODE)}, 1, {sql_quote('/1')},
        1, {sql_quote('学习模块默认根分类')}, 0, 1000, 1, 0,
        {operator_id}, {operator_id}, NOW(), NOW()
    )
    """
    return adapter.insert_and_get_id(insert_sql)


def get_or_create_category(
    adapter: BaseAdapter,
    root_id: int,
    category_code: str,
    category_name: str,
    source_file_name: str,
    operator_id: int,
) -> int:
    rows = adapter.query(
        f"""
        SELECT id
        FROM study_category
        WHERE category_code = {sql_quote(category_code)}
          AND is_deleted = 0
        LIMIT 1;
        """
    )
    if rows:
        return int(rows[0][0])

    path = f"/{root_id}"
    insert_sql = f"""
    INSERT INTO study_category (
        parent_id, category_name, category_code, category_level, category_path,
        source_type, source_file_name, description, question_count, sort_order, status,
        is_deleted, create_by, update_by, create_time, update_time
    ) VALUES (
        {root_id}, {sql_quote(category_name)}, {sql_quote(category_code)}, 2, {sql_quote(path)},
        2, {sql_quote(source_file_name)}, {sql_quote(f'对应题库文件：{source_file_name}')}, 0, 100, 1,
        0, {operator_id}, {operator_id}, NOW(), NOW()
    )
    """
    category_id = adapter.insert_and_get_id(insert_sql)
    adapter.execute(
        f"UPDATE study_category SET category_path = {sql_quote(f'/{root_id}/{category_id}')}, update_time = NOW() WHERE id = {category_id};"
    )
    return category_id


def create_import_batch(adapter: BaseAdapter, args: argparse.Namespace, operator_id: int) -> tuple[int, str]:
    batch_seed = f"{args.source_dir}|{operator_id}|{os.getpid()}"
    batch_no = "STUDY-IMP-" + hashlib.md5(batch_seed.encode("utf-8")).hexdigest()[:12].upper()
    insert_sql = f"""
    INSERT INTO study_import_batch (
        batch_no, source_type, source_name, source_path, total_count, success_count,
        fail_count, duplicate_count, batch_status, start_time, operator_id, remark,
        create_time, update_time
    ) VALUES (
        {sql_quote(batch_no)}, 1, {sql_quote('Markdown题库导入')}, {sql_quote(str(args.source_dir))},
        0, 0, 0, 0, 0, NOW(), {operator_id}, {sql_quote('自动导入面试题大全md')},
        NOW(), NOW()
    )
    """
    batch_id = adapter.insert_and_get_id(insert_sql)
    return batch_id, batch_no


def find_existing_question(adapter: BaseAdapter, category_id: int, question: ParsedQuestion) -> dict[str, Any] | None:
    if question.question_no is not None:
        sql = f"""
        SELECT id, version_no, title, COALESCE(question_stem, ''), standard_answer,
               COALESCE(answer_summary, ''), difficulty
        FROM study_question
        WHERE category_id = {category_id}
          AND question_no = {question.question_no}
          AND is_deleted = 0
        LIMIT 1;
        """
    else:
        sql = f"""
        SELECT id, version_no, title, COALESCE(question_stem, ''), standard_answer,
               COALESCE(answer_summary, ''), difficulty
        FROM study_question
        WHERE category_id = {category_id}
          AND title = {sql_quote(question.title)}
          AND is_deleted = 0
        LIMIT 1;
        """
    rows = adapter.query(sql)
    if not rows:
        return None
    row = rows[0]
    return {
        "id": int(row[0]),
        "version_no": int(row[1]),
        "title": row[2],
        "question_stem": row[3],
        "standard_answer": row[4],
        "answer_summary": row[5],
        "difficulty": int(row[6]),
    }


def insert_question(
    adapter: BaseAdapter,
    batch_id: int,
    category_id: int,
    question: ParsedQuestion,
    operator_id: int,
) -> int:
    insert_sql = f"""
    INSERT INTO study_question (
        category_id, question_no, question_code, question_type, title, question_stem,
        standard_answer, answer_summary, keywords, difficulty, source_type, source_file_name,
        source_section, source_ref_no, import_batch_id, estimated_minutes, answer_word_count,
        score_full_mark, score_pass_mark, ai_score_enabled, self_assessment_enabled,
        ai_score_prompt_version, score_rubric_json, review_status, publish_time, version_no,
        view_count, study_count, check_count, status, is_deleted, create_by, update_by,
        create_time, update_time
    ) VALUES (
        {category_id}, {sql_quote(question.question_no)}, {sql_quote(question.question_code)}, 1, {sql_quote(question.title)}, NULL,
        {sql_quote(question.standard_answer)}, {sql_quote(question.answer_summary)}, {sql_quote(question.keywords)}, {question.difficulty}, 2, {sql_quote(question.file_name)},
        {sql_quote(question.source_section)}, {sql_quote(question.source_ref_no)}, {batch_id}, {question.estimated_minutes}, {question.answer_word_count},
        100.00, 60.00, 1, 1,
        'v1', {sql_quote(json.dumps(DEFAULT_RUBRIC, ensure_ascii=False))}, 2, NOW(), 1,
        0, 0, 0, 1, 0, {operator_id}, {operator_id},
        NOW(), NOW()
    )
    """
    question_id = adapter.insert_and_get_id(insert_sql)
    insert_question_version(adapter, question_id, 1, question, operator_id, change_type=1, change_reason="Markdown题库首次导入")
    return question_id


def update_question(
    adapter: BaseAdapter,
    question_id: int,
    next_version_no: int,
    batch_id: int,
    category_id: int,
    question: ParsedQuestion,
    operator_id: int,
) -> None:
    update_sql = f"""
    UPDATE study_question
    SET category_id = {category_id},
        question_no = {sql_quote(question.question_no)},
        question_code = {sql_quote(question.question_code)},
        title = {sql_quote(question.title)},
        standard_answer = {sql_quote(question.standard_answer)},
        answer_summary = {sql_quote(question.answer_summary)},
        keywords = {sql_quote(question.keywords)},
        difficulty = {question.difficulty},
        source_type = 2,
        source_file_name = {sql_quote(question.file_name)},
        source_section = {sql_quote(question.source_section)},
        source_ref_no = {sql_quote(question.source_ref_no)},
        import_batch_id = {batch_id},
        estimated_minutes = {question.estimated_minutes},
        answer_word_count = {question.answer_word_count},
        ai_score_prompt_version = 'v1',
        score_rubric_json = {sql_quote(json.dumps(DEFAULT_RUBRIC, ensure_ascii=False))},
        review_status = 2,
        publish_time = IFNULL(publish_time, NOW()),
        version_no = {next_version_no},
        update_by = {operator_id},
        update_time = NOW()
    WHERE id = {question_id};
    """
    adapter.execute(update_sql)
    insert_question_version(adapter, question_id, next_version_no, question, operator_id, change_type=3, change_reason="Markdown题库导入更新")


def insert_question_version(
    adapter: BaseAdapter,
    question_id: int,
    version_no: int,
    question: ParsedQuestion,
    operator_id: int,
    change_type: int,
    change_reason: str,
) -> None:
    version_sql = f"""
    INSERT INTO study_question_version (
        question_id, version_no, title, question_stem, standard_answer, answer_summary,
        keywords, difficulty, score_full_mark, score_pass_mark, score_rubric_json,
        change_type, change_reason, operator_id, create_time
    ) VALUES (
        {question_id}, {version_no}, {sql_quote(question.title)}, NULL, {sql_quote(question.standard_answer)}, {sql_quote(question.answer_summary)},
        {sql_quote(question.keywords)}, {question.difficulty}, 100.00, 60.00, {sql_quote(json.dumps(DEFAULT_RUBRIC, ensure_ascii=False))},
        {change_type}, {sql_quote(change_reason)}, {operator_id}, NOW()
    );
    """
    adapter.execute(version_sql)


def insert_batch_detail(
    adapter: BaseAdapter,
    batch_id: int,
    question: ParsedQuestion,
    category_name: str,
    question_id: int | None,
    process_status: int,
    error_message: str | None,
) -> None:
    detail_sql = f"""
    INSERT INTO study_import_batch_detail (
        batch_id, source_line_no, source_section, source_question_no, source_title,
        category_name, question_id, process_status, error_message, raw_payload, create_time
    ) VALUES (
        {batch_id}, NULL, {sql_quote(question.source_section)}, {sql_quote(question.source_ref_no)},
        {sql_quote(question.title)}, {sql_quote(category_name)}, {sql_quote(question_id)},
        {process_status}, {sql_quote(error_message)}, {sql_quote(question.raw_payload)}, NOW()
    );
    """
    adapter.execute(detail_sql)


def update_category_question_counts(adapter: BaseAdapter) -> None:
    adapter.execute(
        """
        UPDATE study_category c
        LEFT JOIN (
            SELECT category_id, COUNT(*) AS cnt
            FROM study_question
            WHERE is_deleted = 0
            GROUP BY category_id
        ) q ON c.id = q.category_id
        SET c.question_count = IFNULL(q.cnt, 0),
            c.update_time = NOW();
        """
    )


def finalize_batch(
    adapter: BaseAdapter,
    batch_id: int,
    total_count: int,
    success_count: int,
    fail_count: int,
    duplicate_count: int,
) -> None:
    if fail_count > 0 and success_count > 0:
        status = 2
    elif fail_count > 0:
        status = 3
    else:
        status = 1
    adapter.execute(
        f"""
        UPDATE study_import_batch
        SET total_count = {total_count},
            success_count = {success_count},
            fail_count = {fail_count},
            duplicate_count = {duplicate_count},
            batch_status = {status},
            end_time = NOW(),
            update_time = NOW()
        WHERE id = {batch_id};
        """
    )


def import_questions(args: argparse.Namespace) -> None:
    source_dir = Path(args.source_dir).resolve()
    if not source_dir.exists():
        raise FileNotFoundError(f"题库目录不存在: {source_dir}")

    questions = parse_source_dir(source_dir, limit=args.limit, only_file=args.only_file)
    if not questions:
        print("未解析到任何题目，请检查题库目录或文件格式。")
        return

    print(f"共解析到 {len(questions)} 道题。")
    if args.dry_run:
        preview = questions[:5]
        for item in preview:
            print(f"[预览] {item.file_name} | {item.source_section} | {item.question_no} | {item.title}")
        print("dry-run 模式下未执行数据库写入。")
        return

    config = load_database_config(args)
    adapter = build_adapter(config, args.force_cli)

    try:
        if args.bootstrap:
            bootstrap_tables(adapter, Path(args.bootstrap_sql))

        ensure_tables_exist(adapter)
        root_id = get_or_create_root_category(adapter, args.operator_id)
        batch_id, batch_no = create_import_batch(adapter, args, args.operator_id)
        print(f"导入批次已创建: {batch_no} (ID={batch_id})")

        success_count = 0
        fail_count = 0
        duplicate_count = 0

        category_cache: dict[str, tuple[int, str]] = {}
        for index, question in enumerate(questions, start=1):
            try:
                if question.category_code not in category_cache:
                    category_id = get_or_create_category(
                        adapter,
                        root_id,
                        question.category_code,
                        question.category_name,
                        question.file_name,
                        args.operator_id,
                    )
                    category_cache[question.category_code] = (category_id, question.category_name)

                category_id, category_name = category_cache[question.category_code]
                existing = find_existing_question(adapter, category_id, question)

                if existing is None:
                    question_id = insert_question(adapter, batch_id, category_id, question, args.operator_id)
                    success_count += 1
                    insert_batch_detail(adapter, batch_id, question, category_name, question_id, 1, None)
                    print(f"[{index}/{len(questions)}] 新增题目: {question.title}")
                    continue

                changed = (
                    existing["title"].strip() != question.title.strip()
                    or existing["standard_answer"].strip() != question.standard_answer.strip()
                    or existing["answer_summary"].strip() != question.answer_summary.strip()
                    or int(existing["difficulty"]) != question.difficulty
                )

                if changed:
                    next_version_no = int(existing["version_no"]) + 1
                    update_question(
                        adapter,
                        int(existing["id"]),
                        next_version_no,
                        batch_id,
                        category_id,
                        question,
                        args.operator_id,
                    )
                    success_count += 1
                    insert_batch_detail(adapter, batch_id, question, category_name, int(existing["id"]), 1, None)
                    print(f"[{index}/{len(questions)}] 更新题目: {question.title} -> v{next_version_no}")
                else:
                    duplicate_count += 1
                    insert_batch_detail(adapter, batch_id, question, category_name, int(existing["id"]), 3, "内容未变化，跳过更新")
                    print(f"[{index}/{len(questions)}] 跳过重复: {question.title}")
            except Exception as exc:
                fail_count += 1
                category_name = category_cache.get(question.category_code, (0, question.category_name))[1]
                insert_batch_detail(adapter, batch_id, question, category_name, None, 2, str(exc))
                print(f"[{index}/{len(questions)}] 导入失败: {question.title} | {exc}", file=sys.stderr)

        update_category_question_counts(adapter)
        finalize_batch(adapter, batch_id, len(questions), success_count, fail_count, duplicate_count)
        print(
            textwrap.dedent(
                f"""
                导入完成
                - 批次号: {batch_no}
                - 总题数: {len(questions)}
                - 成功: {success_count}
                - 重复跳过: {duplicate_count}
                - 失败: {fail_count}
                """
            ).strip()
        )
    finally:
        adapter.close()


def build_arg_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="自动导入 面试题大全md 到学习模块企业版数据库")
    parser.add_argument("--source-dir", default=str(DEFAULT_SOURCE_DIR), help="Markdown 题库目录")
    parser.add_argument("--bootstrap", action="store_true", help="导入前自动执行企业版建表 SQL")
    parser.add_argument("--bootstrap-sql", default=str(DEFAULT_BOOTSTRAP_SQL), help="企业版建表 SQL 路径")
    parser.add_argument("--dry-run", action="store_true", help="只解析不入库")
    parser.add_argument("--limit", type=int, default=None, help="仅处理前 N 道题，便于测试")
    parser.add_argument("--only-file", default=None, help="只导入指定文件名，例如 Java八股文200道-Java基础篇.md")
    parser.add_argument("--operator-id", type=int, default=1, help="导入操作人ID，默认1")
    parser.add_argument("--force-cli", action="store_true", help="强制使用 mysql 客户端，不使用 PyMySQL")
    parser.add_argument("--mysql-bin", default="mysql", help="mysql 客户端命令路径")
    parser.add_argument("--host", default=None, help="数据库主机")
    parser.add_argument("--port", default=None, help="数据库端口")
    parser.add_argument("--database", default=None, help="数据库名")
    parser.add_argument("--username", default=None, help="数据库用户名")
    parser.add_argument("--password", default=None, help="数据库密码")
    return parser


def main() -> None:
    parser = build_arg_parser()
    args = parser.parse_args()
    try:
        import_questions(args)
    except Exception as exc:
        print(f"执行失败: {exc}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
