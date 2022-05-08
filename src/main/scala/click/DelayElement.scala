package click

import chisel3._
import chisel3.experimental.IO
import chisel3.util._


/**
 * A matched delay element is required when implementing combinational logic circuits in bundle-data asynchronous circuits.
 * The delay element must delay the request signal until the output has been computed.
 * This abstract class defines the interface for delay elements when working with click elements.
 * @param delay The delay of the delay element. Must be greater than zero
 * @throws IllegalArgumentException if delay < 1
 */
abstract class DelayElement(delay: Int) extends BlackBox {
  require(delay > 0)
  val io = IO(new Bundle {
    val reqIn = Input(Bool())
    val reqOut = Output(Bool())
  })
}

/**
 * A delay element for use when simulating circuits. This delay element will *not* serve to delay
 * signals in a synthesized circuit.
 * @param delay The delay of the delay element. Must be greater than zero
 */
class DelayElementSim(delay: Int = 1) extends DelayElement(delay) with HasBlackBoxInline {
  override val desiredName = s"DelayElementSim_$delay"
  setInline(s"DelayElementSim_$delay.v",
    s"""module DelayElementSim_$delay (
      | input reqIn,
      | output reg reqOut
      |);
      |always@(reqIn) begin
      | reqOut <= #$delay reqIn;
      |end
      |initial begin
      | reqOut <= 1'b0;
      |end
      |endmodule""".stripMargin)
}

/**
 * A delay element for use when synthesizing circuits.
 * This delay element will *not* delay signals in a simulated circuit, but will generate
 * circuits with reproducible delay values when implemented on an FPGA.
 * @param delay The delay of the delay element. Must be greater than zero
 */
class DelayElementSynth(delay: Int) extends DelayElement(delay) with HasBlackBoxResource {
  override val desiredName = "delay_element_synth"
  addResource("/delay_element_synth.vhd")
}