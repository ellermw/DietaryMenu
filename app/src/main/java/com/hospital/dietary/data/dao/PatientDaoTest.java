package com.hospital.dietary.dao;

import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.Patient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PatientDAOTest {
    
    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    
    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        // Use in-memory database for testing
        dbHelper = new DatabaseHelper(context) {
            @Override
            public String getDatabaseName() {
                return null; // In-memory database
            }
        };
        patientDAO = new PatientDAO(dbHelper);
    }
    
    @After
    public void tearDown() {
        dbHelper.close();
    }
    
    @Test
    public void testAddPatient() {
        // Create test patient
        Patient patient = createTestPatient("John", "Doe", "North", "101");
        
        // Add patient
        long id = patientDAO.addPatient(patient);
        
        // Verify
        assertTrue("Patient ID should be greater than 0", id > 0);
    }
    
    @Test
    public void testGetPatientById() {
        // Add patient
        Patient patient = createTestPatient("Jane", "Smith", "South", "202");
        long id = patientDAO.addPatient(patient);
        
        // Retrieve patient
        Patient retrieved = patientDAO.getPatientById(id);
        
        // Verify
        assertNotNull("Retrieved patient should not be null", retrieved);
        assertEquals("First name should match", "Jane", retrieved.getPatientFirstName());
        assertEquals("Last name should match", "Smith", retrieved.getPatientLastName());
        assertEquals("Wing should match", "South", retrieved.getWing());
        assertEquals("Room should match", "202", retrieved.getRoomNumber());
    }
    
    @Test
    public void testUpdatePatient() {
        // Add patient
        Patient patient = createTestPatient("Bob", "Johnson", "East", "303");
        long id = patientDAO.addPatient(patient);
        
        // Update patient
        patient.setPatientId(id);
        patient.setDiet("ADA Diabetic");
        patient.setFluidRestriction("1500ml");
        boolean success = patientDAO.updatePatient(patient);
        
        // Verify
        assertTrue("Update should be successful", success);
        
        Patient updated = patientDAO.getPatientById(id);
        assertEquals("Diet should be updated", "ADA Diabetic", updated.getDiet());
        assertEquals("Fluid restriction should be updated", "1500ml", updated.getFluidRestriction());
    }
    
    @Test
    public void testDeletePatient() {
        // Add patient
        Patient patient = createTestPatient("Alice", "Brown", "West", "404");
        long id = patientDAO.addPatient(patient);
        
        // Delete patient
        boolean success = patientDAO.deletePatient(id);
        
        // Verify
        assertTrue("Delete should be successful", success);
        
        Patient deleted = patientDAO.getPatientById(id);
        assertNull("Deleted patient should not exist", deleted);
    }
    
    @Test
    public void testGetAllPatients() {
        // Add multiple patients
        patientDAO.addPatient(createTestPatient("Patient", "One", "North", "101"));
        patientDAO.addPatient(createTestPatient("Patient", "Two", "South", "202"));
        patientDAO.addPatient(createTestPatient("Patient", "Three", "East", "303"));
        
        // Get all patients
        List<Patient> patients = patientDAO.getAllPatients();
        
        // Verify
        assertNotNull("Patient list should not be null", patients);
        assertTrue("Should have at least 3 patients", patients.size() >= 3);
    }
    
    @Test
    public void testGetPendingPatients() {
        // Add patient with incomplete meals
        Patient patient = createTestPatient("Pending", "Patient", "North", "105");
        patient.setBreakfastComplete(false);
        patient.setLunchComplete(false);
        patient.setDinnerComplete(false);
        patientDAO.addPatient(patient);
        
        // Get pending patients
        List<Patient> pendingPatients = patientDAO.getPendingPatients();
        
        // Verify
        assertNotNull("Pending patient list should not be null", pendingPatients);
        assertTrue("Should have at least 1 pending patient", pendingPatients.size() >= 1);
    }
    
    @Test
    public void testGetCompletedPatients() {
        // Add patient with all meals complete
        Patient patient = createTestPatient("Complete", "Patient", "South", "206");
        long id = patientDAO.addPatient(patient);
        
        // Mark all meals complete
        patientDAO.updateBreakfastComplete(id, true);
        patientDAO.updateLunchComplete(id, true);
        patientDAO.updateDinnerComplete(id, true);
        
        // Get completed patients
        List<Patient> completedPatients = patientDAO.getCompletedPatients();
        
        // Verify
        assertNotNull("Completed patient list should not be null", completedPatients);
        assertTrue("Should have at least 1 completed patient", completedPatients.size() >= 1);
    }
    
    @Test
    public void testSearchPatients() {
        // Add patients with different names
        patientDAO.addPatient(createTestPatient("John", "Smith", "North", "110"));
        patientDAO.addPatient(createTestPatient("Jane", "Johnson", "South", "210"));
        patientDAO.addPatient(createTestPatient("Bob", "Williams", "East", "310"));
        
        // Search for "John"
        List<Patient> results = patientDAO.searchPatients("John");
        
        // Verify
        assertNotNull("Search results should not be null", results);
        assertTrue("Should find at least 2 patients with 'John'", results.size() >= 2);
    }
    
    @Test
    public void testUpdateMealCompletion() {
        // Add patient
        Patient patient = createTestPatient("Meal", "Test", "West", "410");
        long id = patientDAO.addPatient(patient);
        
        // Update meal completion
        assertTrue("Breakfast update should succeed", patientDAO.updateBreakfastComplete(id, true));
        assertTrue("Lunch update should succeed", patientDAO.updateLunchComplete(id, true));
        assertTrue("Dinner update should succeed", patientDAO.updateDinnerComplete(id, true));
        
        // Verify
        Patient updated = patientDAO.getPatientById(id);
        assertTrue("Breakfast should be complete", updated.isBreakfastComplete());
        assertTrue("Lunch should be complete", updated.isLunchComplete());
        assertTrue("Dinner should be complete", updated.isDinnerComplete());
    }
    
    @Test
    public void testTextureModifications() {
        // Add patient with texture modifications
        Patient patient = createTestPatient("Texture", "Test", "North", "115");
        patient.setMechanicalChopped(true);
        patient.setBiteSize(true);
        patient.setNectarThick(true);
        long id = patientDAO.addPatient(patient);
        
        // Retrieve and verify
        Patient retrieved = patientDAO.getPatientById(id);
        assertTrue("Mechanical chopped should be true", retrieved.isMechanicalChopped());
        assertTrue("Bite size should be true", retrieved.isBiteSize());
        assertTrue("Nectar thick should be true", retrieved.isNectarThick());
    }
    
    // Helper method to create test patient
    private Patient createTestPatient(String firstName, String lastName, String wing, String room) {
        Patient patient = new Patient();
        patient.setPatientFirstName(firstName);
        patient.setPatientLastName(lastName);
        patient.setWing(wing);
        patient.setRoomNumber(room);
        patient.setDiet("Regular");
        patient.setDietType("Regular");
        patient.setFluidRestriction("None");
        return patient;
    }
}