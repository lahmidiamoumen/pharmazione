package com.example.pharmazione.ui.speciality;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DiffUtil;

import com.google.android.material.transition.MaterialContainerTransform;
import com.google.android.material.transition.MaterialElevationScale;
import com.example.pharmazione.R;
import com.example.pharmazione.databinding.FragmentSpecialityBinding;
import com.example.pharmazione.ui.home.SpecialityAdapter;
import com.example.pharmazione.utils.MedClickListener;

import java.util.ArrayList;
import java.util.List;

public class SpecialityFragment extends Fragment implements MedClickListener<Speciality> {
    private FragmentSpecialityBinding binding;
    private long duration;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        duration = getResources().getInteger(R.integer.reply_motion_duration_large);
        prepareTransitions();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSpecialityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.navigationIcon.setOnClickListener(o-> NavHostFragment.findNavController(this).navigateUp());


        String[] st = getResources().getStringArray(R.array.specialities);

        List<Speciality> list = new ArrayList<>();

        for (int i=0;i<st.length;i++){
            int dr =getResources().
                            getIdentifier("ic_"+(i+1),
                                    "drawable",
                                    getActivity().getPackageName());
            list.add( new Speciality(dr,st[i]));
        }
        list.add(new Speciality(getResources().
                getIdentifier("ic_21",
                        "drawable",
                        getActivity().getPackageName()),"Plus ..."));


        SpecialityAdapter horizantallAdapter = new SpecialityAdapter(this,new DiffUtil.ItemCallback<Speciality>() {
            @Override
            public boolean areItemsTheSame(@NonNull Speciality oldItem, @NonNull Speciality newItem) {
                return oldItem.mName.equals(newItem.mName) ;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Speciality oldItem, @NonNull Speciality newItem) {
                return oldItem.mName.equals(newItem.mName);
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

        this.setEnterTransition(  new MaterialElevationScale(true)
                .setDuration(duration).setInterpolator(interpolator));

        this.setReturnTransition(  new MaterialContainerTransform()
                .setDuration(duration).setInterpolator(interpolator));

    }


    @Override
    public void onClick(View view, Speciality item) {
        this.setReenterTransition(  new MaterialElevationScale( true)
                .setDuration(duration));

        this.setExitTransition(  new MaterialElevationScale( false)
                .setDuration(duration));

        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                .build();

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_specialityFrag_to_doctorsFragment,
                null,
                null,
                extras);
    }
}
