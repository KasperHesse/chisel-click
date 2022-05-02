package click

import chisel3._
import chisel3.experimental.IO
import chisel3.util._


abstract class DelayElement(delay: Int) extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val reqIn = Input(Bool())
    val reqOut = Output(Bool())
  })
}

class DelayElementSim(delay: Int = 1) extends DelayElement(delay) {
  override val desiredName = s"DelayElementSim_$delay"
  require(delay > 0)
  setInline(s"DelayElementSim_$delay.v",
    s"""module DelayElementSim_$delay (
      | input reqIn,
      | output reg reqOut
      |);
      |always@(reqIn) begin
      | #$delay reqOut <= reqIn;
      |end
      |initial begin
      | reqOut <= 1'b0;
      |end
      |endmodule""".stripMargin)
}

class DelayElementSynth(delay: Int) extends DelayElement(delay) {
  //TODO Define this type of delay element
}

object DelayElement {
  def apply(delay: Int): DelayElement = {
    if(ClickConfig.SIMULATION) new DelayElementSim(delay) else new DelayElementSim(delay)
  }
}