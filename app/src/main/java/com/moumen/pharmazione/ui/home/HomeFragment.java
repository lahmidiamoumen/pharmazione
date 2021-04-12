package com.moumen.pharmazione.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.moumen.pharmazione.databinding.EmailItemLayoutBinding;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.material.transition.MaterialElevationScale;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.moumen.pharmazione.FilterDialogFragment;
import com.moumen.pharmazione.Filters;
import com.moumen.pharmazione.R;
import com.moumen.pharmazione.SearchableActivity;
import com.moumen.pharmazione.SharedViewModel;
import com.moumen.pharmazione.databinding.FragmentHomeBinding;
import com.moumen.pharmazione.persistance.Document;
import com.moumen.pharmazione.persistance.HorizantallContent;
import com.moumen.pharmazione.utils.ItemClickListener;
import com.moumen.pharmazione.utils.MedClickListener;

import static com.moumen.pharmazione.utils.Util.PATH;

public class HomeFragment extends Fragment implements ItemClickListener , FilterDialogFragment.FilterListener , MedClickListener<HorizantallContent> {

    private  long duration ;
    private SharedViewModel sharedViewModel;
    private FirestorePagingAdapter adapter = null;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FragmentHomeBinding binding;
    private Query query;
    private FilterDialogFragment mFilterDialog;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel =  new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        FirebaseApp.initializeApp(getContext());

//        sharedViewModel = ViewModelProviders.of(requireActivity()).get(SharedViewModel.class);
//        setEnterTransition(new MaterialFadeThrough().setDuration(getResources().getInteger(R.integer.reply_motion_duration_large)));
        Query dbCollection = FirebaseFirestore.getInstance().collection(PATH);
        //query = dbCollection.whereEqualTo("isVerified",true).whereEqualTo("satisfied",false).orderBy("scanned", Query.Direction.DESCENDING);
        query = dbCollection.orderBy("scanned", Query.Direction.DESCENDING);
        adapter = getPaging();
        duration = getResources().getInteger(R.integer.reply_motion_duration_large);
        mFilterDialog = FilterDialogFragment.getInstance();
        mFilterDialog.setCallback(this);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        postponeEnterTransition();
        startPostponedEnterTransition();

        //setHorizontalScroll();

        binding.editText.setOnClickListener(o-> onFilterClicked());
        binding.filter.setOnClickListener(o->goToSearch());

        swipeRefreshLayout = binding.swipeContainer;
        swipeRefreshLayout.setRefreshing(true);
        RecyclerView recyclerView = binding.listView;

