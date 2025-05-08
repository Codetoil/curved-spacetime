#!/bin/sh
./apache-maven-3.9.9/bin/mvn -U install
rm target/curved-spacetime-installer-pomxml-0.1.0-SNAPSHOT.jar target/maven-archiver/pom.properties
rmdir target/maven-archiver target
echo "Please press ENTER to exit."
read dummyVar