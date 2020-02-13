#!/usr/bin/env sh

#export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64

HOME="\$( cd "\$(dirname "\$0")/.." ; pwd -P )"
WEB="\$HOME/web"
WORK="\$HOME/web/WEB-INF"

BOOT_CP="\$HOME/lib/org.zenframework.z8.boot-${project.z8Version}.jar"

JAVA_OPTS="\$JAVA_OPTS -Xbootclasspath/p:\$BOOT_CP"

export JAVA_OPTS

(cd "\$WORK" && "\$HOME/bin/${project.name}" -server webserver -stop)
