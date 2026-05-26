@echo off
cd /d "%~dp0"
mvn -q -f "%~dp0pom.xml" org.openjfx:javafx-maven-plugin:0.0.8:run
pause
