create table public.sc_teacher
(
    teacher_uuid       varchar(32)          not null
        constraint sc_teacher_pk
            primary key,
    teacher_num        varchar(32)          not null
        constraint sc_teacher_num_unique
            unique,
    teacher_name       varchar(32)          not null,
    title              varchar(32)          not null,
    teacher_password   varchar              not null,
    max_hours_per_week integer              not null,
    like_time          varchar              not null,
    is_active          boolean default true not null
);

comment on table public.sc_teacher is '教师表';

comment on column public.sc_teacher.teacher_uuid is '教师UUID';

comment on column public.sc_teacher.teacher_num is '教师编号（唯一工号）';

comment on column public.sc_teacher.teacher_name is '教师名称';

comment on column public.sc_teacher.title is '职称';

comment on column public.sc_teacher.teacher_password is '密码';

comment on column public.sc_teacher.max_hours_per_week is '每周最高授课时长';

comment on column public.sc_teacher.like_time is '喜欢时间 (字符串格式)';

comment on column public.sc_teacher.is_active is '是否启用（不启用不参与排课）';

alter table public.sc_teacher
    owner to "smart-schedule-core";

