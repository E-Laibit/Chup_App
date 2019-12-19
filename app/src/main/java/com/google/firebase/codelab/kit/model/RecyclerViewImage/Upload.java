package com.google.firebase.codelab.kit.model.RecyclerViewImage;

public class Upload {
    private String mName;
    private String mTranslate;
    private String mImageUrl;


    public Upload(){
        //to work with the firebase database
        //needed dont delete
    }

    public Upload(String name,String translate ,String imageurl) {
        if (name.trim().equals("")){
            name = "no name";
        }


        mName = name;
        mImageUrl = imageurl;
        mTranslate = translate;
    }

    public String getName(){
        return mName;
    }

    public void setName(String name){
        mName= name;
    }

    public String getTranslate() {return mTranslate;}

    public void setTranslate(String translate){mTranslate = translate;}

    public String getImageUrl(){
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl){
        mImageUrl = imageUrl;
    }
}
