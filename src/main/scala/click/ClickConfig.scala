package click

/**
 * A configuration object for use when simulating and synthesizing asynchronous circuits
 * using click elements
 * @param MUX_DELAY Simulation delay of multiplexers
 * @param DEMUX_DELAY Simulation delay of demultiplexers
 * @param REG_DELAY Simulation delay of handshake registers
 * @param ADD_DELAY Simulation delay of adders. Also used in synthesis
 * @param MERGE_DELAY Simulation delay of merge components
 * @param JOIN_DELAY Simulation delay of join components
 * @param FORK_DELAY Simulation delay of fork components
 * @param COMP_DELAY Simulation delay of comparator modules (>, =/=, etc)
 * @param SIMULATION Controls whether to instantiate simulation or synthesis-based delay elements
 */
case class ClickConfig(
                      MUX_DELAY: Int = 3,
                      DEMUX_DELAY: Int = 3,
                      REG_DELAY: Int = 5,
                      ADD_DELAY: Int = 15,
                      MERGE_DELAY: Int = 2,
                      JOIN_DELAY: Int = 2,
                      FORK_DELAY: Int = 2,
                      COMP_DELAY: Int = 2,
                      SIMULATION: Boolean = true
                      )
