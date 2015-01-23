package com.digital.bills;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Authored by vedhavyas on 10/12/14.
 * Project JaagrT
 */
public class Database extends SQLiteOpenHelper {

    public static final String USER_TABLE = "user_details";
    public static final String BILL_TABLE = "bill_table";
    private static final String FOLDER_TABLE = "folder_table";
    private static final String FOLDER_BILL_CONNECTIONS = "folder_bill_connection";
    private static final String DB_NAME = "Bills.db";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_OBJECT_ID = "objectID";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_DESC = "description";
    private static final String COLUMN_BILL_ID = "billID";
    private static final String COLUMN_FOLDER_ID = "folderID";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_BILL_IMAGE = "billPic";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_FOLDER_NAME = "folderName";
    private static final String SQL_USER_TABLE_CREATE_QUERY = "CREATE TABLE " + USER_TABLE
            + " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_OBJECT_ID + " TEXT,"
            + COLUMN_NAME + " TEXT,"
            + COLUMN_EMAIL + " TEXT UNIQUE)";

    private static final String SQL_BILL_TABLE_CREATE_QUERY = "CREATE TABLE "+ BILL_TABLE
            + " ( " + COLUMN_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_OBJECT_ID + " TEXT,"
            + COLUMN_BILL_ID + " TEXT,"
            + COLUMN_DESC + " TEXT,"
            + COLUMN_AMOUNT + " REAL,"
            + COLUMN_CATEGORY + " TEXT,"
            + COLUMN_BILL_IMAGE + " BLOB)";

    private static final String SQL_FOLDER_TABLE_CREATE_QUERY = "CREATE TABLE "+ FOLDER_TABLE
            +" ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_OBJECT_ID + " TEXT,"
            + COLUMN_FOLDER_NAME + " TEXT)";

    private static final String SQL_FOLDER_BILLS_CREATE_TABLE = "CREATE TABLE "+ FOLDER_BILL_CONNECTIONS
            +" ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_FOLDER_ID + " INTEGER,"
            + COLUMN_BILL_ID + " INTEGER)";
    private static final String DROP_TABLE = "DROP TABLE IF EXISTS  ";


    private static Database dbFactory;
    private static Cursor cursor;

    private Database(Context context) {
        super(context, DB_NAME, null, 1);
    }

