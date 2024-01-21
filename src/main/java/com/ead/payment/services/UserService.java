package com.ead.payment.services;

import com.ead.payment.models.UserModel;

import java.util.Optional;
import java.util.UUID;

public sealed interface UserService permits UserServiceImpl {

    UserModel save(UserModel userModel);
    void delete(UUID userId);
    Optional<UserModel> findById(UUID userId);
}
