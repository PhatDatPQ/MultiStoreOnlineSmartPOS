package com.app.superpos.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import com.app.superpos.customers.EditCustomersActivity;
import com.app.superpos.model.Customer;
import com.app.superpos.networking.ApiClient;
import com.app.superpos.networking.ApiInterface;
import com.app.superpos.utils.Utils;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;

import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype.Slidetop;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.MyViewHolder> {
    private List<Customer> customerData;
    private Context context;
    Utils utils;


    public CustomerAdapter(Context context, List<Customer> customerData) {
        this.context = context;
        this.customerData = customerData;
        utils = new Utils();

    }

    @NonNull
    @Override
    public CustomerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_item, parent, false);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final CustomerAdapter.MyViewHolder holder, int position) {

        final String customer_id = customerData.get(position).getCustomerId();
        String name = customerData.get(position).getCustomerName();
        String cell = customerData.get(position).getCustomerCell();
        String email = customerData.get(position).getCustomerEmail();
        String address = customerData.get(position).getCustomerAddress();

        holder.txtCustomerName.setText(name);
        holder.txtCell.setText(cell);
        holder.txtEmail.setText(email);
        holder.txtAddress.setText(address);


        holder.imgCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                String phone = "tel:" + cell;
                callIntent.setData(Uri.parse(phone));
                context.startActivity(callIntent);
            }
        });

        holder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                NiftyDialogBuilder dialogBuilder = NiftyDialogBuilder.getInstance(context);
                dialogBuilder
                        .withTitle(context.getString(R.string.delete))
                        .withMessage(context.getString(R.string.want_to_delete_customer))
                        .withEffect(Slidetop)
                        .withDialogColor("#43a047") //use color code for dialog
                        .withButton1Text(context.getString(R.string.yes))
                        .withButton2Text(context.getString(R.string.cancel))
                        .setButton1Click(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {


                                if (utils.isNetworkAvailable(context)) {
                                    deleteCustomer(customer_id);
                                    customerData.remove(holder.getAdapterPosition());
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
                Intent i = new Intent(context, EditCustomersActivity.class);
                i.putExtra("customer_id", customer_id);
                i.putExtra("customer_name", name);
                i.putExtra("customer_cell", cell);
                i.putExtra("customer_email", email);
                i.putExtra("customer_address", address);
                i.putExtra("customer_isEdit", true);
                context.startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        return customerData.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txtCustomerName, txtCell, txtEmail, txtAddress;
        ImageView imgDelete, imgCall,img_edit;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtCustomerName = itemView.findViewById(R.id.txt_customer_name);
            txtCell = itemView.findViewById(R.id.txt_cell);
            txtEmail = itemView.findViewById(R.id.txt_email);
            txtAddress = itemView.findViewById(R.id.txt_address);

            imgDelete = itemView.findViewById(R.id.img_delete);
            imgCall = itemView.findViewById(R.id.img_call);
            img_edit = itemView.findViewById(R.id.img_edit);


           // itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(context, EditCustomersActivity.class);
            i.putExtra("customer_id", customerData.get(getAdapterPosition()).getCustomerId());
            i.putExtra("customer_name", customerData.get(getAdapterPosition()).getCustomerName());
            i.putExtra("customer_cell", customerData.get(getAdapterPosition()).getCustomerCell());
            i.putExtra("customer_email", customerData.get(getAdapterPosition()).getCustomerEmail());
            i.putExtra("customer_address", customerData.get(getAdapterPosition()).getCustomerAddress());
            i.putExtra("customer_isEdit", false);
            context.startActivity(i);
        }
    }


    //delete from server
    private void deleteCustomer(String customerId) {

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        Call<Customer> call = apiInterface.deleteCustomer(customerId);
        call.enqueue(new Callback<Customer>() {
            @Override
            public void onResponse(@NonNull Call<Customer> call, @NonNull Response<Customer> response) {


                if (response.isSuccessful() && response.body() != null) {

                    String value = response.body().getValue();

                    if (value.equals(Constant.KEY_SUCCESS)) {
                        Toasty.error(context, R.string.customer_deleted, Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();

                    } else if (value.equals(Constant.KEY_FAILURE)) {
                        Toasty.error(context, R.string.error, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, R.string.no_network_connection, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Customer> call, Throwable t) {
                Toast.makeText(context, "Error! " + t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
