package org.infinispan.commands.functional;

import static org.infinispan.functional.impl.EntryViews.snapshot;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import org.infinispan.commands.AbstractTopologyAffectedCommand;
import org.infinispan.commands.LocalCommand;
import org.infinispan.commands.Visitor;
import org.infinispan.commons.api.functional.EntryView.ReadEntryView;
import org.infinispan.commons.marshall.MarshallUtil;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.context.InvocationContext;
import org.infinispan.functional.impl.EntryViews;
import org.infinispan.lifecycle.ComponentStatus;

public class ReadOnlyManyCommand<K, V, R> extends AbstractTopologyAffectedCommand implements LocalCommand {
   public static final int COMMAND_ID = 63;

   protected Collection<? extends K> keys;
   protected Function<ReadEntryView<K, V>, R> f;

   public ReadOnlyManyCommand(Collection<? extends K> keys, Function<ReadEntryView<K, V>, R> f) {
      this.keys = keys;
      this.f = f;
   }

   public ReadOnlyManyCommand() {
   }

   public ReadOnlyManyCommand(ReadOnlyManyCommand c) {
      this.keys = c.keys;
      this.f = c.f;
   }

   public Collection<? extends K> getKeys() {
      return keys;
   }

   public void setKeys(Collection<? extends K> keys) {
      this.keys = keys;
   }

   public final ReadOnlyManyCommand<K, V, R> withKeys(Collection<? extends K> keys) {
      setKeys(keys);
      return this;
   }

   @Override
   public byte getCommandId() {
      return COMMAND_ID;
   }

   @Override
   public boolean isReturnValueExpected() {
      return true;
   }

   @Override
   public boolean canBlock() {
      return false;
   }

   @Override
   public void writeTo(ObjectOutput output) throws IOException {
      MarshallUtil.marshallCollection(keys, output);
      output.writeObject(f);
   }

   @Override
   public void readFrom(ObjectInput input) throws IOException, ClassNotFoundException {
      this.keys = MarshallUtil.unmarshallCollection(input, ArrayList::new);
      this.f = (Function<ReadEntryView<K, V>, R>) input.readObject();
   }

   @Override
   public Object perform(InvocationContext ctx) throws Throwable {
      // lazy execution triggers exceptions on unexpected places
      ArrayList<R> retvals = new ArrayList<R>(keys.size());
      for (K k : keys) {
         CacheEntry<K, V> me = lookupCacheEntry(ctx, k);
         R ret = f.apply(me.isNull() ? EntryViews.noValue(k) : EntryViews.readOnly(me));
         retvals.add(snapshot(ret));
      }
      return retvals.stream();
   }

   protected CacheEntry<K, V> lookupCacheEntry(InvocationContext ctx, Object key) {
      return ctx.lookupEntry(key);
   }

   @Override
   public Object acceptVisitor(InvocationContext ctx, Visitor visitor) throws Throwable {
      return visitor.visitReadOnlyManyCommand(ctx, this);
   }

   @Override
   public boolean shouldInvoke(InvocationContext ctx) {
      return true;
   }

   @Override
   public boolean ignoreCommandOnStatus(ComponentStatus status) {
      return false;
   }

   @Override
   public LoadType loadType() {
      return LoadType.OWNER;
   }

   @Override
   public String toString() {
      return "ReadOnlyManyCommand{" +
         "keys=" + keys +
         ", f=" + f +
         '}';
   }
}
