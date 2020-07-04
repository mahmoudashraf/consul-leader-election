package com.consul.leader.distributed.processing;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import com.consul.leader.elections.leader.LeaderObserver;
import com.consul.leader.elections.leader.Watcher;
import com.consul.leader.elections.services.ServiceDefinition;

@Component
@Configuration
public class DistributedProcesssingHelper {

    public DistributedProcesssingHelper() {
        this.lock.lock();
    }

    private Map<String, ServiceDefinition> services = new Hashtable<String, ServiceDefinition>();
    private List<DistributedOperation> operations = new ArrayList<DistributedOperation>();
    private BlockingQueue<String> serviceQueue = new LinkedBlockingQueue<>();
    private Lock lock = new ReentrantLock();
    private static int numberOfCompletedRequests = 0;
    CountDownLatch latch = new CountDownLatch(1);

    public int getNumberOfCompletedRequests() {
        return numberOfCompletedRequests;
    }

    public Map<String, ServiceDefinition> getServices() {
        return services;
    }

    public void addServiceToMap(ServiceDefinition serviceDef) {
        this.services.put(serviceDef.getMetadata().get("service.id"), serviceDef);
    }

    public ServiceDefinition getService(String serviceId) {
        return this.services.get(serviceId);
    }


    public void addNewOperation(LeaderRequest request) {
        this.operations.add(new DistributedOperation(request));
    }

    public void completeOperation(int leaderRequestId, ServantResponse servantResponse) {
        DistributedOperation operation = this.operations.get(leaderRequestId);
        operation.setServantResponse(servantResponse);
        setFreeServiceToQueue(operation.getLeaderRequest().getTagertServiceID());
        this.numberOfCompletedRequests++;
        System.out.println("getNumberOfCompletedRequests" + this.getNumberOfCompletedRequests());
        if (this.isProcessingCompleted()) {
            latch.countDown();
        }
    }

    public boolean isProcessingCompleted() {
        return (this.getTotalNumberOfOperaions() == this.numberOfCompletedRequests) ? true : false;
    }

    public void setFreeServiceToQueue(String serviceId) {
        this.serviceQueue.add(serviceId);
    }

    public ServiceDefinition pickFreeService() {
        return this.services.get(this.serviceQueue.remove());
    }

    public int getTotalNumberOfOperaions() {
        return this.operations.size();
    }

    public List<DistributedOperation> getOperations() {
        return this.operations;
    }

    public void reset() {
        this.operations.clear();
        LeaderRequest.resetIDGenerator();
        numberOfCompletedRequests = 0;

    }

    public void builServicesList(String tageName, String tagValue) {
        reset();
        LeaderObserver.getInstance().getServentListByTag(tageName, tagValue).stream()
                .filter(service -> service != null).forEach(service -> {
                    if (service != null)
                        this.addServiceToMap(service);
                });
    }

    public void builServicesList() {
        LeaderObserver.getInstance().getAllServentList().stream().filter(service -> service != null)
                .forEach(service -> {
                    if (service != null)
                        this.addServiceToMap(service);
                });
    }

    public void waitReceiveProcessingResult(Watcher watcher) {
        System.out.println("start waiting lock");

        while (!this.isProcessingCompleted()) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("enf waiting lock");
        watcher.receiveProcessingResult(this.operations);
    }
}
