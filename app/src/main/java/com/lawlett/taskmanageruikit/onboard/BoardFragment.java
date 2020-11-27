package com.lawlett.taskmanageruikit.onboard;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.main.MainActivity;
import com.lawlett.taskmanageruikit.settings.SettingsActivity;
import com.lawlett.taskmanageruikit.utils.LanguagePreference;
import com.lawlett.taskmanageruikit.utils.Preference;
import com.lawlett.taskmanageruikit.utils.ThemePreference;

import java.util.Locale;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class BoardFragment extends Fragment {
    ConstraintLayout container;
    ImageView imageDay, imageNight, imageDaySelect, imageNightSelect;
    LottieAnimationView calendar_anim, notes_anim, todo_anim, time_anim;
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title_tv = view.findViewById(R.id.title_tv);
        desc_tv = view.findViewById(R.id.desc_tv);
        start_tv = view.findViewById(R.id.start_tv);
        change_lang = view.findViewById(R.id.change_lang);

        calendar_anim = view.findViewById(R.id.calendar_animation);
        todo_anim = view.findViewById(R.id.todo_animation);
        notes_anim = view.findViewById(R.id.notes_animation);
        time_anim = view.findViewById(R.id.time_animation);
        container = view.findViewById(R.id.container_theme);
        imageDay = view.findViewById(R.id.image_day);
        imageNight = view.findViewById(R.id.image_night);
        imageDaySelect = view.findViewById(R.id.image_day_select);
        imageNightSelect = view.findViewById(R.id.image_night_select);
        loadLocale();

        if(ThemePreference.getInstance(getContext()).isTheme()){
            imageDaySelect.setVisibility(View.VISIBLE);
            imageNightSelect.setVisibility(View.GONE);
        }else{
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
                break;
            case 1:
                title_tv.setText(R.string.done_tasks);
                desc_tv.setText(R.string.list_tasks_help_you);
                calendar_anim.setVisibility(View.GONE);
                todo_anim.setVisibility(View.VISIBLE);
                notes_anim.setVisibility(View.GONE);
                change_lang.setVisibility(View.GONE);
                container.setVisibility(View.GONE);
                break;
            case 2:
                title_tv.setText(R.string.record_idea_simple);
                desc_tv.setText(R.string.most_effect_idea);
                calendar_anim.setVisibility(View.GONE);
                todo_anim.setVisibility(View.GONE);
                notes_anim.setVisibility(View.VISIBLE);
                change_lang.setVisibility(View.GONE);
                container.setVisibility(View.GONE);
                break;
            case 3:
                title_tv.setVisibility(View.GONE);
                desc_tv.setVisibility(View.GONE);
                container.setVisibility(View.VISIBLE);
                calendar_anim.setVisibility(View.GONE);
                todo_anim.setVisibility(View.GONE);
                notes_anim.setVisibility(View.GONE);
                time_anim.setVisibility(View.GONE);
                start_tv.setVisibility(View.GONE);
                change_lang.setVisibility(View.GONE);
                break;
            case 4:
                title_tv.setText(R.string.check_timing);
                desc_tv.setText(R.string.plus_you_kpd);
                calendar_anim.setVisibility(View.GONE);
                todo_anim.setVisibility(View.GONE);
                notes_anim.setVisibility(View.GONE);
                time_anim.setVisibility(View.VISIBLE);
                start_tv.setVisibility(View.VISIBLE);
                change_lang.setVisibility(View.GONE);
                container.setVisibility(View.GONE);
                break;

        }

        imageDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThemePreference.getInstance(getContext()).saveThemeTrue();
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });

        imageNight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThemePreference.getInstance(getContext()).saveThemeFalse();
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        start_tv.setOnClickListener(v -> {
            Preference.getInstance(getContext()).saveShown();
            startActivity(new Intent(getContext(), MainActivity.class));
            Objects.requireNonNull(getActivity()).finish();
        });
        change_lang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeLanguageDialog();
            }
        });

    }
    private void showChangeLanguageDialog() {
        final String[] listItems = {"English", "Русский", "Кырзгызча"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
        mBuilder.setTitle(R.string.choose_language);
        mBuilder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (i == 0) {
                    setLocale("en");
                    Objects.requireNonNull(getActivity()).recreate();
                } else if (i == 1) {
                    setLocale("ru");
                    Objects.requireNonNull(getActivity()).recreate();
                } else if (i == 2) {
                    setLocale("ky");
                    Objects.requireNonNull(getActivity()).recreate();
                }
                dialog.dismiss();
            }
        });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config,getResources().getDisplayMetrics());

        LanguagePreference.getInstance(getContext()).saveLanguage(lang);
    }
    public void loadLocale(){
        String language= LanguagePreference.getInstance(getContext()).getLanguage();
        setLocale(language);
    }
}
