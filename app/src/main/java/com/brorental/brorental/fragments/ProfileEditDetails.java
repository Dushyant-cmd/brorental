package com.brorental.brorental.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.brorental.brorental.R;
import com.brorental.brorental.databinding.FragmentProfileDetailsBinding;
import com.brorental.brorental.utilities.AppClass;
import com.brorental.brorental.utilities.DialogCustoms;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ProfileEditDetails extends Fragment {
    private FragmentProfileDetailsBinding binding;
    private String TAG = "ProfileEditDet.java";
    private boolean isAadhaarUpload = false, isDLUpload = false, isProfileUpload = false;
    private AppClass appClass;
    private File fileAadhaarImage, fileDLImage, fileProfileImage;
    private String currentPhotoPath;
    private ProgressDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_details, container, false);
        appClass = (AppClass) getActivity().getApplication();
        setListeners();
        return binding.getRoot();
    }
    private void setListeners() {
        binding.saveTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save user details
                dialog = new ProgressDialog(getActivity());
                dialog.show();
                StorageReference rootRef = appClass.storage.getReference();
                StorageReference profileRef = rootRef.child("/profile");
                StorageReference dLRef = rootRef.child("/drivingLicense");
                StorageReference aadhaarRef = rootRef.child("/aadhaar");
                String name = binding.nameET.getText().toString();
                String altMob = binding.altMobET.getText().toString();

                HashMap<String, Object> map = new HashMap<>();
                if (altMob.charAt(0) != '6' && altMob.charAt(0) != '7' && altMob.charAt(0) != '8' && altMob.charAt(0) != '9') {
                    if(!altMob.isEmpty()) {
                        map.put("alternateMobile", altMob);
                    } else
                        return;
                }
                else
                    return;
                if (!name.isEmpty())
                    map.put("name", name);

                if (!map.isEmpty()) {
                    appClass.firestore.collection("users").document(appClass.sharedPref.getUser().getPin())
                        .update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    dialog.dismiss();
                                    Log.d(TAG, "onComplete: success");
                                } else {
                                    Log.d(TAG, "onComplete: " + task.getException());
                                }
                            }
                        });
                }

                if (fileAadhaarImage != null) {
                    aadhaarRef.putFile(Uri.fromFile(fileAadhaarImage)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                aadhaarRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        dialog.show();
                                        saveImageUrl("aadhaarImgUrl", uri);
                                        Log.d(TAG, "onComplete: aadhaar success");
                                    }
                                });
                            } else {
                                Log.d(TAG, "onComplete: " + task.getException());
                            }
                        }
                    });
                }

                if (fileProfileImage != null) {
                    profileRef.putFile(Uri.fromFile(fileProfileImage)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        dialog.show();
                                        saveImageUrl("profileImageUrl", uri);
                                        Log.d(TAG, "onComplete: profile success");
                                    }
                                });
                            } else {
                                Log.d(TAG, "onComplete: " + task.getException());
                            }
                        }
                    });
                }

                if (fileDLImage != null) {
                    dLRef.putFile(Uri.fromFile(fileDLImage)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                dLRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Log.d(TAG, "onComplete: success");
                                        dialog.show();
                                        saveImageUrl("drivingLicenseImg", uri);
                                    }
                                });
                            } else {
                                Log.d(TAG, "onComplete: " + task.getException());
                            }
                        }
                    });
                }
            }
        });

        binding.profileCirIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkStoragePermission()) {
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_DENIED) {
                        Log.d(TAG, "onCreate: permission");
                        android.app.AlertDialog uploadDialog = DialogCustoms.getUploadDialog(getActivity());

                        LinearLayout uploadFileLayout = uploadDialog.findViewById(R.id.upload_file_layout);
                        LinearLayout takePhotoLayout = uploadDialog.findViewById(R.id.take_photo_layout);

                        uploadFileLayout.setOnClickListener(v1 ->
                        {
                            isProfileUpload = true;
                            uploadDialog.dismiss();
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            uploadProfileImage.launch(galleryIntent);
                        });

                        takePhotoLayout.setOnClickListener(v1 -> {
                            isProfileUpload = false;
                            uploadDialog.dismiss();
                            Intent cameraIntent = getCameraIntent();
                            uploadProfileImage.launch(cameraIntent);
                        });
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_MEDIA_IMAGES,
                                            Manifest.permission.CAMERA},
                                    1);
                        } else {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
                                    },
                                    1);
                        }
                    }
                }
            }
        });

        binding.dLTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkStoragePermission()) {
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_DENIED) {
                        Log.d(TAG, "onCreate: permission");
                        android.app.AlertDialog uploadDialog = DialogCustoms.getUploadDialog(getActivity());

                        LinearLayout uploadFileLayout = uploadDialog.findViewById(R.id.upload_file_layout);
                        LinearLayout takePhotoLayout = uploadDialog.findViewById(R.id.take_photo_layout);

                        uploadFileLayout.setOnClickListener(v1 ->
                        {
                            isDLUpload = true;
                            uploadDialog.dismiss();
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            uploadDLImage.launch(galleryIntent);
                        });

                        takePhotoLayout.setOnClickListener(v1 -> {
                            isDLUpload = false;
                            uploadDialog.dismiss();
                            Intent cameraIntent = getCameraIntent();
                            uploadDLImage.launch(cameraIntent);
                        });
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_MEDIA_IMAGES,
                                            Manifest.permission.CAMERA},
                                    1);
                        } else {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
                                    },
                                    1);
                        }
                    }
                }
            }
        });
        binding.aadhaarTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkStoragePermission()) {
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_DENIED) {
                        Log.d(TAG, "onCreate: permission");
                        android.app.AlertDialog uploadDialog = DialogCustoms.getUploadDialog(getActivity());

                        LinearLayout uploadFileLayout = uploadDialog.findViewById(R.id.upload_file_layout);
                        LinearLayout takePhotoLayout = uploadDialog.findViewById(R.id.take_photo_layout);

                        uploadFileLayout.setOnClickListener(v1 ->
                        {
                            isAadhaarUpload = true;
                            uploadDialog.dismiss();
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            uploadAadhaarImage.launch(galleryIntent);
                        });

                        takePhotoLayout.setOnClickListener(v1 -> {
                            isAadhaarUpload = false;
                            uploadDialog.dismiss();
                            Intent cameraIntent = getCameraIntent();
                            uploadAadhaarImage.launch(cameraIntent);
                        });
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_MEDIA_IMAGES,
                                            Manifest.permission.CAMERA},
                                    1);
                        } else {
                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA,
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
                                    },
                                    1);
                        }
                    }
                }
            }
        });
    }

    private void saveImageUrl(String key, Uri uri) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(key, uri.toString());

        appClass.firestore.collection("users").document(appClass.sharedPref.getUser().getPin())
                .update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success");
                        } else {
                            Log.d(TAG, "onComplete: " + task.getException());
                        }
                    }
                });
    }
    private Intent getCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 0);
        takePictureIntent.putExtra("android.intent.extra.USE_BACK_CAMERA", true);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Log.d(TAG, "getCameraIntent: " + ex);
            ex.printStackTrace();
        }

        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(getActivity(), "com.brorental.brorental.provider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        }
        return takePictureIntent;
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @SuppressLint("SetTextI18n")
    ActivityResultLauncher<Intent> uploadProfileImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                try {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (isProfileUpload) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                Uri selectedImage = data.getData();
                                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                                Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn,
                                        null, null, null);
                                assert cursor != null;
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String mediaPath = cursor.getString(columnIndex);
                                fileProfileImage = new File(mediaPath);
                                binding.profileCirIV.setImageURI(Uri.fromFile(fileProfileImage));

                                cursor.close();
                            }
                        } else {
                            try {
                                Log.d(TAG, "uri of camera image: " + currentPhotoPath);
                                if (currentPhotoPath != null) {
                                    Uri uri = Uri.fromFile(new File(currentPhotoPath));
                                    fileProfileImage = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                                            , "Pan" + System.currentTimeMillis() + ".jpeg");
                                    binding.profileCirIV.setImageURI(Uri.fromFile(fileProfileImage));

                                    InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
                                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                                    byte[] bytes = baos.toByteArray();

                                    FileOutputStream fileOutputStream = new FileOutputStream(fileProfileImage);
                                    fileOutputStream.write(bytes);
                                    fileOutputStream.flush();
                                    fileOutputStream.close();
                                    baos.close();

                                    Log.d(TAG, "bitmap: " + bitmap);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d(TAG, "onCatch pan: " + e.getLocalizedMessage());
                            }
                        }

                    } else {
                        fileProfileImage = null;
                    }
                } catch (Exception e) {
                    Log.d(TAG, "onCatch: " + e);
                }
            });

    ActivityResultLauncher<Intent> uploadDLImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                try {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (isDLUpload) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                Uri selectedImage = data.getData();
                                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                                Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn,
                                        null, null, null);
                                assert cursor != null;
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String mediaPath = cursor.getString(columnIndex);
                                fileDLImage = new File(mediaPath);
                                binding.dLTV.setText("Document Uploaded");

                                cursor.close();
                            }
                        } else {
                            try {
                                Log.d(TAG, "uri of camera image: " + currentPhotoPath);
                                if (currentPhotoPath != null) {
                                    Uri uri = Uri.fromFile(new File(currentPhotoPath));
                                    fileDLImage = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                                            , "Pan" + System.currentTimeMillis() + ".jpeg");
                                    binding.dLTV.setText("Document Uploaded");

                                    InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
                                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                                    byte[] bytes = baos.toByteArray();

                                    FileOutputStream fileOutputStream = new FileOutputStream(fileDLImage);
                                    fileOutputStream.write(bytes);
                                    fileOutputStream.flush();
                                    fileOutputStream.close();
                                    baos.close();

                                    Log.d(TAG, "bitmap: " + bitmap);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d(TAG, "onCatch pan: " + e.getLocalizedMessage());
                            }
                        }

                    } else {
                        fileDLImage = null;
                    }
                } catch (Exception e) {
                    Log.d(TAG, "onCatch: " + e);
                }
            });
    ActivityResultLauncher<Intent> uploadAadhaarImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                try {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (isAadhaarUpload) {
                            Intent data = result.getData();
                            if (data != null && data.getData() != null) {
                                Uri selectedImage = data.getData();
                                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                                Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn,
                                        null, null, null);
                                assert cursor != null;
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String mediaPath = cursor.getString(columnIndex);
                                fileAadhaarImage = new File(mediaPath);
                                binding.aadhaarTV.setText("Document Uploaded");

                                cursor.close();
                            }
                        } else {
                            try {
                                Log.d(TAG, "uri of camera image: " + currentPhotoPath);
                                if (currentPhotoPath != null) {
                                    Uri uri = Uri.fromFile(new File(currentPhotoPath));
                                    fileAadhaarImage = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                                            , "Pan" + System.currentTimeMillis() + ".jpeg");
                                    binding.aadhaarTV.setText("Document Uploaded");

                                    InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
                                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                                    byte[] bytes = baos.toByteArray();

                                    FileOutputStream fileOutputStream = new FileOutputStream(fileAadhaarImage);
                                    fileOutputStream.write(bytes);
                                    fileOutputStream.flush();
                                    fileOutputStream.close();
                                    baos.close();

                                    Log.d(TAG, "bitmap: " + bitmap);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d(TAG, "onCatch pan: " + e.getLocalizedMessage());
                            }
                        }

                    } else {
                        fileAadhaarImage = null;
                    }
                } catch (Exception e) {
                    Log.d(TAG, "onCatch: " + e);
                }
            });

    public boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                    || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                    || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA},
                        1);
                return false;
            }
        } else {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_DENIED
                    || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.CAMERA},
                        1);
                return false;
            }
        }
        return true;
    }

}