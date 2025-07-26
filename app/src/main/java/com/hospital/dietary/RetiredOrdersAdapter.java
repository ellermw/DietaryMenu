package com.hospital.dietary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import com.hospital.dietary.models.Patient;
import java.util.ArrayList;
import java.util.List;

public class RetiredOrdersAdapter extends ArrayAdapter<Patient> {

    private Context context;
    private List<Patient> patients;
    private List<Integer> selectedPositions;

    public RetiredOrdersAdapter(Context context, List<Patient> patients) {
        super(context, R.layout.item_retired_order, patients);
        this.context = context;
        this.patients = patients;
        this.selectedPositions = new ArrayList<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_retired_order, parent, false);
            holder = new ViewHolder();
            holder.checkBox = convertView.findViewById(R.id.checkBox);
            holder.patientNameText = convertView.findViewById(R.id.patientNameText);
            holder.roomInfoText = convertView.findViewById(R.id.roomInfoText);
            holder.dietInfoText = convertView.findViewById(R.id.dietInfoText);
            holder.mealsCompletedText = convertView.findViewById(R.id.mealsCompletedText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Patient patient = patients.get(position);

        // Set patient info
        holder.patientNameText.setText(patient.getFirstName() + " " + patient.getLastName());
        holder.roomInfoText.setText(patient.getWing() + " - Room " + patient.getRoomNumber());
        holder.dietInfoText.setText(patient.getDietType());

        // Show completed meals
        String completedMeals = getCompletedMealsText(patient);
        holder.mealsCompletedText.setText(completedMeals);

        // Handle checkbox
        holder.checkBox.setChecked(selectedPositions.contains(position));
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedPositions.contains(position)) {
                    selectedPositions.add(position);
                }
            } else {
                selectedPositions.remove(Integer.valueOf(position));
            }
        });

        return convertView;
    }

    private String getCompletedMealsText(Patient patient) {
        List<String> completedMeals = new ArrayList<>();

        if (patient.isBreakfastComplete() || patient.isBreakfastNPO()) {
            completedMeals.add("B");
        }
        if (patient.isLunchComplete() || patient.isLunchNPO()) {
            completedMeals.add("L");
        }
        if (patient.isDinnerComplete() || patient.isDinnerNPO()) {
            completedMeals.add("D");
        }

        return String.join(", ", completedMeals);
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

    public void clearSelections() {
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public void selectAll() {
        selectedPositions.clear();
        for (int i = 0; i < patients.size(); i++) {
            selectedPositions.add(i);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder {
        CheckBox checkBox;
        TextView patientNameText;
        TextView roomInfoText;
        TextView dietInfoText;
        TextView mealsCompletedText;
    }
}