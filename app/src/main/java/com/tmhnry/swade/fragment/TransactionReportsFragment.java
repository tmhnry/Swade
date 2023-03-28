package com.tmhnry.swade.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.tmhnry.swade.databinding.FragmentTransactionReportsBinding;
import com.tmhnry.swade.model.Entity;
import com.tmhnry.swade.model.Transaction;
import com.tmhnry.swade.recyclerview.TransactionsViewAdapter;
import com.tmhnry.swade.singleton.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionReportsFragment extends Fragment {
    FragmentTransactionReportsBinding binding;
    RecyclerView recyclerView;
    TransactionsViewAdapter adapter;
    List<Transaction> transactions;
    Spinner spinner;
    String[] options = new String[]{"All", "Customer", "Delivery"};
    Date start;
    Entity entity;
    int flag;

    public TransactionReportsFragment() {
    }

    public static TransactionReportsFragment Builder() {
        TransactionReportsFragment fragment = new TransactionReportsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        for (Entity entity : Entity.getModels().values()) {
            if (entity.userKey.equals(User.getKey(context))) {
                this.entity = entity;
            }
        }
        transactions = new ArrayList<>();
        start = null;
        adapter = new TransactionsViewAdapter(context, transactions);
        flag = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTransactionReportsBinding.inflate(inflater, container, false);

        recyclerView = binding.recyclerView;
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        spinner = binding.spinner;
        spinner.setDropDownHorizontalOffset(-300);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                flag = position;
                updateTransactions();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<String> optionsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, options);
        optionsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(optionsAdapter);

        setStartFromJoinDate();
        updateTransactions();

        binding.all.setOnClickListener(v -> {
            setStartFromJoinDate();
            updateTransactions();
        });

        binding.today.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            start = calendar.getTime();
            updateTransactions();
        });
        binding.thisWeek.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            start = calendar.getTime();
            updateTransactions();
        });

        binding.thisMonth.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            start = calendar.getTime();
            updateTransactions();
        });

        return binding.getRoot();
    }

    public void setStartFromJoinDate() {
        Context context = getContext();
        start = entity.joinDate;
    }

    public void updateTransactions() {
        binding.startDate.setText("from " + start.toLocaleString());
        if (flag == 0) {
            showAllTransactions();
        }
        if (flag == 1) {
            showCustomerTransactions();
        }
        if (flag == 2) {
            showDeliveryTransactions();
        }
    }

    public void showAllTransactions() {
        transactions.clear();
        if (entity.position.equals(Entity.OWNER)) {
            transactions.addAll(Transaction.getTransactions(Transaction.BUYER, start));
            transactions.addAll(Transaction.getTransactions(Transaction.SELLER, start));
        } else {
            transactions.addAll(Transaction.getTransactions(Transaction.EMPLOYEE, entity.key, Transaction.BUYER, start));
            transactions.addAll(Transaction.getTransactions(Transaction.EMPLOYEE, entity.key, Transaction.SELLER, start));
        }
        binding.filter.setText("All");
        adapter.notifyDataSetChanged();
    }

    public void showCustomerTransactions() {
        String userKey = User.getKey(getContext());
        transactions.clear();
        if (entity.position.equals(Entity.OWNER)) {
            transactions.addAll(Transaction.getTransactions(Transaction.SELLER, start));
        } else {
            transactions.addAll(Transaction.getTransactions(Transaction.EMPLOYEE, entity.key, Transaction.SELLER, start));
        }
        binding.filter.setText("Customer");
        adapter.notifyDataSetChanged();
    }

    public void showDeliveryTransactions() {
        String userKey = User.getKey(getContext());
        transactions.clear();
        if (entity.position.equals(Entity.OWNER)) {
            transactions.addAll(Transaction.getTransactions(Transaction.BUYER, start));
        } else {
            transactions.addAll(Transaction.getTransactions(Transaction.EMPLOYEE, entity.key, Transaction.BUYER, start));
        }
        binding.filter.setText("Delivery");
        adapter.notifyDataSetChanged();
    }
}