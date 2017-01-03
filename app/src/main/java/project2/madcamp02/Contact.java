package project2.madcamp02;

/**
 * Created by q on 2017-01-01.
 */

    public class Contact {
        private String title;
        private String phone;
        private String imgUrl;
        private String email;
        private String image;
        private int mongoId;

    public Contact(String Title){
        title = Title;
    }

    public Contact(String Title, String phone, String imgUrl, String email, String img){
        title = Title;
        this.phone = phone;
        this.imgUrl = imgUrl;
        this.email = email;
        this.image = img;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {return image;}

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