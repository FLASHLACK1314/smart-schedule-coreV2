create table public.sc_activity_log
(
    activity_uuid varchar(32)   not null
        constraint sc_activity_log_pk
            primary key,
    user_uuid     varchar(32)   not null,
    user_name     varchar(50)   not null,
    action_type   varchar(30)   not null,
    action_text   varchar(200)  not null,
    created_at    timestamp     not null default now()
);

comment on table public.sc_activity_log is '活动记录表';
comment on column public.sc_activity_log.activity_uuid is '活动记录UUID(主键)';
comment on column public.sc_activity_log.user_uuid is '操作用户UUID';
comment on column public.sc_activity_log.user_name is '操作用户名称';
comment on column public.sc_activity_log.action_type is '操作类型(AUTO_SCHEDULE/MANUAL_SCHEDULE等)';
comment on column public.sc_activity_log.action_text is '操作描述文本';
comment on column public.sc_activity_log.created_at is '创建时间';

create index sc_activity_log_user_uuid_index on public.sc_activity_log (user_uuid);
create index sc_activity_log_created_at_index on public.sc_activity_log (created_at desc);
