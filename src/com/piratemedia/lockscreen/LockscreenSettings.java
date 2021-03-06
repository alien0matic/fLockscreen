package com.piratemedia.lockscreen;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

public class LockscreenSettings extends PreferenceActivity {

    static final String KEY_MUSIC_PLAYER = "music_player_select";

    static final String KEY_FULLSCREEN = "fullscreen";

    static final String KEY_SHOW_ART = "albumart";

    static final String KEY_SHOW_CUSTOM_BG = "custom_bg";

    static final String KEY_PICK_BG = "bg_picker";

    static final String KEY_SENSOR_ROTATE = "sensor_rotate";

    static final String KEY_LANDSCAPE = "landscape";

    static final String KEY_HOME_APP_PACKAGE = "user_home_app_package";

    static final String KEY_HOME_APP_ACTIVITY = "user_home_app_activity";

    static final String SMS_COUNT_KEY = "sms_count";

    static final String MISSED_CALL_KEY = "missed_calls";

    static final String GMAIL_COUNT_KEY = "gmail_count";

    static final String MUTE_TOGGLE_KEY = "mute_toggle";

    static final String USB_MS_KEY = "usb_ms";

    static final String WIFI_MODE_KEY = "wifi_mode";

    static final String COUNT_KEY = "countDown";

    static final String LEFT_ACTION_KEY = "leftAction";

    static final String RIGHT_ACTION_KEY = "rightAction";

    static final String BLUETOOTH_MODE_KEY = "bluetooth_mode";

    static final String MUTE_MODE_KEY = "muteMode";

    static final String TWEET_MODE_KEY = "tweets_mode";

    static final String MENTION_MODE_KEY = "mention_mode";

    static final String DIRECT_MODE_KEY = "direct_mode";

    static final String GMAIL_VIEW_KEY = "gmail_view";

    static final String GMAIL_ACCOUNT_KEY = "gmail_labels";

    static final String GMAIL_MERGE_KEY = "gmail_merge";

    static final String SERVICE_FOREGROUND = "service_foreground";

    static final String SMALL_TEXT_KEY = "small_text_notif";

    static final String ENABLE_KEY = "enable_disable";

    private static final String TEMP_PHOTO_FILE = "tempBG_Image.png";

    public static final String BG_PHOTO_FILE = "bg_pic.png";

    public static final String THEME_DEFAULT = "fLockScreen";

    static final String THEME_KEY = "themePackageName";

    static final String THEME_BACKGROUND_SLIDE_KEY = "theme_background_slide";

    static final String THEME_ART_SLIDE_KEY = "theme_allow_art_slide";

    static final String THEME_MUSIC_CONTROL_KEY = "theme_music_control_pad";

    static final String THEME_TEXT_NOTIF_KEY = "theme_text_notif_pad";

    static final String THEME_NETWORK_TEXT_KEY = "theme_network_text_color";

    static final String THEME_NETWORK_SHADOW_KEY = "theme_network_text_shadow_color";

    static final String THEME_CLOCK_TEXT_KEY = "theme_clock_text_color";

    static final String THEME_CLOCK_SHADOW_KEY = "theme_clock_text_shadow_color";

    static final String THEME_MUSIC_TEXT_KEY = "theme_music_text_color";

    static final String THEME_MUSIC_SHADOW_KEY = "theme_music_text_shadow_color";

    static final String THEME_NOTIFICATION_TEXT_KEY = "theme_notification_text_color";

    static final String THEME_NOTIFICATION_SHADOW_KEY = "theme_notification_text_shadow_color";

    static final String THEME_SHOW_ICONS_KEY = "theme_show_icons";

    private Intent ServiceStrt;

    private Intent serviceIntent;

    private Preference gmailAccounts;

    private Preference gmailMerge;

