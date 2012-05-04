@echo off

REM *******************************************************
REM Build file for building Scarab's database and content for "sample"
REM *******************************************************

mvn clean initialize scarab:create-db -Dscarab.database.build.mode=sample