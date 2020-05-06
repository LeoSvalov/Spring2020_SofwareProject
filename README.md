# Automated Attendance Monitoring

An android app, which collects attendance using bluetooth and saves results to the database<br>
It was a project on Software Project Spring 2020 (2nd year) course in Innopolis University



## Useful links

* [Our web app for database API and online attendance viewing](https://github.com/sham1lk/AMS)
* [Latest version .apk file](https://yadi.sk/d/Vn6nqKWbzwDUwQ)



## How it looks like

Actual interface differs (a little bit) from what shown below.<br>
These gifs just show an idea of how app looks like and what it can do. 

| Student's interface | Teacher's interface |
| --- | --- |
| <img src="https://i.imgur.com/7aHX9PH.gif" width="240" height="480"> | <img src="https://i.imgur.com/a23zwLf.gif" width="240" height="480"> |



## How to import it

If you are using *Android Studio*, just use this dialog: **File > New > Project from Version Control... > Git**

If you want to change this app for your own needs, you might want to:
* Change `UUID` constant in [`BluetoothHelper.java`](/app/src/main/java/com/example/automatedattendancemonitoring/BluetoothHelper.java) file. This constant is supposed to be unique for each app.
* Have running api which will recieve api calls and add records to the database. This app was written to work with this one: [our web app for database API and online attendance viewing](https://github.com/sham1lk/AMS)
* Change `apiAddress` constant in [`DatabaseHelper.java`](/app/src/main/java/com/example/automatedattendancemonitoring/DatabaseHelper.java) file
* Change [`DatabaseHelper.java`](/app/src/main/java/com/example/automatedattendancemonitoring/DatabaseHelper.java) file to reflect your database API or to not work with database at all.