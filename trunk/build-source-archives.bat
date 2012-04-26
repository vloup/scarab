@echo off

REM *******************************************************
REM Build file for building Scarab's source archive to deploy
REM *******************************************************

mvn clean assembly:single -Dmaven.test.skip=true