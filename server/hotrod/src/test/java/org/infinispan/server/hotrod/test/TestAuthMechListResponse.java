package org.infinispan.server.hotrod.test;

import static org.infinispan.server.hotrod.OperationStatus.Success;

import java.util.Set;

import org.infinispan.server.hotrod.HotRodOperation;

/**
 * @author wburns
 * @since 9.0
 */
public class TestAuthMechListResponse extends TestResponse {
   public final Set<String> mechs;

   protected TestAuthMechListResponse(byte version, long messageId, String cacheName, short clientIntel,
                                      int topologyId, AbstractTestTopologyAwareResponse topologyResponse, Set<String> mechs) {
      super(version, messageId, cacheName, clientIntel, HotRodOperation.AUTH_MECH_LIST, Success, topologyId, topologyResponse);
      this.mechs = mechs;
   }
}
