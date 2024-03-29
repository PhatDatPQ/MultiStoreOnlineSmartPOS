package com.app.superpos.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.superpos.Constant;
import com.app.superpos.R;
import com.app.superpos.model.Product;
import com.app.superpos.networking.ApiClient;
import com.app.superpos.networking.ApiInterface;
import com.app.superpos.product.EditProductActivity;
import com.app.superpos.utils.Utils;
import com.bumptech.glide.Glide;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;

import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype.Slidetop;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.MyViewHolder> {


    private List<Product> productData;
    private Context context;
    Utils utils;
    SharedPreferences sp;
    String currency;


    public ProductAdapter(Context context, List<Product> productData) {
        this.context = context;
        this.productData = productData;
        utils = new Utils();
        sp = context.getSharedPreferences(Constant.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        currency = sp.getString(Constant.SP_CURRENCY_SYMBOL, "");


    }


    @NonNull
    @Override
    public ProductAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_item, parent, false);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final ProductAdapter.MyViewHolder holder, int position) {
        final String product_id = productData.get(position).getProductId();
        String name = productData.get(position).getProductName();
        String stock = productData.get(position).getProductStock();
        String sellPrice = productData.get(position).getProductSellPrice();
        String buyPrice = productData.get(position).getProductBuyPrice();
        String productImage = productData.get(position).getProductImage();

        String imageUrl = Constant.PRODUCT_IMAGE_URL + productImage;


        holder.txtProductName.setText(name);
        holder.txtSupplierName.setText(context.getString(R.string.stock) + " :" + stock);
        holder.txtSellPrice.setText(context.getString(R.string.sell_price) + currency + sellPrice);
        holder.txtBuyPrice.setText(context.getString(R.string.buy_price) + currency + buyPrice);


        if (productImage != null) {
            if (productImage.length() < 3) {

                holder.productImage.setImageResource(R.drawable.image_placeholder);
            } else {


                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.loading)
                        .error(R.drawable.image_placeholder)
                        .into(holder.productImage);

            }
        }


        holder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(context);
                dialogBuilder
                        .withTitle(context.getString(R.string.delete))
                        .withMessage(context.getString(R.string.want_to_delete_product))
                        .withEffect(Slidetop)
                        .withDialogColor("#43a047") //use color code for dialog
                        .withButton1Text(context.getString(R.string.yes))
                        .withButton2Text(context.getString(R.string.cancel))
                        .setButton1Click(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (utils.isNetworkAvailable(context)) {
                                    deleteProduct(product_id);
                                    productData.remove(holder.getAdapterPosition());
                                    dialogBuilder.dismiss();
                                } else {
                                    dialogBuilder.dismiss();
                                    Toasty.error(context, R.string.no_network_connection, Toast.LENGTH_SHORT).show();
                                }


                            }
                        })
                        .setButton2Click(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                dialogBuilder.dismiss();
                            }
                        })
                        .show();

            }
        });

        holder.img_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, EditProductActivity.class);
                i.putExtra(Constant.PRODUCT_ID, productData.get(position).getProductId());
                i.putExtra(Constant.PRODUCT_ISEDIT, true);
                context.startActivity(i);

            }
        });
    }

    @Override
    public int getItemCount() {
        return productData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txtProductName, txtSupplierName, txtSellPrice,txtBuyPrice;
        ImageView imgDelete, productImage,img_edit;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtProductName = itemView.findViewById(R.id.txt_product_name);
            txtSupplierName = itemView.findViewById(R.id.txt_product_supplier);
            txtSellPrice = itemView.findViewById(R.id.txt_product_sell_price);
            txtBuyPrice = itemView.findViewById(R.id.txt_product_buy_price);

            imgDelete = itemView.findViewById(R.id.img_delete);
            productImage = itemView.findViewById(R.id.product_image);
            img_edit = itemView.findViewById(R.id.img_edit);

            itemView.setOnClickListener(this);


        }


        @Override
        public void onClick(View view) {
            Intent i = new Intent(context, EditProductActivity.class);
            i.putExtra(Constant.PRODUCT_ID, productData.get(getAdapterPosition()).getProductId());
            i.putExtra(Constant.PRODUCT_ISEDIT, false);
            context.startActivity(i);

        }
    }


    //delete from server
    private void deleteProduct(String productId) {

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        Call<Product> call = apiInterface.deleteProduct(productId);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(@NonNull Call<Product> call, @NonNull Response<Product> response) {


                if (response.isSuccessful() && response.body() != null) {

                    String value = response.body().getValue();

                    if (value.equals(Constant.KEY_SUCCESS)) {
                        Toasty.error(context, R.string.product_deleted, Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();

                    } else if (value.equals(Constant.KEY_FAILURE)) {
                        Toasty.error(context, R.string.error, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, R.string.no_network_connection, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Product> call, Throwable t) {
                Toast.makeText(context, "Error! " + t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
