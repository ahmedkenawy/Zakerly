package com.graduationproject.zakerly.navigation.favorites;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.graduationproject.zakerly.adapters.TeacherCardAdapter;
import com.graduationproject.zakerly.core.base.BaseFragment;
import com.graduationproject.zakerly.databinding.FragmentFavoritesBinding;

public class FavoritesFragment extends BaseFragment {

    private FragmentFavoritesBinding binding;
    private FavoritesViewModel viewModel;
    TextView mFavorite;
    RecyclerView mRecyclerViewFavorite;
    TeacherCardAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        viewModel = new FavoritesViewModelFactory(new FavoriteRepository()).create(FavoritesViewModel.class);
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(getViewLifecycleOwner());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
        viewModel.setUpFavoritesData(adapter);
    }

    private void initViews() {
        mFavorite = binding.textFavorites;
        mRecyclerViewFavorite = binding.recyclerviewFavorites;
        adapter = new TeacherCardAdapter(this);
        mRecyclerViewFavorite.setAdapter(adapter);
    }

}