package tech.kandara.quizapp;

import java.util.List;

/**
 * Created by ravi on 12/26/2017.
 */

public class User {
    String email, firstname,lastname,photolink,gender,referral_code;
    int avail_cred, avail_energy, total_game,total_credit_won,user_lvl,age;
    boolean reviewed,fb_linked,gplus_linked;
    List<String> qnList;

    public List getQnList() {
        return qnList;
    }

    public void setQnList(List qnList) {
        this.qnList = qnList;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getReferral_code() {
        return referral_code;
    }

    public void setReferral_code(String referral_code) {
        this.referral_code = referral_code;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPhotolink() {
        return photolink;
    }

    public void setPhotolink(String photolink) {
        this.photolink = photolink;
    }

    public int getAvail_cred() {
        return avail_cred;
    }

    public void setAvail_cred(int avail_cred) {
        this.avail_cred = avail_cred;
    }

    public int getAvail_energy() {
        return avail_energy;
    }

    public void setAvail_energy(int avail_energy) {
        this.avail_energy = avail_energy;
    }

    public int getTotal_game() {
        return total_game;
    }

    public void setTotal_game(int total_game) {
        this.total_game = total_game;
    }

    public int getTotal_credit_won() {
        return total_credit_won;
    }

    public void setTotal_credit_won(int total_credit_won) {
        this.total_credit_won = total_credit_won;
    }

    public int getUser_lvl() {
        return user_lvl;
    }

    public void setUser_lvl(int user_lvl) {
        this.user_lvl = user_lvl;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    public boolean isFb_linked() {
        return fb_linked;
    }

    public void setFb_linked(boolean fb_linked) {
        this.fb_linked = fb_linked;
    }

    public boolean isGplus_linked() {
        return gplus_linked;
    }

    public void setGplus_linked(boolean gplus_linked) {
        this.gplus_linked = gplus_linked;
    }

    public User(String email, String firstname, String lastname, String photolink, int avail_cred, int avail_energy, int total_game, int total_credit_won, int user_lvl, boolean reviewed, boolean fb_linked, boolean gplus_linked) {
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.photolink = photolink;
        this.avail_cred = avail_cred;
        this.avail_energy = avail_energy;
        this.total_game = total_game;
        this.total_credit_won = total_credit_won;
        this.user_lvl = user_lvl;
        this.reviewed = reviewed;
        this.fb_linked = fb_linked;
        this.gplus_linked = gplus_linked;
    }

    public User() {
    }

    public User(String gender, int avail_cred, int avail_energy, int total_game, int total_credit_won, int user_lvl, int age, boolean reviewed, boolean fb_linked, boolean gplus_linked) {
        this.gender = gender;
        this.avail_cred = avail_cred;
        this.avail_energy = avail_energy;
        this.total_game = total_game;
        this.total_credit_won = total_credit_won;
        this.user_lvl = user_lvl;
        this.age = age;
        this.reviewed = reviewed;
        this.fb_linked = fb_linked;
        this.gplus_linked = gplus_linked;
    }

    public User(String email, String firstname, String lastname, String photolink, String gender, int avail_cred, int avail_energy, int total_game, int total_credit_won, int user_lvl, int age, boolean reviewed, boolean fb_linked, boolean gplus_linked) {
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.photolink = photolink;
        this.gender = gender;
        this.avail_cred = avail_cred;
        this.avail_energy = avail_energy;
        this.total_game = total_game;
        this.total_credit_won = total_credit_won;
        this.user_lvl = user_lvl;
        this.age = age;
        this.reviewed = reviewed;
        this.fb_linked = fb_linked;
        this.gplus_linked = gplus_linked;
    }
}
