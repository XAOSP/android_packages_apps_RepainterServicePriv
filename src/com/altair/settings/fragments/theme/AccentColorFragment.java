/*
 * Copyright (C) 2022 Altair ROM Project
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

package com.altair.settings.fragments.theme;

//import android.app.UiModeManager;
import android.content.Context;
import android.content.DialogInterface;
//import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
//import android.os.UserHandle;
//import android.provider.SearchIndexableResource;
//import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
//import com.android.settings.dashboard.DashboardFragment;
//import com.android.settings.display.darkmode.DarkModePreference;
//import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.utils.MonetUtils;
import com.android.settings.utils.ThemeUtils;
//import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AccentColorFragment extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_THEME_COLORS_ACCENT_COLOR = "theme_colors_accent_color";

    private Context mContext;
    private Resources mResources;

    private ThemeUtils mThemeUtils;
    private MonetUtils mMonetUtils;

    private List<String> mAccentColorValues;
    private List<String> mAccentColorNames;

    private Preference mAccentColorPreference;

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.accent_color;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity().getApplicationContext();
        mResources = getResources();

        final PreferenceScreen prefScreen = getPreferenceScreen();

        mThemeUtils = new ThemeUtils(mContext);
        mMonetUtils = new MonetUtils(mContext);

        mAccentColorValues = Arrays.asList(mResources.getStringArray(
                R.array.theme_accent_color_values));
        mAccentColorNames = Arrays.asList(mResources.getStringArray(
                R.array.theme_accent_color_names));

        mAccentColorPreference = prefScreen.findPreference(KEY_THEME_COLORS_ACCENT_COLOR);
        updateAccentColorSummary();

        setHasOptionsMenu(true);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALTAIR;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.reset_accent_color_settings, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.reset_accent_color_settings) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.theme_colors_reset_settings_title)
                    .setMessage(R.string.theme_colors_reset_settings_message)
                    .setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {
                             //mMonetUtils.setAccentColor(MonetUtils.ACCENT_COLOR_DISABLED);
                             mMonetUtils.setSurfaceTintEnabled(MonetUtils.SURFACE_TINT_DEFAULT);
                             mMonetUtils.setAccurateShadesEnabled(MonetUtils.ACCURATE_SHADES_DEFAULT);
                             mMonetUtils.setRicherColorsEnabled(MonetUtils.RICHER_COLORS_DEFAULT);
                             mMonetUtils.setChromaFactor(MonetUtils.CHROMA_FACTOR_DEFAULT);
                             mMonetUtils.setWhiteLuminance(MonetUtils.WHITE_LUMINANCE_DEFAULT);
                             mMonetUtils.setLinearLightnessEnabled(MonetUtils.LINEAR_LIGHTNESS_DEFAULT);
                        }
                    })
                    .setNegativeButton(R.string.dlg_cancel, null);
            builder.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            case KEY_THEME_COLORS_ACCENT_COLOR:
                updateAccentColorSummary();
                break;
        }
        return true;
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
}
