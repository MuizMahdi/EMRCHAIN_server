package com.project.EhrRoute.Controllers;
import com.project.EhrRoute.Core.GenesisBlock;
import com.project.EhrRoute.Core.Utilities.StringUtil;
import com.project.EhrRoute.Entities.Auth.User;
import com.project.EhrRoute.Entities.Core.ChainRoot;
import com.project.EhrRoute.Entities.Core.Network;
import com.project.EhrRoute.Exceptions.ResourceNotFoundException;
import com.project.EhrRoute.Payload.Auth.ApiResponse;
import com.project.EhrRoute.Payload.Core.SerializableBlock;
import com.project.EhrRoute.Security.CurrentUser;
import com.project.EhrRoute.Security.UserPrincipal;
import com.project.EhrRoute.Services.NetworkService;
import com.project.EhrRoute.Services.UserService;
import com.project.EhrRoute.Utilities.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/network")
public class NetworkController
{
    private NetworkService networkService;
    private UserService userService;

    private GenesisBlock genesisBlock;
    private ModelMapper modelMapper;
    private StringUtil stringUtil;


    @Autowired
    public NetworkController(NetworkService networkService, UserService userService, GenesisBlock genesisBlock, ModelMapper modelMapper, StringUtil stringUtil) {
        this.networkService = networkService;
        this.userService = userService;
        this.genesisBlock = genesisBlock;
        this.modelMapper = modelMapper;
        this.stringUtil = stringUtil;
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createNetwork(@CurrentUser UserPrincipal currentUser) throws Exception
    {
        if (currentUser == null) {
            return new ResponseEntity<>(
                new ApiResponse(false, "User not logged in"),
                HttpStatus.BAD_REQUEST
            );
        }

        // Get current logged in user
        User user = userService.findUserByUsernameOrEmail(currentUser.getUsername());

        if (user == null) {
            return new ResponseEntity<>(
                new ApiResponse(false, "User not found"),
                HttpStatus.BAD_REQUEST
            );
        }

        // Initialize genesis block, a random network UUID is generated for every call,
        // thus the hash of the block will also change.
        genesisBlock.initializeBlock();

        // A networkUUID is generated in every genesis block.
        String networkUUID = genesisBlock.getBlock().getBlockHeader().getNetworkUUID();

        // Chain has only one block, so chainRoot is the hash of that block
        String chainRoot = stringUtil.getStringFromBytes(genesisBlock.getBlock().getBlockHeader().getHash());

        // Generate a new network using the genesis block data
        Network network = new Network();
        network.setNetworkUUID(networkUUID);

        ChainRoot networkChainRoot = new ChainRoot(chainRoot);
        network.setChainRoot(networkChainRoot);

        // Save network in DB
        networkService.saveNetwork(network);

        // Add network to user's networks
        user.addNetwork(network);
        userService.saveUser(user);

        // Genesis block to be returned
        SerializableBlock genesis = modelMapper.mapBlockToSerializableBlock(genesisBlock.getBlock());

        return new ResponseEntity<>(
            genesis,
            HttpStatus.OK
        );
    }
}