    public static Database getInstance(Context context) {
        if (dbFactory == null) {

            dbFactory = new Database(context);
        }

        return dbFactory;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_USER_TABLE_CREATE_QUERY);
        db.execSQL(SQL_BILL_TABLE_CREATE_QUERY);
        db.execSQL(SQL_FOLDER_TABLE_CREATE_QUERY);
        db.execSQL(SQL_FOLDER_BILLS_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE + USER_TABLE);
        db.execSQL(DROP_TABLE + BILL_TABLE);
        db.execSQL(DROP_TABLE + FOLDER_TABLE);
        db.execSQL(DROP_TABLE + FOLDER_BILL_CONNECTIONS);
        onCreate(db);
    }

    public void dropTables(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(DROP_TABLE+USER_TABLE);
        db.execSQL(DROP_TABLE + BILL_TABLE);
        db.execSQL(DROP_TABLE + FOLDER_TABLE);
        db.execSQL(DROP_TABLE + FOLDER_BILL_CONNECTIONS);
        onCreate(db);
    }


    public long saveUser(User user) {
        long result;
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = getContentValuesFromUserObject(user);

        result = db.insert(USER_TABLE, null, contentValues);
        return result;
    }

    public User getUser(int userID) {
        if (userID > 0) {
            SQLiteDatabase db = this.getReadableDatabase();

            String sqlQuery = "SELECT * FROM " + USER_TABLE + " WHERE " + COLUMN_ID
                    + " = " + String.valueOf(userID);
            cursor = db.rawQuery(sqlQuery, null);

            if (cursor.moveToFirst()) {
                User user = new User();
                do {
                    user.setID(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                    user.setEmail(cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)));
                } while (cursor.moveToNext());

                return user;
            }
            return null;

        } else {
            return null;
        }
    }

    public int updateUserData(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = -1;

        ContentValues contentValues = getContentValuesFromUserObject(user);

        try {
            result = db.update(USER_TABLE, contentValues, COLUMN_ID + " = " + user.getID(), null);
        } catch (SQLiteConstraintException e) {
        }
        return result;
    }

    public int updateBill(Bill bill){
        SQLiteDatabase db = this.getWritableDatabase();
        int result = -1;
        ContentValues contentValues = getContentValuesFromBill(bill);
        try {
            result = db.update(BILL_TABLE, contentValues, COLUMN_ID + " = "+ bill.getID(), null);
        }catch (SQLiteConstraintException e){

        }
        return result;
    }

    private ContentValues getContentValuesFromUserObject(User user) {
        ContentValues contentValues = new ContentValues();

        if (user.getName() != null) {
            contentValues.put(COLUMN_NAME, user.getName());
        }

        if (user.getEmail() != null) {
            contentValues.put(COLUMN_EMAIL, user.getEmail());
        }

        if (user.getObjectID() != null) {
            contentValues.put(COLUMN_OBJECT_ID, user.getObjectID());
        }

        return contentValues;
    }

    public List<Bill> getAllBills(){
        SQLiteDatabase db = this.getReadableDatabase();
        List<Bill> bills = new ArrayList<>();
        String sqlQuery = "SELECT * FROM " + BILL_TABLE;
        cursor = db.rawQuery(sqlQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Bill bill = new Bill();
                bill.setID(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                bill.setBillID(cursor.getString(cursor.getColumnIndex(COLUMN_BILL_ID)));
                bill.setObjectID(cursor.getString(cursor.getColumnIndex(COLUMN_OBJECT_ID)));
                bill.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESC)));
                bill.setAmount(cursor.getFloat(cursor.getColumnIndex(COLUMN_AMOUNT)));
                bill.setCategory(cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY)));
                bill.setBill(cursor.getBlob(cursor.getColumnIndex(COLUMN_BILL_IMAGE)));
                bills.add(bill);
            } while (cursor.moveToNext());

            return bills;
        }
        return null;
    }

    public Bill getBill(int billID){
        SQLiteDatabase db = this.getReadableDatabase();
        String sqlQuery = "SELECT * FROM " + BILL_TABLE + " WHERE "+COLUMN_ID+" = "+billID;
        cursor = db.rawQuery(sqlQuery, null);
        Bill bill;
        if (cursor.moveToFirst()) {
            do {
                bill = new Bill();
                bill.setID(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                bill.setObjectID(cursor.getString(cursor.getColumnIndex(COLUMN_OBJECT_ID)));
                bill.setBillID(cursor.getString(cursor.getColumnIndex(COLUMN_BILL_ID)));
                bill.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESC)));
                bill.setAmount(cursor.getFloat(cursor.getColumnIndex(COLUMN_AMOUNT)));
                bill.setCategory(cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY)));
                bill.setBill(cursor.getBlob(cursor.getColumnIndex(COLUMN_BILL_IMAGE)));
            } while (cursor.moveToNext());

            return bill;
        }
        return null;
    }

    public int saveBill(Bill bill){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = getContentValuesFromBill(bill);
        return (int)db.insert(BILL_TABLE, null, contentValues);
    }

    private ContentValues getContentValuesFromBill(Bill bill){
        ContentValues contentValues = new ContentValues();

        if (bill.getBillID() != null) {
            contentValues.put(COLUMN_BILL_ID, bill.getBillID());
        }

        if (bill.getAmount() > 0) {
            contentValues.put(COLUMN_AMOUNT, bill.getAmount());
        }

        if (bill.getDescription() != null) {
            contentValues.put(COLUMN_DESC, bill.getDescription());
        }

        if(bill.getCategory() != null){
            contentValues.put(COLUMN_CATEGORY, bill.getCategory());
        }

        if(bill.getBill() != null){
            contentValues.put(COLUMN_BILL_IMAGE, bill.getBill());
        }

        if(bill.getObjectID() != null){
            contentValues.put(COLUMN_OBJECT_ID, bill.getObjectID());
        }

        return contentValues;
    }

    public void removeBills(List<Bill> bills){
        if(bills.size() > 0){
            SQLiteDatabase db = this.getWritableDatabase();
            for(Bill bill : bills){
                removeBill(db, bill.getID());
            }
        }
    }

    private int removeBill(SQLiteDatabase db, int billID){
        removeFolderBill(db, billID);
        return db.delete(BILL_TABLE, COLUMN_ID + " = " + billID, null);
    }

    public int createFolder(String objectID, String folder){
        if(folder != null && !folder.isEmpty()){
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_OBJECT_ID, objectID);
            contentValues.put(COLUMN_FOLDER_NAME, folder);
            return (int)db.insert(FOLDER_TABLE, null, contentValues);
        }

        return -1;
    }

    public List<String> getFolders (){
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> folders = new ArrayList<>();
        String sqlQuery = "SELECT * FROM " + FOLDER_TABLE;
        cursor = db.rawQuery(sqlQuery, null);
        if (cursor.moveToFirst()) {
            do {
                folders.add(cursor.getString(cursor.getColumnIndex(COLUMN_FOLDER_NAME)));
            } while (cursor.moveToNext());

            return folders;
        }
        return null;
    }

    public void removeFolders(List<String> folders){
        if(folders.size() > 0){
            SQLiteDatabase db = getWritableDatabase();
            for(String folder : folders){
                removeFolder(db, folder);
            }
        }
    }

    private void removeFolder(SQLiteDatabase db, String folder){
        int folderID = getFolderID(folder);
        if(folderID > 0){
            db.delete(FOLDER_TABLE, COLUMN_ID + " = " + folderID, null);
            List<Integer> billIDs = getBillIds(folderID);
            db.delete(FOLDER_BILL_CONNECTIONS, COLUMN_FOLDER_ID+ " = "+folderID, null);
            deleteFolderBillsWithIds(billIDs);
        }
    }

    public int getFolderID(String folderName){
        SQLiteDatabase db = this.getReadableDatabase();
        String sqlQuery = "SELECT * FROM " + FOLDER_TABLE;
        cursor = db.rawQuery(sqlQuery, null);
        int folderId = -1;
        if(cursor.moveToFirst()){
            do {
               if(folderName.equalsIgnoreCase(cursor.getString(cursor.getColumnIndex(COLUMN_FOLDER_NAME)))){
                   folderId = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
               }
            } while (cursor.moveToNext());
        }

        return folderId;
    }


    public List<Bill> getFolderBills(int folderID){

        List<Integer> billIDs = getBillIds(folderID);
        if(billIDs.size() > 0){
            List<Bill> bills = getBills(billIDs);
            if(bills.size() > 0){
                return bills;
            }
        }
        return null;
    }

    private List<Integer> getBillIds(int folderID){
        SQLiteDatabase db =this.getReadableDatabase();
        List<Integer> billIDs = new ArrayList<>();
        String sqlQuery = "SELECT * FROM " + FOLDER_BILL_CONNECTIONS + " WHERE "+COLUMN_FOLDER_ID+ " = " + folderID;
        cursor = db.rawQuery(sqlQuery, null);
        if(cursor.moveToFirst()){
            do {
                billIDs.add(cursor.getInt(cursor.getColumnIndex(COLUMN_BILL_ID)));
            } while (cursor.moveToNext());

        }

        return billIDs;
    }

    private List<Bill> getBills(List<Integer> billIds){
        List<Bill> folderBills = new ArrayList<>();
        List<Bill> bills = getAllBills();
        for (Bill bill : bills){
            if(billIds.contains(bill.getID())){
                folderBills.add(bill);
            }
        }

        return folderBills;
    }

    public void deleteFolderBillsWithIds(List<Integer> billIDs){
        SQLiteDatabase db = this.getWritableDatabase();
        if(billIDs.size() > 0){
            for(int billId : billIDs){
                db.delete(BILL_TABLE, COLUMN_ID + " = " + billId, null);
            }
        }
    }

    public void deleteFolderBillsWithObjects(List<Bill> bills){
        List<Integer> billIds = new ArrayList<>();
        for(Bill bill : bills){
            billIds.add(bill.getID());
        }

        deleteFolderBillsWithIds(billIds);
    }

    public int saveFolderBill(int folderID, int billID){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_FOLDER_ID, folderID);
        contentValues.put(COLUMN_BILL_ID, billID);
        return (int)db.insert(FOLDER_BILL_CONNECTIONS, null, contentValues);
    }

    private void removeFolderBill(SQLiteDatabase db, int billID){
        db.delete(FOLDER_BILL_CONNECTIONS, COLUMN_BILL_ID+ " = "+billID, null);
    }

    public String getFolderObjectID(int folderID){
        SQLiteDatabase db = this.getReadableDatabase();
        String sqlQuery = "SELECT * FROM " + FOLDER_TABLE + " WHERE "+COLUMN_ID+ " = " + folderID;
        String objectID ;
        cursor = db.rawQuery(sqlQuery, null);
        if(cursor.moveToFirst()){
            do {
               objectID = cursor.getString(cursor.getColumnIndex(COLUMN_OBJECT_ID));
            } while (cursor.moveToNext());

            return objectID;
        }

        return null;
    }

    public int getBillID(String objectID){
        SQLiteDatabase db = this.getReadableDatabase();
        String sqlQuery = "SELECT * FROM "+ BILL_TABLE +" WHERE " + COLUMN_OBJECT_ID + " = " + "\""+objectID+"\"";
        cursor = db.rawQuery(sqlQuery, null);
        int billID = -1;
        if(cursor.moveToFirst()){
            do {
                billID = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            } while (cursor.moveToNext());

        }

        return billID;
    }

    public List<Bill> getBillsOnDescSearch(String data){
        List<Bill> allBills = getAllBills();
        List<Bill> selectedBills = new ArrayList<>();
        for (Bill bill : allBills){
            if(bill.getDescription() != null && bill.getDescription().contains(data)){
                selectedBills.add(bill);
            }
        }

        return selectedBills;
    }

    public List<Bill> getBillsOnCategorySearch(String category){
        List<Bill> allBills = getAllBills();
        List<Bill> selectedBills = new ArrayList<>();
        for(Bill bill : allBills){
            if(bill.getCategory() != null && bill.getCategory().equalsIgnoreCase(category)){
                selectedBills.add(bill);
            }
        }

        return selectedBills;
    }

    public List<Bill> getBillsOnFolderSearch(String data){
        List<String> folders = getFolders();
        List<String> selectedFolders = new ArrayList<>();
        for(String folder : folders){
            if(folder.contains(data)){
                selectedFolders.add(folder);
            }
        }

        return getBillsFromFolders(folders);
    }

    public List<Bill> getBillsFromFolders(List<String> folders){
        List<Bill> selectedBills = new ArrayList<>();
        for(String folder : folders){
            selectedBills.addAll(getFolderBills(getFolderID(folder)));
        }

        return selectedBills;
    }

}