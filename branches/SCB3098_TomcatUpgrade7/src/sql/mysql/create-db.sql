drop database if exists ${scarab.database.name};
create database ${scarab.database.name};
grant all on ${scarab.database.name}.* to ${scarab.database.username} identified by '${scarab.database.password}';