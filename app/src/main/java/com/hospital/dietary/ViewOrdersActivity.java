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
        
        // Update print button states when selection changes
        ordersListView.setOnItemClickListener((parent, view, position, id) -> updatePrintButtonStates());
    }
    
    private void loadAvailableDates() {
        availableDates = orderDAO.getAvailableDates();
        
        List<String> spinnerItems = new ArrayList<>();
        spinnerItems.add("Select Date");
        spinnerItems.addAll(availableDates);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, spinnerItems);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSpinner.setAdapter(adapter);
        
        if (availableDates.isEmpty()) {
            orderCountText.setText("No finalized orders found");
            printAllButton.setEnabled(false);
            printSelectedButton.setEnabled(false);
        }
    }
    
    private void loadOrdersForDate(String date) {
        allOrders.clear();
        allOrders.addAll(orderDAO.getOrdersByDate(date));
        ordersAdapter.notifyDataSetChanged();
        
        orderCountText.setText(allOrders.size() + " orders for " + date);
        
        updatePrintButtonStates();
    }
    
    private void updatePrintButtonStates() {
        boolean hasOrders = !allOrders.isEmpty();
        boolean hasSelection = ordersListView.getCheckedItemCount() > 0;
        
        printAllButton.setEnabled(hasOrders);
        printSelectedButton.setEnabled(hasSelection);
    }
    
    private void printAllOrders() {
        if (allOrders.isEmpty()) {
            Toast.makeText(this, "No orders to print", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Print All Orders")
            .setMessage("Print all " + allOrders.size() + " orders for the selected date?")
            .setPositiveButton("Print", (dialog, which) -> {
                generateAndPrintOrders(allOrders);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void printSelectedOrders() {
        SparseBooleanArray checked = ordersListView.getCheckedItemPositions();
        List<Order> selectedOrders = new ArrayList<>();
        
        for (int i = 0; i < checked.size(); i++) {
            int position = checked.keyAt(i);
            if (checked.valueAt(i)) {
                selectedOrders.add(allOrders.get(position));
            }
        }
        
        if (selectedOrders.isEmpty()) {
            Toast.makeText(this, "No orders selected", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Print Selected Orders")
            .setMessage("Print " + selectedOrders.size() + " selected orders?")
            .setPositiveButton("Print", (dialog, which) -> {
                generateAndPrintOrders(selectedOrders);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void generateAndPrintOrders(List<Order> orders) {
        try {
            // Create PDF document
            PdfDocument document = new PdfDocument();
            
            for (int i = 0; i < orders.size(); i++) {
                Order order = orders.get(i);
                
                // Create page info (landscape orientation)
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(792, 612, i + 1).create();
                PdfDocument.Page page = document.startPage(pageInfo);
                
                // Draw order on page
                drawOrderOnPage(page, order);
                
                document.finishPage(page);
            }
            
            // Save and print document
            printPdfDocument(document, orders.size());
            
        } catch (Exception e) {
            Toast.makeText(this, "Error generating print document: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    
    private void drawOrderOnPage(PdfDocument.Page page, Order order) {
        android.graphics.Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        
        // Set up paint for headers
        Paint headerPaint = new Paint();
        headerPaint.setTextSize(18);
        headerPaint.setColor(Color.BLACK);
        headerPaint.setFakeBoldText(true);
        
        // Set up paint for content
        Paint contentPaint = new Paint();
        contentPaint.setTextSize(14);
        contentPaint.setColor(Color.BLACK);
        
        // Set up paint for meal headers
        Paint mealHeaderPaint = new Paint();
        mealHeaderPaint.setTextSize(16);
        mealHeaderPaint.setColor(Color.BLACK);
        mealHeaderPaint.setFakeBoldText(true);
        
        int x = 50;
        int y = 50;
        int lineHeight = 25;
        
        // Title
        canvas.drawText("DIETARY ORDER - " + order.getOrderDate(), x, y, headerPaint);
        y += lineHeight * 2;
        
        // Patient info
        canvas.drawText("Patient: " + order.getPatientName(), x, y, headerPaint);
        y += lineHeight;
        canvas.drawText("Location: " + order.getWingRoom(), x, y, contentPaint);
        y += lineHeight;
        canvas.drawText("Diet: " + order.getDiet(), x, y, contentPaint);
        y += lineHeight;
        
        if (order.getFluidRestriction() != null && !order.getFluidRestriction().isEmpty()) {
            canvas.drawText("Fluid Restriction: " + order.getFluidRestriction(), x, y, contentPaint);
            y += lineHeight;
        }
        
        if (order.getTextureModifications() != null && !order.getTextureModifications().isEmpty()) {
            canvas.drawText("Texture Modifications: " + order.getTextureModifications(), x, y, contentPaint);
            y += lineHeight;
        }
        
        y += lineHeight;
        
        // Draw meals in columns
        int col1X = x;
        int col2X = x + 250;
        int col3X = x + 500;
        int startY = y;
        
        // Breakfast
        y = drawMeal(canvas, "BREAKFAST", order.getBreakfastItems(), order.getBreakfastDrinks(), 
                col1X, startY, mealHeaderPaint, contentPaint, lineHeight);
        
        // Lunch
        y = drawMeal(canvas, "LUNCH", order.getLunchItems(), order.getLunchDrinks(), 
                col2X, startY, mealHeaderPaint, contentPaint, lineHeight);
        
        // Dinner
        y = drawMeal(canvas, "DINNER", order.getDinnerItems(), order.getDinnerDrinks(), 
                col3X, startY, mealHeaderPaint, contentPaint, lineHeight);
        
        // Add footer
        canvas.drawText("Order Time: " + order.getOrderTime(), x, 580, contentPaint);
        canvas.drawText("Generated: " + new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault())
                .format(new Date()), x + 400, 580, contentPaint);
    }
    
    private int drawMeal(android.graphics.Canvas canvas, String mealName, List<String> items, List<String> drinks,
                        int x, int y, Paint headerPaint, Paint contentPaint, int lineHeight) {
        
        canvas.drawText(mealName, x, y, headerPaint);
        y += lineHeight;
        
        // Draw food items
        if (items != null && !items.isEmpty()) {
            for (String item : items) {
                canvas.drawText("• " + item, x, y, contentPaint);
                y += lineHeight;
            }
        }
        
        // Draw drinks
        if (drinks != null && !drinks.isEmpty()) {
            canvas.drawText("Drinks:", x, y, headerPaint);
            y += lineHeight;
            for (String drink : drinks) {
                canvas.drawText("• " + drink, x, y, contentPaint);
                y += lineHeight;
            }
        }
        
        if ((items == null || items.isEmpty()) && (drinks == null || drinks.isEmpty())) {
            canvas.drawText("No items selected", x, y, contentPaint);
            y += lineHeight;
        }
        
        return y;
    }
    
    private void printPdfDocument(PdfDocument document, int orderCount) {
        try {
            // Create temporary file
            File cacheDir = getCacheDir();
            File pdfFile = new File(cacheDir, "dietary_orders_" + System.currentTimeMillis() + ".pdf");
            
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
            
            PrintDocumentInfo info = new PrintDocumentInfo.Builder("dietary_orders.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .build();
            
            callback.onLayoutFinished(info, true);
        }
        
        @Override
        public void onWrite(PageRange[] pages, android.os.ParcelFileDescriptor destination,
                android.os.CancellationSignal cancellationSignal, WriteResultCallback callback) {
            
            try {
                // Copy PDF file to destination
                java.io.InputStream input = new java.io.FileInputStream(filePath);
                java.io.OutputStream output = new java.io.FileOutputStream(destination.getFileDescriptor());
                
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                
                input.close();
                output.close();
                
                callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
                
            } catch (Exception e) {
                callback.onWriteFailed(e.toString());
            }
        }
    }
}