#!/bin/bash
PARENT_CURRENT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
PARENT_CURRENT_MAJOR=$(echo $PARENT_CURRENT_VERSION | cut -d . -f1)
PARENT_CURRENT_MINOR=$(echo $PARENT_CURRENT_VERSION | cut -d . -f2)
PARENT_CURRENT_PATCH=$(echo $PARENT_CURRENT_VERSION | cut -d . -f3)
PARENT_NEW_PATCH=$((PARENT_CURRENT_PATCH+1))
PARENT_NEW_VERSION_STRING="$PARENT_CURRENT_MAJOR.$PARENT_CURRENT_MINOR.$PARENT_NEW_PATCH"
xq --arg NEW_VERSION_STRING "$PARENT_NEW_VERSION_STRING" -x '.project.version=$NEW_VERSION_STRING' pom.xml  > new-pom.xml
mv new-pom.xml pom.xml
BACKEND_BASE_CURRENT_VERSION=$(mvn help:evaluate -pl backend-base -Dexpression=project.version -q -DforceStdout)
BACKEND_BASE_CURRENT_MAJOR=$(echo $BACKEND_BASE_CURRENT_VERSION | cut -d . -f1)
BACKEND_BASE_CURRENT_MINOR=$(echo $BACKEND_BASE_CURRENT_VERSION | cut -d . -f2)
BACKEND_BASE_CURRENT_PATCH=$(echo $BACKEND_BASE_CURRENT_VERSION | cut -d . -f3)
BACKEND_BASE_NEW_PATCH=$((BACKEND_BASE_CURRENT_PATCH+1))
BACKEND_BASE_NEW_VERSION_STRING="$BACKEND_BASE_CURRENT_MAJOR.$BACKEND_BASE_CURRENT_MINOR.$BACKEND_BASE_NEW_PATCH"
xq --arg NEW_VERSION_STRING "$BACKEND_BASE_NEW_VERSION_STRING" -x '.project.version=$NEW_VERSION_STRING' backend-base/pom.xml  > backend-base/new-pom.xml
mv backend-base/new-pom.xml backend-base/pom.xml
xq --arg PARENT_VERSION "$PARENT_NEW_VERSION_STRING" -x '.project.parent.version=$PARENT_VERSION' backend-base/pom.xml > backend-base/new-pom.xml
mv backend-base/new-pom.xml backend-base/pom.xml
JAVASCRIPT_CURRENT_VERSION=$(mvn help:evaluate -pl javascript -Dexpression=project.version -q -DforceStdout)
JAVASCRIPT_CURRENT_MAJOR=$(echo $JAVASCRIPT_CURRENT_VERSION | cut -d . -f1)
JAVASCRIPT_CURRENT_MINOR=$(echo $JAVASCRIPT_CURRENT_VERSION | cut -d . -f2)
JAVASCRIPT_CURRENT_PATCH=$(echo $JAVASCRIPT_CURRENT_VERSION | cut -d . -f3)
JAVASCRIPT_NEW_PATCH=$((JAVASCRIPT_CURRENT_PATCH+1))
JAVASCRIPT_NEW_VERSION_STRING="$JAVASCRIPT_CURRENT_MAJOR.$JAVASCRIPT_CURRENT_MINOR.$JAVASCRIPT_NEW_PATCH"
xq --arg NEW_VERSION_STRING "$JAVASCRIPT_NEW_VERSION_STRING" -x '.project.version=$NEW_VERSION_STRING' javascript/pom.xml > javascript/new-pom.xml
mv javascript/new-pom.xml javascript/pom.xml
xq --arg PARENT_VERSION "$PARENT_NEW_VERSION_STRING" -x '.project.parent.version=$PARENT_VERSION' javascript/pom.xml > javascript/new-pom.xml
mv javascript/new-pom.xml javascript/pom.xml
