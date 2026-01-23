-- 课程教师资格关联表
create table public.sc_course_qualification
(
    course_qualification_uuid varchar(32) not null
        constraint sc_course_qualification_pk
            primary key,
    course_uuid varchar(32) not null
        constraint sc_course_qualification_course_fk
            references public.sc_course,
    teacher_uuid varchar(32) not null
        constraint sc_course_qualification_teacher_fk
            references public.sc_teacher,
    constraint sc_course_qualification_course_teacher_unique
        unique (course_uuid, teacher_uuid)
);

-- 添加注释
comment on table public.sc_course_qualification is '课程教师资格关联表';
comment on column public.sc_course_qualification.course_qualification_uuid is '关联关系UUID';
comment on column public.sc_course_qualification.course_uuid is '课程UUID';
comment on column public.sc_course_qualification.teacher_uuid is '教师UUID';

-- 权限归属
alter table public.sc_course_qualification
    owner to "smart-schedule-core";
