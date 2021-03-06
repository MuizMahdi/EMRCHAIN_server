package com.project.EhrRoute.Events;
import org.springframework.context.ApplicationEvent;

public class SseKeepAliveEvent extends ApplicationEvent
{
    private String keepAliveData;

    public SseKeepAliveEvent(String keepAliveData) {
        super(keepAliveData);
        this.keepAliveData = keepAliveData;
    }

    public String getKeepAliveData() {
        return keepAliveData;
    }
    public void setKeepAliveData(String keepAliveData) {
        this.keepAliveData = keepAliveData;
    }
}
