package com.example.commutequest.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.commutequest.R;
import com.example.commutequest.model.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messages;

    public MessageAdapter() {
        this.messages = new ArrayList<>();
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public void addMessage(Message message) {
        this.messages.add(message);
        notifyItemInserted(this.messages.size() - 1);
    }

    public void clearMessages() {
        this.messages.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Message.TYPE_INCOMING) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_incoming, parent, false);
            return new IncomingMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_outgoing, parent, false);
            return new OutgoingMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder instanceof IncomingMessageViewHolder) {
            ((IncomingMessageViewHolder) holder).bind(message);
        } else if (holder instanceof OutgoingMessageViewHolder) {
            ((OutgoingMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    static class IncomingMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;

        IncomingMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            timeText = itemView.findViewById(R.id.message_time);
        }

        void bind(Message message) {
            messageText.setText(message.getText());
            timeText.setText(message.getFormattedTime());
        }
    }

    static class OutgoingMessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;

        OutgoingMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            timeText = itemView.findViewById(R.id.message_time);
        }

        void bind(Message message) {
            messageText.setText(message.getText());
            timeText.setText(message.getFormattedTime());
        }
    }
}