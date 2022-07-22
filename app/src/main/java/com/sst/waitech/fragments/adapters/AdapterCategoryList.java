package com.sst.waitech.fragments.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sst.waitech.R;
import com.sst.waitech.database.prefs.SharedPref;

import java.util.List;

public class AdapterCategoryList extends RecyclerView.Adapter<AdapterCategoryList.ViewHolder> {

    Context context;
    List<String> items;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view,  List<String> items, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterCategoryList(Context context, List<String> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public AdapterCategoryList.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_label_chips, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AdapterCategoryList.ViewHolder holder, int position) {
        holder.title.setText(items.get(position));
        SharedPref sharedPref = new SharedPref(context);

        if (sharedPref.getIsDarkTheme()) {
            holder.lytLabel.setBackgroundResource(R.drawable.bg_chips_dark);
        } else {
            holder.lytLabel.setBackgroundResource(R.drawable.bg_chips_default);
        }

        holder.title.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, items, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        LinearLayout lytLabel;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.txt_label);
            lytLabel = view.findViewById(R.id.lyt_label);
        }
    }

}