package com.app.superpos.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.superpos.Constant;
import com.app.superpos.R;
import com.app.superpos.database.DatabaseAccess;
import com.app.superpos.model.Product;
import com.app.superpos.pos.PosActivity;
import com.app.superpos.product.EditProductActivity;
import com.bumptech.glide.Glide;

import java.util.List;

import es.dmoral.toasty.Toasty;

public class PosProductAdapter extends RecyclerView.Adapter<PosProductAdapter.MyViewHolder> {

    private List<Product> productData;
    private Context context;
    MediaPlayer player;
    public static int count;
    String qty1;


    public PosProductAdapter(Context context, List<Product> productData) {
        this.context = context;
        this.productData = productData;
        player = MediaPlayer.create(context, R.raw.delete_sound);
    }


    @NonNull
    @Override
    public PosProductAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pos_product_item, parent, false);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final PosProductAdapter.MyViewHolder holder, int position) {

        final DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);
        databaseAccess.open();
        String currency = databaseAccess.getCurrency();

        final String product_id = productData.get(position).getProductId();
        final String name = productData.get(position).getProductName();
        final String product_weight = productData.get(position).getProductWeight();
        final String product_UnitName = productData.get(position).getProductWeightUnit();
        final String product_stock = productData.get(position).getProductStock();
        final String product_price = productData.get(position).getProductSellPrice();
        final String weight_unit_id = productData.get(position).getProductWeightUnitId();
        final String weight_unit_name = productData.get(position).getProductWeightUnit();
        final String tax = productData.get(position).getTax();
        String base64Image = productData.get(position).getProductImage();
        String imagePath = productData.get(position).getProductImage();
        if (!TextUtils.isEmpty(imagePath) && imagePath != null) {
            Glide.with(context).load(imagePath).centerCrop().placeholder(R.mipmap.ic_launcher).into(holder.product_image);
        }
        databaseAccess.open();
        qty1 = databaseAccess.getLatestProductQuantity(product_id);
        holder.txt_number.setText(qty1);
        databaseAccess.open();
        // final String weight_unit_name = databaseAccess.getWeightUnitName(weight_unit_id);

        holder.txtProductName.setText(name);

        //Low stock marked as RED color
        int getStock = Integer.parseInt(product_stock);
        if (getStock > 5) {
            holder.txtStock.setText(context.getString(R.string.stock) + " : " + product_stock);
        } else {
            holder.txtStock.setText(context.getString(R.string.stock) + " : " + product_stock);
            holder.txtStock.setTextColor(Color.RED);

        }
        holder.txtWeight.setText(product_weight + " " + product_UnitName);
        holder.txtPrice.setText(currency + " " + product_price);
        holder.txt_Tax.setText("Tax: "+tax);


        holder.cardProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                player.start();
                Intent intent = new Intent(context, EditProductActivity.class);
                intent.putExtra("product_id", product_id);
                context.startActivity(intent);
            }
        });


        try {
            if (base64Image != null) {
                if (base64Image.length() < 6) {
                    Log.d("64base", base64Image);
                    holder.product_image.setImageResource(R.drawable.image_placeholder);
                    holder.product_image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                } else {
                    byte[] bytes = Base64.decode(base64Image, Base64.DEFAULT);
                    holder.product_image.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));

                }
            }

        } catch (Exception ex) {
            Log.d("", "" + ex.getMessage());
        }


        holder.txt_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);
                databaseAccess.open();
                qty1 = databaseAccess.getLatestProductQuantity(product_id);
                int get_qty = Integer.parseInt(qty1);

                if (get_qty >= getStock) {
                    Toasty.error(context, context.getString(R.string.stock_not_available_please_update_stock) + " " + getStock, Toast.LENGTH_SHORT).show();
                } else {
                    get_qty++;
                    databaseAccess.open();
                    AddToCartFromPOSProduct(getStock, name, databaseAccess, product_id, product_weight, weight_unit_name, product_price, get_qty, product_stock, tax,currency);
                    holder.txt_number.setText("" + get_qty);
                    databaseAccess.open();
                    final String cart_id = databaseAccess.getCartIdFromProductCart(product_id);
                    databaseAccess.open();
                    databaseAccess.updateProductQty(cart_id, "" + get_qty);
                }
            }
        });

        holder.txt_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);
                databaseAccess.open();

                //String qty1 = holder.txt_number.getText().toString();
                qty1 = databaseAccess.getLatestProductQuantity(product_id);
                int get_qty = Integer.parseInt(qty1);
                if (get_qty >= 2) {
                    get_qty--;
                    holder.txt_number.setText("" + get_qty);
                    databaseAccess.open();
                    final String cart_id = databaseAccess.getCartIdFromProductCart(product_id);
                    databaseAccess.open();
                    databaseAccess.updateProductQty(cart_id, "" + get_qty);
                }


            }
        });
    }

    public void AddToCartFromPOSProduct(int getStock, String productName, DatabaseAccess databaseAccess, String product_id, String product_weight, String weight_unit_name, String product_price, int qty, String product_stock, String tax, String currency) {
        if (getStock <= 0) {
            Toasty.warning(context, R.string.stock_is_low_please_update_stock, Toast.LENGTH_SHORT).show();
        } else {
            // Log.d("w_id", weight_unit_id);
            databaseAccess.open();
            int check = databaseAccess.addToCart(product_id, productName, product_weight, weight_unit_name, product_price, qty, product_stock, "0", tax, currency);
            databaseAccess.open();
            int count = databaseAccess.getCartItemCount();
            if (count == 0) {
                PosActivity.txtCount.setVisibility(View.INVISIBLE);
            } else {
                PosActivity.txtCount.setVisibility(View.VISIBLE);
                PosActivity.txtCount.setText(String.valueOf(count));
            }
        }

    }

    @Override
    public int getItemCount() {
        return productData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CardView cardProduct;
        TextView txtProductName, txtStock, txtWeight, txtPrice, txt_number, txt_Tax;
        ImageView txt_minus, txt_plus;
        Button btnAddToCart;
        ImageView product_image;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtProductName = itemView.findViewById(R.id.txt_product_name);
            txtWeight = itemView.findViewById(R.id.txt_weight);
            txtPrice = itemView.findViewById(R.id.txt_price);
            txtStock = itemView.findViewById(R.id.txt_stock);
            product_image = itemView.findViewById(R.id.img_product);
            txt_plus = itemView.findViewById(R.id.txt_plus);
            txt_minus = itemView.findViewById(R.id.txt_minus);
            txt_number = itemView.findViewById(R.id.txt_number);
            //  btnAddToCart = itemView.findViewById(R.id.btn_add_cart);
            cardProduct = itemView.findViewById(R.id.card_product);
            txt_Tax = itemView.findViewById(R.id.txt_tax);
        }
    }
}