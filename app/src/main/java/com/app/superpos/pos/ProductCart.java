package com.app.superpos.pos;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.superpos.Constant;
import com.app.superpos.R;
import com.app.superpos.adapter.CartAdapter;
import com.app.superpos.bt_device.DeviceListActivity;
import com.app.superpos.database.DatabaseAccess;
import com.app.superpos.model.Customer;
import com.app.superpos.model.OrdersSubmitResp;
import com.app.superpos.networking.ApiClient;
import com.app.superpos.networking.ApiInterface;
import com.app.superpos.orders.OrdersActivity;
import com.app.superpos.orders.TestPrinter;
import com.app.superpos.payments.PaymentActivity;
import com.app.superpos.utils.BaseActivity;
import com.app.superpos.utils.IPrintToPrinter;
import com.app.superpos.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductCart extends BaseActivity {
    List<HashMap<String, String>> cartProductList;
    SharedPreferences.Editor editor;
    private static final int REQUEST_CONNECT = 100;
    double total_price, calculated_total_price;
    String currency, shop_name, shop_contact, shop_email, shop_address, longText, shortText;
    String order_id, order_date, order_time, tax, discount;
    CartAdapter productCartAdapter;
    ImageView imgNoProduct;
    Button btnSubmitOrder;
    TextView txtNoProduct, txtTotalPrice, txt_total_price_with_tax, txt_total_tax;
    LinearLayout linearLayout;
    DatabaseAccess databaseAccess;

    SharedPreferences sp;
    String shopID, ownerId;
    DecimalFormat f;

    double orderSubTotal = 0.0, orderTotalPriceWithTax = 0.0, order_totalTax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_cart);
        databaseAccess = DatabaseAccess.getInstance(ProductCart.this);
        getSupportActionBar().setHomeButtonEnabled(true); //for back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//for back button
        getSupportActionBar().setTitle(R.string.product_cart);
        f = new DecimalFormat("#0.00");
        sp = getSharedPreferences(Constant.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();

        currency = sp.getString(Constant.SP_CURRENCY_SYMBOL, "");

        shopID = sp.getString(Constant.SP_SHOP_ID, "");
        ownerId = sp.getString(Constant.SP_OWNER_ID, "");
        RecyclerView recyclerView = findViewById(R.id.cart_recyclerview);
        imgNoProduct = findViewById(R.id.image_no_product);
        btnSubmitOrder = findViewById(R.id.btn_submit_order);
        txtNoProduct = findViewById(R.id.txt_no_product);
        linearLayout = findViewById(R.id.linear_layout);
        txtTotalPrice = findViewById(R.id.txt_total_price);
        txt_total_price_with_tax = findViewById(R.id.txt_total_price_with_tax);
        txt_total_tax = findViewById(R.id.txt_total_tax);

        txtNoProduct.setVisibility(View.GONE);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager); // set LayoutManager to RecyclerView
        recyclerView.setHasFixedSize(true);
        databaseAccess.open();
        //get data from local database
        cartProductList = databaseAccess.getCartProduct();

        Log.d("CartSize", "" + cartProductList.size());

        if (cartProductList.isEmpty()) {

            imgNoProduct.setImageResource(R.drawable.empty_cart);
            imgNoProduct.setVisibility(View.VISIBLE);
            txtNoProduct.setVisibility(View.VISIBLE);
            btnSubmitOrder.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            linearLayout.setVisibility(View.GONE);
            txtTotalPrice.setVisibility(View.GONE);
            txt_total_tax.setVisibility(View.GONE);
            txt_total_price_with_tax.setVisibility(View.GONE);
        } else {
            imgNoProduct.setVisibility(View.GONE);
            productCartAdapter = new CartAdapter(ProductCart.this, cartProductList, txtTotalPrice, txt_total_price_with_tax, txt_total_tax, btnSubmitOrder, imgNoProduct, txtNoProduct);
            recyclerView.setAdapter(productCartAdapter);
            calculateValues();
        }


        btnSubmitOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gotoPaymentScreen = new Intent(ProductCart.this, PaymentActivity.class);
                startActivity(gotoPaymentScreen);
            }
        });

        databaseAccess.createProduct();
        databaseAccess.createProductWeights();
        databaseAccess.createCustomers();
    }


    //for back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CONNECT && resultCode == RESULT_OK) {
            try {
                //Get device address to print to.
                String blutoothAddr = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                //The interface to print text to thermal printers.
                //    public TestPrinter(Context context, String shopName, String shopAddress, String shopEmail, String shopContact, String invoiceId, String orderDate, String orderTime, String customerName, String footer, double subTotal, String totalPrice, String tax, String discount, String currency, String served_by, List<OrderDetails> orderDetailsList) {
                IPrintToPrinter testPrinter = new TestPrinter(this, shop_name, shop_address, shop_email, shop_contact, order_id, order_date, order_time, shortText, longText, total_price, calculated_total_price + "", tax, discount, currency, shopID, null);
                //Connect to the printer and after successful connection issue the print command.
                //mPrnMng = printerFactory.createPrnMng(this, blutoothAddr, testPrinter);
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void calculateValues() {
        double subTotal = 0.0, totalTax = 0.0, totalPriceWithTax = 0.0, salesTax = 0.0, totalSalesTax = 0.0;
        for (int i = 0; i <= cartProductList.size() - 1; i++) {
            double qty = Double.parseDouble(cartProductList.get(i).get(Constant.PRODUCT_QTY));
            double price = Double.parseDouble(cartProductList.get(i).get(Constant.PRODUCT_PRICE));

            double totalPrice = price * qty;
            subTotal = subTotal + totalPrice;
            if (cartProductList.get(i).get(Constant.TAX) != null || !cartProductList.get(i).get(Constant.TAX).isEmpty()) {
                salesTax = Double.parseDouble(cartProductList.get(i).get(Constant.TAX));
                totalSalesTax = salesTax * qty;
                order_totalTax = totalSalesTax + order_totalTax;
            }

        }
        txtTotalPrice.setText(getString(R.string.sub_total)+" " + currency + " " + f.format(subTotal));
        orderSubTotal = subTotal;

        txt_total_tax.setText(getString(R.string.total_tax) + currency + " " + f.format(totalSalesTax));

        totalPriceWithTax = subTotal + order_totalTax;

        txt_total_price_with_tax.setText(getString(R.string.total_price) + currency + " " + f.format(totalPriceWithTax));
        orderTotalPriceWithTax = totalPriceWithTax;

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toasty.success(ProductCart.this, "onBackPressed").show();
    }
}

