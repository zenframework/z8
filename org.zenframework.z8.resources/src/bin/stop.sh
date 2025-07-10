#!/usr/bin/env sh

#export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64

HOME="\$( cd "\$(dirname "\$0")/.." ; pwd -P )"
WEB="\$HOME/web"
WORK="\$HOME/work"

BOOT_CP="\$HOME/lib/${project.z8BootLib}"

JAVA_OPTS="\$JAVA_OPTS -Xbootclasspath/p:\$BOOT_CP"

export JAVA_OPTS

(cd "\$WORK" && "\$HOME/bin/${project.name}" -server webserver -stop)
