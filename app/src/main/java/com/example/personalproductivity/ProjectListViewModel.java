package com.example.personalproductivity;

import androidx.lifecycle.ViewModel;
import lombok.Getter;
import lombok.Setter;

public class ProjectListViewModel extends ViewModel {

    @Getter @Setter private TaskOrParentType requestedType;
    @Getter @Setter private TaskOrParent parent;
}
