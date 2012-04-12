@echo off
REM This is an alterntive build script for maven goal "site"
REM see SCOF106 accordingly

call maven clean

call maven site -Dmaven.test.skip=true -Dmaven.velocity.version=1.5 -Dtorque.doc.dir=target\docs\databases

