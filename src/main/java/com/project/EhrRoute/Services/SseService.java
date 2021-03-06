package com.project.EhrRoute.Services;
import com.project.EhrRoute.Events.SseKeepAliveEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
public class SseService
{
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public SseService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }


    // Sends an event every 30 seconds to keep the connection alive and to filter out disconnected nodes
    @Scheduled(fixedRate = 30000)
    public void SseKeepAlive()
    {
        SseKeepAliveEvent event = new SseKeepAliveEvent("0");
        this.eventPublisher.publishEvent(event);
    }
}
