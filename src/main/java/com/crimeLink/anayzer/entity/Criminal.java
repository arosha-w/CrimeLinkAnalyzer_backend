package com.crimeLink.anayzer.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "criminals")
public class Criminal {

    @Id
    @Column(name = "id", length = 100)
    private String id;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "nic", length = 20)
    private String nic;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @Column(name = "secondary_contact", length = 20)
    private String secondaryContact;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "status", length = 50)
    private String status;

    // Constructors
    public Criminal() {}

    // Getters and Setters
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

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getSecondaryContact() {
        return secondaryContact;
    }

    public void setSecondaryContact(String secondaryContact) {
        this.secondaryContact = secondaryContact;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
