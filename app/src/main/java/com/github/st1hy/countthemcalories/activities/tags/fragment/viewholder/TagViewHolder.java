package com.github.st1hy.countthemcalories.activities.tags.fragment.viewholder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class TagViewHolder extends RecyclerView.ViewHolder {


    public TagViewHolder(@NonNull View itemView) {
        super(itemView);
    }


    public abstract void onAttached();

    public abstract void onDetached();
}
