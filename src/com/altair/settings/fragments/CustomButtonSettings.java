/*
 * Copyright (C) 2019-2024 Altair ROM Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.altair.settings.fragments;

import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_2BUTTON;
import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_3BUTTON_OVERLAY;
import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_GESTURAL_OVERLAY;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.altair.settings.fragments.button.ButtonBacklightBrightness;
import com.altair.settings.utils.DeviceUtils;
import com.altair.settings.utils.TelephonyUtils;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import static org.lineageos.internal.util.DeviceKeysConstants.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lineageos.hardware.LineageHardwareManager;
import lineageos.providers.LineageSettings;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class CustomButtonSettings extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "CustomButtonSettings";

    private static final String KEY_BUTTON_BACKLIGHT = "button_backlight";
    private static final String KEY_BACK_WAKE_SCREEN = "back_wake_screen";
    private static final String KEY_CAMERA_LAUNCH = "camera_launch";
    private static final String KEY_CAMERA_SLEEP_ON_RELEASE = "camera_sleep_on_release";
    private static final String KEY_CAMERA_WAKE_SCREEN = "camera_wake_screen";
    private static final String KEY_BACK_LONG_PRESS = "hardware_keys_back_long_press";
    private static final String KEY_BACK_DOUBLE_TAP = "hardware_keys_back_double_tap";
    private static final String KEY_HOME_LONG_PRESS = "hardware_keys_home_long_press";
    private static final String KEY_HOME_DOUBLE_TAP = "hardware_keys_home_double_tap";
    private static final String KEY_HOME_WAKE_SCREEN = "home_wake_screen";
    private static final String KEY_MENU_PRESS = "hardware_keys_menu_press";
    private static final String KEY_MENU_LONG_PRESS = "hardware_keys_menu_long_press";
    private static final String KEY_MENU_DOUBLE_TAP = "hardware_keys_menu_double_tap";
    private static final String KEY_MENU_WAKE_SCREEN = "menu_wake_screen";
    private static final String KEY_ASSIST_PRESS = "hardware_keys_assist_press";
    private static final String KEY_ASSIST_LONG_PRESS = "hardware_keys_assist_long_press";
    private static final String KEY_ASSIST_DOUBLE_TAP = "hardware_keys_assist_double_tap";
    private static final String KEY_ASSIST_WAKE_SCREEN = "assist_wake_screen";
    private static final String KEY_APP_SWITCH_PRESS = "hardware_keys_app_switch_press";
    private static final String KEY_APP_SWITCH_LONG_PRESS = "hardware_keys_app_switch_long_press";
    private static final String KEY_APP_SWITCH_DOUBLE_TAP = "hardware_keys_app_switch_double_tap";
    private static final String KEY_APP_SWITCH_WAKE_SCREEN = "app_switch_wake_screen";
    private static final String KEY_VOLUME_KEY_CURSOR_CONTROL = "volume_key_cursor_control";
    private static final String KEY_SWAP_VOLUME_BUTTONS = "swap_volume_buttons";
    private static final String KEY_VOLUME_WAKE_SCREEN = "volume_wake_screen";
    private static final String KEY_VOLUME_ANSWER_CALL = "volume_answer_call";
    private static final String KEY_POWER_END_CALL = "power_end_call";
    private static final String KEY_HOME_ANSWER_CALL = "home_answer_call";
    private static final String KEY_VOLUME_MUSIC_CONTROLS = "volbtn_music_controls";
    private static final String KEY_TORCH_LONG_PRESS_POWER_GESTURE =
            "torch_long_press_power_gesture";
    private static final String KEY_TORCH_LONG_PRESS_POWER_TIMEOUT =
            "torch_long_press_power_timeout";
    private static final String KEY_CLICK_PARTIAL_SCREENSHOT =
            "click_partial_screenshot";
    private static final String KEY_SWAP_CAPACITIVE_KEYS = "swap_capacitive_keys";

    private static final String CATEGORY_POWER = "power_key";
    private static final String CATEGORY_HOME = "home_key";
    private static final String CATEGORY_BACK = "back_key";
    private static final String CATEGORY_MENU = "menu_key";
    private static final String CATEGORY_ASSIST = "assist_key";
    private static final String CATEGORY_APPSWITCH = "app_switch_key";
    private static final String CATEGORY_CAMERA = "camera_key";
    private static final String CATEGORY_VOLUME = "volume_keys";
    private static final String CATEGORY_BACKLIGHT = "key_backlight";
    private static final String CATEGORY_EXTRAS = "extras_category";

    private ContentResolver mResolver;

    private ListPreference mBackLongPressAction;
    private ListPreference mBackDoubleTapAction;
    private ListPreference mHomeLongPressAction;
    private ListPreference mHomeDoubleTapAction;
    private ListPreference mMenuPressAction;
    private ListPreference mMenuLongPressAction;
    private ListPreference mMenuDoubleTapAction;
    private ListPreference mAssistPressAction;
    private ListPreference mAssistLongPressAction;
    private ListPreference mAssistDoubleTapAction;
    private ListPreference mAppSwitchPressAction;
    private ListPreference mAppSwitchLongPressAction;
    private ListPreference mAppSwitchDoubleTapAction;
    private SwitchPreferenceCompat mCameraWakeScreen;
    private SwitchPreferenceCompat mCameraSleepOnRelease;
    private ListPreference mVolumeKeyCursorControl;
    private SwitchPreferenceCompat mSwapVolumeButtons;
    private SwitchPreferenceCompat mPowerEndCall;
    private SwitchPreferenceCompat mHomeAnswerCall;
    private ListPreference mTorchLongPressPowerTimeout;
    private SwitchPreferenceCompat mSwapCapacitiveKeys;

    private LineageHardwareManager mHardware;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.menu_button_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResolver = getActivity().getContentResolver();

        final Resources res = getResources();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mHardware = LineageHardwareManager.getInstance(getActivity());

        final boolean hasPowerKey = DeviceUtils.hasPowerKey();
        final boolean hasHomeKey = DeviceUtils.hasHomeKey(getActivity());
        final boolean hasBackKey = DeviceUtils.hasBackKey(getActivity());
        final boolean hasMenuKey = DeviceUtils.hasMenuKey(getActivity());
        final boolean hasAssistKey = DeviceUtils.hasAssistKey(getActivity());
        final boolean hasAppSwitchKey = DeviceUtils.hasAppSwitchKey(getActivity());
        final boolean hasCameraKey = DeviceUtils.hasCameraKey(getActivity());
        final boolean hasVolumeKeys = DeviceUtils.hasVolumeKeys(getActivity());

        final boolean showHomeWake = DeviceUtils.canWakeUsingHomeKey(getActivity());
        final boolean showBackWake = DeviceUtils.canWakeUsingBackKey(getActivity());
        final boolean showMenuWake = DeviceUtils.canWakeUsingMenuKey(getActivity());
        final boolean showAssistWake = DeviceUtils.canWakeUsingAssistKey(getActivity());
        final boolean showAppSwitchWake = DeviceUtils.canWakeUsingAppSwitchKey(getActivity());
        final boolean showCameraWake = DeviceUtils.canWakeUsingCameraKey(getActivity());
        final boolean showVolumeWake = DeviceUtils.canWakeUsingVolumeKeys(getActivity());

        final PreferenceCategory powerCategory = prefScreen.findPreference(CATEGORY_POWER);
        final PreferenceCategory homeCategory = prefScreen.findPreference(CATEGORY_HOME);
        final PreferenceCategory backCategory = prefScreen.findPreference(CATEGORY_BACK);
        final PreferenceCategory menuCategory = prefScreen.findPreference(CATEGORY_MENU);
        final PreferenceCategory assistCategory = prefScreen.findPreference(CATEGORY_ASSIST);
        final PreferenceCategory appSwitchCategory = prefScreen.findPreference(CATEGORY_APPSWITCH);
        final PreferenceCategory volumeCategory = prefScreen.findPreference(CATEGORY_VOLUME);
        final PreferenceCategory cameraCategory = prefScreen.findPreference(CATEGORY_CAMERA);
        final PreferenceCategory extrasCategory = prefScreen.findPreference(CATEGORY_EXTRAS);

        // Power button ends calls.
        mPowerEndCall = findPreference(KEY_POWER_END_CALL);

        // Long press power while display is off to activate torchlight
        SwitchPreferenceCompat torchLongPressPowerGesture =
                findPreference(KEY_TORCH_LONG_PRESS_POWER_GESTURE);
        final int torchLongPressPowerTimeout = LineageSettings.System.getInt(mResolver,
                LineageSettings.System.TORCH_LONG_PRESS_POWER_TIMEOUT, 0);
        mTorchLongPressPowerTimeout = initList(KEY_TORCH_LONG_PRESS_POWER_TIMEOUT,
                torchLongPressPowerTimeout);

        // Home button answers calls.
        mHomeAnswerCall = findPreference(KEY_HOME_ANSWER_CALL);

        Action defaultBackLongPressAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_longPressOnBackBehavior));
        Action defaultBackDoubleTapAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_doubleTapOnBackBehavior));
        Action defaultHomeLongPressAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_longPressOnHomeBehavior));
        Action defaultHomeDoubleTapAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_doubleTapOnHomeBehavior));
        Action defaultAppSwitchLongPressAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_longPressOnAppSwitchBehavior));
        Action defaultAppSwitchDoubleTapAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_doubleTapOnAppSwitchBehavior));
        Action backLongPressAction = Action.fromSettings(mResolver,
                LineageSettings.System.KEY_BACK_LONG_PRESS_ACTION,
                defaultBackLongPressAction);
        Action backDoubleTapAction = Action.fromSettings(mResolver,
                LineageSettings.System.KEY_BACK_DOUBLE_TAP_ACTION,
                defaultBackDoubleTapAction);
        Action homeLongPressAction = Action.fromSettings(mResolver,
                LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION,
                defaultHomeLongPressAction);
        Action homeDoubleTapAction = Action.fromSettings(mResolver,
                LineageSettings.System.KEY_HOME_DOUBLE_TAP_ACTION,
                defaultHomeDoubleTapAction);
        Action appSwitchLongPressAction = Action.fromSettings(mResolver,
                LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION,
                defaultAppSwitchLongPressAction);
        Action appSwitchDoubleTapAction = Action.fromSettings(mResolver,
                LineageSettings.System.KEY_APP_SWITCH_DOUBLE_TAP_ACTION,
                defaultAppSwitchDoubleTapAction);
        Action edgeLongSwipeAction = Action.fromSettings(mResolver,
                LineageSettings.System.KEY_EDGE_LONG_SWIPE_ACTION,
                Action.NOTHING);

        final boolean navkeysEnabled = LineageSettings.System.getIntForUser(
                mResolver, LineageSettings.System.FORCE_SHOW_NAVBAR, 0,
                UserHandle.USER_CURRENT) != 0;
        updateDisableNavkeysCategories(navkeysEnabled, /* force */ true);

        if (hasPowerKey) {
            if (!TelephonyUtils.isVoiceCapable(getActivity())) {
                powerCategory.removePreference(mPowerEndCall);
                mPowerEndCall = null;
            }
            if (!DeviceUtils.deviceSupportsFlashLight(getActivity())) {
                powerCategory.removePreference(torchLongPressPowerGesture);
                powerCategory.removePreference(mTorchLongPressPowerTimeout);
            }
        }
        if (!hasPowerKey || powerCategory.getPreferenceCount() == 0) {
            prefScreen.removePreference(powerCategory);
        }

        if (hasHomeKey) {
            if (!showHomeWake) {
                homeCategory.removePreference(findPreference(KEY_HOME_WAKE_SCREEN));
            }

            if (!TelephonyUtils.isVoiceCapable(getActivity())) {
                homeCategory.removePreference(mHomeAnswerCall);
                mHomeAnswerCall = null;
            }

            mHomeLongPressAction = initList(KEY_HOME_LONG_PRESS, homeLongPressAction);
            mHomeDoubleTapAction = initList(KEY_HOME_DOUBLE_TAP, homeDoubleTapAction);
            if (navkeysEnabled) {
                mHomeLongPressAction.setEnabled(false);
                mHomeDoubleTapAction.setEnabled(false);
            }
        }
        if (!hasHomeKey || homeCategory.getPreferenceCount() == 0) {
            prefScreen.removePreference(homeCategory);
        }

        if (hasBackKey) {
            if (!showBackWake) {
                backCategory.removePreference(findPreference(KEY_BACK_WAKE_SCREEN));
            }

            mBackLongPressAction = initList(KEY_BACK_LONG_PRESS, backLongPressAction);
            mBackDoubleTapAction = initList(KEY_BACK_DOUBLE_TAP, backDoubleTapAction);
            if (navkeysEnabled) {
                mBackLongPressAction.setEnabled(false);
                mBackDoubleTapAction.setEnabled(false);
            }
        }
        if (!hasBackKey || backCategory.getPreferenceCount() == 0) {
            prefScreen.removePreference(backCategory);
        }

        if (hasMenuKey) {
            if (!showMenuWake) {
                menuCategory.removePreference(findPreference(KEY_MENU_WAKE_SCREEN));
            }

            Action pressAction = Action.fromSettings(mResolver,
                    LineageSettings.System.KEY_MENU_ACTION, Action.MENU);
            mMenuPressAction = initList(KEY_MENU_PRESS, pressAction);

            Action longPressAction = Action.fromSettings(mResolver,
                        LineageSettings.System.KEY_MENU_LONG_PRESS_ACTION,
                        hasAssistKey ? Action.NOTHING : Action.APP_SWITCH);
            mMenuLongPressAction = initList(KEY_MENU_LONG_PRESS, longPressAction);

            Action doubleTapAction = Action.fromSettings(mResolver,
                        LineageSettings.System.KEY_MENU_DOUBLE_TAP_ACTION,
                        Action.NOTHING);
            mMenuDoubleTapAction = initList(KEY_MENU_DOUBLE_TAP, doubleTapAction);
        }
        if (!hasMenuKey || menuCategory.getPreferenceCount() == 0) {
            prefScreen.removePreference(menuCategory);
        }

        if (hasAssistKey) {
            if (!showAssistWake) {
                assistCategory.removePreference(findPreference(KEY_ASSIST_WAKE_SCREEN));
            }

            Action pressAction = Action.fromSettings(mResolver,
                    LineageSettings.System.KEY_ASSIST_ACTION, Action.SEARCH);
            mAssistPressAction = initList(KEY_ASSIST_PRESS, pressAction);

            Action longPressAction = Action.fromSettings(mResolver,
                    LineageSettings.System.KEY_ASSIST_LONG_PRESS_ACTION, Action.VOICE_SEARCH);
            mAssistLongPressAction = initList(KEY_ASSIST_LONG_PRESS, longPressAction);

            Action doubleTapAction = Action.fromSettings(mResolver,
                    LineageSettings.System.KEY_ASSIST_DOUBLE_TAP_ACTION, Action.NOTHING);
            mAssistLongPressAction = initList(KEY_ASSIST_DOUBLE_TAP, doubleTapAction);
        }
        if (!hasAssistKey || assistCategory.getPreferenceCount() == 0) {
            prefScreen.removePreference(assistCategory);
        }

        if (hasAppSwitchKey) {
            if (!showAppSwitchWake) {
                appSwitchCategory.removePreference(findPreference(KEY_APP_SWITCH_WAKE_SCREEN));
            }

            Action pressAction = Action.fromSettings(mResolver,
                    LineageSettings.System.KEY_APP_SWITCH_ACTION, Action.APP_SWITCH);
            mAppSwitchPressAction = initList(KEY_APP_SWITCH_PRESS, pressAction);

            mAppSwitchLongPressAction = initList(KEY_APP_SWITCH_LONG_PRESS,
                    appSwitchLongPressAction);
            mAppSwitchDoubleTapAction = initList(KEY_APP_SWITCH_DOUBLE_TAP,
                    appSwitchDoubleTapAction);
        }
        if (!hasAppSwitchKey || appSwitchCategory.getPreferenceCount() == 0) {
            prefScreen.removePreference(appSwitchCategory);
        }

        if (hasCameraKey) {
            mCameraWakeScreen = findPreference(KEY_CAMERA_WAKE_SCREEN);
            mCameraSleepOnRelease = findPreference(KEY_CAMERA_SLEEP_ON_RELEASE);

            if (!showCameraWake) {
                prefScreen.removePreference(mCameraWakeScreen);
            }
            // Only show 'Camera sleep on release' if the device has a focus key
            if (res.getBoolean(
                    org.lineageos.platform.internal.R.bool.config_singleStageCameraKey)) {
                prefScreen.removePreference(mCameraSleepOnRelease);
            }
        }
        if (!hasCameraKey || cameraCategory.getPreferenceCount() == 0) {
            prefScreen.removePreference(cameraCategory);
        }

        if (hasVolumeKeys) {
            if (!showVolumeWake) {
                volumeCategory.removePreference(findPreference(KEY_VOLUME_WAKE_SCREEN));
            }

            if (!TelephonyUtils.isVoiceCapable(getActivity())) {
                volumeCategory.removePreference(findPreference(KEY_VOLUME_ANSWER_CALL));
            }

            int cursorControlAction = Settings.System.getInt(mResolver,
                    Settings.System.VOLUME_KEY_CURSOR_CONTROL, 0);
            mVolumeKeyCursorControl = initList(KEY_VOLUME_KEY_CURSOR_CONTROL,
                    cursorControlAction);

            int swapVolumeKeys = LineageSettings.System.getInt(mResolver,
                    LineageSettings.System.SWAP_VOLUME_KEYS_ON_ROTATION, 0);
            mSwapVolumeButtons = prefScreen.findPreference(KEY_SWAP_VOLUME_BUTTONS);
            if (mSwapVolumeButtons != null) {
                mSwapVolumeButtons.setChecked(swapVolumeKeys > 0);
            }
        } else {
            extrasCategory.removePreference(findPreference(KEY_CLICK_PARTIAL_SCREENSHOT));
        }
        if (!hasVolumeKeys || volumeCategory.getPreferenceCount() == 0) {
            prefScreen.removePreference(volumeCategory);
        }

        final ButtonBacklightBrightness backlight = findPreference(KEY_BUTTON_BACKLIGHT);
        if (!DeviceUtils.hasButtonBacklightSupport(getActivity())
                && !DeviceUtils.hasKeyboardBacklightSupport(getActivity())) {
            prefScreen.removePreference(backlight);
        }

        if (mCameraWakeScreen != null) {
            if (mCameraSleepOnRelease != null && !res.getBoolean(
                    org.lineageos.platform.internal.R.bool.config_singleStageCameraKey)) {
                mCameraSleepOnRelease.setDependency(KEY_CAMERA_WAKE_SCREEN);
            }
        }

        SwitchPreferenceCompat volumeWakeScreen = findPreference(KEY_VOLUME_WAKE_SCREEN);
        SwitchPreferenceCompat volumeMusicControls = findPreference(KEY_VOLUME_MUSIC_CONTROLS);

        if (volumeWakeScreen != null) {
            if (volumeMusicControls != null) {
                volumeMusicControls.setDependency(KEY_VOLUME_WAKE_SCREEN);
                volumeWakeScreen.setDisableDependentsState(true);
            }
        }

        mSwapCapacitiveKeys = findPreference(KEY_SWAP_CAPACITIVE_KEYS);
        if (mSwapCapacitiveKeys != null && !isKeySwapperSupported(getActivity())) {
            prefScreen.removePreference(mSwapCapacitiveKeys);
        } else {
            mSwapCapacitiveKeys.setOnPreferenceChangeListener(this);
        }

        List<Integer> unsupportedValues = new ArrayList<>();
        List<String> entries = new ArrayList<>(
                Arrays.asList(res.getStringArray(R.array.navbar_key_action_entries)));
        List<String> values = new ArrayList<>(
                Arrays.asList(res.getStringArray(R.array.navbar_key_action_values)));

        // hide split screen option unconditionally - it doesn't work at the moment
        // once someone gets it working again: hide it only for low-ram devices
        // (check ActivityManager.isLowRamDeviceStatic())
        unsupportedValues.add(Action.SPLIT_SCREEN.ordinal());

        for (int unsupportedValue: unsupportedValues) {
            entries.remove(unsupportedValue);
            values.remove(unsupportedValue);
        }

        for (int unsupportedValue: unsupportedValues) {
            entries.remove(unsupportedValue);
            values.remove(unsupportedValue);
        }

        String[] actionEntries = entries.toArray(new String[0]);
        String[] actionValues = values.toArray(new String[0]);

        if (hasBackKey) {
            mBackLongPressAction.setEntries(actionEntries);
            mBackLongPressAction.setEntryValues(actionValues);

            mBackDoubleTapAction.setEntries(actionEntries);
            mBackDoubleTapAction.setEntryValues(actionValues);
        }

        if (hasHomeKey) {
            mHomeLongPressAction.setEntries(actionEntries);
            mHomeLongPressAction.setEntryValues(actionValues);

            mHomeDoubleTapAction.setEntries(actionEntries);
            mHomeDoubleTapAction.setEntryValues(actionValues);
        }

        if (hasMenuKey) {
            mMenuPressAction.setEntries(actionEntries);
            mMenuPressAction.setEntryValues(actionValues);

            mMenuLongPressAction.setEntries(actionEntries);
            mMenuLongPressAction.setEntryValues(actionValues);

            mMenuDoubleTapAction.setEntries(actionEntries);
            mMenuDoubleTapAction.setEntryValues(actionValues);
        }

        if (hasAssistKey) {
            mAssistPressAction.setEntries(actionEntries);
            mAssistPressAction.setEntryValues(actionValues);

            mAssistLongPressAction.setEntries(actionEntries);
            mAssistLongPressAction.setEntryValues(actionValues);

            mAssistDoubleTapAction.setEntries(actionEntries);
            mAssistDoubleTapAction.setEntryValues(actionValues);
        }

        if (hasAppSwitchKey) {
            mAppSwitchPressAction.setEntries(actionEntries);
            mAppSwitchPressAction.setEntryValues(actionValues);

            mAppSwitchLongPressAction.setEntries(actionEntries);
            mAppSwitchLongPressAction.setEntryValues(actionValues);

            mAppSwitchDoubleTapAction.setEntries(actionEntries);
            mAppSwitchDoubleTapAction.setEntryValues(actionValues);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALTAIR_SETTINGS;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Power button ends calls.
        if (mPowerEndCall != null) {
            final int incallPowerBehavior = Settings.Secure.getInt(mResolver,
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR,
                    Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_DEFAULT);
            final boolean powerButtonEndsCall =
                    (incallPowerBehavior == Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP);
            mPowerEndCall.setChecked(powerButtonEndsCall);
        }

        // Home button answers calls.
        if (mHomeAnswerCall != null) {
            final int incallHomeBehavior = LineageSettings.Secure.getInt(mResolver,
                    LineageSettings.Secure.RING_HOME_BUTTON_BEHAVIOR,
                    LineageSettings.Secure.RING_HOME_BUTTON_BEHAVIOR_DEFAULT);
            final boolean homeButtonAnswersCall =
                (incallHomeBehavior == LineageSettings.Secure.RING_HOME_BUTTON_BEHAVIOR_ANSWER);
            mHomeAnswerCall.setChecked(homeButtonAnswersCall);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private ListPreference initList(String key, Action value) {
        return initList(key, value.ordinal());
    }

    private ListPreference initList(String key, int value) {
        ListPreference list = getPreferenceScreen().findPreference(key);
        if (list == null) return null;
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
        list.setOnPreferenceChangeListener(this);
        return list;
    }

    private void handleListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        LineageSettings.System.putInt(mResolver, setting, Integer.valueOf(value));
    }

    private void handleSystemListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putInt(mResolver, setting, Integer.valueOf(value));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mBackLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_BACK_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mBackDoubleTapAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_BACK_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mHomeLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mHomeDoubleTapAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_HOME_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mMenuPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_MENU_ACTION);
            return true;
        } else if (preference == mMenuLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_MENU_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mMenuDoubleTapAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_MENU_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mAssistPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_ASSIST_ACTION);
            return true;
        } else if (preference == mAssistLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_ASSIST_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mAssistDoubleTapAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_ASSIST_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mAppSwitchPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_APP_SWITCH_ACTION);
            return true;
        } else if (preference == mAppSwitchLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mAppSwitchDoubleTapAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_APP_SWITCH_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mVolumeKeyCursorControl) {
            handleListChange((ListPreference) preference, newValue,
                    Settings.System.VOLUME_KEY_CURSOR_CONTROL);
            return true;
        } else if (preference == mTorchLongPressPowerTimeout) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.TORCH_LONG_PRESS_POWER_TIMEOUT);
            return true;
        } else if (preference == mSwapCapacitiveKeys) {
            mHardware.set(LineageHardwareManager.FEATURE_KEY_SWAP, (Boolean) newValue);
            return true;
        }
        return false;
    }

    private void enablePreference(Preference pref, boolean enabled) {
        if (pref != null) {
            pref.setEnabled(enabled);
        }
    }

    private void updateDisableNavkeysCategories(boolean navbarEnabled, boolean force) {
        final PreferenceScreen prefScreen = getPreferenceScreen();

        /* Disable hw-key options if they're disabled */
        final PreferenceCategory homeCategory =
                prefScreen.findPreference(CATEGORY_HOME);
        final PreferenceCategory backCategory =
                prefScreen.findPreference(CATEGORY_BACK);
        final PreferenceCategory menuCategory =
                prefScreen.findPreference(CATEGORY_MENU);
        final PreferenceCategory assistCategory =
                prefScreen.findPreference(CATEGORY_ASSIST);
        final PreferenceCategory appSwitchCategory =
                prefScreen.findPreference(CATEGORY_APPSWITCH);
        final ButtonBacklightBrightness backlight =
                (ButtonBacklightBrightness) prefScreen.findPreference(KEY_BUTTON_BACKLIGHT);

        /* Toggle backlight control depending on navbar state, force it to
           off if enabling */
        if (backlight != null) {
            backlight.setEnabled(!navbarEnabled);
            backlight.updateSummary();
        }

        /* Toggle hardkey control availability depending on navbar state */
        if (backCategory != null) {
            enablePreference(mBackLongPressAction, !navbarEnabled);
            enablePreference(mBackDoubleTapAction, !navbarEnabled);
        }
        if (homeCategory != null) {
            enablePreference(mHomeAnswerCall, !navbarEnabled);
            enablePreference(mHomeLongPressAction, !navbarEnabled);
            enablePreference(mHomeDoubleTapAction, !navbarEnabled);
        }
        if (menuCategory != null) {
            enablePreference(mMenuPressAction, !navbarEnabled);
            enablePreference(mMenuLongPressAction, !navbarEnabled);
            enablePreference(mMenuDoubleTapAction, !navbarEnabled);
        }
        if (assistCategory != null) {
            enablePreference(mAssistPressAction, !navbarEnabled);
            enablePreference(mAssistLongPressAction, !navbarEnabled);
            enablePreference(mAssistDoubleTapAction, !navbarEnabled);
        }
        if (appSwitchCategory != null) {
            enablePreference(mAppSwitchPressAction, !navbarEnabled);
            enablePreference(mAppSwitchLongPressAction, !navbarEnabled);
            enablePreference(mAppSwitchDoubleTapAction, !navbarEnabled);
        }
    }

    private static boolean hasNavigationBar() {
        boolean hasNavigationBar = false;
        try {
            IWindowManager windowManager = WindowManagerGlobal.getWindowManagerService();
            hasNavigationBar = windowManager.hasNavigationBar(Display.DEFAULT_DISPLAY);
        } catch (RemoteException e) {
            Log.e(TAG, "Error getting navigation bar status");
        }
        return hasNavigationBar;
    }

    private static boolean isKeySwapperSupported(Context context) {
        final LineageHardwareManager hardware = LineageHardwareManager.getInstance(context);
        return hardware.isSupported(LineageHardwareManager.FEATURE_KEY_SWAP);
    }

    public static void restoreKeySwapper(Context context) {
        if (!isKeySwapperSupported(context)) {
            return;
        }

        final SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        final LineageHardwareManager hardware = LineageHardwareManager.getInstance(context);
        hardware.set(LineageHardwareManager.FEATURE_KEY_SWAP,
                preferences.getBoolean(KEY_SWAP_CAPACITIVE_KEYS, false));
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mSwapVolumeButtons) {
            int value;

            if (mSwapVolumeButtons.isChecked()) {
                /* The native inputflinger service uses the same logic of:
                 *   1 - the volume rocker is on one the sides, relative to the natural
                 *       orientation of the display (true for all phones and most tablets)
                 *   2 - the volume rocker is on the top or bottom, relative to the
                 *       natural orientation of the display (true for some tablets)
                 */
                value = getResources().getInteger(
                        R.integer.config_volumeRockerVsDisplayOrientation);
            } else {
                /* Disable the re-orient functionality */
                value = 0;
            }
            LineageSettings.System.putInt(mResolver,
                    LineageSettings.System.SWAP_VOLUME_KEYS_ON_ROTATION, value);
        } else if (preference == mPowerEndCall) {
            handleTogglePowerButtonEndsCallPreferenceClick();
            return true;
        } else if (preference == mHomeAnswerCall) {
            handleToggleHomeButtonAnswersCallPreferenceClick();
            return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    private void handleTogglePowerButtonEndsCallPreferenceClick() {
        Settings.Secure.putInt(mResolver,
                Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR, (mPowerEndCall.isChecked()
                        ? Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_HANGUP
                        : Settings.Secure.INCALL_POWER_BUTTON_BEHAVIOR_SCREEN_OFF));
    }

    private void handleToggleHomeButtonAnswersCallPreferenceClick() {
        LineageSettings.Secure.putInt(mResolver,
                LineageSettings.Secure.RING_HOME_BUTTON_BEHAVIOR, (mHomeAnswerCall.isChecked()
                        ? LineageSettings.Secure.RING_HOME_BUTTON_BEHAVIOR_ANSWER
                        : LineageSettings.Secure.RING_HOME_BUTTON_BEHAVIOR_DO_NOTHING));
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.menu_button_settings;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    if (!TelephonyUtils.isVoiceCapable(context)) {
                        keys.add(KEY_POWER_END_CALL);
                        keys.add(KEY_HOME_ANSWER_CALL);
                        keys.add(KEY_VOLUME_ANSWER_CALL);
                    }

                    if (!DeviceUtils.hasBackKey(context)) {
                        keys.add(CATEGORY_BACK);
                        keys.add(KEY_BACK_LONG_PRESS);
                        keys.add(KEY_BACK_DOUBLE_TAP);
                        keys.add(KEY_BACK_WAKE_SCREEN);
                    } else if (!DeviceUtils.canWakeUsingHomeKey(context)) {
                        keys.add(KEY_BACK_WAKE_SCREEN);
                    }

                    if (!DeviceUtils.hasHomeKey(context)) {
                        keys.add(CATEGORY_HOME);
                        keys.add(KEY_HOME_LONG_PRESS);
                        keys.add(KEY_HOME_DOUBLE_TAP);
                        keys.add(KEY_HOME_ANSWER_CALL);
                        keys.add(KEY_HOME_WAKE_SCREEN);
                    } else if (!DeviceUtils.canWakeUsingHomeKey(context)) {
                        keys.add(KEY_HOME_WAKE_SCREEN);
                    }

                    if (!DeviceUtils.hasMenuKey(context)) {
                        keys.add(CATEGORY_MENU);
                        keys.add(KEY_MENU_PRESS);
                        keys.add(KEY_MENU_LONG_PRESS);
                        keys.add(KEY_MENU_WAKE_SCREEN);
                        keys.add(KEY_MENU_DOUBLE_TAP);
                    } else if (!DeviceUtils.canWakeUsingMenuKey(context)) {
                        keys.add(KEY_MENU_WAKE_SCREEN);
                    }

                    if (!DeviceUtils.hasAssistKey(context)) {
                        keys.add(CATEGORY_ASSIST);
                        keys.add(KEY_ASSIST_PRESS);
                        keys.add(KEY_ASSIST_LONG_PRESS);
                        keys.add(KEY_ASSIST_DOUBLE_TAP);
                        keys.add(KEY_ASSIST_WAKE_SCREEN);
                    } else if (!DeviceUtils.canWakeUsingAssistKey(context)) {
                        keys.add(KEY_ASSIST_WAKE_SCREEN);
                    }

                    if (!DeviceUtils.hasAppSwitchKey(context)) {
                        keys.add(CATEGORY_APPSWITCH);
                        keys.add(KEY_APP_SWITCH_PRESS);
                        keys.add(KEY_APP_SWITCH_LONG_PRESS);
                        keys.add(KEY_APP_SWITCH_DOUBLE_TAP);
                        keys.add(KEY_APP_SWITCH_WAKE_SCREEN);
                    } else if (!DeviceUtils.canWakeUsingAppSwitchKey(context)) {
                        keys.add(KEY_APP_SWITCH_WAKE_SCREEN);
                    }

                    if (!DeviceUtils.hasCameraKey(context)) {
                        keys.add(CATEGORY_CAMERA);
                        keys.add(KEY_CAMERA_LAUNCH);
                        keys.add(KEY_CAMERA_SLEEP_ON_RELEASE);
                        keys.add(KEY_CAMERA_WAKE_SCREEN);
                    } else if (!DeviceUtils.canWakeUsingCameraKey(context)) {
                        keys.add(KEY_CAMERA_WAKE_SCREEN);
                    }

                    if (!DeviceUtils.hasVolumeKeys(context)) {
                        keys.add(CATEGORY_VOLUME);
                        keys.add(KEY_SWAP_VOLUME_BUTTONS);
                        keys.add(KEY_VOLUME_ANSWER_CALL);
                        keys.add(KEY_VOLUME_KEY_CURSOR_CONTROL);
                        keys.add(KEY_VOLUME_MUSIC_CONTROLS);
                        keys.add(KEY_VOLUME_WAKE_SCREEN);
                        keys.add(KEY_CLICK_PARTIAL_SCREENSHOT);
                    } else if (!DeviceUtils.canWakeUsingVolumeKeys(context)) {
                        keys.add(KEY_VOLUME_WAKE_SCREEN);
                    }

                    if (!DeviceUtils.deviceSupportsFlashLight(context)) {
                        keys.add(KEY_TORCH_LONG_PRESS_POWER_GESTURE);
                        keys.add(KEY_TORCH_LONG_PRESS_POWER_TIMEOUT);
                    }

                    if (!isKeySwapperSupported(context)) {
                        keys.add(KEY_SWAP_CAPACITIVE_KEYS);
                    }

                    if (!DeviceUtils.hasButtonBacklightSupport(context)
                            && !DeviceUtils.hasKeyboardBacklightSupport(context)) {
                        keys.add(KEY_BUTTON_BACKLIGHT);
                    }

                    return keys;
                }
            };
}
