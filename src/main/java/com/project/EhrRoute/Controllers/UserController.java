package com.project.EhrRoute.Controllers;
import com.project.EhrRoute.Core.Node;
import com.project.EhrRoute.Entities.Auth.Role;
import com.project.EhrRoute.Entities.Auth.User;
import com.project.EhrRoute.Entities.Core.Network;
import com.project.EhrRoute.Events.SseKeepAliveEvent;
import com.project.EhrRoute.Exceptions.InternalErrorException;
import com.project.EhrRoute.Exceptions.NullUserNetworkException;
import com.project.EhrRoute.Exceptions.ResourceNotFoundException;
import com.project.EhrRoute.Models.RoleName;
import com.project.EhrRoute.Payload.App.ProviderAdditionRequest;
import com.project.EhrRoute.Payload.Auth.ApiResponse;
import com.project.EhrRoute.Payload.Auth.UserInfo;
import com.project.EhrRoute.Payload.Auth.UserRoleResponse;
import com.project.EhrRoute.Payload.Core.UserNetworksResponse;
import com.project.EhrRoute.Repositories.RoleRepository;
import com.project.EhrRoute.Security.CurrentUser;
import com.project.EhrRoute.Security.UserPrincipal;
import com.project.EhrRoute.Services.ClustersContainer;
import com.project.EhrRoute.Services.ProviderService;
import com.project.EhrRoute.Services.UserService;
import com.project.EhrRoute.Utilities.ModelMapper;
import com.project.EhrRoute.Utilities.SimpleStringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.validation.Valid;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@RestController
@RequestMapping("/users")
public class UserController
{
    private UserService userService;
    private RoleRepository roleRepository;
    private ProviderService providerService;
    private ClustersContainer clustersContainer;
    private SimpleStringUtil simpleStringUtil;
    private ModelMapper modelMapper;

    @Autowired
    public UserController(SimpleStringUtil simpleStringUtil, ClustersContainer clustersContainer, UserService userService, ModelMapper modelMapper, RoleRepository roleRepository, ProviderService providerService) {
        this.simpleStringUtil = simpleStringUtil;
        this.userService = userService;
        this.providerService = providerService;
        this.roleRepository = roleRepository;
        this.clustersContainer = clustersContainer;
        this.modelMapper = modelMapper;
    }


    @GetMapping("/current")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('PROVIDER')")
    public ResponseEntity getCurrentUser(@CurrentUser UserPrincipal currentUser)
    {
        if (currentUser == null) {
            return new ResponseEntity<>(
                new ApiResponse(false, "User not logged in"),
                HttpStatus.BAD_REQUEST
            );
        }

        return ResponseEntity.ok(
            userService.getUserInfo(currentUser.getId())
        );
    }


    @GetMapping("/current/roles")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('PROVIDER')")
    public ResponseEntity<?> getCurrentUserRoles(@CurrentUser UserPrincipal currentUser)
    {
        if (currentUser == null) {
            return new ResponseEntity<>(
                new ApiResponse(false, "User not logged in"),
                HttpStatus.BAD_REQUEST
            );
        }

        Set<Role> userRoles;

        try {
            // Get the user's set of Role
            userRoles = userService.findUserRoles(currentUser.getUsername());
        }
        catch(ResourceNotFoundException Ex) {
            return new ResponseEntity<>(
                new ApiResponse(false, "User not logged in"),
                HttpStatus.BAD_REQUEST
            );
        }

        // Create a set of UserRoleResponse 
        Set<UserRoleResponse> userRolesNames = new HashSet<>();

        // Map each user Role's role name to value in userRoleNames set
        for (Role role : userRoles) {
            UserRoleResponse userRole = new UserRoleResponse(role.getName().toString());
            userRolesNames.add(userRole);
        }

        // Return the userRoleNames set
        return new ResponseEntity<>(
            userRolesNames,
            HttpStatus.OK
        );
    }


    @GetMapping("/current/networks")
    @PreAuthorize("hasRole('PROVIDER')")
    public ResponseEntity getCurrentUserNetwork(@CurrentUser UserPrincipal currentUser)
    {
        if (currentUser == null) {
            return new ResponseEntity<>(
                new ApiResponse(false, "User not logged in"),
                HttpStatus.BAD_REQUEST
            );
        }

        User user = userService.findUserByUsernameOrEmail(currentUser.getUsername());

        if (user == null) {
            return new ResponseEntity<>(
                new ApiResponse(false, "User not found on DB"),
                HttpStatus.BAD_REQUEST
            );
        }

        Set<Network> userNetworks;

        try {
            userNetworks = userService.findUserNetworks(user);
        }
        catch (NullUserNetworkException Ex) {
            return new ResponseEntity<>(
                new ApiResponse(false, Ex.getMessage()),
                HttpStatus.NOT_FOUND
            );
        }

        // Map Networks set to UserNetworksResponse payload
        UserNetworksResponse userNetworksResponse = modelMapper.mapNetworksToUserNetworksResponse(userNetworks);

        return new ResponseEntity<>(
            userNetworksResponse,
            HttpStatus.OK
        );
    }


