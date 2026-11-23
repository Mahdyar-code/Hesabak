
package com.example.hesabak;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



public class TransactionAdapter
        extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    public static class Transaction {
        public int id;
        public String title, category, type, date;
        public long amount;

        public Transaction(int id, String title, String category,
                           String type, String date, long amount) {
            this.id = id;
            this.title = title;
            this.category = category;
            this.type = type;
            this.date = date;
            this.amount = amount;
        }
    }

    public interface OnDeleteClickListener {
        void onDelete(int transactionId);
    }

    public interface OnEditClickListener {
        void onEdit(int transactionId);
    }


    private final Context context;
    private final List<Transaction> items = new ArrayList<>();
    private final OnDeleteClickListener deleteListener;

    private final OnEditClickListener editListener;


//    public TransactionAdapter(Context context,
//                              OnDeleteClickListener deleteListener) {
//        this.context = context;
//        this.deleteListener = deleteListener;
//    }

    public TransactionAdapter(
            Context context,
            OnDeleteClickListener deleteListener,
            OnEditClickListener editListener) {

        this.context = context;
        this.deleteListener = deleteListener;
        this.editListener = editListener;
    }


    public void setTransactions(List<Transaction> transactions) {
        items.clear();
        items.addAll(transactions);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_transaction, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        Transaction item = items.get(position);

        holder.tvTitle.setText(item.title);
        holder.tvCategory.setText(item.category);
        holder.tvDate.setText(item.date);

        boolean isIncome = item.type.equals("income");

        String sign = isIncome ? "+ " : "- ";

        String amount = NumberFormat
                .getNumberInstance(Locale.US)
                .format(item.amount);

        holder.tvAmount.setText(sign + amount + " AFN");

        holder.tvAmount.setTextColor(context.getColor(
                isIncome ? R.color.income : R.color.expense
        ));

        holder.btnDelete.setOnClickListener(v ->
                deleteListener.onDelete(item.id));

        holder.btnEdit.setOnClickListener(v -> {
            editListener.onEdit(item.id);
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvCategory, tvDate, tvAmount;
//        Button btnDelete;

        Button btnDelete, btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvCategory = itemView.findViewById(R.id.tvItemCategory);
            tvDate = itemView.findViewById(R.id.tvItemDate);
            tvAmount = itemView.findViewById(R.id.tvItemAmount);
            btnDelete = itemView.findViewById(
                    R.id.btnDeleteTransaction);
            btnEdit = itemView.findViewById(R.id.btnEditTransaction);
        }
    }
}
