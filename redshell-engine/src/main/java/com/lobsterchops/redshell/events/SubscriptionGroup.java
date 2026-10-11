package com.lobsterchops.redshell.events;

import java.util.ArrayList;
import java.util.List;

/** Collects subscriptions so an owner (a scene or system) can drop all of them at once on dispose. */
public final class SubscriptionGroup {

    private final List<Subscription> subscriptions = new ArrayList<>();

    public <S extends Subscription> S add(S subscription) {
        subscriptions.add(subscription);
        return subscription;
    }

    public void cancelAll() {
        for (int i = 0; i < subscriptions.size(); i++) {
            subscriptions.get(i).cancel();
        }
        subscriptions.clear();
    }

    public int size() {
        return subscriptions.size();
    }
}