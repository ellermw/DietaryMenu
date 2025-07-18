package com.hospital.dietary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.hospital.dietary.models.Patient;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExistingPatientAdapter extends BaseAdapter {
    
    private Context context;
    private List<Patient> patients;
    private LayoutInflater inflater;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public ExistingPatientAdapter(Context context, List<Patient> patients) {
        this.context = context;
        this.patients = patients;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return patients.size();
    }

    @Override
    public Object getItem(int position) {
        return patients.get(position);
    }

    @Override
    public long getItemId(int position) {
        return patients.get(position).getPatientId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_existing_patient, parent, false);
            holder = new ViewHolder();
            holder.patientNameText = convertView.findViewById(R.id.patientNameText);
            holder.locationText = convertView.findViewById(R.id.locationText);
            holder.dietText = convertView.findViewById(R.id.dietText);
            holder.createdDateText = convertView.findViewById(R.id.createdDateText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        Patient patient = patients.get(position);
        
        // Set patient name
        holder.patientNameText.setText(patient.getPatientFirstName() + " " + patient.getPatientLastName());
        
        // Set location
        holder.locationText.setText("📍 " + patient.getWing() + " - Room " + patient.getRoomNumber());
        
        // Set diet
        holder.dietText.setText("🍽️ " + patient.getDiet());
        
        // Set created date
        if (patient.getCreatedDate() != null) {
            try {
                holder.createdDateText.setText("📅 " + dateFormat.format(patient.getCreatedDate()));
                holder.createdDateText.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                holder.createdDateText.setVisibility(View.GONE);
            }
        } else {
            holder.createdDateText.setVisibility(View.GONE);
        }
        
        return convertView;
    }
    
    private static class ViewHolder {
        TextView patientNameText;
        TextView locationText;
        TextView dietText;
        TextView createdDateText;
    }
}