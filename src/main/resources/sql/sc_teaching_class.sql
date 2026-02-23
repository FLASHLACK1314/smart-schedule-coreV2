-- 教学班表
-- Author: Smart Schedule System
-- Version: 1.1 (2026-02-23)
-- Changes: 添加 weekly_sessions 和 sections_per_session 字段用于智能排课
create table public.sc_teaching_class
(
    teaching_class_uuid   varchar(32) not null
        constraint sc_teaching_class_pk
            primary key,
    course_uuid           varchar(32) not null
        constraint sc_teaching_course_fk references public.sc_course,
    teacher_uuid          varchar(32) not null
        constraint sc_teaching_teacher_fk references public.sc_teacher,
    semester_uuid         varchar(32) not null
        constraint sc_teaching_semester_fk references public.sc_semester,
    teaching_class_name   varchar(64),
    teaching_class_hours  integer not null default 0,
    weekly_sessions       integer default 1,
    sections_per_session  integer default 2,
    constraint chk_weekly_sessions_positive check (weekly_sessions > 0),
    constraint chk_sections_per_session_positive check (sections_per_session > 0)
);

-- 添加注释
comment on table public.sc_teaching_class is '教学班表 (排课的业务主体)';
comment on column public.sc_teaching_class.teaching_class_uuid is '教学班UUID';
comment on column public.sc_teaching_class.course_uuid is '课程UUID';
comment on column public.sc_teaching_class.teacher_uuid is '教师UUID';
comment on column public.sc_teaching_class.semester_uuid is '学期UUID';
comment on column public.sc_teaching_class.teaching_class_name is '教学班名称';
comment on column public.sc_teaching_class.teaching_class_hours is '教学班学时 (排课记录累计)';
comment on column public.sc_teaching_class.weekly_sessions is '每周上课次数 (默认1次)';
comment on column public.sc_teaching_class.sections_per_session is '每次上课节次数 (默认2节)';

-- ============================================================================
-- 迁移脚本 (用于已存在的数据库)
-- 如果表已存在但缺少新字段，请执行以下ALTER语句：
-- ============================================================================
--
-- -- 添加 weekly_sessions 字段
-- ALTER TABLE public.sc_teaching_class ADD COLUMN IF NOT EXISTS weekly_sessions INTEGER DEFAULT 1;
--
-- -- 添加 sections_per_session 字段
-- ALTER TABLE public.sc_teaching_class ADD COLUMN IF NOT EXISTS sections_per_session INTEGER DEFAULT 2;
--
-- -- 添加约束 (如果不存在)
-- ALTER TABLE public.sc_teaching_class ADD CONSTRAINT IF NOT EXISTS chk_weekly_sessions_positive
--     CHECK (weekly_sessions > 0);
--
-- ALTER TABLE public.sc_teaching_class ADD CONSTRAINT IF NOT EXISTS chk_sections_per_session_positive
--     CHECK (sections_per_session > 0);
--
-- -- 添加注释 (如果不存在)
-- COMMENT ON COLUMN public.sc_teaching_class.weekly_sessions IS '每周上课次数 (默认1次)';
-- COMMENT ON COLUMN public.sc_teaching_class.sections_per_session IS '每次上课节次数 (默认2节)';
--