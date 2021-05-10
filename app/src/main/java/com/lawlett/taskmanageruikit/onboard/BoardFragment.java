package com.lawlett.taskmanageruikit.onboard;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.auth.GoogleSignInActivity;
import com.lawlett.taskmanageruikit.splash.SplashActivity;
import com.lawlett.taskmanageruikit.utils.dialoglanguage.BaseRadioAdapter;
import com.lawlett.taskmanageruikit.utils.dialoglanguage.GridSpacingItemDecoration;
import com.lawlett.taskmanageruikit.utils.dialoglanguage.LanguageAdapter;
import com.lawlett.taskmanageruikit.utils.preferences.IntroPreference;
import com.lawlett.taskmanageruikit.utils.preferences.LanguagePreference;
import com.lawlett.taskmanageruikit.utils.preferences.ThemePreference;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class BoardFragment extends Fragment implements BaseRadioAdapter.LanguageChooseListener{
    ConstraintLayout container;
    ImageView imageDay, imageNight, imageDaySelect, imageNightSelect;
    LottieAnimationView calendar_anim, notes_anim, todo_anim, time_anim, google_anim;
    TextView title_tv, desc_tv, start_tv, change_lang;

    public BoardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_board, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        loadLocale();

        if (ThemePreference.getInstance(getContext()).getTheme()) {
            imageDaySelect.setVisibility(View.VISIBLE);
            imageNightSelect.setVisibility(View.GONE);
        } else {
            imageDaySelect.setVisibility(View.GONE);
            imageNightSelect.setVisibility(View.VISIBLE);
        }

        int pos = getArguments().getInt("pos");
        switch (pos) {
            case 0:
                title_tv.setText(R.string.event_calendar);
                desc_tv.setText(R.string.fast_add_your_event);
                calendar_anim.setVisibility(View.VISIBLE);
                todo_anim.setVisibility(View.GONE);
                notes_anim.setVisibility(View.GONE);
                change_lang.setVisibility(View.VISIBLE);
                container.setVisibility(View.GONE);
                google_anim.setVisibility(View.GONE);
                break;
            case 1:
                title_tv.setText(R.string.done_tasks);
                desc_tv.setText(R.string.list_tasks_help_you);
                calendar_anim.setVisibility(View.GONE);
                todo_anim.setVisibility(View.VISIBLE);
                notes_anim.setVisibility(View.GONE);
                change_lang.setVisibility(View.INVISIBLE);
                container.setVisibility(View.GONE);
                google_anim.setVisibility(View.GONE);
                break;
            case 2:
                title_tv.setText(R.string.record_idea_simple);
                desc_tv.setText(R.string.most_effect_idea);
                calendar_anim.setVisibility(View.GONE);
                todo_anim.setVisibility(View.GONE);
                notes_anim.setVisibility(View.VISIBLE);
                container.setVisibility(View.GONE);
                google_anim.setVisibility(View.GONE);
                break;
            case 3:
                title_tv.setText(R.string.check_timing);
                desc_tv.setText(R.string.plus_you_kpd);
                calendar_anim.setVisibility(View.GONE);
                todo_anim.setVisibility(View.GONE);
                notes_anim.setVisibility(View.GONE);
                time_anim.setVisibility(View.VISIBLE);
                start_tv.setVisibility(View.GONE);
                container.setVisibility(View.GONE);
                google_anim.setVisibility(View.GONE);
                break;
            case 4:
                title_tv.setVisibility(View.GONE);
                desc_tv.setVisibility(View.GONE);
                container.setVisibility(View.VISIBLE);
                calendar_anim.setVisibility(View.GONE);
                todo_anim.setVisibility(View.GONE);
                notes_anim.setVisibility(View.GONE);
                time_anim.setVisibility(View.GONE);
                start_tv.setVisibility(View.GONE);
                google_anim.setVisibility(View.GONE);
                break;
            case 5:
                title_tv.setText(R.string.cloud_save_data);
                desc_tv.setText(R.string.sync_planner_with_google);
                calendar_anim.setVisibility(View.GONE);
                todo_anim.setVisibility(View.GONE);
                notes_anim.setVisibility(View.GONE);
                time_anim.setVisibility(View.GONE);
                google_anim.setVisibility(View.VISIBLE);
                start_tv.setVisibility(View.VISIBLE);
                container.setVisibility(View.GONE);
                break;
        }

        imageDay.setOnClickListener(v -> {
            ThemePreference.getInstance(getContext()).saveNightTheme();
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        });

        imageNight.setOnClickListener(v -> {
            ThemePreference.getInstance(getContext()).saveLightTheme();
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        });

        start_tv.setOnClickListener(v -> {
            IntroPreference.getInstance(getContext()).saveShown();
            startActivity(new Intent(getContext(), GoogleSignInActivity.class));
            Objects.requireNonNull(requireActivity()).finish();
        });
        change_lang.setOnClickListener(v -> languageAlert());
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void languageAlert() {
        LayoutInflater inflater = LayoutInflater.from(requireActivity());
        View view = inflater.inflate(R.layout.language_layout, null);

        Dialog alertDialog = new Dialog(requireContext());
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(view);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        RecyclerView recyclerView;

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireActivity().getApplicationContext(), 1);
        gridLayoutManager.generateDefaultLayoutParams();
        List<String> languages = List.of(
                "English", "Русский", "Кыргызча", "Português", "한국어",
                "Український", "Deutsche", "हिंदी", "Қазақ тілі", "Беларускі", "Español", "Italiano", "Français", "Türk", "中文", "日本語");
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.setAdapter(new LanguageAdapter(requireContext(), languages, this));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(45));
        alertDialog.show();
    }

    private void initViews(@NonNull View view) {
        title_tv = view.findViewById(R.id.title_tv);
        desc_tv = view.findViewById(R.id.desc_tv);
        start_tv = view.findViewById(R.id.start_tv);
        change_lang = view.findViewById(R.id.change_lang);
        calendar_anim = view.findViewById(R.id.calendar_animation);
        todo_anim = view.findViewById(R.id.todo_animation);
        notes_anim = view.findViewById(R.id.notes_animation);
        time_anim = view.findViewById(R.id.time_animation);
        google_anim = view.findViewById(R.id.google_animation);
        container = view.findViewById(R.id.container_theme);
        imageDay = view.findViewById(R.id.image_day);
        imageNight = view.findViewById(R.id.image_night);
        imageDaySelect = view.findViewById(R.id.image_day_select);
        imageNightSelect = view.findViewById(R.id.image_night_select);
    }

    private void setLanguage(int position) {
        switch (position) {
            case 0:
                setLocale("en");
                break;
            case 1:
                setLocale("ru");
                break;
            case 2:
                setLocale("ky");
                break;
            case 3:
                setLocale("pt");
                break;
            case 4:
                setLocale("ko");
                break;
            case 5:
                setLocale("uk");
                break;
            case 6:
                setLocale("de");
                break;
            case 7:
                setLocale("hi");
                break;
            case 8:
                setLocale("kk");
                break;
            case 9:
                setLocale("be");
                break;
            case 10:
                setLocale("es");
                break;
            case 11:
                setLocale("it");
                break;
            case 12:
                setLocale("fr");
                break;
            case 13:
                setLocale("tr");
                break;
            case 14:
                setLocale("zh");
                break;
            case 15:
                setLocale("ja");
                break;
        }
        startActivity(new Intent(requireContext(), SplashActivity.class));
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        LanguagePreference.getInstance(getContext()).saveLanguage(lang);
    }

    public void loadLocale() {
        String language = LanguagePreference.getInstance(getContext()).getLanguage();
        setLocale(language);
    }

    @Override
    public void onClick(int position) {
        setLanguage(position);
    }
}
