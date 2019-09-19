package me.tykang.webCrawler.domain;

import java.util.Date;

public class CommentInfo {

    private Long id;
    private Double score;

    private String titile;
    private String comment;
    private String writer;
    private Long commentDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getTitile() {
        return titile;
    }

    public void setTitile(String titile) {
        this.titile = titile;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getWriter() {
        return writer;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public Long getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(Long commentDate) {
        this.commentDate = commentDate;
    }
}
