package com.num.wiz.aws.lambda.models;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class NumberWizardModel {

    private String user_id;
    private String saved_games;
    private String nickname;
    private String profile_badge;
    private Integer profile_score;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getSaved_games() {
        return saved_games;
    }

    public void setSaved_games(String saved_games) {
        this.saved_games = saved_games;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getProfile_badge() {
        return profile_badge;
    }

    public void setProfile_badge(String profile_badge) {
        this.profile_badge = profile_badge;
    }

    public Integer getProfile_score() {
        return profile_score;
    }

    public void setProfile_score(Integer profile_score) {
        this.profile_score = profile_score;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NumberWizardModel{");
        sb.append("user_id='").append(user_id).append('\'');
        sb.append(", saved_games='").append(saved_games).append('\'');
        sb.append(", nickname='").append(nickname).append('\'');
        sb.append(", profile_badge='").append(profile_badge).append('\'');
        sb.append(", profile_score=").append(profile_score);
        sb.append('}');
        return sb.toString();
    }
}