        recyclerView.setAdapter(adapter);
        swipeRefreshLayout.setOnRefreshListener(() -> adapter.refresh());

    }

    public void setUp(Query baseQuery){

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(10)
                .build();

        FirestorePagingOptions<Document> options = new FirestorePagingOptions.Builder<Document>()
                .setLifecycleOwner(getViewLifecycleOwner())
                .setQuery(baseQuery, config, Document.class)
                .build();

        adapter.updateOptions(options);

    }

    private void showToast() {
        Toast.makeText(getContext(), "An error occurred.", Toast.LENGTH_SHORT).show();
    }


    private void onFilterClicked() {
        mFilterDialog.show(requireActivity().getSupportFragmentManager(), FilterDialogFragment.TAG);
    }

    @Override
    public void onFilter(Filters filters) {
        String category = filters.getCategory();
        String city = filters.getCity();

        if (filters.hasCategory() && filters.hasCity()) {
            switch (category) {
                case "Orientation vers une pharmacie":
                    setQuery("Orientation", city);
                    break;
                case "Dons de medicaments":
                    setQuery("Don", city);
                    break;
                case "Besoin de médicament":
                    setQuery("Besoin", city);
                    break;
                default:
                    setQuery(null, city);
                    break;
            }
        }
        else if (filters.hasCategory()) {
            if(category.equals("Orientation vers une pharmacie")){
                setQuery("Orientation",null );
            }
            if(category.equals("Dons de medicaments")){
                setQuery("Don",null );
            }
            if(category.equals("Besoin de médicament")){
                setQuery("Besoin",null );
            }else
                setQuery(null,null);

        }
        else if (filters.hasCity()) {
            setQuery(null,city);
        }else {
            setQuery(null,null);
        }
        binding.filter.setText(Html.fromHtml(filters.getSearchDescription()));
    }


    private void goToSearch(){
        Intent intent = new Intent(getContext(), SearchableActivity.class);
        startActivityForResult(intent, 0);
    }


    @Override
    public void onClick(Document item, View sliderLayout) {


//       new MaterialSharedAxis(MaterialSharedAxis.Z, false)
//
//       this.setEnterTransition(  new MaterialSharedAxis(MaterialSharedAxis.Z, false)
//        .setDuration(getResources().getInteger(R.integer.reply_motion_duration_large)));
//
//       this.setExitTransition( new MaterialSharedAxis(MaterialSharedAxis.Z, true)
//        .setDuration(getResources().getInteger(R.integer.reply_motion_duration_large)));
//        this.setEnterTransition( new MaterialElevationScale(true)
//                .setDuration(getResources().getInteger(R.integer.reply_motion_duration_large)));
//        this.setExitTransition( new MaterialContainerTransform()
//                .setDuration(getResources().getInteger(R.integer.reply_motion_duration_large)));

        sharedViewModel.getDocData().setValue(item);

        this.setReenterTransition(  new MaterialElevationScale( true)
                .setDuration(duration));

        this.setExitTransition(  new MaterialElevationScale( false)
                .setDuration(duration));

        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                .addSharedElement(sliderLayout, sliderLayout.getTransitionName())
                .build();

        NavHostFragment.findNavController(this).navigate(R.id.action_navigation_home_to_showFragment,
                null,
                null,
                extras);
    }


    private void  setQuery(String opt,String location){
        if(location == null && opt == null)
            setUp(query);
        else if(location == null)
            setUp(query.whereEqualTo("category",opt));
        else if (opt == null){
            setUp(query.whereEqualTo("location",location));
        }else {
            setUp(query.whereEqualTo("location",location).whereEqualTo("category",opt));
        }
    }

    private FirestorePagingAdapter<Document, RecyclerView.ViewHolder> getPaging(){

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(10)
                .build();

        FirestorePagingOptions<Document> options = new FirestorePagingOptions.Builder<Document>()
                .setLifecycleOwner(this)
                .setQuery(query, config, Document.class)
                .build();

        return new FirestorePagingAdapter<Document, RecyclerView.ViewHolder>(options) {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
                EmailItemLayoutBinding binding = EmailItemLayoutBinding.inflate(layoutInflater, viewGroup, false);
                return new DocumentViewHolder(binding, HomeFragment.this);
            }


            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Document model) {
                DocumentViewHolder doc = (DocumentViewHolder) holder;
                doc.bindTo(model);
            }


            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                switch (state) {
                    case LOADING_INITIAL:
                        swipeRefreshLayout.setRefreshing(true);
                        break;
                    case LOADING_MORE:
                        //swipeRefreshLayout.setRefreshing(true);
                        break;
                    case LOADED:
                        swipeRefreshLayout.setRefreshing(false);
                        break;
                    case FINISHED:{
                        swipeRefreshLayout.setRefreshing(false);
                        break;}
                    case ERROR:
                        swipeRefreshLayout.setRefreshing(false);
                        showToast();
                        //retry();
                        break;
                }
            }
            @Override
            protected void onError(@NonNull Exception e) {
                swipeRefreshLayout.setRefreshing(false);
                Log.e("Home Layout", e.getMessage(), e);
            }
        };
    }


    @Override
    public void onClick(View view, HorizantallContent item) {

        this.setReenterTransition(  new MaterialElevationScale( false)
                .setDuration(duration));

        this.setExitTransition(  new MaterialElevationScale( true)
                .setDuration(duration));

        FragmentNavigator.Extras extras = new FragmentNavigator.Extras.Builder()
                .build();

        NavHostFragment.findNavController(this).navigate(R.id.action_navigation_home_to_specialityFrag,
                null,
                null,
                extras);
    }


//    @Override
//    public void onStart() {
//        super.onStart();
//        if (adapter != null) {
//            adapter.startListening();
//        }
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        if (adapter != null) {
//            adapter.stopListening();
//        }
//    }


//    private void setHorizontalScroll() {
//        List<HorizantallContent> list = new ArrayList<>();
//        HorizantallContent h1 = new HorizantallContent("1",null,"Find Doctors","https://firebasestorage.googleapis.com/v0/b/data-278806.appspot.com/o/images%2Fdoc.jpg?alt=media&token=5a0093eb-1400-4eee-8e66-cbbf640f375d","Diagnostic test and checkups");
//        HorizantallContent h2 = new HorizantallContent("2",null,"Find Pharmacies","https://firebasestorage.googleapis.com/v0/b/data-278806.appspot.com/o/images%2Fpharm.jpg?alt=media&token=cc589390-e242-42b3-9235-2fbb260ddc49","Check fo medicines");
//        list.add(h1);
//        list.add(h2);
//
//        HorizontalAdapter horizantallAdapter = new HorizontalAdapter(this,new DiffUtil.ItemCallback<HorizantallContent>() {
//            @Override
//            public boolean areItemsTheSame(@NonNull HorizantallContent oldItem, @NonNull HorizantallContent newItem) {
//                return oldItem.ID.equals(newItem.ID) ;
//            }
//
//            @Override
//            public boolean areContentsTheSame(@NonNull HorizantallContent oldItem, @NonNull HorizantallContent newItem) {
//                return oldItem.mDescription.equals(newItem.mDescription);
//            }
//        });
//        horizantallAdapter.submitList(list);
//        binding.alsoLikeList.setAdapter(horizantallAdapter);
//    }
}