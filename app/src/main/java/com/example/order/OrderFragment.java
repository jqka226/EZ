package com.example.order;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import task.CommonTask;
import task.ImageTask;

public class OrderFragment extends Fragment {
    private static final String TAG = "TAG_OrderFragment";
    private RecyclerView rvMenu;
    private TextView edTotal, edNote;
    private Activity activity;
    private CommonTask menuGetAllTask;
    private ImageTask menuImageTask;
    private List<Menu> menus;
    private int totalPrice;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SearchView searchView = view.findViewById(R.id.searchView);
        totalPrice = 0;
        edTotal = view.findViewById(R.id.edTotal);
        edTotal.setText(String.valueOf(totalPrice));
        rvMenu = view.findViewById(R.id.rvMenu);
        edNote = view.findViewById(R.id.edNote);
        rvMenu.setLayoutManager(new LinearLayoutManager(activity));
        menus = getMenu();
        showMenu(menus);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    showMenu(menus);
                } else {
                    List<Menu> searchMenu = new ArrayList<>();
                    for (Menu menu : menus) {
                        if (menu.getFOOD_NAME().toUpperCase().contains(newText.toUpperCase())) {
                            searchMenu.add(menu);
                        }
                    }
                    showMenu(searchMenu);
                }
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
        });


        edNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                BottomNavigationView navbar = getActivity().findViewById(R.id.bv);
                navbar.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                BottomNavigationView navbar = getActivity().findViewById(R.id.bv);
                navbar.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                BottomNavigationView navbar = getActivity().findViewById(R.id.bv);
                navbar.setVisibility(View.VISIBLE);
            }
        });
    }

    private List<Menu> getMenu() {
        List<Menu> menus = null;
        if (Common.networkConnected(activity)) {
            String url = Common.URL_SERVER + "/MenuServlet";
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("action", "getAll");
            String jsonOut = jsonObject.toString();
            menuGetAllTask = new CommonTask(url, jsonOut);
            try {
                String jsonIn = menuGetAllTask.execute().get();
                Type ListType = new TypeToken<List<Menu>>() {
                }.getType();
                Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                menus = gson.fromJson(jsonIn, ListType);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        } else {
            Common.showToast(activity, R.string.textNoNetwork);
        }
        return menus;
    }

    private void showMenu(List<Menu> menus) {
        if (menus == null || menus.isEmpty()) {
            Common.showToast(activity, R.string.textNOMenu);
        }
        MenuAdapter menuAdapter = (MenuAdapter) rvMenu.getAdapter();

        if (menuAdapter == null) {
            rvMenu.setAdapter(new MenuAdapter(activity, menus));
        } else {
            menuAdapter.setMenus(menus);
            menuAdapter.notifyDataSetChanged();
        }
    }

    private class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MyViewHolder> {
        private LayoutInflater layoutInflater;
        private List<Menu> menus;
        private int imageSize;
        MenuAdapter(Context context, List<Menu> menus) {
            layoutInflater = LayoutInflater.from(context);
            this.menus = menus;
            imageSize = getResources().getDisplayMetrics().widthPixels / 4;
        }

        void setMenus(List<Menu> menus) {
            this.menus = menus;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            int amount;
            int price;
            ImageView imageView;
            TextView tvName, tvPrice, tvAmount;
            Button btAdd, btLess;


            MyViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.ivMenu);
                tvName = itemView.findViewById(R.id.tvName);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvAmount = itemView.findViewById(R.id.tvAmount);
                amount = Integer.parseInt(tvAmount.getText().toString());
                btAdd = itemView.findViewById(R.id.btadd);
                btAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        amount++;
                        tvAmount.setText(String.valueOf(amount));
                        tvPrice.setText(String.valueOf(amount * price));
                        totalPrice += price;
                        edTotal.setText(String.valueOf(totalPrice));
                    }
                });
                btLess = itemView.findViewById(R.id.btless);
                btLess.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(amount > 0) {
                            amount--;
                            tvAmount.setText(String.valueOf(amount));
                            tvPrice.setText(String.valueOf(amount * price));
                            totalPrice -= price;
                            edTotal.setText(String.valueOf(totalPrice));
                        } else {
                            Common.showToast(activity, R.string.textNOFood);
                        }
                    }
                });
            }

            public void setPrice(int price) {
                this.price = price;
            }
        }

        @Override
        public int getItemCount() {
            return menus.size();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_view_menu, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final Menu menu = menus.get(position);
            String url = Common.URL_SERVER + "MenuServlet";
            String id = menu.getMENU_ID();
            menuImageTask = new ImageTask(url, id, imageSize, holder.imageView);
            menuImageTask.execute();
            holder.tvName.setText(menu.getFOOD_NAME());
            holder.tvPrice.setText("0");
            holder.setPrice(menu.getFOOD_PRICE());
            }
        }

    @Override
    public void onStop() {
        super.onStop();
        if (menuGetAllTask != null) {
            menuGetAllTask.cancel(true);
            menuGetAllTask = null;
        }

        if (menuImageTask != null) {
            menuImageTask.cancel(true);
            menuImageTask = null;
        }

    }

    }
