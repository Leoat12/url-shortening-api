create schema url_shortening;

alter schema url_shortening owner to postgres;

set schema 'url_shortening';

create table if not exists url_info
(
    id        bigserial not null
        constraint url_info_pkey
            primary key,
    long_url  varchar(1000),
    short_url varchar(100),
    expiry_at date
);

alter table url_info
    owner to postgres;

