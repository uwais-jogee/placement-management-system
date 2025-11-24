package com.example.placementmanagementsystem.model;

import com.example.placementmanagementsystem.enumeration.PlacementStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Placement model class representing a Student's placement at a company
 */
@Entity
public class Placement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private PlacementStatus status;
    @ManyToOne
    @JoinColumn(name = "student_username")
    private Student student;
    @ManyToOne
    @JoinColumn(name = "tutor_username")
    private Tutor tutor;
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
    @OneToOne
    private PlacementAuthRequest placementAuthRequest;
    @OneToOne(cascade = CascadeType.ALL)
    private PlacementEvaluation placementEvaluation;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "placement_id")
    private List<Message> messageChat = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "placement_id")
    private List<Visit> visits = new ArrayList<>();
    private LocalDate startDate;
    private LocalDate endDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Tutor getTutor() {
        return tutor;
    }

    public void setTutor(Tutor tutor) {
        this.tutor = tutor;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public PlacementAuthRequest getPlacementAuthRequest() {
        return placementAuthRequest;
    }

    public void setPlacementAuthRequest(PlacementAuthRequest placementAuthRequest) {
        this.placementAuthRequest = placementAuthRequest;
    }

    public PlacementEvaluation getPlacementEvaluation() {
        return placementEvaluation;
    }

    public void setPlacementEvaluation(PlacementEvaluation placementEvaluation) {
        this.placementEvaluation = placementEvaluation;
    }

    public List<Message> getMessageChat() {
        return messageChat;
    }

    public void setMessageChat(List<Message> messageChat) {
        this.messageChat = messageChat;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public PlacementStatus getStatus() {
        return status;
    }

    public void setStatus(PlacementStatus status) {
        this.status = status;
    }

    public List<Visit> getVisits() {
        return visits;
    }

    public void setVisits(List<Visit> visits) {
        this.visits = visits;
    }
}