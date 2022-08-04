#!/usr/bin/env sh

HOME="\$( cd "\$(dirname "\$0")/.." ; pwd -P )"

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
if [[ ! -z "\$${project.name.toUpperCase().replaceAll('\\W', '_')}_LANGUAGE" ]]; then
 JAVA_OPTS="\$JAVA_OPTS -Dz8.application.language=\$${project.name.toUpperCase().replaceAll('\\W', '_')}_LANGUAGE"
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
if [[ ! -z "\$Z8_LANGUAGE" ]]; then
 JAVA_OPTS="\$JAVA_OPTS -Dz8.application.language=\$Z8_LANGUAGE"
fi

export JAVA_OPTS

"\$HOME/bin/service.sh"
