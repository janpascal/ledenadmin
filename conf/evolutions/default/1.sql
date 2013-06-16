# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table betaling (
  id                        bigint not null,
  bedrag                    bigint,
  dc                        integer,
  tegenrekening             varchar(255),
  vermelding                varchar(255),
  constraint ck_betaling_dc check (dc in (0,1)),
  constraint pk_betaling primary key (id))
;

create table lid (
  id                        bigint not null,
  name                      varchar(255),
  name2                     varchar(255),
  lid_sinds                 timestamp,
  lid_tot                   timestamp,
  address                   varchar(255),
  constraint pk_lid primary key (id))
;

create sequence betaling_seq;

create sequence lid_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists betaling;

drop table if exists lid;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists betaling_seq;

drop sequence if exists lid_seq;

