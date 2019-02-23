package com.project.EhrRoute.Services;
import com.project.EhrRoute.Entities.EHR.EhrDetails;
import com.project.EhrRoute.Exceptions.ResourceNotFoundException;
import com.project.EhrRoute.Repositories.EhrDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class EhrDetailService
{
    private EhrDetailsRepository ehrDetailsRepository;

    @Autowired
    public EhrDetailService(EhrDetailsRepository ehrDetailsRepository) {
        this.ehrDetailsRepository = ehrDetailsRepository;
    }


    @Transactional
    public EhrDetails findEhrDetails(String address) {
        return this.ehrDetailsRepository.findByAddress(address).orElseThrow(() ->
                new ResourceNotFoundException("EHR Details", "address", address)
        );
    }


    @Transactional
    public void generateUserEhrDetails(String address) {
        // Create EhrDetails using address
        EhrDetails ehrDetails = new EhrDetails(address);
        // Persist it
        ehrDetailsRepository.save(ehrDetails);
    }
}