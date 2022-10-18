package com.example.personalproductivity.viewmodels;

import androidx.lifecycle.ViewModel;
import com.example.personalproductivity.db.TaskOrParent;
import com.example.personalproductivity.db.types.TaskOrParentType;
import lombok.Getter;
import lombok.Setter;

public class ProjectListViewModel extends ViewModel {

    @Getter @Setter private TaskOrParentType requestedType;
    @Getter @Setter private TaskOrParent parent;
}
