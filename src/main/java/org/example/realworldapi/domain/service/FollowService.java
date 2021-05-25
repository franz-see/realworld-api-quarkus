package org.example.realworldapi.domain.service;

import lombok.AllArgsConstructor;
import org.example.realworldapi.domain.model.user.FollowRelationship;
import org.example.realworldapi.domain.model.user.FollowRelationshipRepository;

import javax.inject.Singleton;
import java.util.UUID;

@Singleton
@AllArgsConstructor
public class FollowService {

    private final UserService userService;
    private final FollowRelationshipRepository followRelationshipRepository;

    public FollowRelationship followUserByUsername(UUID loggedUserId, String username) {
        final var loggedUser = userService.findById(loggedUserId);
        final var userToFollow = userService.findByUsername(username);
        final var followingRelationship = new FollowRelationship(loggedUser, userToFollow);
        followRelationshipRepository.save(followingRelationship);
        return followingRelationship;
    }

    public void unfollowUserByUsername(UUID loggedUserId, String username) {
        final var loggedUser = userService.findById(loggedUserId);
        final var userToUnfollow = userService.findByUsername(username);
        final var followingRelationship =
                followRelationshipRepository.findByUsers(loggedUser, userToUnfollow).orElseThrow();
        followRelationshipRepository.remove(followingRelationship);
    }

    public boolean isFollowingUser(UUID currentUserId, UUID followedUserId) {
        return followRelationshipRepository.isFollowing(currentUserId, followedUserId);
    }
}
