Onni Smart Care Desktop Application

What is it?
Requirements
Installation
Licensing
Known issues
Contact
-----------------


What is it?
----------------
Onni Smart Care Desktop Application is a Java written application that uses ffmpeg and VLC libraries to stream from the computer's webcam.
The purpose of the application is to allow the parents to remotely monitor their children using the Onni Smart Care Web or Mobile applications.

Requirements
----------------
Onni Smart Care Desktop Application works with the following Operation Systems:
- Windows XP or latest
- Linux


Installation
----------------
You need Ant to compile the source code. After installing Ant go to the root of the project and run the command 'ant package'. 

Running the application
----------------
To start the application: 
        On Windows: Open the file startWebcam.jar from extraction directory.
        On Linux: Go to /usr/share/webcamstreaming directory and run the startWebcam.jar as sudo.

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

Contact
----------------
Erasmus van Niekerk <erasmus.van.niekerk@sepsolutions.fi>




