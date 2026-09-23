package sn.diafoune.allo_dakar.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.diafoune.allo_dakar.mappers.BookingMapper;
import sn.diafoune.allo_dakar.mappers.PaymentMapper;
import sn.diafoune.allo_dakar.mappers.TripMapper;
import sn.diafoune.allo_dakar.mappers.UserMapper;
import sn.diafoune.allo_dakar.repositories.BookingRepository;
import sn.diafoune.allo_dakar.repositories.PaymentRepository;
import sn.diafoune.allo_dakar.repositories.TripRepository;
import sn.diafoune.allo_dakar.repositories.UserRepository;
import sn.diafoune.allo_dakar.services.interfaces.AdminService;
import sn.diafoune.allo_dakar.services.interfaces.UserService;
import sn.diafoune.allo_dakar.web.dtos.booking.BookingResponse;
import sn.diafoune.allo_dakar.web.dtos.payment.PaymentResponse;
import sn.diafoune.allo_dakar.web.dtos.trip.TripResponse;
import sn.diafoune.allo_dakar.web.dtos.user.UserResponse;

import java.util.UUID;

/**
 * Orchestre plutôt que de dupliquer : suspend/activate délèguent à UserService qui journalise
 * déjà dans AuditLog (voir UserServiceImpl). Les listes admin réutilisent directement les
 * repositories + mappers déjà validés par les autres domaines (pas de logique métier propre).
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final UserMapper userMapper;
    private final TripMapper tripMapper;
    private final BookingMapper bookingMapper;
    private final PaymentMapper paymentMapper;
    private final UserService userService;

    @Override
    public Page<UserResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Override
    public void suspendUser(UUID adminId, UUID userId, String reason) {
        userService.suspend(userId, adminId, reason);
    }

    @Override
    public void activateUser(UUID adminId, UUID userId) {
        userService.activate(userId, adminId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TripResponse> listAllTrips(Pageable pageable) {
        return tripRepository.findAll(pageable).map(tripMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> listAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(bookingMapper::toResponse);
    }

    @Override
    public Page<PaymentResponse> listAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(paymentMapper::toResponse);
    }
}
