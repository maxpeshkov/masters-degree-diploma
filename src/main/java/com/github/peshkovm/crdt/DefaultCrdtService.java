package com.github.peshkovm.crdt;

import com.github.peshkovm.crdt.commutative.protocol.DownstreamUpdate;
import com.github.peshkovm.crdt.registry.CrdtRegistry;
import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.crdt.routing.fsm.AddResource;
import com.github.peshkovm.crdt.routing.fsm.AddResourceResponse;
import com.github.peshkovm.crdt.routing.fsm.GetPayload;
import com.github.peshkovm.crdt.routing.fsm.GetPayloadResponse;
import com.github.peshkovm.raft.Raft;
import com.github.peshkovm.raft.protocol.CommandResult;
import com.github.peshkovm.raft.resource.ResourceRegistry;
import com.github.peshkovm.transport.TransportController;
import io.vavr.collection.Vector;
import io.vavr.concurrent.Future;
import java.io.Serializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Default implementation of {@link CrdtService}.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class DefaultCrdtService implements CrdtService {

  private final Logger logger = LogManager.getLogger();
  private final Raft raft;
  private final CrdtRegistry crdtRegistry;

  @Autowired
  public DefaultCrdtService(
      Raft raft,
      ResourceRegistry registry,
      CrdtRegistry crdtRegistry,
      TransportController transportController) {
    this.raft = raft;
    this.crdtRegistry = crdtRegistry;

    registry.registerHandler(AddResource.class, this::handle);
    transportController.registerMessageHandler(DownstreamUpdate.class, this::handle);
  }

  @Override
  public Future<Vector<AddResourceResponse>> addResource(
      String resourceId, ResourceType resourceType) {
    return raft.command(new AddResource(resourceId, resourceType))
        .map(Vector::ofAll)
        .map(commandResults -> commandResults.map(CommandResult::getResult))
        .filter(
            commandResults ->
                commandResults.forAll(result -> result instanceof AddResourceResponse))
        .map(commandResults -> commandResults.map(result -> (AddResourceResponse) result));
  }

  @Override
  public <T extends Serializable, R extends Serializable, M extends Crdt<T, R>>
  Future<Vector<R>> queryAllNodes(String crdtId, Class<M> crdtType) {
    return raft.command(new GetPayload<>(crdtId, crdtType))
        .map(Vector::ofAll)
        .map(commandResults -> commandResults.map(CommandResult::getResult))
        .filter(
            commandResults ->
                commandResults.forAll(result -> result instanceof AddResourceResponse))
        .map(commandResults -> commandResults.map(result -> (GetPayloadResponse) result))
        .map(getPayloadResponses -> getPayloadResponses.map(response -> (R) response.getPayload()));
  }

  @Override
  public CrdtRegistry crdtRegistry() {
    return crdtRegistry;
  }

  private boolean createCrdt(String resourceId, ResourceType resourceType) {
    boolean isCreated = false;

    switch (resourceType) {
      case GCounter:
        isCreated = crdtRegistry.createGCounter(resourceId);
        break;
      default:
        logger.warn("Unexpected crdt type: {}", () -> resourceType);
    }

    if (isCreated) {
      logger.info("Successfully created GCounter");
    } else {
      logger.error("GCounter with id: {} already exists", () -> resourceId);
    }

    return isCreated;
  }

  private CommandResult handle(AddResource resource) {
    final boolean isCreated = createCrdt(resource.getResourceId(), resource.getResourceType());

    final AddResourceResponse addResourceResponse =
        new AddResourceResponse(resource.getResourceId(), resource.getResourceType());

    return new CommandResult(addResourceResponse, isCreated);
  }

  private synchronized <T extends Serializable, R extends Serializable> void handle(
      DownstreamUpdate<T, R> downstreamUpdate) {
    final String crdtId = downstreamUpdate.getCrdtId();
    final var crdtType = downstreamUpdate.getCrdtType();

    final var cmRDT = crdtRegistry().crdt(crdtId, crdtType);

    cmRDT.downstream(downstreamUpdate.getAtSourceResult(), downstreamUpdate.getArgument());
  }
}
