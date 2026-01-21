create table public.sc_semester
(
    semester_uuid varchar(32) not null
        constraint sc_semester_pk
            primary key,
    semester_name varchar(32) not null
);

comment on table public.sc_semester is '学期表';

comment on column public.sc_semester.semester_uuid is '学期UUID';

comment on column public.sc_semester.semester_name is '学期名称';

alter table public.sc_semester
    owner to "smart-schedule-core";

