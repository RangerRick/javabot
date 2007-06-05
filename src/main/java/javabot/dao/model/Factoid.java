package javabot.dao.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

//
// User: joed
// Date: Apr 11, 2007
// Time: 2:22:19 PM

//
@Entity
@Table(name = "factoids")
public class Factoid implements Serializable {
    @Id
    @Column(name = "`id`")
    @GeneratedValue(strategy = GenerationType.AUTO)
//    @GeneratedValue(generator = "factoid_sequence")
//    @SequenceGenerator(name = "factoid_sequence", sequenceName = "factoid_sequence", allocationSize = 1)
    private Long id;
    @Column(name = "`name`", length = 255)
    private String name;
    @Column(name = "`value`", length = 2000)
    private String value;
    @Column(name = "`username`", length = 100)
    private String userName;
    @Column(name = "`updated`")
    private Date updated;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }
}
