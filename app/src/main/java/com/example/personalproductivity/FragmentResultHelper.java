package com.example.personalproductivity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.NavController;

public class FragmentResultHelper {

    private final NavController navController;
    public FragmentResultHelper(NavController navController) {
        this.navController = navController;
    }

    public <T> LiveData<T> getNavigationResultLiveData(String key) {
        NavBackStackEntry back = navController.getCurrentBackStackEntry();
        if (back != null) {
            return back.getSavedStateHandle().getLiveData(key);
        }
        return new MutableLiveData<>(null);
    }

    public <T> void setNavigationResult(String key, T result) {
        NavBackStackEntry back = navController.getPreviousBackStackEntry();
        if (back != null) {
            back.getSavedStateHandle().set(key, result);
        }
    }
}
