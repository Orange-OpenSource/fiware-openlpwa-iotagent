#!/bin/sh

cat DockerReleaseNote.txt | head -10 | tail -6
echo
java -jar "fiware-openlpwa-genericagent-1.0.1.jar"