    @GetMapping("/current/first-login-status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('PROVIDER')")
    public ResponseEntity getCurrentUserFirstLoginStatus(@CurrentUser UserPrincipal currentUser)
    {
        if (currentUser == null) {
            return new ResponseEntity<>(
                new ApiResponse(false, "User not logged in"),
                HttpStatus.BAD_REQUEST
            );
        }

        boolean isFirstLogin = userService.isUserFirstLogin(currentUser.getId());

        return ResponseEntity.ok(isFirstLogin);
    }


    @PostMapping("/current/info-addition-status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity setCurrentUserHasAddedInfoStatus(@CurrentUser UserPrincipal currentUser)
    {
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(
                new ApiResponse(false, "User not logged in")
            );
        }

        userService.setUserHasAddedInfo(currentUser.getId());

        return ResponseEntity.ok(
            new ApiResponse(false, "Your info has been saved")
        );
    }


    @GetMapping("/search-by-username")
    public List<String> searchUsersnamesByUsername(@RequestParam("keyword") String username)
    {
        return userService.searchUsername(username);
    }


    @GetMapping("/get-by-username/{username}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROVIDER')")
    public ResponseEntity getUserByUsername(@PathVariable("username") String username)
    {
        User user = userService.findUserByUsernameOrEmail(username);

        if (user == null) {
            return new ResponseEntity<>(
                new ApiResponse(false, "Invalid username"),
                HttpStatus.BAD_REQUEST
            );
        }

        return ResponseEntity.ok (
            userService.getUserInfo(user.getId())
        );
    }


    @GetMapping("/get-notifications")
    @PreAuthorize("hasRole('USER')")
    public SseEmitter streamUserNotifications(@RequestParam("userid") String userID , @CurrentUser UserPrincipal currentUser) throws IOException
    {
        /*
        *   Users are saved in clusters using their IDs that are stored in database
        *   in order to send consent requests to individual users and save the notifications.
        */

        SseEmitter userNotificationEmitter = new SseEmitter(86400000L); // 1 Day timeout

        if (userID == null || userID.isEmpty() || !simpleStringUtil.isValidNumber(userID)) {
            userNotificationEmitter.send("Invalid user ID", MediaType.APPLICATION_JSON);
        }

        // Empty set for node networks since a user cannot be in a network.
        Set<String> nodeNetworks = new HashSet<>();

        Node userNode = new Node(userNotificationEmitter, nodeNetworks);
        clustersContainer.getAppUsers().addNode(userID, userNode);

        /*
        if (currentUser != null) {
            Node userNode = new Node(userNotificationEmitter, "");
            clustersContainer.getAppUsers().addNode(userID, userNode);
        }
        */

        // Remove the emitter on timeout/error/completion
        userNotificationEmitter.onTimeout(() -> clustersContainer.getAppUsers().removeNode(userID));
        userNotificationEmitter.onError(error -> clustersContainer.getAppUsers().removeNode(userID));
        userNotificationEmitter.onCompletion(() -> clustersContainer.getAppUsers().removeNode(userID));

        return userNotificationEmitter;
    }


    @PostMapping("/providers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity addProvider(@Valid @RequestBody ProviderAdditionRequest providerAdditionRequest)
    {
        /* Get and validate user with username */
        User user = userService.findUserByUsernameOrEmail(providerAdditionRequest.getUsername());

        if (user == null) {
            return ResponseEntity.badRequest().body(
                new ApiResponse(false, "Invalid username in provider addition request")
            );
        }

        /* Set user's role to Provider */
        // Get the role
        Role userRole = roleRepository.findByName(RoleName.ROLE_PROVIDER).orElseThrow(() ->
            new InternalErrorException("Invalid Role")
        );

        // Update the user roles set
        Set<Role> userRoles = user.getRoles();
        userRoles.add(userRole);
        user.setRoles(userRoles);

        // Persist changes of user roles
        userService.saveUser(user);

        // Create an institution and generate provider details for user
        providerService.generateInstitutionProviderDetails(user, providerAdditionRequest.getInstitutionName());

        return ResponseEntity.ok(
            new ApiResponse(true, "Provider has been successfully added")
        );
    }


    @EventListener
    protected void SseKeepAlive(SseKeepAliveEvent event)
    {
        if (clustersContainer.getAppUsers().getCluster().size() > 0)
        {
            event.setKeepAliveData("0"); // Keep-Alive fake data

            clustersContainer.getAppUsers().getCluster().forEach((uuid, node) -> {
                try {
                    // Send fake data every 4 minutes to keep the connection alive and check whether the user disconnected or not
                    node.getEmitter().send(event.getKeepAliveData(), MediaType.APPLICATION_JSON);
                }
                catch (IOException Ex) { // If could not be send due to user quitting then remove them from cluster
                    clustersContainer.getAppUsers().removeNode(uuid);
                }
            });
        }
    }
}
