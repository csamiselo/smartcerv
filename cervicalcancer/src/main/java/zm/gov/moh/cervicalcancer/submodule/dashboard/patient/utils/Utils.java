package zm.gov.moh.cervicalcancer.submodule.dashboard.patient.utils;

import android.content.Context;
import androidx.appcompat.widget.AppCompatTextView;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TableRow;

import zm.gov.moh.cervicalcancer.R;

public class Utils {

    public static ImageView renderCheckMarkIconView(Context context, boolean check){

        int fourDp = zm.gov.moh.core.utils.Utils.dpToPx(context,2);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(zm.gov.moh.core.utils.Utils.dpToPx(context,0), zm.gov.moh.core.utils.Utils.dpToPx(context,30));
        layoutParams.weight = 1;
        ImageView checkMark = new ImageView(context);
        if(check)checkMark.setImageResource(R.drawable.tick);
        checkMark.setLayoutParams(layoutParams);
        checkMark.setScaleType(ImageView.ScaleType.FIT_CENTER);
        checkMark.setPadding(fourDp,fourDp,fourDp,fourDp);
        checkMark.setBackground(context.getResources().getDrawable(R.drawable.border_right));

        return checkMark;
    }

    public static AppCompatTextView dateCellView(Context context, String date){

        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(zm.gov.moh.core.utils.Utils.dpToPx(context,0), TableRow.LayoutParams.MATCH_PARENT);
        layoutParams.weight = 1;
        AppCompatTextView dateTv = new AppCompatTextView(context);
        dateTv.setText(date);
        dateTv.setGravity(Gravity.CENTER);
        dateTv.setLayoutParams(layoutParams);
        dateTv.setTextColor(context.getResources().getColor(R.color.black));
        dateTv.setBackground(context.getResources().getDrawable(R.drawable.border_right));

        return dateTv;
    }
}
