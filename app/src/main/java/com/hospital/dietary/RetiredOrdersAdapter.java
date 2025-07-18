package com.hospital.dietary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.hospital.dietary.models.FinalizedOrder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RetiredOrdersAdapter extends BaseAdapter {
    
    private Context context;
    private List<FinalizedOrder> orders;
    private LayoutInflater inflater;
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat parseDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public RetiredOrdersAdapter(Context context, List<FinalizedOrder> orders) {
        this.context = context;
        this.orders = orders;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return orders.size();
    }

    @Override
    public Object getItem(int position) {
        return orders.get(position);
    }

    @Override
    public long getItemId(int position) {
        return orders.get(position).getOrderId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_retired_order, parent, false);
            holder = new ViewHolder();
            holder.patientNameText = convertView.findViewById(R.id.patientNameText);
            holder.locationText = convertView.findViewById(R.id.locationText);
            holder.orderDateText = convertView.findViewById(R.id.orderDateText);
            holder.dietText = convertView.findViewById(R.id.dietText);
            holder.mealSummaryText = convertView.findViewById(R.id.mealSummaryText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        FinalizedOrder order = orders.get(position);
        
        // Set patient name
        holder.patientNameText.setText(order.getPatientName());
        
        // Set location
        holder.locationText.setText("📍 " + order.getWing() + " - Room " + order.getRoom());
        
        // Set order date
        try {
            Date orderDate = parseDateFormat.parse(order.getOrderDate());
            holder.orderDateText.setText("📅 " + displayDateFormat.format(orderDate));
        } catch (ParseException e) {
            holder.orderDateText.setText("📅 " + order.getOrderDate());
        }
        
        // Set diet
        holder.dietText.setText("🍽️ " + order.getDietType());
        
        // Set meal summary
        StringBuilder mealSummary = new StringBuilder();
        int mealCount = 0;
        
        if (order.getBreakfastItems() != null && !order.getBreakfastItems().isEmpty()) {
            mealCount++;
        }
        if (order.getLunchItems() != null && !order.getLunchItems().isEmpty()) {
            mealCount++;
        }
        if (order.getDinnerItems() != null && !order.getDinnerItems().isEmpty()) {
            mealCount++;
        }
        
        mealSummary.append("📋 ").append(mealCount).append(" meal(s) ordered");
        holder.mealSummaryText.setText(mealSummary.toString());
        
        return convertView;
    }
    
    private static class ViewHolder {
        TextView patientNameText;
        TextView locationText;
        TextView orderDateText;
        TextView dietText;
        TextView mealSummaryText;
    }
}