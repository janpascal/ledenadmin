# --- !Ups


alter table factuur modify bedrag decimal(38,2) not null

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

alter table factuur modify bedrag decimal(38) not null

SET FOREIGN_KEY_CHECKS=1;

