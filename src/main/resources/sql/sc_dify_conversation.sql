create table public.sc_dify_conversation
(
    conversation_uuid    varchar(32)  not null
        constraint sc_dify_conversation_pk
            primary key,
    user_uuid            varchar(32)  not null,
    user_type            varchar(32)  not null,
    dify_conversation_id varchar(128) not null,
    created_at           timestamp    not null default now(),
    updated_at           timestamp    not null default now(),
    constraint sc_dify_conversation_user_dify_unique
        unique (user_uuid, user_type, dify_conversation_id)
);

comment on table public.sc_dify_conversation is 'Dify会话关联表';
comment on column public.sc_dify_conversation.conversation_uuid is '会话记录UUID(主键)';
comment on column public.sc_dify_conversation.user_uuid is '用户UUID';
comment on column public.sc_dify_conversation.user_type is '用户类型(STUDENT/TEACHER/ACADEMIC_ADMIN/SYSTEM_ADMIN)';
comment on column public.sc_dify_conversation.dify_conversation_id is 'Dify会话ID';
comment on column public.sc_dify_conversation.created_at is '创建时间';
comment on column public.sc_dify_conversation.updated_at is '最后对话时间(用于获取最新会话)';
