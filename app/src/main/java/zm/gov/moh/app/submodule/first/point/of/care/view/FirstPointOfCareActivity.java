package zm.gov.moh.app.submodule.first.point.of.care.view;


import androidx.lifecycle.ViewModelProviders;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import zm.gov.moh.app.R;
import zm.gov.moh.app.BR;
import zm.gov.moh.app.databinding.FirstPointOfCareActivityBinding;
import zm.gov.moh.app.submodule.first.point.of.care.viewmodel.FirstPointOfCareViewModel;
import zm.gov.moh.common.ui.BaseActivity;

public class FirstPointOfCareActivity extends BaseActivity {


    FirstPointOfCareViewModel viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(FirstPointOfCareViewModel.class);

       FirstPointOfCareActivityBinding binding =  DataBindingUtil.setContentView(this, R.layout.first_point_of_care_activity);
       BaseActivity.ToolBarEventHandler toolBarEventHandler = getToolbarHandler();
        toolBarEventHandler.setTitle("Point of Care");

       binding.setVariable(BR.fpocareviewmodel, viewModel);
       binding.setToolbarhandler(toolBarEventHandler);

       viewModel.getStartSubmodule().observe(this,this::startSubmodule);
    }
}
