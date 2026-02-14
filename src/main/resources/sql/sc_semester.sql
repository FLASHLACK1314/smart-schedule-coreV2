create table public.sc_semester
(
    semester_uuid varchar(32) not null
        constraint sc_semester_pk
            primary key,
    semester_name varchar(32) not null,
    semester_weeks int         not null default 18
);

comment on table public.sc_semester is '学期表';

comment on column public.sc_semester.semester_uuid is '学期UUID';

comment on column public.sc_semester.semester_name is '学期名称';

comment on column public.sc_semester.semester_weeks is '学期周数';

alter table public.sc_semester
    owner to "smart-schedule-core";

