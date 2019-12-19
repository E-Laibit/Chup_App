package com.google.firebase.codelab.kit.model.RecyclerViewImage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.codelab.kit.model.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<Upload> mUploads;

    //Header Item Type
    private static final int HEADER_ITEM = 0;
    ////Food Item Type
    private static final int IMAGE_ITEM = 2;

    LayoutInflater inflater;


    public ImageAdapter(Context context, List<Upload> uploads){
        mContext = context;
        mUploads = uploads;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == IMAGE_ITEM){
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item,
                    parent, false);
            return new ImageViewHolder(itemView);

        } else if (viewType == HEADER_ITEM){
            View itemView =
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_history_header, parent, false);
            return new HeaderHolder(itemView);
        }
        else return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof HeaderHolder){
            HeaderHolder headerHolder = (HeaderHolder) holder;

            headerHolder.blueHeader.isShown();
            headerHolder.historyHeader.isShown();

        } else if (holder instanceof ImageViewHolder){
            ImageViewHolder imageViewHolder = (ImageViewHolder) holder;

            imageViewHolder.textViewName.setText(mUploads.get(position).getName());
            imageViewHolder.textViewTranslate.setText(mUploads.get(position).getTranslate());
            Picasso.with(mContext).load(mUploads.get(position).getImageUrl()).fit().centerCrop().rotate(90)
                    .placeholder(R.mipmap.pinknobackground)
                    .into(imageViewHolder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return mUploads.size(); // show as many items in the uploads list
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewName;
        public ImageView imageView;
        public TextView textViewTranslate;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.text_view_name);
            textViewTranslate = itemView.findViewById(R.id.text_view_translate);
            imageView = itemView.findViewById(R.id.image_view_upload);


        }
    }

    public class HeaderHolder extends RecyclerView.ViewHolder{
        public TextView blueHeader;
        public TextView historyHeader;

        public HeaderHolder(@NonNull View itemView){
            super(itemView);
            blueHeader = itemView.findViewById(R.id.blueheader);
            historyHeader = itemView.findViewById(R.id.historyheader);
        }
    }

    public int getItemViewType (int position){
        if (position == 0){
            return HEADER_ITEM;
        }
        return IMAGE_ITEM;
    }
}
