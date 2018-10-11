package com.inmobi.platform.elasticyarn.gateway.entity;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity // This tells Hibernate to make a table out of this class
@Getter
@Setter
public class Group {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer id;


    private String groupName;

    @Column(length = 555)
    private String permission;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }





}
