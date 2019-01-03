package com.project.EhrRoute.Services;
import com.project.EhrRoute.Entities.App.NetworkInvitationRequest;
import com.project.EhrRoute.Repositories.NetworkInvitationRequestRepository;
import com.project.EhrRoute.Utilities.UuidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class NetworkInvitationRequestService
{
    private NetworkInvitationRequestRepository networkInvitationRequestRepository;
    private VerificationTokenService verificationTokenService;
    private UuidUtil uuidUtil;

    @Autowired
    public NetworkInvitationRequestService(NetworkInvitationRequestRepository networkInvitationRequestRepository, VerificationTokenService verificationTokenService, UuidUtil uuidUtil) {
        this.networkInvitationRequestRepository = networkInvitationRequestRepository;
        this.verificationTokenService = verificationTokenService;
        this.uuidUtil = uuidUtil;
    }


    @Transactional
    public void saveInvitationRequest(String senderName, String networkName, String networkUUID)
    {
        // Save token on DB, will be used to verify invitation request expiration
        String token = uuidUtil.generateUUID();
        verificationTokenService.createToken(token);

        // Create invitation request
        NetworkInvitationRequest invitationRequest = new NetworkInvitationRequest(
                senderName, networkName, networkUUID, token
        );

        // Save invitation request
        networkInvitationRequestRepository.save(invitationRequest);
    }
}