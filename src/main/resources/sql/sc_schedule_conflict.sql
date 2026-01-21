create table public.sc_schedule_conflict
(
    conflict_uuid   varchar(32) not null
        constraint sc_schedule_conflict_pk
            primary key,
    semester_uuid   varchar(32) not null,

    schedule_uuid_a varchar(32) not null
        constraint sc_conf_sch_a_fk references public.sc_schedule,
    schedule_uuid_b varchar(32)
        constraint sc_conf_sch_b_fk references public.sc_schedule,

    conflict_type   varchar(32) not null,
    severity        integer     not null, -- 1:硬冲突, 0:软冲突
    description     text        not null
);

comment on table public.sc_schedule_conflict is '排课冲突记录表';
