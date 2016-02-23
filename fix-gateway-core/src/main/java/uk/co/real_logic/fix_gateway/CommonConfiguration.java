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
package uk.co.real_logic.fix_gateway;

import uk.co.real_logic.aeron.Aeron;
import uk.co.real_logic.agrona.IoUtil;
import uk.co.real_logic.agrona.concurrent.BackoffIdleStrategy;
import uk.co.real_logic.agrona.concurrent.IdleStrategy;
import uk.co.real_logic.fix_gateway.session.SenderAndTargetSessionIdStrategy;
import uk.co.real_logic.fix_gateway.session.SessionIdStrategy;

import java.io.File;

import static java.lang.Integer.getInteger;
import static java.lang.System.getProperty;

/**
 * Common configuration for both the Fix Engine and Library. Some options are configurable via
 * commandline properties. Setters override commandline properties, not the other way around.
 * <p>
 * See setters or properties for documentation of what specific configuration options do.
 *
 * @see uk.co.real_logic.fix_gateway.engine.EngineConfiguration
 * @see uk.co.real_logic.fix_gateway.library.LibraryConfiguration
 */
public class CommonConfiguration
{

    // ------------------------------------------------
    //          Configuration Properties
    // ------------------------------------------------

    /** Property name for length of the memory mapped buffers for the counters file */
    public static final String MONITORING_BUFFERS_LENGTH_PROPERTY = "fix.monitoring.length";
    /** Property name for directory of the conductor buffers */
    public static final String MONITORING_FILE_PROPERTY = "fix.monitoring.file";
    /** Property name for the flag to enable or disable debug logging */
    public static final String DEBUG_PRINT_MESSAGES_PROPERTY = "fix.core.debug";
    /** Property name for the flag to enable or disable flushing of writes */
    public static final String FORCE_WRITES_MESSAGES_PROPERTY = "fix.core.flush";
    /**
     * Property name for the flag to set the maximum number of attempts to claim a message
     * slot on the inbound stream.
     */
    public static final String INBOUND_MAX_CLAIM_ATTEMPTS_PROPERTY = "fix.core.inbound_max_claims";
    /**
     * Property name for the flag to set the maximum number of attempts to claim a message
     * slot on the outbound stream.
     */
    public static final String OUTBOUND_MAX_CLAIM_ATTEMPTS_PROPERTY = "fix.core.outbound_max_claims";
    /** Property name for the flag to enable or disable message timing */
    public static final String TIME_MESSAGES_PROPERTY = "fix.core.timing";
    /** Property name for the file to log debug messages to, default is standard output */
    public static final String DEBUG_FILE_PROPERTY = "fix.core.debug.file";

    public static final int DEFAULT_MONITORING_BUFFER_LENGTH = 8 * 1024 * 1024;
    public static final String DEFAULT_MONITORING_FILE =
        optimalTmpDirName() + File.separator + "fix-%s" + File.separator + "monitoring";

    // ------------------------------------------------
    //          Static Configuration
    // ------------------------------------------------

    /** These are static final fields in order to give the optimiser more scope */
    public static final boolean DEBUG_PRINT_MESSAGES = Boolean.getBoolean(DEBUG_PRINT_MESSAGES_PROPERTY);
    public static final String DEBUG_FILE = System.getProperty(DEBUG_FILE_PROPERTY);
    public static final boolean TIME_MESSAGES = Boolean.getBoolean(TIME_MESSAGES_PROPERTY);
    public static final int WARMUP_MESSAGES = Integer.getInteger("fix.benchmark.warmup", 10_000);
    public static final int MESSAGES_EXCHANGED = Integer.getInteger("fix.benchmark.messages", 100_000);
    public static final boolean FORCE_WRITES = Boolean.getBoolean(DEBUG_PRINT_MESSAGES_PROPERTY);

    public static final int BACKOFF_SPINS = Integer.getInteger("fix.core.spins", 1_000);
    public static final int BACKOFF_YIELDS = Integer.getInteger("fix.core.yields", 10_000);

    // ------------------------------------------------
    //          Configuration Defaults
    // ------------------------------------------------

    public static final int DEFAULT_INBOUND_MAX_CLAIM_ATTEMPTS = BACKOFF_SPINS + BACKOFF_YIELDS + 1000;
    public static final int DEFAULT_OUTBOUND_MAX_CLAIM_ATTEMPTS = DEFAULT_INBOUND_MAX_CLAIM_ATTEMPTS;

    private static final long DEFAULT_REPLY_TIMEOUT_IN_MS = 10_000L;
    private static final int DEFAULT_ERROR_SLOT_SIZE = 1024;

    private SessionIdStrategy sessionIdStrategy = new SenderAndTargetSessionIdStrategy();
    private int monitoringBuffersLength = getInteger(MONITORING_BUFFERS_LENGTH_PROPERTY, DEFAULT_MONITORING_BUFFER_LENGTH);
    private String monitoringFile = null;
    private String aeronChannel = null;
    private long replyTimeoutInMs = DEFAULT_REPLY_TIMEOUT_IN_MS;
    private Aeron.Context aeronContext = new Aeron.Context();
    private int errorSlotSize = DEFAULT_ERROR_SLOT_SIZE;

