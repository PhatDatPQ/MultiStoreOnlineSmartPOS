package com.app.superpos.settings;

import static com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype.Slidetop;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.cardview.widget.CardView;

import com.app.superpos.Constant;
import com.app.superpos.R;
import com.app.superpos.login.LoginActivity;
import com.app.superpos.settings.categories.CategoriesActivity;
import com.app.superpos.settings.payment_method.PaymentMethodActivity;
import com.app.superpos.settings.shop.ShopInformationActivity;
import com.app.superpos.utils.BaseActivity;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;

public class SettingsActivity extends BaseActivity {

    SharedPreferences sp;
    SharedPreferences.Editor editor;
    CardView cardShopInfo,cardCategory,cardPaymentMethod,card_logout_method;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sp = getSharedPreferences(Constant.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = sp.edit();
        getSupportActionBar().setHomeButtonEnabled(true); //for back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//for back button
        getSupportActionBar().setTitle(R.string.action_settings);


        cardShopInfo = findViewById(R.id.card_shop_info);
        cardCategory=findViewById(R.id.card_category);
        cardPaymentMethod=findViewById(R.id.card_payment_method);
        card_logout_method=findViewById(R.id.card_logout_method);



        cardShopInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SettingsActivity.this, ShopInformationActivity.class);
                startActivity(intent);
            }
        });


        cardCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(SettingsActivity.this, CategoriesActivity.class);
                startActivity(intent);
            }
        });


        cardPaymentMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SettingsActivity.this, PaymentMethodActivity.class);
                startActivity(intent);
            }
        });

        card_logout_method.setOnClickListener(v -> {


            NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(this);
            dialogBuilder
                    .withTitle(getString(R.string.logout))
                    .withMessage(R.string.want_to_logout_from_app)
                    .withEffect(Slidetop)
                    .withDialogColor("#43a047") //use color code for dialog
                    .withButton1Text(getString(R.string.yes))
                    .withButton2Text(getString(R.string.cancel))
                    .setButton1Click(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            editor.putString(Constant.SP_EMAIL, "");
                            editor.putString(Constant.SP_PASSWORD, "");
                            editor.putString(Constant.SP_USER_NAME, "");
                            editor.putString(Constant.SP_USER_TYPE, "");
                            editor.apply();

                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                            dialogBuilder.dismiss();
                        }
                    })
                    .setButton2Click(v1 -> dialogBuilder.dismiss())
                    .show();


        });

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
}
