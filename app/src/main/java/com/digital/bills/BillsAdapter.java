package com.digital.bills;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Authored by vedhavyas on 17/12/14.
 * Project JaagrT
 */

public class BillsAdapter extends RecyclerView.Adapter<BillsAdapter.BillViewHolder> {

    private List<Bill> bills;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public BillsAdapter(Context context, List<Bill> bills) {
        this.context = context;
        this.bills = bills;
    }

    @Override
    public BillViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.bill_card_view, viewGroup, false);

        return new BillViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(BillViewHolder holder, int position) {
        Bill bill = bills.get(position);
        String description = bill.getDescription();
        holder.title.setText(description);
        if (bill.getBill() != null) {
            holder.billView.setImageBitmap(Utilities.getBitmapFromBlob(bill.getBill()));
        } else {
            holder.billView.setImageDrawable(Utilities.getRoundedDrawable(context, description));
        }
    }

    @Override
    public int getItemCount() {
        if (bills != null) {
            return bills.size();
        }
        return 0;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public List<Bill> getBills(){
        return this.bills;
    }

    public void setBills(List<Bill> bills) {
        this.bills.clear();
        this.bills.addAll(bills);
        notifyDataSetChanged();
    }

    public Bill getBill(int position) {
        return bills.get(position);
    }

    public void removeBill(int position) {
        this.bills.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    public void addBill(Bill bill) {
        int position = bills.size();
        bills.add(position, bill);
        notifyItemInserted(position);
        notifyDataSetChanged();
    }

    public class BillViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected ImageView billView;
        protected TextView title;

        public BillViewHolder(View v) {
            super(v);
            billView = (ImageView) v.findViewById(R.id.billView);
            title = (TextView) v.findViewById(R.id.title);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(view, bills.get(getPosition()).getID());
            }
        }
    }

}
