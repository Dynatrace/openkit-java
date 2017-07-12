@ECHO OFF

ECHO Cleaning up old javadoc
rmdir /s /q javadoc

ECHO Generating new javadoc ...
ECHO.
%JAVA_HOME%\bin\javadoc -public -sourcepath src/main/java -d javadoc com.dynatrace.openkit.api com.dynatrace.openkit

ECHO.
ECHO DONE!