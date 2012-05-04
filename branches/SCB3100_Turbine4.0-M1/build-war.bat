@echo off

REM *******************************************************
REM Build file for building scarab.war and target directory
REM *******************************************************

mvn clean package -Dmaven.test.skip=true