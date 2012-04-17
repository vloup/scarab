drop database if exists @DB_NAME@;
create database @DB_NAME@;
grant all on @DB_NAME@.* to @DATABASE_USERNAME@ identified by '@DATABASE_PASSWORD@';