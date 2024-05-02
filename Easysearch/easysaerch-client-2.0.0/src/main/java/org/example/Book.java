package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.org.apache.xml.internal.serialize.Serializer;

import java.io.Serializable;

public class Book implements Serializable {
    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("author")
    private String author;

    @JsonProperty("year")
    private Integer year;

    @JsonProperty("publisher")
    private String publisher;

    @JsonProperty("ratings")
    private Float ratings;

    // 添加带有 @JsonProperty 注解的属性的构造函数
    public Book(@JsonProperty("title") String title,
                @JsonProperty("description") String description,
                @JsonProperty("author") String author,
                @JsonProperty("year") Integer year,
                @JsonProperty("publisher") String publisher,
                @JsonProperty("ratings") Float ratings) {
        this.title = title;
        this.description = description;
        this.author = author;
        this.year = year;
        this.publisher = publisher;
        this.ratings = ratings;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Float getRatings() {
        return ratings;
    }

    public void setRatings(Float ratings) {
        this.ratings = ratings;
    }

    @Override
    public String toString() {
        return "Book{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", author='" + author + '\'' +
                ", year=" + year +
                ", publisher='" + publisher + '\'' +
                ", ratings=" + ratings +
                '}';
    }
}