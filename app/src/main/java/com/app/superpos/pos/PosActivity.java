package com.app.superpos.pos;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.superpos.Constant;
import com.app.superpos.R;
import com.app.superpos.adapter.PosProductAdapter;
import com.app.superpos.adapter.ProductCategoryAdapter;
import com.app.superpos.database.DatabaseAccess;
import com.app.superpos.model.Category;
import com.app.superpos.model.Product;
import com.app.superpos.networking.ApiClient;
import com.app.superpos.networking.ApiInterface;
import com.app.superpos.product.AddProductActivity;
import com.app.superpos.utils.BaseActivity;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PosActivity extends BaseActivity {
    private RecyclerView recyclerView, categoryRecyclerView;
    PosProductAdapter productAdapter;
    TextView txtNoProducts, txtReset;
    ProductCategoryAdapter categoryAdapter;

    ImageView imgNoProduct, imgScanner, imgCart, imgBack;
    public static EditText etxtSearch;
    public static TextView txtCount;

    private ShimmerFrameLayout mShimmerViewContainer;
    DatabaseAccess databaseAccess;
    FloatingActionButton fabAdd;
    LinearLayout liner_no_products;
    SharedPreferences sp;
    String shopID, ownerId;
    List<Category> productCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos);
        getSupportActionBar().setHomeButtonEnabled(true); //for back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//for back button
        getSupportActionBar().setTitle(R.string.all_product);
        getSupportActionBar().hide();

        etxtSearch = findViewById(R.id.etxt_search);
        recyclerView = findViewById(R.id.recycler);
        imgNoProduct = findViewById(R.id.image_no_product);
        txtNoProducts = findViewById(R.id.txt_no_products);
        imgScanner = findViewById(R.id.img_scanner);
        categoryRecyclerView = findViewById(R.id.category_recyclerview);
        txtReset = findViewById(R.id.txt_reset);
        imgBack = findViewById(R.id.img_back);
        imgCart = findViewById(R.id.img_cart);
        txtCount = findViewById(R.id.txt_count);
        fabAdd = findViewById(R.id.fab_add);
        liner_no_products = findViewById(R.id.liner_no_products);
        databaseAccess = DatabaseAccess.getInstance(PosActivity.this);

        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);

        sp = getSharedPreferences(Constant.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        shopID = sp.getString(Constant.SP_SHOP_ID, "");
        ownerId = sp.getString(Constant.SP_OWNER_ID, "");
        imgScanner.setOnClickListener(v -> {
            Intent intent = new Intent(PosActivity.this, ScannerActivity.class);
            startActivity(intent);
        });
        imgNoProduct.setVisibility(View.GONE);
        txtNoProducts.setVisibility(View.GONE);
        txtReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getProductsData("", shopID, ownerId);
            }
        });
        etxtSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                Log.d("data", s.toString());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() > 1) {

                    //search data from server
                    getProductsData(s.toString(), shopID, ownerId);
                } else {
                    getProductsData("", shopID, ownerId);
                }


            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("data", s.toString());
            }


        });
        imgCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(PosActivity.this, ProductCart.class);
                startActivity(intent);
            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(PosActivity.this, AddProductActivity.class);
            startActivity(intent);
        });
        refreshProductList();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_cart, menu);
        return true;
    }


    public void getProductCategory(String shopId, String ownerId) {

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        Call<List<Category>> call;


        call = apiInterface.getCategory(shopId, ownerId);

        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {


                if (response.isSuccessful() && response.body() != null) {
                    productCategory = response.body();

                    if (productCategory.isEmpty()) {
                        Toasty.info(PosActivity.this, R.string.no_data_found, Toast.LENGTH_SHORT).show();
                        imgNoProduct.setImageResource(R.drawable.no_data);


                    } else {

                        categoryAdapter = new ProductCategoryAdapter(PosActivity.this, productCategory, recyclerView, liner_no_products, mShimmerViewContainer);

                        categoryRecyclerView.setAdapter(categoryAdapter);

                    }


                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {

                //write own action
            }
        });


    }


    public void getProductsData(String searchText, String shopId, String ownerId) {

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<List<Product>> call;
        call = apiInterface.getProducts(searchText, shopId, ownerId);

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {


                if (response.isSuccessful() && response.body() != null) {
                    List<Product> productsList;
                    productsList = response.body();
                    if (productsList.isEmpty()) {

                        recyclerView.setVisibility(View.GONE);
                        liner_no_products.setVisibility(View.VISIBLE);
                        imgNoProduct.setVisibility(View.VISIBLE);
                        txtNoProducts.setVisibility(View.VISIBLE);
                        imgNoProduct.setImageResource(R.drawable.not_found);
                        //Stopping Shimmer Effects
                        mShimmerViewContainer.stopShimmer();
                        mShimmerViewContainer.setVisibility(View.GONE);
                    } else {
                        //Stopping Shimmer Effects
                        mShimmerViewContainer.stopShimmer();
                        mShimmerViewContainer.setVisibility(View.GONE);
                        txtNoProducts.setVisibility(View.GONE);
                        liner_no_products.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        imgNoProduct.setVisibility(View.GONE);
                        productAdapter = new PosProductAdapter(PosActivity.this, productsList);
                        recyclerView.setAdapter(productAdapter);
                        categoryAdapter = new ProductCategoryAdapter(PosActivity.this, productCategory, recyclerView, liner_no_products, mShimmerViewContainer);

                        categoryRecyclerView.setAdapter(categoryAdapter);
                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {

                Toast.makeText(PosActivity.this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                Log.d("Error : ", t.toString());
            }
        });


    }


    //to recheck item count when back this activity
    @Override
    protected void onResume() {
        super.onResume();
        refreshProductList();
    }


  public void refreshProductList() {
        databaseAccess.open();
        getProductCategory(shopID, ownerId);
        // set a GridLayoutManager with default vertical orientation and 3 number of columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager); // set LayoutManager to RecyclerView
        recyclerView.setHasFixedSize(true);
        //Load data from server
        getProductsData("", shopID, ownerId);
        // set a GridLayoutManager with default vertical orientation and 3 number of columns
        LinearLayoutManager linerLayoutManager = new LinearLayoutManager(PosActivity.this, LinearLayoutManager.HORIZONTAL, false);
        categoryRecyclerView.setLayoutManager(linerLayoutManager); // set LayoutManager to RecyclerView
        categoryRecyclerView.setHasFixedSize(true);
        int count = databaseAccess.getCartItemCount();
        if (count == 0) {
            txtCount.setVisibility(View.INVISIBLE);
        } else {
            txtCount.setVisibility(View.VISIBLE);
            txtCount.setText(String.valueOf(count));
        }
    }

}
