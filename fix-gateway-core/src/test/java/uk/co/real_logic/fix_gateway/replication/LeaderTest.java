/*
 * Copyright 2015 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.fix_gateway.replication;

import org.junit.Test;
import uk.co.real_logic.aeron.Subscription;
import uk.co.real_logic.agrona.collections.IntHashSet;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.co.real_logic.fix_gateway.replication.AbstractReplicationTest.neverBecomesFollower;

public class LeaderTest
{
    private static final short ID = 2;
    private static final int LEADERSHIP_TERM = 1;
    private static final long TIME = 10L;
    private static final long POSITION = 40L;

    private ControlPublication controlPublication = mock(ControlPublication.class);
    private RaftNode raftNode = mock(RaftNode.class);

    private Leader leader = new Leader(
        ID,
        new EntireClusterLeadershipTermAcknowledgementStrategy(),
        new IntHashSet(40, -1),
        controlPublication,
        mock(Subscription.class),
        mock(Subscription.class),
        raftNode,
        mock(ReplicationHandler.class),
        0,
        10, new TermState().leadershipTerm(LEADERSHIP_TERM))
        .getsElected(TIME);

    @Test
    public void shouldBecomeFollowerUponOtherLeaderHeartbeating()
    {
        final short newLeaderId = 3;

        leader.onConcensusHeartbeat(newLeaderId, LEADERSHIP_TERM + 1, POSITION);

        verify(raftNode, atLeastOnce()).transitionToFollower(any(Leader.class), anyLong());
    }

    @Test
    public void shouldNotBecomeFollowerFromOldTermHeartbeating()
    {
        final short newLeaderId = 3;

        leader.onConcensusHeartbeat(newLeaderId, LEADERSHIP_TERM, POSITION);

        neverBecomesFollower(raftNode);
    }

    @Test
    public void shouldNotBecomeFollowerFromOwnHeartbeats()
    {
        leader.onConcensusHeartbeat(ID, LEADERSHIP_TERM, POSITION);

        neverBecomesFollower(raftNode);
    }

}