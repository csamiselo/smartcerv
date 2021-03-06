package zm.gov.moh.cervicalcancer.submodule.enrollment.view;

import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.widget.Toast;

import zm.gov.moh.cervicalcancer.CervicalCancerModule;
import zm.gov.moh.cervicalcancer.submodule.enrollment.viewmodel.CervicalCancerEnrollmentViewModel;
import zm.gov.moh.common.model.FormJson;
import zm.gov.moh.core.model.Key;
import zm.gov.moh.core.model.submodule.Submodule;
import zm.gov.moh.core.model.submodule.SubmoduleGroup;
import zm.gov.moh.common.ui.BaseActivity;
import zm.gov.moh.core.utils.BaseApplication;
import zm.gov.moh.core.utils.Utils;

public class CervicalCancerEnrollmentActivity extends BaseActivity {

    private CervicalCancerEnrollmentViewModel viewModel;
    private SubmoduleGroup cervicalCancerModule;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_cervical_cancer_enrollment);

        viewModel = ViewModelProviders.of(this).get(CervicalCancerEnrollmentViewModel.class);
        setViewModel(viewModel);

        //viewModel.getRepository().getClientById(34).observe(this, );
        cervicalCancerModule = (SubmoduleGroup)((BaseApplication) this.getApplication()).getSubmodule(CervicalCancerModule.MODULE);

        Submodule enrollmentSubmodule = cervicalCancerModule.getSubmodule(CervicalCancerModule.Submodules.CLIENT_ENROLLMENT);
        final Bundle bundle = getIntent().getExtras();

        String action = (bundle != null)? bundle.getString(BaseActivity.ACTION_KEY): "";

        long personId = bundle.getLong(Key.PERSON_ID);

        getViewModel().getRepository().getDatabase().genericDao()
                .getPatientById(personId)
                .observe(this ,patient->{
                    if(patient != null) {

                        Toast toast = Toast.makeText(this,null, Toast.LENGTH_LONG);
                        if(bundle.containsKey(BaseActivity.ACTION_KEY))
                            toast.setText("Client has been enrolled");
                        else
                            toast.setText("Client already exists");

                        toast.show();
                        CervicalCancerEnrollmentActivity.this.finish();
                    }else {

                        if(action != null && action.equals(Action.ENROLL_PATIENT)) {

                            viewModel.enrollPatient(bundle);

                        }
                        else{

                            Submodule formSubmodule = ((BaseApplication)this.getApplication()).getSubmodule(BaseApplication.CoreModule.FORM);

                            try{
                                String json = Utils.getStringFromInputStream(this.getAssets().open("forms/cervical_cancer_enrollment.json"));

                                // if(bundle == null)
                                // bundle = new Bundle();

                                FormJson formJson = new FormJson("Facility Information",
                                        Utils.getStringFromInputStream(this.getAssets().open("forms/cervical_cancer_enrollment.json")));

                                bundle.putSerializable(BaseActivity.JSON_FORM_KEY,formJson);
                                bundle.putString(BaseActivity.ACTION_KEY, Action.ENROLL_PATIENT);
                                bundle.putString(Key.START_MODULE_ON_RESULT, CervicalCancerModule.Submodules.CLIENT_ENROLLMENT);
                            }catch (Exception ex){

                            }

                            startSubmodule(formSubmodule, bundle);
                            finish();
                        }
                    }
                });


    }

    public class Action{
        static final String ENROLL_PATIENT = "ENROLL_PATIENT";
    }
}
