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
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.custom.MonetUtils;
import com.android.internal.util.custom.ThemeUtils;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.darkmode.DarkModePreference;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import com.lineage.support.colorpicker.SecureSettingColorPickerPreference;
import com.lineage.support.preferences.SecureSettingListPreference;

import java.util.Arrays;
import java.util.List;

@SearchIndexable(forTarget = SearchIndexable.ALL & ~SearchIndexable.ARC)
public class CustomThemeSettings extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "CustomThemeSettings";

    private static final String KEY_THEME_DARK_UI_MODE = "theme_dark_ui_mode";
    private static final String KEY_THEME_RESET_COLORS = "theme_colors_reset_colors";
    private static final String KEY_THEME_FONT = ThemeUtils.FONT_KEY;
    private static final String KEY_THEME_ACCENT_COLOR = ThemeUtils.ACCENT_KEY;
    private static final String KEY_THEME_ICON_SHAPE = ThemeUtils.ICON_SHAPE_KEY;
    private static final String KEY_THEME_SIGNAL_ICON = ThemeUtils.SIGNAL_ICON_KEY;
    private static final String KEY_THEME_WIFI_ICON = ThemeUtils.WIFI_ICON_KEY;
    private static final String KEY_THEME_NAVBAR_STYLE = ThemeUtils.NAVBAR_KEY;

    private Context mContext;
    private Handler mHandler;
    private ContentResolver mResolver;

    private UiModeManager mUiModeManager;
    private ThemeUtils mThemeUtils;
    private MonetUtils mMonetUtils;

    private DarkModePreference mDarkMode;

    private SecureSettingColorPickerPreference mMonetColorOverridePreference;
    private Preference mAccentColorPreference;
    private Preference mResetColorsPreference;

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
        mHandler = new Handler();
        mResolver = getActivity().getContentResolver();

        final Resources res = getResources();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mUiModeManager = getContext().getSystemService(UiModeManager.class);
        mThemeUtils = new ThemeUtils(mContext);
        mMonetUtils = new MonetUtils(mContext);

        mDarkMode = findPreference(KEY_THEME_DARK_UI_MODE);
        mDarkMode.setOnPreferenceChangeListener(this);

        mMonetColorOverridePreference = prefScreen.findPreference(
                MonetUtils.KEY_MONET_COLOR_OVERRIDE);
        mAccentColorPreference = prefScreen.findPreference(KEY_THEME_ACCENT_COLOR);
        updateSummary(mAccentColorPreference, "android");
        mResetColorsPreference = prefScreen.findPreference(KEY_THEME_RESET_COLORS);

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

        updatePreferences();
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
            case KEY_THEME_ACCENT_COLOR:
                updateSummary(mAccentColorPreference, "android");
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
        updatePreferences();
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mResetColorsPreference) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.theme_colors_reset_colors_title)
                    .setMessage(R.string.theme_colors_reset_colors_message)
                    .setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {
                             mThemeUtils.setOverlayEnabled(ThemeUtils.ACCENT_KEY, "android", "android");
                             updateSummary(mAccentColorPreference, "android");
                             mMonetUtils.setOverrideColor(-1);
                             mMonetUtils.resetAccentColor();
                        }
                    })
                    .setNegativeButton(R.string.dlg_cancel, null);
            builder.show();
        }
        return super.onPreferenceTreeClick(preference);
    }

    public void updateSummary(Preference preference, String target) {
        String currentPackageName = mThemeUtils.getOverlayInfos(preference.getKey(), target).stream()
                .filter(info -> info.isEnabled())
                .map(info -> info.packageName)
                .findFirst()
                .orElse(target);

        List<String> pkgs = mThemeUtils.getOverlayPackagesForCategory(preference.getKey(), target);
        List<String> labels = mThemeUtils.getLabels(preference.getKey(), target);

        preference.setSummary(target.equals(currentPackageName) ? "Default" : labels.get(pkgs.indexOf(currentPackageName)));
    }

    public void updatePreferences() {
        final int colorType = mMonetUtils.getColorType();
        mMonetColorOverridePreference.setEnabled(colorType == MonetUtils.COLOR_TYPE_CUSTOM);
        mAccentColorPreference.setEnabled(colorType == MonetUtils.COLOR_TYPE_INTERNAL);
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
