package project2.madcamp02;

/**
 * Created by q on 2017-01-01.
 */

    public class Contact {
    private String title;
    private String phone;
    private String name;
    private int birth;
    private boolean gender;
    private int mongoId;

    public Contact(String Title){
        title = Title;
    }

    public Contact(String Title, String Phone){
        title = Title;
        phone = Phone;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public int getBirth() {
        return birth;
    }

    public boolean isGender() {
        return gender;
    }

    public int getMongoId() {
        return mongoId;
    }
}