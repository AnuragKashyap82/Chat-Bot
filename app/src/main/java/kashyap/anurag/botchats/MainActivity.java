package kashyap.anurag.botchats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import kashyap.anurag.botchats.Adapters.AdapterBots;
import kashyap.anurag.botchats.Models.ModelBots;
import kashyap.anurag.botchats.databinding.ActivityMainBinding;

import android.os.Bundle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AdapterBots adapterBots;
    private ArrayList<ModelBots> botsArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadAllBots();
    }

    private void loadAllBots() {
        botsArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Bots");
        reference
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        botsArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelBots modelBots = ds.getValue(ModelBots.class);
                            botsArrayList.add(modelBots);
                        }
                        adapterBots = new AdapterBots(MainActivity.this, botsArrayList);
                        binding.allBotRv.setAdapter(adapterBots);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}