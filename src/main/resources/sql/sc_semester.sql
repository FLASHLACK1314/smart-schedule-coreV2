create table public.sc_semester
(
    semester_uuid varchar(32) not null
        constraint sc_semester_pk
            primary key,
    semester_name varchar(32) not null,
    semester_weeks int         not null default 18,
    start_date    date         not null,
    end_date      date         not null,
    constraint chk_date_range check (start_date <= end_date)
);

comment on table public.sc_semester is '学期表';

comment on column public.sc_semester.semester_uuid is '学期UUID';

comment on column public.sc_semester.semester_name is '学期名称';

comment on column public.sc_semester.semester_weeks is '学期周数';

comment on column public.sc_semester.start_date is '学期开始日期';

comment on column public.sc_semester.end_date is '学期结束日期';

alter table public.sc_semester
    owner to "smart-schedule-core";

