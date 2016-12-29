package org.folio.inventory.storage

import io.vertx.groovy.core.Vertx
import org.folio.inventory.domain.CollectionProvider
import org.folio.inventory.domain.InstanceCollection
import org.folio.inventory.domain.ItemCollection
import org.folio.inventory.resources.ingest.IngestJobCollection
import org.folio.inventory.storage.external.ExternalStorageCollections
import org.folio.inventory.storage.memory.InMemoryCollections
import org.folio.metadata.common.Context

import java.util.function.Function

class Storage {
  private final Function<Context, CollectionProvider> providerFactory;

  Storage( final Function<Context, CollectionProvider> providerFactory) {
    this.providerFactory = providerFactory
  }

  static Storage basedUpon(Vertx vertx, Map<String, Object> config) {
    def storageType = config.get("storage.type", "okapi")

    switch(storageType) {
      case "external":
        def location = config.get("storage.location", null)

        if(location == null) {
          throw new IllegalArgumentException(
            "For external storage, location must be provided.")
        }

        return new Storage(
          { context -> new ExternalStorageCollections(vertx, location) })
        break;

      case "okapi":
        return new Storage(
        { context ->
          new ExternalStorageCollections(vertx, context.okapiLocation) })

      case "memory":
        def inMemoryCollections = new InMemoryCollections()

        return new Storage({ context -> inMemoryCollections })
        break;

      default:
        throw new IllegalArgumentException(
          "Storage type must be one of [external, okapi, memory]")
    }
  }

  ItemCollection getItemCollection(Context context) {
    providerFactory.apply(context).getItemCollection(context.tenantId)
  }

  InstanceCollection getInstanceCollection(Context context) {
    providerFactory.apply(context).getInstanceCollection(context.tenantId)
  }

  IngestJobCollection getIngestJobCollection(Context context) {
    providerFactory.apply(context).getIngestJobCollection(context.tenantId)
  }
}
