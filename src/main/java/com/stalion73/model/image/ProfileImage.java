package com.stalion73.model.image;

import com.stalion73.model.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.stalion73.model.AuditableEntity;

@Entity
@Table(name = "profile_images")
public class ProfileImage extends AuditableEntity{
    
    @Column(unique = true)
    private String name;
    private String type;
    @Lob
    private byte[] img;

    private Integer compress;
    private Integer decompress;

    @JoinColumn(name = "username")
    @OneToOne
    private User user;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte[] getImg() {
        return img;
    }

    public void setImg(byte[] img) {
        this.img = img;
    }

    public Integer getCompress() {
        return compress;
    }

    public void setCompress(Integer compress) {
        this.compress = compress;
    }

    public Integer getDecompress() {
        return decompress;
    }

    public void setDecompress(Integer decompress) {
        this.decompress = decompress;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    

    
}

