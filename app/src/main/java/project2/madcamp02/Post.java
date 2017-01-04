package project2.madcamp02;

/**
 * Created by Madstein on 2017-01-04.
 */

public class Post {
    String title;
    String content;
    String author; // mongo_id of author
    String id;

    public Post(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changeContent(String content) {
        this.content = content;
    }
}
