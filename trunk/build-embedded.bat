@echo off

REM *******************************************************
REM Build Scarab's embedded version (hypersonic database)
REM
REM *******************************************************

call mvn clean

if errorlevel 1 goto BatchException

call mvn initialize scarab:create-db -Dscarab.database.build.mode=default -DapplicationRoot=target/scarab

if errorlevel 1 goto BatchException

call mvn package -Dmaven.test.skip=true

:BatchException

echo Error, build was aborted