    private int inboundMaxClaimAttempts =
        getInteger(INBOUND_MAX_CLAIM_ATTEMPTS_PROPERTY, DEFAULT_INBOUND_MAX_CLAIM_ATTEMPTS);
    private int outboundMaxClaimAttempts =
        getInteger(OUTBOUND_MAX_CLAIM_ATTEMPTS_PROPERTY, DEFAULT_OUTBOUND_MAX_CLAIM_ATTEMPTS);

    /**
     * Sets the session id strategy.
     *
     * @param sessionIdStrategy the session id strategy.
     * @return this
     *
     * @see SessionIdStrategy
     */
    public CommonConfiguration sessionIdStrategy(final SessionIdStrategy sessionIdStrategy)
    {
        this.sessionIdStrategy = sessionIdStrategy;
        return this;
    }

    /**
     * Sets the length of the buffer used for monitoring counters.
     *
     * @param monitoringBuffersLength the length of the buffer used for monitoring counters.
     * @return this
     *
     * @see CommonConfiguration#MONITORING_BUFFERS_LENGTH_PROPERTY
     */
    public CommonConfiguration monitoringBuffersLength(final Integer monitoringBuffersLength)
    {
        this.monitoringBuffersLength = monitoringBuffersLength;
        return this;
    }

    /**
     * Sets the location for the monitoring file.
     *
     * @param monitoringFile the location for the monitoring file.
     * @return this
     *
     * @see CommonConfiguration#MONITORING_FILE_PROPERTY
     */
    public CommonConfiguration monitoringFile(String monitoringFile)
    {
        this.monitoringFile = monitoringFile;
        return this;
    }

    /**
     * Sets the channel used by aeron connections.
     *
     * @param aeronChannel the channel used by aeron connections.
     * @return this
     */
    public CommonConfiguration aeronChannel(final String aeronChannel)
    {
        this.aeronChannel = aeronChannel;
        return this;
    }

    /**
     * Sets the reply timeout in milliseconds.
     * <p>
     * This is the timeout for control protocol messages between the FIX Gateway and FIX Library instances.
     *
     * @param replyTimeoutInMs the reply timeout in milliseconds.
     * @return this
     */
    public CommonConfiguration replyTimeoutInMs(final long replyTimeoutInMs)
    {
        this.replyTimeoutInMs = replyTimeoutInMs;
        return this;
    }

    /**
     * Sets the error slot size. The error slot size is the number of different types of errors that are
     * simultaneously held in the error buffer.
     *
     * @param errorSlotSize the error slot size
     * @return this
     */
    public CommonConfiguration errorSlotSize(final int errorSlotSize)
    {
        this.errorSlotSize = errorSlotSize;
        return this;
    }

    /**
     * Sets the inbound max claim attempts.
     *
     * @param inboundMaxClaimAttempts the inbound max claim attempts
     * @return this
     *
     * @see CommonConfiguration#INBOUND_MAX_CLAIM_ATTEMPTS_PROPERTY
     */
    public CommonConfiguration inboundMaxClaimAttempts(final int inboundMaxClaimAttempts)
    {
        this.inboundMaxClaimAttempts = inboundMaxClaimAttempts;
        return this;
    }

    /**
     * Sets the outbound max claim attempts.
     *
     * @param outboundMaxClaimAttempts the outbound max claim attempts
     * @return this
     *
     * @see CommonConfiguration#OUTBOUND_MAX_CLAIM_ATTEMPTS_PROPERTY
     */
    public CommonConfiguration outboundMaxClaimAttempts(final int outboundMaxClaimAttempts)
    {
        this.outboundMaxClaimAttempts = outboundMaxClaimAttempts;
        return this;
    }

    public Aeron.Context aeronContext()
    {
        return aeronContext;
    }

    public SessionIdStrategy sessionIdStrategy()
    {
        return sessionIdStrategy;
    }

    public int monitoringBuffersLength()
    {
        return monitoringBuffersLength;
    }

    public String monitoringFile()
    {
        return monitoringFile;
    }

    public String aeronChannel()
    {
        return aeronChannel;
    }

    public long replyTimeoutInMs()
    {
        return replyTimeoutInMs;
    }

    public int errorSlotSize()
    {
        return errorSlotSize;
    }

    /**
     * If shared memory is available, use that as a temporary directory,
     * otherwise use the default temp directory
     *
     * @return the optimal temporary directory
     */
    public static String optimalTmpDirName()
    {
        if ("Linux".equalsIgnoreCase(System.getProperty("os.name")))
        {
            final File devShmDir = new File("/dev/shm");

            if (devShmDir.exists())
            {
                return devShmDir.getAbsolutePath();
            }
        }

        return IoUtil.tmpDirName();
    }

    public static IdleStrategy backoffIdleStrategy()
    {
        return new BackoffIdleStrategy(BACKOFF_SPINS, BACKOFF_YIELDS, 1, 1 << 20);
    }

    public int inboundMaxClaimAttempts()
    {
        return inboundMaxClaimAttempts;
    }

    public int outboundMaxClaimAttempts()
    {
        return outboundMaxClaimAttempts;
    }

    protected void conclude(final String fixSuffix)
    {
        if (aeronChannel() == null)
        {
            throw new IllegalArgumentException("Missing required configuration: aeron channel");
        }

        if (monitoringFile() == null)
        {
            monitoringFile(getProperty(MONITORING_FILE_PROPERTY, String.format(DEFAULT_MONITORING_FILE, fixSuffix)));
        }
    }
}
