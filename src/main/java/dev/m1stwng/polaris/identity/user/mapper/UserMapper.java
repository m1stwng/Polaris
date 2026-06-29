package dev.m1stwng.polaris.identity.user.mapper;

import dev.m1stwng.polaris.identity.user.entity.User;
import dev.m1stwng.polaris.security.entity.SecurityUser;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    SecurityUser userToSecurityUser(User user);
}
