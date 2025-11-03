#!/bin/sh

##############################################################################
# Gradle start up script for UN*X
##############################################################################

# Attempt to set APP_HOME
SAVED="`pwd`"
cd "`dirname \"$0\"`"
APP_HOME="`pwd -P`"
cd "$SAVED"

exec java -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
