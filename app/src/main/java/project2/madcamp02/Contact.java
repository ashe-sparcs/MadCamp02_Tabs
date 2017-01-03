package project2.madcamp02;

import android.graphics.Bitmap;

/**
 * Created by q on 2017-01-01.
 */

    public class Contact {
    private String title;
    private String phone;
    private String imgUrl;
    private String email;
    private Bitmap img;
    private int mongoId;

    public Contact(String Title){
        title = Title;
    }

    public Contact(String Title, String phone, String imgUrl, String email, Bitmap img){
        title = Title;
        this.phone = phone;
        this.imgUrl = imgUrl;
        this.email = email;
        this.img = img;
    }

    public String getTitle() {
        return title;
    }

    public Bitmap getImg() {return img;}

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhone() {
        return phone;
    }

    public int getMongoId() {
        return mongoId;
    }
}