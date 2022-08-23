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

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.recyclerview.widget.RecyclerView;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.custom.MonetUtils;
import com.android.internal.util.custom.ThemeUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.Utils;

import java.util.List;

public class AccentColors extends SettingsPreferenceFragment {
    private static final String TAG = "AccentColors";

    private static final String KEY_MONET_COLOR_ACCENT = "monet_engine_color_accent";

    private RecyclerView mRecyclerView;
    private ThemeUtils mThemeUtils;
    private MonetUtils mMonetUtils;
    private String mCategory = ThemeUtils.ACCENT_KEY;
    private String mTarget = "android";

    private List<String> mPkgs;
    private List<Integer> mThemeColors;

    private ContentResolver mResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.theme_colors_predefined_accent_colors_title);

        mResolver = getActivity().getContentResolver();

        mThemeUtils = new ThemeUtils(getActivity());
        mPkgs = mThemeUtils.getOverlayPackagesForCategory(mCategory, mTarget);
        mThemeColors = mThemeUtils.getColors();

        mMonetUtils = new MonetUtils(getActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.item_view, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        Adapter mAdapter = new Adapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.ALTAIR;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.CustomViewHolder> {
        Context context;
        String mSelectedPkg;
        String mAppliedPkg;

        public Adapter(Context context) {
            this.context = context;
        }

        @Override
        public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_option, parent,
                    false);
            CustomViewHolder vh = new CustomViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(CustomViewHolder holder, final int position) {
            String accentPkg = mPkgs.get(position);

            final int color = mThemeColors.get(position);
            holder.image.setBackgroundResource(R.drawable.accent_background);
            holder.image.setBackgroundTintList(ColorStateList.valueOf(color));

            String currentPackageName = mThemeUtils.getOverlayInfos(mCategory).stream()
                .filter(info -> info.isEnabled())
                .map(info -> info.packageName)
                .findFirst()
                .orElse(mTarget);
            holder.name.setText(mTarget.equals(accentPkg) ? "Default"
                    : getLabel(holder.name.getContext(), accentPkg));

            if (currentPackageName.equals(accentPkg)) {
                mAppliedPkg = accentPkg;
                if (mSelectedPkg == null) {
                    mSelectedPkg = accentPkg;
                }
            }

            holder.itemView.setActivated(accentPkg == mSelectedPkg);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateActivatedStatus(mSelectedPkg, false);
                    updateActivatedStatus(accentPkg, true);
                    mSelectedPkg = accentPkg;
                    enableOverlays(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mPkgs.size();
        }

        public class CustomViewHolder extends RecyclerView.ViewHolder {
            TextView name;
            ImageView image;
            public CustomViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.option_label);
                image = (ImageView) itemView.findViewById(R.id.option_thumbnail);
            }
        }

        private void updateActivatedStatus(String pkg, boolean isActivated) {
            int index = mPkgs.indexOf(pkg);
            if (index < 0) {
                return;
            }
            RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(index);
            if (holder != null && holder.itemView != null) {
                holder.itemView.setActivated(isActivated);
            }
        }
    }

    public Drawable getDrawable(Context context, String pkg, String drawableName) {
        try {
            PackageManager pm = context.getPackageManager();
            Resources res = pkg.equals(mTarget) ? Resources.getSystem()
                    : pm.getResourcesForApplication(pkg);
            return res.getDrawable(res.getIdentifier(drawableName, "drawable", pkg));
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getLabel(Context context, String pkg) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getApplicationInfo(pkg, 0)
                    .loadLabel(pm).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pkg;
    }

    public void enableOverlays(int position) {
        mThemeUtils.setOverlayEnabled(mCategory, mPkgs.get(position), mTarget);
        mMonetUtils.setAccentColor(
                ColorStateList.valueOf(mThemeColors.get(position)).getDefaultColor());
    }
}
