

import com.restaurant.reservation.converter.ReservationConverter;
import com.restaurant.reservation.dto.ReservationRequest;
import com.restaurant.reservation.dto.ReservationResponse;
import com.restaurant.reservation.exception.ResourceNotFoundException;
import com.restaurant.reservation.model.Reservation;
import com.restaurant.reservation.model.Table;
import com.restaurant.reservation.model.User;
import com.restaurant.reservation.repository.ReservationRepository;
import com.restaurant.reservation.repository.TableRepository;
import com.restaurant.reservation.repository.UserRepository;
import com.restaurant.reservation.security.services.UserDetailsImpl;
import com.restaurant.reservation.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private TableRepository tableRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationConverter reservationConverter;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private User user;
    private Table table;
    private Reservation reservation;
    private ReservationRequest reservationRequest;
    private ReservationResponse reservationResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);

        table = new Table();
        table.setId(1L);
        table.setAvailable(true);

        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUser(user);
        reservation.setTable(table);
        reservation.setReservationDateTime(LocalDateTime.now());
        reservation.setGuestCount(2);
        reservation.setCancelled(false);

        reservationRequest = new ReservationRequest();
        reservationRequest.setReservationDateTime(LocalDateTime.now());
        reservationRequest.setGuestCount(2);
        reservationRequest.setSpecialRequests("Near window");

        reservationResponse = new ReservationResponse();
        reservationResponse.setId(1L);

        UserDetailsImpl userDetails = new UserDetailsImpl(user.getId(), "testUser", "password");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetAllReservations() {
        when(reservationRepository.findAll()).thenReturn(Arrays.asList(reservation));
        when(reservationConverter.toDto(reservation)).thenReturn(reservationResponse);

        List<ReservationResponse> responses = reservationService.getAllReservations();
        assertEquals(1, responses.size());
    }

    @Test
    void testGetCurrentUserReservations() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(reservationRepository.findByUser(user)).thenReturn(Arrays.asList(reservation));
        when(reservationConverter.toDto(reservation)).thenReturn(reservationResponse);

        List<ReservationResponse> responses = reservationService.getCurrentUserReservations();
        assertEquals(1, responses.size());
    }

    @Test
    void testCreateReservation_Success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(tableRepository.findById(table.getId())).thenReturn(Optional.of(table));
        when(tableRepository.findAvailableTablesForTimeSlot(any(), any())).thenReturn(List.of(table));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        when(reservationConverter.toDto(reservation)).thenReturn(reservationResponse);

        ReservationResponse response = reservationService.createReservation(table.getId(), reservationRequest);
        assertNotNull(response);
    }

    @Test
    void testCreateReservation_TableNotAvailable() {
        table.setAvailable(false);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(tableRepository.findById(table.getId())).thenReturn(Optional.of(table));

        Exception exception = assertThrows(RuntimeException.class, () ->
                reservationService.createReservation(table.getId(), reservationRequest));
        assertEquals("Table is not available", exception.getMessage());
    }

    @Test
    void testCancelReservation_Success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(reservationRepository.findByUserAndTableIdAndCancelledFalse(user, table.getId()))
                .thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);
        when(reservationConverter.toDto(reservation)).thenReturn(reservationResponse);

        ReservationResponse response = reservationService.cancelReservation(table.getId());
        assertNotNull(response);
        assertTrue(reservation.isCancelled());
    }

    @Test
    void testCancelReservation_NotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(reservationRepository.findByUserAndTableIdAndCancelledFalse(user, table.getId()))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                reservationService.cancelReservation(table.getId()));
        assertEquals("Active reservation not found for this table", exception.getMessage());
    }
}
