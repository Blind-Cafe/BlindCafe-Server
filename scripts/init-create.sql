create table custom_reason
(
    custom_reason_id bigint auto_increment primary key,
    created_at datetime not null,
    updated_at datetime not null,
    age int not null,
    matching_id bigint null,
    my_gender varchar(10) null,
    partner_gender varchar(10) null,
    platform varchar(255) null,
    reason varchar(255) null,
    type varchar(30) null,
    user_id bigint null
);

create table drink
(
    drink_id bigint not null
        primary key,
    name varchar(20) not null
);

create table hibernate_sequence
(
    next_val bigint null
);

create table interest
(
    interest_id bigint not null
        primary key,
    name varchar(10) not null
);

create table matching_history
(
    matching_history_id bigint auto_increment
        primary key,
    partners varchar(255) null
);

create table matching_topic
(
    matching_topic_id bigint auto_increment
        primary key,
    entire varchar(255) null,
    remain varchar(255) null
);

create table matching
(
    matching_id bigint auto_increment
        primary key,
    created_at datetime not null,
    updated_at datetime not null,
    begin_time datetime null,
    expired_time datetime null,
    is_active bit not null,
    is_continuous tinyint(1) default 0 not null,
    is_exchange_profile tinyint(1) default 0 not null,
    is_open_profile tinyint(1) default 0 not null,
    status varchar(20) default 'MATCHING' not null,
    interest_id bigint null,
    matching_push_id bigint null,
    matching_topic_id bigint null,
    constraint FK5gxcuypd0waf54i853dfnkjwx
        foreign key (matching_topic_id) references matching_topic (matching_topic_id),
    constraint FK6fyxljib734j2m5rek59xp47y
        foreign key (interest_id) references interest (interest_id)
);

create table matching_push
(
    matching_push_id bigint auto_increment
        primary key,
    create_matching bit not null,
    end_of_one_hour bit not null,
    last_chat bit not null,
    matching_continue bit not null,
    one_day bit not null,
    three_days bit not null,
    two_days bit not null,
    matching_id bigint null,
    constraint FKjiqkpcxxlmbmaruccuf861iia
        foreign key (matching_id) references matching (matching_id)
);

alter table matching
    add constraint FK4x12bub76jivgxv5dcn1g358j
        foreign key (matching_push_id) references matching_push (matching_push_id);

create table notification_setting
(
    notification_setting_id bigint auto_increment
        primary key,
    is_all bit not null,
    off varchar(255) null
);

create table reason
(
    reason_id bigint auto_increment
        primary key,
    num bigint null,
    reason_type varchar(255) null,
    text varchar(255) null
);

create table retired_user
(
    retired_user_id bigint not null
        primary key,
    created_at datetime not null,
    updated_at datetime not null,
    address varchar(255) null,
    age int not null,
    mbti varchar(255) null,
    my_gender varchar(10) null,
    nickname varchar(10) null,
    partner_gender varchar(10) null,
    phone varchar(255) null,
    platform varchar(255) null,
    reason varchar(255) null,
    social_type varchar(10) not null
);

create table suggestion
(
    suggestion_id bigint auto_increment
        primary key,
    content varchar(255) null,
    created_at datetime null,
    image varchar(255) null,
    nickname varchar(255) null,
    phone varchar(255) null,
    user_id bigint null
);

create table ticket
(
    ticket_id bigint auto_increment
        primary key,
    count int not null
);

create table topic
(
    dtype varchar(31) not null,
    topic_id bigint not null
        primary key,
    src varchar(255) null,
    title varchar(255) null,
    interest_id bigint null,
    subject varchar(255) null
);

create table user
(
    user_id bigint auto_increment
        primary key,
    created_at datetime not null,
    updated_at datetime not null,
    region varchar(255) null,
    state varchar(255) null,
    admin bit not null,
    age int not null,
    device_token varchar(255) null,
    mbti varchar(255) null,
    my_gender varchar(10) null,
    nickname varchar(10) null,
    partner_gender varchar(10) null,
    phone varchar(255) null,
    platform varchar(255) null,
    social_id varchar(255) null,
    social_type varchar(10) not null,
    status varchar(255) null,
    voice text null,
    matching_history_id bigint null,
    notification_setting_id bigint null,
    ticket_id bigint null,
    constraint UK_589idila9li6a4arw1t8ht1gx
        unique (phone),
    constraint UK_m3tiv2iugdximo00e50thjcbh
        unique (social_id),
    constraint FK6r48iqeribkca61g52v7jj4xm
        foreign key (ticket_id) references ticket (ticket_id),
    constraint FKejlkvmtwoa97dsrn3u15865o8
        foreign key (matching_history_id) references matching_history (matching_history_id),
    constraint FKnxr20l3vgstqcw9o1afvdm1l6
        foreign key (notification_setting_id) references notification_setting (notification_setting_id)
);

create table notice
(
    dtype varchar(31) not null,
    notice_id bigint auto_increment
        primary key,
    created_at datetime not null,
    updated_at datetime not null,
    content text null,
    title varchar(255) null,
    user_id bigint null,
    constraint FKcvf4mh5se36inrxn7xlh2brfv
        foreign key (user_id) references user (user_id)
);

create table profile_image
(
    profile_image_id bigint not null
        primary key,
    created_at datetime not null,
    updated_at datetime not null,
    sequence int not null,
    src text null,
    status varchar(255) null,
    user_id bigint null,
    constraint FK7c5ge678vgxydo2sepdmrj6ge
        foreign key (user_id) references user (user_id)
);

create table report
(
    report_id bigint auto_increment
        primary key,
    created_at datetime not null,
    updated_at datetime not null,
    matching_id bigint null,
    reason_id bigint null,
    reported bigint null,
    reporter bigint null,
    constraint FK7ak4xcyfux0igvm1j5cqud5d8
        foreign key (reason_id) references reason (reason_id),
    constraint FKf57jf06r4tjpacaou1uue4xgd
        foreign key (reported) references user (user_id),
    constraint FKq4q3ntts6prfw5n852srs70bn
        foreign key (reporter) references user (user_id)
);

create table user_drink
(
    user_drink_id bigint not null
        primary key,
    created_at datetime not null,
    updated_at datetime not null,
    drink_id bigint null,
    user_id bigint null,
    constraint FKerb2327ue72hwui86fkehj500
        foreign key (drink_id) references drink (drink_id),
    constraint FKp6qtsfb41er7yppx5w8hetwlu
        foreign key (user_id) references user (user_id)
);

create table user_interest
(
    user_interest_id bigint not null
        primary key,
    created_at datetime not null,
    updated_at datetime not null,
    active bit not null,
    interest_id bigint null,
    user_id bigint null,
    constraint FKb2c20k2dqknrm5t337typ3s1b
        foreign key (interest_id) references interest (interest_id),
    constraint FKdi9smphhv09dottb2sc1j3k64
        foreign key (user_id) references user (user_id)
);

create table user_matching
(
    user_matching_id bigint auto_increment
        primary key,
    created_at datetime not null,
    updated_at datetime not null,
    interests varchar(255) null,
    is_accept_exchange_profile bit null,
    status varchar(255) not null,
    drink_id bigint null,
    matching_id bigint null,
    user_id bigint null,
    constraint FKj3q8ldn7yinpnhmn4qh3b8tiv
        foreign key (drink_id) references drink (drink_id),
    constraint FKl7gejlkdtjoe3p2n18i9rv9pk
        foreign key (user_id) references user (user_id),
    constraint FKmvmym7ftbi2j5eabgul9xuiqc
        foreign key (matching_id) references matching (matching_id)
);