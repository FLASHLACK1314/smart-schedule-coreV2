create table public.sc_stats_snapshot
(
    snapshot_uuid          varchar(32)  not null
        constraint sc_stats_snapshot_pk
            primary key,
    week_start_date        date         not null,
    weekly_schedule_count  integer      not null default 0,
    active_teacher_count   integer      not null default 0,
    total_student_count    integer      not null default 0,
    classroom_usage_rate   decimal(5,2) not null default 0,
    created_at             timestamp    not null default now(),
    constraint sc_stats_snapshot_week_unique
        unique (week_start_date)
);

comment on table public.sc_stats_snapshot is '统计快照表';
comment on column public.sc_stats_snapshot.snapshot_uuid is '快照UUID(主键)';
comment on column public.sc_stats_snapshot.week_start_date is '周开始日期(周一)';
comment on column public.sc_stats_snapshot.weekly_schedule_count is '本周排课数量';
comment on column public.sc_stats_snapshot.active_teacher_count is '活跃教师数量';
comment on column public.sc_stats_snapshot.total_student_count is '学生总数';
comment on column public.sc_stats_snapshot.classroom_usage_rate is '教室使用率(百分比)';
comment on column public.sc_stats_snapshot.created_at is '创建时间';

create index sc_stats_snapshot_week_start_date_index on public.sc_stats_snapshot (week_start_date);
