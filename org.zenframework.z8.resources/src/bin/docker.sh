#!/usr/bin/env sh

#export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64

#HOST=\$(hostname -I | awk '{print \$1}')
FORM_CONTENT_SIZE=15000000

HOME="\$( cd "\$(dirname "\$0")/.." ; pwd -P )"
LOG="\$HOME/log"
WEB="\$HOME/web"
WORK="\$HOME/work"

BOOT_CP="\$HOME/lib/org.zenframework.z8.boot-${project.z8Version}.jar"

JAVA_OPTS="\$JAVA_OPTS -Xmx2048M"
JAVA_OPTS="\$JAVA_OPTS -Xbootclasspath/p:\$BOOT_CP"
JAVA_OPTS="\$JAVA_OPTS -Dorg.eclipse.jetty.server.Request.maxFormContentSize=\$FORM_CONTENT_SIZE"
JAVA_OPTS="\$JAVA_OPTS -Dorg.mortbay.http.HttpRequest.maxFormContentSize=\$FORM_CONTENT_SIZE"
JAVA_OPTS="\$JAVA_OPTS -Dz8.web.server.webapp=\$WEB"
#JAVA_OPTS="\$JAVA_OPTS -Djava.rmi.server.hostname=\$HOST"

# Debug mode
#JAVA_OPTS="\$JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=9999,server=y,suspend=n"

# Env variables, format <<PROJECT_NAME>_<VAR>>
if [[ ! -z "\$${project.name.toUpperCase().replaceAll('\\W', '_')}_DB_SCHEMA" ]]; then
 JAVA_OPTS="\$JAVA_OPTS -Dz8.application.database.schema=\$${project.name.toUpperCase().replaceAll('\\W', '_')}_DB_SCHEMA"
fi
if [[ ! -z "\$${project.name.toUpperCase().replaceAll('\\W', '_')}_DB_USER" ]]; then
 JAVA_OPTS="\$JAVA_OPTS -Dz8.application.database.user=\$${project.name.toUpperCase().replaceAll('\\W', '_')}_DB_USER"
fi
if [[ ! -z "\$${project.name.toUpperCase().replaceAll('\\W', '_')}_DB_PASSWORD" ]]; then
 JAVA_OPTS="\$JAVA_OPTS -Dz8.application.database.password=\$${project.name.toUpperCase().replaceAll('\\W', '_')}_DB_PASSWORD"
fi
if [[ ! -z "\$${project.name.toUpperCase().replaceAll('\\W', '_')}_DB_CONNECTION" ]]; then
 JAVA_OPTS="\$JAVA_OPTS -Dz8.application.database.connection=\$${project.name.toUpperCase().replaceAll('\\W', '_')}_DB_CONNECTION"
fi

# Alternate env variables, format <Z8_<VAR>>
if [[ ! -z "\$Z8_DB_SCHEMA" ]]; then
 JAVA_OPTS="\$JAVA_OPTS -Dz8.application.database.schema=\$Z8_DB_SCHEMA"
fi
if [[ ! -z "\$Z8_DB_USER" ]]; then
 JAVA_OPTS="\$JAVA_OPTS -Dz8.application.database.user=\$Z8_DB_USER"
fi
if [[ ! -z "\$Z8_DB_PASSWORD" ]]; then
 JAVA_OPTS="\$JAVA_OPTS -Dz8.application.database.password=\$Z8_DB_PASSWORD"
fi
if [[ ! -z "\$Z8_DB_CONNECTION" ]]; then
 JAVA_OPTS="\$JAVA_OPTS -Dz8.application.database.connection=\$Z8_DB_CONNECTION"
fi

export JAVA_OPTS

rm "\$WORK/*.cache"

(cd "\$WORK" && "\$HOME/bin/${project.name}" -server webserver)