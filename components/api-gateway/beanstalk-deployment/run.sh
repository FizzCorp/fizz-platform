#!/bin/bash
MEMORY_SPACE_FREE_MB="$(free -m | grep Mem | awk '{print ($4 + $6)}')"

XMS=$(echo $MEMORY_SPACE_FREE_MB 0.35 | awk '{ printf "%f", $1 * $2 }')
XMX=$(echo $MEMORY_SPACE_FREE_MB 0.7 | awk '{ printf "%f", $1 * $2}')

XMS=${XMS%.*}
XMX=${XMX%.*}

java -javaagent:./newrelic/newrelic.jar -jar application.jar -Xms${XMS}m -Xmx${XMX}m -XX:+UseConcMarkSweepGC -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=60
