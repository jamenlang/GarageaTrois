GarageaTrois
============
If viewing on GitHub use RAW to see the code formatting.

Android App Client for GarageaTrois Server

If compiling yourself:
Edit the following values in /app/src/main/res/xml/pref_server_settings.xml

'<string name="server_URL">YOUR-SERVER-URL-OR-IP</string>'
e.g. tocos.com or backstreetboysrule.net
'<string name="script_path">YOUR-SCRIPT-PATH</string>'
e.g. guacamole or NickCarter
'<string name="script_name">YOUR-SCRIPT-NAME</string>'
e.g. GarageaTrois-Server.php or GaT.php

and make sure these two are the same as the server script.

'<string name="adminresult">ADMIN-RESULT-FROM-SERVER</string>'
e.g. taco or backstreetsback
'<string name="userresult">USER-RESULT-FROM-SERVER</string>'
e.g. grande or alright!

and finally sleepytime, the time that it takes the door to open and close.
<string name="sleepytime">HOW-LONG-IT-TAKES-FOR-DOOR-TO-MOVE(11.4 = 11.4 seconds)</string>
e.g. 11.4 for 11.4 or 30.9 for 30.9

also set 
'<string name="disablesettings">value</string>' to true.
