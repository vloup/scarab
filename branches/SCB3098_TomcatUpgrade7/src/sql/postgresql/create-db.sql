drop database ${scarab.database.name};
drop user ${scarab.database.username};
create user ${scarab.database.username} with encrypted password '${scarab.database.password}';
create database ${scarab.database.name} with owner ${scarab.database.username};
 
