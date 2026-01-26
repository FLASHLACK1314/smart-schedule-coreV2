-- 排课冲突记录表
create table public.sc_schedule_conflict
(
    conflict_uuid   varchar(32) not null
        constraint sc_schedule_conflict_pk
            primary key,
    semester_uuid   varchar(32) not null
        constraint sc_conf_semester_fk
            references public.sc_semester,
    schedule_uuid_a varchar(32) not null
        constraint sc_conf_sch_a_fk
            references public.sc_schedule,
    schedule_uuid_b varchar(32)
        constraint sc_conf_sch_b_fk
            references public.sc_schedule,
    conflict_type   varchar(32) not null,
    severity        integer     not null,
    description     text        not null
);

-- 添加注释
comment on table public.sc_schedule_conflict is '排课冲突记录表';
comment on column public.sc_schedule_conflict.conflict_uuid is '冲突记录UUID';
comment on column public.sc_schedule_conflict.semester_uuid is '学期UUID';
comment on column public.sc_schedule_conflict.schedule_uuid_a is '排课记录UUID A';
comment on column public.sc_schedule_conflict.schedule_uuid_b is '排课记录UUID B (可为空，单条记录冲突)';
comment on column public.sc_schedule_conflict.conflict_type is '冲突类型';
comment on column public.sc_schedule_conflict.severity is '严重程度：1-硬冲突, 0-软冲突';
comment on column public.sc_schedule_conflict.description is '冲突描述';

-- 权限归属
alter table public.sc_schedule_conflict
    owner to "smart-schedule-core";