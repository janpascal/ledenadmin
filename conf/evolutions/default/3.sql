# --- !Ups


alter table factuur change _type type integer(31) not null

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

alter table factuur change type _type integer(31) not null

SET FOREIGN_KEY_CHECKS=1;

