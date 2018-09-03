package tech.kandara.quizapp;

import java.util.ArrayList;

/**
 * Created by Abinash on 6/8/2017.
 */

public class Question {

    private String question;
    private int level;
    private String rightAnswer;
    private ArrayList<String> wronganswer;
    private int category;
    private int totalRequest;

    public int getTotalRequest() {
        return totalRequest;
    }

    public void setTotalRequest(int totalRequest) {
        this.totalRequest = totalRequest;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public int getLevel() {
        return level;
    }

    public String getRightAnswer() {
        return rightAnswer;
    }

    public ArrayList<String> getWronganswer() {
        return wronganswer;
    }

    public int getCategory() {
        return category;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setRightAnswer(String rightAnswer) {
        this.rightAnswer = rightAnswer;
    }

    public void setWronganswer(ArrayList<String> wronganswer) {
        this.wronganswer = wronganswer;
    }

    public void setCategory(int category) {
        this.category = category;
    }
}
