package com.app.superpos;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import static com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype.Slidetop;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.superpos.adapter.HomeModuleAdapter;
import com.app.superpos.customers.CustomersActivity;
import com.app.superpos.expense.ExpenseActivity;
import com.app.superpos.interfaces.IHomeGridClickListener;
import com.app.superpos.login.LoginActivity;
import com.app.superpos.model.HomeModuleGrid;
import com.app.superpos.model.MonthData;
import com.app.superpos.networking.ApiClient;
import com.app.superpos.networking.ApiInterface;
import com.app.superpos.orders.OrdersActivity;
import com.app.superpos.pos.PosActivity;
import com.app.superpos.product.ProductActivity;
import com.app.superpos.report.GraphReportActivity;
import com.app.superpos.report.ReportActivity;
import com.app.superpos.settings.SettingsActivity;
import com.app.superpos.suppliers.SuppliersActivity;
import com.app.superpos.utils.BaseActivity;
import com.app.superpos.utils.LocaleManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.whiteelephant.monthpicker.MonthPickerDialog;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends BaseActivity implements IHomeGridClickListener {
    DecimalFormat f;
    String currency,shopID,ownerId;
    BarChart barChart;
    int mYear;

    ArrayList<BarEntry> barEntries;

    CardView cardCustomers, cardProducts, cardSupplier, cardPos, cardOrderList, cardReport, cardSettings, cardExpense, cardLogout;
    //for double back press to exit
    private static final int TIME_DELAY = 2000;
    private static long backPressed;

    SharedPreferences sp;
    SharedPreferences.Editor editor;
    String userType;
    TextView txtShopName, txtSubText;

    private AdView adView;
    RecyclerView recyViewModuleGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_gradient));
        getSupportActionBar().setElevation(0);
        recyViewModuleGrid = findViewById(R.id.recycler_home_prod);
        recyViewModuleGrid.setAdapter(new HomeModuleAdapter(this, initModuleGridDate(), this));
        barChart = findViewById(R.id.barchart);

        barChart.setDrawBarShadow(false);

        barChart.setDrawValueAboveBar(true);

        barChart.setMaxVisibleValueCount(50);
        barChart.setPinchZoom(false);
        barChart.setDrawGridBackground(true);

        barChart.setVisibility(View.INVISIBLE);
        sp = getSharedPreferences(Constant.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();

        userType = sp.getString(Constant.SP_USER_TYPE, "");
        String shopName = sp.getString(Constant.SP_SHOP_NAME, "");
        String staffName = sp.getString(Constant.SP_STAFF_NAME, "");
        shopID = sp.getString(Constant.SP_SHOP_ID, "");
        ownerId = sp.getString(Constant.SP_OWNER_ID, "");
        currency = sp.getString(Constant.SP_CURRENCY_SYMBOL, "N/A");
        f = new DecimalFormat("#0.00");

        String currentYear = new SimpleDateFormat("yyyy", Locale.ENGLISH).format(new Date());
       // txtSelectYear.setText(getString(R.string.year) + currentYear);

        mYear=Integer.parseInt(currentYear);

        getMonthlySales(shopID,ownerId,currentYear);

        if (SDK_INT >= Build.VERSION_CODES.R) {
            requestPermission();
        }
       //       Admob initialization
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        //  adView = findViewById(R.id.adview);
        AdRequest adRequest = new AdRequest.Builder().build();
       //   adView.loadAd(adRequest);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.language_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {


            case R.id.local_french:
                setNewLocale(this, LocaleManager.FRENCH);
                return true;


            case R.id.local_english:
                setNewLocale(this, LocaleManager.ENGLISH);
                return true;


            case R.id.local_bangla:
                setNewLocale(this, LocaleManager.BANGLA);
                return true;

            case R.id.local_spanish:
                setNewLocale(this, LocaleManager.SPANISH);
                return true;

            case R.id.local_hindi:
                setNewLocale(this, LocaleManager.HINDI);
                return true;
            default:
                Log.d("Default", "default");

        }

        return super.onOptionsItemSelected(item);
    }


    private void setNewLocale(AppCompatActivity mContext, @LocaleManager.LocaleDef String language) {
        LocaleManager.setNewLocale(this, language);
        Intent intent = mContext.getIntent();
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    //double back press to exit
    @Override
    public void onBackPressed() {
        if (backPressed + TIME_DELAY > System.currentTimeMillis()) {

            finishAffinity();

        } else {
            Toasty.info(this, R.string.press_once_again_to_exit,
                    Toast.LENGTH_SHORT).show();
        }
        backPressed = System.currentTimeMillis();
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    private void requestPermission() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.MANAGE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {

                            //write your action if needed
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings

                        }
                    }


                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(error -> Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show())
                .onSameThread()
                .check();
    }


    private ArrayList<HomeModuleGrid> initModuleGridDate() {

        ArrayList<HomeModuleGrid> lstModules = new ArrayList<>();
        lstModules.add(new HomeModuleGrid(R.drawable.customers, R.string.customers));
        lstModules.add(new HomeModuleGrid(R.drawable.suppliers, R.string.suppliers));
        lstModules.add(new HomeModuleGrid(R.drawable.ic_products, R.string.products));
        lstModules.add(new HomeModuleGrid(R.drawable.ic_pos, R.string.pos));
        lstModules.add(new HomeModuleGrid(R.drawable.expense, R.string.expense));
        lstModules.add(new HomeModuleGrid(R.drawable.allorders, R.string.all_orders));
        lstModules.add(new HomeModuleGrid(R.drawable.report, R.string.report));
        lstModules.add(new HomeModuleGrid(R.drawable.analytics, R.string.analytics));
        lstModules.add(new HomeModuleGrid(R.drawable.ic_settings, R.string.settings));

        return lstModules;
    }

    @Override
    public void onItemClickListener(HomeModuleGrid selectedmodule) {
        Intent gotoSelectedModule = null;
        switch (selectedmodule.moduleTitle) {

            case R.string.customers:
                gotoSelectedModule = new Intent(this, CustomersActivity.class);
                break;
            case R.string.suppliers:
                gotoSelectedModule = new Intent(this, SuppliersActivity.class);
                break;

            case R.string.products:
                gotoSelectedModule = new Intent(this, ProductActivity.class);
                break;
            case R.string.pos:
                gotoSelectedModule = new Intent(this, PosActivity.class);
                break;
            case R.string.expense:
                if (userType.equals(Constant.ADMIN) || userType.equals(Constant.MANAGER)) {
                    Intent intent = new Intent(HomeActivity.this, ExpenseActivity.class);
                    startActivity(intent);
                } else {
                    Toasty.error(HomeActivity.this, R.string.you_dont_have_permission_to_access_this_page, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.string.all_orders:
                gotoSelectedModule = new Intent(HomeActivity.this, OrdersActivity.class);
                break;
            case R.string.report:
                if (userType.equals(Constant.ADMIN) || userType.equals(Constant.MANAGER)) {
                    Intent intent = new Intent(HomeActivity.this, ReportActivity.class);
                    startActivity(intent);
                } else {
                    Toasty.error(HomeActivity.this, R.string.you_dont_have_permission_to_access_this_page, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.string.analytics:
                break;
            case R.string.settings:
                if (userType.equals(Constant.ADMIN)) {
                    Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                    startActivity(intent);
                } else {
                    Toasty.error(HomeActivity.this, R.string.you_dont_have_permission_to_access_this_page, Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                gotoSelectedModule = new Intent(this, CustomersActivity.class);
                break;
        }
        if (gotoSelectedModule != null)
            startActivity(gotoSelectedModule);


    }

    public void getGraphData() {

        String[] monthList = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(monthList));
        xAxis.setCenterAxisLabels(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(12);

        BarDataSet barDataSet = new BarDataSet(barEntries, getString(R.string.monthly_sales_report));
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.9f);

        barChart.setData(barData);

        barChart.setScaleEnabled(false);  //for fixed bar chart,no zoom
        //for refresh chart
        barChart.notifyDataSetChanged();
        barChart.invalidate();

    }

    public void getMonthlySales(String shopId,String ownerId,String year) {

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<List<MonthData>> call;
        call = apiInterface.getMonthlySales(shopId,ownerId,year);

        call.enqueue(new Callback<List<MonthData>>() {
            @Override
            public void onResponse(@NonNull Call<List<MonthData>> call, @NonNull Response<List<MonthData>> response) {


                if (response.isSuccessful() && response.body() != null) {
                    List<MonthData> monthDataList;
                    monthDataList = response.body();


                    if (monthDataList.isEmpty()) {


                        Log.d("Data", "Empty");


                    } else {

                        //Stopping Shimmer Effects
                      //  mShimmerViewContainer.stopShimmer();
                       // mShimmerViewContainer.setVisibility(View.GONE);

                        barEntries = new ArrayList<>();
                        float jan = Float.parseFloat(monthDataList.get(0).getJan());
                        float feb = Float.parseFloat(monthDataList.get(0).getFeb());
                        float mar = Float.parseFloat(monthDataList.get(0).getMar());
                        float apr = Float.parseFloat(monthDataList.get(0).getApr());
                        float may = Float.parseFloat(monthDataList.get(0).getMay());
                        float jun = Float.parseFloat(monthDataList.get(0).getJun());
                        float jul = Float.parseFloat(monthDataList.get(0).getJul());
                        float aug = Float.parseFloat(monthDataList.get(0).getAug());
                        float sep = Float.parseFloat(monthDataList.get(0).getSep());
                        float oct = Float.parseFloat(monthDataList.get(0).getOct());
                        float nov = Float.parseFloat(monthDataList.get(0).getNov());
                        float dec = Float.parseFloat(monthDataList.get(0).getDec());


                        float totalTax = monthDataList.get(0).getTotalTax();
                        float totalDiscount = monthDataList.get(0).getTotalDiscount();
                        float totalOrderPrice = monthDataList.get(0).getTotalOrderPrice();


                        barEntries.add(new BarEntry(1, jan));
                        barEntries.add(new BarEntry(2, feb));
                        barEntries.add(new BarEntry(3, mar));
                        barEntries.add(new BarEntry(4, apr));
                        barEntries.add(new BarEntry(5, may));
                        barEntries.add(new BarEntry(6, jun));
                        barEntries.add(new BarEntry(7, jul));
                        barEntries.add(new BarEntry(8, aug));
                        barEntries.add(new BarEntry(9, sep));
                        barEntries.add(new BarEntry(10, oct));
                        barEntries.add(new BarEntry(11, nov));
                        barEntries.add(new BarEntry(12, dec));

                        getGraphData();

                        barChart.setVisibility(View.VISIBLE);
                       // txtTotalSales.setVisibility(View.VISIBLE);


                       // txtTotalSales.setText(getString(R.string.total_sales) + currency + f.format(totalOrderPrice));

                       // txtTotalTax.setText(getString(R.string.total_tax) + ":" + currency + f.format(totalTax));
                       // txtTotalDiscount.setText(getString(R.string.total_discount) + ":" + currency + f.format(totalDiscount));
                        float netSales = totalOrderPrice + totalTax - totalDiscount;

                       // txtNetSales.setText(getString(R.string.net_sales) + currency + f.format(netSales));

                    }

                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MonthData>> call, @NonNull Throwable t) {

                Toast.makeText(HomeActivity.this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                Log.d("Error : ", t.toString());
            }
        });


    }

    private void chooseYearOnly() {
        MonthPickerDialog.Builder builder = new MonthPickerDialog.Builder(this, new MonthPickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(int selectedMonth, int selectedYear) {
               // txtSelectYear.setText(getString(R.string.year)+" "+selectedYear);
                mYear = selectedYear;
                getMonthlySales(shopID,ownerId,""+mYear);

            }
        }, mYear, 0);

        builder.showYearOnly()
                .setYearRange(1990, 2099)
                .build()
                .show();
    }



}
