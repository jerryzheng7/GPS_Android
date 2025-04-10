/**
 * REQUIRED STATEMENT: All javadoc-ing in this document was done by chatGPT.
 * PROMPT: javadoc this file
 */
package com.example.runtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar;
import androidx.appcompat.widget.SwitchCompat;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * MainActivity is the main entry point of the Run Tracker application.
 * <p>
 * This activity handles location tracking by checking for location permissions,
 * requesting location updates from the GPS provider, and displaying the current
 * latitude, longitude, and speed on the screen using string concatenation.
 * It also includes a timer functionality that displays elapsed time since the start
 * of the application or the last reset, along with buttons to reset the timer,
 * toggle the speed unit between miles per hour (mph) and meters per second (m/s),
 * and pause/resume both the timer and location updates. The size of the text that
 * displays speed can also be changed using a seek bar.
 * </p>
 */
public class MainActivity extends AppCompatActivity {
    /** Conversion factor to convert speed from meters per second to miles per hour.
     * 1 meter per second is approximately equal to 2.23694 miles per hour.
     */
    private static final double MPS_TO_MPH = 2.23694;

    /** Default progress on seek bar (maximum value is 100) */
    private static final int DEFAULT_SEEKBAR_PROGRESS = 40;

    /** Manager for accessing system location services. */
    private LocationManager locationManager;

    /** Listener for receiving location updates. */
    private LocationListener locationListener;

    /** TextView for displaying the current location details. */
    private TextView locationTextView;

    /** TextView for displaying the current speed. */
    private TextView speedTextView;

    /** TextView for displaying the elapsed timer value. */
    private TextView timerTextView;

    /** Button for resetting the timer. */
    private Button resetButton;

    /** Button for toggling the speed unit. */
    private Button unitToggleButton;

    /** Button for pausing/resuming the timer and location updates. */
    private Button pauseButton;

    /** Button for display help information. */
    private Button helpButton;

    /** Request code for location permission. */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /** Handler for scheduling timer updates. */
    private final Handler handler = new Handler();

    /** Start time in milliseconds for the timer. */
    private long startTime;

    /** Flag indicating whether the timer is currently running. */
    private boolean isTimerRunning = false;

    /** Flag indicating whether the app is in development mode. */
    private boolean isDevMode = false;

    /**
     * Flag indicating whether the speed is displayed in miles per hour.
     * If false, the speed will be displayed in meters per second.
     */
    private boolean useMph = true;

    /** Flag indicating whether the app is currently paused. */
    private boolean isPaused = false;

    /** The elapsed time when the app was paused. */
    private long pausedTime = 0;

    /** SeekBar to adjust the font size of the speed display. */
    private SeekBar fontSizeSeekBar;

