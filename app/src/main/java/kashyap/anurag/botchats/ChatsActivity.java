package kashyap.anurag.botchats;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import kashyap.anurag.botchats.Adapters.AdapterMessages;
import kashyap.anurag.botchats.Models.ModelMessages;
import kashyap.anurag.botchats.databinding.ActivityChatsBinding;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ChatsActivity extends AppCompatActivity {

    private ActivityChatsBinding binding;
    private String message;
    private AdapterMessages adapterMessages;
    private ArrayList<ModelMessages> messagesArrayList;
    private Uri fileUri;
    private String myUrl = "";
    private StorageTask uploadTask;
    private TextRecognizer textRecognizer;
    private String botId, botName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        botId = getIntent().getStringExtra("botId");

        loadBotDetails(botId);
        loadAllMessages(botId);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sendBotResponseStart();
            }
        }, 1000);

        binding.sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
        binding.attachmentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[] = new CharSequence[]{

                        "Images",

                };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatsActivity.this);
                builder.setTitle("Select the File");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            galleryActivityResultLauncher.launch(intent);

                        }
                    }
                }).show();
            }
        });
    }

    private void loadBotDetails(String botId) {
        DatabaseReference databaseReference  = FirebaseDatabase.getInstance().getReference("Bots").child(botId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    botName = snapshot.child("botName").getValue().toString();
                    binding.botNameTv.setText(botName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendBotResponseStart() {
        long timestamp = System.currentTimeMillis();

        String[] arrayOfStrings = getResources().getStringArray(R.array.name);
        String randomString = arrayOfStrings[new Random().nextInt(arrayOfStrings.length)];

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("response", "Hello, Today you are talking to "+randomString+ ". How can I help you");
        hashMap.put("messageId", "RESPONSE");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bots").child(botId).child("Messages").child("" + timestamp);
        databaseReference.updateChildren(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                        } else {
                            Toast.makeText(ChatsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadAllMessages(String botId) {
        messagesArrayList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Bots").child(botId).child("Messages");
        reference
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            messagesArrayList.clear();
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                ModelMessages modelMessages = ds.getValue(ModelMessages.class);
                                messagesArrayList.add(modelMessages);

                            }
                            adapterMessages = new AdapterMessages(ChatsActivity.this, messagesArrayList);
                            binding.messagesRv.setAdapter(adapterMessages);
                            binding.scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }else{

                        }



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void validateData() {
        message = binding.inputMessageEt.getText().toString().trim();
        if (message.isEmpty()) {
            Toast.makeText(this, "Enter your Message!!", Toast.LENGTH_SHORT).show();
        } else {
            sendMessage(message);
        }
    }

    private void sendMessage(String message) {

        long timestamp = System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("message", "" + message);
        hashMap.put("messageId", "SENDER");
        hashMap.put("messageType", "text");

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bots").child(botId).child("Messages");
        databaseReference.child("" + timestamp)
                .setValue(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    sendBotResponse(message);
                                }
                            }, 500);

                            binding.inputMessageEt.setText("");
                        } else {
                            Toast.makeText(ChatsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendBotResponse(String message) {
        long timestamp = System.currentTimeMillis();
        if (botName.equals("Chat Bot")){
            if (message.toLowerCase().contains("hello")) {
                String[] arrayOfStrings = getResources().getStringArray(R.array.hello);
                String randomString = arrayOfStrings[new Random().nextInt(arrayOfStrings.length)];

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("response", "" + randomString);
                hashMap.put("messageId", "RESPONSE");

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bots").child(botId).child("Messages").child("" + timestamp);
                databaseReference.updateChildren(hashMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                } else {
                                    Toast.makeText(ChatsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ChatsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else if (message.toLowerCase().contains("flip") || message.toLowerCase().contains("coin")) {

                String[] arrayOfStrings = getResources().getStringArray(R.array.coin);
                String randomString = arrayOfStrings[new Random().nextInt(arrayOfStrings.length)];

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("response", "" + randomString);
                hashMap.put("messageId", "RESPONSE");

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bots").child(botId).child("Messages").child("" + timestamp);
                databaseReference.updateChildren(hashMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                } else {
                                    Toast.makeText(ChatsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ChatsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else if (message.toLowerCase().contains("how are you")) {

                String[] arrayOfStrings = getResources().getStringArray(R.array.howAreYou);
                String randomString = arrayOfStrings[new Random().nextInt(arrayOfStrings.length)];

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("response", "" + randomString);
                hashMap.put("messageId", "RESPONSE");

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bots").child(botId).child("Messages").child("" + timestamp);
                databaseReference.updateChildren(hashMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                } else {
                                    Toast.makeText(ChatsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ChatsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else if (message.toLowerCase().contains("open google")) {

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("response", "http://www.google.com");
                hashMap.put("messageId", "RESPONSE");

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bots").child(botId).child("Messages").child("" + timestamp);
                databaseReference.updateChildren(hashMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
                                    startActivity(browserIntent);

                                } else {
                                    Toast.makeText(ChatsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ChatsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else if (message.toLowerCase().contains("open youtube")) {

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("response", "Opening youtube");
                hashMap.put("messageId", "RESPONSE");

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bots").child(botId).child("Messages").child("" + timestamp);
                databaseReference.updateChildren(hashMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse("https://www.youtube.com/"));
                                    intent.setPackage("com.google.android.youtube");
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(ChatsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ChatsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else if (message.toLowerCase().contains("search")) {

                String key = message.replace("search", "");

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("response", "Searching " + key);
                hashMap.put("messageId", "RESPONSE");

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bots").child(botId).child("Messages").child("" + timestamp);
                databaseReference.updateChildren(hashMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?&q=" + key));
                                    startActivity(browserIntent);

                                } else {
                                    Toast.makeText(ChatsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ChatsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            } else if (message.toLowerCase().contains("time") || message.toLowerCase().contains("date")) {

                Calendar calForDate = Calendar.getInstance();
                SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
                String date = currentDateFormat.format(calForDate.getTime());

                Calendar calForTime = Calendar.getInstance();
                SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
                String time = currentTimeFormat.format(calForTime.getTime());

                String dateTime = "Today's date is: " + date + "\n" + "and the time is: " + time;

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("response", "" + dateTime);
                hashMap.put("messageId", "RESPONSE");

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bots").child(botId).child("Messages").child("" + timestamp);
                databaseReference.updateChildren(hashMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                } else {
                                    Toast.makeText(ChatsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ChatsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("response", "IDK");
                hashMap.put("messageId", "RESPONSE");

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bots").child(botId).child("Messages").child("" + timestamp);
                databaseReference.updateChildren(hashMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                } else {
                                    Toast.makeText(ChatsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ChatsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }else if (botName.equals("Image Text Reader")){
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("response", "Send me a Image with Text and I will scan Text from that image for u");
            hashMap.put("messageId", "RESPONSE");

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bots").child(botId).child("Messages").child("" + timestamp);
            databaseReference.updateChildren(hashMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                            } else {
                                Toast.makeText(ChatsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ChatsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {

                        binding.progressBar.setVisibility(View.VISIBLE);

                        Intent data = result.getData();
                        fileUri = data.getData();

                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                        long timestamp = System.currentTimeMillis();

                        StorageReference filePath = storageReference.child(timestamp + "." + "jpg");
                        uploadTask = filePath.putFile(fileUri)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                        while (!uriTask.isSuccessful()) ;
                                        myUrl = "" + uriTask.getResult();

                                        Map<String, Object> hashMap = new HashMap<>();
                                        hashMap.put("messageId", "SENDER");
                                        hashMap.put("messageType", "image");
                                        hashMap.put("imageUrl", "" + myUrl);

                                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bots").child(botId).child("Messages").child("" + timestamp);
                                        databaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {
                                                if (task.isSuccessful()) {
                                                    new Handler().postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            sendBotResponseImage();
                                                        }
                                                    }, 500);

                                                    binding.progressBar.setVisibility(View.GONE);
                                                } else {

                                                    binding.progressBar.setVisibility(View.GONE);
                                                    Toast.makeText(ChatsActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                                }
                                                binding.inputMessageEt.setText("");
                                            }
                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        binding.progressBar.setVisibility(View.GONE);
                                        Toast.makeText(ChatsActivity.this, "here" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(ChatsActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void sendBotResponseImage() {
        long timestamp = System.currentTimeMillis();

        if (botName.equals("Image Text Reader")){
            try {
                InputImage inputImage = InputImage.fromFilePath(this, fileUri);
                Task<Text> textTaskResult = textRecognizer.process(inputImage)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text text) {
                                binding.progressBar.setVisibility(View.GONE);
                                String recognizedText = text.getText();

                                if (recognizedText.equals("")){
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("response", "No text in this image");
                                    hashMap.put("messageId", "RESPONSE");

                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bots").child(botId).child("Messages").child("" + timestamp);
                                    databaseReference.updateChildren(hashMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {

                                                    } else {
                                                        Toast.makeText(ChatsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(ChatsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }else {
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("response", ""+recognizedText);
                                    hashMap.put("messageId", "RESPONSE");

                                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bots").child(botId).child("Messages").child("" + timestamp);
                                    databaseReference.updateChildren(hashMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {

                                                    } else {
                                                        Toast.makeText(ChatsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(ChatsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                binding.progressBar.setVisibility(View.GONE);
                                Toast.makeText(ChatsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else {

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("response", "Nice Image!!!");
            hashMap.put("messageId", "RESPONSE");

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Bots").child(botId).child("Messages").child("" + timestamp);
            databaseReference.updateChildren(hashMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                            } else {
                                Toast.makeText(ChatsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ChatsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}