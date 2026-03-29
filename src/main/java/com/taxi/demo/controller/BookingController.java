package com.taxi.demo.controller;

import com.taxi.demo.dto.booking.BookingResponse;
import com.taxi.demo.dto.booking.CreateBookingRequest;
import com.taxi.demo.dto.booking.UpdateBookingRequest;
import com.taxi.demo.entity.User;
import com.taxi.demo.repository.UserRepository;
import com.taxi.demo.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    @PostMapping
    public BookingResponse create(@RequestBody CreateBookingRequest request, Authentication authentication) {
        User user = getCurrentUser(authentication);
        return bookingService.createBooking(user.getId(), request);
    }

    @GetMapping
    public List<BookingResponse> getList(Authentication authentication) {
        User user = getCurrentUser(authentication);
        if (isAdmin(user)) {
            return bookingService.getAllBookings();
        }
        return bookingService.getBookingsByPassenger(user.getId());
    }

    @GetMapping("/{id}")
    public BookingResponse getDetail(@PathVariable Long id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        BookingResponse booking = bookingService.getBookingById(id);
        if (!isAdmin(user) && !booking.getPassengerId().equals(user.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền xem booking này");
        }
        return booking;
    }

    @PutMapping("/{id}")
    public BookingResponse update(@PathVariable Long id,
                                  @RequestBody UpdateBookingRequest request,
                                  Authentication authentication) {
        User user = getCurrentUser(authentication);
        BookingResponse booking = bookingService.getBookingById(id);
        if (!isAdmin(user) && !booking.getPassengerId().equals(user.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền sửa booking này");
        }
        return bookingService.updateBooking(id, request);
    }

    private User getCurrentUser(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));
    }

    private boolean isAdmin(User user) {
        return "ROLE_ADMIN".equalsIgnoreCase(user.getRole());
    }
}
