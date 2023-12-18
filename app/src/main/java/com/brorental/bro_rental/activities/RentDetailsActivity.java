package com.brorental.bro_rental.activities;

import android.app.Activity;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.brorental.bro_rental.R;
import com.brorental.bro_rental.databinding.ActivityRentDetailsBinding;
import com.brorental.bro_rental.databinding.PaymentBottomSheetBinding;
import com.brorental.bro_rental.interfaces.UtilsInterface;
import com.brorental.bro_rental.localdb.SharedPref;
import com.brorental.bro_rental.models.User;
import com.brorental.bro_rental.utilities.AppClass;
import com.brorental.bro_rental.utilities.DialogCustoms;
import com.brorental.bro_rental.utilities.ProgressDialog;
import com.brorental.bro_rental.utilities.Utility;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentSnapshot;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RentDetailsActivity extends AppCompatActivity {
    private ActivityRentDetailsBinding binding;
    private String TAG = "RentDetailsActivity.java";
    private AppClass application;
    private SharedPref sharedPref;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rent_details);
        application = (AppClass) getApplication();
        //REGISTER BROADCAST RECEIVER FOR INTERNET
        Utility.registerConnectivityBR(RentDetailsActivity.this, application);
        activity = RentDetailsActivity.this;
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
            binding.imageSlider.setImageList(list, ScaleTypes.FIT);
            binding.imageSlider.setItemClickListener(new ItemClickListener() {
                @Override
                public void onItemSelected(int i) {
                    Log.d(TAG, "onItemSelected: " + list.get(i));
                }
            });

            String perHourCharge = data.getString("perHourCharge");
            String extraCharge = data.getString("extraCharge");
            binding.advertId.setText("Ads Id: " + data.getString("advertisementId"));
            binding.amountTV.setText("\u20B9 " + perHourCharge);
            binding.nameTV.setText(data.getString("name"));
            binding.ownerDesc.setText(data.getString("ownerDescription"));
            binding.addressTV.setText(data.getString("address"));
            binding.timingsTV.setText(data.getString("timings"));
            binding.extraChargeTV.setText("\u20B9 " + extraCharge + " /hour");
            binding.yearTV.setText(data.getString("year"));
            binding.pdColor.setText(data.getString("productColor"));
            binding.pdHealth.setText(data.getString("productHealth"));

            binding.rentBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Display a bottom sheet to select from - to and total cost, extra charge,
                    //terms & conditions with a complete payment button.
                    try {
                        if (sharedPref.getStatus().matches("pending")) {
                            DialogCustoms.noKycDialog(RentDetailsActivity.this, RentDetailsActivity.this, application, new UtilsInterface.NoKycInterface() {
                                @Override
                                public void refresh(AlertDialog alertDialog) {
                                    getProfile(alertDialog);
                                }
                            });
                            Toast.makeText(RentDetailsActivity.this, "Upload Profile.", Toast.LENGTH_SHORT).show();
                        } else {
                            PayBottomSheet sheet = new PayBottomSheet(data.getString("advertisementId"), perHourCharge, extraCharge, data, application);
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


    private void getProfile(AlertDialog alertDialog) {
        android.app.AlertDialog pDialog = ProgressDialog.createAlertDialog(RentDetailsActivity.this);
        pDialog.show();
        application.firestore.collection("users").document(application.sharedPref.getUser().getPin())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot d) {
                        pDialog.dismiss();
                        application.sharedPref.saveUser(new User(d.getString("name"), d.getString("mobile"), d.getString("pin"),
                                d.getString("totalRentItem"), d.getString("totalRides"), true,
                                d.getString("profileUrl"), d.getString("wallet")));
                        application.sharedPref.setAadhaarImg(d.getString("aadhaarImgUrl"));
                        application.sharedPref.setAadhaarPath(d.getString("aadhaarImgPath"));
                        application.sharedPref.setDLImg(d.getString("drivingLicenseImg"));
                        application.sharedPref.setDLPath(d.getString("drivingLicImgPath"));
                        application.sharedPref.setProfilePath(d.getString("profileImgPath"));
                        application.sharedPref.setStatus(d.getString("status"));
                        onActivityResult(101, RESULT_OK, null);

                        if(alertDialog != null)
                            if(!application.sharedPref.getStatus().equalsIgnoreCase("pending")) {
                                alertDialog.dismiss();
                            }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pDialog.dismiss();
                        Log.d(TAG, "onFailure: " + e);
                    }
                });
    }
    public static class PayBottomSheet extends BottomSheetDialogFragment {
        public static String TAG = "PayBottomSheet.java";
        public String advertId = "", perHourCharge, extraCharge;
        private JSONObject data;
        private String fromDate = "", toDate = "";
        private long fromTS, toTS, hours;
        private long rentAmt = 0L;
        private PaymentBottomSheetBinding binding;
        private AppClass appClass;

        public PayBottomSheet(String advertId, String perHourCharge, String extraCharge, JSONObject data, AppClass appClass) {
            this.advertId = advertId;
            this.perHourCharge = perHourCharge;
            this.data = data;
            this.extraCharge = extraCharge;
            this.appClass = appClass;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            binding = DataBindingUtil.inflate(inflater, R.layout.payment_bottom_sheet, container, false);
            SimpleDateFormat spf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
            binding.payBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (rentAmt <= 0)
                            DialogCustoms.showSnackBar(requireActivity(), "Minimum rent for 1 hour", binding.getRoot());
                        else if (fromDate.isEmpty() && toDate.isEmpty())
                            DialogCustoms.showSnackBar(requireActivity(), "Please select date", binding.getRoot());
                        else {
                            /** TODO make dynamic amount of rent item. */
                            Intent i = new Intent(requireActivity(), PaymentActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putLong("rentCost", (hours * Long.parseLong(perHourCharge)));
//                            bundle.putString("amt", String.valueOf(rentAmt));
                            bundle.putString("amt", "1");
                            bundle.putString("id", advertId);
                            bundle.putString("data", data.toString());
                            bundle.putString("rentStartDate", fromDate);
                            bundle.putString("rentEndDate", toDate);
                            bundle.putLong("hours", hours);
                            i.putExtra("rentBundle", bundle);
                            startActivity(i);
                            dismiss();
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "onClick: " + e);
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
                                    try {
                                        fromDate += " " + hourOfDay + ":" + minute + ":" + "00";
                                        Date d = spf.parse(fromDate);
                                        fromTS = d.getTime();
                                        binding.fromET.setText(spf.format(d));
                                        binding.toET.setText("");
                                    } catch (Exception e) {
                                        Log.d(TAG, "onTimeSet: " + e);
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
                                        toDate += " " + hourOfDay + ":" + minute + ":" + "00";
                                        Date d = spf.parse(toDate);
                                        toTS = d.getTime();
                                        binding.toET.setText(spf.format(d));
                                        hours = getDateDiff(spf, fromDate, toDate);
                                    } catch (Exception e) {
                                        Log.d(TAG, "onCatch to date: " + e);
                                    }
                                }
                            }, calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE), true);

                            timeDialog.show();
                        }
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                    if (fromTS > 0L) {
                        String[] str = fromDate.split("-");
                        calendar.set(Integer.parseInt(str[2].split(" ")[0]), Integer.parseInt(str[1]), Integer.parseInt(str[0]));
                    }

                    dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

                    dialog.show();
                }
            });
            return binding.getRoot();
        }

        public long getDateDiff(SimpleDateFormat format, String oldDate, String newDate) {
            try {
                long diffHours = TimeUnit.HOURS.convert(format.parse(newDate).getTime()
                        - format.parse(oldDate).getTime(), TimeUnit.MILLISECONDS);
                if (diffHours < 0) {
                    DialogCustoms.showSnackBar(requireActivity(), "Invalid Date and Time", binding.getRoot());
                    return 0L;
                }
                long wal = Long.parseLong(appClass.sharedPref.getUser().getWallet());
                rentAmt = (diffHours * Long.parseLong(perHourCharge));
                if (rentAmt > 0) {
                    binding.extraAmtTV.setText("\u20B9 " + extraCharge + " /hour");
                    binding.textLL.setVisibility(View.VISIBLE);
                    binding.rentCostTv.setText("Rent cost: " + Utility.rupeeIcon + rentAmt);
                    if (wal > rentAmt)
                        rentAmt = 2500;
                    else
                        rentAmt = ((rentAmt - wal) + 2500);
                    binding.totalAmtV.setText(Utility.rupeeIcon + rentAmt);
                    binding.tvSecDep.setText("Security deposit: +" + Utility.rupeeIcon + 2500);
                    binding.walletTV.setText("Wallet amount: -" + Utility.rupeeIcon + wal);
                } else {
                    DialogCustoms.showSnackBar(requireContext(), "Minimum rent for 1 hour", binding.getRoot());
                }
                return diffHours;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        }
    }
}