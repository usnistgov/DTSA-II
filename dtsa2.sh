#!/bin/bash
# -----------------------------------------------------------------------------
# dtsa2.sh - Start script for the DTSA-II under Linux (Ubuntu 7.10)
# -----------------------------------------------------------------------------

# ----- Change to the directory in which dtsa2.jar is located -----------------
cd $INSTALL_PATH
# ----- Execute The Requested Command -----------------------------------------
exec java -Xmx256m -Xss1024k -jar dtsa2.jar
