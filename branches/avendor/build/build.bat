@echo off
echo.

rem #--------------------------------------------
rem # No need to edit anything past here
rem #--------------------------------------------

set BUILDFILE=build.xml

if "%JAVA_HOME%" == "" goto JavaHomeError

if exist %JAVA_HOME%\lib\tools.jar set CLASSPATH=%CLASSPATH%;%JAVA_HOME%\lib\tools.jar

set CLASSPATH=%CLASSPATH%;ant-1.2.jar

%JAVA_HOME%\bin\java -classpath %CLASSPATH% org.apache.tools.ant.Main -buildfile %BUILDFILE% %1 %2 %3 %4 %5 %6 %7 %8 %9

goto End

:JavaHomeError
    echo ERROR: JAVA_HOME not found in your environment.
    echo Please, set the JAVA_HOME variable in your environment to match the
    echo location of the Java Virtual Machine you want to use.
    
    goto end

:End