package com.example.placementmanagementsystem.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service class for Google Gemini AI operations
 */
@Service
public class GoogleGeminiService {

    @Value("${google.gemini.api-key}")
    private String googleApiKey;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Generate a cover letter using the Gemini API
     *
     * @param cv             The CV of the student
     * @param companyName    The name of the company the student is applying to
     * @param jobTitle       The title of the job the student is applying for
     * @param jobDescription The description of the job the student is applying for
     * @return The generated cover letter in string format
     */
    public String generateCoverLetter(String cv, String companyName, String jobTitle, String jobDescription) {
        // URL for the Gemini API
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + googleApiKey;

        // Build the request payload
        String payload = String.format("""
                {
                    "system_instruction": {
                        "parts": {
                            "text": "You are a professional career advisor and an expert in crafting compelling and tailored cover letters. Your task is to help university students applying for placements by creating cover letters that showcase their skills, experiences, and enthusiasm for the role while aligning closely with the job description. Your responses should adopt a professional tone, be concise, and follow standard cover letter formatting, including an introduction, a body, and a closing."
                        }
                    },
                    "contents": {
                        "parts": {
                            "text": "CV: %s\\nCompany Name: %s\\nJob Title: %s\\nJob Description: %s"
                        }
                    }
                }
                """, cv, companyName, jobTitle, jobDescription);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // Create request entity
        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        // Send the request
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        // Extract the response body
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(response.getBody(), JsonObject.class);
        String extractedText = jsonObject.getAsJsonArray("candidates").get(0).getAsJsonObject().getAsJsonObject("content").getAsJsonArray("parts").get(0).getAsJsonObject().get("text").getAsString();

        // Return the response body
        return extractedText;
    }
}