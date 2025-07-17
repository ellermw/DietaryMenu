// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/ViewOrdersActivity.java
// ================================================================================================

package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.OrderDAO;
import com.hospital.dietary.models.Order;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.print.PageRange;
import android.print.PrintDocumentInfo;

public class ViewOrdersActivity extends AppCompatActivity {
    
    private DatabaseHelper dbHelper;
    private OrderDAO orderDAO;
    
    private Spinner dateSpinner;
    private ListView ordersListView;
    private TextView orderCountText;
    private Button printAllButton, printSelectedButton;
    
    private List<Order> allOrders = new ArrayList<>();
    private ArrayAdapter<Order> ordersAdapter;
    private List<String> availableDates = new ArrayList<>();
    
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_orders);
        
        // Get user info
        isAdmin = getIntent().getBooleanExtra("is_admin", false);
        String username = getIntent().getStringExtra("current_username");
        
        setTitle("View Orders - " + username);
        
        // Initialize database
        dbHelper = new DatabaseHelper(this);
        orderDAO = new OrderDAO(dbHelper);
        
        // Initialize UI
        initializeUI();
        setupListeners();
        loadAvailableDates();
    }
    
    private void initializeUI() {
        dateSpinner = findViewById(R.id.dateSpinner);
        ordersListView = findViewById(R.id.ordersListView);
        orderCountText = findViewById(R.id.orderCountText);
        printAllButton = findViewById(R.id.printAllButton);
        printSelectedButton = findViewById(R.id.printSelectedButton);
        
        // Setup ListView for multiple selection
        ordersListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
        // Setup orders adapter
        ordersAdapter = new ArrayAdapter<Order>(this, R.layout.item_order_row, allOrders) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_order_row, parent, false);
                }
                
                Order order = getItem(position);
                
                TextView patientName = convertView.findViewById(R.id.patientNameText);
                TextView patientDetails = convertView.findViewById(R.id.patientDetailsText);
                TextView orderTime = convertView.findViewById(R.id.orderTimeText);
                
                patientName.setText(order.getPatientName());
                
                String details = order.getWingRoom() + " • " + order.getDiet();
                if (order.getFluidRestriction() != null && !order.getFluidRestriction().isEmpty()) {
                    details += " • " + order.getFluidRestriction();
                }
                if (order.getTextureModifications() != null && !order.getTextureModifications().isEmpty()) {
                    details += " • " + order.getTextureModifications();
                }
                
                patientDetails.setText(details);
                orderTime.setText("Ordered: " + order.getOrderTime());
                
                return convertView;
            }
        };
        ordersListView.setAdapter(ordersAdapter);
    }
    
    private void setupListeners() {
        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Skip "Select Date" option
                    String selectedDate = availableDates.get(position - 1);
                    loadOrdersForDate(selectedDate);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        printAllButton.setOnClickListener(v -> printAllOrders());
        printSelectedButton.setOnClickListener(v -> printSelectedOrders());
        
        // FIXED: Add back button functionality
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            // Navigate back to the previous activity (MainActivity)
            finish();
        });
        
        // Update print button states when selection changes
        ordersListView.setOnItemClickListener((parent, view, position, id) -> updatePrintButtonStates());
    }
    
    private void loadAvailableDates() {
        availableDates = orderDAO.getAvailableDates();
        
        List<String> spinnerItems = new ArrayList<>();
        spinnerItems.add("Select Date");
        spinnerItems.addAll(availableDates);
        
        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerItems);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSpinner.setAdapter(dateAdapter);
    }
    
    private void loadOrdersForDate(String date) {
        allOrders.clear();
        allOrders.addAll(orderDAO.getOrdersByDate(date));
        ordersAdapter.notifyDataSetChanged();
        
        updateOrderCount();
        updatePrintButtonStates();
    }
    
    private void updateOrderCount() {
        int count = allOrders.size();
        if (count == 0) {
            orderCountText.setText("No orders found");
            findViewById(R.id.emptyStateContainer).setVisibility(View.VISIBLE);
            ordersListView.setVisibility(View.GONE);
        } else {
            orderCountText.setText(count + " order" + (count == 1 ? "" : "s") + " found");
            findViewById(R.id.emptyStateContainer).setVisibility(View.GONE);
            ordersListView.setVisibility(View.VISIBLE);
        }
    }
    
    private void updatePrintButtonStates() {
        boolean hasOrders = !allOrders.isEmpty();
        printAllButton.setEnabled(hasOrders);
        
        // Check if any orders are selected
        SparseBooleanArray checked = ordersListView.getCheckedItemPositions();
        boolean hasSelected = false;
        for (int i = 0; i < checked.size(); i++) {
            if (checked.valueAt(i)) {
                hasSelected = true;
                break;
            }
        }
        printSelectedButton.setEnabled(hasSelected);
    }
    
    private void printAllOrders() {
        if (allOrders.isEmpty()) {
            Toast.makeText(this, "No orders to print", Toast.LENGTH_SHORT).show();
            return;
        }
        generateAndPrintPDF(allOrders);
    }
    
    private void printSelectedOrders() {
        SparseBooleanArray checked = ordersListView.getCheckedItemPositions();
        List<Order> selectedOrders = new ArrayList<>();
        
        for (int i = 0; i < allOrders.size(); i++) {
            if (checked.get(i)) {
                selectedOrders.add(allOrders.get(i));
            }
        }
        
        if (selectedOrders.isEmpty()) {
            Toast.makeText(this, "No orders selected", Toast.LENGTH_SHORT).show();
            return;
        }
        
        generateAndPrintPDF(selectedOrders);
    }
    
    private void generateAndPrintPDF(List<Order> orders) {
        try {
            // Create PDF document
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(792, 612, 1).create(); // Letter landscape
            PdfDocument.Page page = document.startPage(pageInfo);
            
            android.graphics.Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            
            // Header
            paint.setTextSize(18);
            paint.setColor(Color.BLACK);
            paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            canvas.drawText("Hospital Dietary Orders", 50, 50, paint);
            
            // Date
            paint.setTextSize(12);
            paint.setTypeface(android.graphics.Typeface.DEFAULT);
            String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            canvas.drawText("Generated: " + currentDate, 50, 70, paint);
            
            // Orders
            int y = 100;
            int orderCount = orders.size();
            
            for (Order order : orders) {
                paint.setTextSize(14);
                paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                canvas.drawText(order.getPatientName(), 50, y, paint);
                
                paint.setTextSize(12);
                paint.setTypeface(android.graphics.Typeface.DEFAULT);
                canvas.drawText(order.getWingRoom() + " | " + order.getDiet(), 50, y + 15, paint);
                
                if (order.getFluidRestriction() != null && !order.getFluidRestriction().isEmpty()) {
                    canvas.drawText("Fluid: " + order.getFluidRestriction(), 50, y + 30, paint);
                }
                
                y += 50;
                
                if (y > 550) { // New page if needed
                    document.finishPage(page);
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 50;
                }
            }
            
            document.finishPage(page);
            
            // Save PDF
            File pdfFile = new File(getExternalFilesDir(null), "dietary_orders.pdf");
            FileOutputStream fos = new FileOutputStream(pdfFile);
            document.writeTo(fos);
            fos.close();
            document.close();
            
            // Print using Android Print Framework
            PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
            
            PrintDocumentAdapter adapter = new MenuPrintDocumentAdapter(pdfFile.getAbsolutePath());
            
            PrintAttributes.Builder builder = new PrintAttributes.Builder();
            builder.setMediaSize(PrintAttributes.MediaSize.NA_LETTER.asLandscape());
            builder.setResolution(new PrintAttributes.Resolution("id", "label", 300, 300));
            builder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
            
            String jobName = "Dietary Orders (" + orderCount + " orders)";
            PrintJob printJob = printManager.print(jobName, adapter, builder.build());
            
            Toast.makeText(this, "Print job started: " + jobName, Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "Error printing: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Refresh")
                .setIcon(android.R.drawable.ic_menu_rotate)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1: // Refresh
                loadAvailableDates();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    // Custom PrintDocumentAdapter for PDF printing
    private class MenuPrintDocumentAdapter extends PrintDocumentAdapter {
        private String filePath;

        public MenuPrintDocumentAdapter(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, 
                           android.os.CancellationSignal cancellationSignal, 
                           LayoutResultCallback callback, Bundle extras) {
            
            if (cancellationSignal.isCanceled()) {
                callback.onLayoutCancelled();
                return;
            }

            PrintDocumentInfo.Builder builder = new PrintDocumentInfo.Builder("dietary_orders.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1);

            PrintDocumentInfo info = builder.build();
            callback.onLayoutFinished(info, true);
        }

        @Override
        public void onWrite(PageRange[] pages, android.os.ParcelFileDescriptor destination, 
                          android.os.CancellationSignal cancellationSignal, WriteResultCallback callback) {
            
            try {
                // Copy the PDF file to the destination
                java.io.InputStream input = new java.io.FileInputStream(filePath);
                java.io.OutputStream output = new java.io.FileOutputStream(destination.getFileDescriptor());
                
                byte[] buffer = new byte[1024];
                int size;
                while ((size = input.read(buffer)) >= 0 && !cancellationSignal.isCanceled()) {
                    output.write(buffer, 0, size);
                }
                
                if (cancellationSignal.isCanceled()) {
                    callback.onWriteCancelled();
                } else {
                    callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
                }
                
                input.close();
                output.close();
                
            } catch (Exception e) {
                callback.onWriteFailed(e.toString());
            }
        }
    }
}