package com.example.pharmazione.ui.doctors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DiffUtil;

import com.google.android.material.transition.MaterialContainerTransform;
import com.google.android.material.transition.MaterialElevationScale;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.database.annotations.Nullable;
import com.example.pharmazione.R;
import com.example.pharmazione.SharedViewModel;
import com.example.pharmazione.databinding.FragmentDoctorsBinding;
import com.example.pharmazione.persistance.Doctors;
import com.example.pharmazione.utils.MedClickListener;

import java.util.ArrayList;
import java.util.List;

public class DoctorsFragment extends Fragment implements MedClickListener<Doctors> {
    private FragmentDoctorsBinding binding;
    private long duration;
    private SharedViewModel sharedViewModel;
    private static final String GOOGLE_BROWSER_API_KEY = "AIzaSyDyHgN3_5qTd0w4Gp7ldiLuiSC7DoNAaxY";




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        duration = getResources().getInteger(R.integer.reply_motion_duration_large);
        prepareTransitions();
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDoctorsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);

        binding.navigationIcon.setOnClickListener(o->{
            NavHostFragment.findNavController(this).navigateUp();
        });

        String url = "2.8895,36.7594";
        List<Doctors> list = new ArrayList<>();
        Drawable dr = getResources().getDrawable(R.drawable.avatar_3);
        Doctors h1 = new Doctors("1","Alger","Dr Nawel KARA",dr,"33 rue des abattoirs staouali alger\n 16000 Alger Algérie\n +213.05.60.91.90.85","Médecin dentiste","3 ans d'experience",url);
        Doctors h2 = new Doctors("2","Oran","Dr Alladin SAOUDI",dr,"Rue Ben Aknoun N°5\n 16000 Alger Algérie\n +213.032.72.72.00   \n +213.07.71.57.50.88   ","Généraliste","10 ans d'experience","3.0884,36.7352");
        Doctors h3 = new Doctors("3","Sétif","Dr. Abd Elhakim Guezzou",dr,"Rien","Physiotherapy","5 ans d'experience",url);
        Doctors h4 = new Doctors("4","Blida","Dr. Djalal Rahmani",dr,"Rien","Mental Wllness","27 ans d'experience",url);
        Doctors h5 = new Doctors("5","Adrar","Dr. Rahim Guezzouri",dr,"Rien","Bone & Joints","2 ans d'experience",url);
        Doctors h6 = new Doctors("6","Boumerdes","Dr. Lina Belbouabe",dr,"Rien","Brain & Nerves","6 ans d'experience",url);
        Doctors h7 = new Doctors("6","Boumerdes","Dr. Lina Belbouabe",dr,"Rien","Brain & Nerves","6 ans d'experience",url);
        Doctors h8 = new Doctors("6","Boumerdes","Dr. Lina Belbouabe",dr,"Rien","Brain & Nerves","6 ans d'experience",url);
        list.add(h1);
        list.add(h2);
        list.add(h3);
        list.add(h4);
        list.add(h5);
        list.add(h6);
        list.add(h7);
        list.add(h8);

        DoctorsAdapter horizantallAdapter = new DoctorsAdapter(this,new DiffUtil.ItemCallback<Doctors>() {
            @Override
            public boolean areItemsTheSame(@NotNull Doctors oldItem, @NotNull Doctors newItem) {
                return oldItem.ID.equals(newItem.ID) ;
            }

            @Override
            public boolean areContentsTheSame(@NotNull Doctors oldItem, @NotNull Doctors newItem) {
                return oldItem.mDescription.equals(newItem.mDescription);
            }
        });

        horizantallAdapter.submitList(list);
        binding.recyclerView.setAdapter(horizantallAdapter);

        startTransitions();
    }

    private void startTransitions() {
        binding.executePendingBindings();
        startPostponedEnterTransition();
    }

    private void prepareTransitions() {
        postponeEnterTransition();
        Context cn = getContext();
        assert cn != null;
        @SuppressLint("Recycle")
        int array = cn.obtainStyledAttributes(new int[]{R.attr.motionInterpolatorPersistent}).getResourceId(0, android.R.interpolator.fast_out_slow_in);
        Interpolator interpolator =  AnimationUtils.loadInterpolator(cn,array);

        @SuppressLint("Recycle")
        int array2 = cn.obtainStyledAttributes(new int[]{R.attr.colorSurface}).getColor(0, Color.MAGENTA);

        MaterialContainerTransform mat = new MaterialContainerTransform();
        mat.setDuration(duration);
        mat.setDrawingViewId(R.id.navigation_home);
        mat.setScrimColor(Color.TRANSPARENT);
        mat.setAllContainerColors(array2);
        mat.setInterpolator(interpolator);
        setSharedElementEnterTransition(mat);

        this.setEnterTransition(  new MaterialElevationScale(false)
                .setDuration(duration).setInterpolator(interpolator));

        this.setReturnTransition(  new MaterialContainerTransform()
                .setDuration(duration).setInterpolator(interpolator));

    }


    @Override
    public void onClick(View view, Doctors item) {
        sharedViewModel.getDoctorsData().setValue(item);
        this.setReenterTransition(  new MaterialElevationScale( true)
                .setDuration(duration));

        this.setExitTransition(  new MaterialElevationScale( false)
                .setDuration(duration));

        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                .addSharedElement(view, view.getTransitionName())
                .build();

        NavHostFragment.findNavController(this).navigate(R.id.action_doctorsFrag_to_doctorsShowFrag,
                null,
                null,
                extras);
    }
}
