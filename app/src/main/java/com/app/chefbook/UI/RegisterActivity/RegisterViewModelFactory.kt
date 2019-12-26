package com.app.chefbook.UI.RegisterActivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.chefbook.Data.IDataManager

@Suppress("UNCHECKED_CAST")
class RegisterViewModelFactory (val dataManager: IDataManager): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RegisterViewModel(dataManager) as T
    }
}