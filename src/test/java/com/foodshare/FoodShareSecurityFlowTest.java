package com.foodshare;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:foodshare_security_test;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "foodshare.admin.email=",
    "foodshare.admin.password=",
    "foodshare.mail.from=",
    "foodshare.ngo-notification-emails="
})
@AutoConfigureMockMvc
@Transactional
class FoodShareSecurityFlowTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private DonorRepository donors;

    @Autowired
    private NgoRepository ngos;

    // Prevent real emails during automated testing.
    @MockitoBean
    private NotificationService emailService;

    private final String donorEmail = "testdonor@example.com";
    private final String ngoEmail = "testngo@example.com";

    private Donor createDonor() {
        return donors.save(new Donor(
            "Test Donor",
            donorEmail,
            "unused-test-password-hash",
            "9876543210",
            "Individual",
            "Test Organisation",
            "Test Pickup Address"
        ));
    }

    private Ngo createNgo(
            String email,
            String registrationNumber,
            boolean verified
    ) {

        Ngo ngo = new Ngo(
            "Test Helping NGO",
            email,
            "unused-test-password-hash",
            "9876501234",
            registrationNumber,
            "NGO Coordinator",
            "Test NGO Address"
        );

        ngo.setVerified(verified);
        return ngos.save(ngo);
    }

    private long publishDonation() throws Exception {

        String deadline = LocalDateTime
            .now(ZoneId.of("Asia/Kolkata"))
            .plusDays(2)
            .format(DateTimeFormatter.ofPattern(
                "yyyy-MM-dd'T'HH:mm"
            ));

        String body = """
            {
              "name":"Test Donor",
              "phone":"9876543210",
              "food":"Test Vegetarian Meals",
              "type":"Vegetarian",
              "unit":"Packets",
              "quantity":20,
              "location":"Private Test Pickup Address",
              "deadline":"%s",
              "notes":"Freshly prepared; chilled; contains dairy"
            }
            """.formatted(deadline);

        MvcResult result = mvc.perform(
                post("/api/donations")
                    .with(user(donorEmail).roles("DONOR"))
                    .with(csrf())
                    .contentType("application/json")
                    .content(body)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("AVAILABLE"))
            .andReturn();

        return mapper.readTree(
            result.getResponse().getContentAsString()
        ).get("id").asLong();
    }

    @Test
    void unauthenticatedUsersCannotAccessPrivateApis()
            throws Exception {

        mvc.perform(get("/api/donations/mine"))
            .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/ngo/donations/mine"))
            .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/admin/stats"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void userRolesCannotAccessOtherDashboards()
            throws Exception {

        mvc.perform(
                get("/api/admin/stats")
                    .with(user(donorEmail).roles("DONOR"))
            )
            .andExpect(status().isForbidden());

        mvc.perform(
                get("/api/donations/mine")
                    .with(user(ngoEmail).roles("NGO"))
            )
            .andExpect(status().isForbidden());

        mvc.perform(
                get("/api/ngo/donations/mine")
                    .with(user(donorEmail).roles("DONOR"))
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void publicFoodApiDoesNotExposeDonorInformation()
            throws Exception {

        createDonor();
        publishDonation();

        mvc.perform(get("/api/donations/available"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].food")
                .value("Test Vegetarian Meals"))
            .andExpect(jsonPath("$[0].phone").doesNotExist())
            .andExpect(jsonPath("$[0].name").doesNotExist())
            .andExpect(jsonPath("$[0].location").doesNotExist())
            .andExpect(jsonPath("$[0].notes").doesNotExist());
    }

    @Test
    void unverifiedNgoCannotAcceptDonation()
            throws Exception {

        createDonor();
        createNgo(ngoEmail, "TEST-NGO-001", false);

        long donationId = publishDonation();

        mvc.perform(
                post("/api/ngo/donations/{id}/accept", donationId)
                    .with(user(ngoEmail).roles("NGO"))
                    .with(csrf())
            )
            .andExpect(status().isForbidden());
    }

    @Test
    void verifiedNgoCanAcceptAndCompletePickup()
            throws Exception {

        createDonor();
        createNgo(ngoEmail, "TEST-NGO-002", true);

        String otherNgo = "otherngo@example.com";

        createNgo(
            otherNgo,
            "TEST-NGO-003",
            true
        );

        long donationId = publishDonation();

        // Correct NGO accepts the food.
        mvc.perform(
                post("/api/ngo/donations/{id}/accept", donationId)
                    .with(user(ngoEmail).roles("NGO"))
                    .with(csrf())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ACCEPTED"));

        // Donor can see the accepting NGO.
        mvc.perform(
                get("/api/donations/mine")
                    .with(user(donorEmail).roles("DONOR"))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status")
                .value("ACCEPTED"))
            .andExpect(jsonPath("$[0].acceptedNgoName")
                .value("Test Helping NGO"))
            .andExpect(jsonPath("$[0].acceptedNgoPhone")
                .value("9876501234"));

        // Another NGO cannot complete this pickup.
        mvc.perform(
                post("/api/ngo/donations/{id}/complete", donationId)
                    .with(user(otherNgo).roles("NGO"))
                    .with(csrf())
            )
            .andExpect(status().isConflict());

        // Donor cannot use an NGO's completion API.
        mvc.perform(
                post("/api/ngo/donations/{id}/complete", donationId)
                    .with(user(donorEmail).roles("DONOR"))
                    .with(csrf())
            )
            .andExpect(status().isForbidden());

        // Correct NGO completes the pickup.
        mvc.perform(
                post("/api/ngo/donations/{id}/complete", donationId)
                    .with(user(ngoEmail).roles("NGO"))
                    .with(csrf())
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COLLECTED"));

        // Donor sees the completed status.
        mvc.perform(
                get("/api/donations/mine")
                    .with(user(donorEmail).roles("DONOR"))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].status")
                .value("COLLECTED"));

        // Duplicate pickup completion must fail.
        mvc.perform(
                post("/api/ngo/donations/{id}/complete", donationId)
                    .with(user(ngoEmail).roles("NGO"))
                    .with(csrf())
            )
            .andExpect(status().isConflict());
    }
}
