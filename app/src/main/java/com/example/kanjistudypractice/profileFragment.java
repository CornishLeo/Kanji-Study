package com.example.kanjistudypractice;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import de.hdodenhof.circleimageview.CircleImageView;


public class profileFragment extends Fragment {

    ImageButton settingsBTN;
    CircleImageView profileIMG;
    TextView username;
    TextView email;
    TextView dateCreation;

    FirebaseFirestore db;
    FirebaseUser user;
    FirebaseStorage storage;



    private Uri croppedImageUri;

    private ActivityResultLauncher<CropImageContractOptions> cropImage =
            registerForActivityResult(new CropImageContract(), new ActivityResultCallback<CropImageView.CropResult>() {
                @Override
                public void onActivityResult(CropImageView.CropResult result) {
                    if (result.isSuccessful()) {
                        Bitmap cropped = BitmapFactory.decodeFile(result.getUriFilePath(getContext(), true));
                        profileIMG.setImageBitmap(cropped);
                        uploadBitmapToFirebaseStorage(cropped);
                    }
                }
            });

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    public profileFragment() {
        // Required empty public constructor
    }

    public static profileFragment newInstance(String param1, String param2) {
        profileFragment fragment = new profileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            // Callback is invoked after the user selects a media item or closes the photo picker.
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: " + uri);
                // Handle the selected URI, for example, upload it to Firebase Storage
                CropImageOptions cropImageOptions = new CropImageOptions();
                cropImageOptions.imageSourceIncludeGallery = false;
                cropImageOptions.imageSourceIncludeCamera = true;
                cropImageOptions.cropShape= CropImageView.CropShape.OVAL;
                cropImageOptions.fixAspectRatio = true;
                CropImageContractOptions cropImageContractOptions = new CropImageContractOptions(uri, cropImageOptions);
                cropImage.launch(cropImageContractOptions);
            } else {
                Log.d("PhotoPicker", "No media selected");
            }
        });


        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        settingsBTN = view.findViewById(R.id.settingsBTN);

        settingsBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), settings.class);
                startActivity(intent);
            }
        });

        profileIMG = view.findViewById(R.id.imgProfile);

        profileIMG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });

        username = view.findViewById(R.id.username);
        email = view.findViewById(R.id.email);
        dateCreation = view.findViewById(R.id.dateCreation);

        user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        db = FirebaseFirestore.getInstance();

        DocumentReference userRef = db.collection("users").document(uid);

        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // DocumentSnapshot contains the data
                    Map<String, Object> userData = documentSnapshot.getData();

                    // Now you can access individual fields in userData
                    String userName = (String) userData.get("username");
                    String emailtxt = (String) userData.get("email");
                    String profilePictureUrl = (String) userData.get("profilePictureUrl");
                    long dateCreated = (long) userData.get("dateCreated");

                    // Update your UI with the retrieved data
                    updateUI(userName, emailtxt, profilePictureUrl, dateCreated);
                } else {
                    Toast.makeText(view.getContext(), "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            private void updateUI(String userName, String emailtxt, String profilePictureUrl, long dateCreated) {

                email.setText(emailtxt);
                username.setText(userName);

                dateCreation.setText(convertToDate(dateCreated));


                downloadImage(profilePictureUrl);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(view.getContext(), "Database Failure", Toast.LENGTH_SHORT).show();
            }
        });


        return view;
    }

    private void uploadBitmapToFirebaseStorage(Bitmap bitmap) {
        storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Assuming 'imageUri' is the Uri of the selected image
        StorageReference imageRef = storageRef.child("profile_pictures/" + user.getUid() + ".jpg");

        try {
            // Apply circular transformation to the Bitmap
            Bitmap circularBitmap = getCircleBitmap(bitmap);

            // Convert the circular Bitmap to a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            circularBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            // Upload the circular image to Firebase Storage
            UploadTask uploadTask = imageRef.putBytes(data);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Image uploaded successfully, now get the download URL
                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUri) {
                            // Save the download URL in the user document in Firestore
                            saveImageUrlToFirestore(downloadUri.toString());
                        }

                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Handle failed upload
                    Log.e("FirebaseStorage", "Upload failed: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            // Handle exception
            e.printStackTrace();
        }
    }

    private void saveImageUrlToFirestore(String uri) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Update the 'profilePictureUrl' field in the user document
        db.collection("users")
                .document(uid)
                .update("profilePictureUrl", uri)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Profile picture URL saved successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Log.e("Firestore", "Firestore update failed: " + e.getMessage());
                    }
                });
    }

    public void downloadImage(String imageUrl) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        Future<Bitmap> future = executorService.submit(() -> {
            try {
                // Download the image in the background
                return BitmapFactory.decodeStream(new URL(imageUrl).openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });

        try {
            // Get the result of the asynchronous task
            Bitmap result = future.get();

            // Update UI with the downloaded image
            if (result != null) {
                profileIMG.setImageBitmap(result);
            } else {
                // Handle the case where image download failed
                Toast.makeText(getContext(), "Failed to download image", Toast.LENGTH_SHORT).show();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        executorService.shutdown();
    }


    private Bitmap getCircleBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, width, height);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

        canvas.drawCircle(width / 2, height / 2, width / 2, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return outputBitmap;
    }

    public String convertToDate(long timeCreated) {
        Date date = new Date(timeCreated);

        // Define the desired date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Format the date using the specified format
        String formattedDate = dateFormat.format(date);

        return formattedDate;

    }


}