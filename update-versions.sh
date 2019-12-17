#!/bin/bash
CURRENT_VERSION=$(mvn help:evaluate -pl backend-base -Dexpression=project.version -q -DforceStdout)
CURRENT_MAJOR=$(echo $CURRENT_VERSION | cut -d . -f1)
CURRENT_MINOR=$(echo $CURRENT_VERSION | cut -d . -f2)
CURRENT_PATCH=$(echo $CURRENT_VERSION | cut -d . -f3)
NEW_PATCH=$((CURRENT_PATCH+1))
NEW_VERSION_STRING="<backend-base-version>$CURRENT_MAJOR.$CURRENT_MINOR.$NEW_PATCH</backend-base-version>"
sed  -i.bak -e "s#<backend-base-version>[0-9]\+\.[0-9]\+\.[0-9]\+</backend-base-version>#$NEW_VERSION_STRING#g" pom.xml
