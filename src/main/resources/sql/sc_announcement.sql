create table public.sc_announcement
(
    announcement_uuid varchar(32)   not null
        constraint sc_announcement_pk
            primary key,
    title             varchar(200)  not null,
    content           text          not null,
    priority          varchar(10)   not null default 'NORMAL',
    user_type         varchar(20)   not null,
    created_at        timestamp     not null default now()
);

comment on table public.sc_announcement is '系统公告表';
comment on column public.sc_announcement.announcement_uuid is '公告UUID(主键)';
comment on column public.sc_announcement.title is '公告标题';
comment on column public.sc_announcement.content is '公告内容';
comment on column public.sc_announcement.priority is '优先级(HIGH/MEDIUM/LOW)';
comment on column public.sc_announcement.user_type is '目标用户类型(STUDENT/TEACHER/ACADEMIC_ADMIN/SYSTEM_ADMIN)';
comment on column public.sc_announcement.created_at is '创建时间';

create index sc_announcement_user_type_index on public.sc_announcement (user_type);
create index sc_announcement_created_at_index on public.sc_announcement (created_at desc);
