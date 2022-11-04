package kashyap.anurag.botchats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import kashyap.anurag.botchats.databinding.ActivityImageViewBinding;

import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ImageViewActivity extends AppCompatActivity {

    private ActivityImageViewBinding binding;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding  =ActivityImageViewBinding.inflate(getLayoutInflater());
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
    }
}