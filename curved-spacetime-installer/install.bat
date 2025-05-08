@echo off
cmd /c ""apache-maven-3.9.9\bin\mvn.cmd" -q -Dmaven.repo.local=%userprofile%\.curved-spacetime\maven\ -U install"
attrib +h %userprofile%\.curved-spacetime
del target\curved-spacetime-installer-pomxml-0.1.0-SNAPSHOT.jar
del target\maven-archiver\pom.properties
rmdir target\maven-archiver
rmdir target
echo Done. Press any key (Such as ENTER) to exit the installer.
pause >nul