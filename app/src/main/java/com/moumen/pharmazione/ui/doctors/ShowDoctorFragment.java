package com.moumen.pharmazione.ui.doctors;

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
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.fragment.NavHostFragment;

import com.moumen.pharmazione.R;
import com.moumen.pharmazione.SharedViewModel;
import com.moumen.pharmazione.databinding.FragmentShowDoctorsBinding;
import com.google.android.material.transition.MaterialContainerTransform;
import com.google.android.material.transition.MaterialElevationScale;

public class ShowDoctorFragment extends Fragment {
    private FragmentShowDoctorsBinding binding;
    private SharedViewModel sharedViewModel;
    private long duration;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        duration = getResources().getInteger(R.integer.reply_motion_duration_large);
        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);
        prepareTransitions();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentShowDoctorsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.navigationIcon.setOnClickListener(o->{
            NavHostFragment.findNavController(this).navigateUp();
        });

        sharedViewModel.getDoctorsData().observe(getViewLifecycleOwner(), doc ->  {
            binding.setItem(doc);
//            dbCollection = FirebaseFirestore.getInstance().collection("med-dwa")
//                    .document(doc.getDocumentID()).collection("message");
//            mAuth = FirebaseAuth.getInstance();
//            setUpComments();
        });

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

//        this.setExitTransition(  new MaterialElevationScale( false)
//                .setDuration(duration));
//
//        this.setExitTransition(  new MaterialSharedAxis(MaterialSharedAxis.Z, false)
//                .setDuration(duration));
//
//
//        this.setEnterTransition(  new MaterialSharedAxis(MaterialSharedAxis.Z, false)
//                .setDuration(duration));
    }

}
