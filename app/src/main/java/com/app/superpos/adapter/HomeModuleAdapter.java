package com.app.superpos.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.app.superpos.HomeActivity;
import com.app.superpos.R;
import com.app.superpos.interfaces.IHomeGridClickListener;
import com.app.superpos.model.HomeModuleGrid;

import java.util.ArrayList;

public class HomeModuleAdapter extends RecyclerView.Adapter<HomeModuleAdapter.HomeModuleViewHolder> {

    public ArrayList<HomeModuleGrid> listOfModules;
    Context context;
    IHomeGridClickListener listener;

    public HomeModuleAdapter(Context context, ArrayList<HomeModuleGrid> listOfModules, IHomeGridClickListener listener) {
        this.context = context;
        this.listOfModules = listOfModules;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HomeModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_module, parent, false);
        return new HomeModuleViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull HomeModuleViewHolder holder, int position) {

        HomeModuleGrid hmd = listOfModules.get(position);

        holder.moduleImage.setImageDrawable(context.getDrawable(hmd.moduleImage));

        holder.moduletile.setText(context.getText(hmd.moduleTitle));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClickListener(hmd);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listOfModules.size();
    }

    class HomeModuleViewHolder extends RecyclerView.ViewHolder {
        ImageView moduleImage;
        TextView moduletile;

        public HomeModuleViewHolder(@NonNull View itemView) {
            super(itemView);
            moduleImage = itemView.findViewById(R.id.img_module_image);
            moduletile = itemView.findViewById(R.id.txt_module_title);
        }
    }
}
