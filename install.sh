#!/bin/bash

rm -rf lejos
wget http://heanet.dl.sourceforge.net/project/ev3.lejos.p/0.9.1-beta/leJOS_EV3_0.9.1-beta_source.tar.gz
tar xvfz leJOS_EV3_0.9.1-beta_source.tar.gz
mv leJOS_EV3_0.9.1-beta_source lejos
rm leJOS_EV3_0.9.1-beta_source.tar.gz

rm -rf lib
mkdir -p lib
scp ev3:/home/root/lejos/lib/opencv-2411.jar ./lib/
