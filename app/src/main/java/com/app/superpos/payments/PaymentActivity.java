package com.app.superpos.payments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.app.superpos.Constant;
import com.app.superpos.R;
import com.app.superpos.bt_device.DeviceListActivity;
import com.app.superpos.database.DatabaseAccess;
import com.app.superpos.model.Customer;
import com.app.superpos.model.OrdersSubmitResp;
import com.app.superpos.networking.ApiClient;
import com.app.superpos.networking.ApiInterface;
import com.app.superpos.orders.OrderDetailsActivity;
import com.app.superpos.orders.OrdersActivity;
import com.app.superpos.pos.PosActivity;
import com.app.superpos.utils.BaseActivity;
import com.app.superpos.utils.PrefMng;
import com.app.superpos.utils.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class PaymentActivity extends BaseActivity {
    double order_Price_With_Tax, order_totalTax;
    List<HashMap<String, String>> cartProductList, shopData;
    DatabaseAccess databaseAccess;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    List<String> customerNames, orderTypeNames, paymentMethodNames;
    ArrayAdapter<String> customerAdapter, paymentMethodAdapter;
    String SelectedCardOption, shopID, ownerId, shop_currency;
    private final int REQUEST_CONNECT = 100;
    String[] selectedCustomerNameItem = new String[1];
    String[] SelectedPaymentType = {"None"};
    final boolean[] IsReceived = new boolean[1];
    ImageView img_tick_cash, img_tick_card, img_tick_upi, img_tick_credit;
    ImageButton dialog_btn_close;
    TextView txt_total_tax, et_customerName, txt_total_cost, txt_sub_total_value, txt_balanced_due;
    EditText et_received, et_discount;
    CheckBox chk_received;
    double[] CalCReceived = new double[1];
    List<HashMap<String, String>> order_type, payment_method, lines;
    String shop_name, shop_contact, shop_email, shop_address, currency, discount, currentDate, currentTime,
            customer_name, type, shortText, longText, total_price, order_date, getTax, calculated_total_price, calculated_tax;
    Intent intent = null;
    LinearLayout linear_img_order_payment_method, linerLayout_cash, linerLayout_upi, linerLayout_save, linerLayout_card, linerLayout_CreditCard, linerLayout_submit, linerLayout_print;
    List<Customer> customerData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        getSupportActionBar().hide();
        initDatabase();
        sp = getSharedPreferences(Constant.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        shopID = sp.getString(Constant.SP_SHOP_ID, "");
        ownerId = sp.getString(Constant.SP_OWNER_ID, "");
        getCustomers(shopID, ownerId);
        if (shopData.size() > 0) {
            shop_currency = shopData.get(0).get(Constant.SP_SHOP_CURRENCY);
        }
        editor = sp.edit();
        editor.putString(Constant.CURRENCY_SYMBOL, shop_currency);
        editor.apply();

        linerLayout_cash = findViewById(R.id.linerLayout_cash);
        linerLayout_upi = findViewById(R.id.linerLayout_upi);
        linerLayout_card = findViewById(R.id.linerLayout_card);
        linerLayout_save = findViewById(R.id.linerLayout_save);
        linerLayout_CreditCard = findViewById(R.id.linerLayout_CreditCard);
        linerLayout_submit = findViewById(R.id.linerLayout_submit);

        txt_total_cost = findViewById(R.id.txt_total_cost);
        et_received = findViewById(R.id.et_received);
        txt_balanced_due = findViewById(R.id.txt_balanced_due);
        et_discount = findViewById(R.id.et_discount);
        txt_total_tax = findViewById(R.id.txt_total_tax);
        txt_sub_total_value = findViewById(R.id.txt_sub_total_value);

        img_tick_cash = findViewById(R.id.img_tick_cash);
        img_tick_card = findViewById(R.id.img_tick_card);
        img_tick_upi = findViewById(R.id.img_tick_upi);
        img_tick_credit = findViewById(R.id.img_tick_credit);
        dialog_btn_close = findViewById(R.id.btn_close);
        linear_img_order_payment_method = findViewById(R.id.linear_img_order_payment_method);
        linerLayout_print = findViewById(R.id.linerLayout_print);
        et_customerName = findViewById(R.id.et_customerName);
        chk_received = findViewById(R.id.chk_received);
        linerLayout_print.setOnClickListener(v -> {
            SelectedCardOption = "print";
            if (!SelectedPaymentType[0].equals("None")) {
                linerLayout_print.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                linerLayout_save.setBackground(getResources().getDrawable(R.drawable.round_border));
                linerLayout_submit.setBackground(getResources().getDrawable(R.drawable.round_border));

                img_tick_cash.setImageResource(R.drawable.ic_check_payment);
                img_tick_card.setImageResource(R.drawable.ic_check_payment);
                img_tick_credit.setImageResource(R.drawable.ic_check_payment);
                img_tick_upi.setImageResource(R.drawable.ic_check_payment);
                FindPrinter();
            } else {
                Toasty.error(PaymentActivity.this, "Select At-least one payment method", Toast.LENGTH_SHORT).show();
            }
        });

        chk_received.setOnCheckedChangeListener((buttonView, isChecked) -> {
            IsReceived[0] = isChecked;
            if (isChecked) {
                et_received.setText(txt_total_cost.getText().toString() + "");
            } else {
                et_received.setText("");
            }
        });

        et_discount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                double _discount = 0;
                String get_discount = s.toString();
                if (!get_discount.isEmpty() && !get_discount.equals(".")) {
                    _discount = Double.parseDouble(get_discount);
                    double order_PriceWithDiscount = order_Price_With_Tax - _discount;
                    if (_discount > order_PriceWithDiscount) {
                        et_discount.setError(getString(R.string.discount_cant_be_greater_than_total_price));
                        et_discount.requestFocus();
                        linerLayout_submit.setVisibility(View.INVISIBLE);

                    } else {
                        linerLayout_submit.setVisibility(View.VISIBLE);
                        txt_total_cost.setText(order_PriceWithDiscount + "");
                        order_Price_With_Tax = order_PriceWithDiscount;
                        discount = _discount + "";
                    }

                } else {
                    txt_total_cost.setText(order_Price_With_Tax + "");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        et_received.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String enteredReceived = s.toString();
                if (!enteredReceived.isEmpty() && !enteredReceived.equals(".") && !enteredReceived.equals("null")) {
                    double enterReceived = Double.parseDouble(et_received.getText().toString().replaceAll("[A-Za-z]", ""));
                    CalCReceived[0] = Double.parseDouble(txt_total_cost.getText().toString().replaceAll("[A-Za-z]", "")) - enterReceived;
                    txt_balanced_due.setText(shop_currency + " " + CalCReceived[0] + "");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });


        orderTypeNames = new ArrayList<>();
        for (int i = 0; i < order_type.size(); i++) {
            // Get the ID of selected Country
            orderTypeNames.add(order_type.get(i).get("order_type_name"));
        }

        for (int i = 0; i < payment_method.size(); i++) {

            // Get the ID of selected Country
            paymentMethodNames.add(payment_method.get(i).get("payment_method_name"));
        }


        linear_img_order_payment_method.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                paymentMethodAdapter = new ArrayAdapter<String>(PaymentActivity.this, android.R.layout.simple_list_item_1);
                paymentMethodAdapter.addAll(paymentMethodNames);

                AlertDialog.Builder dialog = new AlertDialog.Builder(PaymentActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_list_search, null);
                dialog.setView(dialogView);
                dialog.setCancelable(false);

                Button dialog_button = (Button) dialogView.findViewById(R.id.dialog_button);
                EditText dialog_input = (EditText) dialogView.findViewById(R.id.dialog_input);
                TextView dialog_title = (TextView) dialogView.findViewById(R.id.dialog_title);
                ListView dialog_list = (ListView) dialogView.findViewById(R.id.dialog_list);

                dialog_title.setText(R.string.select_payment_method);
                dialog_list.setVerticalScrollBarEnabled(true);
                dialog_list.setAdapter(paymentMethodAdapter);

                dialog_input.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                        paymentMethodAdapter.getFilter().filter(charSequence);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });


                final AlertDialog alertDialog = dialog.create();

                dialog_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();


                dialog_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        alertDialog.dismiss();
                        String selectedItem = paymentMethodAdapter.getItem(position);

                    }
                });
            }


        });

        et_customerName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customerAdapter = new ArrayAdapter<String>(PaymentActivity.this, android.R.layout.simple_list_item_1);
                if (customerNames != null) {
                    customerAdapter.addAll(customerNames);
                }

                AlertDialog.Builder dialog = new AlertDialog.Builder(PaymentActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_list_search, null);
                dialog.setView(dialogView);
                dialog.setCancelable(false);

                Button dialog_button = (Button) dialogView.findViewById(R.id.dialog_button);
                EditText dialog_input = (EditText) dialogView.findViewById(R.id.dialog_input);
                TextView dialog_title = (TextView) dialogView.findViewById(R.id.dialog_title);
                ListView dialog_list = (ListView) dialogView.findViewById(R.id.dialog_list);
                dialog_title.setText(R.string.select_customer);
                dialog_list.setVerticalScrollBarEnabled(true);
                dialog_list.setAdapter(customerAdapter);

                dialog_input.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                        customerAdapter.getFilter().filter(charSequence);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });


                final AlertDialog alertDialog = dialog.create();

                dialog_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();


                dialog_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        alertDialog.dismiss();
                        selectedCustomerNameItem[0] = customerAdapter.getItem(position);
                        et_customerName.setText(selectedCustomerNameItem[0]);
                        customer_name = selectedCustomerNameItem[0];
                    }
                });
            }
        });


        linerLayout_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectedCardOption = "preview";
                if (SelectedPaymentType[0].equals("Cash")) {
                    String receivedAmount = et_received.getText().toString().replaceAll("[A-Za-z]", "");
                    if (!receivedAmount.isEmpty()) {

                        double enteredReceivedAmount = Double.parseDouble(et_received.getText().toString().replaceAll("[A-Za-z]", ""));
                        double finalTotalPrice = Double.parseDouble(txt_total_cost.getText().toString().replaceAll("[A-Za-z]", ""));
                        boolean IsReceivedAmountAndTotalAmount = enteredReceivedAmount >= finalTotalPrice;
                        if (IsReceivedAmountAndTotalAmount) {
                            String discount = et_discount.getText().toString().trim();
                            if (discount.isEmpty()) {
                                et_discount.setText("0.0");
                            } else {
                                et_discount.setText(discount);
                            }
                            proceedOrder();
                            linerLayout_submit.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                            img_tick_cash.setImageResource(R.drawable.ic_check_payment);
                            img_tick_card.setImageResource(R.drawable.ic_check_payment);
                            img_tick_credit.setImageResource(R.drawable.ic_check_payment);
                            img_tick_upi.setImageResource(R.drawable.ic_check_payment);

                        } else {
                            Toasty.error(PaymentActivity.this, "Received amount should match or more than total amount", Toasty.LENGTH_LONG).show();
                        }
                    } else {
                        Toasty.error(PaymentActivity.this, "Received Amount is Mandatory ", Toasty.LENGTH_LONG).show();
                    }
                } else {
                    if (selectedCustomerNameItem[0] != null && !selectedCustomerNameItem[0].isEmpty()) {
                        if (!SelectedPaymentType[0].equals("None")) {


                            String discount = et_discount.getText().toString().trim();
                            if (discount.isEmpty()) {
                                discount = "0.00";
                            }

                            proceedOrder();
                            linerLayout_submit.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                            img_tick_cash.setImageResource(R.drawable.ic_check_payment);
                            img_tick_card.setImageResource(R.drawable.ic_check_payment);
                            img_tick_credit.setImageResource(R.drawable.ic_check_payment);
                            img_tick_upi.setImageResource(R.drawable.ic_check_payment);
                        } else {
                            Toasty.error(PaymentActivity.this, "Select At-least one payment method", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toasty.error(PaymentActivity.this, "Enter Customer Name", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        linerLayout_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectedCardOption = "save";
                if (SelectedPaymentType[0].equals("Cash")) {
                    String receivedAmount = et_received.getText().toString().replaceAll("[A-Za-z]", "");
                    if (!receivedAmount.isEmpty()) {
                        double enteredReceivedAmount = Double.parseDouble(et_received.getText().toString().replaceAll("[A-Za-z]", ""));
                        double finalTotalPrice = Double.parseDouble(txt_total_cost.getText().toString().replaceAll("[A-Za-z]", ""));
                        boolean IsReceivedAmountAndTotalAmount = enteredReceivedAmount >= finalTotalPrice;
                        if (IsReceivedAmountAndTotalAmount) {
                            linerLayout_save.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                            img_tick_cash.setImageResource(R.drawable.ic_check_payment);
                            img_tick_card.setImageResource(R.drawable.ic_check_payment);
                            img_tick_credit.setImageResource(R.drawable.ic_check_payment);
                            img_tick_upi.setImageResource(R.drawable.ic_check_payment);


                            String discount = et_discount.getText().toString().trim();
                            if (discount.isEmpty()) {
                                discount = "0.00";
                            }

                            proceedOrder();
                        } else {
                            Toasty.error(PaymentActivity.this, "Received amount should match or more than total amount", Toasty.LENGTH_LONG).show();
                        }
                    } else {
                        Toasty.error(PaymentActivity.this, "Received Amount is Mandatory ", Toasty.LENGTH_LONG).show();
                    }

                } else {
                    if (selectedCustomerNameItem[0] != null && !selectedCustomerNameItem[0].isEmpty()) {
                        if (!SelectedPaymentType[0].equals("None")) {
                            linerLayout_save.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                            img_tick_cash.setImageResource(R.drawable.ic_check_payment);
                            img_tick_card.setImageResource(R.drawable.ic_check_payment);
                            img_tick_credit.setImageResource(R.drawable.ic_check_payment);
                            img_tick_upi.setImageResource(R.drawable.ic_check_payment);


                            String discount = et_discount.getText().toString().trim();
                            if (discount.isEmpty()) {
                                discount = "0.00";
                            }

                            proceedOrder();
                        } else {
                            Toasty.error(PaymentActivity.this, "Select At-least one payment method", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toasty.error(PaymentActivity.this, "Enter Customer Name", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

        linerLayout_upi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!et_customerName.getText().toString().isEmpty()) {
                    SelectedPaymentType[0] = "UPI";
                    linerLayout_save.setBackground(getResources().getDrawable(R.drawable.round_border));
                    linerLayout_print.setBackground(getResources().getDrawable(R.drawable.round_border));
                    linerLayout_submit.setBackground(getResources().getDrawable(R.drawable.round_border));
                    img_tick_cash.setImageResource(R.drawable.ic_check_payment);
                    img_tick_card.setImageResource(R.drawable.ic_check_payment);
                    img_tick_credit.setImageResource(R.drawable.ic_check_payment);
                    img_tick_upi.setImageResource(R.drawable.ic_tick_payement);
                } else {
                    Toasty.error(PaymentActivity.this, R.string.enter_customer_name).show();
                }
            }
        });
        linerLayout_cash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectedPaymentType[0] = "Cash";
                linerLayout_save.setBackground(getResources().getDrawable(R.drawable.round_border));
                linerLayout_print.setBackground(getResources().getDrawable(R.drawable.round_border));
                linerLayout_submit.setBackground(getResources().getDrawable(R.drawable.round_border));

                img_tick_cash.setImageResource(R.drawable.ic_tick_payement);
                img_tick_card.setImageResource(R.drawable.ic_check_payment);
                img_tick_credit.setImageResource(R.drawable.ic_check_payment);
                img_tick_upi.setImageResource(R.drawable.ic_check_payment);
            }
        });
        linerLayout_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!et_customerName.getText().toString().isEmpty()) {
                    SelectedPaymentType[0] = "Card";
                    linerLayout_save.setBackground(getResources().getDrawable(R.drawable.round_border));
                    linerLayout_print.setBackground(getResources().getDrawable(R.drawable.round_border));
                    linerLayout_submit.setBackground(getResources().getDrawable(R.drawable.round_border));
                    img_tick_cash.setImageResource(R.drawable.ic_check_payment);
                    img_tick_card.setImageResource(R.drawable.ic_tick_payement);
                    img_tick_credit.setImageResource(R.drawable.ic_check_payment);
                    img_tick_upi.setImageResource(R.drawable.ic_check_payment);

                } else {
                    Toasty.error(PaymentActivity.this, R.string.enter_customer_name).show();
                }
            }
        });
        linerLayout_CreditCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!et_customerName.getText().toString().isEmpty()) {
                    SelectedPaymentType[0] = "Card";
                    linerLayout_save.setBackground(getResources().getDrawable(R.drawable.round_border));
                    linerLayout_print.setBackground(getResources().getDrawable(R.drawable.round_border));
                    linerLayout_submit.setBackground(getResources().getDrawable(R.drawable.round_border));
                    img_tick_cash.setImageResource(R.drawable.ic_check_payment);
                    img_tick_card.setImageResource(R.drawable.ic_check_payment);
                    img_tick_credit.setImageResource(R.drawable.ic_tick_payement);
                    img_tick_upi.setImageResource(R.drawable.ic_check_payment);

                } else {
                    Toasty.error(PaymentActivity.this, R.string.enter_customer_name).show();
                }
            }
        });
        dialog_btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectedPaymentType[0] = "None";
                finish();
            }
        });
        calculateValues();
    }

    private void initDatabase() {
        databaseAccess = DatabaseAccess.getInstance(PaymentActivity.this);
        shopData = databaseAccess.getShopInformation();
        order_type = databaseAccess.getOrderType();
        paymentMethodNames = new ArrayList<>();
        payment_method = databaseAccess.getPaymentMethod();
    }

    void FindPrinter() {
        //Check if the Bluetooth is available and on.
        if (!Tools.isBlueToothOn(PaymentActivity.this)) return;
        PrefMng.saveActivePrinter(PaymentActivity.this, PrefMng.PRN_WOOSIM_SELECTED);
        //Pick a Bluetooth device
        Intent i = new Intent(PaymentActivity.this, DeviceListActivity.class);
        startActivityForResult(i, REQUEST_CONNECT);
    }

    public void proceedOrder() {
        databaseAccess.open();
        int itemCount = databaseAccess.getCartItemCount();

        if (itemCount > 0) {
            databaseAccess.open();
            //get data from local database
            lines = databaseAccess.getCartProduct();

            if (lines.isEmpty()) {
                Toasty.error(PaymentActivity.this, R.string.no_product_found, Toast.LENGTH_SHORT).show();
            } else {

                //get current timestamp

                currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(new Date());
                //H denote 24 hours and h denote 12 hour hour format
                currentTime = new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date()); //HH:mm:ss a

                //timestamp use for invoice id for unique
                Long tsLong = System.currentTimeMillis() / 1000;
                String timeStamp = tsLong.toString();
                Log.d("Time", timeStamp);

                final JSONObject obj = new JSONObject();
                try {
                    obj.put(Constant.SP_SHOP_ID, shopID);
                    obj.put(Constant.SP_OWNER_ID, ownerId);
                    obj.put(Constant.ORDER_DATE, currentDate);
                    obj.put(Constant.ORDER_TIME, currentTime);
                    obj.put(Constant.ORDER_TYPE, type);
                    obj.put(Constant.ORDER_PAYMENT_METHOD, SelectedPaymentType[0]);
                    obj.put(Constant.CUSTOMER_NAME, customer_name);
                    obj.put(Constant.TAX, order_totalTax);
                    obj.put(Constant.DISCOUNT, discount);

                    editor = sp.edit();
                    editor.putString(Constant.TAX, order_totalTax + "");
                    editor.putString(Constant.ORDER_DATE, currentDate + "");
                    editor.putString(Constant.ORDER_TIME, currentTime + "");
                    editor.putString(Constant.ORDER_TYPE, SelectedPaymentType[0]);

                    JSONArray array = new JSONArray();

                    for (int i = 0; i < lines.size(); i++) {

                        databaseAccess.open();
                        String product_id = lines.get(i).get("product_id");
                        String product_name = databaseAccess.getProductName(product_id);

                        databaseAccess.open();
                        String weight_unit_id = lines.get(i).get("product_weight_unit");
                        String weight_unit = "";
                        if (weight_unit_id != null && TextUtils.isDigitsOnly(weight_unit_id)) {
                            weight_unit = databaseAccess.getWeightUnitName(weight_unit_id);
                        }

                        databaseAccess.open();
                        String product_image = databaseAccess.getProductImage(product_id);

                        JSONObject objp = new JSONObject();
                        objp.put(Constant.PRODUCT_ID, product_id);
                        objp.put(Constant.PRODUCT_NAME, lines.get(i).get(Constant.PRODUCT_NAME));
                        objp.put(Constant.PRODUCT_WEIGHT, lines.get(i).get(Constant.PRODUCT_WEIGHT) + "" + weight_unit);
                        objp.put(Constant.PRODUCT_QTY, lines.get(i).get(Constant.PRODUCT_QTY));
                        objp.put(Constant.PRODUCT_STOCK, lines.get(i).get(Constant.stock));
                        objp.put(Constant.PRODUCT_PRICE, lines.get(i).get(Constant.PRODUCT_PRICE));
                        objp.put(Constant.PRODUCT_IMAGE, product_image);
                        objp.put(Constant.PRODUCT_ORDER_DATE, currentDate);
                        objp.put(Constant.TAX, lines.get(i).get(Constant.TAX));
                        array.put(objp);
                    }
                    obj.put("lines", array);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                orderSubmit(obj);
            }

        } else {
            Toasty.error(PaymentActivity.this, R.string.no_product_in_cart, Toast.LENGTH_SHORT).show();
        }
    }

    private void orderSubmit(final JSONObject obj) {
        Log.d("Json", obj.toString());
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();
        RequestBody body2 = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), String.valueOf(obj));
        Call<OrdersSubmitResp> call = apiInterface.submitOrders(body2);

        call.enqueue(new Callback<OrdersSubmitResp>() {
            @Override
            public void onResponse(@NonNull Call<OrdersSubmitResp> call, @NonNull Response<OrdersSubmitResp> response) {

                if (response.isSuccessful()) {
                    OrdersSubmitResp ordersSubmitResp = response.body();

                    if (ordersSubmitResp != null) {
                        editor.putString(Constant.INVOICE_ID, ordersSubmitResp.getInvoiceId() + "");
                        editor.apply();
                        saveOrderInOfflineDb(obj);
                        Toasty.success(PaymentActivity.this, R.string.order_successfully_done, Toast.LENGTH_SHORT).show();
                        databaseAccess.open();
                        databaseAccess.emptyCart();
                        dialogSuccess();
                    } else {

                    }
                    progressDialog.dismiss();

                } else {

                    Toasty.error(PaymentActivity.this, R.string.error, Toast.LENGTH_SHORT).show();

                    progressDialog.dismiss();
                    Log.d("error", response.toString());

                }


            }

            @Override
            public void onFailure(@NonNull Call<OrdersSubmitResp> call, @NonNull Throwable t) {

                Log.d("onFailure", t.toString());
                Toasty.error(PaymentActivity.this, R.string.something_went_wrong, Toasty.LENGTH_SHORT).show();
                progressDialog.dismiss();

            }
        });


    }

    private void saveOrderInOfflineDb(final JSONObject obj) {
        String invoiceId = sp.getString(Constant.INVOICE_ID, "0");
        databaseAccess.open();
        databaseAccess.createOrderListTable();
        boolean isOrderInserted = databaseAccess.insertOrder(invoiceId, obj);
        Toasty.success(this, R.string.order_done_successful, Toast.LENGTH_SHORT).show();
        shop_name = shopData.get(0).get("shop_name");
        shop_contact = shopData.get(0).get("shop_contact");
        shop_email = shopData.get(0).get("shop_email");
        shop_address = shopData.get(0).get("shop_address");
        currency = shopData.get(0).get("shop_currency");

        //for pdf report

        shortText = "Customer Name: Mr/Mrs. " + customer_name;

        longText = "Thanks for purchase. Visit again";

        try {
            JSONArray array = obj.getJSONArray("lines");
            for (int i = 0; i <= array.length() - 1; i++) {

                String prod_id = array.getJSONObject(i).getString("product_id");
                databaseAccess.open();
                databaseAccess.updateCartProductSubmitted("1", prod_id);
            }
            if (SelectedCardOption.equals("preview")) {
                intent = new Intent(PaymentActivity.this, OrderDetailsActivity.class);
                editor.putString(Constant.ORDER_ID, invoiceId);
                editor.putString(Constant.ORDER_PRICE, order_Price_With_Tax + "");
                editor.putString(Constant.ORDER_DATE, order_date + "");
                editor.putString(Constant.ORDER_TIME, invoiceId);
                editor.putString(Constant.CUSTOMER_NAME, customer_name + "");
                editor.putString(Constant.TAX, getTax + "");
                editor.putString(Constant.calculatedTotalPrice, order_Price_With_Tax + "");
                editor.putString(Constant.TotalPriceWithTax, order_Price_With_Tax + "");
                editor.putString(Constant.calculatedTotal_Tax, calculated_tax + "");
                editor.putString(Constant.DISCOUNT, discount);
                editor.apply();
            } else {
                intent = new Intent(PaymentActivity.this, PosActivity.class);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void dialogSuccess() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(PaymentActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_success, null);
        dialog.setView(dialogView);
        dialog.setCancelable(false);

        ImageButton dialogBtnCloseDialog = dialogView.findViewById(R.id.btn_close_dialog);
        Button dialogBtnViewAllOrders = dialogView.findViewById(R.id.btn_view_all_orders);

        AlertDialog alertDialogSuccess = dialog.create();

        dialogBtnCloseDialog.setOnClickListener(v -> {

            alertDialogSuccess.dismiss();

            Intent intent = new Intent(PaymentActivity.this, PosActivity.class);
            startActivity(intent);
            finish();

        });


        dialogBtnViewAllOrders.setOnClickListener(v -> {

            alertDialogSuccess.dismiss();

            Intent intent = new Intent(PaymentActivity.this, OrdersActivity.class);
            startActivity(intent);
            finish();

        });

        alertDialogSuccess.show();


    }

    private void calculateValues() {
        cartProductList = databaseAccess.getCartProduct();

        double subTotal = 0.0, salesTax = 0.0, totalSalesTax = 0.0;
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
        txt_sub_total_value.setText(subTotal + "");
        txt_total_tax.setText(order_totalTax + "");
        order_Price_With_Tax = subTotal + order_totalTax;
        txt_total_cost.setText(order_Price_With_Tax + "");
    }

    public void getCustomers(String shopId, String ownerId) {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<List<Customer>> call;
        call = apiInterface.getCustomers("", shopId, ownerId);
        call.enqueue(new Callback<List<Customer>>() {
            @Override
            public void onResponse(@NonNull Call<List<Customer>> call, @NonNull Response<List<Customer>> response) {


                if (response.isSuccessful() && response.body() != null) {

                    customerData = response.body();

                    customerNames = new ArrayList<>();

                    for (int i = 0; i < customerData.size(); i++) {

                        customerNames.add(customerData.get(i).getCustomerName());

                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Customer>> call, @NonNull Throwable t) {

                //write own action
            }
        });


    }

}