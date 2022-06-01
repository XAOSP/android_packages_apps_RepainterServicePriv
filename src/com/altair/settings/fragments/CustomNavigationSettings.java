/*
 * Copyright (C) 2019-2022 Altair ROM Project
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

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.om.IOverlayManager;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
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
import androidx.preference.SwitchPreference;

import com.altair.settings.utils.DeviceUtils;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import static com.android.systemui.shared.recents.utilities.Utilities.isTablet;

import java.util.Arrays;
import java.util.List;

import lineageos.hardware.LineageHardwareManager;
import lineageos.preference.LineageSystemSettingListPreference;
import lineageos.providers.LineageSettings;

import static org.lineageos.internal.util.DeviceKeysConstants.*;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class CustomNavigationSettings extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "CustomNavigationSettings";

    private static final String KEY_DISABLE_NAV_KEYS = "disable_nav_keys";
    private static final String KEY_ENABLE_TASKBAR = "enable_taskbar";
    private static final String KEY_NAVIGATION_ARROW_KEYS = "navigation_bar_menu_arrow_keys";
    private static final String KEY_NAV_BAR_INVERSE = "sysui_nav_bar_inverse";
    private static final String KEY_NAVIGATION_BACK_LONG_PRESS = "navigation_back_long_press";
    private static final String KEY_NAVIGATION_HOME_LONG_PRESS = "navigation_home_long_press";
    private static final String KEY_NAVIGATION_HOME_DOUBLE_TAP = "navigation_home_double_tap";
    private static final String KEY_NAVIGATION_APP_SWITCH_LONG_PRESS =
            "navigation_app_switch_long_press";
    private static final String KEY_EDGE_LONG_SWIPE = "navigation_bar_edge_long_swipe";

    private static final String CATEGORY_NAVBAR_OPTIONS = "navigation_bar_options_category";
    private static final String CATEGORY_NAVBAR_ACTIONS = "navigation_bar_actions_category";

    private Context mContext;
    private Handler mHandler;
    private ContentResolver mResolver;

    private SwitchPreference mDisableNavigationKeys;
    private SwitchPreference mEnableTaskbar;
    private SwitchPreference mNavigationArrowKeys;
    private SwitchPreference mNavBarInverse;
    private ListPreference mNavigationBackLongPressAction;
    private ListPreference mNavigationHomeLongPressAction;
    private ListPreference mNavigationHomeDoubleTapAction;
    private ListPreference mNavigationAppSwitchLongPressAction;
    private ListPreference mEdgeLongSwipeAction;

    private PreferenceCategory mNavigationOptionsPreferencesCat;
    private PreferenceCategory mNavigationActionsPreferencesCat;

    private LineageHardwareManager mHardware;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.menu_navigation_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity().getApplicationContext();
        mHandler = new Handler();
        mResolver = getActivity().getContentResolver();

        final Resources res = getResources();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mHardware = LineageHardwareManager.getInstance(getActivity());

        // Force Navigation bar related options
        mDisableNavigationKeys = findPreference(KEY_DISABLE_NAV_KEYS);

        mNavigationOptionsPreferencesCat = findPreference(CATEGORY_NAVBAR_OPTIONS);
        mNavigationActionsPreferencesCat = findPreference(CATEGORY_NAVBAR_ACTIONS);

        Action defaultBackLongPressAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_longPressOnBackBehavior));
        Action defaultHomeLongPressAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_longPressOnHomeBehavior));
        Action defaultHomeDoubleTapAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_doubleTapOnHomeBehavior));
        Action defaultAppSwitchLongPressAction = Action.fromIntSafe(res.getInteger(
                org.lineageos.platform.internal.R.integer.config_longPressOnAppSwitchBehavior));
        Action backLongPressAction = Action.fromSettings(mResolver,
                LineageSettings.System.KEY_BACK_LONG_PRESS_ACTION,
                defaultBackLongPressAction);
        Action homeLongPressAction = Action.fromSettings(mResolver,
                LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION,
                defaultHomeLongPressAction);
        Action homeDoubleTapAction = Action.fromSettings(mResolver,
                LineageSettings.System.KEY_HOME_DOUBLE_TAP_ACTION,
                defaultHomeDoubleTapAction);
        Action appSwitchLongPressAction = Action.fromSettings(mResolver,
                LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION,
                defaultAppSwitchLongPressAction);
        Action edgeLongSwipeAction = Action.fromSettings(mResolver,
                LineageSettings.System.KEY_EDGE_LONG_SWIPE_ACTION,
                Action.NOTHING);

        // Navigation bar arrow keys while typing
        mNavigationArrowKeys = findPreference(KEY_NAVIGATION_ARROW_KEYS);

        // Navigation bar back long press
        mNavigationBackLongPressAction = initList(KEY_NAVIGATION_BACK_LONG_PRESS,
                backLongPressAction);

        // Navigation bar home long press
        mNavigationHomeLongPressAction = initList(KEY_NAVIGATION_HOME_LONG_PRESS,
                homeLongPressAction);

        // Navigation bar home double tap
        mNavigationHomeDoubleTapAction = initList(KEY_NAVIGATION_HOME_DOUBLE_TAP,
                homeDoubleTapAction);

        // Navigation bar app switch long press
        mNavigationAppSwitchLongPressAction = initList(KEY_NAVIGATION_APP_SWITCH_LONG_PRESS,
                appSwitchLongPressAction);

        // Edge long swipe gesture
        mEdgeLongSwipeAction = initList(KEY_EDGE_LONG_SWIPE, edgeLongSwipeAction);

        // Hardware key disabler
        if (isKeyDisablerSupported(getActivity())) {
            // Remove keys that can be provided by the navbar
            updateDisableNavkeysOption();
            enableNavigationPreferencesCats(mDisableNavigationKeys.isChecked());
            mDisableNavigationKeys.setDisableDependentsState(true);
        } else {
            prefScreen.removePreference(mDisableNavigationKeys);
        }
        updateDisableNavkeysCategories(mDisableNavigationKeys.isChecked(), /* force */ true);

        // Only show the navigation bar category on devices that have a navigation bar
        // or support disabling the hardware keys
        if (!hasNavigationBar() && !isKeyDisablerSupported(getActivity())) {
            enableNavigationPreferencesCats(false);
        }

        mNavBarInverse = findPreference(KEY_NAV_BAR_INVERSE);

        mEnableTaskbar = findPreference(KEY_ENABLE_TASKBAR);
        if (mEnableTaskbar != null) {
            if (!isTablet(getContext()) || !hasNavigationBar()) {
                mNavigationOptionsPreferencesCat.removePreference(mEnableTaskbar);
                mNavigationActionsPreferencesCat.removePreference(mEnableTaskbar);
            } else {
                mEnableTaskbar.setOnPreferenceChangeListener(this);
                mEnableTaskbar.setChecked(LineageSettings.System.getInt(getContentResolver(),
                        LineageSettings.System.ENABLE_TASKBAR,
                        isTablet(getContext()) ? 1 : 0) == 1);
                toggleTaskBarDependencies(mEnableTaskbar.isChecked());
            }
        }

        // Override key actions on Go devices in order to hide any unsupported features
        if (ActivityManager.isLowRamDeviceStatic()) {
            String[] actionEntriesGo = res.getStringArray(R.array.hardware_keys_action_entries_go);
            String[] actionValuesGo = res.getStringArray(R.array.hardware_keys_action_values_go);

            mNavigationBackLongPressAction.setEntries(actionEntriesGo);
            mNavigationBackLongPressAction.setEntryValues(actionValuesGo);

            mNavigationHomeLongPressAction.setEntries(actionEntriesGo);
            mNavigationHomeLongPressAction.setEntryValues(actionValuesGo);

            mNavigationHomeDoubleTapAction.setEntries(actionEntriesGo);
            mNavigationHomeDoubleTapAction.setEntryValues(actionValuesGo);

            mNavigationAppSwitchLongPressAction.setEntries(actionEntriesGo);
            mNavigationAppSwitchLongPressAction.setEntryValues(actionValuesGo);

            mEdgeLongSwipeAction.setEntries(actionEntriesGo);
            mEdgeLongSwipeAction.setEntryValues(actionValuesGo);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALTAIR;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public void onResume() {
        super.onResume();
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
        LineageSettings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }

    private void handleSystemListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNavigationBackLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_BACK_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mNavigationHomeLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_HOME_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mNavigationHomeDoubleTapAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_HOME_DOUBLE_TAP_ACTION);
            return true;
        } else if (preference == mNavigationAppSwitchLongPressAction) {
            handleListChange((ListPreference) preference, newValue,
                    LineageSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mEdgeLongSwipeAction) {
            handleListChange(mEdgeLongSwipeAction, newValue,
                    LineageSettings.System.KEY_EDGE_LONG_SWIPE_ACTION);
            return true;
        } else if (preference == mEnableTaskbar) {
            toggleTaskBarDependencies((Boolean) newValue);
            if ((Boolean) newValue && is2ButtonNavigationEnabled(getContext())) {
                // Let's switch to gestural mode if user previously had 2 buttons enabled.
                setButtonNavigationMode(NAV_BAR_MODE_GESTURAL_OVERLAY);
            }
            LineageSettings.System.putInt(getContentResolver(),
                    LineageSettings.System.ENABLE_TASKBAR, ((Boolean) newValue) ? 1 : 0);
            return true;
        }
        return false;
    }

    private static boolean is2ButtonNavigationEnabled(Context context) {
        return NAV_BAR_MODE_2BUTTON == context.getResources().getInteger(
                com.android.internal.R.integer.config_navBarInteractionMode);
    }

    private static void setButtonNavigationMode(String overlayPackage) {
        IOverlayManager overlayManager = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));
        try {
            overlayManager.setEnabledExclusiveInCategory(overlayPackage, UserHandle.USER_CURRENT);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private void toggleTaskBarDependencies(boolean enabled) {
        if (mNavigationArrowKeys != null) {
            mNavigationArrowKeys.setEnabled(!enabled);
        }

        if (mNavBarInverse != null) {
            mNavBarInverse.setEnabled(!enabled);
        }

        if (mNavigationBackLongPressAction != null) {
            mNavigationBackLongPressAction.setEnabled(!enabled);
        }

        if (mNavigationHomeLongPressAction != null) {
            mNavigationHomeLongPressAction.setEnabled(!enabled);
        }

        if (mNavigationHomeDoubleTapAction != null) {
            mNavigationHomeDoubleTapAction.setEnabled(!enabled);
        }

        if (mNavigationAppSwitchLongPressAction != null) {
            mNavigationAppSwitchLongPressAction.setEnabled(!enabled);
        }
    }

    private static void writeDisableNavkeysOption(Context context, boolean enabled) {
        LineageSettings.System.putIntForUser(context.getContentResolver(),
                LineageSettings.System.FORCE_SHOW_NAVBAR, enabled ? 1 : 0, UserHandle.USER_CURRENT);
    }

    private void updateDisableNavkeysOption() {
        boolean enabled = LineageSettings.System.getIntForUser(getActivity().getContentResolver(),
                LineageSettings.System.FORCE_SHOW_NAVBAR, 0, UserHandle.USER_CURRENT) != 0;

        mDisableNavigationKeys.setChecked(enabled);
    }

    private void updateDisableNavkeysCategories(boolean navbarEnabled, boolean force) {

        /* Toggle navbar control availability depending on navbar state */
        if (mNavigationActionsPreferencesCat != null) {
            if (force || navbarEnabled) {
                if (DeviceUtils.isEdgeToEdgeEnabled(getContext())) {
                    mNavigationActionsPreferencesCat.addPreference(mEdgeLongSwipeAction);

                    mNavigationActionsPreferencesCat.removePreference(mNavigationArrowKeys);
                    mNavigationActionsPreferencesCat.removePreference(mNavigationBackLongPressAction);
                    mNavigationActionsPreferencesCat.removePreference(mNavigationHomeLongPressAction);
                    mNavigationActionsPreferencesCat.removePreference(mNavigationHomeDoubleTapAction);
                    mNavigationActionsPreferencesCat.removePreference(mNavigationAppSwitchLongPressAction);
                } else if (DeviceUtils.isSwipeUpEnabled(getContext())) {
                    mNavigationActionsPreferencesCat.addPreference(mNavigationBackLongPressAction);
                    mNavigationActionsPreferencesCat.addPreference(mNavigationHomeLongPressAction);
                    mNavigationActionsPreferencesCat.addPreference(mNavigationHomeDoubleTapAction);

                    mNavigationActionsPreferencesCat.removePreference(mNavigationAppSwitchLongPressAction);
                    mNavigationActionsPreferencesCat.removePreference(mEdgeLongSwipeAction);
                } else {
                    mNavigationActionsPreferencesCat.addPreference(mNavigationBackLongPressAction);
                    mNavigationActionsPreferencesCat.addPreference(mNavigationHomeLongPressAction);
                    mNavigationActionsPreferencesCat.addPreference(mNavigationHomeDoubleTapAction);
                    mNavigationActionsPreferencesCat.addPreference(mNavigationAppSwitchLongPressAction);

                    mNavigationActionsPreferencesCat.removePreference(mEdgeLongSwipeAction);
                }
            }
        }
    }

    private void enableNavigationPreferencesCats(boolean enable) {
        mNavigationOptionsPreferencesCat.setEnabled(enable);
        mNavigationActionsPreferencesCat.setEnabled(enable);
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

    private static boolean isKeyDisablerSupported(Context context) {
        final LineageHardwareManager hardware = LineageHardwareManager.getInstance(context);
        return hardware.isSupported(LineageHardwareManager.FEATURE_KEY_DISABLE);
    }

    public static void restoreKeyDisabler(Context context) {
        if (!isKeyDisablerSupported(context)) {
            return;
        }

        boolean enabled = LineageSettings.System.getIntForUser(context.getContentResolver(),
                LineageSettings.System.FORCE_SHOW_NAVBAR, 0, UserHandle.USER_CURRENT) != 0;

        writeDisableNavkeysOption(context, enabled);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mDisableNavigationKeys) {
            mDisableNavigationKeys.setEnabled(false);
            enableNavigationPreferencesCats(false);
            if (!mDisableNavigationKeys.isChecked()) {
                setButtonNavigationMode(NAV_BAR_MODE_3BUTTON_OVERLAY);
            }
            writeDisableNavkeysOption(getActivity(), mDisableNavigationKeys.isChecked());
            updateDisableNavkeysOption();
            updateDisableNavkeysCategories(true, false);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDisableNavigationKeys.setEnabled(true);
                    enableNavigationPreferencesCats(mDisableNavigationKeys.isChecked());
                    updateDisableNavkeysCategories(mDisableNavigationKeys.isChecked(), false);
                }
            }, 1000);
        }

        return super.onPreferenceTreeClick(preference);
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.menu_navigation_settings;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    if (!isKeyDisablerSupported(context)) {
                        keys.add(KEY_DISABLE_NAV_KEYS);
                    }

                    if (hasNavigationBar()) {
                        if (DeviceUtils.isEdgeToEdgeEnabled(context)) {
                            keys.add(KEY_NAVIGATION_ARROW_KEYS);
                            keys.add(KEY_NAVIGATION_HOME_LONG_PRESS);
                            keys.add(KEY_NAVIGATION_HOME_DOUBLE_TAP);
                            keys.add(KEY_NAVIGATION_APP_SWITCH_LONG_PRESS);
                        } else if (DeviceUtils.isSwipeUpEnabled(context)) {
                            keys.add(KEY_NAVIGATION_APP_SWITCH_LONG_PRESS);
                            keys.add(KEY_EDGE_LONG_SWIPE);
                        } else {
                            keys.add(KEY_EDGE_LONG_SWIPE);
                        }
                    }

                    return keys;
                }
            };
}