    /**
     * Runnable that updates the timer TextView every second with the elapsed time.
     */
    private final Runnable updateTimeRunnable = new Runnable() {
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
            timerTextView.setText("Elapsed Time: " + elapsedTime + " sec");
            handler.postDelayed(this, 1000);
        }
    };

    /**
     * Called when the activity is first created.
     * <p>
     * Initializes the user interface, sets up the location listener, timer, reset button,
     * unit toggle button, and pause/resume button. Checks for location permission and starts receiving
     * location updates if granted.
     * </p>
     *
     * @param savedInstanceState the saved state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTextView = findViewById(R.id.locationTextView);
        speedTextView = findViewById(R.id.speedTextView);
        fontSizeSeekBar = findViewById(R.id.fontSizeSeekBar);
        timerTextView = findViewById(R.id.timerTextView);
        resetButton = findViewById(R.id.resetButton);
        unitToggleButton = findViewById(R.id.unitToggleButton);
        pauseButton = findViewById(R.id.pauseButton);
        helpButton = findViewById(R.id.helpButton);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Start the timer.
        startTimer();

        // Set up buttons using modular methods.
        setupResetButton();
        setupUnitToggleButton();
        setupPauseButton();
        setupHelpButton();

        setupDevSwitch();

        // Create a LocationListener to receive location updates.
        locationListener = new LocationListener() {
            /**
             * Called when the location has changed.
             * <p>
             * Retrieves the current latitude, longitude, and speed, logs these values,
             * and updates the UI using concatenated strings. The speed is displayed in the unit selected
             * by the user (mph or m/s).
             * </p>
             *
             * @param location the updated location.
             */
            @SuppressLint({"SetTextI18n", "DefaultLocale"})
            @Override
            public void onLocationChanged(@NonNull Location location) {
                double latitude, longitude, speed;
                String speedUnit;

                if (isDevMode) {
                    // Fake location set to BU Beach
                    latitude = 42.3505;
                    longitude = -71.1076;
                    speed = useMph ? (4.4704 * MPS_TO_MPH) : 4.4704;
                } else {
                    // Use real location data
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    speed = useMph ? (location.getSpeed() * MPS_TO_MPH) : location.getSpeed();
                }

                speedUnit = useMph ? "mph" : "m/s";

                // Log location details for debugging.
                System.out.println("Latitude: " + latitude);
                System.out.println("Longitude: " + longitude);
                System.out.println("Speed: " + speed);
                System.out.println("Speed Unit: " + speedUnit);

                // Update the TextView using concatenation.
                locationTextView.setText(
                        "Latitude: " + String.format("%.8f", latitude) + "°" +
                        "\nLongitude: " + String.format("%.8f", longitude) + "°");

                // Update the speed TextView.
                speedTextView.setText("Speed: " + String.format("%.3f", speed) + " " + speedUnit);

                // Set the text color based on the speed.
                int colorResId = getSpeedColor(speed, useMph);
                speedTextView.setTextColor(ContextCompat.getColor(MainActivity.this, colorResId));
            }

            /**
             * Called when the provider status changes.
             *
             * @param provider the name of the location provider.
             * @param status   the new status of the provider.
             * @param extras   additional provider-specific information.
             */
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            /**
             * Called when the location provider is enabled.
             *
             * @param provider the name of the enabled location provider.
             */
            @Override
            public void onProviderEnabled(@NonNull String provider) { }

            /**
             * Called when the location provider is disabled.
             *
             * @param provider the name of the disabled location provider.
             */
            @Override
            public void onProviderDisabled(@NonNull String provider) { }
        };

        // Check if the location permission is granted.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if it is not granted.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission granted; start receiving location updates.
            startLocationUpdates();
        }

        setupFontSizeSeekBar();
    }
    /**
     * Sets up the SeekBar to adjust the font size of the TextView.
     * Initializes the SeekBar progress and updates the TextView size based on user interaction.
     */
    private void setupFontSizeSeekBar() {
        // Set the initial font size for the TextView based on the SeekBar progress
        fontSizeSeekBar.setProgress(DEFAULT_SEEKBAR_PROGRESS); // Default size (in sp)

        // Set an OnSeekBarChangeListener to update font size based on SeekBar progress
        fontSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            /**
             * Called when the SeekBar's progress has been changed.
             * Updates the font size of the speed text based on the SeekBar's progress.
             *
             * @param seekBar The SeekBar whose progress has been changed.
             * @param progress The current progress of the SeekBar.
             * @param fromUser True if the progress change was initiated by the user.
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Calculate the text size based on the SeekBar progress
                float textSize = (float) (progress + 10); // adding 10 gives a smooth transition
                speedTextView.setTextSize(textSize); // Apply the new font size to the TextView
            }

            /**
             * Called when the user starts touching the SeekBar.
             *
             * @param seekBar The SeekBar that was touched.
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No action needed when starting to track the SeekBar
            }

            /**
             * Called when the user stops touching the SeekBar.
             *
             * @param seekBar The SeekBar that was touched.
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No action needed when stopping the tracking of the SeekBar
            }
        });
    }


    /**
     * Sets up the Dev Mode toggle switch.
     * This method finds the switch from the layout and sets an OnCheckedChangeListener
     * that toggles the value of the isDevMode boolean and displays a Toast message
     * to indicate the current state of Dev Mode.
     */
    private void setupDevSwitch() {
        // Find the switch by ID
        SwitchCompat devModeSwitch = findViewById(R.id.devModeSwitch);

        // Set a listener for when the switch is toggled
        devModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Toggle the boolean value based on the switch state
            isDevMode = isChecked;

            // Display a Toast message indicating the current mode
            String mode = isDevMode ? "Dev Mode Enabled" : "Dev Mode Disabled";
            Toast.makeText(MainActivity.this, mode, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Sets up the reset button with an OnClickListener.
     * <p>
     * When clicked, the button resets the timer to zero, updates the timer display,
     * resets the SeekBar to its default progress, restores the font size of the speed display,
     * and shows a Toast message indicating the reset action.
     * </p>
     */
    private void setupResetButton() {
        resetButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                // Stop any pending timer updates
                handler.removeCallbacks(updateTimeRunnable);

                // Reset start time to the current time
                startTime = System.currentTimeMillis();

                // Update the timer TextView to show 0 seconds elapsed
                timerTextView.setText("Elapsed Time: 0 sec");

                // Clear the paused state so that it resumes correctly from 0
                isPaused = false;
                pauseButton.setText("Pause");

                // Restart the timer updates
                handler.post(updateTimeRunnable);

                // Reset SeekBar progress and font size
                fontSizeSeekBar.setProgress(DEFAULT_SEEKBAR_PROGRESS);
                speedTextView.setTextSize(DEFAULT_SEEKBAR_PROGRESS);

                // Show a Toast message indicating reset action
                Toast.makeText(MainActivity.this, "Reset", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * Sets up the unit toggle button with an OnClickListener.
     * <p>
     * When clicked, the button toggles the speed display unit between mph and m/s,
     * updates the button text accordingly, and shows a Toast message.
     * </p>
     */
    private void setupUnitToggleButton() {
        unitToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useMph = !useMph;
                unitToggleButton.setText(useMph ? "Switch to m/s" : "Switch to mph");
                Toast.makeText(MainActivity.this, "Speed unit changed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sets up the pause button with an OnClickListener.
     * <p>
     * When clicked, the button toggles between pausing and resuming the timer and location updates.
     * If the app is not paused, it pauses the updates and changes the button text to "Resume" along with a Toast.
     * If already paused, it resumes the updates, changes the button text back to "Pause", and shows a Toast.
     * </p>
     */
    private void setupPauseButton() {
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (!isPaused) {
                    // Pause timer and location updates.
                    pausedTime = System.currentTimeMillis() - startTime;
                    handler.removeCallbacks(updateTimeRunnable);
                    if (locationManager != null) {
                        locationManager.removeUpdates(locationListener);
                    }
                    isPaused = true;
                    pauseButton.setText("Resume");
                    Toast.makeText(MainActivity.this, "Timer paused", Toast.LENGTH_SHORT).show();
                } else {
                    // Resume timer and location updates.
                    startTime = System.currentTimeMillis() - pausedTime;
                    handler.post(updateTimeRunnable);
                    startLocationUpdates();
                    isPaused = false;
                    pauseButton.setText("Pause");
                    Toast.makeText(MainActivity.this, "Timer resumed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Sets up the help button with an OnClickListener.
     * <p>
     * When clicked, the button shows a Toast message with help information.
     * </p>
     */
    private void setupHelpButton() {
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Run Tracker Help")
                        .setMessage("Welcome to Run Tracker!\n\n"
                                + "• This app tracks your location, speed, and elapsed time during your run.\n"
                                + "• Use the reset button to start the timer over.\n"
                                + "• Toggle between mph and m/s with the unit button.\n"
                                + "• Pause or resume tracking with the pause button.\n\n"
                                + "Enjoy your run and stay safe!")
                        // Optionally, you can set an icon if available (e.g., builder.setIcon(R.drawable.ic_help))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.show();
            }
        });
    }

    /**
     * Returns the appropriate color resource based on the speed and unit used.
     *
     * @param speed  the current speed.
     * @param useMph true if the speed is in mph; false if the speed is in m/s.
     * @return the color resource id corresponding to the speed range.
     */
    private int getSpeedColor(double speed, boolean useMph) {
        if (useMph) {
            if (speed < 33) {
                return android.R.color.holo_green_dark;
            } else if (speed < 66) {
                return android.R.color.holo_orange_dark;
            } else {
                return android.R.color.holo_red_dark;
            }
        } else {
            if (speed < 14.7523) {
                return android.R.color.holo_green_dark;
            } else if (speed < 29.5046) {
                return android.R.color.holo_orange_dark;
            } else {
                return android.R.color.holo_red_dark;
            }
        }
    }

    /**
     * Starts the timer by recording the start time and scheduling timer updates.
     * <p>
     * Ensures that the timer is not already running before starting.
     * </p>
     */
    private void startTimer() {
        if (!isTimerRunning) {
            startTime = System.currentTimeMillis();
            handler.post(updateTimeRunnable);
            isTimerRunning = true;
        }
    }

    /**
     * Callback for the result from requesting permissions.
     * <p>
     * If the location permission is granted, location updates are started.
     * Otherwise, the UI is updated to indicate that permission was denied.
     * </p>
     *
     * @param requestCode  the request code passed in via requestPermissions().
     * @param permissions  the requested permissions.
     * @param grantResults the grant results for the corresponding permissions.
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                locationTextView.setText("Location permission denied.");
            }
        }
    }

    /**
     * Starts receiving location updates from the GPS provider.
     * <p>
     * Requests updates every 1 second or every 1 meter of movement.
     * </p>
     */
    private void startLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when the activity is paused.
     * <p>
     * Removes location updates and stops timer updates to conserve battery.
     * </p>
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
        handler.removeCallbacks(updateTimeRunnable);
        Toast.makeText(this, "App is leaving the foreground", Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when the activity is resumed.
     * <p>
     * Restarts the timer and location updates if the app isn't in a paused state.
     * </p>
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (!isPaused) {
            handler.post(updateTimeRunnable);
            startLocationUpdates();
        }
        Toast.makeText(this, "App is back in the foreground", Toast.LENGTH_SHORT).show();
    }
}
