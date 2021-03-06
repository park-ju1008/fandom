package com.info.idol.community.chat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.info.idol.community.Class.User;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;


public class MyDataBase extends SQLiteOpenHelper {
    private static final String DB_NAME = "fandom";
    private static final int DB_VERSION = 2;
    public static MyDataBase myDataBase = null;

    public static MyDataBase getInstance(Context context) { //싱글톤 패턴
        if (myDataBase == null) {
            myDataBase = new MyDataBase(context);
        }
        return myDataBase;
    }

    private MyDataBase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS room(id INTEGER PRIMARY KEY," +
                " title TEXT not null,"+
                "people INTEGER,"+
                "capacity INTEGER)");
        db.execSQL("CREATE TABLE IF NOT EXISTS chat(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "uid INTEGER not null," +
                "content TEXT not null," +
                "act INTEGER ,"+
                "rid INTEGER not null," +
                "FOREIGN KEY(rid) REFERENCES room(id) on DELETE CASCADE)");
        db.execSQL("CREATE TABLE IF NOT EXISTS user(uid INTEGER PRIMARY KEY," +
                "nickname TEXT not null," +
                "profile_image TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

    public void insertRoom(Room room) {
        //읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT OR IGNORE INTO room VALUES(?,?,?,?)",new String[]{""+room.getId(),room.getName(),""+room.getPeopleNum(),""+room.getCapacity()});
        db.close();
    }

    public int insertChat(int uid, String content, int act,int rid) {
        SQLiteDatabase db = getWritableDatabase();
//        db.execSQL("INSERT INTO chat VALUES(null," + uid + ",'"+content+"',"+rid+")");
        ContentValues values=new ContentValues();
        values.put("uid",uid);
        values.put("content",content);
        values.put("act",act);
        values.put("rid",rid);
        int cid=(int)db.insert("chat",null,values);
        db.close();
        return cid;
    }

    public void insertUser(int uid,String nickname,String profile_image){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("REPLACE INTO user VALUES(" + uid + ",?,?)",new String[]{nickname,profile_image});
        db.close();
    }
    public boolean findMyRoom(int rid){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor=db.rawQuery("SELECT id FROM room WHERE id=?",new String[]{""+rid});
        int rowNum=cursor.getCount();
        if(rowNum!=0){
            return true;
        }else{
            return false;
        }
    }
    public User getUser(int uid){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT uid,nickname,profile_image FROM user WHERE uid=?",new String[]{""+uid});
        cursor.moveToNext();
        return new User(cursor.getInt(0),cursor.getString(1),cursor.getString(2));
    }

    public ArrayList<Chat> getChatList(int roomId){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT c.id,c.content,c.act,c.uid,u.nickname,u.profile_image from chat AS c LEFT JOIN user AS u on c.uid=u.uid where c.rid=?",new String[]{""+roomId});
        ArrayList<Chat> chats = new ArrayList<>();
        while (cursor.moveToNext()) {
            chats.add(new Chat(cursor.getInt(0), cursor.getString(1),cursor.getInt(2), new User(cursor.getInt(3), cursor.getString(4), cursor.getString(5))));
        }
        return chats;
    }

    public ArrayList<Room> getRoomList(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor=db.rawQuery("SELECT * FROM room",null);
        ArrayList<Room> rooms=new ArrayList<>();
        while (cursor.moveToNext()){
            rooms.add(new Room(cursor.getInt(0),cursor.getString(1),cursor.getInt(2),cursor.getInt(3)));
        }
        return rooms;
    }

    public Room getRoom(int rid){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor=db.rawQuery("SELECT * FROM room WHERE id=?",new String[]{""+rid});
        cursor.moveToNext();
        return new Room(cursor.getInt(0),cursor.getString(1),cursor.getInt(2),cursor.getInt(3));
    }

    public void updateUserNum(int rid,int num){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE room SET people=people+? WHERE id=?",new Integer[]{num,rid});
        db.close();
    }

}