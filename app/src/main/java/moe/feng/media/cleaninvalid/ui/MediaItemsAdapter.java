package moe.feng.media.cleaninvalid.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import moe.feng.media.cleaninvalid.R;
import moe.feng.media.cleaninvalid.model.MediaItem;

public class MediaItemsAdapter extends ListAdapter<MediaItem, MediaItemsAdapter.ViewHolder> {

    public MediaItemsAdapter() {
        super(new ItemCallback());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_media, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.onBind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkBox;
        TextView title, text1;

        private MediaItem item;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(android.R.id.checkbox);
            title = itemView.findViewById(android.R.id.title);
            text1 = itemView.findViewById(android.R.id.text1);

            itemView.setOnClickListener(v -> checkBox.toggle());
            checkBox.setOnCheckedChangeListener((view, checked) -> {
                item.isChecked = checked;
            });
        }

        void onBind(MediaItem item) {
            this.item = item;
            checkBox.setChecked(item.isChecked);
            title.setText(item.displayName);
            text1.setText(item.path);
        }

    }

    static class ItemCallback extends DiffUtil.ItemCallback<MediaItem> {

        @Override
        public boolean areItemsTheSame(@NonNull MediaItem oldItem, @NonNull MediaItem newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull MediaItem oldItem, @NonNull MediaItem newItem) {
            return areItemsTheSame(oldItem, newItem);
        }

    }

}
