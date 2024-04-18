package com.example.mobile.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mobile.R;
import com.example.mobile.api.ApiService;
import com.example.mobile.currentUser;
import com.example.mobile.model.item;
import com.example.mobile.model.postReview;
import com.example.mobile.model.product;
import com.example.mobile.model.receivedOrder;
import com.example.mobile.model.review;
import com.example.mobile.popupController.ThanksForRatingDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewManagement extends AppCompatActivity {
    private TextView tvProName, tvPrice, tvDes, tvCusName;
    private TextInputEditText tvContent;
    private RatingBar ratingBar;
    private Button btnSend;
    private ImageView ivExit;
    private List<item> itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_management);

        initView();
        itemList = new ArrayList<>();
        for(item item : currentUser.order.getItems()) {
            if(!item.isReviewed()) {
                itemList.add(item);
            }
        }
        setInfoProducts();
    }

    private void setInfoProducts() {
        if (itemList != null && itemList.size() > 0) {
            product product = itemList.get(itemList.size()-1).getProduct();
            tvProName.setText(product.getProductName());
            DecimalFormat df = new DecimalFormat("#,###");
            String formattedPrice = df.format(Double.parseDouble(String.valueOf(product.getProductPrice())));
            tvPrice.setText(formattedPrice + "đ");
            tvDes.setText(product.getDescription());
        }
        else {
            System.out.print("productList is empty");
        }
    }


    private void initView() {
        tvProName = findViewById(R.id.tvProName);
        tvPrice = findViewById(R.id.tvPrice);
        tvDes = findViewById(R.id.tvDes);
        tvCusName = findViewById(R.id.tvCusName);
        tvContent = findViewById(R.id.tvContent);
        ratingBar = findViewById(R.id.ratingBar);
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);
        btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(v -> {
            System.out.println("itemList Size = " + itemList.size());
            String comment = tvContent.getText().toString();
            if (ratingBar.getRating() > 0 && comment != null && comment.trim().length() > 0) {
                String content = tvContent.getText().toString();
                int rating = ratingBar.getNumStars();
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                Date date = new Date();
                String orderDate = formatter.format(date);
                postReview review = new postReview(rating, content, orderDate);
                System.out.println("You reviewed item " + itemList.get(itemList.size()-1).getId());
                ApiService.apiService.addReview(currentUser.currentCustomer.getId(), itemList.get(itemList.size()-1).getId(), review)
                        .enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(ReviewManagement.this, "Add review successful", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    System.out.println("Error code: " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                System.out.println("Throwable: " + t);
                            }
                        });
                itemList.remove(itemList.size()-1);
                ratingBar.setRating(5);
                tvContent.setText("");

            }

            if (itemList.size() > 0) {
                setInfoProducts();
            }
            else {
                ThanksForRatingDialog dialog = new ThanksForRatingDialog(ReviewManagement.this);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
                dialog.setCancelable(false);
                dialog.show();
            }
        });
        ivExit = findViewById(R.id.ivExit);
        ivExit.setOnClickListener(v -> {
            finish();
        });
    }

}