--------------------------------------------------------DJIAerialPhotography----------------------------------------------------
	The DJI Aerial Photography application autonomously flies over a user-specified area and takes pictures of it. These pictures
can then be used with a structure from motion (SFM) application to be put together and create an aerial map of a certain area.



Prerequisites: Android Studio (https://developer.android.com/studio/install.html)


Create APK:
1. Download the Github repository onto your computer and extract it.
2. Open Android Studio.
3. Select the folder “FPVDemo” as the Android Studio Project to import.
4. Click on Build >> Make Project

Install & Use APK:
1. Download the APK onto your phone.
2. Open up the app and give it the necessary permissions.
3. Ensure that your phone is connected to the internet, exit the app, and reopen it.
4. Open the app again. If successful, a small text box should pop up saying “Registered”. 
If not,  try connecting your phone to the internet through cellular data. Note that you will only have to register this app once.
5. After turning on both the drone and remote, open the app and connect the phone to the remote via a micro USB cable to the remote. Ensure that the drone is on the ground and not armed when first starting the application.
6. If the drone is connected to the remote, the connect button should be blue and enabled. 
7. Click the blue button and enter the requested information on the next page.
8. After entering the requested information, click the “Submit” button. If the information is judged by the application to be incorrect, a popup message will be displayed giving the reason why the input given may be invalid.
9. On the next screen, there will be two buttons: “Initialize Drone” and “Start Aerial Photography”.  Hit the “Initialize Drone” button to activate the drone to take off. Note that one should only press the Initialize Drone button when the drone is on the ground an unarmed.
10. After, and only after, the pop up saying “Ready for Flight!”, one is ready to hit the “Start Aerial Photography” button. While the drone will have been set for obstacle avoidance, please ensure that you keep an eye on the drone and ensure that it stays away from hitting people and property. If you see the drone about to hit something, unplug the phone and you will be able to regain control of the drone. However, the application will start to malfunction after this and one may need to either quit and/or reinstall the application.
