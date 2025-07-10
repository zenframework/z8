#!/usr/bin/env sh

### !!! NOTE !!! ###
# If Ubuntu, change sh to bash
### !!! NOTE !!! ###

#export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64

# if "true" enable debug mode
#DEBUG="true"

FORM_CONTENT_SIZE=15000000

HOME="\$( cd "\$(dirname "\$0")/.." ; pwd -P )"
CONF="\$HOME/conf"
WEB="\$HOME/web"
WORK="\$HOME/work"

BOOT_CP="\$HOME/lib/${project.z8BootLib}"

JAVA_OPTS="\$JAVA_OPTS -Xmx2048M"
JAVA_OPTS="\$JAVA_OPTS -Xbootclasspath/p:\$BOOT_CP"
JAVA_OPTS="\$JAVA_OPTS -Dorg.eclipse.jetty.server.Request.maxFormContentSize=\$FORM_CONTENT_SIZE"
JAVA_OPTS="\$JAVA_OPTS -Dorg.mortbay.http.HttpRequest.maxFormContentSize=\$FORM_CONTENT_SIZE"
JAVA_OPTS="\$JAVA_OPTS -Dz8.application.working.path=\$WORK"
JAVA_OPTS="\$JAVA_OPTS -Dz8.web.server.webapp=\$WEB"

if [ "\$DEBUG" == "true" ] ; then
 JAVA_OPTS="\$JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=9999,server=y,suspend=n"
fi

export JAVA_OPTS

(cd "\$CONF" && "\$HOME/bin/${project.name}" -server webserver)
