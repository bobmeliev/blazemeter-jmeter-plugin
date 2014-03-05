package com.blazemeter.jmeter.entities;

/**
 * Created by dzmitrykashlach on 3/4/14.
 */
public class Users {
    private String id;
    private String name;
    private String mail;
    private String access;
    private String login;
    private String created;
    private boolean enabled;
    private Plan plan;

    public Users(String id, String name, String mail, String access, String login, String created, boolean enabled, Plan plan) {
        this.id = id;
        this.name = name;
        this.mail = mail;
        this.access = access;
        this.login = login;
        this.created = created;
        this.enabled = enabled;
        this.plan = plan;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Plan getPlan() {
        return this.plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public String toString() {
        return String.format("%s: %d users", name, plan.getConcurrency());
    }
}
