@echo off
REM The directory which the scripts are pre-processed into
set POPULATION_SCRIPT_DIR=..\..\target\webapps\scarab\WEB-INF\sql

echo Drop Scarab database...
mysqladmin --force drop scarab

echo Create Scarab database...
mysqladmin --force create scarab

echo Importing turbine.sql...
mysql scarab < %POPULATION_SCRIPT_DIR%\turbine.sql

echo Importing scarab.sql...
mysql scarab < %POPULATION_SCRIPT_DIR%\scarab.sql

echo Importing scheduler.sql...
mysql scarab < %POPULATION_SCRIPT_DIR%\scheduler.sql

echo Importing id-table.sql...
mysql scarab < %POPULATION_SCRIPT_DIR%\id-table.sql

echo Importing turbine-id-table-init.sql...
mysql scarab < %POPULATION_SCRIPT_DIR%\turbine-id-table-init.sql

echo Importing scarab-id-table-init.sql...
mysql scarab < %POPULATION_SCRIPT_DIR%\scarab-id-table-init.sql

echo Importing scarab-required-data.sql...
mysql scarab < %POPULATION_SCRIPT_DIR%\scarab-required-data.sql

echo Importing scarab-default-data.sql...
mysql scarab < %POPULATION_SCRIPT_DIR%\scarab-default-data.sql

echo Importing scarab-security.sql...
mysql scarab < %POPULATION_SCRIPT_DIR%\scarab-security.sql

echo Importing scarab-sample-data.sql...
mysql scarab < %POPULATION_SCRIPT_DIR%\scarab-sample-data.sql