    private Preference landscape;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lockscreensettings);

        PreferenceScreen screen = this.getPreferenceScreen();
        Preference pick = (Preference) screen.findPreference(KEY_PICK_BG);
        landscape = (Preference) screen.findPreference(KEY_LANDSCAPE);
        Preference service_foreground = (Preference) screen.findPreference(SERVICE_FOREGROUND);
        Preference laction = (Preference) screen.findPreference(LEFT_ACTION_KEY);
        Preference raction = (Preference) screen.findPreference(RIGHT_ACTION_KEY);
        Preference smallNotif = (Preference) screen.findPreference(SMALL_TEXT_KEY);
        gmailAccounts = (Preference) screen.findPreference(GMAIL_ACCOUNT_KEY);
        gmailMerge = (Preference) screen.findPreference(GMAIL_MERGE_KEY);
        Preference Enable = (Preference) screen.findPreference(ENABLE_KEY);
        Preference sensor = (Preference) screen.findPreference(KEY_SENSOR_ROTATE);

        //make sure service is running
        ServiceStrt = new Intent("com.piratemedia.lockscreen.startservice");
        serviceIntent = new Intent(this, updateService.class);
        if (utils.getCheckBoxPref(getBaseContext(), ENABLE_KEY, true)) {
            serviceIntent.setAction(ServiceStrt.getAction());
            serviceIntent.putExtras(ServiceStrt);
            getBaseContext().startService(serviceIntent);
        }

        DefaultMusicApp();

        //TODO: need to disable this for default theme
        if (utils.getCheckBoxPref(getBaseContext(), THEME_SHOW_ICONS_KEY, false)) {
            smallNotif.setEnabled(true);
        } else {
            smallNotif.setEnabled(false);
            utils.setCheckBoxPref(getBaseContext(), SMALL_TEXT_KEY, false);
        }

        if (utils.getCheckBoxPref(getBaseContext(), SMALL_TEXT_KEY, false)) {
            gmailAccounts.setEnabled(false);
            gmailMerge.setEnabled(false);
        } else {
            gmailAccounts.setEnabled(true);
            gmailMerge.setEnabled(true);
        }

        sensor.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String NewVal = newValue.toString();
                boolean Enabled = Boolean.parseBoolean(NewVal);
                if (Enabled) {
                    landscape.setEnabled(false);
                } else {
                    landscape.setEnabled(true);
                }
                return true;
            }
        });

        Enable.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String NewVal = newValue.toString();
                boolean Enabled = Boolean.parseBoolean(NewVal);
                if (!Enabled) {
                    notifyChange(updateService.STOP_SERVICE);
                } else {
                    serviceIntent.setAction(ServiceStrt.getAction());
                    serviceIntent.putExtras(ServiceStrt);
                    getBaseContext().startService(serviceIntent);
                }
                return true;
            }
        });

        laction.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                actionLeft(newValue);
                return true;
            }
        });

        raction.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                actionRight(newValue);
                return true;
            }
        });

        pick.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                pickImage();
                return true;
            }
        });

        landscape.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                String warning = getString(R.string.landscape_image_warning);
                Toast.makeText(getBaseContext(), warning, 1700).show();
                final String FileName = "bg_pic";
                deleteFile(FileName + ".jpg");
                return true;
            }
        });

        service_foreground.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference arg0) {
                StartStopForground();
                return true;
            }
        });

        smallNotif.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference arg0) {
                //change some vals
                if (utils.getCheckBoxPref(getBaseContext(), SMALL_TEXT_KEY, false)) {
                    utils.setCheckBoxPref(getBaseContext(), GMAIL_ACCOUNT_KEY, false);
                    utils.setCheckBoxPref(getBaseContext(), GMAIL_MERGE_KEY, true);
                    startActivity(new Intent(getBaseContext(),
                                             LockscreenSettings.class));
                    finish();
                } else {
                    gmailAccounts.setEnabled(true);
                    gmailMerge.setEnabled(true);
                }
                return true;
            }
        });

        //ADW: Home app preference
        Preference homeApp = findPreference("user_home_app");
        homeApp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(LockscreenSettings.this, HomeChooserActivity.class);
                intent.putExtra("loadOnClick", false);
                startActivity(intent);
                return true;
            }
        });
        //ADW: theme settings
        SharedPreferences sp = getPreferenceManager().getSharedPreferences();
        final String themePackage = sp.getString(THEME_KEY, LockscreenSettings.THEME_DEFAULT);
        ListPreference themeLp = (ListPreference) findPreference(THEME_KEY);
        themeLp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                PreviewPreference themePreview = (PreviewPreference) findPreference("themePreview");
                themePreview.setTheme(newValue.toString());
                return false;
            }
        });

        Intent intent = new Intent("com.piratemedia.lockscreen.THEMES");
        intent.addCategory("android.intent.category.DEFAULT");
        PackageManager pm = getPackageManager();
        List<ResolveInfo> themes = pm.queryIntentActivities(intent, 0);
        String[] entries = new String[themes.size() + 1];
        String[] values = new String[themes.size() + 1];
        entries[0] = LockscreenSettings.THEME_DEFAULT;
        values[0] = LockscreenSettings.THEME_DEFAULT;
        for (int i = 0; i < themes.size(); i++) {
            String appPackageName = (themes.get(i)).activityInfo.packageName.toString();
            String themeName = (themes.get(i)).loadLabel(pm).toString();
            entries[i + 1] = themeName;
            values[i + 1] = appPackageName;
        }
        themeLp.setEntries(entries);
        themeLp.setEntryValues(values);
        PreviewPreference themePreview = (PreviewPreference) findPreference("themePreview");
        themePreview.setTheme(themePackage);
    }

    private void actionLeft(Object newVal) {
        int LeftInt;
        int RightInt;
        String LeftString = newVal.toString();
        String RightString = utils.getStringPref(getBaseContext(), LockscreenSettings.RIGHT_ACTION_KEY, "2");
        LeftInt = Integer.parseInt(LeftString);
        RightInt = Integer.parseInt(RightString);
        if (LeftInt != 1 && RightInt != 1) {
            Toast.makeText(getBaseContext(), "one of the actions must be unlock, setting right to unlock",
                           Toast.LENGTH_SHORT).show();
            utils.setStringPref(getBaseContext(), RIGHT_ACTION_KEY, "1");
            startActivity(new Intent(getBaseContext(),
                                     LockscreenSettings.class));
            finish();
        }
    }

    private void actionRight(Object newVal) {
        int RightInt;
        int LeftInt;
        String RightString = newVal.toString();
        String LeftString = utils.getStringPref(getBaseContext(), LockscreenSettings.LEFT_ACTION_KEY, "1");
        LeftInt = Integer.parseInt(LeftString);
        RightInt = Integer.parseInt(RightString);
        if (LeftInt != 1 && RightInt != 1) {
            Toast.makeText(getBaseContext(), "one of the actions must be unlock, setting left to unlock",
                           Toast.LENGTH_SHORT).show();
            utils.setStringPref(getBaseContext(), LEFT_ACTION_KEY, "1");
            startActivity(new Intent(getBaseContext(),
                                     LockscreenSettings.class));
            finish();
        }
    }

    private void pickImage() {
        int width;
        int height;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        if (utils.getCheckBoxPref(this, LockscreenSettings.KEY_LANDSCAPE, false)) {
            //for some reson these dont work unless the are halved, ie 800x480 is too big
            //TODO: we need to fix this :)
            int orientation = getRequestedOrientation();
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                width = display.getHeight();
                height = display.getWidth();
            } else {
                width = display.getWidth();
                height = display.getHeight();
            }
        } else {
            int orientation = getRequestedOrientation();
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                width = display.getHeight();
                height = display.getWidth();
            } else {
                width = display.getWidth();
                height = display.getHeight();
            }
        }
        intent.putExtra("crop", "true");
        intent.putExtra("outputX", width);
        intent.putExtra("outputY", height);
        intent.putExtra("scale", true);
        intent.putExtra("aspectX", width);
        intent.putExtra("aspectY", height);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
        intent.putExtra("noFaceDetection", true);

        startActivityForResult(intent, 4);
    }

    private Uri getTempUri() {
        return Uri.fromFile(getTempFile());
    }

    private File getTempFile() {
        if (isSDCARDMounted()) {

            File f = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE);
            //try {
            //f.createNewFile();
            //} catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            //Toast.makeText(this, "Something Fucked Up", Toast.LENGTH_LONG).show();
            //}
            return f;
        } else {
            return null;
        }
    }

    private boolean isSDCARDMounted() {
        String status = Environment.getExternalStorageState();

        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    public static void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {

            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 4) {
            if (resultCode == RESULT_OK) {
                try {

                    File src = getTempFile();
                    File dst = new File(getFilesDir(), BG_PHOTO_FILE);
                    copyFile(src, dst);
                    boolean deleted = src.delete();

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
            if (resultCode == RESULT_CANCELED) {
                String no_image = getString(R.string.custombg_none_selected);
                Toast.makeText(getBaseContext(), no_image, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // check if android music exists, will use this to set default music player

    private void StartStopForground() {
        notifyChange(updateService.START_STOP_FORGROUND);
    }

    private void notifyChange(String what) {
        Intent i = new Intent(what);
        sendBroadcast(i);
    }

    private void DefaultMusicApp() {

        PreferenceScreen screen = this.getPreferenceScreen();
        Preference MusicSel = (Preference) screen.findPreference(KEY_MUSIC_PLAYER);

        final ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        final List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        String StockMusic = "com.android.music";
        String HTCMusic = "com.htc.music";

        for (int i = 0; i < services.size(); i++) {
            if (StockMusic.equals(services.get(i).service.getPackageName())) {
                MusicSel.setDefaultValue("1");
            } else if (HTCMusic.equals(services.get(i).service.getPackageName())) {
                MusicSel.setDefaultValue("2");
            } else {
                MusicSel.setDefaultValue("3");
            }
        }
    }

    public void getThemes(View v) {
        //TODO:warn theme devs to use "fLockTheme" as keyword.
        Uri marketUri = Uri.parse("market://search?q=fLockTheme");
        Intent marketIntent = new Intent(Intent.ACTION_VIEW).setData(marketUri);
        try {
            startActivity(marketIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e("ADW", "Launcher does not have the permission to launch " + marketIntent +
                         ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                         "or use the exported attribute for this activity.", e);
        }
        finish();
    }

    /**
     * ADW: Apply and store the theme stuff
     * @param v
     */
    public void applyTheme(View v) {
        PreviewPreference themePreview = (PreviewPreference) findPreference("themePreview");
        String packageName = themePreview.getValue().toString();
        //this time we really save the themepackagename
        SharedPreferences sp = getPreferenceManager().getSharedPreferences();
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("themePackageName", packageName);
        //and update the preferences from the theme
        //TODO:ADW maybe this should be optional for the user
        if (!packageName.equals(LockscreenSettings.THEME_DEFAULT)) {
            Resources themeResources = null;
            try {
                themeResources = getPackageManager().getResourcesForApplication(packageName.toString());
            } catch (NameNotFoundException e) {
                //e.printStackTrace();
            }
            if (themeResources != null) {
                int tmpId = themeResources.getIdentifier("network_text_color", "color", packageName.toString());
                if (tmpId != 0) {
                    int network_text_color = themeResources.getColor(tmpId);
                    editor.putInt("theme_network_text_color", network_text_color);
                }
                tmpId = themeResources.getIdentifier("network_text_shadow_color", "color", packageName.toString());
                if (tmpId != 0) {
                    int network_text_shadow_color = themeResources.getColor(tmpId);
                    editor.putInt("theme_network_text_shadow_color", network_text_shadow_color);
                }
                tmpId = themeResources.getIdentifier("clock_text_color", "color", packageName.toString());
                if (tmpId != 0) {
                    int clock_text_color = themeResources.getColor(tmpId);
                    editor.putInt("theme_clock_text_color", clock_text_color);
                }
                tmpId = themeResources.getIdentifier("clock_text_shadow_color", "color", packageName.toString());
                if (tmpId != 0) {
                    int clock_text_shadow_color = themeResources.getColor(tmpId);
                    editor.putInt("theme_clock_text_shadow_color", clock_text_shadow_color);
                }
                tmpId = themeResources.getIdentifier("music_text_color", "color", packageName.toString());
                if (tmpId != 0) {
                    int music_text_color = themeResources.getColor(tmpId);
                    editor.putInt("theme_music_text_color", music_text_color);
                }
                tmpId = themeResources.getIdentifier("music_text_shadow_color", "color", packageName.toString());
                if (tmpId != 0) {
                    int music_text_shadow_color = themeResources.getColor(tmpId);
                    editor.putInt("theme_music_text_shadow_color", music_text_shadow_color);
                }
                tmpId = themeResources.getIdentifier("notification_text_color", "color", packageName.toString());
                if (tmpId != 0) {
                    int notification_text_color = themeResources.getColor(tmpId);
                    editor.putInt("theme_notification_text_color", notification_text_color);
                }
                tmpId = themeResources.getIdentifier("notification_text_shadow_color", "color", packageName.toString());
                if (tmpId != 0) {
                    int notification_text_shadow_color = themeResources.getColor(tmpId);
                    editor.putInt("theme_notification_text_shadow_color", notification_text_shadow_color);
                }
                tmpId = themeResources.getIdentifier("music_control_pad", "dimen", packageName.toString());
                if (tmpId != 0) {
                    int music_control_pad = themeResources.getDimensionPixelSize(tmpId);
                    editor.putInt("theme_music_control_pad", music_control_pad);
                }
                tmpId = themeResources.getIdentifier("text_notif_pad", "dimen", packageName.toString());
                if (tmpId != 0) {
                    int text_notif_pad = themeResources.getDimensionPixelSize(tmpId);
                    editor.putInt("theme_text_notif_pad", text_notif_pad);
                }
                tmpId = themeResources.getIdentifier("background_slide", "bool", packageName.toString());
                if (tmpId != 0) {
                    boolean background_slide = themeResources.getBoolean(tmpId);
                    editor.putBoolean("theme_background_slide", background_slide);
                }
                tmpId = themeResources.getIdentifier("allow_art_slide", "bool", packageName.toString());
                if (tmpId != 0) {
                    boolean allow_art_slide = themeResources.getBoolean(tmpId);
                    editor.putBoolean("theme_allow_art_slide", allow_art_slide);
                }
                tmpId = themeResources.getIdentifier("show_icons", "bool", packageName.toString());
                if (tmpId != 0) {
                    boolean show_icons = themeResources.getBoolean(tmpId);
                    editor.putBoolean("theme_show_icons", show_icons);
                }
                tmpId = themeResources.getIdentifier("simple_text_notif", "bool", packageName.toString());
                if (tmpId != 0) {
                    boolean simple_text_notif = themeResources.getBoolean(tmpId);
                    editor.putBoolean("small_text_notif", simple_text_notif);
                }
            }
        } else {
            editor.remove("small_text_notif");
            editor.remove("theme_show_icons");
            editor.remove("theme_allow_art_slide");
            editor.remove("theme_background_slide");
            editor.remove("theme_text_notif_pad");
            editor.remove("theme_music_control_pad");
            editor.remove("theme_notification_text_shadow_color");
            editor.remove("theme_notification_text_color");
            editor.remove("theme_music_text_shadow_color");
            editor.remove("theme_music_text_color");
            editor.remove("theme_clock_text_shadow_color");
            editor.remove("theme_clock_text_color");
            editor.remove("theme_network_text_shadow_color");
            editor.remove("theme_network_text_color");

        }
        editor.commit();
        if (utils.getCheckBoxPref(getBaseContext(), SMALL_TEXT_KEY, false)) {
            utils.setCheckBoxPref(getBaseContext(), GMAIL_ACCOUNT_KEY, false);
            utils.setCheckBoxPref(getBaseContext(), GMAIL_MERGE_KEY, true);
        }
        startActivity(new Intent(getBaseContext(),
                                 LockscreenSettings.class));
        finish();
    }

}