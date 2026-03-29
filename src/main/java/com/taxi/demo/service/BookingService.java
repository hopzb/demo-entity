package com.taxi.demo.service;

import com.taxi.demo.dto.booking.BookingResponse;
import com.taxi.demo.dto.booking.CreateBookingRequest;
import com.taxi.demo.dto.booking.UpdateBookingRequest;
import com.taxi.demo.entity.Booking;
import com.taxi.demo.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingResponse createBooking(Long passengerId, CreateBookingRequest request) {
        validateCreateRequest(request);

        Booking booking = Booking.builder()
                .passengerId(passengerId)
                .pickupLocation(request.getPickupLocation())
                .dropoffLocation(request.getDropoffLocation())
                .distanceKm(request.getDistanceKm())
                .totalPrice(request.getTotalPrice())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        return toResponse(bookingRepository.save(booking));
    }

    public List<BookingResponse> getBookingsByPassenger(Long passengerId) {
        return bookingRepository.findByPassengerId(passengerId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public BookingResponse getBookingById(Long id) {
        return toResponse(getEntityById(id));
    }

    public BookingResponse updateBooking(Long id, UpdateBookingRequest request) {
        Booking booking = getEntityById(id);

        if (request.getPickupLocation() != null) booking.setPickupLocation(request.getPickupLocation());
        if (request.getDropoffLocation() != null) booking.setDropoffLocation(request.getDropoffLocation());
        if (request.getDistanceKm() != null) booking.setDistanceKm(request.getDistanceKm());
        if (request.getTotalPrice() != null) booking.setTotalPrice(request.getTotalPrice());
        if (request.getStatus() != null) booking.setStatus(request.getStatus());
        if (request.getDriverId() != null) booking.setDriverId(request.getDriverId());
        if ("COMPLETED".equalsIgnoreCase(request.getStatus())) booking.setCompletedAt(LocalDateTime.now());

        return toResponse(bookingRepository.save(booking));
    }

    public long countBookingsByPassenger(Long passengerId) {
        return bookingRepository.countByPassengerId(passengerId);
    }

    private Booking getEntityById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy booking với id = " + id));
    }

    private void validateCreateRequest(CreateBookingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request không hợp lệ");
        }
        if (request.getPickupLocation() == null || request.getPickupLocation().isBlank()) {
            throw new IllegalArgumentException("pickupLocation không được để trống");
        }
        if (request.getDropoffLocation() == null || request.getDropoffLocation().isBlank()) {
            throw new IllegalArgumentException("dropoffLocation không được để trống");
        }
    }

    private BookingResponse toResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .passengerId(booking.getPassengerId())
                .driverId(booking.getDriverId())
                .pickupLocation(booking.getPickupLocation())
                .dropoffLocation(booking.getDropoffLocation())
                .totalPrice(booking.getTotalPrice())
                .distanceKm(booking.getDistanceKm())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .completedAt(booking.getCompletedAt())
                .build();
    }
}
