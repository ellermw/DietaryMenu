package com.hospital.dietary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import com.hospital.dietary.models.Patient;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PatientAdapter extends BaseAdapter {

    private Context context;
    private List<Patient> patients;
    private Set<Integer> selectedPositions;
    private LayoutInflater inflater;

    public PatientAdapter(Context context, List<Patient> patients) {
        this.context = context;
        this.patients = new ArrayList<>(patients);
        this.selectedPositions = new HashSet<>();
        this.inflater = LayoutInflater.from(context);
    }

    public void updateData(List<Patient> newPatients) {
        this.patients.clear();
        this.patients.addAll(newPatients);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return patients.size();
    }

    @Override
    public Patient getItem(int position) {
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
            convertView = inflater.inflate(R.layout.item_patient, parent, false);
            holder = new ViewHolder();
            holder.patientNameText = convertView.findViewById(R.id.patientNameText);
            holder.patientLocationText = convertView.findViewById(R.id.patientLocationText);
            holder.patientDietText = convertView.findViewById(R.id.patientDietText);
            holder.patientMealStatusText = convertView.findViewById(R.id.patientMealStatusText);
            holder.selectionCheckBox = convertView.findViewById(R.id.selectionCheckBox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Patient patient = getItem(position);

        // Set patient information
        holder.patientNameText.setText(patient.getFullName());
        holder.patientLocationText.setText(patient.getWing() + " - Room " + patient.getRoomNumber());

        // Display diet with ADA indicator if applicable
        String dietDisplay = patient.getDiet();
        if (patient.isAdaDiet() && !dietDisplay.contains("ADA")) {
            dietDisplay += " (ADA)";
        }
        holder.patientDietText.setText("Diet: " + dietDisplay);

        // Show meal completion status
        String mealStatus = getMealStatus(patient);
        holder.patientMealStatusText.setText(mealStatus);

        // Handle selection checkbox
        holder.selectionCheckBox.setChecked(selectedPositions.contains(position));
        holder.selectionCheckBox.setOnClickListener(v -> {
            if (holder.selectionCheckBox.isChecked()) {
                selectedPositions.add(position);
            } else {
                selectedPositions.remove(position);
            }
            if (context instanceof ExistingPatientsActivity) {
                ((ExistingPatientsActivity) context).updateBulkOperationVisibility();
            }
        });

        return convertView;
    }

    private String getMealStatus(Patient patient) {
        StringBuilder status = new StringBuilder();

        if (patient.isBreakfastComplete() || patient.isBreakfastNPO()) {
            status.append("B✓ ");
        } else {
            status.append("B○ ");
        }

        if (patient.isLunchComplete() || patient.isLunchNPO()) {
            status.append("L✓ ");
        } else {
            status.append("L○ ");
        }

        if (patient.isDinnerComplete() || patient.isDinnerNPO()) {
            status.append("D✓");
        } else {
            status.append("D○");
        }

        return status.toString();
    }

    public void selectAll(boolean selectAll) {
        selectedPositions.clear();
        if (selectAll) {
            for (int i = 0; i < getCount(); i++) {
                selectedPositions.add(i);
            }
        }
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return selectedPositions.size();
    }

    public List<Patient> getSelectedPatients() {
        List<Patient> selected = new ArrayList<>();
        for (Integer position : selectedPositions) {
            if (position < patients.size()) {
                selected.add(patients.get(position));
            }
        }
        return selected;
    }

    private static class ViewHolder {
        TextView patientNameText;
        TextView patientLocationText;
        TextView patientDietText;
        TextView patientMealStatusText;
        CheckBox selectionCheckBox;
    }
}