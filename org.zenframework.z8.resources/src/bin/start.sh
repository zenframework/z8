#!/usr/bin/env sh

HOME="\$( cd "\$(dirname "\$0")/.." ; pwd -P )"
LOG="\$HOME/log"

mkdir -p \$LOG

(cd "\$WORK" && "\$HOME/bin/service.sh" 1>\$LOG/out.log 2>\$LOG/err.log) &
