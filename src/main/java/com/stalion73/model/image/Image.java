package com.stalion73.model.image;

import com.stalion73.model.Business;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stalion73.model.AuditableEntity;

@Entity
@Table(name = "images")
public class Image extends AuditableEntity{
    
    @Column(unique = true)
    private String name;
    private String type;
    @Lob
    private byte[] img;

    private Integer compress;
    private Integer decompress;

    @JoinColumn(name = "business_id")
    @ManyToOne
    @JsonIgnore
    private Business business;

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

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    

    
}
