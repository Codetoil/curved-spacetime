@echo off
cmd /c ""apache-maven-3.9.9/bin/mvn.cmd" -U install"
del target/curved-spacetime-installer-0.1.0-SNAPSHOT.jar
del maven-archiver/pom.properties
rmdir maven-archiver
rmdir target
echo Please press ENTER to exit.
pause >nul