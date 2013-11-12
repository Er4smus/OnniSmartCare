Onni Smart Care Desktop Application
This directory contains the 1.0 release of Onni Smart Care Desktop Application

What is it?
Requirements
Downloading
Installation
Licensing
Known issues
Troubleshooting
Authors
Contact
-----------------

What is it?
----------------
Onni Smart Care Desktop Application is a Java written application that uses ffmpeg and VLC libraries to stream from the computer's webcam.
The purpose of the application is to allow the parents to remotely monitor their children using the Onni Smart Care Web or Mobile applications.

Requirements
----------------
Onni Smart Care Desktop Application works with the following Operation Systems:
- Windows XP and up
- Linux

Downloading
----------------
Onni Smart Care Desktop Application can be downloaded from ***[link].

Installation
----------------
On Windows: 

Extract all files from the downloaded package.

On Linux:

Steps:

1. Install the .deb file

Double click on the .deb file or run the following command:
sudo dpkg -i DEB_FILENAME.deb

This will install the application in the /usr/share/webcamstreaming directory and install Java 7 if you don't have it installed.

2. Configure Java Runtime Environment

If another version of Java already exists on your computer, you have to set Java 7 as default.
To do this, run the following command:
sudo apt-get update-alternatives --config java 

Choose Java 7 by typing the number of the line which contains the Java 7 home directory. 

3. Configure the dependencies

Vlc and ffmpeg dependencies are needed for streaming. To configure all dependencies, run the /usr/share/webcamstreaming/config.sh script.


Running the application
----------------
To start the application: 
        On Windows: Open the file webcam.jar from extraction directory.
        On Linux: Go to /usr/share/webcamstreaming directory and run the webcam.jar as sudo.

You need a webcam code from your server.

Licensing
----------------
Please see the file called LICENSE.txt

Known issues
----------------
You cannot stream from outside your network in the following situations:
- your computer is connected to a switch (switches does not support UPnP)
- your computer is connected to a router that does not support UPnP protocol or the router is behind another router or switch.
- your 3G carrier blocks the packages send from the routers.
 
Troubleshooting
----------------
For recommended configurations see the file called TROUBLESHOOTING.txt.
If you see the message that the stream only works in your network see the file called TROUBLESHOOTING.txt.

Authors
----------------

Contact
----------------
Erasmus van Niekerk <erasmus.van.niekerk@sepsolutions.fi>




