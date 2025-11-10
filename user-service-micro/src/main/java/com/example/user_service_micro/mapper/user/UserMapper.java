package com.example.user_service_micro.mapper.user;

import com.example.user_service_micro.dto.user.UserDTO;
import com.example.user_service_micro.model.user.User;
import com.example.user_service_micro.dto.user.UserInfoResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "user.id", target = "id")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.name", target = "name")
    @Mapping(source = "user.role", target = "role")
    UserDTO toDTO(User user);

    // Used in /api/users endpoints
    UserDTO toUsersDTO(User user);
}

/*

Why we map only getName() & getEmail()
DTO only has name, email, balance.
DTO has balance but User entity doesn’t.

@Override
public UserInfoResponse toDTO(User user, Double balance) {
    if (user == null) {
        return null;
    }

    UserInfoResponse dto = new UserInfoResponse(
        user.getName(),      // mapped from user.name
        user.getEmail(),     // mapped from user.email
        balance              // passed directly
    );
    return dto;
}

 */