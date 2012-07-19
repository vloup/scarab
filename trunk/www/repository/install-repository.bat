call mvn install:install-file -DgroupId=fulcrum -DartifactId=fulcrum-parser -Dversion=1.0.2-dev-r223202-patched -Dfile=fulcrum/jars/fulcrum-parser-1.0.2-dev-r223202-patched.jar -Dpackaging=jar

call mvn install:install-file -DgroupId=turbine -DartifactId=turbine -Dversion=20041117.142852 -Dfile=turbine/jars/turbine-20041117.142852.jar -Dpackaging=jar

REM do the same for jndi-1.2.1.jar

