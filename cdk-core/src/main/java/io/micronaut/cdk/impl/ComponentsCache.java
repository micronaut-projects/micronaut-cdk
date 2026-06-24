/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.cdk.impl;

import io.micronaut.cdk.action.Action;
import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.component.Component;
import io.micronaut.cdk.component.ExistingComponents;
import io.micronaut.cdk.util.Assert;
import io.micronaut.core.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the current state of the deployed Components and active Actions.
 * Active Actions are tracked in {@link #actionsAndComponents}. Actions that have been reverted
 * (i.e. deploy whose Component was deleted, or undeploy whose component's cdk ID was restored)
 * are tracked separately, so active action only contains stuff still effective in the Cloud.
 * <p>
 * The structure also tracks and updates the difference from {@link ExistingComponents}: tracks
 * resolved and deployed components by ID (+cloud) and CDK ID, and undeployed Components by CDK ID
 * only (as cloud ID will be assigned on next deploy). This difference must be consulted during
 * findDeployed so a reverted Component is not accidentally found/rejected.
 * <p>
 * Not sure if it is necessary to divide so precisely, but
 * <ul>
 *     <li>{@link #resolvedByCdkId}: components that are resolvable <b>at the given time</b>. It is an
 *     updated cache, filed from {@link ExistingComponents} on start, and updated with each registered Deploy or Undeploy.</li>
 *     <li>{@link #undeployedCdkIds} components that have been undeployed. Lookups for such IDs fail without querying the
 *     cloud.</li>
 *     <li>{@link #deployedCdkIds} components that have been deployed or updated. Used for tracking purposes.</li>
 *     <li>{@link #actionsAndComponents} actions that have produced Components and were not reverted</li>
 *     <li>{@link #revertedComponents} actions that have been reverted, their components undeployed or redeployed</li>
 * </ul>
 * The cache is now a part of ExecutionContext; it may become injectable on itself (must define an interface for it)
 */
public class ComponentsCache {
    private static final Logger LOG = LoggerFactory.getLogger(ComponentsCache.class);

    /**
     * Active actions.
     * TODO support more Components per action.
     */
    private final Map<Action<?>, Component<?>> actionsAndComponents = new LinkedHashMap<>();

    /**
     * All actions and their relevant components, including the
     * reverted or overridden ones.
     */
    private final Map<Action<?>, Component<?>> allActionsAndComponents = new LinkedHashMap<>();

    /**
     * Components that have been known but were deleted.
     */
    private final Map<DeleteSpec<?>, Component<?>> deletedComponents = new LinkedHashMap<>();

    /**
     * Actions whose Components were reverted.
     * TODO support more Components per action.
     */
    private final Map<Action<?>, Component<?>> revertedComponents = new LinkedHashMap<>();

    /**
     * CDK IDs deployed, in addition to ExistingComponents.
     */
    private final Map<String, Action<?>> deployedCdkIds = new LinkedHashMap<>();

    /**
     * Cloud IDs deployed, in addition to ExistingComponents.
     */
    private final Map<String, Action<?>> deployedCloudIds = new LinkedHashMap<>();

    /**
     * CDK IDs, that have been undeployed after ExistingComponents creation.
     */
    private final Map<String, DeleteSpec<?>> undeployedCdkIds = new ConcurrentHashMap<>();

    /**
     * Cloud IDs, that have been undeployed after ExistingComponents creation.
     */
    private final Map<String, DeleteSpec<?>> undeployedIds = new ConcurrentHashMap<>();

    /**
     * Components deployed or resolved, by their CDK ID. Initialized from ExistingComponents.
     */
    private final Map<String, Component<?>> resolvedByCdkId = new ConcurrentHashMap<>();

    /**
     * Components deployed or resolved, by their Cloud ID. Initialized from ExistingComponents.
     */
    private final Map<String, Component<?>> resolvedByCloudId = new ConcurrentHashMap<>();

    /**
     * For an action, registers a previous action that worked on the same component. There can be
     * a series of Specs each updating a bit of the component, or a DeleteSpec that deletes an existing component.
     */
    private final Map<Action<?>, Action<?>> predecessors = new ConcurrentHashMap<>();

    /**
     * Returns set of resolved Components. Makes a copy of the cache.
     *
     * @return resolved components.
     */
    public Map<String, Component<?>> getResolved() {
        return Map.copyOf(resolvedByCdkId);
    }

    /**
     * Registers a resolved Component. The Component will be registered with its CDK ID and Cloud ID.
     *
     * @param component resolved component.
     */
    public synchronized void registerResolved(@NonNull Component<?> component) {
        Assert.notNull(component, "Component cannot be null");
        String cdkId = component.getCdkId();
        if (cdkId != null) {
            resolvedByCdkId.put(cdkId, component);
        }
        resolvedByCloudId.put(component.getCloudId(), component);

        DeleteSpec<?> undeployed = undeployedIds.remove(component.getCloudId());
        DeleteSpec<?> undeployed2 = null;
        if (undeployed != null) {
            LOG.warn("Component {} found although it had been undeployed by {}", component, undeployed);
        }
        if (cdkId != null) {
            undeployed2 = undeployedCdkIds.remove(component.getCdkId());
            if (undeployed2 != null) {
                LOG.warn("Component {} found although it had been undeployed by {}", component, undeployed);
            }
        }
        revertUndeploy(undeployed2, revertUndeploy(undeployed, null, null, component), null, component);
    }

    /**
     * Finds an action that operated over the same resource immediately before the passed one.
     * For a Spec action, it will return a preceding Spec action creating/updating the same resource,
     * or a DeleteSpec action that deleted previous version of a resource with the same CDK ID.
     * For a DeleteSpec action, the method returns a preceding creation or update of the same cloud
     * resource.
     *
     * @param action action whose predecessor should be searched for
     * @return immediately preceding action on the resource, or empty optional.
     */
    @NonNull
    public Optional<Action<?>> findPredecessor(@NonNull Action<?> action) {
        return Optional.ofNullable(predecessors.get(action));
    }

    /**
     * Finds a sequence of actions that operate on the same resource as passed action. The list is
     * ordered from the newest action to the earliest, the 'action' itself not part of the list.
     *
     * @param action action whose predecessors should be returned
     * @return List of predecessors in newer-first order
     */
    public List<? extends Action<?>> getAllPredecessors(@NonNull Action<?> action) {
        Optional<Action<?>> pred = findPredecessor(action);
        if (pred.isEmpty()) {
            return List.of();
        }
        List<Action<?>> predecessors = new ArrayList<>();
        do {
            Action<?> predAction = pred.get();
            predecessors.add(predAction);
            pred = findPredecessor(predAction);
        } while (pred.isPresent());
        return predecessors;
    }

    /**
     * Registers the action in deployed. If the Component was previously undeployed (by CDK ID), it
     * adds its relevant Delete action among revertedComponents.
     *
     * @param action   the executed Action
     * @param deployed deployed Component
     */
    // @GuardedBy(this)
    public synchronized void registerDeployed(@NonNull Action<?> action,
                                              @NonNull Component<?> deployed) {
        Assert.notNull(action, "action cannot be null");
        Assert.notNull(deployed, "deployed cannot be null");
        if (actionsAndComponents.containsKey(action)) {
            throw new IllegalArgumentException("Multiple Components published per action are not supported yet");
        }
        actionsAndComponents.put(action, deployed);
        allActionsAndComponents.put(action, deployed);
        Action<?> first = null;
        Action<?> predecessor = null;
        if (deployed.getCdkId() != null) {
            predecessor = deployedCdkIds.remove(deployed.getCdkId());
            first = revertUndeploy(undeployedCdkIds.remove(deployed.getCdkId()), null, action, deployed);
        }
        if (predecessor == null) {
            predecessor = deployedCloudIds.remove(deployed.getCloudId());
        }
        if (predecessor != null) {
            predecessors.put(action, predecessor);
            actionsAndComponents.remove(predecessor);
        }
        // once cloudID is undeployed, it is gone for good, do not remove from the list.
        revertUndeploy(undeployedIds.get(deployed.getCloudId()), first, action, deployed);

        // put in the deploy maps:
        if (deployed.getCdkId() != null) {
            deployedCdkIds.put(action.getCdkId(), action);
        }
        deployedCloudIds.put(deployed.getCloudId(), action);
        resolvedByCdkId.put(deployed.getCdkId(), deployed);
        resolvedByCloudId.put(deployed.getCloudId(), deployed);
    }

    /**
     * Records an action as reverted, and removes it from the deletedActions.
     *
     * @param action the action
     * @param <T>    action type
     * @return the action itself
     */
    // @GuardedBy(this)
    private <T extends DeleteSpec<?>> T revertUndeploy(@NonNull T action, Action<?> previousAction, Action<?> deploy, Component<?> replaceWith) {
        if (action == null) {
            return null;
        }
        Component<?> component = deletedComponents.remove(action);
        if (component != null) {
            if (deploy != null) {
                predecessors.put(deploy, action);
            }
            revertedComponents.put(action, replaceWith);
        } else if (previousAction != action) {
            LOG.warn("Reverting undeploy {} that has no registered Component", action);
        }
        return action;
    }

    /**
     * Records an action as reverted, and removes it from the active actionsAndComponents.
     *
     * @param action the action
     * @param <T>    action type
     * @return the action itself
     */
    // @GuardedBy(this)
    private <T extends Action<?>> T revertDeployUpdate(@NonNull T action, Action<?> delAction, Action<?> previousAction) {
        if (action == null) {
            return null;
        }
        Component<?> component = actionsAndComponents.remove(action);
        if (component != null) {
            revertedComponents.put(action, component);
            predecessors.put(delAction, action);
        } else if (previousAction != action) {
            LOG.warn("Reverting deploy {} that has no registered Component", action);
        }
        return action;
    }

    /**
     * Registers the action as undeploy. If a Component that match DeleteSpec's selector is known,
     * its matching deploy action, if known, is marked as reverted. Component's CDK ID and Cloud ID
     * become unavailable for the lookup.
     *
     * @param spec the delete action
     */
    // @GuardedBy(this)
    public synchronized void registerUndeployed(@NonNull DeleteSpec<?> spec) {
        Assert.notNull(spec, "Spec cannot be null");

        String cdkId = spec.isByCdkId() ? spec.getId() : null;
        String cloudId = spec.isByCdkId() ? null : spec.getId();

        Component<?> undeployed;
        Optional<? extends Component> deployed = findDeployedComponent(spec.getId(), spec.isByCdkId(), Component.class);
        if (deployed.isPresent()) {
            undeployed = deployed.get();
            Action<?> first = null;
            if (undeployed.getCdkId() != null) {
                cdkId = undeployed.getCdkId();
                undeployedCdkIds.put(cdkId, spec);
                first = revertDeployUpdate(deployedCdkIds.remove(cdkId), spec, null);
            }
            cloudId = undeployed.getCloudId();
            undeployedIds.put(cloudId, spec);
            revertDeployUpdate(deployedCloudIds.remove(cloudId), spec, first);
            deletedComponents.put(spec, undeployed);
        } else {
            LOG.warn("Registering undeployed component {} that was not fetched before deletion", spec);
        }
        if (cdkId != null) {
            deployedCdkIds.remove(cdkId);
            resolvedByCdkId.remove(cdkId);
        }
        if (cloudId != null) {
            deployedCloudIds.remove(cloudId);
            resolvedByCloudId.remove(cloudId);
        }
    }

    /**
     * Finds a deployed Component by its ID. Only returns components assignable to the passed 'clazz' (returns
     * {@code Optional.empty()} if a mismatching component is registered for the given ID).
     *
     * @param id      id to search for
     * @param byCdkId true, if searching by CDK ID, false if searching by cloud ID.
     * @param clazz   desired component's type
     * @param <C>     component type.
     * @return Optional that holds the component instance
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public <C extends Component<?>> Optional<C> findDeployedComponent(@NonNull String id, boolean byCdkId, @NonNull Class<C> clazz) {
        Assert.notNull(id, "id cannot be null");
        Assert.notNull(clazz, "clazz cannot be null");
        if ((byCdkId ? undeployedCdkIds : undeployedIds).containsKey(id)) {
            // has been undeployed and no longer exists
            return Optional.empty();
        }
        Component<?> c;
        if (byCdkId) {
            c = resolvedByCdkId.get(id);
        } else {
            c = resolvedByCloudId.get(id);
        }
        return clazz.isInstance(c) ? Optional.of((C) c) : Optional.empty();
    }

    /**
     * Finds a deployed cloud resource by its ID. Only returns resources assignable to the passed 'clazz' (returns
     * {@code null} if a mismatching component is registered for the given ID). This call does not return a {@link Component},
     * it unwraps it to cloud data.
     *
     * @param id      id to search for
     * @param byCdkId true, if searching by CDK ID, false if searching by cloud ID.
     * @param clazz   desired component's type
     * @param <C>     cloud resource type.
     * @return Optional that holds the component instance
     */
    @NonNull
    public <C> Optional<C> findDeployed(@NonNull String id, boolean byCdkId, @NonNull Class<C> clazz) {

        return findDeployedComponent(id, byCdkId, Component.class).
                map(c -> clazz.isInstance(c.getCloudComponent()) ? clazz.cast(c.getCloudComponent()) : null);
    }

    /**
     * Loads the registered / resolved Components from the existing components stream.
     *
     * @param existingComponents existing components
     */
    public void loadFrom(ExistingComponents existingComponents) {
        for (Component<?> component : existingComponents.getExisting()) {
            registerResolved(component);
        }
    }

    /**
     * Returns the Component associated with an action. If {@code all} is true, it returns a
     * Component although another action may have modified or deleted it later.
     *
     * @param action the action
     * @param all    true include also obsolete states
     * @return component associated with the action
     */
    public Component<?> getActionComponent(Action<?> action, boolean all) {
        if (action instanceof DeleteSpec) {
            return deletedComponents.get(action);
        } else {
            return (all ? allActionsAndComponents : actionsAndComponents).get(action);
        }
    }

    /**
     * Returns create-update actions and their published/updated components. The Map
     * is keyed by an action, value is the published component. Actions are enumerated in
     * execution order.
     *
     * @return Map of actions and their components.
     */
    public Map<Action<?>, Component<?>> getActionsAndComponents() {
        return Map.copyOf(actionsAndComponents);
    }

    /**
     * Components published or updated.
     *
     * @return collection of published r updated components.
     */
    public Collection<Component<?>> getComponents() {
        return List.copyOf(actionsAndComponents.values());
    }

    /**
     * Get executed undeploy actions.
     *
     * @return undeploy actions.
     */
    public List<DeleteSpec<?>> getUndeployed() {
        return List.copyOf(deletedComponents.keySet());
    }

    /**
     * Returns a Component that has been undeployed.
     *
     * @param action the action
     * @param <C>    type of the component.
     * @return the undeployed component
     */
    @SuppressWarnings({"unchecked"})
    public <C extends Component<?>> C getUndeployed(Action<?> action) {
        Component<?> c;
        if (action instanceof DeleteSpec<?> del) {
            c = deletedComponents.get(del);
        } else if (action instanceof Spec<?>) {
            c = revertedComponents.get(action);
        } else {
            return null;
        }
        return (C) c;
    }

    /**
     * If the action has been reverted, returns the reverted component's state. For Spec actions, it returns
     * the deleted Component's state. For DeleteSpec actions, it returns the Component that was put back in the
     * place of the deleted one.
     *
     * @param action the action to inspect
     * @param <C>    component
     * @return component that has been reverted or empty optional
     */
    @SuppressWarnings("unchecked")
    public <C extends Component<?>> Optional<C> findRevertedComponent(Action<?> action) {
        return (Optional<C>) Optional.ofNullable(revertedComponents.get(action));
    }
}
