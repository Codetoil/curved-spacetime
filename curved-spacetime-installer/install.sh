#!/bin/sh
./apache-maven-3.9.10/bin/mvn -q -Dmaven.repo.local=$HOME/.curved-spacetime/maven/ -U install
rm target/curved-spacetime-installer-pomxml-0.1.0-SNAPSHOT.jar target/maven-archiver/pom.properties
rmdir target/maven-archiver target
echo "Done. Press ENTER to exit the installer."
read dummyVar