package com.example.shawara.chat.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shawara.chat.R;
import com.example.shawara.chat.model.User;
import com.example.shawara.chat.utils.Constants;
import com.example.shawara.chat.utils.ImageUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * Created by shawara on 9/24/2016.
 */

public class MyProfileFragment extends Fragment {
    private SimpleDraweeView mProfileImageView;
    private ImageView mEditNameImageView;
    private TextView mNameEditText;
    private FloatingActionButton mProfileCameraFAB;
    private ProgressBar mUploadProgressBar;
    private StorageReference mStorageRef;
    private UploadTask mUploadTask;
    private DatabaseReference mDatabase;
    private User mUser;

    public final static int PICK_IMAGE_REQUEST = 100;
    public final static String TAG = "MyProfileFragment";

    public static MyProfileFragment newInstance() {
        return new MyProfileFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        mUser = (User) getActivity().getIntent().getSerializableExtra(MyProfileActivity.EXTRA_USER);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        mStorageRef = storage.getReferenceFromUrl(Constants.FIREBASE_STORAGE_URL_USERS + "/" + mUser.getUid());
        mDatabase = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.FIREBASE_URL_USERS);
        mDatabase.keepSynced(true);


        mNameEditText = (TextView) v.findViewById(R.id.profile_name_edit_text);
        mProfileImageView = (SimpleDraweeView) v.findViewById(R.id.profile_image_view);
        mEditNameImageView = (ImageView) v.findViewById(R.id.profile_edit_name_image_view);
        mProfileCameraFAB = (FloatingActionButton) v.findViewById(R.id.profile_fab_camera);
        mUploadProgressBar = (ProgressBar) v.findViewById(R.id.profile_upload_image_progress_bar);
        mUploadProgressBar.setVisibility(View.GONE);
        setProfileImage();

        mNameEditText.setText(mUser.getName());

        mProfileCameraFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivityForResult(getIntentImageChooser(), PICK_IMAGE_REQUEST);
            }
        });

        return v;
    }


    private void setProfileImage() {
//        Picasso.with(getActivity())
//                .load(mUser.getProfileImageUrl())
//                .placeholder(R.drawable.default_profile)
//                .error(R.drawable.default_profile)
//                .into(mProfileImageView);
        mProfileImageView.setImageURI(mUser.getProfileImageUrl());
    }

    public Intent getIntentImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        return Intent.createChooser(intent, "Select Picture");
    }


    public void uploadProfileImage(Uri file) {
        Uri compressedFile = ImageUtils.getImageFileCompressedUri(file, 600, getContext());

        mUploadProgressBar.setVisibility(View.VISIBLE);
        // mUploadProgressBar.setProgress(0);

        mUploadTask = mStorageRef.child("profileImage/" + compressedFile.getLastPathSegment()).putFile(compressedFile);


        mUploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mUploadProgressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "error occurred during uploading file.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: " + e);
            }
        });

        mUploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mUploadProgressBar.setVisibility(View.GONE);
                mDatabase.child(mUser.getUid()).child("profileImageUrl").setValue(taskSnapshot.getDownloadUrl().toString());
                mUser.setProfileImageUrl(taskSnapshot.getDownloadUrl().toString());
                setProfileImage();
            }
        });

    }


}
