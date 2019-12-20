#!/bin/bash
BACKEND_BASE_CURRENT_VERSION=$(mvn help:evaluate -pl backend-base -Dexpression=project.version -q -DforceStdout)
BACKEND_BASE_CURRENT_MAJOR=$(echo $BACKEND_BASE_CURRENT_VERSION | cut -d . -f1)
BACKEND_BASE_CURRENT_MINOR=$(echo $BACKEND_BASE_CURRENT_VERSION | cut -d . -f2)
BACKEND_BASE_CURRENT_PATCH=$(echo $BACKEND_BASE_CURRENT_VERSION | cut -d . -f3)
BACKEND_BASE_NEW_PATCH=$((BACKEND_BASE_CURRENT_PATCH+1))
BACKEND_BASE_NEW_VERSION_STRING="<backend-base-version>$BACKEND_BASE_CURRENT_MAJOR.$BACKEND_BASE_CURRENT_MINOR.$BACKEND_BASE_NEW_PATCH</backend-base-version>"
sed  -i.bak -e "s#<backend-base-version>[0-9]\+\.[0-9]\+\.[0-9]\+</backend-base-version>#$BACKEND_BASE_NEW_VERSION_STRING#g" pom.xml
PARENT_CURRENT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
PARENT_CURRENT_MAJOR=$(echo $PARENT_CURRENT_VERSION | cut -d . -f1)
PARENT_CURRENT_MINOR=$(echo $PARENT_CURRENT_VERSION | cut -d . -f2)
PARENT_CURRENT_PATCH=$(echo $PARENT_CURRENT_VERSION | cut -d . -f3)
PARENT_NEW_PATCH=$((PARENT_CURRENT_PATCH+1))
PARENT_NEW_VERSION_STRING="<parent-version>$PARENT_CURRENT_MAJOR.$PARENT_CURRENT_MINOR.$PARENT_NEW_PATCH</parent-version>"
sed  -i.bak -e "s#<parent-version>[0-9]\+\.[0-9]\+\.[0-9]\+</parent-version>#$PARENT_NEW_VERSION_STRING#g" pom.xml
JAVASCRIPT_CURRENT_VERSION=$(mvn help:evaluate -pl javascript -Dexpression=project.version -q -DforceStdout)
JAVASCRIPT_CURRENT_MAJOR=$(echo $JAVASCRIPT_CURRENT_VERSION | cut -d . -f1)
JAVASCRIPT_CURRENT_MINOR=$(echo $JAVASCRIPT_CURRENT_VERSION | cut -d . -f2)
JAVASCRIPT_CURRENT_PATCH=$(echo $JAVASCRIPT_CURRENT_VERSION | cut -d . -f3)
JAVASCRIPT_NEW_PATCH=$((JAVASCRIPT_CURRENT_PATCH+1))
JAVASCRIPT_NEW_VERSION_STRING="<javascript-version>$JAVASCRIPT_CURRENT_MAJOR.$JAVASCRIPT_CURRENT_MINOR.$JAVASCRIPT_NEW_PATCH</javascript-version>"
sed  -i.bak -e "s#<javascript-version>[0-9]\+\.[0-9]\+\.[0-9]\+</javascript-version>#$JAVASCRIPT_NEW_VERSION_STRING#g" pom.xml
