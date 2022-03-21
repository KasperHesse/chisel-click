import chisel3._
import chisel3.stage.ChiselStage
import click._

object Top extends App {
  (new ChiselStage).emitVerilog(new HandshakeRegister(UInt(8.W), 0.U(8.W)), Array("-td", "gen"))
}
