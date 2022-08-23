/*
 * Copyright (C) 2021-2022 crDroid Android Project
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

package com.altair.settings.fragments.ui;

import android.content.Context;
import android.os.Bundle;

import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.custom.MonetUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class MonetSettings extends SettingsPreferenceFragment {
    final static String TAG = "MonetSettings";

    private Context mContext;

    private MonetUtils mMonetUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.monet_engine);

        mContext = getActivity().getApplicationContext();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mMonetUtils = new MonetUtils(mContext);
        if (!mMonetUtils.isSurfaceTintEnabled()) {
            prefScreen.findPreference(MonetUtils.KEY_MONET_CHROMA_FACTOR).setEnabled(false);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.ALTAIR;
    }
}
