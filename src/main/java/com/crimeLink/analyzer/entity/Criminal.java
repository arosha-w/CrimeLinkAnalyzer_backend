package com.crimeLink.analyzer.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

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

    @Column(name = "risk_level", length = 50)
    private String riskLevel;

    @Column(name = "crime_history", columnDefinition = "TEXT")
    private String crimeHistory;

    @Column(name = "primary_photo_url", length = 500)
    private String primaryPhotoUrl;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "alias", length = 255)
    private String alias;

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

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getCrimeHistory() {
        return crimeHistory;
    }

    public void setCrimeHistory(String crimeHistory) {
        this.crimeHistory = crimeHistory;
    }

    public String getPrimaryPhotoUrl() {
        return primaryPhotoUrl;
    }

    public void setPrimaryPhotoUrl(String primaryPhotoUrl) {
        this.primaryPhotoUrl = primaryPhotoUrl;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
