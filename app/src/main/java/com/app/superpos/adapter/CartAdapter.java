package com.app.superpos.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
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
import androidx.recyclerview.widget.RecyclerView;

import com.app.superpos.Constant;
import com.app.superpos.R;
import com.app.superpos.database.DatabaseAccess;
import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.MyViewHolder> {


    private List<HashMap<String, String>> cart_product;
    private Context context;
    MediaPlayer player;
    TextView txt_total_price, txt_no_product, txt_total_price_with_tax, txt_total_tax;
    public Double total_price, total_price_with_tax, total_tax;
    Button btnSubmitOrder;
    ImageView imgNoProduct;
    DecimalFormat f;
    SharedPreferences sp;

    String currency;

    public CartAdapter(Context context, List<HashMap<String, String>> cart_product, TextView txt_total_price, TextView txt_total_price_with_tax, TextView txt_total_tax, Button btnSubmitOrder, ImageView imgNoProduct, TextView txt_no_product) {
        this.context = context;
        this.cart_product = cart_product;
        player = MediaPlayer.create(context, R.raw.delete_sound);
        this.txt_total_price = txt_total_price;
        this.txt_total_tax = txt_total_tax;
        this.btnSubmitOrder = btnSubmitOrder;
        this.imgNoProduct = imgNoProduct;
        this.txt_no_product = txt_no_product;
        f = new DecimalFormat("#0.00");
        this.txt_total_price_with_tax = txt_total_price_with_tax;
        sp = context.getSharedPreferences(Constant.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        currency = sp.getString(Constant.SP_CURRENCY_SYMBOL, "");
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_product_items, parent, false);
        return new MyViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

        final DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);
        databaseAccess.open();

        final String cart_id = cart_product.get(position).get(Constant.CART_ID);
        String product_id = cart_product.get(position).get(Constant.PRODUCT_ID);
        String product_name = cart_product.get(position).get(Constant.PRODUCT_NAME);
        final String price = cart_product.get(position).get(Constant.PRODUCT_PRICE);
        final String weight_unit_id = cart_product.get(position).get(Constant.PRODUCT_WEIGHT_UNIT_ID);
        final String product_weight_unit = cart_product.get(position).get(Constant.PRODUCT_WEIGHT_UNIT);
        final String weight = cart_product.get(position).get(Constant.PRODUCT_WEIGHT);
        final String qty = cart_product.get(position).get(Constant.PRODUCT_QTY);
        final String stock = cart_product.get(position).get(Constant.PRODUCT_STOCK);
        final String tax = cart_product.get(position).get(Constant.TAX);
        int getStock;
        if (stock != null) {
            getStock = Integer.parseInt(stock);
        } else {
            getStock = 0;
        }
        databaseAccess.open();
        String base64Image = databaseAccess.getProductImage(product_id);
        databaseAccess.open();

        if (base64Image != null) {
            if (base64Image.isEmpty() || base64Image.length() < 6) {
                holder.imgProduct.setImageResource(R.drawable.image_placeholder);
            } else {
                byte[] bytes = Base64.decode(base64Image, Base64.DEFAULT);
                holder.imgProduct.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));

            }
        }


        final double getPrice = Double.parseDouble(price) * Integer.parseInt(qty);

        holder.txtItemName.setText(product_name);
        holder.txtPrice.setText(currency + " " + f.format(getPrice));
        holder.txtWeight.setText(weight + " " + product_weight_unit);
        holder.txtQtyNumber.setText(qty);
        holder.txt_tax.setText("Tax: " + tax);
        holder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);
                databaseAccess.open();
                boolean deleteProduct = databaseAccess.deleteProductFromCart(cart_id);

                if (deleteProduct) {
                    Toasty.success(context, context.getString(R.string.product_removed_from_cart), Toast.LENGTH_SHORT).show();
                    player.start();
                    cart_product.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());

                } else {
                    Toasty.error(context, context.getString(R.string.failed), Toast.LENGTH_SHORT).show();
                }


                databaseAccess.open();
                int itemCount = databaseAccess.getCartItemCount();
                Log.d("itemCount", "" + itemCount);
                if (itemCount <= 0) {
                    txt_total_price.setVisibility(View.GONE);
                    txt_total_price_with_tax.setVisibility(View.GONE);
                    txt_total_tax.setVisibility(View.GONE);
                    btnSubmitOrder.setVisibility(View.GONE);

                    imgNoProduct.setVisibility(View.VISIBLE);
                    txt_no_product.setVisibility(View.VISIBLE);
                }
                calculateValues();
            }

        });
        holder.txtPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qty1 = holder.txtQtyNumber.getText().toString();
                int get_qty = Integer.parseInt(qty1);

                if (get_qty >= getStock) {
                    Toasty.error(context, context.getString(R.string.available_stock) + " " + getStock, Toast.LENGTH_SHORT).show();
                } else {
                    get_qty++;
                    double cost = Double.parseDouble(price) * get_qty;
                    holder.txtPrice.setText(currency + " " + f.format(cost));
                    holder.txtQtyNumber.setText("" + get_qty);
                    DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);
                    databaseAccess.open();
                    databaseAccess.updateProductQty(cart_id, "" + get_qty);
                    calculateValues();
                }
            }
        });
        holder.txtMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qty = holder.txtQtyNumber.getText().toString();
                int get_qty = Integer.parseInt(qty);

                if (get_qty >= 2) {
                    get_qty--;
                    double cost = Double.parseDouble(price) * get_qty;
                    holder.txtPrice.setText(currency + " " + f.format(cost));
                    holder.txtQtyNumber.setText("" + get_qty);
                    DatabaseAccess databaseAccess = DatabaseAccess.getInstance(context);
                    databaseAccess.open();
                    databaseAccess.updateProductQty(cart_id, "" + get_qty);
                    calculateValues();
                }


            }
        });
    }


    @Override
    public int getItemCount() {
        return cart_product.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtItemName, txtPrice, txtWeight, txtQtyNumber, txt_tax;
        ImageView imgDelete, imgProduct, txtPlus, txtMinus;

        public MyViewHolder(View itemView) {
            super(itemView);
            txtItemName = itemView.findViewById(R.id.txt_item_name);
            txtPrice = itemView.findViewById(R.id.txt_price);
            txtWeight = itemView.findViewById(R.id.txt_weight);
            txtQtyNumber = itemView.findViewById(R.id.txt_number);
            imgProduct = itemView.findViewById(R.id.cart_product_image);
            imgDelete = itemView.findViewById(R.id.img_delete);
            txtMinus = itemView.findViewById(R.id.txt_minus);
            txtPlus = itemView.findViewById(R.id.txt_plus);
            txt_tax = itemView.findViewById(R.id.txt_tax);

        }


    }

    public void calculateValues() {
        DatabaseAccess.getInstance(context).open();
        cart_product = DatabaseAccess.getInstance(context).getCartProduct();

        double subTotal = 0.0, totalTax = 0.0, totalPriceWithTax = 0.0, salesTax, totalSalesTax = 0.0, order_totalTax = 0.0;
        for (int i = 0; i <= cart_product.size() - 1; i++) {
            double qty = Double.parseDouble(cart_product.get(i).get(Constant.PRODUCT_QTY));
            double price = Double.parseDouble(cart_product.get(i).get(Constant.PRODUCT_PRICE));

            double totalPrice = price * qty;
            subTotal = subTotal + totalPrice;
            if (cart_product.get(i).get(Constant.TAX) != null || cart_product.get(i).get(Constant.TAX).isEmpty()) {
                salesTax = Double.parseDouble(cart_product.get(i).get(Constant.TAX));
                totalSalesTax = salesTax * qty;
                order_totalTax = totalSalesTax + order_totalTax;
            }
        }
        totalPriceWithTax = subTotal + order_totalTax;
        txt_total_price.setText(context.getString(R.string.sub_total) + " " + currency + " " + f.format(subTotal));
        txt_total_tax.setText(context.getString(R.string.total_tax) + " " + currency + " " + f.format(order_totalTax));
        txt_total_price_with_tax.setText(context.getString(R.string.total_price) + " " + currency + " " + f.format(totalPriceWithTax));
    }
}
