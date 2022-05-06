package com.app.bible.knowbible.mvvm.model;

import android.graphics.Bitmap;

public class ArticleModel {
    private String article_name_ru;
    private String article_name_uk;
    private String article_name_en;

    private String article_link_ru;
    private String article_link_uk;
    private String article_link_en;

    private String image; //Это поле используется в случае получения данных из Firebase
    private Bitmap imageBitmap; //Это поле используется в случае получения данных из сохранённой БД
    private boolean is_article_new;
    private String new_article_text_color;

    public ArticleModel() {
        //Не удалять этот конструктор, он нужен
    }

    public String getArticle_link_ru() {
        return article_link_ru;
    }

    public String getArticle_link_uk() {
        return article_link_uk;
    }

    public String getArticle_link_en() {
        return article_link_en;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setImageBitmap(Bitmap imageBitmap) { this.imageBitmap = imageBitmap; }

    public String getArticle_name_ru() {
        return article_name_ru;
    }

    public String getArticle_name_uk() {
        return article_name_uk;
    }

    public String getArticle_name_en() {
        return article_name_en;
    }

    public String getImage() {
        return image;
    }

    public boolean isIs_article_new() {
        return is_article_new;
    }

    public String getNew_article_text_color() {
        return new_article_text_color;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }
}

