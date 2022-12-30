#!/bin/bash

echo -e "[ Entrypoint args ] $@\n---"

if [ $# -eq 1 -a x"$1" == x"start" ] ; then
    /bin/bash /opt/welab/wefe-flow/flow/service.sh start
    while : ; do tail -f /dev/null ; done
fi

## else default to run whatever the user wanted like "bash" or "sh"
exec "$@"
