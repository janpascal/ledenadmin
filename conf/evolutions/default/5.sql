# --- !Ups

CREATE TABLE `probe_response` (
    id        bigint auto_increment not null,
    `probe_id`  bigint NOT NULL,
    `timestamp` datetime NOT NULL,
    `type`      char(10) not null,
    INDEX ( `id` ),
    INDEX ( `probe_id` ),
    INDEX ( `timestamp` )
) DEFAULT CHARSET=utf8;


# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table probe_response;

SET FOREIGN_KEY_CHECKS=1;

