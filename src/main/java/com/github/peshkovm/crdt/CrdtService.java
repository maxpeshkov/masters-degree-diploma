package com.github.peshkovm.crdt;

import com.github.peshkovm.crdt.registry.CrdtRegistry;
import com.github.peshkovm.crdt.routing.ResourceType;
import com.github.peshkovm.crdt.routing.fsm.AddResourceResponse;
import com.github.peshkovm.crdt.routing.fsm.DeleteResourceResponse;
import io.vavr.collection.Vector;
import io.vavr.concurrent.Future;
import java.io.Serializable;

/** Defines methods to service replicated crdt objects. */
public interface CrdtService {

  Future<Vector<AddResourceResponse>> addResource(String resourceId, ResourceType resourceType);

  Future<Vector<DeleteResourceResponse>> deleteResource(String crdtId, ResourceType crdtType);

  CrdtRegistry crdtRegistry();
}
