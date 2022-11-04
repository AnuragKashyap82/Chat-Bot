package kashyap.anurag.botchats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import kashyap.anurag.botchats.databinding.ActivityImageViewBinding;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;

public class ImageViewActivity extends AppCompatActivity {

    private ActivityImageViewBinding binding;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding  = ActivityImageViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.progressBar.setVisibility(View.VISIBLE);

        imageUrl = getIntent().getStringExtra("imageUrl");
        try {
            Picasso.get().load(imageUrl).placeholder(R.drawable.ic_attach_white).into(binding.imageView);

        } catch (Exception e) {
            binding.imageView.setImageResource(R.drawable.ic_attach_white);
        }

        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        double bytes = storageMetadata.getSizeBytes();

                        double kb = bytes / 1024;
                        double mb = kb / 1024;

                        if (mb >= 1) {
                            binding.sizeTv.setText(String.format("%.2f", mb) + " MB");
                            binding.progressBar.setVisibility(View.GONE);
                        } else if (kb >= 1) {
                            binding.sizeTv.setText(String.format("%.2f", kb) + " KB");
                            binding.progressBar.setVisibility(View.GONE);
                        } else {
                            binding.sizeTv.setText(String.format("%.2f", bytes) + " bytes");
                            binding.progressBar.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
        binding.downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadImage(imageUrl);
            }
        });
    }

    private void downloadImage(String imageUrl) {

        long timestamp = System.currentTimeMillis();
        String nameWithExtension = timestamp + ".png";

        ProgressDialog progressDialog = new ProgressDialog(ImageViewActivity.this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Downloading" + nameWithExtension + "....");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
        storageReference.getBytes(50000000)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        saveDownloadedBook(ImageViewActivity.this, progressDialog, bytes, nameWithExtension);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(ImageViewActivity.this, "Failed to download due to" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private  void saveDownloadedBook(Context context, ProgressDialog progressDialog, byte[] bytes, String nameWithExtension) {
        try {
            File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadsFolder.mkdir();

            String filePath = downloadsFolder.getPath() + "/" + nameWithExtension;

            FileOutputStream out = new FileOutputStream(filePath);
            out.write(bytes);
            out.close();

            Toast.makeText(context, "Saved to Download Folder", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();

        } catch (Exception e) {
            Toast.makeText(context, "Failed saving to download folder due to" + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }
    }
}