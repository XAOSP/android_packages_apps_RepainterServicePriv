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

import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.custom.MonetUtils;
import com.android.internal.util.custom.ThemeUtils;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.darkmode.DarkModePreference;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.lineage.support.colorpicker.SecureSettingColorPickerPreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class CustomThemeSettings extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "CustomThemeSettings";

    private static final String KEY_THEME_DARK_UI_MODE = "theme_dark_ui_mode";
    private static final String KEY_THEME_COLORS_ACCENT_COLOR = "theme_colors_accent_color";
    private static final String KEY_THEME_COLORS_RESET_SETTINGS = "theme_colors_reset_settings";
    private static final String KEY_THEME_FONT = ThemeUtils.FONT_KEY;
    private static final String KEY_THEME_ICON_SHAPE = ThemeUtils.ICON_SHAPE_KEY;
    private static final String KEY_THEME_SIGNAL_ICON = ThemeUtils.SIGNAL_ICON_KEY;
    private static final String KEY_THEME_WIFI_ICON = ThemeUtils.WIFI_ICON_KEY;
    private static final String KEY_THEME_NAVBAR_STYLE = ThemeUtils.NAVBAR_KEY;

    private Context mContext;
    private Resources mResources;

    private UiModeManager mUiModeManager;
    private ThemeUtils mThemeUtils;
    private MonetUtils mMonetUtils;

    private List<String> mAccentColorValues;
    private List<String> mAccentColorNames;

    private DarkModePreference mDarkMode;

    private Preference mAccentColorPreference;
    private Preference mResetSettingsPreference;

    private Preference mFontPreference;
    private Preference mIconShapePreference;
    private Preference mSignalIconPreference;
    private Preference mWiFiIconPreference;
    private Preference mNavbarStylePreference;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.menu_theme_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity().getApplicationContext();
        mResources = getResources();

        final PreferenceScreen prefScreen = getPreferenceScreen();

        mUiModeManager = getContext().getSystemService(UiModeManager.class);
        mThemeUtils = new ThemeUtils(mContext);
        mMonetUtils = new MonetUtils(mContext);

        mAccentColorValues = Arrays.asList(mResources.getStringArray(
                R.array.theme_accent_color_values));
        mAccentColorNames = Arrays.asList(mResources.getStringArray(
                R.array.theme_accent_color_names));

        mDarkMode = findPreference(KEY_THEME_DARK_UI_MODE);
        mDarkMode.setOnPreferenceChangeListener(this);

        mAccentColorPreference = prefScreen.findPreference(KEY_THEME_COLORS_ACCENT_COLOR);
        updateAccentColorSummary();

        mResetSettingsPreference = prefScreen.findPreference(KEY_THEME_COLORS_RESET_SETTINGS);

        mFontPreference = prefScreen.findPreference(KEY_THEME_FONT);
        updateSummary(mFontPreference, "android");
        mIconShapePreference = prefScreen.findPreference(KEY_THEME_ICON_SHAPE);
        updateSummary(mIconShapePreference, "android");
        mSignalIconPreference = prefScreen.findPreference(KEY_THEME_SIGNAL_ICON);
        updateSummary(mSignalIconPreference, "android");
        mWiFiIconPreference = prefScreen.findPreference(KEY_THEME_WIFI_ICON);
        updateSummary(mWiFiIconPreference, "android");
        mNavbarStylePreference = prefScreen.findPreference(KEY_THEME_NAVBAR_STYLE);
        updateSummary(mNavbarStylePreference, "com.android.systemui");
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

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        switch (key) {
            case KEY_THEME_DARK_UI_MODE:
                mUiModeManager.setNightModeActivated((boolean) newValue);
                break;
            case KEY_THEME_COLORS_ACCENT_COLOR:
                updateAccentColorSummary();
                break;
            case KEY_THEME_FONT:
                updateSummary(mFontPreference, "android");
                break;
            case KEY_THEME_SIGNAL_ICON:
                updateSummary(mSignalIconPreference, "android");
                break;
            case KEY_THEME_WIFI_ICON:
                updateSummary(mWiFiIconPreference, "android");
                break;
            case KEY_THEME_NAVBAR_STYLE:
                updateSummary(mNavbarStylePreference, "com.android.systemui");
                break;
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mResetSettingsPreference) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.theme_colors_reset_settings_title)
                    .setMessage(R.string.theme_colors_reset_settings_message)
                    .setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {
                             mThemeUtils.setOverlayEnabled(ThemeUtils.ACCENT_KEY, "android",
                                    "android");
                             updateSummary(mAccentColorPreference, "android");
                             mMonetUtils.setAccentColor(MonetUtils.ACCENT_COLOR_DISABLED);
                             mMonetUtils.setSurfaceTintEnabled(true);
                             mMonetUtils.setAccurateShadesEnabled(true);
                        }
                    })
                    .setNegativeButton(R.string.dlg_cancel, null);
            builder.show();
        }
        return super.onPreferenceTreeClick(preference);
    }

    public void updateSummary(Preference preference, String target) {
        String currentPackageName = mThemeUtils.getOverlayInfos(preference.getKey(), target)
                .stream()
                .filter(info -> info.isEnabled())
                .map(info -> info.packageName)
                .findFirst()
                .orElse(target);

        List<String> pkgs = mThemeUtils.getOverlayPackagesForCategory(preference.getKey(), target);
        List<String> labels = mThemeUtils.getLabels(preference.getKey(), target);

        preference.setSummary(target.equals(currentPackageName) ? "Default"
                : labels.get(pkgs.indexOf(currentPackageName)));
    }

    public void updateAccentColorSummary() {
        if (mMonetUtils.isAccentColorSet()) {
            final String color = String.format("#%06X", (0xFFFFFF & mMonetUtils.getAccentColor()));
            final int index = mAccentColorValues.indexOf(color.toLowerCase());
            if (index < 0) {
                return;
            }
            mAccentColorPreference.setSummary(mAccentColorNames.get(index));
        } else {
            mAccentColorPreference.setSummary(mResources.getString(
                    R.string.theme_accent_color_wallpaper));
        }
    }

    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.menu_theme_settings;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    return keys;
                }
            };
}
