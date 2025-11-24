package com.example.placementmanagementsystem.model;

import com.example.placementmanagementsystem.enumeration.PlacementStatus;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Company model to store company details, placements, and evaluations.
 */
@Entity
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String companyName;
    @ManyToOne
    @JoinColumn(name = "address_place_id")
    private Address address;
    private String industry;
    private String webAddress;
    @OneToMany(mappedBy = "company")
    private List<Placement> placements = new ArrayList<>();
    @OneToMany(mappedBy = "company")
    private List<PlacementEvaluation> evaluations = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getWebAddress() {
        return webAddress;
    }

    public void setWebAddress(String webAddress) {
        this.webAddress = webAddress;
    }

    public List<Placement> getPlacements() {
        return placements;
    }

    public void setPlacements(List<Placement> placements) {
        this.placements = placements;
    }

    public List<PlacementEvaluation> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(List<PlacementEvaluation> evaluations) {
        this.evaluations = evaluations;
    }

    public float getAverageOverallRating() {
        if (this.evaluations.isEmpty()) {
            return 0;
        }
        float sum = 0;
        for (PlacementEvaluation evaluation : this.evaluations) {
            sum += evaluation.getOverallRating();
        }
        return sum / this.evaluations.size();
    }

    public List<Placement> getCurrentPlacements() {
        List<Placement> currentPlacements = new ArrayList<>();
        for (Placement placement : this.placements) {
            if (placement.getStatus() == PlacementStatus.IN_PROGRESS || placement.getStatus() == PlacementStatus.UPCOMING) {
                currentPlacements.add(placement);
            }
        }
        return currentPlacements;
    }
}