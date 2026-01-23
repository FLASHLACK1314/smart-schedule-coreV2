-- 创建课程主表
create table public.sc_course
(
    course_uuid             varchar(32) not null
        constraint sc_course_pk
            primary key,
    course_num              varchar(32) not null
        constraint sc_course_num_unique
            unique,
    course_name             varchar(64) not null,
    course_type_uuid        varchar(32) not null
        constraint sc_course_course_type_fk
            references public.sc_course_type,
    course_credit           numeric     not null
);

-- 添加注释
comment on table public.sc_course is '课程主表';
comment on column public.sc_course.course_uuid is '课程UUID';
comment on column public.sc_course.course_num is '课程编号 (唯一编码)';
comment on column public.sc_course.course_name is '课程名称';
comment on column public.sc_course.course_type_uuid is '课程类型UUID';
comment on column public.sc_course.course_credit is '课程学分 (支持半分)';

-- 权限归属
alter table public.sc_course owner to "smart-schedule-core";