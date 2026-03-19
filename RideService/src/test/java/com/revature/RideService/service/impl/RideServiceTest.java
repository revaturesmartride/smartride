package com.revature.RideService.service.impl;

import com.revature.RideService.client.UserServiceClient;
import com.revature.RideService.dto.request.RideRequestDTO;
import com.revature.RideService.dto.response.RideResponseDTO;
import com.revature.RideService.dto.response.UserResponse;
import com.revature.RideService.entity.Ride;
import com.revature.RideService.entity.RideRequest;
import com.revature.RideService.entity.RideStatus;
import com.revature.RideService.exception.RideNotFoundException;
import com.revature.RideService.kafka.producer.RideEventProducer;
import com.revature.RideService.repository.RideRepository;
import com.revature.RideService.repository.RideRequestRepository;
import com.revature.RideService.util.FareCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private RideRequestRepository rideRequestRepository;

    @Mock
    private RideEventProducer rideEventProducer;

    @Mock
    private FareCalculator fareCalculator;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private RideServiceImpl rideService;

    @Captor
    private ArgumentCaptor<RideRequest> rideRequestCaptor;

    @Captor
    private ArgumentCaptor<Ride> rideCaptor;

    private UserResponse riderUser;
    private UserResponse driverUser;

    @BeforeEach
    void setUp() {
        riderUser = UserResponse.builder()
                .userId(10L)
                .userRole("RIDER")
                .build();
        driverUser = UserResponse.builder()
                .userId(99L)
                .userRole("DRIVER")
                .build();
    }

    @Test
    void requestRide_persistsRideRequest_publishesEvent_returnsResponse() {
        RideRequestDTO dto = RideRequestDTO.builder()
                .riderId(10L)
                .pickupLocation("A")
                .dropLocation("B")
                .build();

        when(userServiceClient.getUserById(10L)).thenReturn(riderUser);
        when(rideRequestRepository.save(any(RideRequest.class))).thenAnswer(inv -> {
            RideRequest rr = inv.getArgument(0, RideRequest.class);
            rr.setId(123L);
            rr.setRequestedTime(LocalDateTime.of(2026, 3, 19, 10, 0));
            return rr;
        });

        RideResponseDTO resp = rideService.requestRide(dto);

        assertNotNull(resp);
        assertEquals(10L, resp.getRiderId());
        assertEquals("A", resp.getPickupLocation());
        assertEquals("B", resp.getDropLocation());
        assertEquals("PENDING", resp.getStatus());
        assertNotNull(resp.getRequestedTime());

        verify(rideRequestRepository).save(rideRequestCaptor.capture());
        RideRequest saved = rideRequestCaptor.getValue();
        assertEquals(10L, saved.getRiderId());
        assertEquals("A", saved.getPickupLocation());
        assertEquals("B", saved.getDropLocation());
        assertEquals("PENDING", saved.getStatus());

        verify(rideEventProducer, times(1)).publishRideCreated(any());
        verifyNoInteractions(rideRepository);
    }

    @Test
    void requestRide_whenRiderMissing_throwsNotFound() {
        RideRequestDTO dto = RideRequestDTO.builder()
                .riderId(10L)
                .pickupLocation("A")
                .dropLocation("B")
                .build();

        when(userServiceClient.getUserById(10L)).thenReturn(null);

        assertThrows(RideNotFoundException.class, () -> rideService.requestRide(dto));
        verifyNoInteractions(rideRequestRepository, rideRepository, rideEventProducer);
    }

    @Test
    void requestRide_whenUserNotRider_throwsIllegalState() {
        RideRequestDTO dto = RideRequestDTO.builder()
                .riderId(10L)
                .pickupLocation("A")
                .dropLocation("B")
                .build();

        when(userServiceClient.getUserById(10L)).thenReturn(UserResponse.builder().userId(10L).userRole("DRIVER").build());

        assertThrows(IllegalStateException.class, () -> rideService.requestRide(dto));
        verifyNoInteractions(rideRequestRepository, rideRepository, rideEventProducer);
    }

    @Test
    void requestRide_whenPickupBlank_throwsIllegalArgument() {
        RideRequestDTO dto = RideRequestDTO.builder()
                .riderId(10L)
                .pickupLocation(" ")
                .dropLocation("B")
                .build();

        when(userServiceClient.getUserById(10L)).thenReturn(riderUser);

        assertThrows(IllegalArgumentException.class, () -> rideService.requestRide(dto));
        verifyNoInteractions(rideRequestRepository, rideRepository, rideEventProducer);
    }

    @Test
    void acceptRide_persistsRideAndUpdatesRequest_publishesEvent_returnsResponse() {
        RideRequest rideRequest = RideRequest.builder()
                .id(55L)
                .riderId(10L)
                .pickupLocation("A")
                .dropLocation("B")
                .status("PENDING")
                .requestedTime(LocalDateTime.of(2026, 3, 19, 9, 0))
                .build();

        when(userServiceClient.getUserById(99L)).thenReturn(driverUser);
        when(rideRequestRepository.findById(55L)).thenReturn(Optional.of(rideRequest));
        when(rideRequestRepository.save(any(RideRequest.class))).thenAnswer(inv -> inv.getArgument(0, RideRequest.class));
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> {
            Ride r = inv.getArgument(0, Ride.class);
            r.setId(777L);
            return r;
        });

        RideResponseDTO resp = rideService.acceptRide(55L, 99L);

        assertEquals(777L, resp.getRideId());
        assertEquals(10L, resp.getRiderId());
        assertEquals(99L, resp.getDriverId());
        assertEquals("ACCEPTED", resp.getStatus());

        verify(rideRequestRepository).save(rideRequestCaptor.capture());
        assertEquals("ACCEPTED", rideRequestCaptor.getValue().getStatus());

        verify(rideRepository).save(rideCaptor.capture());
        assertEquals(99L, rideCaptor.getValue().getDriverId());
        assertEquals(RideStatus.ACCEPTED, rideCaptor.getValue().getStatus());
        assertNotNull(rideCaptor.getValue().getRideRequest());

        verify(rideEventProducer, times(1)).publishRideAssigned(any());
    }

    @Test
    void acceptRide_whenDriverMissing_throwsNotFound() {
        when(userServiceClient.getUserById(99L)).thenReturn(null);
        assertThrows(RideNotFoundException.class, () -> rideService.acceptRide(55L, 99L));
        verifyNoInteractions(rideRequestRepository, rideRepository, rideEventProducer);
    }

    @Test
    void acceptRide_whenRideRequestNotPending_throwsIllegalState() {
        RideRequest rideRequest = RideRequest.builder()
                .id(55L)
                .riderId(10L)
                .pickupLocation("A")
                .dropLocation("B")
                .status("CANCELLED")
                .requestedTime(LocalDateTime.now())
                .build();

        when(userServiceClient.getUserById(99L)).thenReturn(driverUser);
        when(rideRequestRepository.findById(55L)).thenReturn(Optional.of(rideRequest));

        assertThrows(IllegalStateException.class, () -> rideService.acceptRide(55L, 99L));
        verify(rideRequestRepository, never()).save(any());
        verifyNoInteractions(rideRepository, rideEventProducer);
    }

    @Test
    void startRide_whenAccepted_transitionsToStarted_andPublishesEvent() {
        Ride ride = Ride.builder()
                .id(1L)
                .riderId(10L)
                .driverId(99L)
                .pickupLocation("A")
                .dropLocation("B")
                .status(RideStatus.ACCEPTED)
                .requestedTime(LocalDateTime.now())
                .rideRequest(RideRequest.builder().id(55L).build())
                .build();

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0, Ride.class));

        RideResponseDTO resp = rideService.startRide(1L);

        assertEquals("STARTED", resp.getStatus());
        assertNotNull(resp.getStartTime());
        verify(rideEventProducer, times(1)).publishRideStarted(any());
    }

    @Test
    void startRide_whenWrongStatus_throwsIllegalState() {
        Ride ride = Ride.builder()
                .id(1L)
                .riderId(10L)
                .driverId(99L)
                .pickupLocation("A")
                .dropLocation("B")
                .status(RideStatus.STARTED)
                .requestedTime(LocalDateTime.now())
                .rideRequest(RideRequest.builder().id(55L).build())
                .build();

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

        assertThrows(IllegalStateException.class, () -> rideService.startRide(1L));
        verify(rideRepository, never()).save(any());
        verifyNoMoreInteractions(rideEventProducer);
    }

    @Test
    void completeRide_whenStarted_setsFareAndCompleted_andPublishesEvent() {
        Ride ride = Ride.builder()
                .id(2L)
                .riderId(10L)
                .driverId(99L)
                .pickupLocation("A")
                .dropLocation("B")
                .status(RideStatus.STARTED)
                .requestedTime(LocalDateTime.now())
                .startTime(LocalDateTime.now().minusMinutes(3))
                .rideRequest(RideRequest.builder().id(55L).build())
                .build();

        when(rideRepository.findById(2L)).thenReturn(Optional.of(ride));
        when(fareCalculator.calculate("A", "B")).thenReturn(123.45);
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0, Ride.class));

        RideResponseDTO resp = rideService.completeRide(2L);

        assertEquals("COMPLETED", resp.getStatus());
        assertEquals(123.45, resp.getFare());
        assertNotNull(resp.getEndTime());
        verify(rideEventProducer, times(1)).publishRideCompleted(any());
    }

    @Test
    void cancelRide_whenInProgress_cancelsRide_andCancelsLinkedRideRequest() {
        RideRequest rideRequest = RideRequest.builder()
                .id(55L)
                .riderId(10L)
                .pickupLocation("A")
                .dropLocation("B")
                .status("ACCEPTED")
                .requestedTime(LocalDateTime.now())
                .build();
        Ride ride = Ride.builder()
                .id(3L)
                .riderId(10L)
                .driverId(99L)
                .pickupLocation("A")
                .dropLocation("B")
                .status(RideStatus.STARTED)
                .requestedTime(LocalDateTime.now())
                .rideRequest(rideRequest)
                .build();

        when(rideRepository.findById(3L)).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0, Ride.class));
        when(rideRequestRepository.save(any(RideRequest.class))).thenAnswer(inv -> inv.getArgument(0, RideRequest.class));

        RideResponseDTO resp = rideService.cancelRide(3L);

        assertEquals("CANCELLED", resp.getStatus());
        verify(rideRequestRepository).save(rideRequestCaptor.capture());
        assertEquals("CANCELLED", rideRequestCaptor.getValue().getStatus());
    }

    @Test
    void getRide_whenMissing_throwsNotFound() {
        when(rideRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RideNotFoundException.class, () -> rideService.getRide(999L));
    }

    @Test
    void getRidesByRider_mapsEntitiesToDtos() {
        Ride r1 = Ride.builder()
                .id(1L).riderId(10L).driverId(99L)
                .pickupLocation("A").dropLocation("B")
                .status(RideStatus.COMPLETED)
                .requestedTime(LocalDateTime.now())
                .rideRequest(RideRequest.builder().id(55L).build())
                .fare(50.0)
                .build();
        when(rideRepository.findByRiderId(10L)).thenReturn(List.of(r1));

        List<RideResponseDTO> out = rideService.getRidesByRider(10L);
        assertEquals(1, out.size());
        assertEquals(1L, out.get(0).getRideId());
        assertEquals("COMPLETED", out.get(0).getStatus());
    }
}