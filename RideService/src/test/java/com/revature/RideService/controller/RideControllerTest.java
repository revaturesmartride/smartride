package com.revature.RideService.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.RideService.dto.request.RideRequestDTO;
import com.revature.RideService.dto.response.RideResponseDTO;
import com.revature.RideService.service.RideService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RideController.class)
class RideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RideService rideService;

    @Test
    void requestRide_setsRiderIdFromHeader_returnsCreated() throws Exception {
        RideResponseDTO serviceResp = RideResponseDTO.builder()
                .riderId(10L)
                .pickupLocation("A")
                .dropLocation("B")
                .status("PENDING")
                .requestedTime(LocalDateTime.of(2026, 3, 19, 10, 0))
                .build();
        when(rideService.requestRide(any(RideRequestDTO.class))).thenReturn(serviceResp);

        RideRequestDTO body = RideRequestDTO.builder()
                .pickupLocation("A")
                .dropLocation("B")
                .build();

        mockMvc.perform(post("/api/rides/request")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "RIDER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.riderId").value(10))
                .andExpect(jsonPath("$.status").value("PENDING"));

        ArgumentCaptor<RideRequestDTO> captor = ArgumentCaptor.forClass(RideRequestDTO.class);
        verify(rideService).requestRide(captor.capture());
        assertEquals(10L, captor.getValue().getRiderId());
        assertEquals("A", captor.getValue().getPickupLocation());
        assertEquals("B", captor.getValue().getDropLocation());
    }

    @Test
    void acceptRide_callsService_returnsOk() throws Exception {
        when(rideService.acceptRide(55L, 99L)).thenReturn(RideResponseDTO.builder().rideId(777L).status("ACCEPTED").build());

        mockMvc.perform(put("/api/rides/55/accept")
                        .header("X-User-Id", "99")
                        .header("X-User-Role", "DRIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideId").value(777))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        verify(rideService).acceptRide(55L, 99L);
    }

    @Test
    void startRide_callsService_returnsOk() throws Exception {
        when(rideService.startRide(1L)).thenReturn(RideResponseDTO.builder().rideId(1L).status("STARTED").build());

        mockMvc.perform(put("/api/rides/1/start")
                        .header("X-User-Id", "99")
                        .header("X-User-Role", "DRIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("STARTED"));

        verify(rideService).startRide(1L);
    }

    @Test
    void completeRide_callsService_returnsOk() throws Exception {
        when(rideService.completeRide(2L)).thenReturn(RideResponseDTO.builder().rideId(2L).status("COMPLETED").fare(123.45).build());

        mockMvc.perform(put("/api/rides/2/complete")
                        .header("X-User-Id", "99")
                        .header("X-User-Role", "DRIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.fare").value(123.45));

        verify(rideService).completeRide(2L);
    }

    @Test
    void cancelRide_callsService_returnsOk() throws Exception {
        when(rideService.cancelRide(3L)).thenReturn(RideResponseDTO.builder().rideId(3L).status("CANCELLED").build());

        mockMvc.perform(put("/api/rides/3/cancel")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "RIDER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(rideService).cancelRide(3L);
    }

    @Test
    void getRide_callsService_returnsOk() throws Exception {
        when(rideService.getRide(7L)).thenReturn(RideResponseDTO.builder().rideId(7L).status("ACCEPTED").build());

        mockMvc.perform(get("/api/rides/7")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "RIDER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideId").value(7));

        verify(rideService).getRide(7L);
    }

    @Test
    void getRidesByRider_callsService_returnsList() throws Exception {
        when(rideService.getRidesByRider(10L)).thenReturn(List.of(
                RideResponseDTO.builder().rideId(1L).status("COMPLETED").build(),
                RideResponseDTO.builder().rideId(2L).status("CANCELLED").build()
        ));

        mockMvc.perform(get("/api/rides/rider/my-rides")
                        .header("X-User-Id", "10")
                        .header("X-User-Role", "RIDER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].rideId").value(1));

        verify(rideService).getRidesByRider(10L);
    }

    @Test
    void getRidesByDriver_callsService_returnsList() throws Exception {
        when(rideService.getRidesByDriver(99L)).thenReturn(List.of(
                RideResponseDTO.builder().rideId(1L).status("COMPLETED").build()
        ));

        mockMvc.perform(get("/api/rides/driver/my-rides")
                        .header("X-User-Id", "99")
                        .header("X-User-Role", "DRIVER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rideId").value(1));

        verify(rideService).getRidesByDriver(99L);
    }
}