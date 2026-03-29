create table public.sc_score
(
    score_uuid           varchar(32)   not null
        constraint sc_score_pk
            primary key,
    student_uuid         varchar(32)   not null
        constraint sc_score_sc_student_student_uuid_fk
            references public.sc_student(student_uuid)
            on delete cascade,
    teaching_class_uuid  varchar(32)   not null
        constraint sc_score_sc_teaching_class_teaching_class_uuid_fk
            references public.sc_teaching_class(teaching_class_uuid)
            on delete cascade,
    semester_uuid        varchar(32)   not null
        constraint sc_score_sc_semester_semester_uuid_fk
            references public.sc_semester(semester_uuid)
            on delete cascade,
    usual_score          decimal(5, 2),
    midterm_score        decimal(5, 2),
    final_score          decimal(5, 2),
    total_score          decimal(5, 2),
    grade_point          decimal(3, 1),
    remark               text,
    create_time          timestamp     default current_timestamp,
    update_time          timestamp     default current_timestamp,
    constraint uk_score_student_teaching_class
        unique (student_uuid, teaching_class_uuid)
);

-- 注释部分
comment on table public.sc_score is '成绩表';
comment on column public.sc_score.score_uuid is '成绩UUID（主键）';
comment on column public.sc_score.student_uuid is '学生UUID';
comment on column public.sc_score.teaching_class_uuid is '教学班UUID';
comment on column public.sc_score.semester_uuid is '学期UUID';
comment on column public.sc_score.usual_score is '平时成绩（0-100）';
comment on column public.sc_score.midterm_score is '期中成绩（0-100）';
comment on column public.sc_score.final_score is '期末成绩（0-100）';
comment on column public.sc_score.total_score is '总评成绩（0-100）';
comment on column public.sc_score.grade_point is '绩点（0-5.0）';
comment on column public.sc_score.remark is '备注';
comment on column public.sc_score.create_time is '创建时间';
comment on column public.sc_score.update_time is '更新时间';

-- 索引
create index idx_score_student on public.sc_score(student_uuid);
create index idx_score_teaching_class on public.sc_score(teaching_class_uuid);
create index idx_score_semester on public.sc_score(semester_uuid);
