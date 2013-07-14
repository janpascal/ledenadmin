# --- !Ups

CREATE TABLE `security_role` (
    id        bigint auto_increment not null,
    name      varchar(255) not null,
    constraint pk_security_role PRIMARY KEY (ID)
) DEFAULT CHARSET=utf8;

CREATE TABLE `persoon_security_role` (
    id        bigint auto_increment not null,
    persoon_id   bigint NOT NULL,
    security_role_id  bigint NOT NULL,
    constraint pk_persoon_security_role PRIMARY KEY (ID)
) DEFAULT CHARSET=utf8;

alter table persoon_security_role add constraint fk_persoon_security_role_persoon foreign key (persoon_id) references persoon (id) on delete restrict on update restrict;
alter table persoon_security_role add constraint fk_persoon_security_role_security_role foreign key (security_role_id) references security_role (id) on delete restrict on update restrict;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table security_role;
drop table persoon_security_role;

SET FOREIGN_KEY_CHECKS=1;

