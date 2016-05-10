#!/bin/bash
if [ "${TRAVIS_BRANCH}" = "sonar-qube" ]; then
   mvn sonar:sonar
fi