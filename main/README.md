# Leo Martins & Gordon Zhu, hw1p0 EC531

# Run Tracker

Run Tracker is an Android application designed to help you monitor your running sessions by tracking real-time location, speed, and elapsed time. The app provides an intuitive user interface and several useful features to enhance your running experience.

## Features

- **Location Tracking:**  
  Utilizes the device's GPS to display current latitude and longitude.

- **Speed Measurement:**  
  Shows the current speed with an option to display in either miles per hour (mph) or meters per second (m/s). The conversion used is 1 m/s ≈ 2.23694 mph.

- **Elapsed Timer:**  
  A built-in timer that displays the elapsed time since the session started or was last reset.

- **User Controls:**
  - **Reset Button:** Resets the timer, UI elements, and font size to their default settings.
  - **Unit Toggle Button:** Switches the speed display between mph and m/s.
  - **Pause/Resume Button:** Allows you to pause and resume both the timer and location updates.
  - **Font Size Adjustment:** A seek bar to dynamically adjust the font size of the speed display.
  - **Help Button:** Provides a help dialog with basic usage instructions.

- **Developer Mode:**  
  A toggleable mode that simulates location and speed data for testing purposes (e.g., using fixed values for latitude, longitude, and speed).

## Installation

### Prerequisites

- [Android Studio](https://developer.android.com/studio) (latest version recommended)
- Android SDK and relevant build tools
- An Android device or emulator with GPS support

### Steps

1. **Clone the Repository:**
   ```bash
   git clone https://agile.bu.edu/gitlab/ec531/homeworks/solutions/homework-one/hw1p0/group6.git
2. **Open the Project:**
   - Open Android Studio.
   - Build gradle and sync the project.
3. **Run the App:**
   - Connect your Android device or start an emulator.
   - Click the "Run" button in Android Studio to install and launch the app.

----

# HomeworkOneGroup5

## Team: Jerry Zheng + Minghong Zou


Update
------------------------------------

1. MainActivity

    Update the function to allow import user preference 

    Fix Fake location issue, dev mode fake location can now successfully simulate the virtual source

2. UserDatabase

   UserDatabase will maintain and use a SQLlite database that allows the program to 
   save user information and find user information, save measurements, 
   and maintain a method that allows the user to clear measurements when reset

3. RegisterActivity

   Allows users to write user preferences, user names and passwords to the database

4. LoginActivity

   The App will compare the information entered by the user with the user information stored in the
   database, and if it is consistent, it will allow login

-------

Individualize
----------------

Minghong Zou: Issue 8 Memory, Issue 10 Database, Issue17 Acceleration
Jerry Zheng: Issue 9 Change indicator, Issue11 Stats, Issue12 Moving time