package org.example.realworldapi.domain.service;

import lombok.AllArgsConstructor;
import org.example.realworldapi.domain.exception.EmailAlreadyExistsException;
import org.example.realworldapi.domain.exception.InvalidPasswordException;
import org.example.realworldapi.domain.exception.UserNotFoundException;
import org.example.realworldapi.domain.exception.UsernameAlreadyExistsException;
import org.example.realworldapi.domain.model.provider.HashProvider;
import org.example.realworldapi.domain.model.user.*;
import org.example.realworldapi.domain.validator.ModelValidator;

import javax.inject.Singleton;
import java.util.UUID;

@Singleton
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ModelValidator modelValidator;
    private final UserModelBuilder userBuilder;
    private final HashProvider hashProvider;

    public User findById(UUID id) {
        return userRepository.findUserById(id).orElseThrow(UserNotFoundException::new);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
    }

    public User update(UpdateUserInput updateUserInput) {
        final var user = findById(updateUserInput.getId());
        checkValidations(updateUserInput, updateUserInput.getId());
        updateFields(user, updateUserInput);
        userRepository.update(modelValidator.validate(user));
        return user;
    }

    public User create(CreateUserInput createUserInput) {
        final var user =
                userBuilder.build(
                        createUserInput.getUsername(),
                        createUserInput.getEmail(),
                        createUserInput.getPassword());
        checkExistingUsername(user.getUsername());
        checkExistingEmail(user.getEmail());
        user.setPassword(hashProvider.hashPassword(user.getPassword()));
        userRepository.save(user);
        return user;
    }

    public User login(LoginUserInput loginUserInput) {
        final var user =
                userRepository
                        .findByEmail(loginUserInput.getEmail())
                        .orElseThrow(UserNotFoundException::new);
        if (!isPasswordValid(loginUserInput.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }
        return user;
    }

    private void updateFields(User user, UpdateUserInput updateUserInput) {
        if (isPresent(updateUserInput.getUsername())) {
            user.setUsername(updateUserInput.getUsername());
        }

        if (isPresent(updateUserInput.getEmail())) {
            user.setEmail(updateUserInput.getEmail());
        }

        if (isPresent(updateUserInput.getBio())) {
            user.setBio(updateUserInput.getBio());
        }

        if (isPresent(updateUserInput.getImage())) {
            user.setImage(updateUserInput.getImage());
        }
    }

    private void checkValidations(UpdateUserInput updateUserInput, UUID excludeId) {

        if (isPresent(updateUserInput.getUsername())) {
            checkUsername(excludeId, updateUserInput.getUsername());
        }

        if (isPresent(updateUserInput.getEmail())) {
            checkEmail(excludeId, updateUserInput.getEmail());
        }
    }

    private boolean isPresent(String property) {
        return property != null && !property.isEmpty();
    }

    private void checkUsername(UUID selfId, String username) {
        if (userRepository.existsUsername(selfId, username)) {
            throw new UsernameAlreadyExistsException();
        }
    }

    private void checkEmail(UUID selfId, String email) {
        if (userRepository.existsEmail(selfId, email)) {
            throw new EmailAlreadyExistsException();
        }
    }

    private void checkExistingUsername(String username) {
        if (userRepository.existsBy("username", username)) {
            throw new UsernameAlreadyExistsException();
        }
    }

    private void checkExistingEmail(String email) {
        if (userRepository.existsBy("email", email)) {
            throw new EmailAlreadyExistsException();
        }
    }

    private boolean isPasswordValid(String password, String hashedPassword) {
        return hashProvider.checkPassword(password, hashedPassword);
    }
}
