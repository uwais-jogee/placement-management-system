package com.example.placementmanagementsystem.controller;

import com.example.placementmanagementsystem.enumeration.PlacementAuthRequestStatus;
import com.example.placementmanagementsystem.model.EmailToken;
import com.example.placementmanagementsystem.model.PlacementAuthRequest;
import com.example.placementmanagementsystem.repository.EmailTokenRepo;
import com.example.placementmanagementsystem.repository.PlacementAuthRequestRepo;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller class to handle company specific requests
 */
@Controller
public class CompanyController {

    @Autowired
    private PlacementAuthRequestRepo placementAuthRequestRepo;
    @Autowired
    private EmailTokenRepo emailTokenRepo;

    /**
     * Company view placement authorisation request endpoint
     *
     * @param token The token provided in the email link, to authenticate the request and provide access to view
     * @param model The model to add attributes to
     * @return The view placement authorisation request view
     */
    @GetMapping("/company/auth-request/view")
    public String viewAuthRequest(@RequestParam String token, Model model) {
        Optional<EmailToken> emailToken = emailTokenRepo.findByToken(token);
        if (emailToken.isPresent()) {
            if (emailToken.get().getExpiresAt().isAfter(LocalDateTime.now())) {
                model.addAttribute("authRequest", placementAuthRequestRepo.findByEmailToken(emailToken.get()));
                model.addAttribute("token", token);
                return "company/viewAuthRequest";
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token has expired");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token not found");
        }
    }

    /**
     * Endpoint for the company to approve a placement authorisation request
     *
     * @param id    The id of the placement authorisation request
     * @param token The token provided in the email link, to authenticate the request
     * @return The confirm response view
     */
    @PostMapping("/company/auth-request/company-approve")
    public String approveAuthRequest(@RequestParam Long id, @RequestParam String token) {
        Optional<EmailToken> emailToken = emailTokenRepo.findByToken(token);
        Optional<PlacementAuthRequest> authRequest = placementAuthRequestRepo.findById(id);

        // Check if the token is valid
        if (emailToken.isPresent()) {
            EmailToken emailTokenObj = emailToken.get();
            // Check if the token is valid and matches the token that has been passed back
            if (emailTokenObj.getToken().equals(token) && emailTokenObj.getExpiresAt().isAfter(LocalDateTime.now())) {
                // Check if the request is present
                if (authRequest.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Placement authorisation request not found");
                }
                PlacementAuthRequest placementAuthRequest = authRequest.get();
                if (placementAuthRequest.getStatus() == PlacementAuthRequestStatus.PENDING_COMPANY_APPROVAL) {
                    // Delete the token
                    placementAuthRequest.setEmailToken(null);
                    placementAuthRequestRepo.save(placementAuthRequest);
                    emailTokenRepo.delete(emailTokenObj);

                    // Set the status of the request to the next stage - pending final admin approval
                    placementAuthRequest.setStatus(PlacementAuthRequestStatus.PENDING_FINAL_ADMIN_APPROVAL);
                    placementAuthRequestRepo.save(placementAuthRequest);
                    return "company/confirmResponse";
                } else {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Request is not in the correct state for company approval, it may have already been approved or rejected");
                }
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token has expired");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token not found");
        }
    }

    /**
     * Endpoint for the company to reject a placement authorisation request
     *
     * @param id              The id of the placement authorisation request
     * @param token           The token provided in the email link, to authenticate the request
     * @param rejectionReason The reason for rejecting the request
     * @return The confirm response view
     */
    @PostMapping("/company/auth-request/company-reject")
    public String rejectAuthRequest(@RequestParam Long id, @RequestParam String token, @RequestParam String rejectionReason) {
        Optional<EmailToken> emailToken = emailTokenRepo.findByToken(token);
        Optional<PlacementAuthRequest> authRequest = placementAuthRequestRepo.findById(id);

        // Check if the token is valid
        if (emailToken.isPresent()) {
            EmailToken emailTokenObj = emailToken.get();
            // Check if the token is valid and matches the token that has been passed back
            if (emailTokenObj.getToken().equals(token) && emailTokenObj.getExpiresAt().isAfter(LocalDateTime.now())) {
                // Check if the request is present
                if (authRequest.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Placement authorisation request not found");
                }
                PlacementAuthRequest placementAuthRequest = authRequest.get();
                if (placementAuthRequest.getStatus() == PlacementAuthRequestStatus.PENDING_COMPANY_APPROVAL) {
                    // Delete the token
                    placementAuthRequest.setEmailToken(null);
                    placementAuthRequestRepo.save(placementAuthRequest);
                    emailTokenRepo.delete(emailTokenObj);

                    // Set the status of the request to rejected, and set the rejection reason
                    placementAuthRequest.setStatus(PlacementAuthRequestStatus.REJECTED_BY_COMPANY);
                    placementAuthRequest.setRejectionReason(rejectionReason);
                    placementAuthRequestRepo.save(placementAuthRequest);
                    return "company/confirmResponse";
                } else {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Request is not in the correct state for company rejection, it may have already been approved or rejected");
                }
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token has expired");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token not found");
        }
    }
}