import chisel3._
import chisel3.experimental.ChiselAnnotation
import chisel3.stage.ChiselStage
import click._
import examples.Fifo
import firrtl.annotations.Annotation
import firrtl.{AttributeAnnotation, DescriptionAnnotation, DocStringAnnotation}

object Top extends App {
  def fork(a: UInt): (UInt, UInt) = {
    (a(7,6), a(5,0))
  }

  (new ChiselStage).emitVerilog(Adder(8), Array("-td", "gen", "--emission-options", "disableRegisterRandomization"))
}