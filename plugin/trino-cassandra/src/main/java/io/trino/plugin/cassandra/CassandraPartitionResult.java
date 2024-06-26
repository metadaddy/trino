/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.plugin.cassandra;

import com.google.common.collect.ImmutableList;
import io.trino.spi.connector.ColumnHandle;
import io.trino.spi.predicate.TupleDomain;

import java.util.List;

import static com.google.common.collect.Iterables.getOnlyElement;
import static java.util.Objects.requireNonNull;

public record CassandraPartitionResult(List<CassandraPartition> partitions, TupleDomain<ColumnHandle> unenforcedConstraint)
{
    public CassandraPartitionResult
    {
        partitions = ImmutableList.copyOf(requireNonNull(partitions, "partitions is null"));
        requireNonNull(unenforcedConstraint, "unenforcedConstraint is null");
    }

    public boolean unpartitioned()
    {
        return partitions.size() == 1 && getOnlyElement(partitions).isUnpartitioned();
    }

    public boolean indexedColumnPredicatePushdown()
    {
        return partitions.size() == 1 && getOnlyElement(partitions).isIndexedColumnPredicatePushdown();
    }
}
