package kashyap.anurag.botchats.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import kashyap.anurag.botchats.ImageViewActivity;
import kashyap.anurag.botchats.Models.ModelMessages;
import kashyap.anurag.botchats.R;

public class AdapterMessages extends RecyclerView.Adapter<AdapterMessages.HolderMessage> {

    private Context context;
    private ArrayList<ModelMessages> messagesArrayList;

    public AdapterMessages(Context context, ArrayList<ModelMessages> messagesArrayList) {
        this.context = context;
        this.messagesArrayList = messagesArrayList;
    }

    @NonNull
    @Override
    public HolderMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_messages_items, parent, false);
        return new HolderMessage(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderMessage holder, int position) {
        ModelMessages modelMessages = messagesArrayList.get(position);
        String messageId = modelMessages.getMessageId();
        String message = modelMessages.getMessage();
        String response = modelMessages.getResponse();
        String messageType = modelMessages.getMessageType();
        String imageUrl = modelMessages.getImageUrl();

        if (messageId.equals("SENDER")) {
            if (messageType.equals("image")) {
                holder.senderTv.setVisibility(View.GONE);
                holder.responseTv.setVisibility(View.GONE);
                holder.imageIv.setVisibility(View.VISIBLE);
                Picasso.get().load(imageUrl).into(holder.imageIv);
            } else {
                holder.responseTv.setVisibility(View.GONE);
                holder.senderTv.setText(message);
            }
        } else if (messageId.equals("RESPONSE")) {
            if (messageType.equals("image")) {
                holder.senderTv.setVisibility(View.GONE);
                holder.responseIv.setVisibility(View.VISIBLE);
                holder.responseTv.setVisibility(View.GONE);
                Picasso.get().load(imageUrl).into(holder.responseIv);
            } else {
                holder.senderTv.setVisibility(View.GONE);
                holder.responseTv.setText(response);
            }
        }

        holder.imageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ImageViewActivity.class);
                intent.putExtra("imageUrl", ""+imageUrl);
                context.startActivity(intent);
            }
        });
        holder.responseIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ImageViewActivity.class);
                intent.putExtra("imageUrl", ""+imageUrl);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return messagesArrayList.size();
    }

    public class HolderMessage extends RecyclerView.ViewHolder {

        private TextView responseTv, senderTv;
        private ImageView imageIv, responseIv;

        public HolderMessage(@NonNull View itemView) {
            super(itemView);

            responseTv = itemView.findViewById(R.id.responseTv);
            senderTv = itemView.findViewById(R.id.senderTv);
            imageIv = itemView.findViewById(R.id.imageIv);
            responseIv = itemView.findViewById(R.id.responseIv);
        }
    }
}
