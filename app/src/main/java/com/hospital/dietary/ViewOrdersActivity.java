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

        // Create adapter for orders
        ordersAdapter = new ArrayAdapter<Order>(this,
                android.R.layout.simple_list_item_multiple_choice) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);

                Order order = getItem(position);
                if (order != null) {
                    String display = String.format("%s - %s | %s | %s",
                            order.getLocationInfo(),
                            order.getPatientName(),
                            order.getDiet(),
                            order.getMealType());
                    text.setText(display);
                }

                return view;
            }
        };

        ordersListView.setAdapter(ordersAdapter);
    }

    private void setupListeners() {
        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadOrdersForDate(availableDates.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        printAllButton.setOnClickListener(v -> printAllOrders());
        printSelectedButton.setOnClickListener(v -> printSelectedOrders());

        ordersListView.setOnItemClickListener((parent, view, position, id) -> {
            updateSelectedCount();
        });
    }

    private void loadAvailableDates() {
        // Load last 30 days
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date today = new Date();

        availableDates.clear();
        for (int i = 0; i < 30; i++) {
            Date date = new Date(today.getTime() - (i * 24 * 60 * 60 * 1000L));
            availableDates.add(dateFormat.format(date));
        }

        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, availableDates);
        dateSpinner.setAdapter(dateAdapter);
    }

    private void loadOrdersForDate(String date) {
        allOrders.clear();
        allOrders.addAll(orderDAO.getOrdersByDate(date));

        ordersAdapter.clear();
        ordersAdapter.addAll(allOrders);
        ordersAdapter.notifyDataSetChanged();

        orderCountText.setText("Total Orders: " + allOrders.size());
        updateSelectedCount();
    }

    private void updateSelectedCount() {
        SparseBooleanArray checked = ordersListView.getCheckedItemPositions();
        int count = 0;
        for (int i = 0; i < checked.size(); i++) {
            if (checked.valueAt(i)) {
                count++;
            }
        }

        printSelectedButton.setText("Print Selected (" + count + ")");
        printSelectedButton.setEnabled(count > 0);
    }

    private void printAllOrders() {
        if (allOrders.isEmpty()) {
            Toast.makeText(this, "No orders to print", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Order> ordersToPrint = new ArrayList<>(allOrders);
        printOrders(ordersToPrint, "All Orders");
    }

    private void printSelectedOrders() {
        SparseBooleanArray checked = ordersListView.getCheckedItemPositions();
        List<Order> selectedOrders = new ArrayList<>();

        for (int i = 0; i < checked.size(); i++) {
            if (checked.valueAt(i)) {
                int position = checked.keyAt(i);
                selectedOrders.add(allOrders.get(position));
            }
        }

        if (selectedOrders.isEmpty()) {
            Toast.makeText(this, "No orders selected", Toast.LENGTH_SHORT).show();
            return;
        }

        printOrders(selectedOrders, "Selected Orders");
    }

    private void printOrders(List<Order> orders, String title) {
        // Create print job
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        String jobName = getString(R.string.app_name) + " - " + title;

        printManager.print(jobName, new OrderPrintDocumentAdapter(this, orders, title),
                new PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.NA_LETTER)
                        .build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_orders, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_refresh) {
            if (dateSpinner.getSelectedItemPosition() >= 0) {
                loadOrdersForDate(availableDates.get(dateSpinner.getSelectedItemPosition()));
            }
            return true;
        } else if (itemId == R.id.action_export) {
            exportOrdersToCSV();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void exportOrdersToCSV() {
        // Implementation for CSV export
        Toast.makeText(this, "Export feature coming soon", Toast.LENGTH_SHORT).show();
    }

    // Inner class for print document adapter
    private static class OrderPrintDocumentAdapter extends PrintDocumentAdapter {
        private Context context;
        private List<Order> orders;
        private String title;
        private PdfDocument pdfDocument;

        public OrderPrintDocumentAdapter(Context context, List<Order> orders, String title) {
            this.context = context;
            this.orders = orders;
            this.title = title;
        }

        @Override
        public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                             android.os.CancellationSignal cancellationSignal,
                             LayoutResultCallback callback, Bundle extras) {

            pdfDocument = new PdfDocument();

            if (cancellationSignal.isCanceled()) {
                callback.onLayoutCancelled();
                return;
            }

            PrintDocumentInfo info = new PrintDocumentInfo.Builder(title + ".pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build();

            callback.onLayoutFinished(info, true);
        }

        @Override
        public void onWrite(PageRange[] pages, android.os.ParcelFileDescriptor destination,
                            android.os.CancellationSignal cancellationSignal,
                            WriteResultCallback callback) {

            // Create PDF content
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);

            android.graphics.Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(20);

            int y = 50;
            canvas.drawText(title, 50, y, paint);

            y += 30;
            paint.setTextSize(14);

            for (Order order : orders) {
                if (y > 750) {
                    pdfDocument.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842,
                            pdfDocument.getPages().size() + 1).create();
                    page = pdfDocument.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 50;
                }

                String orderText = String.format("%s - %s | %s | %s",
                        order.getLocationInfo(),
                        order.getPatientName(),
                        order.getDiet(),
                        order.getMealType());

                canvas.drawText(orderText, 50, y, paint);
                y += 20;
            }

            pdfDocument.finishPage(page);

            // Write to file
            try {
                pdfDocument.writeTo(new FileOutputStream(destination.getFileDescriptor()));
                callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
            } catch (Exception e) {
                callback.onWriteFailed(e.toString());
            } finally {
                pdfDocument.close();
            }
        }
    }
}