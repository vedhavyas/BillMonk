package com.digital.bills;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Authored by vedhavyas on 17/12/14.
 * Project JaagrT
 */

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private List<String> folders;
    private Context context;
    private OnFolderClickListener onFolderClickListener;

    public FolderAdapter(Context context, List<String> folders) {
        this.context = context;
        this.folders = folders;
    }

    @Override
    public FolderViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.folder_view, viewGroup, false);

        return new FolderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FolderViewHolder holder, int position) {
        String folder = folders.get(position);
        holder.title.setText(folder);
        holder.folderView.setImageDrawable(Utilities.getRoundedDrawable(context, folder));
    }

    @Override
    public int getItemCount() {
        if (folders != null) {
            return folders.size();
        }
        return 0;
    }

    public void setOnFolderClickListener(OnFolderClickListener onFolderClickListener) {
        this.onFolderClickListener = onFolderClickListener;
    }

    public void setFolders(List<String> folders) {
        this.folders.clear();
        this.folders.addAll(folders);
        notifyDataSetChanged();
    }

    public String getFolder(int position) {
        return folders.get(position);
    }

    public void removeFolder(int position) {
        this.folders.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    public void addFolder(String folder) {
        int position = folders.size();
        folders.add(position, folder);
        notifyItemInserted(position);
        notifyDataSetChanged();
    }

    public class FolderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected ImageView folderView;
        protected TextView title;

        public FolderViewHolder(View v) {
            super(v);
            folderView = (ImageView) v.findViewById(R.id.billView);
            title = (TextView) v.findViewById(R.id.title);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (onFolderClickListener != null) {
                onFolderClickListener.onItemClick(view, folders.get(getPosition()));
            }
        }
    }

}
