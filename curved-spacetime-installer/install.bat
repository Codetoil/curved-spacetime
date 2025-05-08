@echo off
cmd /c ""apache-maven-3.9.9/bin/mvn.cmd" -U install"
del target\curved-spacetime-installer-pomxml-0.1.0-SNAPSHOT.jar
del target\maven-archiver\pom.properties
rmdir target\maven-archiver
rmdir target
echo Please press ENTER to exit.
pause >nul