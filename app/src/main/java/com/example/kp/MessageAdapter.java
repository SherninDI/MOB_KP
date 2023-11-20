package com.example.kp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int in = 0;
    private static final int out = 1;
    private final List<Messages> messages = new ArrayList<>();
    private final LayoutInflater mInflater;

    // data is passed into the constructor
    MessageAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    // inflates the row layout from xml when needed
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case in:
                View v1 = inflater.inflate(R.layout.list_item_message_in, parent, false);
                viewHolder = new ViewHolderIn(v1);
                break;
            case out:
                View v2 = inflater.inflate(R.layout.list_item_message_out, parent, false);
                viewHolder = new ViewHolderOut(v2);
                break;
            default:
                View v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
                viewHolder = new RecyclerViewSimpleTextViewHolder(v);
                break;
        }
        return viewHolder;
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case in:
                ViewHolderIn vh1 = (ViewHolderIn) holder;
                configureViewHolderIn(vh1, position);
                break;
            case out:
                ViewHolderOut vh2 = (ViewHolderOut) holder;
                configureViewHolder2(vh2, position);
                break;
            default:
                RecyclerViewSimpleTextViewHolder vh = (RecyclerViewSimpleTextViewHolder) holder;
                configureDefaultViewHolder(vh, position);
                break;
        }
    }

    public void addMessage(Messages msg){
        messages.add(0, msg);
        notifyDataSetChanged();
    }

    private void configureDefaultViewHolder(RecyclerViewSimpleTextViewHolder vh, int position) {
    }

    private void configureViewHolder2(ViewHolderOut vh2, int position) {
        final Messages itemList = messages.get(position);
        vh2.tvMessage.setText(itemList.toString());
    }

    private void configureViewHolderIn(ViewHolderIn vh1, int position) {
        final Messages itemList = messages.get(position);
        vh1.tvMessage.setText(itemList.toString());
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position)
    {
        if (messages.get(position) instanceof MessageIn) {
            return in;
        } else if (messages.get(position) instanceof MessageOut) {
            return out;
        }
        return -1;
    }



    public class ViewHolderIn extends RecyclerView.ViewHolder {
        public TextView tvMessage;
        public ViewHolderIn(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage_in);
        }
    }

    public class  RecyclerViewSimpleTextViewHolder extends RecyclerView.ViewHolder {
        public  RecyclerViewSimpleTextViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class ViewHolderOut extends RecyclerView.ViewHolder {
        public TextView tvMessage;
        public ViewHolderOut(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage_out);
        }

    }

}
