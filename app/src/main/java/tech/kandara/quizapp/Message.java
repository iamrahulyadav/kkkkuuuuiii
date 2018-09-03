package tech.kandara.quizapp;

/**
 * Created by ravi on 12/26/2017.
 */

public class Message {
    String message,title,uid;
    boolean seen;

    public Message() {
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public Message(String message, String title, boolean seen) {

        this.message = message;
        this.title = title;
        this.seen = seen;
    }
}
