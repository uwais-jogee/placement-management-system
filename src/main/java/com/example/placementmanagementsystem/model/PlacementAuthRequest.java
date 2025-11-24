package com.example.placementmanagementsystem.model;

import com.example.placementmanagementsystem.enumeration.PlacementAuthRequestStatus;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Placement Authorisation Request model, used to store request details provided by student when requesting authorisation for their placement.
 * Maps to the form fields in the placement authorisation request form.
 */
@Entity
public class PlacementAuthRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate requestedOn;
    @Enumerated(EnumType.STRING)
    private PlacementAuthRequestStatus status;
    @ManyToOne
    @JoinColumn(name = "student_username")
    private Student student;
    // The company should be set if companySelect is not "Other"
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
    private String studentPhone;
    private String programmeOfStudy;
    private String internationStudent;
    private String visaStatus;
    // Can be id of existing company or Other
    private String companySelect;
    // Can be empty or the name of a new company
    private String companyNameOther;
    private String companyPlaceId;
    private String companyFormattedAddress;
    private String companyIndustry;
    private String companyWebAddress;
    private String companyContactName;
    private String companyContactEmail;
    private String companyContactPhone;
    private String roleTitle;
    private LocalDate placementStartDate;
    private LocalDate placementEndDate;
    private float hoursPerWeek;
    private float salary;
    @Column(columnDefinition = "TEXT") // Allows for long text max length of 65535 rather than 255
    private String roleDescription;
    private String remote;
    private String travelArrangements;
    private String residentialArrangements;
    private String personalAdjustments;
    @Column(columnDefinition = "TEXT") // Allows for long text max length of 65535 rather than 255
    private String rejectionReason;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "email_token_token")
    private EmailToken emailToken;

    public PlacementAuthRequest() {
        this.status = PlacementAuthRequestStatus.PENDING_INITIAL_ADMIN_APPROVAL;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getRequestedOn() {
        return requestedOn;
    }

    public void setRequestedOn(LocalDate requestedOn) {
        this.requestedOn = requestedOn;
    }

    public PlacementAuthRequestStatus getStatus() {
        return status;
    }

    public void setStatus(PlacementAuthRequestStatus status) {
        this.status = status;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getStudentPhone() {
        return studentPhone;
    }

    public void setStudentPhone(String studentPhone) {
        this.studentPhone = studentPhone;
    }

    public String getProgrammeOfStudy() {
        return programmeOfStudy;
    }

    public void setProgrammeOfStudy(String programmeOfStudy) {
        this.programmeOfStudy = programmeOfStudy;
    }

    public String getInternationStudent() {
        return internationStudent;
    }

    public void setInternationStudent(String internationStudent) {
        this.internationStudent = internationStudent;
    }

    public String getVisaStatus() {
        return visaStatus;
    }

    public void setVisaStatus(String visaStatus) {
        this.visaStatus = visaStatus;
    }

    public String getCompanySelect() {
        return companySelect;
    }

    public void setCompanySelect(String companySelect) {
        this.companySelect = companySelect;
    }

    public String getCompanyNameOther() {
        return companyNameOther;
    }

    public void setCompanyNameOther(String companyNameOther) {
        this.companyNameOther = companyNameOther;
    }

    public String getCompanyPlaceId() {
        return companyPlaceId;
    }

    public void setCompanyPlaceId(String companyPlaceId) {
        this.companyPlaceId = companyPlaceId;
    }

    public String getCompanyFormattedAddress() {
        return companyFormattedAddress;
    }

    public void setCompanyFormattedAddress(String companyFormattedAddress) {
        this.companyFormattedAddress = companyFormattedAddress;
    }

    public String getCompanyIndustry() {
        return companyIndustry;
    }

    public void setCompanyIndustry(String companyIndustry) {
        this.companyIndustry = companyIndustry;
    }

    public String getCompanyWebAddress() {
        return companyWebAddress;
    }

    public void setCompanyWebAddress(String companyWebAddress) {
        this.companyWebAddress = companyWebAddress;
    }

    public String getCompanyContactName() {
        return companyContactName;
    }

    public void setCompanyContactName(String companyContactName) {
        this.companyContactName = companyContactName;
    }

    public String getCompanyContactEmail() {
        return companyContactEmail;
    }

    public void setCompanyContactEmail(String companyContactEmail) {
        this.companyContactEmail = companyContactEmail;
    }

    public String getCompanyContactPhone() {
        return companyContactPhone;
    }

    public void setCompanyContactPhone(String companyContactPhone) {
        this.companyContactPhone = companyContactPhone;
    }

    public String getRoleTitle() {
        return roleTitle;
    }

    public void setRoleTitle(String roleTitle) {
        this.roleTitle = roleTitle;
    }

    public LocalDate getPlacementStartDate() {
        return placementStartDate;
    }

    public void setPlacementStartDate(LocalDate placementStartDate) {
        this.placementStartDate = placementStartDate;
    }

    public LocalDate getPlacementEndDate() {
        return placementEndDate;
    }

    public void setPlacementEndDate(LocalDate placementEndDate) {
        this.placementEndDate = placementEndDate;
    }

    public float getHoursPerWeek() {
        return hoursPerWeek;
    }

    public void setHoursPerWeek(float hoursPerWeek) {
        this.hoursPerWeek = hoursPerWeek;
    }

    public float getSalary() {
        return salary;
    }

    public void setSalary(float salary) {
        this.salary = salary;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    public void setRoleDescription(String roleDescription) {
        this.roleDescription = roleDescription;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public String getTravelArrangements() {
        return travelArrangements;
    }

    public void setTravelArrangements(String travelArrangements) {
        this.travelArrangements = travelArrangements;
    }

    public String getResidentialArrangements() {
        return residentialArrangements;
    }

    public void setResidentialArrangements(String residentialArrangements) {
        this.residentialArrangements = residentialArrangements;
    }

    public String getPersonalAdjustments() {
        return personalAdjustments;
    }

    public void setPersonalAdjustments(String personalAdjustments) {
        this.personalAdjustments = personalAdjustments;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public EmailToken getEmailToken() {
        return emailToken;
    }

    public void setEmailToken(EmailToken emailToken) {
        this.emailToken = emailToken;
    }
}