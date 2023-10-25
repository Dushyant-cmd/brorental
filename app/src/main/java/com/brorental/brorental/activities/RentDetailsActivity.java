package com.brorental.brorental.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.brorental.brorental.R;
import com.brorental.brorental.databinding.ActivityRentDetailsBinding;
import com.brorental.brorental.databinding.PaymentBottomSheetBinding;
import com.brorental.brorental.localdb.SharedPref;
import com.brorental.brorental.utilities.AppClass;
import com.brorental.brorental.utilities.DialogCustoms;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class RentDetailsActivity extends AppCompatActivity {
    private ActivityRentDetailsBinding binding;
    private String TAG = "RentDetailsActivity.java";
    private AppClass application;
    private SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rent_details);
        application = (AppClass) getApplication();
        sharedPref = application.sharedPref;
        setSupportActionBar(binding.toolbar);
        getData();
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void getData() {
        try {
            JSONObject data = new JSONObject(Objects.requireNonNull(getIntent().getStringExtra("data")));
            Log.d(TAG, "onCreate: " + data);
            ArrayList<SlideModel> list = new ArrayList<>();
            String[] images = data.getString("adsImageUrl").split(",");
            for (String url : images) {
                list.add(new SlideModel(url, ScaleTypes.FIT));
            }
            list.add(new SlideModel(images[0], ScaleTypes.FIT));
            list.add(new SlideModel(images[0], ScaleTypes.FIT));
            binding.imageSlider.setImageList(list, ScaleTypes.FIT);
            binding.imageSlider.setItemClickListener(new ItemClickListener() {
                @Override
                public void onItemSelected(int i) {
                    Log.d(TAG, "onItemSelected: " + list.get(i));
                }
            });

            binding.advertId.setText("Ads Id: " + data.getString("id"));
            binding.amountTV.setText("\u20B9 " + data.getString("perHourCharge"));
            binding.nameTV.setText(data.getString("name"));
            binding.ownerDesc.setText(data.getString("ownerDescription"));
            binding.addressTV.setText(data.getString("address"));
            binding.timingsTV.setText(data.getString("timings"));
            binding.extraChargeTV.setText("\u20B9 " + data.getString("extraCharge") + " /hour");
            binding.yearTV.setText(data.getString("year"));
            binding.pdColor.setText(data.getString("productColor"));
            binding.pdHealth.setText(data.getString("productHealth"));

            binding.rentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Display a bottom sheet to select from - to and total cost, extra charge,
                    //terms & conditions with a complete payment button.
                    try {
                        if (Long.parseLong(sharedPref.getUser().getWallet()) < 2500) {
                            DialogCustoms.showSnackBar(RentDetailsActivity.this, "Balance must be 2500 Rs.", binding.getRoot());
                        } else {
                            PayBottomSheet sheet = new PayBottomSheet(data.getString("id"));
                            sheet.show(getSupportFragmentManager(), PayBottomSheet.TAG);
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "onClick: json " + e);
                    }
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "onCreate: " + e);
        }
    }

    public static class PayBottomSheet extends BottomSheetDialogFragment {
        public static String TAG = "PayBottomSheet.java";
        public String advertId = "";
        String fromDate = "", toDate = "";

        public PayBottomSheet(String advertId) {
            this.advertId = advertId;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            PaymentBottomSheetBinding binding = DataBindingUtil.inflate(inflater, R.layout.payment_bottom_sheet, container, false);
            binding.payBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!fromDate.isEmpty() && !toDate.isEmpty()) {
                        Intent i = new Intent(getActivity(), PaymentActivity.class);
                        i.putExtra("amt", "1");
                        i.putExtra("id", advertId);
                        startActivity(i);
                    } else {
                        DialogCustoms.showSnackBar(getActivity(), "Please select date", binding.getRoot());
                    }
                }
            });
            binding.fromET.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Calendar calendar = Calendar.getInstance();
                    DatePickerDialog dialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            fromDate = dayOfMonth + "-" + month + "-" + year;
                            TimePickerDialog timeDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                    fromDate += " " + hourOfDay + ":" + minute;
                                    binding.fromET.setText(fromDate);
                                }
                            }, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), true);

                            timeDialog.show();
                        }
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                    dialog.getDatePicker().setMinDate(System.currentTimeMillis());

                    dialog.show();
                }
            });
            binding.toET.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Calendar calendar = Calendar.getInstance();
                    DatePickerDialog dialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            toDate = dayOfMonth + "-" + month + "-" + year;
                            TimePickerDialog timeDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                    try {
                                        toDate += " " + hourOfDay + ":" + minute;
                                        SimpleDateFormat spf = new SimpleDateFormat("dd-MM-yyyy hh:mm", Locale.getDefault());
                                        Date d = spf.parse(fromDate);
                                        Date d2 = spf.parse(toDate);
                                        long diffHour = ((d.getTime() - d2.getTime()) / (1000 * 60 * 60)) % 24;
//                                        long toHour = ((d2.getTime() / (1000 * 60 * 60)) % 24);
                                        binding.toET.setText(toDate);
                                        Log.d(TAG, "onCreateView time1: " + d.getTime() + "," + diffHour);
                                        Log.d(TAG, "onCreateView time2: " + d2.getTime());
                                    } catch (Exception e) {
                                        Log.d(TAG, "onCatch to date: " + e);
                                    }
                                }
                            }, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), true);
                            timeDialog.show();
                        }
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                    dialog.getDatePicker().setMinDate(System.currentTimeMillis());
                    dialog.show();
                }
            });
            return binding.getRoot();
        }
    }
}