#!/bin/bash
# Run this command in order to configure the dependencies required by the application
sudo ldconfig
# Remove lua directory from vlc lib because vlcj doesn't work with java7 on 32 bits LINUX 
MACHINE_TYPE=`uname -m`
if [ ${MACHINE_TYPE} != 'x86_64' ]; then
  # remove lua directory from vlc if exists
  if [ -d "/usr/lib/vlc/lua" ]; then
       sudo rm -r /usr/lib/vlc/lua
  fi
fi
