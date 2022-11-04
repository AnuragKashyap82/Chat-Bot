package kashyap.anurag.botchats.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import kashyap.anurag.botchats.ChatsActivity;
import kashyap.anurag.botchats.Models.ModelBots;
import kashyap.anurag.botchats.R;

public class AdapterBots extends RecyclerView.Adapter<AdapterBots.HolderBots> {

    private Context context;
    private ArrayList<ModelBots> botsArrayList;

    public AdapterBots(Context context, ArrayList<ModelBots> botsArrayList) {
        this.context = context;
        this.botsArrayList = botsArrayList;
    }

    @NonNull
    @Override
    public HolderBots onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_bot_items, parent, false);
        return new HolderBots(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderBots holder, int position) {

        ModelBots modelBots = botsArrayList.get(position);
        String botName = modelBots.getBotName();
        String botId = modelBots.getBotId();

        if (botName.equals("Chat Bot")){
            holder.botDescTv.setText("Chat with us what ever you want");
        }else if (botName.equals("Image Text Reader")){
            holder.botDescTv.setText("Send us Images with text and wee will scan the text in it");
        }else {
            holder.botDescTv.setText("To be integrated!!!");
        }

        holder.botNameTv.setText(botName);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatsActivity.class);
                intent.putExtra("botId", botId);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return botsArrayList.size();
    }

    public class HolderBots extends RecyclerView.ViewHolder {

        private TextView botNameTv, botDescTv;


        public HolderBots(@NonNull View itemView) {
            super(itemView);

            botNameTv = itemView.findViewById(R.id.botNameTv);
            botDescTv = itemView.findViewById(R.id.botDescTv);

        }
    }
}
