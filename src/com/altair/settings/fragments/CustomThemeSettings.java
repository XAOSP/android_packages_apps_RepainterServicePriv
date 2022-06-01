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
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.altair.settings.preferences.FontListPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.custom.ThemeUtils;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.display.darkmode.DarkModePreference;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.Arrays;
import java.util.List;

import lineageos.preference.LineageSystemSettingListPreference;
import lineageos.providers.LineageSettings;

@SearchIndexable
public class CustomThemeSettings extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "CustomThemeSettings";

    private static final String KEY_THEME_DARK_UI_MODE = "theme_dark_ui_mode";
    private static final String KEY_THEME_FONT = ThemeUtils.FONT_KEY;

    private Context mContext;
    private Handler mHandler;
    private ContentResolver mResolver;

    private UiModeManager mUiModeManager;
    private ThemeUtils mThemeUtils;

    private DarkModePreference mDarkMode;
    private FontListPreference mFontPreference;

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

        mDarkMode = findPreference(KEY_THEME_DARK_UI_MODE);
        mDarkMode.setOnPreferenceChangeListener(this);

        mFontPreference = (FontListPreference) prefScreen.findPreference(KEY_THEME_FONT);
        mFontPreference.setOnPreferenceChangeListener(this);
        updateState((ListPreference) mFontPreference);
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
            case KEY_THEME_FONT:
                mThemeUtils.setOverlayEnabled(ThemeUtils.FONT_KEY, (String) newValue);
                break;
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    public void updateState(ListPreference preference) {
        String currentPackageName = mThemeUtils.getOverlayInfos(preference.getKey()).stream()
                .filter(info -> info.isEnabled())
                .map(info -> info.packageName)
                .findFirst()
                .orElse("Default");

        List<String> pkgs = mThemeUtils.getOverlayPackagesForCategory(preference.getKey());
        List<String> labels = mThemeUtils.getLabels(preference.getKey());

        preference.setEntries(labels.toArray(new String[labels.size()]));
        preference.setEntryValues(pkgs.toArray(new String[pkgs.size()]));
        preference.setValue("Default".equals(currentPackageName) ? pkgs.get(0) : currentPackageName);
        preference.setSummary("Default".equals(currentPackageName) ? "Default" : labels.get(pkgs.indexOf(currentPackageName)));
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
